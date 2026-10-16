from pathlib import Path
import json,math,re
M=Path(__file__).resolve().parents[2];R=M/'reports/F3-hair7-direct-locks';R.mkdir(exist_ok=True)
base=R/'before-mesh.json'
if not base.exists():base.write_bytes((M/'reports/F3-hair7-head-seat/mesh.json').read_bytes())
meshes=json.loads(base.read_text());by={e['bone']:e for e in meshes};P=M/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json';saved=R/'before.geo.json'
if not saved.exists():saved.write_bytes(P.read_bytes())
g=json.loads(saved.read_text());bones={b['name']:b for b in g['minecraft:geometry'][0]['bones']};c=math.cos(math.radians(22));s=math.sin(math.radians(22))
# Existing chest cuboid: actual rest-space top/front/outside corner, not a guessed shoulder point.
chest=bones['chest_left'];cube=chest['cubes'][0];outer_x=cube['origin'][0]+cube['size'][0];y0=cube['origin'][1]+cube['size'][1];z0=cube['origin'][2];cy=23+c*(y0-23)+s*(z0+1.6);cz=-1.6-s*(y0-23)+c*(z0+1.6)
# ONE segment from chin edge to chest corner. The intermediate shelf/upgoing segment is deleted.
path=[(24.,3.75,-3.88,.72),(cy,outer_x,cz,.92),(19.40,2.35,-4.99,.74),(17.65,1.55,-4.10,.50)]
def local(p):
 x,y,z=p;dy=y-23;dz=z+1.6;return [x,23+c*dy-s*dz,-1.6+s*dy+c*dz]
def uvq(q,uv=(0,0,1,1)):
 u,v,w,h=uv;return [list(p)+[uu,vv] for p,(uu,vv) in zip(q,[(u,v),(u+w,v),(u+w,v+h),(u,v+h)])]
for side,sign in [('left',1),('right',-1)]:
 vs=[[local([sign*(x-w*.72),y,z]),local([sign*(x+w*.72),y,z])] for y,x,z,w in path];fs=[]
 for j in range(len(path)-1):
  a,b=vs[j];d,e=vs[j+1];fa=[a,b,e,d];bk=[[p[0],p[1],p[2]+.24] for p in fa];fs += [fa,bk,[fa[0],bk[0],bk[1],fa[1]],[fa[1],bk[1],bk[2],fa[2]],[fa[3],bk[3],bk[2],fa[2]],[fa[0],bk[0],bk[3],fa[3]]]
 by['hair6_front_'+side+'_base']['faces']=[uvq(q) for q in fs]
 for tier,off in [('under',.06),('over',.19)]:
  fs=[]
  for j in range(len(path)-1):
   rows=[]
   for k in [j,j+1]:
    y,x,z,w=path[k];rows.append([local([sign*(x-w),y,z-off]),local([sign*(x+w),y,z-off])])
   a,b=rows[0];d,e=rows[1];v0=(path[0][0]-path[j][0])/(path[0][0]-path[-1][0]);v1=(path[0][0]-path[j+1][0])/(path[0][0]-path[-1][0]);fs.append(uvq([a,b,e,d],(0,v0,1,v1-v0)))
  by['hair6_front_'+side+'_'+tier]['faces']=fs
for e in meshes:
 if not e['bone'].startswith('hair6_front_'):continue
 bones[e['bone']]['cubes']=[{'origin':[0,0,0],'size':[1,1,0],'uv':{'north':{'uv':[0,0],'uv_size':[1024,1024]}}} for _ in e['faces']]
P.write_text(json.dumps(g,ensure_ascii=False,indent=2));(R/'mesh.json').write_text(json.dumps(meshes,indent=2));P=M/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java';saved=R/'before-Hair6Meshes.java'
if not saved.exists():saved.write_bytes(P.read_bytes())
src=saved.read_text()
for i,e in enumerate(meshes):
 if not e['bone'].startswith('hair6_front_'):continue
 q=','.join('{'+','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in face)+'}' for face in e['faces']);method=f' private static float[][][] mesh{i}(){{return new float[][][]{{{q}}};}}'
 src=re.sub(r' private static float\[\]\[\]\[\] mesh'+str(i)+r'\(\).*',lambda m:method,src)
P.write_text(src)
(R/'changes.json').write_text(json.dumps({'path_rest_y_x_z_halfwidth':path,'chin_endpoint':[3.75,24,-3.88],'breast_corner':[outer_x,cy,cz],'upper_connector_segments':1,'deleted_intermediate_point':[3.72,23.8,-5.02],'parents':'chest_left/right → body → root, no head','head_seat_unchanged':True,'side_UV_unchanged':True,'new_bones':0},ensure_ascii=False,indent=2));print('Direct chin → chest corner',path[:2])
