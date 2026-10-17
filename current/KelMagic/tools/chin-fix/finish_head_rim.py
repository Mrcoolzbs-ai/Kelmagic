from pathlib import Path
import json,re
M=Path(__file__).resolve().parents[2];R=M/'reports/F3-hair7-chin-fix';floor=json.loads((R/'changes.json').read_text())['new_head_bottom'];meshes=json.loads((R/'mesh.json').read_text());changes=[]
def hull(points):
 p=sorted(set((round(v[0],7),round(v[2],7)) for v in points))
 if len(p)<3:return []
 def cross(a,b,c):return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
 a=[];b=[]
 for v in p:
  while len(a)>1 and cross(a[-2],a[-1],v)<=0:a.pop()
  a.append(v)
 for v in reversed(p):
  while len(b)>1 and cross(b[-2],b[-1],v)<=0:b.pop()
  b.append(v)
 return [[x,floor,z,0,0] for x,z in a[:-1]+b[:-1]]
for e in meshes:
 if e['parent']!='head':continue
 out=[];cuts=[];original=e['faces']
 for q in original:
  p=[]
  for a,b in zip(q,q[1:]+q[:1]):
   da,db=a[1]-floor,b[1]-floor
   if da>=-1e-7:p.append(a)
   if (da>=0)!=(db>=0):
    t=da/(da-db);v=[aa+(bb-aa)*t for aa,bb in zip(a,b)];v[1]=floor;p.append(v);cuts.append(v)
  # Remove repeated vertices introduced by triangle-as-quad placeholders.
  p=[v for i,v in enumerate(p) if i==0 or sum(abs(a-b) for a,b in zip(v[:3],p[i-1][:3]))>1e-7]
  if len(p)>2 and sum(abs(a-b) for a,b in zip(p[0][:3],p[-1][:3]))<1e-7:p.pop()
  if len(p)<3:continue
  if len(p)==3:p.append(p[-1])
  if len(p)==4:out.append(p)
  else:
   for i in range(1,len(p)-1):out.append([p[0],p[i],p[i+1],p[i+1]])
 if cuts and e['solid']:
  cap=hull(cuts)
  for i in range(1,len(cap)-1):out.append([cap[0],cap[i],cap[i+1],cap[i+1]])
 if out!=original:e['faces']=out;changes.append(e['bone'])
(R/'mesh.json').write_text(json.dumps(meshes,indent=2));P=M/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json';g=json.loads(P.read_text());by={b['name']:b for b in g['minecraft:geometry'][0]['bones']}
for e in meshes:
 if e['bone'] in changes:by[e['bone']]['cubes']=[{'origin':[0,0,0],'size':[1,1,0],'uv':{'north':{'uv':[0,0],'uv_size':[1024,1024]}}} for _ in e['faces']]
# The few retained hair4 crown/bang primitives are above the requested cut already.
P.write_text(json.dumps(g,ensure_ascii=False,indent=2));P=M/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java';s=P.read_text()
for i,e in enumerate(meshes):
 if e['bone'] not in changes:continue
 q=','.join('{'+','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in face)+'}' for face in e['faces']);method=f' private static float[][][] mesh{i}(){{return new float[][][]{{{q}}};}}'
 s=re.sub(r' private static float\[\]\[\]\[\] mesh'+str(i)+r'\(\).*',lambda m:method,s)
P.write_text(s);(R/'lower-head-rim.json').write_text(json.dumps({'cutoff_y':floor,'affected_mesh_groups':changes,'scope':'only narrow lower strip of head-owned side/nape hair matching user green mark; upper hair and long back hair unchanged'},ensure_ascii=False,indent=2));print('Trimmed lower head-hair strip:',len(changes),'groups')
