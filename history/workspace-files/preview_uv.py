from PIL import Image,ImageDraw,ImageOps
from pathlib import Path
import json,math
r=Path('/data/KelMagic');bp=json.loads((r/'art/generated/garment-blueprint-1.5.0.json').read_text());out=Image.new('RGB',(760,530),'#24212e');d=ImageDraw.Draw(out)
for variant,baseX in zip(bp,[180,570]):
 robe=variant['robe'];tex=Image.open(r/('src/main/resources/assets/kelmagic/textures/models/armor/'+('mage_robes_layer_1.png' if robe else 'arcane_plate_layer_1.png'))).convert('RGBA')
 if robe:
  tint=Image.new('RGBA',tex.size,'#635875');import numpy as np
  arr=np.asarray(tex).copy();arr[:,:,:3]=(arr[:,:,:3].astype(float)*np.array([99,88,117])/255).astype('uint8');tex=Image.fromarray(arr);tex.alpha_composite(Image.open(r/'src/main/resources/assets/kelmagic/textures/models/armor/mage_robes_layer_1_overlay.png'))
 # Front orthographic face study, not a native game screenshot. Show paired pieces at their physical locations.
 for q in sorted(variant['pieces'],key=lambda q:q['pos'][2]+q['pivot'][2],reverse=True):
  x,y,z=q['pos'];w,h,dep=q['size'];u,v=q['uv'];iw,ih,id=q['uvsize'];px,py,pz=q['pivot'];bx,by={'head':(0,0),'body':(0,0),'right_arm':(-5,2),'left_arm':(5,2),'right_leg':(-1.9,12),'left_leg':(1.9,12)}[q['bone']]
  patch=tex.crop((u+id,v+id,u+id+iw,v+id+ih))
  if q['name'] in ['lapel1','front1']:patch=ImageOps.mirror(patch)
  patch=patch.resize((max(1,round(w*12)),max(1,round(h*12))),Image.Resampling.NEAREST)
  out.paste(patch,(round(baseX+(x+px+bx)*12),round(145+(y+py+by)*12)),patch)
 d.text((baseX-110,15),'Mage robe' if robe else 'Arcane armor',fill='white')
 d.text((baseX-125,38),'Front UV study (not native client)',fill='#c9bdcf')
out.save('/data/uv-front-study.png')
# Paired panels isolate the exact user-reported trim; right texture is reflected by CubeListBuilder.mirror.
for variant in bp:
 robe=variant['robe'];name='mage_robes_layer_1' if robe else 'arcane_plate_layer_1';tex=Image.open(r/f'src/main/resources/assets/kelmagic/textures/models/armor/{name}.png')
 canvas=Image.new('RGB',(430,340),'#24212e');dr=ImageDraw.Draw(canvas)
 for row,stem in enumerate(['lapel','front']):
  for j,sign in enumerate(['-1','1']):
   q=next(q for q in variant['pieces'] if q['name']==stem+sign);u,v=q['uv'];w,h,dep=q['uvsize'];p=tex.crop((u+dep,v+dep,u+dep+w,v+dep+h)).convert('RGBA')
   if sign=='1':p=ImageOps.mirror(p)
   p=p.resize((w*12,h*12),Image.Resampling.NEAREST);canvas.paste(p,(160+j*70,35+row*145),p)
  dr.text((12,65+row*145),stem+' paired trim',fill='white')
 canvas.save('/data/'+('robe' if robe else 'armor')+'-trim-preview.png')
