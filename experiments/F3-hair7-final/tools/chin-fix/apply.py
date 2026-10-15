from pathlib import Path
import json,math,re
M=Path(__file__).resolve().parents[2];A=M/'src/main/resources/assets/kelmagic';R=M/'reports/F3-hair7-chin-fix';R.mkdir(exist_ok=True)
p=A/'geo/red_mage.geo.json';baseline=R/'before.geo.json'
if not baseline.exists():baseline.write_bytes(p.read_bytes())
g=json.loads(baseline.read_text());by={b['name']:b for b in g['minecraft:geometry'][0]['bones']}
old_bottom=24.;mouth_bottom=by['mouth_png']['cubes'][0]['origin'][1];chin_height=mouth_bottom-old_bottom;new_bottom=mouth_bottom-.2*chin_height
changes=[]
for i,cube in enumerate(by['head']['cubes']):
 y=cube['origin'][1];h=cube['size'][1]
 if y>=new_bottom:continue
 if h==0:
  cube['origin'][1]=new_bottom
 else:
  top=y+h
  if top<=new_bottom:raise ValueError('unexpected face below chin cutoff')
  cube['origin'][1]=new_bottom;cube['size'][1]=top-new_bottom
  # Retain the top part of the existing skin UV, remove only the lower pixels.
  for face,uv in cube.get('uv',{}).items():
   if face not in ['up','down'] and isinstance(uv,dict):uv['uv_size'][1]*=(top-new_bottom)/h
 changes.append(i)
# Chest: rest shape retained below the first path point. Only root rises toward new jaw edge.
def chestlocal(p):
 x,y,z=p;c=math.cos(math.radians(22));s=math.sin(math.radians(22));dy=y-23;dz=z+1.6;return [x,23+c*dy-s*dz,-1.6+s*dy+c*dz]
def uvq(q,uv=(0,0,1,1)):
 u,v,w,h=uv;return [list(p)+[uu,vv] for p,(uu,vv) in zip(q,[(u,v),(u+w,v),(u+w,v+h),(u,v+h)])]
meshes=json.loads((M/'reports/F3-hair7-neck-fix/mesh.json').read_text());idx={e['bone']:i for i,e in enumerate(meshes)}
path=[(new_bottom,3.75,-3.88,.72),(23.8,3.72,-5.02,.85),(21.15,3.32,-5.72,.92),(19.40,2.35,-4.99,.74),(17.65,1.55,-4.10,.50)]
for side,sign in [('left',1),('right',-1)]:
 vs=[]
 for y,x,z,w in path:vs.append([chestlocal([sign*(x-w*.72),y,z]),chestlocal([sign*(x+w*.72),y,z])])
 fs=[]
 for j in range(len(path)-1):
  a,b=vs[j];c,d=vs[j+1];fa=[a,b,d,c];bk=[[p[0],p[1],p[2]+.24] for p in fa]
  fs += [fa,bk,[fa[0],bk[0],bk[1],fa[1]],[fa[1],bk[1],bk[2],fa[2]],[fa[3],bk[3],bk[2],fa[2]],[fa[0],bk[0],bk[3],fa[3]]]
 meshes[idx['hair6_front_'+side+'_base']]['faces']=[uvq(q) for q in fs]
 for tier,off in [('under',.06),('over',.19)]:
  fs=[]
  for j in range(len(path)-1):
   rows=[]
   for k in [j,j+1]:
    y,x,z,w=path[k];rows.append([chestlocal([sign*(x-w),y,z-off]),chestlocal([sign*(x+w),y,z-off])])
   a,b=rows[0];c,d=rows[1];v0=(path[0][0]-path[j][0])/(path[0][0]-path[-1][0]);v1=(path[0][0]-path[j+1][0])/(path[0][0]-path[-1][0]);fs.append(uvq([a,b,d,c],(0,v0,1,v1-v0)))
  meshes[idx['hair6_front_'+side+'_'+tier]]['faces']=fs
# Existing placeholders only: no bones added.
for e in meshes:
 if not e['bone'].startswith('hair6_front_'):continue
 by[e['bone']]['cubes']=[{'origin':[0,0,0],'size':[1,1,0],'uv':{'north':{'uv':[0,0],'uv_size':[1024,1024]}}} for _ in e['faces']]
p.write_text(json.dumps(g,ensure_ascii=False,indent=2));(R/'mesh.json').write_text(json.dumps(meshes,indent=2))
p=M/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java';saved=R/'before-Hair6Meshes.java'
if not saved.exists():saved.write_bytes(p.read_bytes())
j=saved.read_text()
for i,e in enumerate(meshes):
 if not e['bone'].startswith('hair6_front_'):continue
 q=','.join('{'+','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in face)+'}' for face in e['faces']);method=f' private static float[][][] mesh{i}(){{return new float[][][]{{{q}}};}}'
 j=re.sub(r' private static float\[\]\[\]\[\] mesh'+str(i)+r'\(\).*',lambda m:method,j)
p.write_text(j)
# Move the full static neckline exactly one model pixel above its prior shoulder-level position.
neck=json.loads((M/'reports/F3-hair7-neck-fix/neck-static-surfaces.json').read_text());patches=neck['patches']
for e in patches:
 for v in e['vertices']:
  v[1]+=1
  if v[2]<-3.5:v[2]+=1.06 # front end meets raised chest root, not its old forward-projecting end
neck.update({'shoulder_y':24,'height_above_body_pixels':1,'new_jaw_bottom_y':new_bottom,'patches':patches});(R/'neck-static-surfaces.json').write_text(json.dumps(neck,indent=2))
p=M/'src/main/java/dev/kelmagic/client/hair/Hair7Neck.java';saved=R/'before-Hair7Neck.java'
if not saved.exists():saved.write_bytes(p.read_bytes())
j=saved.read_text();start=j.index(' private static final Patch[] PATCHES={');end=j.index(' public static void render',start);a=' private static final Patch[] PATCHES={\n'
for e in patches:
 q=','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in e['vertices']);a+=f' new Patch("{e["zone"]}_{e["tier"]}",new float[][]{{{q}}}),\n'
import hashlib
j=j[:start]+a+' };\n'+j[end:];j=re.sub(r'return "[0-9a-f]{64}";', 'return "'+hashlib.sha256(json.dumps(patches).encode()).hexdigest()+'";',j);p.write_text(j)
facts={'original_chin_bottom':old_bottom,'chin_top_mouth_PNG_bottom':mouth_bottom,'original_chin_height':chin_height,'new_chin_height':.2*chin_height,'new_head_bottom':new_bottom,'head_surfaces_changed':changes,'eyes_nose_mouth_scaled':False,'neck_vertical_lift_pixels':1,'new_chest_root_y':path[0][0],'chest_parent':'chest_left/right → body → root; no head','new_bones':0};(R/'changes.json').write_text(json.dumps(facts,ensure_ascii=False,indent=2));print(json.dumps(facts,ensure_ascii=False))
