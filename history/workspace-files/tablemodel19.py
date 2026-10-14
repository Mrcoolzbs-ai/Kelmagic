exec(open('/data/preview19.py').read().split('# Review native UI')[0])
import numpy as np
src=open('/data/preview19.py').read();exec(src.split('# Simplified isometric block geometry')[1].split("for biome in ['plains','desert','taiga','snowy','savanna']:")[0].split('\n',1)[1])
model=json.loads((a/'models/block/archivist_table.json').read_text());im=Image.new('RGBA',(400,360),'#e7e0d2')
def proj(x,y,z):return(200+(x-z)*8,280+(x+z-16)*4-y*8)
for e in sorted(model['elements'],key=lambda e:sum(e['from'][::2])+.01*e['from'][1]):
 x,y,z=e['from'];xx,yy,zz=e['to'];face=e['faces']['up']['texture'][1:];tex=texture(model['textures'][face]);drawface(im,tex,[proj(x,yy,z),proj(xx,yy,z),proj(xx,yy,zz),proj(x,yy,zz)],1.1);drawface(im,tex,[proj(xx,yy,z),proj(xx,y,z),proj(xx,y,zz),proj(xx,yy,zz)],.7);drawface(im,tex,[proj(x,yy,zz),proj(xx,yy,zz),proj(xx,y,zz),proj(x,y,zz)],.9)
im.save(out/'archivist-table-model.png')
# New artifact page, exact recipe and bitmap layout.
z=zipfile.ZipFile(r/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
page=Image.open(a/'textures/gui/codex.png').convert('RGBA');g=ImageDraw.Draw(page)
for i,t in enumerate(['Слова','Артефакты']):g.rectangle((22+i*160,16,175+i*160,33),fill='#6a4240');text(page,t,22+i*160+(154-sum(map(advance,t)))//2,21,col='#e1cba7')
text(page,'Архивариусный кодекс',100,3,col='#dcc28f');page.alpha_composite(im.resize((16,16),Image.Resampling.NEAREST),(26,49));text(page,'Стол архивариуса',49,48,112);text(page,'Только здесь слова соединяются с кодексом, а книга получает свою копию.',25,81,132);text(page,'Схема изготовления',196,49,135)
rec=json.loads((r/'src/main/resources/data/kelmagic/recipe/archivist_table.json').read_text())
for row,line in enumerate(rec['pattern']):
 for col,c in enumerate(line):
  name=rec['key'][c]['item'];key=name.split(':')[1];p=a/f'textures/item/{key}.png' if name.startswith('kelmagic:') else None
  if p:icon=Image.open(p).convert('RGBA')
  else:
   choices=[f'assets/minecraft/textures/item/{key}.png',f'assets/minecraft/textures/block/{key}.png',f'assets/minecraft/textures/block/{key}_front.png'];path=next(x for x in choices if x in z.namelist());icon=Image.open(io.BytesIO(z.read(path))).convert('RGBA')
  x,y=198+col*22,73+row*22;g.rectangle((x,y,x+19,y+19),fill='#634732');page.alpha_composite(icon.resize((16,16),Image.Resampling.NEAREST),(x+2,y+2))
text(page,'→',269,99);page.alpha_composite(im.resize((16,16),Image.Resampling.NEAREST),(291,99));text(page,'Выход: 1. Верстак 3×3.',196,147,135);text(page,'20 / 20',166,192,col='#dcc28f');page.resize((720,432),Image.Resampling.NEAREST).save(out/'codex-archivist-table.png');print('Table model and new artifact page rendered')
