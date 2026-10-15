from pathlib import Path
import json,math,re
M=Path(__file__).resolve().parents[2];R=M/'reports/F3-hair7-head-seat';R.mkdir(exist_ok=True)
# Move the whole head hierarchy ONCE via its native translation. Source facial coordinates
# stay unchanged, so existing absolute-coordinate cheek/eye deformation keeps its timing/shape.
D=json.loads((M/'reports/F3-hair7-chin-fix/changes.json').read_text())['new_head_bottom']-24
headseat='''package dev.kelmagic.client.hair;
/** Native per-draw placement, not scaling. Every face/hat/scalp child inherits the same offset. */
public final class HeadSeat {
 public static final float DROP_PIXELS = '''+f'{D:.7f}f'+''';
 public static void apply(software.bernie.geckolib.cache.object.GeoBone head){
  head.setPosY(head.getInitialSnapshot().getOffsetY()-DROP_PIXELS);
 }
 private HeadSeat(){}
}
'''
(M/'src/main/java/dev/kelmagic/client/hair/HeadSeat.java').write_text(headseat)
p=M/'src/main/java/dev/kelmagic/client/RedMageRenderer.java';s=p.read_text();needle='if(bone.getName().equals("head")){' ;assert needle in s
if 'HeadSeat.apply(bone)' not in s:s=s.replace(needle,needle+'dev.kelmagic.client.hair.HeadSeat.apply(bone);')
p.write_text(s)
base=R/'before-mesh.json'
if not base.exists():base.write_bytes((M/'reports/F3-hair7-chin-fix/mesh.json').read_bytes())
meshes=json.loads(base.read_text());uv_changes=[]
for e in meshes:
 if e['parent']!='head' or e['solid']:continue
 # Remap the COMPLETE painted side/temple/shoulder/nape image to the already shortened
 # head surfaces, rather than leaving the lower part of the artwork cropped away.
 if e['texture'] not in ['sample_side_under','sample_side_over','sample_turn_under','sample_turn_over','sample_shoulder_under','sample_shoulder_over']:continue
 vals=[v[4] for q in e['faces'] for v in q];lo=min(vals);hi=max(vals)
 if hi-lo<1e-7:continue
 for q in e['faces']:
  for v in q:v[4]=(v[4]-lo)/(hi-lo)
 uv_changes.append({'bone':e['bone'],'old_V_range':[lo,hi],'new_V_range':[0,1]})
# Correct only the chest root path. Body/chest parents remain unchanged, no head reads.
def local(p):
 x,y,z=p;c=math.cos(math.radians(22));s=math.sin(math.radians(22));dy=y-23;dz=z+1.6;return [x,23+c*dy-s*dz,-1.6+s*dy+c*dz]
def uvq(q,uv=(0,0,1,1)):
 u,v,w,h=uv;return [list(p)+[uu,vv] for p,(uu,vv) in zip(q,[(u,v),(u+w,v),(u+w,v+h),(u,v+h)])]
by={e['bone']:e for e in meshes};path=[(24.,3.75,-3.88,.72),(23.8,3.72,-5.02,.85),(21.15,3.32,-5.72,.92),(19.40,2.35,-4.99,.74),(17.65,1.55,-4.10,.50)]
for side,sign in [('left',1),('right',-1)]:
 vs=[[local([sign*(x-w*.72),y,z]),local([sign*(x+w*.72),y,z])] for y,x,z,w in path];fs=[]
 for j in range(len(path)-1):
  a,b=vs[j];c,d=vs[j+1];fa=[a,b,d,c];bk=[[p[0],p[1],p[2]+.24] for p in fa];fs += [fa,bk,[fa[0],bk[0],bk[1],fa[1]],[fa[1],bk[1],bk[2],fa[2]],[fa[3],bk[3],bk[2],fa[2]],[fa[0],bk[0],bk[3],fa[3]]]
 by['hair6_front_'+side+'_base']['faces']=[uvq(q) for q in fs]
 for tier,off in [('under',.06),('over',.19)]:
  fs=[]
  for j in range(len(path)-1):
   rows=[]
   for k in [j,j+1]:
    y,x,z,w=path[k];rows.append([local([sign*(x-w),y,z-off]),local([sign*(x+w),y,z-off])])
   a,b=rows[0];c,d=rows[1];v0=(path[0][0]-path[j][0])/(path[0][0]-path[-1][0]);v1=(path[0][0]-path[j+1][0])/(path[0][0]-path[-1][0]);fs.append(uvq([a,b,d,c],(0,v0,1,v1-v0)))
  by['hair6_front_'+side+'_'+tier]['faces']=fs
(R/'mesh.json').write_text(json.dumps(meshes,indent=2));p=M/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java';old=R/'before-Hair6Meshes.java'
if not old.exists():old.write_bytes(p.read_bytes())
s=old.read_text();original=json.loads(base.read_text());changed=[]
for i,(a,e) in enumerate(zip(original,meshes)):
 if a==e:continue
 changed.append(e['bone']);q=','.join('{'+','.join('{'+','.join(f'{v:.7f}f' for v in row)+'}' for row in face)+'}' for face in e['faces']);method=f' private static float[][][] mesh{i}(){{return new float[][][]{{{q}}};}}'
 s=re.sub(r' private static float\[\]\[\]\[\] mesh'+str(i)+r'\(\).*',lambda m:method,s)
p.write_text(s)
(R/'changes.json').write_text(json.dumps({'head_drop_pixels':D,'actual_head_bottom_rest':25.208-D,'body_top':24,'chin_height_preserved':0.302,'all_face_hat_and_head_hair_children_move_together':True,'head_scale_changed':False,'PNG_changed':False,'side_texture_UV_remaps':uv_changes,'changed_mesh_groups':changed,'chest_root_y':24,'chest_parent':'chest_left/right → body → root, no head','static_neck_lift_preserved_pixels':1,'new_bones':0},ensure_ascii=False,indent=2))
print('Head drop',D,'actual head bottom',25.208-D,'side texture remaps',len(uv_changes),'changed mesh groups',len(changed))
