from pathlib import Path
from PIL import Image,ImageDraw
import json,re,io
exec(Path('/data/preview_codex.py').read_text().split("src=(r/'src/main/java")[0])
src=(r/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();lore=dict(re.findall(r'case "([^"]+)"->"([^"]+)"',src.split('private static String lore(String n)')[1].split('private static String purpose')[0]));lang=json.loads((a/'lang/ru_ru.json').read_text());out=Path('/data/qa-audio17')
def icon(item):
 ns,n=item.split(':')
 if ns=='kelmagic':p=a/f'textures/item/{n}.png';p=p if p.exists() else a/'textures/block/cassette_deck_front.png';im=Image.open(p).convert('RGBA')
 else:
  p=next(p for p in [f'assets/minecraft/textures/item/{n}.png',f'assets/minecraft/textures/block/{n}.png',f'assets/minecraft/textures/block/{"stone" if n=="stone_button" else n}.png'] if p in z.namelist());im=Image.open(io.BytesIO(z.read(p))).convert('RGBA')
 return im.resize((16,16),Image.Resampling.NEAREST)
for n in ['cassette','cassette_deck','cassette_player']:
 im=Image.open(a/'textures/gui/codex.png').convert('RGBA');g=ImageDraw.Draw(im)
 for i,t in enumerate(['Слова','Артефакты']):g.rectangle((22+i*160,16,175+i*160,33),fill='#6a4240');text(im,t,22+i*160+(154-sum(map(advance,t)))//2,21,col='#e1cba7')
 text(im,'Архивариусный кодекс',100,3,col='#dcc28f');im.alpha_composite(icon('kelmagic:'+n),(26,49));text(im,lang.get('item.kelmagic.'+n,lang.get('block.kelmagic.'+n)),49,48,112);assert text(im,lore[n],25,81,132)<188
 text(im,'Схема изготовления',196,49,135);rec=json.loads((r/f'src/main/resources/data/kelmagic/recipe/{n}.json').read_text())
 for row,line in enumerate(rec['pattern']):
  for col,c in enumerate(line):xx,yy=198+col*22,73+row*22;g.rectangle((xx,yy,xx+19,yy+19),fill=(99,71,50,80));im.alpha_composite(icon(rec['key'][c]['item']),(xx+2,yy+2))
 text(im,'→',269,99);im.alpha_composite(icon('kelmagic:'+n),(291,99));assert text(im,'Выход: 1. Верстак 3×3.',196,147,135)<188
 label=f'{15+list(["cassette","cassette_deck","cassette_player"]).index(n)} / 17';text(im,label,180-sum(map(advance,label))//2,192,col='#dcc28f');im.resize((720,432),Image.Resampling.NEAREST).save(out/f'codex-{n}.png')
print('New artifact page bounds passed: 6 checks')
