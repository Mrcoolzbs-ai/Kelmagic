from pathlib import Path
from PIL import Image
import json,math,random,zipfile
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';z=zipfile.ZipFile(r/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
# Match vanilla ground/wall/upper planes exactly, retaining own animated sprites.
state=json.loads(z.read('assets/minecraft/blockstates/fire.json'))
def convert(v):
 if isinstance(v,dict):return {k:convert(x) for k,x in v.items()}
 if isinstance(v,list):return [convert(x) for x in v]
 if isinstance(v,str) and v.startswith('minecraft:block/fire_'):return v.replace('minecraft:block/','kelmagic:block/magic_')
 return v
(a/'blockstates/magic_fire.json').write_text(json.dumps(convert(state),indent=2))
for name in ['fire_floor0','fire_floor1','fire_side0','fire_side1','fire_side_alt0','fire_side_alt1','fire_up0','fire_up1','fire_up_alt0','fire_up_alt1']:
 data=json.loads(z.read(f'assets/minecraft/models/block/{name}.json'));data['render_type']='minecraft:cutout'
 data=convert(data)
 # Parent templates retain vanilla planar geometry; only own texture references change.
 for k,v in list(data.get('textures',{}).items()):
  if v in ['minecraft:block/fire_0','minecraft:block/fire_1']:data['textures'][k]=v.replace('minecraft:block/fire','kelmagic:block/magic_fire')
 (a/f'models/block/magic_{name}.json').write_text(json.dumps(data,indent=2))
# Denser continuous foot and flame tongues, full-height like vanilla, with own generated noise.
for kind in [0,1]:
 im=Image.new('RGBA',(32,512))
 for frame in range(16):
  for y in range(32):
   for x in range(32):
    t=frame*math.tau/16;h=y/31
    wave=sum(max(0,1-abs(x-(j*6+2+math.sin(t+j*1.9+(31-y)*.13)*(1.8+h)))/(1.2+4*h)) for j in range(6))
    threshold=.48+.20*math.sin(x*.7+t+kind)+.1*math.sin(y*.5-x*.6-t)
    if wave>threshold and (h>.15 or math.sin(x*1.4+t)>.3):
     bright=min(1,max(0,(wave-.5)*.6+h*.5));im.putpixel((x,frame*32+y),(int(102+125*bright),int(22+127*bright),int(192+60*bright),255))
 im.save(a/f'textures/block/magic_fire_{kind}.png')
 (a/f'textures/block/magic_fire_{kind}.png.mcmeta').write_text(json.dumps({'animation':{'frametime':2}}))
print('Vanilla-compatible multipart fire planes and original purple animation')
