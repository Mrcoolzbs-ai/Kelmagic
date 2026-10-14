"""Read-only D asset/transform ownership guard. Run from any working directory."""
from pathlib import Path
import hashlib,json,zipfile,sys
P=Path(__file__).resolve().parents[1];R=P.parent;A=P/'src/main/resources/assets/kelmagic'
expected=json.loads((R/'control/approved.sha256.json').read_text())
paths={'red_mage.geo.json':A/'geo/red_mage.geo.json','red_mage_geo.png':A/'textures/entity/red_mage_geo.png','red_mage.animation.json':A/'animations/red_mage.animation.json','KelMagic-D-Directional-Lashes.bbmodel':P/'art/red-mage/red_mage_gecko.bbmodel'}
for name,path in paths.items():
 assert hashlib.sha256(path.read_bytes()).hexdigest()==expected[name], f'APPROVED D ASSET CHANGED: {path}'
clips=json.loads(paths['red_mage.animation.json'].read_text())['animations']
for name,clip in clips.items():
 assert not any(b.startswith('lid_') for b in clip.get('bones',{})),f'COMPETING EYELID CLIP WRITER: {name}'
source=(P/'src/main/java/dev/kelmagic/client/RedMageGeoModel.java').read_text()
assert source.count('bone("lid_"+side')==1,'Expected exactly one eyelid transform writer'
for jar in (P/'build/libs').glob('*.jar'):
 with zipfile.ZipFile(jar) as z:
  assert not any(n.startswith('dev/kelmagic/client/Qa') for n in z.namelist()),'QA class in distributable jar'
  for name,path in list(paths.items())[:3]:
   resource='assets/kelmagic/'+str(path.relative_to(A));assert hashlib.sha256(z.read(resource)).hexdigest()==expected[name],f'JAR resource drift {resource}'
print(f'APPROVED_EYES_GUARD_OK: 4 SHA256, {len(clips)} clips, sole model writer, QA excluded')
