from pathlib import Path
from PIL import Image,ImageDraw,ImageFont,ImageOps
import zipfile,json,io,re,math
r=Path('/data/KelMagic');a=r/'src/main/resources/assets/kelmagic';z=zipfile.ZipFile(r/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar');defs=json.loads(z.read('assets/minecraft/font/include/default.json'));glyph={}
for p in defs['providers']:
 if p['type']!='bitmap':continue
 tex=Image.open(io.BytesIO(z.read('assets/minecraft/textures/'+p['file'].split(':')[1]))).convert('RGBA');rows=p['chars'];cw=tex.width//len(rows[0]);ch=tex.height//len(rows);scale=p.get('height',8)/ch
 for iy,row in enumerate(rows):
  for ix,c in enumerate(row):
   crop=tex.crop((ix*cw,iy*ch,(ix+1)*cw,(iy+1)*ch));bb=crop.getbbox()
   if bb and c not in glyph:
    width=max(1,math.ceil(bb[2]*scale));glyph[c]=(crop.resize((max(1,round(cw*scale)),8),Image.Resampling.NEAREST),width+1)
glyph[' ']=(None,4)
def advance(c):return glyph.get(c,(None,6))[1]
def wrap(text,w):
 lines=[];line=''
 for word in text.split():
  cand=(line+' '+word).strip()
  if sum(map(advance,cand))>w and line:lines.append(line);line=word
  else:line=cand
 if line:lines.append(line)
 return lines
def text(im,t,x,y,w=None,col='#493427'):
 lines=wrap(t,w) if w else [t]
 for n,line in enumerate(lines):
  xx=x
  for c in line:
   g,adv=glyph.get(c,(None,6))
   if g:
    ink=Image.new('RGBA',g.size,col);ink.putalpha(g.getchannel('A'));im.alpha_composite(ink,(xx,y+n*9))
   xx+=adv
 return y+len(lines)*9
src=(r/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();lore_part=src.split('private static String lore(String n)')[1].split('private static String purpose')[0];lore=dict(re.findall(r'case "([^"]+)"->"([^"]+)"',lore_part));lang=json.loads((a/'lang/ru_ru.json').read_text());names=['mana_core','magic_ingot','magic_cloth','scripter','dragon_heart']+[p+'_'+s for p in ['arcane','mage'] for s in ['helmet','chestplate','leggings','boots']]+['archivists_codex'];checks=0
out=r/'art/verification-1.5.1';out.mkdir(parents=True,exist_ok=True)
for n in names:
 im=Image.open(a/'textures/gui/codex.png').convert('RGBA')
 # Exact Minecraft bitmap text metrics; item block models are approximated by a source face in this offline layout study.
 for j,label in enumerate(['Слова','Артефакты']):
  dr=ImageDraw.Draw(im);x=22+j*160;dr.rectangle((x,16,x+153,33),fill='#6a4240');text(im,label,x+(154-sum(map(advance,label)))//2,21,col='#e1cba7')
 text(im,'Архивариусный кодекс',100,3,col='#dcc28f');text(im,lang['item.kelmagic.'+n],49,48,112)
 body=lore.get(n,'Мягкий балахон хранит тепло. Золотые швы направляют ману вдоль длинных пол.' if n.startswith('mage') else 'Поверх магического балахона закреплены тонкие металлические пластины. Складки остаются свободными.')
 assert text(im,body,25,81,132)<188;checks+=1
 text(im,'Схема изготовления',196,49,135)
 def icon(item):
  namespace,name=item.split(':');path=a/'textures/item'/f'{name}.png'
  if namespace=='kelmagic':
   result=Image.open(path).convert('RGBA');ov=path.with_name(path.stem+'_overlay.png')
   if ov.exists():result.alpha_composite(Image.open(ov))
  else:
   options=[f'assets/minecraft/textures/item/{name}.png',f'assets/minecraft/textures/block/{name}.png',f'assets/minecraft/textures/block/{name}_side.png']
   path=next((p for p in options if p in z.namelist()),None)
   if not path:return Image.new('RGBA',(16,16))
   result=Image.open(io.BytesIO(z.read(path))).convert('RGBA')
  return result.resize((16,16),Image.Resampling.NEAREST)
 rec=json.loads((r/f'src/main/resources/data/kelmagic/recipe/{n}.json').read_text());pat=rec.get('pattern',['CBF','K  ','   ']);key=rec.get('key',{'C':{'item':'kelmagic:mana_core'},'B':{'item':'minecraft:book'},'F':{'item':'minecraft:feather'},'K':{'item':'minecraft:ink_sac'}})
 im.alpha_composite(icon('kelmagic:'+n),(26,49))
 for row,line in enumerate(pat):
  for col,c in enumerate(line):
   xx,yy=198+col*22,73+row*22;ImageDraw.Draw(im).rectangle((xx,yy,xx+19,yy+19),fill=(99,71,50,80))
   if c!=' ':im.alpha_composite(icon(key[c]['item']),(xx+2,yy+2))
 text(im,'→',269,99);im.alpha_composite(icon('kelmagic:'+n),(291,99))
 cap='Без формы. Обычные или светящиеся чернила. Выход: 1.' if n=='archivists_codex' else f'Выход: {rec["result"]["count"]}. Верстак 3×3.'
 assert text(im,cap,196,147,135)<188;checks+=1
 text(im,f'{names.index(n)+1} / 14',165,192,col='#73533a')
 im.resize((720,432),Image.Resampling.NEAREST).save(out/(n+'.png'))
print('Codex layout bounds passed:',checks,'checks across 14 pages. Offline previews, not native screenshots.')
