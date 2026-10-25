"""CPU orthographic proof of actual baked Java mesh + actual UV atlases.
NOT a Minecraft screenshot. Uses no generative or substitute armor meshes.
"""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parents[2]; A=R/'src/main/resources/assets/kelmagic'; OUT=R/'art/artifact-r14';OUT.mkdir(parents=True,exist_ok=True)
fontpath='/usr/share/fonts/google-noto-vf/NotoSans[wght].ttf'
if not Path(fontpath).exists():fontpath='/usr/share/fonts/dejavu/DejaVuSans.ttf'
def font(n):
 try:return ImageFont.truetype(fontpath,n)
 except:return ImageFont.load_default()
def project(points,yaw,pitch):
 y=math.radians(yaw);p=math.radians(pitch)
 rotation=np.array([[math.cos(y),0,math.sin(y)],[math.sin(p)*math.sin(y),math.cos(p),-math.sin(p)*math.cos(y)],[-math.cos(p)*math.sin(y),math.sin(p),math.cos(p)*math.cos(y)]])
 return points@rotation.T

def render(polygons,textures,w,h,yaw,pitch,scale,offset):
 pix=np.zeros((h,w,3),dtype=np.uint8);pix[:]=[25,29,48];depth=np.full((h,w),np.inf);light=np.array([-.5,-.8,-.7]);light/=np.linalg.norm(light)
 for poly,texture in zip(polygons,textures):
  pts=np.array(poly);xyz=pts[:,:3];q=project(xyz,yaw,pitch);q[:,0]=q[:,0]*scale+offset[0];q[:,1]=q[:,1]*scale+offset[1]
  normal=np.cross(xyz[1]-xyz[0],xyz[2]-xyz[0]);normal/=max(1e-8,np.linalg.norm(normal));shade=.62+.38*abs(normal@light)
  for ids in [(0,1,2),(0,2,3)]:
   tri=q[list(ids)];uv=pts[list(ids),3:5];x0=max(0,int(np.floor(tri[:,0].min())));x1=min(w-1,int(np.ceil(tri[:,0].max())));y0=max(0,int(np.floor(tri[:,1].min())));y1=min(h-1,int(np.ceil(tri[:,1].max())))
   if x1<x0 or y1<y0:continue
   X,Y=np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5)
   den=(tri[1,1]-tri[2,1])*(tri[0,0]-tri[2,0])+(tri[2,0]-tri[1,0])*(tri[0,1]-tri[2,1])
   if abs(den)<1e-8:continue
   aa=((tri[1,1]-tri[2,1])*(X-tri[2,0])+(tri[2,0]-tri[1,0])*(Y-tri[2,1]))/den
   bb=((tri[2,1]-tri[0,1])*(X-tri[2,0])+(tri[0,0]-tri[2,0])*(Y-tri[2,1]))/den;cc=1-aa-bb
   zz=aa*tri[0,2]+bb*tri[1,2]+cc*tri[2,2];region=depth[y0:y1+1,x0:x1+1];mask=(aa>=-1e-6)&(bb>=-1e-6)&(cc>=-1e-6)&(zz<region)
   uu=aa*uv[0,0]+bb*uv[1,0]+cc*uv[2,0];vv=aa*uv[0,1]+bb*uv[1,1]+cc*uv[2,1]
   tw,th=texture.size;src=np.array(texture.convert('RGBA'));tx=np.clip((uu*tw).astype(int),0,tw-1);ty=np.clip((vv*th).astype(int),0,th-1);rgba=src[ty,tx];mask&=rgba[:,:,3]>0
   color=(rgba[:,:,:3]*shade).astype(np.uint8);pix[y0:y1+1,x0:x1+1][mask]=color[mask];region[mask]=zz[mask]
 return Image.fromarray(pix)
if (OUT/'baked-armor.json').exists():
 data=json.loads((OUT/'baked-armor.json').read_text());names={'deep_source':'Облачение глубокого источника','steady_anchor':'Латы неподвижной опоры','grove_keeper':'Покров хранителя сумеречной рощи','strict_negation':'Доспехи строгого отрицания'}
 for set,polys in data.items():
  tex=Image.open(A/'textures/models/armor'/f'{set}_layer_1.png');sheet=Image.new('RGB',(1200,660),'#191d30');d=ImageDraw.Draw(sheet);d.text((28,18),names[set],font=font(25),fill='#e6d8ac');d.text((28,54),'Технический UV-просмотр реальной геометрии • не кадр Minecraft',font=font(14),fill='#98a9c8')
  for i,angle in enumerate([0,32,165]):
   panel=render(polys,[tex]*len(polys),390,540,angle,10,14,(195,165));sheet.paste(panel,(5+i*400,92));d.text((i*400+25,627),['Спереди','Три четверти','Сзади'][i],font=font(16),fill='#abc7c8')
  sheet.save(OUT/f'armor-{set}.png')
# Block geometry proof with Minecraft JSON element rotations and textures.
def blockpolys(id):
 model=json.loads((A/'models/block'/f'{id}.json').read_text());polys=[];textures=[]
 for e in model['elements']:
  x,y,z=e['from'];X,Y,Z=e['to'];faces={'north':[(X,y,z),(x,y,z),(x,Y,z),(X,Y,z)],'south':[(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],'west':[(x,y,z),(x,y,Z),(x,Y,Z),(x,Y,z)],'east':[(X,y,Z),(X,y,z),(X,Y,z),(X,Y,Z)],'up':[(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)],'down':[(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)]}
  for f,points in faces.items():
   pts=np.array(points,dtype=float)
   if 'rotation' in e:
    rot=e['rotation'];axis='xyz'.index(rot['axis']);a=math.radians(rot['angle']);co,si=math.cos(a),math.sin(a);mat=np.eye(3);i,j=[n for n in range(3) if n!=axis];mat[i,i]=mat[j,j]=co;mat[i,j]=-si;mat[j,i]=si;origin=np.array(rot['origin']);pts=(pts-origin)@mat.T+origin
   pts[:,1]=-pts[:,1];pts[:,0]-=8;pts[:,2]-=8
   uv=[(1,0),(0,0),(0,1),(1,1)];polys.append([[*point,*xy] for point,xy in zip(pts,uv)]);tname=model['textures'][e['faces'][f]['texture'][1:]].split(':')[1];texture=Image.open(A/'textures'/f'{tname}.png');texture=texture.crop((0,0,16,16));textures.append(texture)
 return polys,textures
ids=['mana_cup_4','mana_bowl_4','mana_basin_4','delayed_sound_crystal','crystal_striker','forbidden_threshold_base','admission_obelisk','night_shadow_catcher','grove_mana_bowl','mute_sky_obelisk','fleeting_path']
sheet=Image.new('RGB',(1200,math.ceil(len(ids)/4)*290),'#191d30');d=ImageDraw.Draw(sheet)
for i,id in enumerate(ids):
 polys,textures=blockpolys(id);panel=render(polys,textures,300,250,28,16,9,(150,210));x=(i%4)*300;y=(i//4)*290;sheet.paste(panel,(x,y));d.text((x+15,y+260),id,font=font(13),fill='#c6c0d9')
sheet.save(OUT/'blocks-proof.png')
print('Rendered actual baked armor and block JSON technical proofs')
