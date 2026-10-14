from pathlib import Path
import json,zipfile
from PIL import Image
P=Path(__file__).resolve().parents[1];A=P/'src/main/resources/assets/kelmagic'
g=json.loads((A/'geo/red_mage.geo.json').read_text())['minecraft:geometry'][0];bones=g['bones'];names={b['name'] for b in bones};assert len(bones)==len(names)==53
parent={b['name']:b.get('parent') for b in bones}
for b in bones:
 seen=set();n=b['name']
 while n is not None:
  assert n in parent and n not in seen;seen.add(n);n=parent[n]
 for c in b.get('cubes',[]):assert all(v>0 for v in c['size'])
assert parent['head']=='body' and parent['hat']=='head' and parent['left_arm']=='body'
a=json.loads((A/'animations/red_mage.animation.json').read_text())['animations'];assert len(a)==29
assert sum(n.startswith('animation.red_mage.throne_') for n in a)==10
for n,v in a.items():
 assert v['bones'] and set(v['bones'])<=names and v['animation_length']>0
 for tracks in v['bones'].values():
  for vals in tracks.values():
   if isinstance(vals,dict):assert all(0<=float(t)<=v['animation_length'] for t in vals)
assert Image.open(A/'textures/entity/red_mage_geo.png').size==(256,256)
t=json.loads((A/'blockstates/mage_throne.json').read_text())['variants'];assert len(t)==8
for facing in ['north','south','east','west']:
 for half in ['lower','upper']:assert t[f'facing={facing},half={half}']['model']==f'kelmagic:block/mage_throne_{half}'
for half in ['lower','upper']:
 m=json.loads((A/f'models/block/mage_throne_{half}.json').read_text());assert len(m['elements'])>=10
 for e in m['elements']:assert 0<=e['from'][1]<=e['to'][1]<=16
with zipfile.ZipFile(P/'libs/geckolib-neoforge-1.21.1-4.9.3.jar') as z:assert z.testzip() is None
exec((P/'tools/verify_torch_rendering.py').read_text())
print('PASS: 53 valid bones, 29 keyed animations, 10 throne scenes, 8 two-block variants, GeckoLib CRC, fixed legacy/transformed lights')
