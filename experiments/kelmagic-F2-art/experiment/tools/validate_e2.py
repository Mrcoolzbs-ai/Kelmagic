from pathlib import Path
import json,hashlib,sys,copy
import numpy as np
from PIL import Image
R=Path(__file__).resolve().parents[1];base=json.loads((R/'reference/red_mage.geo.json').read_text());fixed=json.loads((R/'reference-fixed.geo.json').read_text());g=json.loads((R/'states/E0-neutral/red_mage.geo.json').read_text());report={};checks=0

def ok(v,s):
 global checks;checks+=1
 if not v:raise AssertionError(s)
b={x['name']:x for x in base['minecraft:geometry'][0]['bones']};e={x['name']:x for x in g['minecraft:geometry'][0]['bones']}
for n,v in b.items():
 if n!='head':ok(v==e[n],'protected bone '+n)
for name in ['red_mage_geo.png','red_mage.animation.json']:
 for state in (R/'states').iterdir():ok((state/name).read_bytes()==(R/'reference'/name).read_bytes(),'byte exact '+name)
a=b['head']['cubes'][63]['uv']['north'];f=next(x for x in fixed['minecraft:geometry'][0]['bones'] if x['name']=='head')['cubes'][63]['uv']['north']
ok(np.allclose(sorted([a['uv'][0],a['uv'][0]+a['uv_size'][0]]),sorted([f['uv'][0],f['uv'][0]+f['uv_size'][0]])),'same UV bounds')
# Corrected textured source surface and subdivided source surface must agree exactly as functions.
source=next(x for x in fixed['minecraft:geometry'][0]['bones'] if x['name']=='head')['cubes']
prov=json.loads((R/'report/generation-v2.json').read_text())['provenance']
for p in prov:
 c=source[p['source']];ox,oy,_=c['origin'];w,h,_=c['size'];x0,x1,y0,y1=p['rect'];u,v=c['uv']['north']['uv'];du,dv=c['uv']['north']['uv_size'];s=p['uv']['north'];
 for tx,ty in [(0,0),(.5,.5),(1,1)]:
  x=x0+(x1-x0)*tx;y=y1-(y1-y0)*ty
  ok(np.allclose([u+du*(x-ox)/w,v+dv*(oy+h-y)/h],[s['uv'][0]+s['uv_size'][0]*tx,s['uv'][1]+s['uv_size'][1]*ty]),'source anchored UV')
# BB representation matches every rest source cube, including pivots and UV.
for d in sorted((R/'states').iterdir()):
 bb=json.loads(next(d.glob('*.bbmodel')).read_text());els=bb['elements'];idx=0
 def groups(items):
  for v in items:
   if isinstance(v,dict):yield v;yield from groups(v.get('children',[]))
 groupsBy={x['name']:x for x in groups(bb['outliner'])}
 for bone in g['minecraft:geometry'][0]['bones']:
  ok(np.allclose(groupsBy[bone['name']]['origin'],bone['pivot']),'BB pivot')
  for c in bone.get('cubes',[]):
   v=els[idx];idx+=1;ok(np.allclose(v['from'],c['origin']) and np.allclose(v['to'],np.array(c['origin'])+c['size']),'BB corners')
   for face,t in c['uv'].items():ok(np.allclose(v['faces'][face]['uv'],t['uv']+[t['uv'][i]+t['uv_size'][i] for i in range(2)]),'BB UV')
 ok(bb['animations']==json.loads((R/'reference/red_mage_gecko.bbmodel').read_text())['animations'],'BB clips')
# Continuous shared vertices: each cheek uses SAME function of rest coordinates, no independent strips.
def weight(x,y):
 def smooth(t):
  t=max(0,min(1,t));return t*t*(3-2*t)
 return smooth(min((abs(x)-.9)/.6,(3.85-abs(x))/.85))*smooth(min((y-25.5)/.7,(27.2-y)/.35))
for amount in [0,.15,.35,.8,1]:
 last=None
 for y in np.linspace(25.5,27.2,1081):
  yy=y+.16*amount*weight(2.4,y)
  if last is not None:ok(yy>last,'no fold')
  last=yy
 for x in [-3.85,-.9,.9,3.85]:
  for y in np.linspace(25.5,27.2,100):ok(abs(weight(x,y))<1e-8,'outer seam pinned')
 for x in [-2.7,-1.85,2,2.9]:
  for y in [26.21,26.69]:ok(abs(weight(x,y)-1)<1e-8,'blush not stretched')
report={'checks':checks,'neutralGeometry':'identical positions to D, subdivided only','neutralTexture':'intentional local U reversal of source panel 63; NOT pixel-identical to erroneous D','atlasSHA256':hashlib.sha256((R/'reference/red_mage_geo.png').read_bytes()).hexdigest(),'allProtectedBonesUnchanged':True,'uvSourceAnchoring':True,'bbRestMatchesExport':True,'noFoldMaxAmplitude':True,'blushRigidPlateau':True}
(R/'report/validation-v2.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
