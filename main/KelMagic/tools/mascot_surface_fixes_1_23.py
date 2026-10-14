"""Connected head-owned hair, recessed expressive face and restored suspended chest silhouette."""
from mascot_surface_fixes_1_22 import refine_rig as old_rig, refine_poses as old_poses
from PIL import Image,ImageDraw
import copy
FACES=['north','south','east','west','up','down']
def refine_rig(bones,tex,A):
 atlas=old_rig(bones,tex,A);by={b['name']:b for b in bones};d=ImageDraw.Draw(atlas);slot=600
 def uv(col,pattern=False):
  nonlocal slot
  x=(slot%32)*32;y=96+(slot//32)*32;slot+=1
  d.rectangle((x,y,x+31,y+31),fill=col)
  if pattern:
   for xx in [x+6,x+18,x+25]:d.line((xx,y,xx,y+31),fill=tuple(max(0,c-10)for c in col[:3])+(255,),width=2)
  return {f:{'uv':[x+2,y+2],'uv_size':[28,28]}for f in FACES}
 skin=(250,224,226,255);hair=(132,36,37,255);cloth=(17,47,48,255)
 def cub(origin,size,col,pattern=False):return {'origin':origin,'size':size,'uv':uv(col,pattern)}
 def new(name,parent,pivot,cubes,rotation=None):
  b={'name':name,'parent':parent,'pivot':pivot,'cubes':cubes}
  if rotation:b['rotation']=rotation
  bones.append(b);by[name]=b;return b
 # Upper attachment restored; two cloth-covered hanging volumes, not rigid flat plates.
 for side,x in [('left',.09),('right',-3.84)]:
  b=by['chest_'+side];b['pivot']=[1.95 if side=='left' else -1.95,23,-1.6];b['rotation']=[22,0,0]
  b['cubes']=[cub([x,18.2,-5.85],[3.75,4.8,4.25],cloth)]
 # Every rear segment is contiguous and follows head yaw. No glued-to-body or flattened hair.
 for name,parent,y,h in [('hair_back','head',20,4),('hair_mid','hair_back',16,4),('hair_tip','hair_mid',12,4)]:
  b=by[name];b['parent']=parent;b['pivot']=[0,y+h,4.65];b['rotation']=[0,0,0]
  b['cubes']=[]
  for i,depth in enumerate([-.03,.12,.22,.12,-.03]):
   low=y+([.3,-.1,-.6,-.1,.3][i] if name=='hair_tip' else 0)
   b['cubes'].append(cub([-4.15+i*1.66,low-.035,4.18+depth+({'hair_back':0,'hair_mid':.018,'hair_tip':.036}[name])],[1.64,y+h-low+.035,.95],hair,True))
 # Volume around the scalp, not a coincident all-face overlay.
 by['hair_crown']['cubes']=[cub([-4.18,24,4.05],[8.36,8.25,1.08],hair,True),cub([-4.25,24,-4.15],[.25,8.25,8.2],hair),cub([4,24,-4.15],[.25,8.25,8.2],hair)]
 by['hair_crown']['cubes'].append(cub([-4.18,32,-4.15],[8.36,.25,9.28],hair,True))
 for i,(x,w,low) in enumerate([(-4,1.45,29.4),(-2.55,1.25,29.7),(-1.3,2.0,28.9),(.7,1.65,29.65),(2.35,1.65,29.35)]):
  new('bang_'+str(i),'head',[x+w/2,32,-4.2],[cub([x,low,-4.28],[w,32.2-low,.55],hair,True)])
 for side,x in [('left',2.85),('right',-4.1)]:
  by['hair_'+side].update(pivot=[x+.625,29.4,-4.4],rotation=[0,0,0],cubes=[cub([x,24,-4.82],[1.25,5.4,.84],hair,True)])
  by['hair_front_'+side].update(pivot=[x+.625,24,-4.4],rotation=[-25,0,0],cubes=[cub([x,18.4,-4.82],[1.25,5.66,.84],hair,True)])
  new('hair_end_'+side,'hair_front_'+side,[x+.625,18.4,-4.4],[cub([x+.15,16.4,-4.76],[.95,2.06,.72],hair,True)],[-6,0,0])
 # Skin head with real eye/mouth recesses. The front is tiled around openings, not layered decals.
 head=by['head'];head['cubes']=[cub([-4,24,-3.05],[8,8,7.05],skin)]
 xs=[-4,-3.35,-1.3,-.55,.55,1.3,3.35,4];ys=[24,25.25,26.35,27.1,29.3,32]
 for x0,x1 in zip(xs,xs[1:]):
  for y0,y1 in zip(ys,ys[1:]):
   eye=y0==27.1 and (x0>=.55 and x1<=3.35 or x0>=-3.35 and x1<=-.55)
   mouth=y0==25.25 and x0>=-1.3 and x1<=1.3
   if not eye and not mouth:head['cubes'].append(cub([x0,y0,-4],[x1-x0,y1-y0,.95],skin))
 # The head is a surfaced skin union: internal cell walls otherwise peek through as hairline seams.
 from mascot_surface_fixes_1_21 import surface_mesh
 parts=[]
 for c in head['cubes']:parts.append((c['origin'],[c['origin'][i]+c['size'][i] for i in range(3)],'wood',None))
 mesh=surface_mesh(parts,True);head['cubes']=[]
 for e in mesh:
  origin=e['from'];origin[1]+=16;top=e['to'];top[1]+=16;face=next(iter(e['faces']));u=uv(skin)
  head['cubes'].append({'origin':origin,'size':[top[i]-origin[i] for i in range(3)],'uv':{face:u[face]}})
 for side,x,col in [('left',.55,(201,61,155,255)),('right',-3.35,(116,119,127,255))]:
  center=x+1.4
  by['eye_'+side].update(pivot=[center,28.2,-3.52],cubes=[cub([x+.08,27.18,-3.52],[2.64,2.04,.4],(253,250,242,255))])
  by['pupil_'+side].update(pivot=[center,28.2,-3.61],cubes=[cub([center-.5,27.35,-3.61],[1,1.66,.13],col),cub([center-.12,27.56,-3.66],[.24,1.3,.08],(44,32,48,255)),cub([center-.28,28.55,-3.7],[.25,.3,.06],(255,255,255,255))])
  by['lid_'+side].update(pivot=[center,29.3,-3.84],cubes=[cub([x+.035,27.1,-3.84],[2.73,2.2,.16],skin),cub([x+.035,27.1,-3.89],[2.73,.12,.08],(75,30,39,255))])
  by['brow_'+side].update(pivot=[center,29.5,-4.05],cubes=[cub([x,29.43,-4.1],[2.8,.18,.15],(103,29,33,255))])
 by['mouth'].update(pivot=[0,25.8,-3.55],cubes=[cub([-1.27,25.25,-3.55],[2.54,1.1,.35],(73,26,36,255)),cub([-.85,25.3,-3.61],[1.7,.22,.1],(185,87,109,255)),cub([-1.05,26.1,-3.59],[2.1,.18,.1],(255,246,230,255))])
 by['lower_lip'].update(pivot=[0,25.25,-4.03],cubes=[cub([-1.27,25.25,-4.04],[2.54,1.1,.24],skin),cub([-1.24,26.25,-4.09],[2.48,.1,.1],(156,70,86,255))])
 # The moving lip is a flush skin surface; side walls on a protruding mask made a visible vertical crease.
 for c in by['lower_lip']['cubes']:
  c['uv']={'north':c['uv']['north']};c['size'][2]=0
 by['lower_lip']['cubes'][0]['origin'][0]=-1.3;by['lower_lip']['cubes'][0]['size'][0]=2.6
 by['lower_lip']['cubes'][0]['origin'][2]=-4
 by['lower_lip']['cubes'][1]['origin'][2]=-4.012
 new('nose','head',[0,27,-4.02],[cub([-.3,26.55,-4.23],[.6,.75,.32],skin)])
 # Overlapping inner caps are inset from the outer segments, not coplanar exposed end-faces.
 for side in ['left','right']:
  by[side+'_forearm']['cubes'][0]['size'][1]=6.28
  by[side+'_shin']['cubes'][0]['size'][1]=6.28
 # A covered hem follows each thigh; knee skin remains exposed, not thigh end-caps.
 for side in ['left','right']:
  x=2 if side=='left' else -2
  by['tunic_'+side].update(parent=side+'_leg',pivot=[x,12,0],rotation=[0,0,0],cubes=[cub([x-1.96,7.45,-2.09],[3.92,4.7,4.18],cloth)])
 new('tunic_bridge','body',[0,12,0],[cub([-.17,10.7,-2.13],[.34,2.7,4.26],cloth)])
 # Cat tail remains on its cushion; paws/body share the same support plane.
 by['cat_tail'].update(pivot=[0,2,2.4],rotation=[-5,65,0],cubes=[cub([-.5,1.5,2.4],[1,1,2.7],(238,224,214,255))])
 # Static face stage: no changes to the rig, texture layout or pose refinements.
 from mascot_static_face_stage1_1_23 import apply_static_face
 apply_static_face(bones)
 for mouth in range(4):
  for blink in [0,1]:atlas.save(A/f'textures/entity/red_mage_face_{mouth}_{blink}.png')
 return atlas

def refine_poses(anims):
 old_poses(anims)
 # Keep all raised hands laterally OUTSIDE the complete hair/head envelope.
 for name,a in anims.items():
  b=a['bones']
  for side,sgn in [('left',-1),('right',1)]:
   track=b.get(side+'_arm',{}).get('rotation')
   if track is not None:
    vals=track.values() if isinstance(track,dict) else [track]
    for v in vals:
     if v[0]<-90:v[2]=-sgn*max(35,min(50,abs(v[2])));v[1]=0
     elif v[0]<-45:v[2]=sgn*max(18,min(30,abs(v[2])));v[1]=0
   if name.endswith(('hat_play','hat_adjust','throne_hat','stretch')):
    b[side+'_forearm']={'rotation':[-12,0,0]}
  if name.endswith('swim'):
   for side,sgn in [('left',-1),('right',1)]:b[side+'_arm']['position']=[-sgn*.8,0,0]
  if name.endswith('climb'):
   b['left_forearm']={'rotation':[45,0,0]};b['right_forearm']={'rotation':[45,0,0]}
   for side,sgn in [('left',-1),('right',1)]:
    for v in b[side+'_arm']['rotation'].values():v[1]=sgn*20
  if name.endswith('cast'):
   for side,sgn in [('left',-1),('right',1)]:
    track=b[side+'_arm']['rotation']
    for v in track.values():v[1]=sgn*15 if abs(v[0])>10 else 0
  if name.endswith(('yawn','throne_chin_rest')):
   b['right_arm']={'rotation':[-48,0,58]};b['right_forearm']={'rotation':[-45,0,0]}
  if name.endswith(('read','throne_read')):
   # Hands ahead of torso, no passage through the head when the elbow folds.
   for side,sgn in [('left',-1),('right',1)]:b[side+'_arm']={'rotation':[-30,0,sgn*18]};b[side+'_forearm']={'rotation':[-65,0,0]}
  if name.startswith('animation.red_mage.throne_'):
   b['root']={'position':[0,-.6,2]}
   head=b.get('head',{}).get('rotation',[0,0,0]);head=head if isinstance(head,dict) else {'0':head,'8':head.copy()}
   for v in head.values():v[0]=max(10,v[0]);v[1]=max(-18,min(18,v[1]));v[2]=max(-5,min(5,v[2]))
   b['head']={'rotation':head}
   if name.endswith('recline'):b['root']={'position':[0,-.6,1.3]};b['body']={'rotation':[-4,0,0]};b['head']={'rotation':[18,0,0]}
   if name.endswith('bored'):
    for v in b['head']['rotation'].values():v[1]=max(-12,min(12,v[1]));v[2]=max(-2,min(2,v[2]))
   if name.endswith('kitten'):
    b['root']={'position':[0,0,0]};b['cat']={'position':[0,9.06,3]};b['cat_tail']={'rotation':{'0':[-5,60,0],'4':[-5,82,0],'8':[-5,60,0]}}
