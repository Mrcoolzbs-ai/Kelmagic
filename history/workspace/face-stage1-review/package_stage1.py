from pathlib import Path
import hashlib,json,zipfile,shutil
P=Path('/data/KelMagic');O=Path('/data/release-face-stage1');O.mkdir(exist_ok=True)
B=Path('/data/KelMagic-stage1-rollback/KelMagic-1.23.0-before-face.zip');z=zipfile.ZipFile(B)
oldjar=Path('/data/.agent-service/files/007fed9d-77bd-4070-9135-316f9d6c193b/kelmagic-1.23.0.jar');jar=P/'build/libs/kelmagic-1.23.0.jar'
with zipfile.ZipFile(jar) as j:
 assert j.testzip()is None;assert not any(n.startswith('dev/kelmagic/client/Qa')for n in j.namelist())
 matched=0
 for p in (P/'src/main/resources').rglob('*'):
  if p.is_file() and p.suffix!='.bbmodel':assert j.read(p.relative_to(P/'src/main/resources').as_posix())==p.read_bytes();matched+=1
 assert 'version="1.23.0"'in j.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
with zipfile.ZipFile(oldjar)as j:
 assert j.read('assets/kelmagic/geo/red_mage.geo.json')==z.read('KelMagic/src/main/resources/assets/kelmagic/geo/red_mage.geo.json')
shutil.copy2(jar,O/'kelmagic-1.23.0-face-stage1.jar');shutil.copy2(P/'docs/design/FACE-STAGE1-1.23.0.md',O/'Face-Stage1-Notes.md')
ignore={'build','.gradle','.git','run','dist','node_modules','__pycache__','.cache'}
files=[p for p in sorted(P.rglob('*'))if p.is_file() and not set(p.relative_to(P).parts)&ignore and p.suffix!='.pyc']
original={n.removeprefix('KelMagic/'):z.read(n)for n in z.namelist()if n.startswith('KelMagic/')and not n.endswith('/')}
def sha(b):return hashlib.sha256(b).hexdigest()
changes={p.relative_to(P).as_posix():{'after':sha(p.read_bytes()),'before':sha(original[p.relative_to(P).as_posix()])if p.relative_to(P).as_posix()in original else None}for p in files if p.relative_to(P).as_posix()not in original or p.read_bytes()!=original[p.relative_to(P).as_posix()]}
assert len([v for v in changes.values()if v['before']])==5
with zipfile.ZipFile(O/'KelMagic-1.23.0-FaceStage1-Source.zip','w',zipfile.ZIP_DEFLATED,6)as out:
 for p in files:out.write(p,'KelMagic/'+p.relative_to(P).as_posix())
restore='''from pathlib import Path
import argparse,json,hashlib
ap=argparse.ArgumentParser(description="Static face stage 1 source rollback; dry run unless --apply")
ap.add_argument("--project",type=Path,required=True);ap.add_argument("--apply",action="store_true");a=ap.parse_args()
root=a.project.resolve();here=Path(__file__).resolve().parent;m=json.loads((here/"rollback-manifest.json").read_text())
assert "mod_version=1.23.0" in (root/"gradle.properties").read_text(),"Wrong project version"
for name,v in m["files"].items():
 p=(root/name).resolve();assert p.is_relative_to(root),"Unsafe path"
 if not p.exists() and v["before"] is None:continue
 assert p.is_file()and hashlib.sha256(p.read_bytes()).hexdigest()==v["after"],"Subsequent change detected: "+name
 if v["before"] is not None:assert hashlib.sha256((here/"before"/name).read_bytes()).hexdigest()==v["before"],"Damaged rollback file"
print("PASS: version and every affected file verified")
if not a.apply:print("Dry run only. Repeat with --apply to restore original source.")
else:
 for name,v in m["files"].items():
  p=root/name
  if v["before"] is None:
   if p.exists():p.unlink()
  else:p.write_bytes((here/"before"/name).read_bytes())
 print("Restored original 1.23.0 source. Replace installed candidate JAR with original JAR separately.")
'''
readme='''Откат статического лица, этап 1\n\nПолное исходное состояние сохранено в оригинальном KelMagic-1.23.0-Source.zip.\nЭтот компактный комплект содержит все исходные изменённые файлы и оригинальный JAR.\n\nРаспакуйте комплект в отдельную папку, НЕ поверх проекта.\nПроверка: python RESTORE.py --project /path/to/KelMagic\nПрименение: python RESTORE.py --project /path/to/KelMagic --apply\n\nСкрипт восстанавливает 5 исходных файлов и удаляет добавленные файлы этапа 1.\nПри дальнейших пользовательских изменениях он остановится, ничего не перезаписав.\nДля установленной игры удалите face-stage1 JAR и поставьте kelmagic-1.23.0-original.jar.\nНе держите оба JAR одновременно. GeckoLib не менять.\nГенерируемые build/run и миры скрипт не трогает. После отката исходников пересоберите проект.\n'''
with zipfile.ZipFile(O/'KelMagic-1.23.0-FaceStage1-Rollback.zip','w',zipfile.ZIP_DEFLATED,6)as out:
 for name,v in changes.items():
  if v['before']:out.writestr('before/'+name,original[name])
 out.writestr('rollback-manifest.json',json.dumps({'original_source_sha256':sha(B.read_bytes()),'files':changes},indent=2));out.writestr('RESTORE.py',restore);out.writestr('README.txt',readme);out.write(oldjar,'kelmagic-1.23.0-original.jar')
for f in O.glob('*.zip'):
 with zipfile.ZipFile(f)as out:assert out.testzip()is None
(O/'SHA256SUMS.txt').write_text(''.join(sha(p.read_bytes())+'  '+p.name+'\n'for p in sorted(O.iterdir())if p.is_file()and p.name!='SHA256SUMS.txt'))
print('PASS: JAR CRC,',matched,'resources match source, no developer QA classes, version1.23.0')
print('Modified source files:',5,'; new diagnostic/model files:',sum(v['before']is None for v in changes.values()))
print('Archives CRC verified')
