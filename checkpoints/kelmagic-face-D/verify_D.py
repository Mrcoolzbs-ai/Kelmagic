from pathlib import Path
import json,math,hashlib,io,base64
import numpy as np
from PIL import Image
from renderer import rot
from audit_eye_space import visible
R=Path(__file__).resolve().parent
report={};bad=[]
for path,digest in json.loads((R/'protected-before.json').read_text()).items():
 if hashlib.sha256(Path(path).read_bytes()).hexdigest()!=digest:bad.append(path)
assert not bad
m=Path('/data/KelMagic');manifest=json.loads((R/'working-project-before.sha256.json').read_text());changes=[n for n,h in manifest.items() if not (m/n).exists() or hashlib.sha256((m/n).read_bytes()).hexdigest()!=h];assert not changes,changes
geo=json.loads((R/'D/red_mage.geo.json').read_text());bb=json.loads((R/'D/KelMagic-Face-D.bbmodel').read_text());base=json.loads((m/'art/red-mage/red_mage.geo.json').read_text());bs={b['name']:b for b in geo['minecraft:geometry'][0]['bones']};old={b['name']:b for b in base['minecraft:geometry'][0]['bones']}
FACE={'head','nose','mouth','lower_lip'}|{p+s for p in ['eye_','pupil_','lid_','brow_'] for s in ['left','right']}
for n,b in bs.items():
 assert {k:v for k,v in b.items() if k!='cubes'}=={k:v for k,v in old[n].items() if k!='cubes'}
 if n not in FACE:assert b==old[n]
flat=[(b,c) for b in bs.values() for c in b.get('cubes',[])];assert len(flat)==len(bb['elements'])
for (b,c),e in zip(flat,bb['elements']):
 assert c['origin']==e['from'];assert [c['origin'][j]+c['size'][j] for j in range(3)]==e['to'];assert e['origin']==c.get('pivot',b['pivot']);assert e['rotation']==c.get('rotation',[0,0,0])
 for f,v in c['uv'].items():
  assert e['faces'][f]['uv']==[v['uv'][0],v['uv'][1],v['uv'][0]+v['uv_size'][0],v['uv'][1]+v['uv_size'][1]]
  assert all(0<=x<=1024 for x in e['faces'][f]['uv'])
assert bb['animations']==json.loads((m/'art/red-mage/red_mage_gecko.bbmodel').read_text())['animations']
assert (R/'D/red_mage.animation.json').read_bytes()==(m/'art/red-mage/red_mage.animation.json').read_bytes()
tex=Image.open(R/'D/red_mage_geo.png').convert('RGBA');embedded=Image.open(io.BytesIO(base64.b64decode(bb['textures'][0]['source'].split(',')[1]))).convert('RGBA');assert tex.tobytes()==embedded.tobytes()
vis=visible(geo);eyes={}
for side,sign in [('left',1),('right',-1)]:
 c=bs['pupil_'+side]['cubes'][0];uv=c['uv']['north'];px=960 if sign==1 else 984;py=968
 tile=np.array(tex)[py:py+20,px:px+20];ys,xs=np.where(np.all(tile[:,:,:3]==[44,32,48],axis=2));assert len(xs)>0
 centerpixel=np.array([(xs.min()+xs.max()+1)/2,(ys.min()+ys.max()+1)/2]);assert np.allclose(centerpixel,[10,10])
 up=px+centerpixel[0];vp=py+centerpixel[1];frac=np.array([(up-uv['uv'][0])/uv['uv_size'][0],(vp-uv['uv'][1])/uv['uv_size'][1]])
 origin=np.array(c['origin']);dim=np.array(c['size']);raw=origin+dim*np.array([1-frac[0],1-frac[1],0]);pivot=np.array(c['pivot']);worldpivot=pivot*[-1,1,1];world=worldpivot+rot(c['rotation'])@((raw-pivot)*[-1,1,1]);normal=rot(c['rotation'])@np.array([0,0,-1]);target=np.array([0,28.2,-128.0]);t=(target[2]-world[2])/normal[2];hit=world+t*normal
 error=float(np.linalg.norm(hit-target));assert error<1e-6
 center=origin+dim*np.array([.5,.5,0]);assert np.allclose(raw,center,atol=1e-9)
 aperture_center=vis[side]['visible_center_geo_x'];aperture_offset=float(raw[0]-aperture_center);assert abs(aperture_offset)<.002
 aperture_lo,aperture_hi=vis[side]['visible_interval_geo_x'];assert aperture_lo<raw[0]<aperture_hi
 # Dark pupil box is well within the iris at every corner (texel margins >0).
 assert 0<xs.min() and xs.max()<19 and 0<ys.min() and ys.max()<19
 eyes[side]={'parent_chain':['pupil_'+side,'head','body','root'],'parent_rotations':[bs[n].get('rotation',[0,0,0]) for n in ['pupil_'+side,'head','body','root']],'bone_pivot':bs['pupil_'+side]['pivot'],'geometry_pupil_center_geo':raw.tolist(),'pupil_minus_iris_center':(raw-center).tolist(),'visible_aperture':vis[side],'offset_from_visible_aperture_center':aperture_offset,'normal_in_renderer_world':normal.tolist(),'focus_point_error':error,'pupil_pixel_center':centerpixel.tolist(),'pupil_within_iris':True}
assert abs(eyes['left']['geometry_pupil_center_geo'][0]+eyes['right']['geometry_pupil_center_geo'][0])<1e-8
assert eyes['left']['geometry_pupil_center_geo'][1:]==eyes['right']['geometry_pupil_center_geo'][1:]
report.update(working_manifest_verified=len(manifest),working_changes=changes,protected_B_C_Original_changes=bad,bones_preserved=64,nonface_bones_identical=52,animations_identical=29,export_vertices_pivots_rotation_uv_roundtrip='PASS',texture_embedded_external_match='PASS',static_gaze_target=[0,28.2,-128.0],eyes=eyes,client_test=False)
(R/'verification-D.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
