# Offline layout study: native bitmap font metrics, not a screenshot from a Minecraft client.
from pathlib import Path
from PIL import Image,ImageDraw
import json
exec(Path('/data/preview_codex.py').read_text().split("src=(r/'src/main/java")[0])
words=json.loads((r/'src/main/resources/data/kelmagic/magic/words.json').read_text())['words'];meta={w['word']:w for w in words};font=Image.open(a/'textures/font/kel.png').convert('RGBA');cwd,ch=48,72
out=Path('/data/book-qa-1.6');out.mkdir(exist_ok=True);bounds=0
for index,word in enumerate(['nis','muk','gav','pir','tek','mina','liv','sin']):
 im=Image.open(a/'textures/gui/codex.png').convert('RGBA');dr=ImageDraw.Draw(im)
 for j,label in enumerate(['Слова','Артефакты']):
  x=22+j*160;dr.rectangle((x,16,x+153,33),fill='#6a4240');text(im,label,x+(154-sum(map(advance,label)))//2,21,col='#e1cba7')
 text(im,'Архивариусный кодекс',100,3,col='#dcc28f');text(im,word,25,47)
 cons='ptkbdgmnslvrj';vowels='aeiou';i=0;xx=48
 while i<len(word):
  c=word[i];slot=cons.index(c)*6;i+=1
  if i<len(word) and word[i] in vowels:slot+=vowels.index(word[i])+1;i+=1
  g=font.crop(((slot%8)*48,(slot//8)*72,(slot%8+1)*48,(slot//8+1)*72)).resize((12,18),Image.Resampling.NEAREST);ink=Image.new('RGBA',g.size,'#386e78');ink.putalpha(g.getchannel('A'));im.alpha_composite(ink,(xx,67));xx+=13
 assert text(im,'Значение: '+meta[word]['translation'],25,91,132)<132;bounds+=1
 actual={'mina':'Цель: заклинатель.','tuna':'Цель: другой игрок.','sila':'Цель: моб.','pir':'Магический огонь. mina liv pir: жизнь в пламени.','muk':'Каждый повтор: сила и время ×1,5.','nis':'Каждый повтор: сила и время ÷1,5.','no':'Обращает поддерживаемые эффекты.','lu':'Множество целей в области.'}.get(word,meta[word]['description'])
 assert text(im,actual,25,132,132)<188;bounds+=1
 text(im,'Заметка архивариуса',196,49,133);text(im,'То, как я понимаю это слово:',196,73,133)
 dr.rectangle((194,115,334,137),fill='#dbc392');dr.line((194,138,334,138),fill='#a88760');text(im,'Сохраняется в этой книге.',196,141,135)
 dr.rectangle((215,159,330,176),fill='#6a4240');text(im,'Записать мысль',225,164,col='#e1cba7')
 for x,label,w in [(20,'<',28),(312,'>',28),(240,'Найти',60)]:
  dr.rectangle((x,188,x+w-1,203),fill='#6a4240');text(im,label,x+(w-sum(map(advance,label)))//2,192,col='#e1cba7')
 dr.rectangle((60,188,140,203),fill='#161616');text(im,'Слово...',63,192,col='#dddddd')
 label=f'{next(i for i,w in enumerate(words) if w["word"]==word)+1} / 180';text(im,label,180-sum(map(advance,label))//2,192,col='#dcc28f')
 im.resize((720,432),Image.Resampling.NEAREST).save(out/(word+'.png'))
# All 180 descriptions/translations obey the same bounds, not only the previews above.
for row in words:
 check_len=len(wrap('Значение: '+row['translation'],132))*9;assert 91+check_len<132,row['word'];bounds+=1
 assert 132+len(wrap(row['description'],132))*9<188,row['word'];bounds+=1
print('WORD PAGE BOUNDS PASSED:',bounds,'checks; 8 representative offline previews.')
