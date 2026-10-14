from pathlib import Path
from PIL import Image,ImageDraw,ImageEnhance
import zipfile,io,json,sys,math
import numpy as np
r=Path('/data/KelMagic');a=r/'src/main/resources/assets/kelmagic';out=Path('/data/qa-1.9');out.mkdir(exist_ok=True);sys.path.insert(0,str(r/'tools'));from nbt19 import read
z=zipfile.ZipFile(r/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
# Bitmap-font layout is the same as the in-game UI, not an OS substituted font.
exec(Path('/data/preview_codex.py').read_text().split("src=(r/'src/main/java")[0])
# Review native UI with actual slots and bitmap labels.
im=Image.open(a/'textures/gui/archive_table.png').crop((0,0,210,194));text(im,'Стол архивариуса',16,10,col='#eaf4d4');text(im,'Книга + лист / материалы копии',16,30,col='#a9c6c7');text(im,'Инвентарь',24,101,col='#9ca995');im.resize((840,776),Image.Resampling.NEAREST).save(out/'table-empty.png')
def item(name):
 if name.startswith('kelmagic:'):p=a/f'textures/item/{name.split(":")[1]}.png';return Image.open(p).convert('RGBA').resize((16,16),Image.Resampling.NEAREST)
 return Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/'+name.split(':')[1]+'.png'))).convert('RGBA').resize((16,16),Image.Resampling.NEAREST)
for n,icons in [('table-merge',[(22,'kelmagic:archivists_codex'),(53,'kelmagic:manuscript_vak'),(170,'kelmagic:archivists_codex')]),('table-copy',[(22,'kelmagic:archivists_codex'),(53,'minecraft:book'),(84,'minecraft:feather'),(115,'minecraft:ink_sac'),(170,'kelmagic:archivists_codex')])]:
 copy=im.copy()
 for x,name in icons:copy.alpha_composite(item(name),(x,58))
 copy.resize((840,776),Image.Resampling.NEAREST).save(out/(n+'.png'))
for biome in ['plains','desert','taiga','snow','savanna','jungle','swamp']:
 base=Image.open(io.BytesIO(z.read('assets/minecraft/textures/entity/villager/villager.png'))).convert('RGBA');p='assets/minecraft/textures/entity/villager/type/'+biome+'.png'
 if p in z.namelist():base.alpha_composite(Image.open(io.BytesIO(z.read(p))).convert('RGBA'))
 base.alpha_composite(Image.open(a/f'textures/entity/villager/profession/archivist_{biome}.png').convert('RGBA'))
 # Front-facing UV proof showing face, nose, outer coat and crossed arms separately in their actual body locations.
 canvas=Image.new('RGBA',(320,460),'#e5dfd0')
 def place(box,x,y,w,h):tex=base.crop(box).resize((w,h),Image.Resampling.NEAREST);canvas.alpha_composite(tex,(x,y))
 place((8,8,16,18),112,30,96,120);hat=base.crop((40,8,48,18)).resize((96,120),Image.Resampling.NEAREST);canvas.alpha_composite(hat,(112,30));place((24,2,26,6),148,101,24,48)
 place((6,44,14,64),112,150,96,240);place((48,26,52,34),100,197,120,60)
 place((20,20,24,32),118,363,36,72);place((20,20,24,32),166,363,36,72)
 canvas.save(out/f'archivist-{biome}.png')
# Simplified isometric block geometry of the EXACT generated template. These are offline schematic previews.
cache={}
def texture(name):
 if name in cache:return cache[name]
 n=name.split(':')[-1].removeprefix('block/');ns=name.split(':')[0]
 if ns=='kelmagic':p=a/f'textures/block/{n}.png';p=p if p.exists() else a/'textures/block/archive_top.png';im=Image.open(p).convert('RGBA')
 else:
  choices=[n,n+'_side',n.replace('_stairs','_planks'),n.replace('_door','_planks'),n.replace('_log','_log_top')]
  if n=='bookshelf':choices=['bookshelf']
  if n=='smooth_sandstone':choices=['sandstone_top']
  if n in ['chest','lantern','jigsaw']:choices=['oak_planks' if n!='lantern' else 'gold_block']
  path=next((f'assets/minecraft/textures/block/{c}.png' for c in choices if f'assets/minecraft/textures/block/{c}.png' in z.namelist()),'assets/minecraft/textures/block/stone.png');im=Image.open(io.BytesIO(z.read(path))).convert('RGBA').crop((0,0,16,16))
 cache[name]=im.resize((16,16),Image.Resampling.NEAREST);return cache[name]
def drawface(im,tex,pts,shade=1):
 bx=int(min(p[0] for p in pts));by=int(min(p[1] for p in pts));bw=int(max(p[0] for p in pts)-bx)+1;bh=int(max(p[1] for p in pts)-by)+1;pts=[(x-bx,y-by) for x,y in pts];p0,p1,p2,p3=pts;M=np.array([[(p1[0]-p0[0])/16,(p3[0]-p0[0])/16,p0[0]],[(p1[1]-p0[1])/16,(p3[1]-p0[1])/16,p0[1]],[0,0,1.]])
 if abs(np.linalg.det(M))<1e-5:return
 inv=np.linalg.inv(M);layer=ImageEnhance.Brightness(tex).enhance(shade).transform((bw,bh),Image.Transform.AFFINE,tuple(inv[:2].ravel()),Image.Resampling.NEAREST);mask=Image.new('L',(bw,bh));ImageDraw.Draw(mask).polygon(pts,fill=255);layer.putalpha(mask);im.alpha_composite(layer,(bx,by))
def schematic(nbt,path,cut):
 palette=nbt['palette'][1][1];bl=nbt['blocks'][1][1];im=Image.new('RGBA',(760,580),'#e7e0d2');w,h,dep=nbt['size'][1][1];unit=22
 def project(x,y,zz):return(380+(x-zz)*unit,455+(x+zz-w-dep)*unit*.5-y*unit)
 for b in sorted(bl,key=lambda v:sum(v['pos'][1][1][::2])*.001+v['pos'][1][1][1]*.00001):
  x,y,zz=b['pos'][1][1];state=palette[b['state'][1]];name=state['Name'][1]
  if name=='minecraft:air':continue
  if cut and (y>=5 or (zz==dep-1 or x==w-1) and y>0):continue
  tex=texture(name)
  drawface(im,tex,[project(x,y+1,zz),project(x+1,y+1,zz),project(x+1,y+1,zz+1),project(x,y+1,zz+1)],1.1)
  drawface(im,tex,[project(x+1,y+1,zz),project(x+1,y,zz),project(x+1,y,zz+1),project(x+1,y+1,zz+1)],.7)
  drawface(im,tex,[project(x,y+1,zz+1),project(x+1,y+1,zz+1),project(x+1,y,zz+1),project(x,y,zz+1)],.9)
 im.save(path)
for biome in ['plains','desert','taiga','snowy','savanna']:
 for v in [1,2]:
  nbt=read((r/f'src/main/resources/data/kelmagic/structure/archive/{biome}_{v}.nbt').read_bytes());schematic(nbt,out/f'archive-{biome}-{v}.png',False);schematic(nbt,out/f'archive-{biome}-{v}-inside.png',True)
for n in ['magic_torch','magic_lantern','temporary_end_gate']:
 texture('kelmagic:'+n).resize((256,256),Image.Resampling.NEAREST).save(out/(n+'.png'))
print('33 offline UI/outfit/structure/light previews generated')
