from pathlib import Path
import zipfile,shutil,hashlib,json
P=Path(__file__).resolve().parents[1];O=Path('/data/release-1.18.0');O.mkdir(exist_ok=True);jar=P/'build/libs/kelmagic-1.18.0.jar';shutil.copy2(jar,O/jar.name)
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None;names=set(z.namelist());assert not any('dev/kelmagic/client/Qa' in n for n in names);matched=0
 for p in (P/'src/main/resources').rglob('*'):
  if not p.is_file() or p.suffix=='.bbmodel':continue
  name=p.relative_to(P/'src/main/resources').as_posix();assert name in names and z.read(name)==p.read_bytes(),name;matched+=1
 assert 'version="1.18.0"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
 assert 'dev/kelmagic/resonance/ResonanceEntity.class' in names
for f in (P/'dist').glob('*.jar'):f.unlink()
shutil.copy2(jar,P/'dist'/jar.name)
shutil.copy2(P/'START_HERE.md',O/'KelMagic-1.18.0-Guide.md')
w=P/'run/saves/KelMagicResearch118'
with zipfile.ZipFile(O/'KelMagic-1.18.0-Test-World.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(w.rglob('*')):
  if p.is_file() and p.name not in ['session.lock','level.dat_old']:z.write(p,Path(w.name)/p.relative_to(w))
with zipfile.ZipFile(O/'KelMagic-1.18.0-Red-Mage-Model.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted((P/'art/red-mage').glob('*')):
  if p.is_file():z.write(p,Path('Red-Mage')/p.name)
(P/'docs/architecture/verification-logs/PACKAGE-1.18.0.txt').write_text(f'PASS: JAR CRC, {matched} resources byte match; no Qa; current version and entity classes. ZIP CRC and hashes: release SHA256SUMS.txt.\n')
with zipfile.ZipFile(O/'KelMagic-1.18.0-Source.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(P.rglob('*')):
  rel=p.relative_to(P)
  if set(rel.parts)&{'build','.gradle','.git','run','dist','node_modules','__pycache__','.cache'}:continue
  if not p.is_file() or p.name.endswith('.pyc'):continue
  if any(part.startswith('native-') and part!='native-resonance-1.18.0' for part in rel.parts[:-1]):continue
  z.write(p,Path('KelMagic')/rel)
for p in O.glob('*.zip'):
 with zipfile.ZipFile(p) as z:assert z.testzip() is None
(O/'SHA256SUMS.txt').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(O.iterdir()) if p.is_file() and p.suffix!='.txt'))
(P/'dist/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest()+'  '+jar.name+'\n')
print(f'PASS: {matched} resource byte matches; all CRC; no Qa; version1.18')
print('FILES:',[(p.name,p.stat().st_size) for p in O.iterdir()])
