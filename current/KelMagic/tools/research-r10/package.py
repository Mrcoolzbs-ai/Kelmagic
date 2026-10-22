from pathlib import Path
import zipfile,hashlib,json,shutil
root=Path(__file__).resolve().parents[2];out=root.parent/'delivery';out.mkdir(exist_ok=True)
jar=root/'build/libs/kelmagic-1.31.0-r10-dreams-TEST.jar';target=out/'KelMagic-1.31.0-r10-dreams-TEST.jar';shutil.copy2(jar,target)
with zipfile.ZipFile(target) as z:
 assert not any('/Qa' in n and n.endswith('.class') for n in z.namelist()),'QA classes shipped'
 assert not any(n.startswith(('software/bernie','top/theillusivec4')) for n in z.namelist()),'library embedded'
 for name in ['otherworld_dream_bed','magic_grass_block']:
  assert f'assets/kelmagic/blockstates/{name}.json' in z.namelist()
  assert f'data/kelmagic/recipe/{name}.json' in z.namelist()
 for n in z.namelist():
  if n.endswith('.json'):json.loads(z.read(n))
 required=['dev/kelmagic/content/DreamBedBlock.class','dev/kelmagic/content/MidnightTreeFeature.class','dev/kelmagic/client/MagicGrassRenderer.class']
 assert all(n in z.namelist() for n in required)
files=[]
for folder in ['src','tools','libs','gradle','reports','logs','screenshots']:
 for p in (root/folder).rglob('*'):
  if p.is_file() and '__pycache__' not in p.parts:files.append(p)
for name in ['README.md','README-RU.md','README-R10-RU.md','README-R9-RU.md','KELMAGIC_HANDOFF.md','build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']:files.append(root/name)
base='KelMagic_R10_Dreams_TRANSFER/'
zipout=out/'KelMagic_R10_Dreams_TRANSFER.zip';manifest=[]
with zipfile.ZipFile(zipout,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 z.write(target,base+target.name)
 manifest.append(hashlib.sha256(target.read_bytes()).hexdigest()+'  '+target.name)
 for p in sorted(files):
  rel='project/'+p.relative_to(root).as_posix();z.write(p,base+rel);manifest.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+rel)
 z.writestr(base+'SHA256SUMS.txt','\n'.join(manifest)+'\n')
with zipfile.ZipFile(zipout) as z:assert z.testzip() is None
for p in [target,zipout]:print(p.name,p.stat().st_size,hashlib.sha256(p.read_bytes()).hexdigest())
print('Project files:',len(files))
