from pathlib import Path
import zipfile,shutil,hashlib,json
P=Path(__file__).resolve().parents[1];O=Path('/data/release-1.20.0');O.mkdir(exist_ok=True);jar=P/'build/libs/kelmagic-1.20.0.jar'
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None;names=set(z.namelist());assert not any(n.startswith('dev/kelmagic/client/Qa') for n in names);matched=0
 for p in (P/'src/main/resources').rglob('*'):
  if not p.is_file() or p.suffix=='.bbmodel':continue
  name=p.relative_to(P/'src/main/resources').as_posix();assert name in names and z.read(name)==p.read_bytes(),name;matched+=1
 toml=z.read('META-INF/neoforge.mods.toml').decode();assert 'version="1.20.0"' in toml.replace(' ','') and 'modId="geckolib"' in toml.replace(' ','')
 for name,parent in [('magic_torch','minecraft:block/template_torch'),('magic_wall_torch','minecraft:block/template_torch_wall')]:
  model=json.loads(z.read(f'assets/kelmagic/models/block/{name}.json'));assert model['parent']==parent and model['render_type']=='minecraft:cutout'
 for name in ['dev/kelmagic/MagicMascot.class','dev/kelmagic/entity/RedMage.class','dev/kelmagic/client/RedMageGeoModel.class','dev/kelmagic/client/LampRendering.class']:assert name in names
shutil.copy2(jar,O/jar.name);shutil.copy2(P/'libs/geckolib-neoforge-1.21.1-4.9.3.jar',O/'geckolib-neoforge-1.21.1-4.9.3.jar');shutil.copy2(P/'docs/design/MASCOT-1.20.0.md',O/'KelMagic-1.20.0-Guide.md')
# Keep final recovery and verification materials with the source checkpoint.
with zipfile.ZipFile(O/'KelMagic-1.20.0-Source.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(P.rglob('*')):
  rel=p.relative_to(P)
  if set(rel.parts)&{'build','.gradle','.git','run','dist','node_modules','__pycache__','.cache'}:continue
  if p.is_file() and p.suffix!='.pyc':z.write(p,Path('KelMagic')/rel)
with zipfile.ZipFile(O/'KelMagic-1.20.0-Source.zip') as z:assert z.testzip() is None
for f in (P/'dist').glob('*.jar'):f.unlink()
shutil.copy2(jar,P/'dist'/jar.name)
report=f'PASS: JAR CRC; {matched} source resources match byte-for-byte; no developer Qa classes; mod version1.20.0; GeckoLib dependency; fixed floor/wall cutout models.\n'
(O/'VERIFICATION.txt').write_text(report)
(O/'SHA256SUMS.txt').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(report.strip());print('Release JAR bytes:',jar.stat().st_size)
