from pathlib import Path
import zipfile,json,shutil,hashlib
P=Path('/data/KelMagic');D=P/'docs/architecture';log=Path('/data/test110-final2.log').read_text();assets=Path('/data/assets110-final.log').read_text();client=Path('/data/client110-qa4.log').read_text()
assert 'All 132 required tests passed' in log and 'CORE TESTS PASSED: 66124 checks' in log and 'BUILD SUCCESSFUL' in log
assert 'RESOURCES PASSED: 7718' in assets and 'KELMAGIC_NATIVE_QA_COMPLETE_29' in client and 'BUILD SUCCESSFUL' in client
jar=P/'build/libs/kelmagic-1.10.0.jar'
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 assert '1.10.0' in z.read('META-INF/neoforge.mods.toml').decode()
 assert not any('Qa110' in n for n in z.namelist())
 for name in ['mixin/SpectralLocalPlayerMixin','mixin/SpectralElytraLayerMixin','mixin/MagicTrimMixin','client/LampRendering','server/MagicCommands','server/ManaTideEvents','MagicPortals$GateEntity']:
  assert 'dev/kelmagic/'+name+'.class' in z.namelist(),name
 for p in (P/'src/main/resources').rglob('*'):
  if p.is_file():assert z.read(str(p.relative_to(P/'src/main/resources')))==p.read_bytes(),str(p)
 assert len([n for n in z.namelist() if n.startswith('data/kelmagic/advancement/') and n.endswith('.json') and '/recipes/' not in n])==33
 assert len([n for n in z.namelist() if n.startswith('assets/kelmagic/textures/trims/') and n.endswith('.png')])==1440
 assert len([n for n in z.namelist() if n.startswith('data/kelmagic/structure/archive/') and n.endswith('.nbt')])==10
assert 'BUILD SUCCESSFUL' in Path('/data/client110-flight2.log').read_text()
sha=hashlib.sha256(jar.read_bytes()).hexdigest()
proof=D/'native-captures-1.10.0';proof.mkdir(exist_ok=True)
files=sorted(Path('/data/native-qa-1.10/screenshots').glob('scene-*.png'));assert len(files)==29
for p in files:shutil.copy2(p,proof/p.name)
for source,target in [('/data/test110-final2.log','TEST-1.10.0.log'),('/data/assets110-final.log','ASSETS-1.10.0.log'),('/data/client110-qa4.log','CLIENT-NATIVE-1.10.0.log'),('/data/client110-flight2.log','CLIENT-FLIGHT-1.10.0.log')]:shutil.copy2(source,D/target)
(D/'RELEASE-1.10.0.md').write_bytes((P/'START_HERE.md').read_bytes())
(D/'VERIFICATION-1.10.0.md').write_text(Path('/data/verification110.md').read_text()+'\nJAR SHA256: `'+sha+'`\n')
for p in (P/'dist').glob('*.jar'):p.unlink()
shutil.copy2(jar,P/'dist'/jar.name);(P/'dist/SHA256SUMS.txt').write_text(sha+'  '+jar.name+'\n')
archive=Path('/data/KelMagic-source-1.10.0.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for p in sorted(P.rglob('*')):
  if not p.is_file():continue
  rel=p.relative_to(P)
  if rel.parts[0] in {'build','.gradle','run','.git'} or '__pycache__' in rel.parts or '.cache' in rel.parts:continue
  z.write(p,'KelMagic/'+str(rel))
with zipfile.ZipFile(archive) as z:
 assert z.testzip() is None
 assert z.read('KelMagic/dist/'+jar.name)==jar.read_bytes()
 assert len(json.loads(z.read('KelMagic/docs/kel-language/CATALOG-1.10.0.json')))==180
 assert z.read('KelMagic/gradle/wrapper/gradle-wrapper.jar')
print('RELEASE VERIFIED:',jar.stat().st_size,'bytes JAR;',archive.stat().st_size,'bytes source ZIP;',sha)
