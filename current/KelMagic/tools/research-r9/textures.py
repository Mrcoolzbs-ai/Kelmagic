from PIL import Image,ImageDraw,ImageOps
from pathlib import Path
import random,colorsys
p=Path('src/main/resources/assets/kelmagic/textures/block');rng=random.Random(931)
def out(n,im):im.save(p/(n+'.png'))
# Dark straight timber grain: staggered seams, no oval knots.
a=Image.new('RGBA',(16,16),(32,45,72,255));d=ImageDraw.Draw(a)
for y in range(16):
 for x in range(16):
  k=rng.choice([-2,-1,0,0,1,2]);base=(28,40,64) if y%4==3 else (34,48,75);a.putpixel((x,y),tuple(c+k for c in base)+(255,))
for row,seam in enumerate([3,11,6,14]):
 yy=row*4;d.line((seam,yy,seam,yy+2),fill=(18,29,49));d.line(((seam+1)%16,yy,(seam+1)%16,yy+2),fill=(45,59,83));d.line(((seam+4)%12,yy+1,(seam+4)%12+3,yy+1),fill=(43,56,82))
out('midnight_planks',a)
# D4 symmetry gives exact same ornament under all horizontal/vertical UV rotations.
a=Image.new('RGBA',(16,16),(24,34,58,255));d=ImageDraw.Draw(a)
d.rectangle((0,0,15,15),outline=(57,75,96),width=1);d.rectangle((1,1,14,14),outline=(15,23,41),width=1);d.rectangle((2,2,13,13),outline=(42,56,80),width=1)
for yy in [4,7,8,11]:d.line((3,yy,12,yy),fill=(34,48,73))
# Symmetric diamond lattice and gold rivets; coherent along all four outer edges.
d.line([(7,3),(8,3),(12,7),(12,8),(8,12),(7,12),(3,8),(3,7),(7,3)],fill=(99,144,158),width=1)
for x,y in [(3,3),(12,3),(3,12),(12,12)]:d.point((x,y),fill=(224,204,137))
# enforce D4 on the decorative surface by taking union in identical palette priority.
# use explicit per-pixel radial symmetry for the grain as well
for y in range(16):
 for x in range(16):
  sx,sy=sorted((min(x,15-x),min(y,15-y)));a.putpixel((x,y),a.getpixel((sx,sy)))
out('midnight_trapdoor',a)
# Door windows centered around x=7.5; structure and glazing bilateral, handle is intentionally on latch side.
for top in [False,True]:
 a=Image.open(p/'midnight_planks.png').copy();d=ImageDraw.Draw(a);d.rectangle((0,0,15,15),outline=(19,28,48));d.line((1,0,1,15),fill=(64,80,102));d.line((14,0,14,15),fill=(64,80,102))
 if top:
  d.rectangle((3,2,12,12),fill=(12,24,40),outline=(100,139,153));d.rectangle((4,3,11,11),fill=(35,71,89));d.line((7,3,7,11),fill=(116,155,161));d.line((8,3,8,11),fill=(116,155,161));d.line((4,7,11,7),fill=(116,155,161));d.line((4,8,11,8),fill=(116,155,161));
  for x,y in [(4,3),(11,3),(4,11),(11,11)]:d.point((x,y),fill=(220,206,154))
 else:
  d.rectangle((3,3,12,13),outline=(67,88,110));d.rectangle((4,4,11,12),outline=(26,37,59));d.rectangle((11,0,12,1),fill=(215,187,110))
 out('midnight_door_top' if top else 'midnight_door_bottom',a)
# Both cabinet facades: symmetric frames and six-by-three clear recesses. All 18 inventory items remain renderer-owned.
for kind in ['manuscript_cabinet','archivist_cabinet']:
 for n in range(16):
  a=Image.open(p/'midnight_planks.png').copy();d=ImageDraw.Draw(a);d.rectangle((0,0,15,15),fill=(34,43,58),outline=(153,135,91));
  for yy in [1,6,11]:
   d.rectangle((1,yy,14,yy+3),fill=(18,26,40));
   for xx in [2,4,6,8,10,12]:d.line((xx,yy,xx,yy+3),fill=(36,43,52))
  for yy in [5,10]:d.line((1,yy,14,yy),fill=(139,123,86))
  # side mirror applied to timber, not inventory; six columns centered evenly
  for yy in range(16):
   for xx in range(8):a.putpixel((15-xx,yy),a.getpixel((xx,yy)))
  target=p/f'{kind}_{n}.png'
  if target.exists():a.save(target)
# Leaves retain blue foliage, with just a handful of pale gold/ivory leaves; same alpha holes.
a=Image.open(p/'midnight_leaves.png').convert('RGBA');d=ImageDraw.Draw(a)
for x,y in [(3,3),(11,6),(6,12),(13,13)]:
 for dx,dy,c in [(0,0,(247,237,188,255)),(1,0,(223,196,115,255)),(0,1,(197,167,89,255))]:
  if a.getpixel((x+dx,y+dy))[3]:d.point((x+dx,y+dy),fill=c)
out('golden_midnight_leaves',a)
# Cyan facets become lilac but alpha, frame shape and internal seam masks are unchanged.
for n in [f'resonance_glass_{i}' for i in range(16)]+['archive_magic']:
 a=Image.open(p/(n+'.png')).convert('RGBA')
 for y in range(a.height):
  for x in range(a.width):
   rr,g,b,alpha=a.getpixel((x,y));h,s,v=colorsys.rgb_to_hsv(rr/255,g/255,b/255)
   if alpha and b>=rr and g>rr:
    nr,ng,nb=colorsys.hsv_to_rgb(.765,s*.85,v);a.putpixel((x,y),(round(nr*255),round(ng*255),round(nb*255),alpha))
 out('archive_magic_violet' if n=='archive_magic' else 'violet_'+n,a)
# Recognisable monochrome mob silhouettes, transparent cuts make the ink colour irrelevant.
p=Path('src/main/resources/assets/kelmagic/textures/ink');a=Image.open(p/'patterns.png').convert('RGBA')
heads={
'creeper': ['................','................','..############..','..############..','..############..','..##...##...##..','..##...##...##..','..#####..#####..','..####....####..','..###......###..','..###......###..','..###..##..###..','..############..','..############..','................','................'],
'zombie':['................','..############..','..############..','..##.###.#####..','..############..','..#...####...#..','..#...####...#..','..############..','..#####..#####..','..############..','..###......###..','..####....####..','..############..','..##.######.##..','..############..','................'],
'skeleton':['................','....########....','...##########...','..############..','..############..','..##...##...##..','..##...##...##..','..#####..#####..','..#####..#####..','...##########...','...#.#.##.#.#...','...#.#.##.#.#...','....########....','................','................','................'],
'enderman':['................','..############..','..############..','..############..','..############..','..############..','..############..','..#....##....#..','..##..####..##..','..############..','..############..','..############..','..############..','..############..','................','................'],
'pig':['................','..###......###..','..############..','..############..','..############..','..##..####..##..','..##..####..##..','..############..','..##........##..','..##.######.##..','..##.#.##.#.##..','..##.#.##.#.##..','..##.######.##..','..##........##..','..############..','................'],
'sheep':['................','...##########...','..############..','.##############.','.###........###.','.###.######.###.','.###.#.##.#.###.','.###.######.###.','.###.######.###.','.###.##..##.###.','.###.######.###.','.####......####.','..############..','...##########...','................','................'],
'cat':['................','..##........##..','..###......###..','..############..','..############..','..#..######..#..','..#..######..#..','..#####..#####..','..#####..#####..','########.#######','..###.#..#.###..','#######..#######','...##########...','....########....','................','................'],
'wolf':['................','..###......###..','..#.#......#.#..','..############..','..############..','..#..######..#..','..#..######..#..','..####....####..','..####....####..','..####.##.####..','..####.##.####..','..####....####..','...##########...','...###....###...','....########....','................'],
'bee':['....##....##....','.....#....#.....','..############..','..############..','..##.######.##..','..##.######.##..','..############..','..#####..#####..','..############..','..############..','....##....##....','...####..####...','....##....##....','................','................','................'],
'slime':['................','................','..############..','..#..........#..','..#.########.#..','..#.#..##..#.#..','..#.#..##..#.#..','..#.########.#..','..#.########.#..','..#.###..###.#..','..#.###..###.#..','..#.########.#..','..#..........#..','..############..','................','................']}
# mob tiles start after 66 Cyrillic + 52 Latin + 10 digits + 10 arrows
for k,(name,rows) in enumerate(heads.items()):
 tile=138+k;x0=tile%16*16;y0=tile//16*16
 for y,row in enumerate(rows):
  assert len(row)==16,(name,row)
  for x,c in enumerate(row):a.putpixel((x0+x,y0+y),(255,255,255,255 if c=='#' else 0))
a.save(p/'patterns.png')
# Individual enlarged surfaces and ink panel for visual inspection.
q=Path('/data/KelMagic-r9/previews');q.mkdir(exist_ok=True)
for n in ['midnight_planks','midnight_trapdoor','midnight_door_top','midnight_door_bottom','golden_midnight_leaves','manuscript_cabinet_0','resonance_glass_0','violet_resonance_glass_0']:
 im=Image.open(Path('src/main/resources/assets/kelmagic/textures/block')/(n+'.png'));im.resize((256,256),Image.Resampling.NEAREST).save(q/(n+'.png'))
canvas=Image.new('RGB',(640,192),(37,33,40));d=ImageDraw.Draw(canvas)
for i,n in enumerate(heads):
 tile=138+i;im=a.crop((tile%16*16,tile//16*16,tile%16*16+16,tile//16*16+16)).resize((96,96),Image.Resampling.NEAREST);x=i%5*128+16;y=i//5*96;canvas.paste(im,(x,y),im)
canvas.save(q/'mob-ink.png')
print('R9 textures generated')
