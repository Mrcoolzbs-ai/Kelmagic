from pathlib import Path
from PIL import Image,ImageDraw
exec(Path('/data/preview_codex.py').read_text().split("src=(r/'src/main/java")[0])
out=Path('/data/qa-audio17');out.mkdir(exist_ok=True)
for name,height in [('tape_player',208),('tape_deck',197)]:
 for state in ['empty','recording' if name=='tape_deck' else 'loaded']:
  im=Image.open(a/f'textures/gui/{name}.png').crop((0,0,176,height));g=ImageDraw.Draw(im)
  text(im,'Кассетный плеер' if name=='tape_player' else 'Кассетная дека',12,10,col='#ffe8bf')
  if name=='tape_player':
   text(im,'Кассета 2 / 8',12,30,col='#edcda0');
   for n in range(8):text(im,str(n+1),29+(n%4)*38,43+(n//4)*29,col='#aa947d')
   g.rectangle((60,52,79,71),outline='#f2ca76')
   if state=='loaded':
    for i in [0,1,3,4,6]:im.alpha_composite(Image.open(a/'textures/item/recorded_cassette.png').resize((16,16),Image.Resampling.NEAREST),(24+(i%4)*38,54+(i//4)*29))
   text(im,'Инвентарь',8,114,col='#bfa88a')
  else:
   text(im,'До записи: 15 с' if state=='recording' else 'Кассета + скриптер',12,29,col='#e7cda6');text(im,'Инвентарь',8,103,col='#bfa88a')
   if state=='recording':
    im.alpha_composite(Image.open(a/'textures/item/cassette.png').resize((16,16),Image.Resampling.NEAREST),(29,58));im.alpha_composite(Image.open(a/'textures/item/scripter.png').resize((16,16),Image.Resampling.NEAREST),(65,58));g.rectangle((23,84,152,87),fill='#29262e');g.rectangle((23,84,72,87),fill='#d8ac62')
  im.resize((704,height*4),Image.Resampling.NEAREST).save(out/f'{name}-{state}.png')
for name in ['cassette','cassette_player']:Image.open(a/f'textures/item/{name}.png').resize((320,320),Image.Resampling.NEAREST).save(out/f'{name}.png')
