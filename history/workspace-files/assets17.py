from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import json
r=Path('/data/KelMagic');a=r/'src/main/resources/assets/kelmagic';d=r/'src/main/resources/data';
def js(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2))
def save(im,p):p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
# Small pixel-native icons: matching gold trim, violet mana, copper analogue devices.
for name in ['cassette','cassette_player']:
 im=Image.new('RGBA',(32,32));g=ImageDraw.Draw(im)
 g.rectangle((3,7 if name=='cassette' else 3,28,25 if name=='cassette' else 28),fill='#291f34',outline='#d8b476',width=2)
 g.rectangle((6,9 if name=='cassette' else 6,25,18 if name=='cassette' else 19),fill='#58435b')
 for x in (11,21):g.ellipse((x-4,11,x+4,19),fill='#b79562',outline='#201c2b');g.ellipse((x-2,13,x+2,17),fill='#302c37');g.line((x,12,x,18),fill='#e5cd94')
 g.rectangle((8,22,23,24),fill='#cda579');g.rectangle((10,22,21,23),fill='#403447')
 if name=='cassette_player':g.rectangle((9,6,22,8),fill='#7f5ac0');g.rectangle((5,25,7,26),fill='#77d3dc');g.rectangle((23,24,26,27),fill='#b5784b')
 save(im,a/f'textures/item/{name}.png');js(a/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'kelmagic:item/{name}'}})
for face in ['front','side','top']:
 im=Image.new('RGB',(16,16),'#3d303d');g=ImageDraw.Draw(im);g.rectangle((0,0,15,15),outline='#c59a62');g.line((0,1,15,1),fill='#edd19a');g.line((0,14,15,14),fill='#1f1927')
 if face=='front':
  g.rectangle((2,4,13,10),fill='#191722',outline='#b58455')
  for x in [5,10]:g.ellipse((x-2,5,x+2,9),fill='#8c6d91');g.point((x,7),fill='#dcbf87')
  g.rectangle((2,12,9,12),fill='#9464b0');g.rectangle((11,12,13,12),fill='#74c8c8')
 elif face=='top':
  for y in range(4,12,2):g.line((3,y,12,y),fill='#271f30')
 else:
  for x in range(3,13,2):g.line((x,4,x,11),fill='#281f2b')
 save(im,a/f'textures/block/cassette_deck_{face}.png')
faces={side:{'texture':'#front' if side=='north' else '#top' if side=='up' else '#side'} for side in ['north','south','east','west','up','down']}
js(a/'models/block/cassette_deck.json',{'textures':{'front':'kelmagic:block/cassette_deck_front','side':'kelmagic:block/cassette_deck_side','top':'kelmagic:block/cassette_deck_top','particle':'kelmagic:block/cassette_deck_side'},'elements':[{'from':[0,0,0],'to':[16,12,16],'faces':faces}]})
js(a/'models/item/cassette_deck.json',{'parent':'kelmagic:block/cassette_deck'})
js(a/'blockstates/cassette_deck.json',{'variants':{'':{'model':'kelmagic:block/cassette_deck'}}})
# GUI: textured walnut/copper, recessed violet slot bays and a clean analogue top panel.
for name,height in [('tape_player',208),('tape_deck',197)]:
 im=Image.new('RGBA',(256,256));g=ImageDraw.Draw(im);g.rectangle((0,0,175,height-1),fill='#251e30',outline='#bd955e',width=2);g.rectangle((3,3,172,height-4),outline='#58414b');g.rectangle((7,6,168,23),fill='#473345')
 for y in range(25,104):
  col='#302635' if y%5 else '#332a37';g.line((7,y,168,y),fill=col)
 # low-contrast fine copper traces in fixed margin (never under item icons).
 for yy in (40,97):g.line((12,yy,164,yy),fill='#5e4647');g.ellipse((10,yy-1,12,yy+1),fill='#9e7952');g.ellipse((164,yy-1,166,yy+1),fill='#9e7952')
 slots=[(24+(i%4)*38,54+(i//4)*29) for i in range(8)] if name=='tape_player' else [(29,58),(65,58),(137,58)]
 for i,(x,y) in enumerate(slots):
  g.rectangle((x-2,y-2,x+17,y+17),fill='#ad8458');g.rectangle((x-1,y-1,x+16,y+16),fill='#181521');g.line((x-1,y+16,x+16,y+16),fill='#655060')
 if name=='tape_deck':
  g.line((91,66,123,66),fill='#d7b274',width=2);g.polygon([(120,62),(125,66),(120,70)],fill='#d7b274')
 iy=126 if name=='tape_player' else 115;hy=184 if name=='tape_player' else 173
 for y in [iy,iy+18,iy+36,hy]:
  for x in range(9):xx=8+x*18;g.rectangle((xx-1,y-1,xx+16,y+16),fill='#18151f',outline='#68524b');g.line((xx,y+16,xx+16,y+16),fill='#9b7855')
 save(im,a/f'textures/gui/{name}.png')
# Precise shaped recipes.
recipes={'cassette':(['IPI','SRS','STS'],{'I':'iron_nugget','P':'paper','S':'slime_ball','R':'redstone','T':'string'}),'cassette_player':(['IQI','BCB','MRS'],{'I':'iron_ingot','Q':'quartz','B':'stone_button','C':'kelmagic:mana_core','M':'copper_ingot','R':'redstone_block','S':'slime_ball'}),'cassette_deck':(['IPI','RCR','IQI'],{'I':'iron_ingot','P':'paper','R':'redstone_block','C':'kelmagic:cassette','Q':'quartz'})}
for name,(pattern,key) in recipes.items():js(d/f'kelmagic/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:{'item':v if ':' in v else 'minecraft:'+v} for k,v in key.items()},'result':{'id':'kelmagic:'+name,'count':1}})
js(d/'kelmagic/loot_table/blocks/cassette_deck.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'kelmagic:cassette_deck'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
p=d/'minecraft/tags/block/mineable/pickaxe.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['kelmagic:cassette_deck']));js(p,v)
js(d/'kelmagic/damage_type/spell.json',{'message_id':'kelmagic.spell','scaling':'never','exhaustion':0.0})
ru={'item.kelmagic.cassette':'Кассета','item.kelmagic.recorded_cassette':'Записанная кассета','item.kelmagic.cassette_player':'Кассетный плеер','block.kelmagic.cassette_deck':'Кассетная дека','menu.kelmagic.cassette_player':'Кассетный плеер','kelmagic.tape.blank':'Чистая лента: запишите её в деке.','kelmagic.tape.recorded':'На ленте записано заклинание.','kelmagic.tape.cost':'Плеер: %s маны (30%%, минимум 1)','kelmagic.tape.selected':'Кассета %s / 8','kelmagic.tape.controls':'ПКМ: воспроизвести. Shift+ПКМ: кассеты. Shift+колесо: выбор.','kelmagic.tape.unavailable':'Нет записанной кассеты, либо заклинание ещё готовится.','kelmagic.deck.remaining':'До записи: %s с','kelmagic.deck.idle':'Кассета + скриптер → запись','kelmagic.no_target':'Нет подходящей цели','kelmagic.count':'Изумруды: %s; золото: %s; самородки: %s','kelmagic.echo':'Рядом: %s, %s блоков','death.attack.kelmagic.spell':'%1$s не выдержал заклинания %2$s','death.attack.kelmagic.scripter_spell':'%1$s погиб от записанного заклинания %2$s','death.attack.kelmagic.tape_spell':'%1$s был стёрт заклинанием с кассеты %2$s'}
en={'item.kelmagic.cassette':'Cassette','item.kelmagic.recorded_cassette':'Recorded Cassette','item.kelmagic.cassette_player':'Cassette Player','block.kelmagic.cassette_deck':'Cassette Deck','menu.kelmagic.cassette_player':'Cassette Player','kelmagic.tape.blank':'Blank tape: record it in a deck.','kelmagic.tape.recorded':'A spell is recorded on this tape.','kelmagic.tape.cost':'Player: %s mana (30%%, min 1)','kelmagic.tape.selected':'Tape %s / 8','kelmagic.tape.controls':'Use: play. Sneak+use: tapes. Sneak+scroll: select.','kelmagic.tape.unavailable':'No recorded tape, or a spell is being prepared.','kelmagic.deck.remaining':'Recording: %s s left','kelmagic.deck.idle':'Tape + scripter → record','kelmagic.no_target':'No suitable target','kelmagic.count':'Emeralds: %s; gold: %s; nuggets: %s','kelmagic.echo':'Nearby: %s, %s blocks','death.attack.kelmagic.spell':'%1$s succumbed to %2$s\'s spell','death.attack.kelmagic.scripter_spell':'%1$s was undone by %2$s\'s recorded spell','death.attack.kelmagic.tape_spell':'%1$s was erased by %2$s\'s cassette spell'}
for src,text in [('spell','собственным заклинанием'),('scripter_spell','эхом собственного скриптера'),('tape_spell','заклинанием со своей кассеты'),('scripter_mana_debt','скриптером, потребовавшим слишком много маны'),('tape_mana_debt','плеером, истратившим последнюю жизненную силу'),('scripter_overload','перегруженным скриптером'),('tape_overload','перегруженной кассетой')]:
 for i in range(1,5):ru[f'death.attack.kelmagic.{src}.{i}']=f'%1$s погублен {text}';en[f'death.attack.kelmagic.{src}.{i}']='%1$s was consumed by '+src.replace('_',' ')
for lang,extra in [('ru_ru',ru),('en_us',en)]:
 p=a/f'lang/{lang}.json';v=json.loads(p.read_text());v.update(extra);js(p,v)
# Named effects/recipes remain discoverable in the codex artifacts tab.
p=r/'src/main/java/dev/kelmagic/client/CodexScreen.java';s=p.read_text().replace('"mage_boots","archivists_codex"','"mage_boots","archivists_codex","cassette","cassette_deck","cassette_player"')
s=s.replace('if(n.equals("mana_core"))return', 'if(n.equals("cassette"))return new String[]{"IPI","SRS","STS"};\n  if(n.equals("cassette_deck"))return new String[]{"IPI","RCR","IQI"};\n  if(n.equals("cassette_player"))return new String[]{"IQI","BCB","MRS"};\n  if(n.equals("mana_core"))return',1)
s=s.replace('if(c==\'M\')return', '''if(n.equals("cassette"))return switch(c){case 'I'->"minecraft:iron_nugget";case 'P'->"minecraft:paper";case 'S'->"minecraft:slime_ball";case 'R'->"minecraft:redstone";default->"minecraft:string";};
  if(n.equals("cassette_deck"))return switch(c){case 'I'->"minecraft:iron_ingot";case 'P'->"minecraft:paper";case 'R'->"minecraft:redstone_block";case 'C'->"kelmagic:cassette";default->"minecraft:quartz";};
  if(n.equals("cassette_player"))return switch(c){case 'I'->"minecraft:iron_ingot";case 'Q'->"minecraft:quartz";case 'B'->"minecraft:stone_button";case 'C'->"kelmagic:mana_core";case 'M'->"minecraft:copper_ingot";case 'R'->"minecraft:redstone_block";default->"minecraft:slime_ball";};
  if(c=='M')return''')
s=s.replace('case "mana_core"->"В золотой', 'case "cassette"->"Тонкая лента хранит эхо слов. В деке оно обретает повторяемую форму.";case "cassette_deck"->"Два входа и один выход. Каждое слово занимает пять секунд записи; скриптер остаётся целым.";case "cassette_player"->"Восемь лент в медном корпусе. ПКМ — воспроизвести, Shift+ПКМ — кассеты, Shift+колесо — выбор.";case "mana_core"->"В золотой',1)
p.write_text(s)
# Enlarged handoff catalogue with synergies.
words=json.loads((r/'docs/kel-language/CATALOG-1.7.0.json').read_text())['words'];text='# Каталог KelMagic 1.7.0\n\n180 слов; 172 неповторяющихся профиля действий и 8 операторов/селекторов. Игровые значения — интерпретация, переводы и глифы сохранены.\n\n| Слово | Перевод | Действие |\n|---|---|---|\n'
for w in words:text+='| '+w['word']+' | '+w['translation']+' | '+w['description']+' |\n'
text+='\n## Особые сочетания\n\nПорядок слов не важен. muk/nis меняют масштаб. Мировые сочетания используют блок прицела, а «Страж» и «Пружина» — самого заклинателя.\n\n| Слова | Название | Результат |\n|---|---|---|\n'
for s in json.loads((d/'kelmagic/magic/synergies.json').read_text())['synergies']:text+='| '+' '.join(s['words'])+' | '+s['name']+' | '+s['description']+' |\n'
text+='| mina liv pir | Жизнь в пламени | Условное лечение при горении и защита от огненного урона, кроме лавы/горячего пола. |\n\nОбычные комбинации других действий выполняют соответствующие эффекты вместе (до восьми действий). Добыча использует текущий инструмент и обычные правила добычи, не обходит бедрок/приват. Создание не заменяет твёрдые блоки, сундуки или существ. Обратные мировые эффекты no доступны для воды, льда, создания материала и превращений; остальные не исполняются вместо молчаливого игнорирования.\n'
(r/'docs/kel-language/CATALOG-1.7.0.md').write_text(text)
print('audio textures, 3 recipes, loot, language and codex updated')
