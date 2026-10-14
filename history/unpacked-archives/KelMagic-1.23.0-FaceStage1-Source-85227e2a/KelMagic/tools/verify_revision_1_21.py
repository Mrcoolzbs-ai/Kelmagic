from pathlib import Path
import json
P=Path(__file__).resolve().parents[1];A=P/'src/main/resources/assets/kelmagic'
g=json.loads((A/'geo/red_mage.geo.json').read_text())['minecraft:geometry'][0];b={x['name']:x for x in g['bones']};assert len(b)==53 and not b['hair_crown'].get('cubes')
assert b['pupil_left']['cubes'][0]['uv']!=b['pupil_right']['cubes'][0]['uv']
assert isinstance(b['book_page']['cubes'][0]['uv'],dict) and b['book_page']['cubes'][0]['uv']['up']['uv_size']==[64,32]
for side in ['left','right']:assert b[side+'_arm']['cubes'][0]['uv']['down']['uv']==[232,96]
a=json.loads((A/'animations/red_mage.animation.json').read_text())['animations'];assert len(a)==29
for name,v in a.items():
 assert v['bones'] and set(v['bones'])<=set(b)
 for tracks in v['bones'].values():
  for val in tracks.values():
   if isinstance(val,dict):assert all(0<=float(t)<=v['animation_length'] for t in val)
 if name.startswith('animation.red_mage.throne_') and not name.endswith('kitten'):
  root=v['bones']['root']['position'];assert root[1]==-.45 and -3<=root[2]<=0
  # Thigh underside must clear the cushion (seat top Y=9).
  assert 12+root[1]-2>9
faces=[]
for half in ['lower','upper']:
 m=json.loads((A/f'models/block/mage_throne_{half}.json').read_text());assert len(m['elements'])>50
 for e in m['elements']:
  assert len(e['faces'])==1;face=next(iter(e['faces']));axis=0 if face in ['east','west'] else 1 if face in ['up','down'] else 2
  lo=e['from'].copy();hi=e['to'].copy();assert lo[axis]==hi[axis]
  if half=='upper':lo[1]+=16;hi[1]+=16
  plane=round(lo[axis],6);other=[k for k in range(3) if k!=axis]
  for prev_axis,prev_plane,pl,ph in faces:
   if axis==prev_axis and plane==prev_plane:assert not all(min(hi[k],ph[k])-max(lo[k],pl[k])>1e-7 for k in other),'coplanar overlapping throne surfaces'
  faces.append((axis,plane,lo,hi))
exec((P/'tools/verify_torch_rendering.py').read_text())
print('PASS: 53 bones,29 clips,skin elbow caps,heterochromia,Kel glyph pages,seated clearance,zero coplanar overlapping throne faces')

# Thin box UVs must not sample transparent atlas borders (stochastic alpha holes).
from PIL import Image
tex=Image.open(P/"src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png") if "P" in globals() else Image.open("src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png")
assert set(tex.crop((192,96,200,104)).getchannel("A").getdata())=={255}
print("PASS: opaque explicit braid UV patch")
