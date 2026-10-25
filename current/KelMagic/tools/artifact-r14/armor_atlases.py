from pathlib import Path
from PIL import Image,ImageDraw,ImageColor
import math
R=Path(__file__).resolve().parents[2];out=R/'src/main/resources/assets/kelmagic/textures/models/armor'
PAL={'deep_source':('#302343','#453259','#8bcfc9','#d4aa52'),'steady_anchor':('#3b4357','#606c80','#72bfc1','#bd9759'),'grove_keeper':('#253e40','#3b6260','#86cba6','#d0b975'),'strict_negation':('#282435','#464052','#93bdce','#b7b8c7')}
def mod(c,m):return tuple(max(0,min(255,int(v*m))) for v in ImageColor.getrgb(c))+(255,)
def paint(d,u,v,w,h,depth,base):
 # Minecraft cube-unwrapping layout: restrained face shading, no repeated all-over print.
 rects=[(u,v+depth,depth,h,.72),(u+depth,v+depth,w,h,1.0),(u+depth+w,v+depth,depth,h,.83),(u+2*depth+w,v+depth,w,h,.9),(u+depth,v,w,depth,1.1),(u+depth+w,v,w,depth,.65)]
 for x,y,ww,hh,shade in rects:
  x,y,ww,hh=round(x),round(y),max(1,round(ww)),max(1,round(hh));d.rectangle((x,y,x+ww-1,y+hh-1),fill=mod(base,shade));
  if ww>3 and hh>3:d.line([(x,y),(x,y+hh-1)],fill=mod(base,shade*.78));d.line([(x+1,y),(x+ww-2,y)],fill=mod(base,shade*1.12))
for name,(base,light,magic,metal) in PAL.items():
 im=Image.new('RGBA',(128,128),mod(base,.9));d=ImageDraw.Draw(im)
 # Shared shell regions use subtle solid material, not global grids.
 for u,v,w,h,dep in [(0,0,8,8,8),(40,0,1,7,9),(0,40,7,7,1),(16,16,8,12,4),(40,16,4,11,4),(0,16,4,11,4),(64,0,10,3,6),(64,16,10,2,10),(64,32,5,3,2),(96,16,6,4,6),(96,32,5,3,5),(64,64,10,2,6),(64,80,9,9,1),(88,80,9,10,1),(96,64,7,6,1),(80,20,8,2,1),(96,0,2,4,6)]:paint(d,u,v,w,h,dep,base)
 # Front body panel: border and a small articulated neckline.
 d.line([(20,23),(20,30)],fill=magic);d.line([(27,23),(27,30)],fill=magic);d.line([(21,20),(23,22),(25,22),(27,20)],fill=metal)
 d.line([(20,31),(27,31)],fill=metal)
 # Sleeves, trousers and footwear have narrow hems rather than noisy symbols.
 for x in [44,4]:
  d.line([(x,28),(x+3,28)],fill=metal);d.line([(x,29),(x+3,29)],fill=magic)
  d.line([(x,22),(x,26)],fill=mod(light,.8))
 # Front/back skirt UVs: sparse vertical trim and a gold hem.
 for x,y,h in [(65,81,9),(89,81,10)]:
  d.line([(x,y+1),(x,y+h-2)],fill=magic);d.line([(x+7,y+1),(x+7,y+h-2)],fill=magic);d.line([(x,y+h-1),(x+8,y+h-1)],fill=metal)
 # All extra metallic islands: intentionally local edge accents.
 for x,y,w,h in [(64,64,30,8),(96,16,28,8),(64,0,31,9),(64,32,15,6)]:
  d.line([(x,y),(x+w,y)],fill=metal);d.line([(x,y+h),(x+w,y+h)],fill=mod(metal,.8))
 d.rectangle((65,49,70,54),fill=mod('#184956',1));d.polygon([(68,49),(70,52),(68,55),(65,52)],fill=magic);d.point((67,51),fill='#c5f6e6')
 # Distinct back insignia at the body rear UV, 8x12 pixels.
 if name=='deep_source':
  d.line([(34,24),(36,22),(38,24),(36,26),(34,24)],fill=magic);d.line([(36,26),(36,29)],fill=metal)
  # Hood hem and cheek-edge threads.
  d.line([(9,10),(17,10)],fill=metal);d.line([(49,10),(49,15)],fill=metal)
 elif name=='grove_keeper':
  d.line([(36,22),(36,29)],fill=metal);d.line([(36,24),(33,22)],fill=magic);d.line([(36,26),(39,24)],fill=magic)
  d.rectangle((80,48,90,57),fill=mod(light,.9));d.line([(82,50),(83,54),(84,52)],fill=magic)
 elif name=='strict_negation':
  d.line([(33,23),(39,29)],fill=metal);d.line([(33,29),(39,23)],fill=metal)
  d.rectangle((8,10,15,12),fill='#111622');d.line([(8,11),(15,11)],fill=magic)
  d.rectangle((81,21,88,22),fill='#10131e');d.line([(81,21),(88,21)],fill=magic)
  d.line([(98,66),(101,69)],fill=metal);d.line([(101,66),(98,69)],fill=metal)
 else:
  d.rectangle((34,23,38,28),outline=metal);d.line([(36,23),(36,28)],fill=magic)
  d.rectangle((8,10,15,12),fill='#151c29');d.line([(8,10),(15,10)],fill=magic)
 for layer in [1,2]:im.save(out/f'{name}_layer_{layer}.png')
print('Painted restrained, UV-specific cloth / metal / grove / negation armor atlases')
