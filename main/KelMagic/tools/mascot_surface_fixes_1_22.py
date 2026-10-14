"""Independent rig repair: connected full hair, proper joints, opaque integer UVs and a flat face."""
from PIL import Image,ImageDraw
import math,copy
FACES=['north','east','south','west','up','down']
def face_uv(c):
 if isinstance(c['uv'],dict):return copy.deepcopy(c['uv'])
 u,v=c['uv'];w,h,d=c['size']
 return {f:{'uv':a[:2],'uv_size':a[2:]} for f,a in {
 'north':[u+d,v+d,w,h],'east':[u,v+d,d,h],'south':[u+2*d+w,v+d,w,h],
 'west':[u+d+w,v+d,d,h],'up':[u+d,v,w,d],'down':[u+d+w,v,w,d]}.items()}
def refine_rig(bones,tex,A):
 by={b['name']:b for b in bones}
 # Articulated hair grows from the head; the long tips ride OVER the upper chest.
 for side,x in [('left',3.1),('right',-3.9)]:
  top=by['hair_'+side];top['pivot']=[x+.4,27,-4.35];top['rotation']=[0,0,0]
  top['cubes']=[{'origin':[x,24.8,-4.65],'size':[.8,3.2,.6],'uv':{f:{'uv':[192,96],'uv_size':[8,8]} for f in FACES}}]
  bones.append({'name':'hair_front_'+side,'parent':'hair_'+side,'pivot':[x+.4,24.8,-4.35],'rotation':[-28,0,0],
  'cubes':[{'origin':[x,18,-4.65],'size':[.8,6.8,.6],'uv':{f:{'uv':[192,96],'uv_size':[8,8]} for f in FACES}}]})
 # No head overlay: hair below the head continues through all three sections.
 for name,y,height in [('hair_back',20,4),('hair_mid',16,4),('hair_tip',12,4)]:
  b=by[name];b['pivot']=[0,y+height,4.4];
  if name=='hair_back':b['parent']='body'
 b['cubes']=[{'origin':[-4.05,y,4.05],'size':[8.1,height-.035,.7],'uv':[64,44]}]
 # Chest moved to the upper torso, reduced depth/tilt and separated halves.
 for side,x in [('left',.12),('right',-3.77)]:
  b=by['chest_'+side];b['pivot']=[1.95 if side=='left' else -1.95,23.5,-1.6];b['rotation']=[10,0,0]
  b['cubes'][0].update(origin=[x,19.5,-5.25],size=[3.65,4,3.5])
 # Coincident elbow faces cannot be fixed by texture offsets: separate the caps by .04 units.
 for side in ['left','right']:
  c=by[side+'_arm']['cubes'][0];c['origin'][1]=18.04;c['size'][1]=5.96
  c=by[side+'_forearm']['cubes'][0];c['origin'][0]+=.045;c['origin'][2]+=.045;c['size'][0]-=.09;c['size'][2]-=.09
 # Original legs overlapped by .2, cuffs were ABOVE their knee pivots.
 for side,x in [('left',2),('right',-2)]:
  for part in ['leg','shin']:
   b=by[side+'_'+part];b['pivot'][0]=x;b['cubes'][0]['origin'][0]=x-1.875;b['cubes'][0]['size'][0]=3.75
   if part=='leg':b['cubes'][0]['origin'][1]=6.04;b['cubes'][0]['size'][1]=5.96
   else:b['cubes'][0]['origin'][0]+=.05;b['cubes'][0]['origin'][2]+=.05;b['cubes'][0]['size'][0]-=.1;b['cubes'][0]['size'][2]-=.1
  b=by[side+'_boot'];b['pivot']=[x,5.8,0];b['cubes'][0].update(origin=[x-1.975,4.8,-2.1],size=[3.95,1,4.2])
 # The book stays visible for the entire selected read scene; move its rear edge clear of torso.
 by['book']['cubes'][0]['origin'][2]=-12;by['book']['pivot'][2]=-9
 by['book_page']['cubes'][0]['origin'][2]=-11.7;by['book_page']['pivot'][2]=-9
 # All face details become ONE head-face texture, not floating layered cubes.
 for b in bones:
  if b['name'].startswith(('eye_','pupil_','lid_','brow_')) or b['name'] in ['mouth','lower_lip']:b.pop('cubes',None)
 atlas=Image.new('RGBA',(1024,1024),(0,0,0,0));index=0
 face_slot=(0,0);book_slot=(96,0)
 for b in bones:
  for c in b.get('cubes',[]):
   old=face_uv(c);new={}
   for f,a in old.items():
    u,v=a['uv'];w,h=a['uv_size'];flipx=w<0;flipy=h<0
    im=tex.crop((math.floor(min(u,u+w)),math.floor(min(v,v+h)),math.ceil(max(u,u+w)),math.ceil(max(v,v+h))))
    if not im.width or not im.height:im=Image.new('RGBA',(1,1))
    col=(17,47,48,255)
    if b['name'].startswith('hair'):col=(132,36,37,255)
    elif b['name'].startswith('cat'):col=(238,224,214,255)
    elif b['name'].endswith(('forearm','arm')):col=(250,224,226,255)
    elif b['name']=='head':col=(250,224,226,255)
    elif b['name'].startswith('hat'):col=(13,43,44,255)
    elif b['name'] in ['bracelet','belt_gem']:col=(199,158,67,255)
    base=Image.new('RGBA',im.size,col);base.alpha_composite(im);base.putalpha(255);im=base
    if flipx:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if flipy:im=im.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    if b['name']=='head' and f=='north':
     im=im.resize((64,64),Image.Resampling.NEAREST);draw_face(im,0,False);atlas.paste(im,face_slot);new[f]={'uv':[0,0],'uv_size':[64,64]};continue
    if b['name']=='book_page' and f=='up':
     im=im.resize((64,32),Image.Resampling.NEAREST);atlas.paste(im,book_slot);new[f]={'uv':[96,0],'uv_size':[64,32]};continue
    x=(index%32)*32;y=96+(index//32)*32;index+=1
    # Two pixel gutters ensure no face ever interpolates transparent adjacent islands.
    im=im.resize((28,28),Image.Resampling.NEAREST);atlas.paste(im.resize((32,32),Image.Resampling.NEAREST),(x,y));atlas.paste(im,(x+2,y+2))
    new[f]={'uv':[x+2,y+2],'uv_size':[28,28]}
   c['uv']=new
 # Skin-coloured, opaque knee caps rather than the trouser/boot pixels from box UVs.
 for side in ['left','right']:
  for part,face in [('leg','down'),('shin','up')]:
   uv=by[side+'_'+part]['cubes'][0]['uv'][face]['uv'];ImageDraw.Draw(atlas).rectangle((uv[0]-2,uv[1]-2,uv[0]+29,uv[1]+29),fill=(250,224,226,255))
 assert index<928
 for blink in [False,True]:
  for mouth in range(4):
   out=atlas.copy();face=atlas.crop((0,0,64,64));draw_face(face,mouth,blink);out.paste(face,(0,0));out.save(A/f'textures/entity/red_mage_face_{mouth}_{int(blink)}.png')
 return atlas

def draw_face(im,mouth,blink):
 d=ImageDraw.Draw(im);skin=(250,224,226,255);hair=(132,36,37,255)
 # Keep the original bangs and cheeks; erase previously baked pupils/lips.
 d.rectangle((8,24,55,63),fill=skin)
 for x,col in [(12,(116,119,127,255)),(44,(201,61,155,255))]:
  d.rectangle((x-4,24,x+11,25),fill=(153,53,55,255))
  if blink:d.rectangle((x-4,31,x+11,32),fill=(129,54,57,255))
  else:
   d.rectangle((x-4,28,x+11,35),fill=(253,250,242,255));d.rectangle((x+2,29,x+6,34),fill=col)
 if mouth==0:d.rectangle((27,46,36,46),fill=(124,54,66,255));d.rectangle((28,48,35,48),fill=(171,96,106,255))
 else:
  d.rectangle((27,45,36,46+mouth),fill=(79,29,38,255));d.rectangle((28,47+mouth,35,47+mouth),fill=(172,97,103,255))

def refine_poses(anims):
 # Fingers/thumbs stay outside the head and torso during all personality clips.
 for name,a in anims.items():
  b=a['bones']
  for side,sgn in [('left',-1),('right',1)]:
   arm=b.get(side+'_arm',{}).get('rotation')
   if arm is not None:
    vals=arm.values() if isinstance(arm,dict) else [arm]
    for v in vals:
     if v[0]<-90:v[2]=sgn*max(36,abs(v[2]));v[1]=0
  if name.endswith(('hat_play','hat_adjust','throne_hat')):
   side='right' if not name.endswith('hat_adjust') else 'left';sgn=1 if side=='right' else -1
   b[side+'_arm']={'rotation':[-145,0,sgn*40]};b[side+'_forearm']={'rotation':[-10,0,0]}
  if name.endswith(('yawn','throne_chin_rest')):
   b['right_arm']={'rotation':[-52,0,40]};b['right_forearm']={'rotation':[-75,0,0]}
  if name.endswith(('read','throne_read')):
   b['book']={'scale':[1,1,1]}
   for side,sgn in [('left',-1),('right',1)]:b[side+'_arm']={'rotation':[-35,0,sgn*13]};b[side+'_forearm']={'rotation':[-65,0,0]}
  if not name.startswith('animation.red_mage.throne_'):continue
  b['root']={'position':[0,-.6,4]}
  if name.endswith('kitten'):b['root']={'position':[0,0,0]};continue
  # With raised armrests, straight upper arms + horizontal forearms land on the velvet.
  if name.endswith(('regal','crossed','side_lounge','recline','bored')):
   for side,sgn in [('left',-1),('right',1)]:b[side+'_arm']={'rotation':[-2,0,sgn*8]};b[side+'_forearm']={'rotation':[-88,0,0]}
  if name.endswith(('hat','chin_rest')):b['left_arm']={'rotation':[-2,0,-8]};b['left_forearm']={'rotation':[-88,0,0]}
  if name.endswith('stretch'):
   for side,sgn in [('left',-1),('right',1)]:
    b[side+'_arm']={'rotation':{'0':[-2,0,sgn*8],'2':[-145,0,sgn*36],'6':[-145,0,sgn*36],'8':[-2,0,sgn*8]}};b[side+'_forearm']={'rotation':{'0':[-88,0,0],'2':[-10,0,0],'6':[-10,0,0],'8':[-88,0,0]}}
  if name.endswith('recline'):b['root']={'position':[0,-.6,2.4]};b['body']={'rotation':[-8,0,0]}
  if name.endswith('side_lounge'):b['body']={'rotation':[0,0,-3]}
