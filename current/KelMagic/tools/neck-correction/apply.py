from pathlib import Path
import json,math,re
M=Path(__file__).resolve().parents[2];R=M/'reports/F3-hair7-neck-fix';A=M/'src/main/resources/assets/kelmagic'
orig=R/'input-hair7'
meshes=json.loads((orig/'reports/F3-hair7/mesh.json').read_text());c=math.cos(math.radians(22));s=math.sin(math.radians(22));limit=23.98
# Existing chest-local vertices → bind-space Y. Clip only the portion ABOVE shoulders.
def wy(a):return 23+c*(a[1]-23)+s*(a[2]+1.6)
def clip(poly):
 out=[]
 for a,b in zip(poly,poly[1:]+poly[:1]):
  da,db=wy(a)-limit,wy(b)-limit
  if da<=0:out.append(a)
  if (da<=0)!=(db<=0):
   t=da/(da-db);out.append([aa+(bb-aa)*t for aa,bb in zip(a,b)])
 return out
changed={}
for e in meshes:
 if not e['bone'].startswith('hair6_front_'):continue
 fs=[]
 for q in e['faces']:
  p=clip(q)
  if len(p)<3:continue
  if len(p)==3:p.append(p[-1])
  if len(p)==4:fs.append(p)
  else:
   for i in range(1,len(p)-1):fs.append([p[0],p[i],p[i+1],p[i+1]])
 changed[e['bone']]=(len(e['faces']),len(fs));e['faces']=fs
(R/'mesh.json').write_text(json.dumps(meshes,indent=2))
# Preserve all non-chest mesh data, bones and hair materials exactly.
p=A/'geo/red_mage.geo.json';g=json.loads((orig/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json').read_text());by={b['name']:b for b in g['minecraft:geometry'][0]['bones']}
for e in meshes:
 if e['bone'] not in changed:continue
 by[e['bone']]['cubes']=[{'origin':[0,0,0],'size':[1,1,0],'uv':{'north':{'uv':[0,0],'uv_size':[1024,1024]}}} for _ in e['faces']]
p.write_text(json.dumps(g,ensure_ascii=False,indent=2))
p=M/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java';j=(orig/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java').read_text()
for i,e in enumerate(meshes):
 if e['bone'] not in changed:continue
 q=','.join('{'+','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in face)+'}' for face in e['faces']);method=f' private static float[][][] mesh{i}(){{return new float[][][]{{{q}}};}}'
 j=re.sub(r' private static float\[\]\[\]\[\] mesh'+str(i)+r'\(\).*',lambda m:method,j)
p.write_text(j)
# Low open-front U at the SHOULDER plane, not upright cards next to the head.
# Front ends coincide with the trimmed chest roots. Side returns and back crossbar
# lie across the shoulder/neckline, never rising along cheek/ear/head.
patches=[]
def ribbon(a,b,width,zone,tier,layer):
 dx=b[0]-a[0];dz=b[2]-a[2];l=math.hypot(dx,dz);nx=-dz/l*width/2;nz=dx/l*width/2
 q=[[a[0]+nx,a[1]+layer,a[2]+nz],[a[0]-nx,a[1]+layer,a[2]-nz],[b[0]-nx,b[1]+layer,b[2]-nz],[b[0]+nx,b[1]+layer,b[2]+nz]]
 patches.append({'zone':zone,'tier':tier,'vertices':q})
for tier,width,layer in [('under',1.0,0),('over',.74,.012)]:
 for sign in [1,-1]:
  path=[[sign*3.72,23.98,-4.94],[sign*3.30,24.005,-1.60],[sign*3.15,24.005,2.12]]
  for a,b in zip(path,path[1:]):ribbon(a,b,width,'side',tier,layer)
 ribbon([3.25,24.005,2.12],[-3.25,24.005,2.12],width,'back',tier,layer)
(R/'neck-static-surfaces.json').write_text(json.dumps({'shoulder_y':24,'maximum_y':max(v[1] for p in patches for v in p['vertices']),'shape':'low open-front U from chest roots across shoulder top around neck sides and back; NOT head perimeter','bone_parent':None,'animation_binding':None,'patches':patches},indent=2))
p=M/'src/main/java/dev/kelmagic/client/hair/Hair7Neck.java';j=(orig/'src/main/java/dev/kelmagic/client/hair/Hair7Neck.java').read_text();start=j.index(' private static final Patch[] PATCHES={');end=j.index(' public static void render',start)
a=' private static final Patch[] PATCHES={\n'
for p0 in patches:
 q=','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in p0['vertices']);a+=f' new Patch("{p0["zone"]}_{p0["tier"]}",new float[][]{{{q}}}),\n'
j=j[:start]+a+' };\n'+j[end:]
import hashlib
j=re.sub(r'return "[0-9a-f]{64}";', 'return "'+hashlib.sha256(json.dumps(patches).encode()).hexdigest()+'";',j)
p.write_text(j)
(R/'scope.json').write_text(json.dumps({'changed_chest_groups':changed,'chest_cutoff_bind_y':limit,'shoulder_y':24,'new_neck_max_y':24.017,'new_bones':0,'neck_bone_binding':None,'neck_extra_volume':False},indent=2))
print('Trimmed only above-shoulder chest portions; replaced high cards by low shoulder-level U, 10 flat quads')
