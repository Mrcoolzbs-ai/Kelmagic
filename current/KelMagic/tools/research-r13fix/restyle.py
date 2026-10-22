from PIL import Image,ImageDraw
from pathlib import Path
R=Path(__file__).resolve().parents[2]
ITEM=R/'src/main/resources/assets/kelmagic/textures/item'; BLOCK=R/'src/main/resources/assets/kelmagic/textures/block'
T=(0,0,0,0); O=(12,16,35,255); O2=(25,25,58,255); IND=(43,39,88,255); PUR=(82,52,126,255); PUR2=(137,79,199,255); LIL=(199,154,244,255)
TEALD=(24,73,86,255); TEAL=(43,139,151,255); CYAN=(77,211,207,255); CYANH=(172,246,224,255)
GD=(104,66,27,255); GOLD=(195,136,47,255); GOLDH=(246,207,105,255)
SD=(73,72,102,255); SIL=(145,147,181,255); SILH=(224,219,239,255); EMB=(206,66,43,255); EMBH=(247,139,55,255)
def canvas(n=32):
 im=Image.new('RGBA',(n,n),T);return im,ImageDraw.Draw(im)
def poly(d,pts,fill,outline=O,w=2):d.polygon(pts,fill=fill,outline=outline,width=w)
def rect(d,box,fill,outline=O,w=1):d.rectangle(box,fill=fill,outline=outline,width=w)
def save(im,n):im.save(ITEM/(n+'.png'))
def handle(d,a=(7,27),b=(22,9)):
 d.line([a,b],fill=O,width=5);d.line([a,b],fill=TEALD,width=3)
 # living-fiber braid and gold ferrule
 for x,y in [(8,25),(10,22),(12,20),(14,17),(16,15),(18,12)]:d.point((x,y),fill=CYAN);d.point((x+1,y),fill=GOLD)
 d.line([(20,11),(23,8)],fill=GOLDH,width=3)
# tools
im,d=canvas();handle(d);poly(d,[(3,8),(6,4),(24,3),(29,7),(27,11),(20,8),(13,9),(8,13)],EMB);d.line([(7,6),(23,5)],fill=EMBH,width=2);d.line([(11,9),(25,8)],fill=GOLD,width=2);d.point((26,6),fill=GOLDH);save(im,'ember_pickaxe')
im,d=canvas();handle(d);poly(d,[(3,8),(7,4),(13,5),(16,3),(20,5),(26,4),(29,8),(26,11),(20,9),(16,11),(11,9),(7,12)],SD);poly(d,[(13,5),(16,4),(20,6),(17,9)],TEAL,outline=O2,w=1);d.line([(7,6),(12,7)],fill=SILH,width=2);d.line([(21,7),(26,6)],fill=SIL,width=2);d.point((16,6),fill=CYANH);save(im,'vein_pickaxe')
im,d=canvas();handle(d);poly(d,[(4,7),(7,3),(26,3),(29,6),(27,10),(18,8),(12,8),(8,11),(5,10)],SIL);d.line([(8,5),(25,5)],fill=SILH,width=2);d.line([(18,8),(25,8)],fill=CYAN,width=1);d.point((6,8),fill=GOLDH);save(im,'large_field_hoe')
im,d=canvas();handle(d);poly(d,[(19,3),(24,4),(28,9),(26,15),(22,18),(18,15),(17,9)],PUR);poly(d,[(21,5),(24,6),(26,9),(24,13),(21,14),(19,11)],PUR2,outline=O2,w=1);d.line([(21,6),(24,7)],fill=LIL,width=2);d.point((24,11),fill=CYAN);save(im,'surveyor_shovel')
im,d=canvas();handle(d,(8,27),(20,13));poly(d,[(9,5),(14,2),(22,3),(27,8),(28,14),(25,18),(21,17),(24,13),(23,9),(20,7),(16,7),(13,10),(11,15),(7,14)],TEAL);poly(d,[(12,6),(16,4),(21,5),(24,8),(25,11),(23,12),(21,9),(17,8),(14,10),(12,14),(9,13)],SILH,outline=O2,w=1);d.line([(14,6),(20,6)],fill=CYANH,width=2);save(im,'gardener_sickle')
# artifacts
im,d=canvas();poly(d,[(9,4),(20,3),(27,10),(25,23),(17,29),(7,24),(4,13)],IND);poly(d,[(11,7),(19,5),(24,11),(22,20),(16,25),(9,21),(7,13)],PUR2,outline=PUR,w=1);poly(d,[(12,9),(19,7),(22,12),(17,17),(10,16)],LIL,outline=O2,w=1);d.line([(10,22),(22,10)],fill=CYAN,width=2);d.line([(12,23),(24,11)],fill=CYANH,width=1);d.rectangle((5,12,7,17),fill=GOLD);d.rectangle((23,9,26,12),fill=GOLDH);save(im,'return_stone')
im,d=canvas();rect(d,(9,6,23,28),GD,w=2);rect(d,(11,9,21,25),GOLD,w=1);rect(d,(13,11,19,23),TEAL,w=1);d.rectangle((14,12,18,21),fill=CYANH);d.rectangle((15,14,18,19),fill=(220,255,226,255));d.line([(12,7),(14,3),(19,3),(22,7)],fill=O,width=4);d.line([(13,7),(15,5),(19,5),(21,7)],fill=CYAN,width=2);d.rectangle((13,25,19,27),fill=GOLDH);d.point((16,16),fill=(255,255,238,255));save(im,'seeker_lantern')
im,d=canvas();poly(d,[(7,4),(14,3),(17,8),(20,3),(26,5),(25,22),(21,28),(15,27),(11,23),(7,20)],IND);poly(d,[(9,7),(13,6),(16,11),(19,6),(23,7),(22,20),(19,24),(15,24),(11,20)],PUR,outline=O2,w=1);d.line([(10,18),(22,18)],fill=GOLD,width=2);d.line([(11,20),(21,20)],fill=GOLDH,width=1);d.line([(13,8),(15,12)],fill=CYAN,width=2);d.line([(20,8),(18,12)],fill=CYAN,width=2);save(im,'gardener_boots')
im,d=canvas();poly(d,[(4,8),(10,5),(14,9),(15,18),(11,26),(6,24),(3,16)],IND);poly(d,[(18,9),(22,5),(28,8),(29,16),(26,25),(21,26),(17,18)],IND);poly(d,[(6,9),(10,8),(12,11),(12,18),(9,22),(6,20)],PUR);poly(d,[(20,11),(22,8),(26,9),(27,17),(24,22),(21,20)],PUR);d.line([(6,18),(12,18)],fill=GOLDH,width=2);d.line([(21,18),(27,18)],fill=GOLDH,width=2);d.point((9,13),fill=CYAN);d.point((24,13),fill=CYAN);save(im,'builder_gloves')
im,d=canvas();d.ellipse((4,4,28,28),fill=O);d.ellipse((7,7,25,25),fill=GOLD);d.ellipse((11,11,21,21),fill=O2);d.arc((7,7,25,25),200,330,fill=GOLDH,width=3);poly(d,[(13,3),(19,3),(22,7),(19,12),(13,12),(10,7)],TEAL);poly(d,[(14,5),(18,5),(20,7),(18,10),(14,10),(12,7)],CYAN,outline=O2,w=1);d.point((15,6),fill=CYANH);save(im,'collector_ring')
im,d=canvas();d.line([(6,4),(12,10),(16,8),(20,10),(26,4)],fill=O,width=4);d.line([(6,4),(12,10),(16,8),(20,10),(26,4)],fill=GOLD,width=2);poly(d,[(11,11),(21,11),(25,17),(22,26),(16,30),(10,26),(7,17)],TEAL);poly(d,[(13,13),(19,13),(22,17),(20,24),(16,27),(12,24),(10,17)],CYAN,outline=O2,w=1);d.line([(13,15),(18,14)],fill=CYANH,width=2);d.point((16,19),fill=(225,255,242,255));save(im,'last_breath_pendant')
im,d=canvas();poly(d,[(12,3),(20,3),(26,9),(25,26),(20,23),(16,29),(12,23),(7,26),(6,9)],IND);poly(d,[(13,6),(19,6),(23,10),(22,21),(18,19),(16,25),(13,19),(9,21),(9,10)],PUR);d.line([(10,9),(16,12),(22,9)],fill=GOLD,width=2);poly(d,[(15,8),(19,11),(18,17),(15,21),(12,16),(13,12)],EMB,outline=O2,w=1);d.line([(15,10),(17,12),(16,16)],fill=EMBH,width=2);d.line([(8,24),(12,22)],fill=CYAN,width=1);save(im,'fading_heat_cloak')
im,d=canvas();poly(d,[(16,2),(24,6),(28,14),(24,23),(16,29),(8,23),(4,14),(8,6)],IND);poly(d,[(16,5),(22,8),(25,14),(22,20),(16,25),(10,20),(7,14),(10,8)],PUR2,outline=PUR,w=1);poly(d,[(16,8),(19,12),(23,13),(20,17),(20,22),(16,20),(12,22),(12,17),(9,13),(13,12)],CYAN,outline=O2,w=1);d.line([(14,10),(16,8),(18,10)],fill=CYANH,width=2);d.point((16,14),fill=(235,255,225,255));save(im,'guardian_brooch')
# Archivist robe item layers: retain dyeable cloth silhouette, add high-contrast fixed embroidery.
for part in ['helmet','chestplate','leggings','boots']:
 src=Image.open(ITEM/f'mage_{part}.png').convert('RGBA'); base=Image.new('RGBA',src.size,T)
 for y in range(32):
  for x in range(32):
   p=src.getpixel((x,y))
   if not p[3]:continue
   if sum(p[:3])<100: q=O
   elif sum(p[:3])<620:q=(118,145,201,255)
   else:q=(176,202,238,255)
   base.putpixel((x,y),q)
 ov=Image.new('RGBA',(32,32),T);od=ImageDraw.Draw(ov)
 if part=='helmet':
  od.line([(8,11),(11,6),(16,4),(21,6),(24,11)],fill=GOLD,width=2);od.line([(11,8),(16,6),(21,8)],fill=GOLDH,width=1);od.rectangle((15,5,17,8),fill=CYAN);od.line([(7,17),(9,22)],fill=TEAL,width=2);od.line([(25,17),(23,22)],fill=TEAL,width=2)
 elif part=='chestplate':
  od.line([(9,8),(13,12),(16,10),(19,12),(23,8)],fill=GOLD,width=2);od.line([(16,10),(16,25)],fill=GOLDH,width=1);od.polygon([(13,13),(16,11),(19,13),(16,17)],fill=CYAN,outline=O2);od.line([(8,24),(13,26)],fill=TEAL,width=2);od.line([(24,24),(19,26)],fill=TEAL,width=2)
 elif part=='leggings':
  od.line([(8,7),(16,10),(24,7)],fill=GOLD,width=2);od.line([(16,10),(16,17)],fill=GOLDH,width=1);od.line([(9,14),(11,26)],fill=TEAL,width=2);od.line([(23,14),(21,26)],fill=TEAL,width=2);od.point((16,13),fill=CYANH)
 else:
  od.line([(7,18),(13,18),(15,24)],fill=GOLD,width=2);od.line([(19,24),(21,18),(27,18)],fill=GOLD,width=2);od.line([(8,23),(14,25)],fill=CYAN,width=1);od.line([(24,25),(27,23)],fill=CYAN,width=1)
 base.save(ITEM/f'archivist_{part}.png');ov.save(ITEM/f'archivist_{part}_overlay.png')
# Unique block textures (16px) with midnight, gold and resonance-glass language.
def blocktex(name,kind):
 im,d=canvas(16)
 if kind=='receiver_side':
  d.rectangle((0,0,15,15),fill=(25,28,57,255));d.rectangle((1,1,14,14),outline=GD,width=2);d.rectangle((3,4,12,11),fill=(38,48,78,255));d.line([(4,8),(6,6),(8,10),(10,5),(12,7)],fill=CYAN,width=2);d.point((4,8),fill=CYANH);d.rectangle((5,13,10,14),fill=GOLD)
 elif kind=='receiver_top':
  d.rectangle((0,0,15,15),fill=(31,35,69,255));d.rectangle((1,1,14,14),outline=PUR,width=2);d.ellipse((4,4,11,11),fill=O2,outline=GOLD,width=1);d.ellipse((6,6,9,9),fill=CYAN);d.point((7,6),fill=CYANH)
 elif kind=='isolator':
  d.rectangle((0,0,15,15),fill=(27,30,64,220));d.rectangle((1,1,14,14),outline=PUR,width=2);d.rectangle((3,3,12,12),fill=(48,72,97,165),outline=TEAL,width=1);d.line([(3,12),(12,3)],fill=(96,190,190,190),width=1);d.line([(3,5),(10,12)],fill=(79,125,151,170),width=1);d.rectangle((0,7,15,8),fill=(19,22,49,255));d.rectangle((7,0,8,15),fill=(19,22,49,255))
 elif kind=='resonator_side':
  d.rectangle((0,0,15,15),fill=(24,26,52,255));d.rectangle((1,1,14,14),outline=GD,width=2);d.rectangle((3,3,12,12),fill=(50,37,71,255));d.line([(4,10),(4,6),(6,6),(6,9),(8,9),(8,4),(10,4),(10,8),(12,8)],fill=GOLDH,width=2);d.point((4,5),fill=CYAN);d.point((10,3),fill=CYAN)
 elif kind=='resonator_top':
  d.rectangle((0,0,15,15),fill=(28,31,61,255));d.rectangle((1,1,14,14),outline=GOLD,width=2);d.ellipse((4,4,11,11),fill=PUR,outline=O,width=1);d.ellipse((6,6,9,9),fill=CYAN);d.point((7,6),fill=CYANH);d.line([(2,8),(4,8)],fill=GOLDH,width=1);d.line([(12,8),(14,8)],fill=GOLDH,width=1)
 im.save(BLOCK/(name+'.png'))
for n,k in [('echo_receiver_side','receiver_side'),('echo_receiver_top','receiver_top'),('echo_isolator','isolator'),('musical_resonator_side','resonator_side'),('musical_resonator_top','resonator_top')]:blocktex(n,k)
print('restyled item and block textures')
