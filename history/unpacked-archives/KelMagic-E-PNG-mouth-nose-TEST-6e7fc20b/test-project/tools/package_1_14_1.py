from pathlib import Path
import zipfile,hashlib,json,shutil
P=Path(__file__).resolve().parents[1];OUT=Path('/data/release-1.14.1');OUT.mkdir(exist_ok=True)
jar=P/'build/libs/kelmagic-1.14.1.jar';shutil.copy2(jar,OUT/jar.name)
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 names=z.namelist();assert not any('dev/kelmagic/client/Qa' in n for n in names)
 matched=0
 for p in (P/'src/main/resources').rglob('*'):
  if not p.is_file() or p.suffix=='.bbmodel':continue
  name=p.relative_to(P/'src/main/resources').as_posix()
  if name in names:assert z.read(name)==p.read_bytes(),name;matched+=1
  else:raise AssertionError('resource missing:'+name)
 assert 'version="1.14.1"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
 print('JAR CRC/Qa exclusion/resource byte equality PASSED:',matched,'resources;',len(names),'entries')
for old in (P/'dist').glob('kelmagic-*.jar'):old.unlink()
shutil.copy2(jar,P/'dist'/jar.name)
shutil.copy2(P/'docs/design/RESEARCH-PROPOSAL.md',OUT/'KelMagic-Research-Proposal.md');shutil.copy2(P/'START_HERE.md',OUT/'KelMagic-1.14.1-READ-ME.md');shutil.copy2(P/'COMMANDS.md',OUT/'KelMagic-1.14.1-COMMANDS.md')
world=P/'run/saves/KelMagicShowcase1141'
with zipfile.ZipFile(OUT/'KelMagic-1.14.1-Test-World.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(world.rglob('*')):
  if p.is_file() and p.name not in ['session.lock','level.dat_old']:z.write(p,Path(world.name)/p.relative_to(world))
with zipfile.ZipFile(OUT/'KelMagic-1.14.1-Source.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(P.rglob('*')):
  rel=p.relative_to(P)
  if set(rel.parts)&{'build','.gradle','.git','run','node_modules','__pycache__','.cache'}:continue
  if not p.is_file() or p.name.endswith('.pyc'):continue
  z.write(p,Path('KelMagic')/rel)
for p in OUT.glob('*.zip'):
 with zipfile.ZipFile(p) as z:assert z.testzip() is None
checks=''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(OUT.iterdir()) if p.is_file() and p.suffix!='.txt')
(OUT/'SHA256SUMS.txt').write_text(checks);(P/'dist/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest()+'  '+jar.name+'\n')
(P/'docs/architecture/verification-logs/PACKAGE-1.14.1.txt').write_text(f'JAR CRC PASS; {matched} resources match; no Qa classes\nZIP CRC PASS\n'+checks)
print('PACKAGED:',[(p.name,p.stat().st_size) for p in sorted(OUT.iterdir())])
