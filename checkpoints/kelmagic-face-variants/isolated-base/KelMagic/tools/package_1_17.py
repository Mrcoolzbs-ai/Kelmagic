from pathlib import Path
import zipfile,hashlib,shutil,json
P=Path(__file__).resolve().parents[1];OUT=Path('/data/release-1.17.0');OUT.mkdir(exist_ok=True)
jar=P/'build/libs/kelmagic-1.17.0.jar';shutil.copy2(jar,OUT/jar.name)
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 names=set(z.namelist());assert not any('dev/kelmagic/client/Qa' in n for n in names)
 matched=0
 for p in (P/'src/main/resources').rglob('*'):
  if not p.is_file() or p.suffix=='.bbmodel':continue
  name=p.relative_to(P/'src/main/resources').as_posix();assert name in names,name;assert z.read(name)==p.read_bytes(),name;matched+=1
 assert 'version="1.17.0"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ','')
 assert len(json.loads(z.read('data/kelmagic/research/catalog.json'))['nodes'])==31
for old in (P/'dist').glob('kelmagic-*.jar'):old.unlink()
shutil.copy2(jar,P/'dist'/jar.name)
for src,dest in [('START_HERE.md','KelMagic-1.17.0-READ-ME.md'),('COMMANDS.md','KelMagic-1.17.0-COMMANDS.md'),('docs/design/RESEARCH-1.17.0.md','KelMagic-1.17.0-Research-Guide.md')]:shutil.copy2(P/src,OUT/dest)
world=P/'run/saves/KelMagicResearch117'
with zipfile.ZipFile(OUT/'KelMagic-1.17.0-Test-World.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(world.rglob('*')):
  if p.is_file() and p.name not in ['session.lock','level.dat_old']:z.write(p,Path(world.name)/p.relative_to(world))
(P/'docs/architecture/verification-logs/PACKAGE-1.17.0.txt').write_text(f'JAR CRC PASS; {matched} resources match; no Qa classes; catalog 31 nodes\nWorld ZIP CRC checked below. Final output hashes: release SHA256SUMS.txt\n')
with zipfile.ZipFile(OUT/'KelMagic-1.17.0-Source.zip','w',zipfile.ZIP_DEFLATED,6) as z:
 for p in sorted(P.rglob('*')):
  rel=p.relative_to(P)
  if set(rel.parts)&{'build','.gradle','.git','run','node_modules','__pycache__','.cache'}:continue
  if not p.is_file() or p.name.endswith('.pyc'):continue
  if any(part.startswith('native-') and part!='native-research-1.17.0' for part in rel.parts[:-1]):continue
  z.write(p,Path('KelMagic')/rel)
for p in OUT.glob('*.zip'):
 with zipfile.ZipFile(p) as z:assert z.testzip() is None
checks=''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(OUT.iterdir()) if p.is_file() and p.suffix!='.txt')
(OUT/'SHA256SUMS.txt').write_text(checks);(P/'dist/SHA256SUMS.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest()+'  '+jar.name+'\n')
(P/'docs/architecture/verification-logs/PACKAGE-1.17.0.txt').write_text(f'JAR CRC PASS; {matched} resources match; no Qa classes; catalog 31 nodes\nZIP CRC PASS\n'+checks)
print('PASS:',matched,'resource byte matches; JAR + ZIP CRC; Qa excluded')
print('PACKAGED:',[(p.name,p.stat().st_size) for p in sorted(OUT.iterdir())])
