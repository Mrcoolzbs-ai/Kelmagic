"""R3 TEST material/UV repair. Seat, envelope and collision remain unchanged."""
from pathlib import Path
import json,math
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[2]/'src/main/resources/assets/kelmagic'
# Exact bilateral woven velvet, with centred diamond/button rhythm.
im=Image.new('RGBA',(32,32))
for y in range(32):
 for x in range(16):
  u=x+.5;v=y+.5;dist=min(abs(u-cx)+abs(v-cy) for cx in [8,24] for cy in [-8,8,24,40]);shade=round(4*math.cos(dist*math.pi/8));c=(120+shade,17+shade//2,40+shade//2,255);im.putpixel((x,y),c);im.putpixel((31-x,y),c)
d=ImageDraw.Draw(im)
for x in [7,23]:
 for y in [7,23]:d.rectangle((x-1,y-1,x+1,y+1),fill='#b38a35')
for y in range(32):
 for x in range(16):im.putpixel((31-x,y),im.getpixel((x,y)))
im.save(R/'textures/block/throne_velvet.png')
# Preserve original gold/wood/gem palette, mirror grain and highlights.
for name in ['gold','wood','gem']:
 p=R/f'textures/block/throne_{name}.png';im=Image.open(p).convert('RGBA');w,h=im.size
 for y in range(h):
  for x in range(w//2):im.putpixel((w-1-x,y),im.getpixel((x,y)))
 im.save(p)
for half,offset in [('lower',0),('upper',16)]:
 p=R/f'models/block/mage_throne_{half}.json';m=json.loads((Path(__file__).parent/'baseline-throne'/p.name).read_text());out=[]
 for e in m['elements']:
  a,b=e['from'],e['to'];materials={f['texture'] for f in e['faces'].values()}
  # Replace the fragmented front infill and off-centre decorative studs only.
  if a[2]>=16.37 and b[2]<=16.59 and (materials=={'#velvet'} or materials=={'#gold'} and abs(a[2]-16.38)<1e-6):continue
  for face,f in e['faces'].items():
   if f['texture'] in ('#gold','#wood'):
    if face in ('north','south'):f['uv']=[a[0],(32-b[1]-offset)/2,b[0],(32-a[1]-offset)/2]
    elif face in ('up','down'):f['uv']=[a[0],a[2]*.8,b[0],b[2]*.8]
    else:f['uv']=[a[2]*.8,(32-b[1]-offset)/2,b[2]*.8,(32-a[1]-offset)/2]
   if f['texture']=='#velvet':
    if face in ('north','south') and a[2]>16:
     f['uv']=[(a[0]-3)*1.6,(26-b[1]-offset), (b[0]-3)*1.6,(26-a[1]-offset)]
    elif face in ('north','south'):f['uv']=[a[0],0,b[0],max(.25,b[1]-a[1])]
    elif face in ('up','down'):f['uv']=[a[0],a[2]*.8,b[0],b[2]*.8]
    else:f['uv']=[0,0,16,16]
  out.append(e)
 lo,hi=(10,16) if half=='lower' else (0,10)
 out.append({'from':[3,lo,16.55],'to':[13,hi,16.55],'faces':{'north':{'texture':'#velvet','uv':[0,26-hi-offset,16,26-lo-offset]}}})
 for y in ([12] if half=='lower' else [0,4,8]):
  for x in [4,6,8,10,12]:
   out.append({'from':[x-.225,y,16.38],'to':[x+.225,y+.45,16.55],'faces':{f:{'texture':'#gold','uv':[0,0,16,16]} for f in ['north','south','up','down','east','west']}})
 m['elements']=out;p.write_text(json.dumps(m,indent=2)+'\n')
