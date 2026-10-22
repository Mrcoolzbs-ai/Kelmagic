from pathlib import Path
import zipfile,hashlib,json,shutil
root=Path(__file__).resolve().parents[2]
out=root.parent/'delivery';out.mkdir(exist_ok=True)
jar=root/'build/libs/kelmagic-1.34.0-r13-grove-TEST.jar'
target=out/'KelMagic-1.34.0-r13-grove-TEST.jar';shutil.copy2(jar,target)
with zipfile.ZipFile(target) as z:
 names=z.namelist();assert z.testzip() is None
 assert not any('/Qa' in n and n.endswith('.class') for n in names)
 assert not any(n.startswith(('software/bernie','top/theillusivec4')) for n in names)
 for p in (root/'src/main/resources').rglob('*'):
  if p.is_file():
   rel=p.relative_to(root/'src/main/resources').as_posix();assert z.read(rel)==p.read_bytes(),rel
 for cls in ['dev/kelmagic/MagicApprovedContent.class','dev/kelmagic/client/MagicGrassRenderer.class','dev/kelmagic/server/GroveAwakening.class']:
  assert cls in names
 for n in names:
  if n.endswith('.json'):json.loads(z.read(n))
files=[]
for folder in ['src','tools','libs','gradle','qa','reports','logs','screenshots']:
 d=root/folder
 if not d.exists():continue
 for p in d.rglob('*'):
  if p.is_file() and '__pycache__' not in p.parts and not p.name.endswith(('.pyc','.lock')):files.append(p)
for p in root.iterdir():
 if p.is_file() and (p.suffix in ['.md','.txt'] or p.name in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']):files.append(p)
files=sorted(set(files))
base='KelMagic_R13_Grove_TRANSFER/'
zipout=out/'KelMagic_R13_Grove_TRANSFER.zip';manifest=[]
with zipfile.ZipFile(zipout,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 entries=[(target,target.name)]+[(p,'project/'+p.relative_to(root).as_posix()) for p in files]
 for p,rel in entries:
  z.write(p,base+rel);manifest.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+rel)
 for name in ['README-R13-RU.md','QA-R13.md','APPROVED-COVERAGE-R13.md','KELMAGIC_HANDOFF.md']:
  p=root/name;z.write(p,base+name);manifest.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+name)
 z.writestr(base+'SHA256SUMS.txt','\n'.join(manifest)+'\n')
with zipfile.ZipFile(zipout) as z:
 assert z.testzip() is None
 lines=z.read(base+'SHA256SUMS.txt').decode().splitlines()
 for line in lines:
  digest,rel=line.split('  ',1);assert hashlib.sha256(z.read(base+rel)).hexdigest()==digest
sums=[]
for p in [target,zipout]:
 h=hashlib.sha256(p.read_bytes()).hexdigest();sums.append(f'{h}  {p.name}');print(p.name,p.stat().st_size,h)
(out/'SHA256SUMS-R13.txt').write_text('\n'.join(sums)+'\n')
print('Verified project files:',len(files),'manifest entries:',len(manifest))
