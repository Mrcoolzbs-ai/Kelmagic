from pathlib import Path
import json,math
from PIL import Image
P=Path(__file__).resolve().parents[1];A=P/'src/main/resources/assets/kelmagic';geo=json.loads((A/'geo/red_mage.geo.json').read_text())['minecraft:geometry'][0];bones={b['name']:b for b in geo['bones']};assert len(bones)==64
assert bones['hair_back']['parent']=='head'
atlas=Image.open(A/'textures/entity/red_mage_geo.png');assert atlas.size==(1024,1024)
for b in bones.values():
 for c in b.get('cubes',[]):
  assert isinstance(c['uv'],dict)
  for f,a in c['uv'].items():
   u,v=a['uv'];w,h=a['uv_size'];assert all(float(x).is_integer() for x in [u,v,w,h])
   assert w>0 and h>0 and 0<=u<u+w<=1024 and 0<=v<v+h<=1024
   assert set(atlas.crop((u,v,u+w,v+h)).getchannel('A').get_flattened_data())=={255},(b['name'],f)
for n in ['eye_left','eye_right','pupil_left','pupil_right','brow_left','brow_right','lid_left','lid_right','mouth','lower_lip']:assert bones[n].get('cubes'),n
for side in ['left','right']:
 assert bones['tunic_'+side]['parent']==side+'_leg'
 assert bones['chest_'+side]['rotation'][0]>=20
 assert bones['chest_'+side]['cubes'][0]['size'][1]>=4.8
for top,low in [('hair_back','hair_mid'),('hair_mid','hair_tip')]:
 c=bones[top]['cubes'][0];d=bones[low]['cubes'][0];assert abs(c['origin'][1]-d['origin'][1]-d['size'][1])<.1
assert bones['hair_mid']['parent']=='hair_back' and bones['hair_tip']['parent']=='hair_mid'
for i in range(4):
 for j in range(2):assert (A/f'textures/entity/red_mage_face_{i}_{j}.png').exists()
for side in ['left','right']:
 upper=bones[side+'_arm']['cubes'][0];low=bones[side+'_forearm']['cubes'][0];assert low['size'][0]<upper['size'][0] and low['size'][2]<upper['size'][2] and low['origin'][1]+low['size'][1]>upper['origin'][1]
 leg=bones[side+'_leg']['cubes'][0];shin=bones[side+'_shin']['cubes'][0];assert shin['size'][0]<leg['size'][0] and shin['size'][2]<leg['size'][2] and shin['origin'][1]+shin['size'][1]>leg['origin'][1]
 cuff=bones[side+'_boot']['cubes'][0];assert cuff['origin'][1]+cuff['size'][1]<6
 assert bones['hair_front_'+side]['cubes'][0]['size'][1]>=5.6 and bones['hair_end_'+side]['parent']=='hair_front_'+side
left=bones['left_leg']['cubes'][0];right=bones['right_leg']['cubes'][0];assert left['origin'][0]>right['origin'][0]+right['size'][0]
a=json.loads((A/'animations/red_mage.animation.json').read_text())['animations'];assert len(a)==29
for n,anim in a.items():
 if n.endswith('read'):assert anim['bones']['book']['scale']==[1,1,1]
 if n.startswith('animation.red_mage.throne_') and not n.endswith('kitten'):
  y=anim['bones']['root']['position'][1];assert 12+y-2>9
# The throne is a surfaced CSG mesh: no hidden coincident faces at half seams.
faces=[]
for half in ['lower','upper']:
 m=json.loads((A/f'models/block/mage_throne_{half}.json').read_text())
 for e in m['elements']:
  assert len(e['faces'])==1;f=next(iter(e['faces']));ax=0 if f in ['east','west'] else 1 if f in ['up','down'] else 2
  lo=e['from'].copy();hi=e['to'].copy();assert lo[ax]==hi[ax]
  if half=='upper':lo[1]+=16;hi[1]+=16
  axes=[k for k in range(3) if k!=ax];plane=round(lo[ax],6)
  for a,p,l,h in faces:
   if a==ax and p==plane:assert not all(min(hi[k],h[k])-max(lo[k],l[k])>1e-7 for k in axes)
  faces.append((ax,plane,lo,hi))
exec((P/'tools/verify_torch_rendering.py').read_text())
print('PASS:64 bones;29 clips;fully opaque integer UVs;recessed expressive face;head-owned contiguous hair;hanging chest;covered thigh caps;per-scene book;throne CSG union')
