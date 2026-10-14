from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter
import random
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';out=a/'textures/item'
# Original hand-designed pixel art, no third-party assets.
edge='#121020'; gold='#be8847';light='#f4d6a0';cyan='#95fff0'
for n in ['mana_core','magic_ingot']:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if n=='mana_core':
  d.polygon([(16,1),(22,7),(24,16),(21,25),(16,31),(10,26),(7,17),(9,8)],fill=edge)
  d.polygon([(16,3),(21,9),(22,17),(19,25),(16,28),(11,24),(9,16),(11,9)],fill='#164957')
  d.polygon([(16,4),(16,16),(10,16),(12,9)],fill='#c6fff3')
  d.polygon([(16,4),(20,9),(21,16),(16,16)],fill='#65d8e2')
  d.polygon([(10,17),(16,17),(16,27),(12,23)],fill='#368cba')
  d.polygon([(17,17),(21,17),(19,23),(17,27)],fill='#63eece')
  for pts in [[(4,10),(9,7),(11,9),(7,12),(6,19),(10,23),(9,26),(4,22)],[(24,7),(28,10),(28,22),(23,26),(21,23),(25,19),(25,12),(22,9)]]:
   d.polygon(pts,fill=edge);d.line(pts,fill=gold,width=1)
  d.line([(5,11),(9,9)],fill=light);d.line([(26,13),(26,19)],fill=light)
  d.line([(10,16),(21,16)],fill='#eefef1');d.point((16,6),fill='white')
  d.rectangle((2,15,5,17),fill=gold);d.point((3,15),fill=light);d.rectangle((28,15,30,17),fill=gold)
 else:
  d.polygon([(2,14),(8,6),(25,6),(30,13),(28,24),(5,27),(1,21)],fill=edge)
  d.polygon([(4,14),(9,8),(24,8),(28,13),(23,19),(6,21)],fill='#8784b0')
  d.polygon([(6,21),(23,19),(27,24),(6,25),(3,20)],fill='#403653')
  d.polygon([(24,19),(28,14),(27,23),(24,24)],fill='#5b4875')
  d.line([(5,14),(10,9),(23,9)],fill='#cec6e5');d.line([(6,25),(25,23)],fill=gold)
  d.polygon([(10,14),(12,11),(20,11),(23,14),(20,17),(12,18)],fill='#303649')
  d.line([(12,16),(14,12),(16,15),(18,12),(20,15)],fill=cyan,width=1)
  d.point((13,11),fill=light);d.point((22,14),fill=light)
  for x,y in [(7,17),(24,15)]:d.rectangle((x,y,x+1,y+1),fill=gold)
 im.save(out/(n+'.png'))
# New armor atlases: textile weave, embossed panels, contrasting seams and gilded inlay.
afolder=a/'textures/models/armor';rng=random.Random(130)
for robe in [False,True]:
 tex=Image.new('RGBA',(128,128));d=ImageDraw.Draw(tex)
 for y in range(128):
  for x in range(64):
   if robe:
    v=185 + (x%8)*2 -(y%16)//2+rng.choice([-2,0,2]);col=(v,v-3,v+2,255)
   else:
    v=38+(x%12)*2+(y%16)//2+rng.choice([-2,0,1]);col=(v+5,v,v+18,255)
   tex.putpixel((x,y),col)
 for y in range(0,128,16):
  d.line((0,y,63,y),fill='#9d909f' if robe else '#a188a8')
  d.line((0,y+15,63,y+15),fill='#79747f' if robe else '#241e36')
 for x in range(4,64,12):d.line((x,0,x,127),fill='#aba0ac' if robe else '#302739')
 # dedicated engraved cloth/plate panel u0 v80; front offset d=1
 d.rectangle((0,80,32,110),fill='#c4bacb' if robe else '#332d47')
 for x in [1,8,16,23]:
  d.line((x,81,x,109),fill='#aca1b4' if robe else '#766384')
  for y in range(83,110,8):d.line([(x,y),(x+2,y+2),(x,y+4)],fill='#ddd3dc' if robe else '#a994ab')
 ov=Image.new('RGBA',(128,128));od=ImageDraw.Draw(ov)
 # brass region used by geometric trim
 od.rectangle((64,0,127,30),fill='#b68b50')
 for y in range(0,31,4):od.line((64,y,127,y),fill='#f1d399');od.line((64,y+3,127,y+3),fill='#65452e')
 # engraved buckle; positioned for front UV (u+depth,v+depth)
 od.rectangle((80,32,112,55),fill='#211c30')
 od.rectangle((81,33,87,39),outline='#dfb86f');od.line([(83,38),(83,34),(85,34),(85,36),(84,37)],fill='#8feede')
 # leather accessories and dark hood liner
 od.rectangle((64,64,127,127),fill='#292331')
 for y in range(66,128,6):od.line((64,y,127,y),fill='#3c3043')
 for x in range(66,128,8):
  for y in range(66,128,8):od.point((x,y),fill='#b69a71')
 for layer in [1,2]:
  if robe:
   tex.save(afolder/f'mage_robes_layer_{layer}.png');ov.save(afolder/f'mage_robes_layer_{layer}_overlay.png')
  else:
   tex.alpha_composite(ov);tex.save(afolder/f'arcane_plate_layer_{layer}.png')
# Shade existing item silhouettes, add decorative pixel inlay; preserve separate dye mask.
for prefix in ['arcane','mage']:
 for slot in ['helmet','chestplate','leggings','boots']:
  n=prefix+'_'+slot;im=Image.open(out/(n+'.png')).convert('RGBA');d=ImageDraw.Draw(im)
  for y in range(32):
   for x in range(32):
    red,g,b,alpha=im.getpixel((x,y))
    if not alpha or max(red,g,b)<35:continue
    if prefix=='arcane' and b>=red:
     shade=int(12+(31-y)/3);im.putpixel((x,y),(min(255,red+shade),max(0,g-8),min(255,b+18),alpha))
    elif prefix=='mage' and abs(red-g)<25 and b>100:
     v=max(90,min(245,red+(15-y)//2));im.putpixel((x,y),(v,v-5,min(255,v+9),alpha))
  if prefix=='mage':
   ov=Image.open(out/(n+'_overlay.png')).convert('RGBA');od=ImageDraw.Draw(ov)
   if slot=='helmet':od.line([(7,14),(7,22),(10,22)],fill=gold);od.line([(25,14),(25,22),(22,22)],fill=gold)
   if slot=='chestplate':od.line([(11,10),(11,20),(21,20),(21,10)],fill=gold);od.rectangle((21,20,24,23),fill='#423248')
   ov.save(out/(n+'_overlay.png'))
  im.save(out/(n+'.png'))
# Proper opaque fallback tint used in QA preview.
items=['mana_core','magic_ingot']+[f'{p}_{s}' for p in ['arcane','mage'] for s in ['helmet','chestplate','leggings','boots']]
sheet=Image.new('RGB',(720,210),'#151222');sd=ImageDraw.Draw(sheet)
for i,n in enumerate(items):
 im=Image.open(out/(n+'.png')).convert('RGBA')
 if n.startswith('mage'):
  for y in range(32):
   for x in range(32):
    rr,g,b,aa=im.getpixel((x,y));im.putpixel((x,y),(rr*64//255,g*52//255,b*79//255,aa))
  im.alpha_composite(Image.open(out/(n+'_overlay.png')))
 x=12+(i%5)*140;y=10+(i//5)*100;im=im.resize((80,80),Image.Resampling.NEAREST);sheet.paste(im,(x,y),im);sd.text((x,y+82),n,fill='#d7b987')
sheet.save(r/'art/generated/revision13-icons.png')
print('Original art updated')
