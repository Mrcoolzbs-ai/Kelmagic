"""Native glyph atlas, non-coincident facial layers and union-only throne surfaces."""
import numpy as np

def boxuv(c):
 u,v=c['uv'];w,h,depth=c['size']
 return {f:{'uv':a[:2],'uv_size':[a[2]-a[0],a[3]-a[1]]} for f,a in {'north':[u+depth,v+depth,u+depth+w,v+depth+h],'east':[u,v+depth,u+depth,v+depth+h],'south':[u+2*depth+w,v+depth,u+2*depth+2*w,v+depth+h],'west':[u+depth+w,v+depth,u+2*depth+w,v+depth+h],'up':[u+depth,v,u+depth+w,v+depth],'down':[u+depth+w,v,u+depth+2*w,v+depth]}.items()}

def fix_rig(bones,tex,A):
 from PIL import Image,ImageDraw
 by={b['name']:b for b in bones}
 by['hair_crown'].pop('cubes',None) # identical overlay cube was exactly coincident with the skin head.
 for side in ['left','right']:
  x=3.1 if side=='left' else -3.9
  by['hair_'+side]['cubes']=[{'origin':[x,22.9,-5.2],'size':[.8,3.5,.55],'uv':{f:{'uv':[192,96],'uv_size':[8,8]} for f in ['north','south','east','west','up','down']}}]
  by['hair_'+side]['pivot']=[x+.4,26.4,-4.95]
 for b in bones:
  name=b['name']
  if name.startswith(('eye_','pupil_','lid_','brow_')):
   z=-4.18 if name.startswith('eye_') else -4.36 if name.startswith('pupil_') else -4.54 if name.startswith('lid_') else -4.72
   b['cubes'][0]['origin'][2]=z;b['cubes'][0]['size'][2]=.025;b['pivot'][2]=z
  if name.endswith('_arm'):
   c=b['cubes'][0];c['uv']=boxuv(c);c['uv']['down']={'uv':[232,96],'uv_size':[16,16]}
  if name.endswith('_forearm'):
   b['cubes'][0]['uv']={f:{'uv':[232,96],'uv_size':[16,16]} for f in ['north','south','east','west','up','down']}
 by['bracelet']['cubes'][0].update(origin=[3.78,13.88,-2.22],size=[3.44,1.04,4.44])
 by['belt_gem']['cubes'][0].update(origin=[-.65,13.65,-3.2],size=[1.3,1.3,.65])
 by['cat_bow']['cubes'][0].update(origin=[-1.35,7.65,-2.95],size=[2.7,.65,.45]);by['cat_bow']['pivot']=[0,7.65,-2.7]
 # A short haircut and rear segments with a limited seated envelope avoid chest/backrest intersection.
 d=ImageDraw.Draw(tex);d.rectangle((192,96,207,111),fill=(132,36,37,255));d.rectangle((192,96,193,103),fill=(112,30,31,255));d.rectangle((198,96,199,103),fill=(145,41,42,255));d.rectangle((232,96,249,113),fill=(250,224,226,255));d.rectangle((208,0,222,14),fill=(116,119,127,255));d.rectangle((224,0,238,14),fill=(201,61,155,255))
 by['pupil_left']['cubes'][0]['uv']=[224,0];by['pupil_right']['cubes'][0]['uv']=[208,0]
 # Use the actual Kel bitmap glyphs, not pretend writing. Twelve glyphs on two pages.
 d.rectangle((160,128,223,159),fill=(240,225,191,255));d.line((192,129,192,158),fill=(169,133,86,255))
 atlas=Image.open(A/'textures/font/kel.png').convert('RGBA');cw=atlas.width//8;ch=atlas.height//10
 for i,idx in enumerate([32,37,65,25,56,43,13,38,2,74,9,61]):
  im=atlas.crop((idx%8*cw,idx//8*ch,(idx%8+1)*cw,(idx//8+1)*ch));alpha=im.getchannel('A');ink=Image.new('RGBA',im.size,(83,61,46,255));ink.putalpha(alpha);ink=ink.resize((7,7),Image.Resampling.NEAREST)
  tex.alpha_composite(ink,(163+(i%3)*8+(32 if i>=6 else 0),132+((i%6)//3)*12))
 by['book_page']['cubes'][0]['uv']={f:{'uv':[178,16],'uv_size':[10,10]} for f in ['north','south','east','west','up','down']};by['book_page']['cubes'][0]['uv']['up']={'uv':[160,128],'uv_size':[64,32]}
 return tex

def fix_poses(anims):
 for n,a in anims.items():
  if not n.startswith('animation.red_mage.throne_'):continue
  b=a['bones'];b['root']={'position':[0,-.45,-1.4]}
  if n.endswith('kitten'):b['root']={'position':[0,0,-1.4]};b['cat']={'position':[0,9.1,0]};continue
  b['left_leg']={'rotation':[-90,0,-3]};b['right_leg']={'rotation':[-90,0,3]};b['left_shin']={'rotation':[90,0,0]};b['right_shin']={'rotation':[90,0,0]}
  for side,sign in [('left',-1),('right',1)]:b[side+'_arm']={'rotation':[-32,0,sign*12]};b[side+'_forearm']={'rotation':[-48,0,0]}
  if n.endswith('crossed'):b['left_leg']={'rotation':[-90,-15,-9]};b['left_shin']={'rotation':[80,0,0]}
  if n.endswith('chin_rest'):b['right_arm']={'rotation':[-62,-12,16]};b['right_forearm']={'rotation':[-96,0,0]}
  if n.endswith('side_lounge'):b['body']={'rotation':[0,0,-7]};b['head']={'rotation':[8,-8,7]}
  if n.endswith('recline'):b['body']={'rotation':[-8,0,0]};b['head']={'rotation':[6,0,0]};b['root']={'position':[0,-.45,-2.8]};b['left_leg']={'rotation':[-104,0,-4]};b['right_leg']={'rotation':[-102,0,4]};b['left_shin']={'rotation':[96,0,0]};b['right_shin']={'rotation':[94,0,0]}
  if n.endswith('read'):b['left_arm']={'rotation':[-40,0,-12]};b['right_arm']={'rotation':[-40,0,12]};b['left_forearm']={'rotation':[-52,0,0]};b['right_forearm']={'rotation':[-52,0,0]};b['book']={'position':[0,0,0],'scale':{'0':[0,0,0],'1':[1,1,1],'7':[1,1,1],'8':[0,0,0]}}
  if n.endswith('hat'):b['right_arm']={'rotation':[-130,0,30]};b['right_forearm']={'rotation':[-30,0,0]}
  if n.endswith('stretch'):b['left_arm']={'rotation':{'0':[-32,0,-12],'2':[-145,0,-28],'6':[-145,0,-28],'8':[-32,0,-12]}};b['right_arm']={'rotation':{'0':[-32,0,12],'2':[-145,0,28],'6':[-145,0,28],'8':[-32,0,12]}}

def surface_mesh(parts,upper):
 # Build the material CSG union; internal and coincident faces never enter the model.
 coords=[sorted(set([0,16,32] if k==1 else [] )|{p[k] for a,b,t,r in parts for p in [a,b]}) for k in range(3)]
 mats=['gold','velvet','wood','gem'];shape=tuple(len(c)-1 for c in coords);grid=np.zeros(shape,dtype=np.uint8)
 for a,b,t,r in parts:
  sl=tuple(slice(coords[k].index(a[k]),coords[k].index(b[k])) for k in range(3));grid[sl]=mats.index(t)+1
 elems=[]
 for axis in range(3):
  others=[k for k in range(3) if k!=axis]
  for sign,face in [(-1,['west','down','north'][axis]),(1,['east','up','south'][axis])]:
   for idx in range(shape[axis]):
    surface=np.take(grid,idx,axis=axis).copy();ni=idx+sign
    if 0<=ni<shape[axis]:surface[np.take(grid,ni,axis=axis)!=0]=0
    plane=coords[axis][idx+(sign==1)]
    for i in range(surface.shape[0]):
     j=0
     while j<surface.shape[1]:
      label=int(surface[i,j])
      if not label:j+=1;continue
      jj=j+1
      while jj<surface.shape[1] and surface[i,jj]==label:jj+=1
      ii=i+1
      while ii<surface.shape[0] and np.all(surface[ii,j:jj]==label):ii+=1
      surface[i:ii,j:jj]=0;aa=[0.,0.,0.];bb=[0.,0.,0.];aa[axis]=bb[axis]=plane;aa[others[0]]=coords[others[0]][i];bb[others[0]]=coords[others[0]][ii];aa[others[1]]=coords[others[1]][j];bb[others[1]]=coords[others[1]][jj]
      low,high=(16,32) if upper else (0,16)
      if axis==1:
       if plane==16 and ((upper and sign==1) or (not upper and sign==-1)):j=jj;continue
       if not low<=plane<=high:j=jj;continue
      else:
       aa[1]=max(aa[1],low);bb[1]=min(bb[1],high)
       if aa[1]>=bb[1]:j=jj;continue
      if upper:aa[1]-=16;bb[1]-=16
      elems.append({'from':aa,'to':bb,'faces':{face:{'texture':'#'+mats[label-1],'uv':[0,0,16,16]}}});j=jj
 return elems
