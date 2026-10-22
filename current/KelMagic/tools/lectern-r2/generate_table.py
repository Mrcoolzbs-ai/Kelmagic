"""TEST-only lectern: bounded geometry, buried support seams, physical-scale UVs."""
from pathlib import Path
import json,math,random
from PIL import Image,ImageDraw
P=Path(__file__).resolve().parents[2];R=P/'src/main/resources/assets/kelmagic';T=R/'textures/block';T.mkdir(exist_ok=True)
rng=random.Random(2718)
def tex(name,base):
 im=Image.new('RGBA',(64,64),base);d=ImageDraw.Draw(im)
 for y in range(64):
  for x in range(64):
   a=rng.choice([-2,-1,0,0,0,1,2]);c=tuple(max(0,min(255,v+a)) for v in base[:3])+(255,);d.point((x,y),fill=c)
 if name=='top':
  for box,col in [((0,0,63,63),'#8c7548'),((1,1,62,62),'#c1a567'),((3,3,60,60),'#526558'),((5,5,58,58),'#ae955e'),((6,6,57,57),'#203c37')]:d.rectangle(box,outline=col)
  for x in [20,42]:d.line((x,8,x,55),fill='#304b42')
  for x,y,sx,sy in [(10,10,1,1),(53,10,-1,1),(10,53,1,-1),(53,53,-1,-1)]:
   d.line((x,y,x+5*sx,y),fill='#998959',width=1);d.line((x,y,x,y+5*sy),fill='#998959');d.line((x+3*sx,y+3*sy,x+5*sx,y+3*sy),fill='#6a9b8a');d.point((x+4*sx,y+2*sy),fill='#6a9b8a')
 elif name=='side':
  for x in [1,61]:d.line((x,0,x,63),fill='#aa945c');d.line((x+1,0,x+1,63),fill='#536556')
  for y in [2,60]:d.line((0,y,63,y),fill='#9f8a56')
 elif name=='trim':
  for y,c in [(0,'#c1ac73'),(1,'#9e8852'),(3,'#40554a'),(12,'#8c7950'),(14,'#beaa70'),(15,'#485c4e')]:d.line((0,y,63,y),fill=c)
 elif name=='ink':
  d.rectangle((0,0,63,8),fill='#131e25');d.rectangle((2,10,61,12),fill='#8b7a50');d.rectangle((3,56,60,59),fill='#736647')
 elif name=='magic':
  for i,c in [(0,'#4b817c'),(4,'#69b9ac'),(10,'#9fdfc9'),(18,'#c9f3d4'),(26,'#ecfbd6')]:d.rectangle((i,i,63-i,63-i),fill=c)
 elif name=='quill':
  d.line((2,0,2,63),fill='#f0e2b7');d.line((5,0,5,63),fill='#b9b08b')
 im.save(T/f'archive_{name}.png')
for n,c in [('top',(31,59,53,255)),('side',(29,52,48,255)),('trim',(126,111,67,255)),('ink',(27,37,43,255)),('magic',(117,201,185,255)),('quill',(211,205,169,255))]:tex(n,c)
E=[];bounds=[];accessories=False
def box(a,b,t='side',omit=(),rot=None,full=False):
 if accessories:
  a,b=[16-b[0],a[1],a[2]],[16-a[0],b[1],b[2]]
  if rot:rot=([16-rot[0][0],rot[0][1],rot[0][2]],rot[1],-rot[2] if rot[1]=='z' else rot[2])
 dx,dy,dz=[b[i]-a[i] for i in range(3)];f={}
 for side,w,h in [('up',dx,dz),('down',dx,dz),('north',dx,dy),('south',dx,dy),('west',dz,dy),('east',dz,dy)]:
  if side not in omit:f[side]={'uv':[0,0,16,16] if full else [0,0,w,h],'texture':'#'+t}
 e={'from':a,'to':b,'faces':f}
 if rot:e['rotation']={'origin':rot[0],'axis':rot[1],'angle':rot[2],'rescale':False}
 E.append(e)
 pts=[]
 for i in range(8):
  q=[b[j] if i>>j&1 else a[j] for j in range(3)]
  if rot:
   o,axis,ang=rot;k='xyz'.index(axis);u,v=(k+1)%3,(k+2)%3;co=math.cos(math.radians(ang));si=math.sin(math.radians(ang));x,y=q[u]-o[u],q[v]-o[v];q[u]=o[u]+co*x-si*y;q[v]=o[v]+si*x+co*y
  pts.append(q)
 bounds.append([min(q[j] for q in pts) for j in range(3)]+[max(q[j] for q in pts) for j in range(3)])
box([.25,0,.25],[15.75,.8,15.75],'trim');box([1.5,.8,2],[14.5,1.5,14],omit=['down'])
for n in range(20):
 z=2+n*.6;h=12.5-math.tan(math.pi/8)*(z-8)-.8/math.cos(math.pi/8)+.15
 box([3,1.5,z],[13,h,z+.6],omit=['up','down','north']+(['south'] if n<19 else []))
box([3,1.5,.9],[13,16,2],omit=['up','down'])
r=([8,12.5,8],'x',22.5)
box([.5,11.7,1],[15.5,12.5,15],'top',rot=r)
box([.5,12.5,14.35],[15.5,13.05,15],'trim',omit=['down'],rot=r)
box([.5,16,.5],[15.5,16.7,2.25])
accessories=True
box([11.8,16.7,.65],[13.7,17,2.1],'trim',omit=['down'])
box([12,17,.8],[13.5,18.25,2],'ink',omit=['down'])
box([12.3,18.25,1.05],[13.2,18.4,1.8],'ink',omit=['down'])
box([9.5,16.7,.8],[10.3,17.15,1.6],'trim',omit=['down'])
rq=([9.9,17.1,1.2],'z',-22.5)
box([9.82,17.1,1.12],[9.98,20.5,1.28],'trim',rot=rq)
for i in range(5):
 y=18+i*.4;w=.5 if i<3 else .35 if i==3 else .16
 box([9.98,y,1.12],[9.98+w,y+.36,1.28],'quill',rot=rq)
box([1.2,16.7,.65],[3.2,17.05,2.15],'trim',omit=['down'])
box([2.05,17.05,1.25],[2.35,18.25,1.55],'trim',omit=['up','down'])
box([1.55,18.25,.85],[2.85,20.1,1.95],'magic',rot=([2.2,19.175,1.4],'z',45),full=True)
box([1.4,20.1,.7],[3,20.4,2.1],'trim',omit=['down'])
assert all(b[0]>=0 and b[2]>=0 and b[3]<=16 and b[5]<=16 for b in bounds)
m={'parent':'minecraft:block/block','ambientocclusion':False,'textures':{n:f'kelmagic:block/archive_{n}' for n in ['top','side','trim','ink','magic','quill']},'elements':E};m['textures']['particle']='#side'
(R/'models/block/archivist_table.json').write_text(json.dumps(m,indent=2)+'\n')
f=P/'src/main/java/dev/kelmagic/archive/ArchiveTableBlock.java';s=f.read_text();i=s.index('  boxes.add(');j=s.index('  for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL)',i)
s=s[:i]+''.join('  boxes.add(new double[]{'+','.join(f'{v:.9f}' for v in b)+'});\n' for b in bounds)+s[j:];f.write_text(s)
print('Generated',len(E),'bounded elements; six 64px textures; matching collision AABBs')
