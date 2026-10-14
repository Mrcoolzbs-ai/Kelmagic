from pathlib import Path
from PIL import Image,ImageDraw,ImageChops
import json,numpy as np
r=Path('/data/KelMagic');a=r/'src/main/resources/assets/kelmagic';out=Path('/data/qa-1.8');out.mkdir(exist_ok=True)
def cassette(color=0x443347,pattern=0):
 im=Image.open(a/'textures/item/tape_casing.png').convert('RGBA');pix=im.load();red=(color>>16)&255;green=(color>>8)&255;blue=color&255
 for y in range(32):
  for x in range(32):
   rr,gg,bb,alpha=pix[x,y];pix[x,y]=(round(rr*red/255),round(gg*green/255),round(bb*blue/255),alpha)
 im.alpha_composite(Image.open(a/'textures/item/tape_trim_recorded.png').convert('RGBA'))
 if pattern:im.alpha_composite(Image.open(a/f'textures/item/tape_pattern_{pattern}.png').convert('RGBA'))
 return im
for i in range(1,10):cassette(0x3c44aa,i).resize((320,320),Image.Resampling.NEAREST).save(out/f'pattern-{i}.png')
for name,col in [('red',0xb02e26),('cyan',0x169c9c),('white',0xf9fffe),('black',0x1d1d21)]:cassette(col).resize((320,320),Image.Resampling.NEAREST).save(out/f'tape-{name}.png')
Image.open(a/'textures/item/golden_thread.png').resize((320,320),Image.Resampling.NEAREST).save(out/'golden-thread.png')
im=Image.new('RGBA',(400,360),'#eee7dc')
tex=Image.open(a/'textures/block/golden_wool.png').convert('RGBA')
for points in [[(200,64),(328,128),(200,192),(72,128)],[(200,192),(328,128),(328,256),(200,320)],[(72,128),(200,192),(200,320),(72,256)]]:
 p0,p1,p2,p3=points;M=np.array([[(p1[0]-p0[0])/16,(p3[0]-p0[0])/16,p0[0]],[(p1[1]-p0[1])/16,(p3[1]-p0[1])/16,p0[1]],[0,0,1.]])
 inv=np.linalg.inv(M);layer=tex.transform(im.size,Image.Transform.AFFINE,tuple(inv[:2].ravel()),Image.Resampling.NEAREST);mask=Image.new('L',im.size);ImageDraw.Draw(mask).polygon(points,fill=255);layer.putalpha(mask);im.alpha_composite(layer)
im.save(out/'golden-wool.png')
# Exact bitmap metrics for the updated codex, with block icons represented by texture faces offline.
exec(Path('/data/preview_codex.py').read_text().split("src=(r/'src/main/java")[0])
import re,io
src=(r/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();lore=dict(re.findall(r'case "([^"]+)"->"([^"]+)"',src.split('private static String lore(String n)')[1].split('private static String purpose')[0]));lang=json.loads((a/'lang/ru_ru.json').read_text())
def icon(item):
 ns,n=item.split(':')
 if ns=='kelmagic':
  if n=='cassette':return cassette().resize((16,16),Image.Resampling.NEAREST)
  p=a/f'textures/item/{n}.png';p=p if p.exists() else a/f'textures/block/{n}.png' if n=='golden_wool' else a/'textures/block/cassette_deck_front.png';im=Image.open(p).convert('RGBA')
 else:
  p=next(p for p in [f'assets/minecraft/textures/item/{n}.png',f'assets/minecraft/textures/block/{n}.png'] if p in z.namelist());im=Image.open(io.BytesIO(z.read(p))).convert('RGBA')
 return im.resize((16,16),Image.Resampling.NEAREST)
for n,index in [('golden_thread',18),('golden_wool',19),('magic_cloth',3),('cassette_deck',16)]:
 im=Image.open(a/'textures/gui/codex.png').convert('RGBA');g=ImageDraw.Draw(im)
 for i,t in enumerate(['Слова','Артефакты']):g.rectangle((22+i*160,16,175+i*160,33),fill='#6a4240');text(im,t,22+i*160+(154-sum(map(advance,t)))//2,21,col='#e1cba7')
 text(im,'Архивариусный кодекс',100,3,col='#dcc28f');im.alpha_composite(icon('kelmagic:'+n),(26,49));text(im,lang.get('item.kelmagic.'+n,lang.get('block.kelmagic.'+n)),49,48,112);assert text(im,lore[n],25,81,132)<188
 text(im,'Схема изготовления',196,49,135);rec=json.loads((r/f'src/main/resources/data/kelmagic/recipe/{n}.json').read_text())
 for row,line in enumerate(rec['pattern']):
  for col,c in enumerate(line):xx,yy=198+col*22,73+row*22;g.rectangle((xx,yy,xx+19,yy+19),fill=(99,71,50,80));im.alpha_composite(icon(rec['key'][c]['item']),(xx+2,yy+2))
 text(im,'→',269,99);im.alpha_composite(icon('kelmagic:'+n),(291,99));cap=f'Выход: {rec["result"]["count"]}. Верстак 3×3.';assert text(im,cap,196,147,135)<188
 text(im,str(index)+' / 19',166,192,col='#dcc28f');im.resize((720,432),Image.Resampling.NEAREST).save(out/f'codex-{n}.png')
# Final tape player layout using actual new tinted layers; this is not a game screenshot.
im=Image.open(a/'textures/gui/tape_player.png').crop((0,0,176,208));g=ImageDraw.Draw(im);text(im,'Кассетный плеер',12,10,col='#ffe8bf');text(im,'Кассета 2 / 8',12,30,col='#edcda0');g.rectangle((60,52,79,71),outline='#f2ca76');colors=[0xb02e26,0x3c44aa,0x169c9c,0xfed83d,0x80c71f,0xf9fffe,0x8932b8,0x1d1d21]
for i,col in enumerate(colors):text(im,str(i+1),29+(i%4)*38,43+(i//4)*29,col='#aa947d');im.alpha_composite(cassette(col,i+1).resize((16,16),Image.Resampling.NEAREST),(24+(i%4)*38,54+(i//4)*29))
text(im,'Инвентарь',8,114,col='#bfa88a');im.resize((704,832),Image.Resampling.NEAREST).save(out/'player-customized.png')
print('18 representative appearance previews; 8 artifact layout bounds passed')
