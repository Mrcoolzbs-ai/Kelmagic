from pathlib import Path
from PIL import Image,ImageDraw
import json,random
r=Path('/data/KelMagic');a=r/'src/main/resources/assets/kelmagic';d=r/'src/main/resources/data'
def js(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2))
def save(im,p):p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
# Woven gold, seamless at 16px, no vanilla wool flammability registration/tag.
im=Image.new('RGBA',(16,16));g=ImageDraw.Draw(im)
for y in range(16):
 for x in range(16):
  colors=['#af792f','#c3943d','#dfb65c','#ebc873'];v=(x//2+y//2)%4;g.point((x,y),fill=colors[v]);
  if y%4==0:g.point((x,y),fill='#efcf7c' if x%4<2 else '#c3943d')
save(im,a/'textures/block/golden_wool.png');js(a/'models/block/golden_wool.json',{'parent':'minecraft:block/cube_all','textures':{'all':'kelmagic:block/golden_wool'}});js(a/'models/item/golden_wool.json',{'parent':'kelmagic:block/golden_wool'});js(a/'blockstates/golden_wool.json',{'variants':{'':{'model':'kelmagic:block/golden_wool'}}})
im=Image.new('RGBA',(32,32));g=ImageDraw.Draw(im)
g.line([(6,23),(5,16),(9,9),(14,7),(23,10),(26,17),(22,24),(13,25),(9,21),(10,16),(14,13),(20,15),(21,20),(17,23)],fill='#73512c',width=4);g.line([(6,22),(6,16),(10,10),(14,8),(22,11),(25,17),(21,23),(13,24),(10,21),(11,16),(14,14),(19,16),(20,20),(17,22)],fill='#e2bc65',width=2);g.line((12,9,18,9),fill='#fff0ae');g.line((14,25,12,29),fill='#dbad52',width=2)
save(im,a/'textures/item/golden_thread.png');js(a/'models/item/golden_thread.json',{'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/golden_thread'}})
# Dyed casing is isolated from the invariant gold trim, reels and label.
shell=Image.new('RGBA',(32,32));g=ImageDraw.Draw(shell);g.rectangle((5,8,26,25),fill='#b0b0b0');g.line((6,8,25,8),fill='#eeeeee');g.line((5,24,26,24),fill='#777777');save(shell,a/'textures/item/tape_casing.png')
for recorded in [False,True]:
 trim=Image.new('RGBA',(32,32));g=ImageDraw.Draw(trim);g.rectangle((3,6,28,27),outline='#d8b476',width=2);g.line((5,6,26,6),fill='#f4dc98')
 for x in [10,21]:g.ellipse((x-4,9,x+4,17),fill='#b79562',outline='#201c2b');g.ellipse((x-2,11,x+2,15),fill='#302c37');g.line((x,10,x,16),fill='#e5cd94')
 g.rectangle((8,22,23,25),fill='#a773d0' if recorded else '#cda579');g.rectangle((10,23,21,24),fill='#78cad0' if recorded else '#403447');save(trim,a/f'textures/item/tape_trim{"_recorded" if recorded else ""}.png')
patterns=[['00011000','00111100','01111110','11100111','11100111','01111110','00111100','00011000'],['00000000','01100110','01100110','00011000','00111100','00111100','00100100','00000000'],['00111100','01111110','01011010','01111110','00111100','00100100','00111100','00000000'],['00011000','00100100','01011010','10111101','10111101','01011010','00100100','00011000'],['00111100','01111110','11011011','11111111','11111111','11011011','01111110','00111100'],['00000000','01100110','11111111','11011011','11111111','01111110','00100100','00000000'],['00011100','00100110','01010010','01011010','01001110','00100000','00011110','00000000'],['00000000','01100110','01000010','00111100','00100100','00011000','00100100','00000000'],['00011000','00111100','01100110','11000011','11000011','01100110','00111100','00011000']]
for i,rows in enumerate(patterns,1):
 im=Image.new('RGBA',(32,32));g=ImageDraw.Draw(im);g.rectangle((11,18,20,27),fill='#211a2a',outline='#edca84');
 for y,row in enumerate(rows):
  for x,v in enumerate(row):
   if v=='1':g.point((12+x,19+y),fill='#ffefc9')
 save(im,a/f'textures/item/tape_pattern_{i}.png');js(a/f'models/item/cassette_pattern_{i}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/tape_casing','layer1':'kelmagic:item/tape_trim_recorded','layer2':f'kelmagic:item/tape_pattern_{i}'}})
js(a/'models/item/recorded_cassette.json',{'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/tape_casing','layer1':'kelmagic:item/tape_trim_recorded'}})
js(a/'models/item/cassette.json',{'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/tape_casing','layer1':'kelmagic:item/tape_trim'},'overrides':[{'predicate':{'kelmagic:recorded':1},'model':'kelmagic:item/recorded_cassette'}]+[{'predicate':{'kelmagic:recorded':1,'kelmagic:pattern':i},'model':f'kelmagic:item/cassette_pattern_{i}'} for i in range(1,10)]})
cloth=json.loads((d/'kelmagic/recipe/magic_cloth.json').read_text());cloth['key']['S']['item']='kelmagic:golden_thread';js(d/'kelmagic/recipe/magic_cloth.json',cloth)
js(d/'kelmagic/recipe/golden_thread.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['SSS','SGS','SSS'],'key':{'S':{'item':'minecraft:string'},'G':{'item':'minecraft:gold_ingot'}},'result':{'id':'kelmagic:golden_thread','count':8}})
js(d/'kelmagic/recipe/golden_wool.json',{'type':'minecraft:crafting_shaped','category':'building','pattern':['TT','TT'],'key':{'T':{'item':'kelmagic:golden_thread'}},'result':{'id':'kelmagic:golden_wool','count':1}})
js(d/'kelmagic/recipe/decorate_tape.json',{'type':'kelmagic:decorate_tape','category':'misc'})
js(d/'kelmagic/loot_table/blocks/golden_wool.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'kelmagic:golden_wool'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
entries=[('kelmagic:golden_thread',20,2,3),('minecraft:glow_ink_sac',25,1,3),('minecraft:amethyst_shard',25,4,8),('minecraft:prismarine_crystals',20,2,5),('minecraft:echo_shard',5,1,1),('kelmagic:magic_cloth',5,1,2)]
js(d/'kelmagic/loot_table/gameplay/golden_wool_barter.json',{'type':'minecraft:barter','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':n,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]} for n,weight,lo,hi in entries]}]})
# New advancement tree. Impossible criteria are awarded from verified server gameplay.
adv=[('root',None,'mana_core','Магия начинается','Найдите ядро маны или произнесите первое слово.'),('first_word','root','manuscript_mina','Первое слово','Произнесите слово Кэл.'),('first_spell','first_word','scripter','Смысл обретает силу','Успешно завершите заклинание.'),('combination','first_spell','manuscript_rak','Два голоса','Примените сочетание слов.'),('neutral_magic','combination','manuscript_mil','Не враг, пока не тронут','Сделайте моба нейтральным магией.'),('flame_life','combination','manuscript_liv','Жизнь в пламени','Примените mina liv pir.'),('golden_thread','root','golden_thread','Золотая прядь','Получите золотую нить.'),('golden_wool','golden_thread','golden_wool','Мягкое золото','Получите блок золотой шерсти.'),('wool_barter','golden_wool','golden_wool','Пиглин оценил','Обменяйте золотую шерсть у взрослого спокойного пиглина.'),('magic_cloth','golden_thread','magic_cloth','Переплетение маны','Получите магическую ткань.'),('dragon_heart','root','dragon_heart','Второе сердце','Пробудите сердце дракона.'),('cassette_deck','root','cassette_deck','Мастерская эха','Получите кассетную деку.'),('recorded_tape','cassette_deck','cassette','Цена записи','Получите записанную кассету.'),('personal_tape','recorded_tape','cassette','Моя лента','Окрасьте или украсьте записанную кассету.'),('cassette_player','recorded_tape','cassette_player','Восемь голосов','Получите кассетный плеер.'),('play_tape','cassette_player','cassette_player','Нажмите Play','Воспроизведите записанную кассету.'),('complete_codex','first_spell','archivists_codex','Архивариус','Соберите все 180 слов в обычном кодексе.'),('overload_101','combination','manuscript_muk','Сто первого не было в плане','Вызовите перегруз 101-м словом.')]
for n,parent,icon,title,desc in adv:
 display={'icon':{'id':'kelmagic:'+icon},'title':{'translate':'advancements.kelmagic.'+n+'.title'},'description':{'translate':'advancements.kelmagic.'+n+'.description'},'frame':'challenge' if n in ['complete_codex','overload_101'] else 'task','show_toast':True,'announce_to_chat':n in ['complete_codex','overload_101'],'hidden':n in ['complete_codex','overload_101']}
 if n=='root':display['background']='kelmagic:textures/gui/advancements_background.png'
 v={'display':display,'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']]}
 if parent:v['parent']='kelmagic:'+parent
 if n=='complete_codex':v['rewards']={'experience':300}
 js(d/f'kelmagic/advancement/{n}.json',v)
im=Image.new('RGBA',(32,32),'#292131');g=ImageDraw.Draw(im)
for y in range(0,32,8):g.line((0,y,31,y),fill='#322839')
for x in range(0,32,8):g.line((x,0,x,31),fill='#332938')
g.point((16,16),fill='#715436');save(im,a/'textures/gui/advancements_background.png')
for lang in ['ru_ru','en_us']:
 p=a/f'lang/{lang}.json';v=json.loads(p.read_text());v.update({'item.kelmagic.golden_thread':'Золотая нить' if lang=='ru_ru' else 'Golden Thread','block.kelmagic.golden_wool':'Золотая шерсть' if lang=='ru_ru' else 'Golden Wool','kelmagic.tape.cost':'Плеер: %s маны' if lang=='ru_ru' else 'Player: %s mana','kelmagic.tape.pattern':'Узор: %s' if lang=='ru_ru' else 'Pattern: %s'})
 for n,parent,icon,title,desc in adv:v['advancements.kelmagic.'+n+'.title']=title if lang=='ru_ru' else n.replace('_',' ').title();v['advancements.kelmagic.'+n+'.description']=desc if lang=='ru_ru' else {'root':'Obtain a mana core or speak your first word.','complete_codex':'Learn all 180 words in an ordinary codex.','overload_101':'Cause an overload with the 101st word.','wool_barter':'Trade golden wool with a calm adult piglin.','personal_tape':'Dye or decorate a recorded cassette.'}.get(n,'Complete this step on your magical journey.')
 js(p,v)
# Update descriptions without re-running historical generators.
p=d/'kelmagic/magic/effects.json';v=json.loads(p.read_text());v['effects']['mil']['description']='Делает агрессивного моба нейтральным: не нападает первым, но отвечает на удар.';v['effects']['nak']['description']='Делает мирного моба нейтральным: теперь он отвечает атакой на удар.';v['effects']['sol']['description']='Зажигает факел на свободной опоре рядом с заклинателем; mina не нужна.';js(p,v)
p=d/'kelmagic/magic/words.json';w=json.loads(p.read_text());
for row in w['words']:
 if row['word'] in v['effects']:row['description']=v['effects'][row['word']]['description']
js(p,w)
(r/'docs/architecture/ADVANCEMENT_POLICY.md').write_text('''# Политика достижений

В каждом обновлении добавлять достижения для новой игровой механики, её прогрессии и хотя бы одного содержательного испытания, если это уместно. Секретные испытания используют display.hidden=true; выдаются только после серверной проверки события. Не раскрывать секретные узлы клиентскими подсказками до завершения. Сохранять существующие ID, не обнулять прогресс миров. Обновлять документы, генератор и GameTest на новые критерии.

1.8.0: 18 узлов, из них 2 скрытых испытания. Творческий полный кодекс не засчитывает завершение обычного кодекса.
''')
print('1.8 textures, recipes, barter loot and 18 advancements generated')
