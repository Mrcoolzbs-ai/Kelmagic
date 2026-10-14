exec(open('/data/preview19.py').read().split("# Review native UI")[0])
# Codex pages at their actual bitmap-font positions, including the new rarity line.
import re
src=(r/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();rar=json.loads((r/'src/main/resources/data/kelmagic/magic/rarities.json').read_text())['words'];effects=json.loads((r/'src/main/resources/data/kelmagic/magic/effects.json').read_text())['effects'];words=json.loads((r/'src/main/resources/data/kelmagic/magic/words.json').read_text())['words'];labels={'common':'Обычное','uncommon':'Необычное','rare':'Редкое','epic':'Эпическое'}
for word in ['vak','riv','mar']:
 im=Image.open(a/'textures/gui/codex.png').convert('RGBA');g=ImageDraw.Draw(im)
 for i,t in enumerate(['Слова','Артефакты']):g.rectangle((22+i*160,16,175+i*160,33),fill='#6a4240');text(im,t,22+i*160+(154-sum(map(advance,t)))//2,21,col='#e1cba7')
 text(im,'Архивариусный кодекс',100,3,col='#dcc28f');text(im,word,25,47,135);text(im,'Значение: '+next(w['translation'] for w in words if w['word']==word),25,91,132);text(im,labels[rar[word]],25,118,132)
 for n,line in enumerate(wrap(effects[word]['description'],132)[:5]):text(im,line,25,132+n*9)
 text(im,'Заметка архивариуса',196,49,133);text(im,'То, как я понимаю это слово:',196,73,133);g.rectangle((194,115,334,137),fill='#dbc392');text(im,'Сохраняется в этой книге.',196,141,135);g.rectangle((215,159,331,176),fill='#6a4240');text(im,'Записать мысль',224,164,col='#e1cba7');text(im,str([w['word'] for w in words].index(word)+1)+' / 180',165,192,col='#dcc28f');im.resize((720,432),Image.Resampling.NEAREST).save(out/f'codex-{word}-rarity.png')
# Actual generated item layer composites with a gold trim and blue robe dye.
for prefix in ['arcane','mage']:
 for slot in ['helmet','chestplate','leggings','boots']:
  name=prefix+'_'+slot;im=Image.open(a/f'textures/item/{name}.png').convert('RGBA')
  if prefix=='mage':
   px=im.load()
   for y in range(im.height):
    for x in range(im.width):rr,gg,bb,alpha=px[x,y];px[x,y]=(rr*68//255,gg*119//255,bb*204//255,alpha)
   im.alpha_composite(Image.open(a/f'textures/item/{name}_overlay.png').convert('RGBA'))
  trim=Image.open(a/f'textures/item/{name}_trim.png').convert('RGBA');px=trim.load()
  for y in range(trim.height):
   for x in range(trim.width):rr,gg,bb,alpha=px[x,y];px[x,y]=(222,182,72,alpha)
  im.alpha_composite(trim);im.resize((320,320),Image.Resampling.NEAREST).save(out/(name+'-trimmed.png'))
print('11 new rarity/item layer previews generated')
