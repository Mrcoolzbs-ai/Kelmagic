from pathlib import Path
import zipfile,hashlib,json,shutil
root=Path(__file__).resolve().parents[2];out=root.parent/'delivery';out.mkdir(exist_ok=True)
jar=root/'build/libs/kelmagic-1.33.0-r12-roots-TEST.jar';target=out/'KelMagic-1.33.0-r12-roots-TEST.jar';shutil.copy2(jar,target)
with zipfile.ZipFile(target) as z:
 names=z.namelist();assert not any('/Qa' in n and n.endswith('.class') for n in names);assert not any(n.startswith(('software/bernie','top/theillusivec4')) for n in names)
 for p in (root/'src/main/resources').rglob('*'):
  if p.is_file():
   rel=p.relative_to(root/'src/main/resources').as_posix();assert z.read(rel)==p.read_bytes(),rel
 for name in ['open','close','travel','hum']:assert f'assets/kelmagic/sounds/threshold/{name}.ogg' in names
 assert 'dev/kelmagic/server/GroveAwakening.class' in names
 for name in ['grove_leaves','threshold_travel','living_fiber','rite_threshold_link']:assert f'data/kelmagic/advancement/{name}.json' not in names
 assert 'data/kelmagic/advancement/word_born_power.json' in names
 assert len([n for n in names if n.startswith('data/kelmagic/advancement/') and n.count('/')==3 and n.endswith('.json')])==45
 for n in names:
  if n.endswith('.json'):json.loads(z.read(n))
files=[]
for folder in ['src','tools','libs','gradle','reports','logs','screenshots']:
 for p in (root/folder).rglob('*'):
  if p.is_file() and '__pycache__' not in p.parts:files.append(p)
for p in root.iterdir():
 if p.is_file() and (p.suffix in ['.md','.txt'] or p.name in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']):files.append(p)
base='KelMagic_R12_Roots_TRANSFER/';zipout=out/'KelMagic_R12_Roots_TRANSFER.zip';manifest=[]
with zipfile.ZipFile(zipout,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for p,rel in [(target,target.name)]+[(p,'project/'+p.relative_to(root).as_posix()) for p in sorted(files)]:
  z.write(p,base+rel);manifest.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+rel)
 for p in ['README-R12-RU.md','FORMULAS-R12.txt','KELMAGIC_HANDOFF.md']:
  z.write(root/p,base+p);manifest.append(hashlib.sha256((root/p).read_bytes()).hexdigest()+'  '+p)
 z.writestr(base+'SHA256SUMS.txt','\n'.join(manifest)+'\n')
with zipfile.ZipFile(zipout) as z:
 assert z.testzip() is None
 for line in z.read(base+'SHA256SUMS.txt').decode().splitlines():
  digest,rel=line.split('  ',1);assert hashlib.sha256(z.read(base+rel)).hexdigest()==digest
for p in [target,zipout]:print(p.name,p.stat().st_size,hashlib.sha256(p.read_bytes()).hexdigest())
print('Verified project files:',len(files),'manifest entries:',len(manifest))
