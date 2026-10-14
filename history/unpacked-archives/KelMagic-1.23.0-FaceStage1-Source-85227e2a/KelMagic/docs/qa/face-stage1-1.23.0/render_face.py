"""Read-only rasterisation of the actual Bedrock faces/UVs, not a replacement model.
Neutral 1.23 procedural values are applied in memory identically before and after.
"""
from pathlib import Path
import json,zipfile,io,math,copy,argparse
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ap=argparse.ArgumentParser();ap.add_argument('--project',type=Path,default=Path.cwd());ap.add_argument('--baseline',type=Path,required=True);ap.add_argument('--output',type=Path,default=Path(__file__).resolve().parent);args=ap.parse_args();P=args.project.resolve();O=args.output.resolve();O.mkdir(parents=True,exist_ok=True);Z=zipfile.ZipFile(args.baseline)
FONT='/usr/share/fonts/msttcore/arial.ttf'
face_names={'head','nose','mouth','lower_lip'}|{pre+s for pre in ['eye_','pupil_','lid_','brow_'] for s in ['left','right']}
N={'north':(0,0,-1),'south':(0,0,1),'west':(-1,0,0),'east':(1,0,0),'up':(0,1,0),'down':(0,-1,0)}
# Vertex order/UV use the actual per-face bounds; flat-colour islands retain their exact RGB.
V={'north':[(1,1,0),(0,1,0),(0,0,0),(1,0,0)],'south':[(0,1,1),(1,1,1),(1,0,1),(0,0,1)],'west':[(0,1,0),(0,1,1),(0,0,1),(0,0,0)],'east':[(1,1,1),(1,1,0),(1,0,0),(1,0,1)],'up':[(0,1,0),(1,1,0),(1,1,1),(0,1,1)],'down':[(0,0,1),(1,0,1),(1,0,0),(0,0,0)]}
def rot(v):
 x,y,z=np.radians(v)*[-1,-1,1];cx,cy,cz=np.cos([x,y,z]);sx,sy,sz=np.sin([x,y,z]);return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
def render(geo,texture,yaw,cutaway=False):
 bs={b['name']:b for b in geo['minecraft:geometry'][0]['bones']}; mats={};size=1200;scale=92; target=np.array([0,28.25,-.5]);a=math.radians(yaw);right=np.array([-math.cos(a),0,-math.sin(a)]);up=np.array([0,1,0]);toward=np.array([math.sin(a),0,-math.cos(a)])
 img=np.empty((size,size,3),np.uint8);img[:]=[35,52,56];depth=np.full((size,size),-np.inf);tex=np.array(texture.convert('RGBA'));light=(toward*.75+np.array([-.35,.7,-.15]));light/=np.linalg.norm(light)
 def matrix(n):
  if n in mats:return mats[n]
  b=bs[n];p=np.array(b['pivot'])*[-1,1,1];r=b.get('rotation',[0,0,0]).copy();s=np.ones(3)
  if n.startswith('lid_'):s[1]=.07
  if n=='lower_lip':s[1]=1-.12*.92
  if n.startswith('hair_front_'):r[2]=24 if n.endswith('left')else -24
  if n=='hair_back':r[0]=0
  m=np.eye(4);m[:3,:3]=rot(r)@np.diag(s);m[:3,3]=p-m[:3,:3]@p
  if 'parent'in b:m=matrix(b['parent'])@m
  mats[n]=m;return m
 def tri(q,uv,shade):
  xmin=max(0,int(np.floor(q[:,0].min())));xmax=min(size-1,int(np.ceil(q[:,0].max())));ymin=max(0,int(np.floor(q[:,1].min())));ymax=min(size-1,int(np.ceil(q[:,1].max())))
  if xmin>xmax or ymin>ymax:return
  den=(q[1,1]-q[2,1])*(q[0,0]-q[2,0])+(q[2,0]-q[1,0])*(q[0,1]-q[2,1])
  if abs(den)<1e-8:return
  yy,xx=np.mgrid[ymin:ymax+1,xmin:xmax+1];xx=xx+.5;yy=yy+.5
  w0=((q[1,1]-q[2,1])*(xx-q[2,0])+(q[2,0]-q[1,0])*(yy-q[2,1]))/den;w1=((q[2,1]-q[0,1])*(xx-q[2,0])+(q[0,0]-q[2,0])*(yy-q[2,1]))/den;w2=1-w0-w1
  zz=w0*q[0,2]+w1*q[1,2]+w2*q[2,2];sl=np.s_[ymin:ymax+1,xmin:xmax+1];ok=(w0>=-1e-8)&(w1>=-1e-8)&(w2>=-1e-8)&(zz>depth[sl]+1e-8)
  ux=np.clip((w0*uv[0,0]+w1*uv[1,0]+w2*uv[2,0]).astype(int),0,tex.shape[1]-1);uy=np.clip((w0*uv[0,1]+w1*uv[1,1]+w2*uv[2,1]).astype(int),0,tex.shape[0]-1);colour=tex[uy,ux];ok &=colour[:,:,3]>127
  depth[sl][ok]=zz[ok];img[sl][ok]=np.clip(colour[:,:,:3][ok]*shade,0,255).astype(np.uint8)
 for n,b in bs.items():
  # Head and its descendants only, at identical neutral transform on both versions.
  parent=n
  while parent in bs and parent!='head':parent=bs[parent].get('parent')
  if parent!='head' or (cutaway and n not in face_names):continue
  m=matrix(n)
  for c in b.get('cubes',[]):
   origin=np.array(c['origin']);dim=np.array(c['size'])
   for f,t in c['uv'].items():
    raw=(origin+np.array(V[f])*dim)*[-1,1,1];v=(np.c_[raw,np.ones(4)]@m.T)[:,:3];normal=m[:3,:3]@(np.array(N[f])*[-1,1,1]);normal/=max(1e-8,np.linalg.norm(normal))
    if normal@toward<=.00001:continue
    q=np.c_[size/2+(v-target)@right*scale,size/2-(v-target)@up*scale,(v-target)@toward]
    u,w=t['uv'],t['uv_size'];uv=np.array([[u[0],u[1]],[u[0]+w[0]-1e-5,u[1]],[u[0]+w[0]-1e-5,u[1]+w[1]-1e-5],[u[0],u[1]+w[1]-1e-5]])
    shade=.68+.30*max(0,normal@light)
    for ids in [(0,1,2),(0,2,3)]:tri(q[list(ids)],uv[list(ids)],shade)
 return Image.fromarray(img).resize((800,800),Image.Resampling.LANCZOS)
for state in ['before','after']:
 if state=='before':geo=json.loads(Z.read('KelMagic/src/main/resources/assets/kelmagic/geo/red_mage.geo.json'));texture=Image.open(io.BytesIO(Z.read('KelMagic/src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png')))
 else:geo=json.loads((P/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json').read_text());texture=Image.open(P/'src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png')
 for name,yaw,cut in [('front',0,False),('angle45',45,False),('side',90,False),('side-cutaway',90,True),('angle45-cutaway',45,True)]:render(geo,texture,yaw,cut).save(O/f'{state}-{name}.png')
print('Rendered unchanged and new game geometry in identical neutral pose: 10 views')
