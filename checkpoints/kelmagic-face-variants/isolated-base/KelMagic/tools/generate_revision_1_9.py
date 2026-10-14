from pathlib import Path
import json,zipfile,io,sys
from PIL import Image,ImageDraw
from nbt19 import write
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';d=r/'src/main/resources/data'
def js(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2))
def save(p,im):p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
# Pixel geometry is intentional, matching existing Minecraft assets.
for n in ['magic_torch','magic_lantern','temporary_end_gate']:
 im=Image.new('RGBA',(16,16));g=ImageDraw.Draw(im)
 if n=='magic_torch':
  g.rectangle((6,4,9,15),fill='#766147');g.line((6,7,6,15),fill='#ba9057');g.rectangle((6,0,9,4),fill='#b891f3');g.rectangle((7,0,8,3),fill='#e9d9ff')
 elif n=='magic_lantern':
  im=Image.new('RGBA',(16,16),'#261e38');g=ImageDraw.Draw(im);g.rectangle((1,1,14,14),outline='#b88c50',width=2);g.rectangle((4,4,11,11),fill='#ba98f0');g.rectangle((6,5,9,10),fill='#efe2ff');g.point((5,5),fill='#ffeaca')
 else:
  im=Image.new('RGBA',(16,16),'#19162d');g=ImageDraw.Draw(im)
  for k in range(22):x=(k*7+3)%16;y=(k*11+5)%16;g.point((x,y),fill=['#6e54af','#ba96e9','#e4d0fa'][k%3])
 save(a/f'textures/block/{n}.png',im)
 js(a/f'blockstates/{n}.json',{'variants':{'':{'model':f'kelmagic:block/{n}'}}})
 js(a/f'models/block/{n}.json',{'parent':'minecraft:block/template_torch','textures':{'torch':f'kelmagic:block/{n}'}} if n=='magic_torch' else {'parent':'minecraft:block/cube_all','textures':{'all':f'kelmagic:block/{n}'}})
# Table UV: desk legs, sloping reader, manuscript and mana lamp.
for n in ['top','side','book','lamp']:
 im=Image.new('RGBA',(16,16),'#31413d');g=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(16):g.point((x,y),fill=['#2b3937','#344743','#455950'][(x//4+y//2)%3])
 if n=='top':g.rectangle((1,1,14,14),outline='#baa465');g.line((2,13,13,13),fill='#899b72')
 if n=='book':g.rectangle((1,1,14,14),fill='#e5d6a6');g.line((7,2,7,13),fill='#aa8c56');[g.line((2,y,5,y),fill='#7b7052') for y in [4,7,10]];[g.line((9,y,13,y),fill='#7b7052') for y in [4,7,10]]
 if n=='lamp':g.rectangle((5,2,10,13),fill='#d4b270');g.rectangle((6,4,9,10),fill='#92ded7')
 save(a/f'textures/block/archive_{n}.png',im)
faces=lambda tex:{s:{'texture':'#'+tex} for s in ['north','south','east','west','up','down']}
elements=[{'from':[0,10,0],'to':[16,12,16],'faces':faces('top')}]+[{'from':[x,0,z],'to':[x+2,10,z+2],'faces':faces('side')} for x in [1,13] for z in [1,13]]+[{'from':[2,12,4],'to':[12,13,12],'faces':faces('book')},{'from':[13,12,3],'to':[15,16,5],'faces':faces('lamp')}]
js(a/'models/block/archivist_table.json',{'textures':{'particle':'kelmagic:block/archive_side',**{n:f'kelmagic:block/archive_{n}' for n in ['top','side','book','lamp']}},'elements':elements,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.7,.7,.7]},'ground':{'scale':[.5,.5,.5]},'fixed':{'rotation':[0,180,0],'scale':[.5,.5,.5]}}})
js(a/'models/item/archivist_table.json',{'parent':'kelmagic:block/archivist_table'});js(a/'blockstates/archivist_table.json',{'variants':{'':{'model':'kelmagic:block/archivist_table'}}})
js(d/'kelmagic/recipe/archivist_table.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['BLB','CLC','PPP'],'key':{'B':{'item':'minecraft:book'},'L':{'item':'minecraft:lantern'},'C':{'item':'kelmagic:mana_core'},'P':{'item':'minecraft:lectern'}},'result':{'id':'kelmagic:archivist_table','count':1}})
js(d/'kelmagic/loot_table/blocks/archivist_table.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'kelmagic:archivist_table'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
im=Image.new('RGBA',(256,256));g=ImageDraw.Draw(im);g.rectangle((0,0,209,193),fill='#202e30',outline='#b6a573',width=3);g.rectangle((6,6,203,188),outline='#546a60');g.rectangle((12,24,198,88),fill='#293b3e');g.line((14,92,196,92),fill='#8b9873');g.rectangle((151,64,163,67),fill='#d8c78c');g.polygon([(161,60),(168,66),(161,72)],fill='#d8c78c')
for x in [22,53,84,115,170]:g.rectangle((x-1,57,x+16,74),fill='#111d25',outline='#aca26b')
for row in range(3):
 for col in range(9):x=24+18*col;y=112+18*row;g.rectangle((x-1,y-1,x+16,y+16),fill='#132129',outline='#6b7864')
for col in range(9):x=24+18*col;g.rectangle((x-1,169,x+16,186),fill='#132129',outline='#928761')
save(a/'textures/gui/archive_table.png',im)
# Seven separate profession overlays, each with a biome-specific cape/hat/sash.
palettes={'plains':('#42585b','#c6ad6b'),'desert':('#b87e4b','#ead599'),'taiga':('#49644f','#bfa97c'),'snow':('#76909e','#e3e4dc'),'savanna':('#b35237','#dda363'),'jungle':('#4d6c58','#dbad68'),'swamp':('#594a73','#aba16d')}
for biome,(coat,trim) in palettes.items():
 im=Image.new('RGBA',(64,64));g=ImageDraw.Draw(im)
 # Actual vanilla villager 64x64 UV coat/body, arms, nose and hat regions.
 g.rectangle((16,20,43,37),fill=coat);g.rectangle((16,38,51,55),fill=coat);g.rectangle((44,22,59,37),fill=coat)
 g.rectangle((20,22,27,36),outline=trim,width=1);g.rectangle((28,22,35,36),outline=trim,width=1);g.line((17,33,42,33),fill=trim,width=2)
 g.rectangle((30,25,34,29),fill='#dec89a');g.line((31,26,33,26),fill='#55462e');g.rectangle((45,29,49,32),fill=trim)
 # Distinct cap and lapel details; preserve face transparency.
 g.rectangle((32,0,63,3),fill=coat);g.line((32,3,63,3),fill=trim)
 for i in range(list(palettes).index(biome)+1):g.point((20+i*2,40+i%3),fill=trim)
 # Correct VillagerModel jacket UV: texOffs(0,38), front=(6,44)-(14,64).
 g.rectangle((0,38,27,63),fill=coat);g.rectangle((22,26,29,37),fill=coat)
 g.line((6,44,6,63),fill=trim);g.line((13,44,13,63),fill=trim);g.line((7,44,9,48),fill=trim);g.line((12,44,10,48),fill=trim)
 g.rectangle((6,53,13,54),fill=trim);g.rectangle((7,61,12,62),fill=trim);g.rectangle((11,48,13,52),fill='#e3d0a5');g.point((12,49),fill='#705e45')
 g.rectangle((48,26,51,33),fill=coat);g.line((48,32,51,32),fill=trim)
 g.rectangle((40,8,47,9),fill=coat);g.line((40,10,47,10),fill=trim)
 if biome in ['snow','taiga']:g.line((7,44,12,44),fill='#d7d7c9',width=2)
 if biome=='desert':g.rectangle((7,58,12,60),fill='#d9bd7d')
 if biome=='jungle':g.line((8,55,11,60),fill='#87ad72')
 if biome=='swamp':g.rectangle((8,57,11,59),fill='#b09acb')
 save(a/f'textures/entity/villager/profession/archivist_{biome}.png',im)
 js(a/f'textures/entity/villager/profession/archivist_{biome}.png.mcmeta',{'villager':{'hat':'partial'}})
# Villager type is named snowy, not snow, in registry.
save(a/'textures/entity/villager/profession/archivist_snowy.png',Image.open(a/'textures/entity/villager/profession/archivist_snow.png'))
save(a/'textures/entity/villager/profession/archivist.png',Image.open(a/'textures/entity/villager/profession/archivist_plains.png'))
im=Image.new('RGBA',(18,18));g=ImageDraw.Draw(im);g.polygon([(8,14),(1,6),(1,2),(5,5),(8,9)],fill='#c7a8ff');g.polygon([(10,14),(17,6),(17,2),(13,5),(10,9)],fill='#c7a8ff');g.line((9,4,9,15),fill='#fff0cd',width=2);save(a/'textures/mob_effect/spectral_wings.png',im)
js(d/'minecraft/tags/point_of_interest_type/acquirable_job_site.json',{'replace':False,'values':['kelmagic:archivist']})
# Smithing trims use vanilla machinery; preserve armor components/attributes.
js(d/'minecraft/tags/item/trimmable_armor.json',{'replace':False,'values':[f'kelmagic:{p}_{s}' for p in ['arcane','mage'] for s in ['helmet','chestplate','leggings','boots']]})
# Inventory trim overlays: vanilla inventories distinguish material, not the pattern's full model.
save(a/'textures/item/empty_layer.png',Image.new('RGBA',(32,32)))
for prefix in ['arcane','mage']:
 for slot in ['helmet','chestplate','leggings','boots']:
  name=prefix+'_'+slot;im=Image.open(a/f'textures/item/{name}.png').convert('RGBA');mask=im.getchannel('A');trim=Image.new('RGBA',im.size);pixels=trim.load()
  for y in range(1,im.height-1):
   for x in range(1,im.width-1):
    if mask.getpixel((x,y))>0 and any(mask.getpixel((x+dx,y+dy))==0 for dx,dy in [(0,1),(0,-1),(1,0),(-1,0)]):pixels[x,y]=(255,255,255,255)
  save(a/f'textures/item/{name}_trim.png',trim)
  path=a/f'models/item/{name}.json';model=json.loads(path.read_text());model['overrides']=[{'predicate':{'kelmagic:trimmed':1},'model':f'kelmagic:item/{name}_trimmed'}];js(path,model)
  textures=dict(model['textures']);textures.setdefault('layer1','kelmagic:item/empty_layer');textures['layer2']=f'kelmagic:item/{name}_trim';js(a/f'models/item/{name}_trimmed.json',{'parent':'minecraft:item/generated','textures':textures})
# Structures: two distinct reading halls per actual village biome. Ordinary villager jigsaw entrance.
z=zipfile.ZipFile(r/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
styles={'plains':('oak_planks','oak_log','oak_stairs','cobblestone'),'desert':('cut_sandstone','smooth_sandstone','sandstone_stairs','sandstone'),'taiga':('spruce_planks','spruce_log','spruce_stairs','mossy_cobblestone'),'snowy':('spruce_planks','stripped_spruce_log','spruce_stairs','stone_bricks'),'savanna':('acacia_planks','acacia_log','acacia_stairs','terracotta')}
for biome,(planks,log,stairs,stone) in styles.items():
 loot=json.loads(z.read(f'data/minecraft/loot_table/chests/village/village_{biome}_house.json'));loot['pools'].append({'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.35}],'entries':[{'type':'minecraft:item','name':'kelmagic:ancient_manuscript'}]});js(d/f'kelmagic/loot_table/chests/archive_{biome}.json',loot)
 for variation in [1,2]:
  width,depth=(11,13) if variation==1 else (9,15);height=8;palette=[];blocks={}
  def block(x,y,z,name,props=None,nbt=None):
   state={'Name':(8,name if ':' in name else 'minecraft:'+name)}
   if props:state['Properties']=(10,{k:(8,v) for k,v in props.items()})
   if state not in palette:palette.append(state)
   b={'pos':(9,(3,[x,y,z])),'state':(3,palette.index(state))}
   if nbt:b['nbt']=(10,nbt)
   blocks[x,y,z]=b
  for x in range(width):
   for zz in range(depth):
    block(x,0,zz,stone)
    for y in range(1,6):block(x,y,zz,'air')
    if x in [0,width-1] or zz in [0,depth-1]:
     for y in range(1,5):block(x,y,zz,log if x in [0,width-1] and zz in [0,depth-1] else planks)
    block(x,5,zz,planks)
  # Windows and entrance, stone cornice, low gabled roof.
  for zz in range(3,depth-2,3):
   for x in [0,width-1]:block(x,2,zz,'glass_pane',{'north':'true','south':'true','east':'false','west':'false','waterlogged':'false'})
  for x in range(width):
   for zz in range(depth):
    if x in [0,width-1] or zz in [0,depth-1]:block(x,5,zz,stone)
    if 2<=x<width-2:block(x,6,zz,planks)
    if 4<=x<width-4:block(x,7,zz,planks)
  block(width//2,1,0,'oak_door' if biome=='desert' else planks.replace('planks','door'),{'facing':'north','half':'lower','hinge':'left','open':'false','powered':'false'})
  block(width//2,2,0,'oak_door' if biome=='desert' else planks.replace('planks','door'),{'facing':'north','half':'upper','hinge':'left','open':'false','powered':'false'})
  block(width//2,0,0,'jigsaw',{'orientation':'north_up'},{'id':(8,'minecraft:jigsaw'),'name':(8,'minecraft:building_entrance'),'target':(8,'minecraft:building_entrance'),'pool':(8,f'minecraft:village/{biome}/streets'),'joint':(8,'aligned'),'final_state':(8,f'minecraft:{stairs}[facing=north,half=bottom,shape=straight,waterlogged=false]')})
  for zz in range(2,depth-1):
   for y in [1,2,3]:block(1,y,zz,'bookshelf')
   if variation==2:
    for y in [1,2,3]:block(width-2,y,zz,'bookshelf')
  for zz in range(3,depth-2,3):block(width//2+1,1,zz,stairs,{'facing':'west','half':'bottom','shape':'straight','waterlogged':'false'})
  for zz in [3,depth-3]:block(width//2,4,zz,'lantern',{'hanging':'true','waterlogged':'false'})
  block(width//2-1,1,depth-3,'kelmagic:archivist_table')
  block(width-3,1,depth-2,'chest',{'facing':'north','type':'single','waterlogged':'false'},{'id':(8,'minecraft:chest'),'LootTable':(8,f'kelmagic:chests/archive_{biome}')})
  entities=[{'pos':(9,(6,[width//2+.5,1.,depth-3.5])),'blockPos':(9,(3,[width//2,1,depth-4])),'nbt':(10,{'id':(8,'minecraft:villager'),'VillagerData':(10,{'type':(8,'minecraft:snow' if biome=='snowy' else 'minecraft:'+biome),'profession':(8,'minecraft:none'),'level':(3,1)})})}]
  root={'DataVersion':(3,3955),'size':(9,(3,[width,height,depth])),'palette':(9,(10,palette)),'blocks':(9,(10,list(blocks.values()))),'entities':(9,(10,entities))}
  path=d/f'kelmagic/structure/archive/{biome}_{variation}.nbt';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(write(root))
 # Keep every vanilla entry; archives each weight1 vs native weights x8 (~0.3% per draw).
 pool=json.loads(z.read(f'data/minecraft/worldgen/template_pool/village/{biome}/houses.json'))
 for e in pool['elements']:e['weight']*=8
 for variation in [1,2]:pool['elements'].append({'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':f'kelmagic:archive/{biome}_{variation}','processors':{'processors':[]},'projection':'rigid'}})
 js(d/f'minecraft/worldgen/template_pool/village/{biome}/houses.json',pool)
# Additional advancements expose outcomes, never formulas.
new=[('archive_table','root','archivist_table','Переплётная мастерская','Получите стол архивариуса.'),('archive_word','archive_table','archivists_codex','Чернила обретают смысл','Добавьте новую страницу в кодекс на столе архивариуса.'),('copy_codex','archive_word','archivists_codex','Вторая жизнь книги','Скопируйте кодекс на столе архивариуса.'),('dye_robes','magic_cloth','mage_chestplate','Свой цвет','Покрасьте одеяние мага.'),('trim_magic','dye_robes','arcane_chestplate','Узор мастера','Нанесите кузнечную отделку на магическое снаряжение.')]
for id,icon,title in [('wings','manuscript_pis','Крылья без перьев'),('island','manuscript_nuk','Небо держит землю'),('blast','manuscript_tup','Разрыв тишины'),('duplicate','manuscript_nov','Два отражения'),('ender','manuscript_kut','Карман между мирами'),('mana_tide','mana_core','Прилив силы'),('meteor','manuscript_sap','Звезда упала'),('transcend','dragon_heart','За пределом'),('end_gate','manuscript_dor','Дверь без стены')]:new.append(('rite_'+id,'combination',icon,title,'Испытайте одну из высших форм магии.'))
for n,parent,icon,title,desc in new:
 display={'icon':{'id':'kelmagic:'+icon},'title':{'translate':'advancements.kelmagic.'+n+'.title'},'description':{'translate':'advancements.kelmagic.'+n+'.description'},'frame':'challenge' if n.startswith('rite_') else 'task','show_toast':True,'announce_to_chat':False,'hidden':n.startswith('rite_')}
 js(d/f'kelmagic/advancement/{n}.json',{'parent':'kelmagic:'+parent,'display':display,'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']]})
for lang in ['ru_ru','en_us']:
 p=a/f'lang/{lang}.json';v=json.loads(p.read_text());ru=lang=='ru_ru';v.update({'block.kelmagic.archivist_table':'Стол архивариуса' if ru else 'Archivist Table','block.kelmagic.magic_torch':'Магический факел' if ru else 'Magic Torch','block.kelmagic.magic_lantern':'Магический фонарь' if ru else 'Magic Lantern','block.kelmagic.temporary_end_gate':'Временные врата' if ru else 'Temporary End Gate','entity.minecraft.villager.kelmagic.archivist':'Архивариус' if ru else 'Archivist','effect.kelmagic.spectral_wings':'Призрачные крылья' if ru else 'Spectral Wings','kelmagic.table.hint':'Книга + лист / материалы копии' if ru else 'Book + page / copying materials','kelmagic.codex_hint':'Объединяйте со страницами только на столе архивариуса.' if ru else 'Add pages only at an archivist table.','kelmagic.codex_copy':'Копия: кодекс + книга + перо + чернила на столе архивариуса.' if ru else 'Copy: codex + book + feather + ink at an archivist table.','kelmagic.page_hint':'Добавьте в кодекс на столе архивариуса.' if ru else 'Add to a codex at an archivist table.','kelmagic.transcend.after1':'Небо запомнило твоё имя. Не произноси его вслух.' if ru else 'The sky remembers your name. Do not speak it aloud.','kelmagic.transcend.after2':'Ты вернулся. Но кто-то посмотрел вслед.' if ru else 'You returned. Something watched you leave.'})
 v['advancements.kelmagic.flame_life.description']='Выживите там, где обычная жизнь гаснет.' if ru else 'Survive where ordinary life fades.'
 for n,parent,icon,title,desc in new:v['advancements.kelmagic.'+n+'.title']=title if ru else n.replace('_',' ').title();v['advancements.kelmagic.'+n+'.description']=desc if ru else 'Discover this milestone without a revealed spell formula.'
 js(p,v)
effects_path=d/'kelmagic/magic/effects.json';effects=json.loads(effects_path.read_text());effects['effects']['vid']['description']='Подсвечивает живых существ вокруг игрока, независимо от взгляда и препятствий.';effects['effects']['sol']['description']='Создаёт магический факел на свободной опоре рядом; mina не нужна.';effects['effects']['lun']['description']='Создаёт магический фонарь рядом с выбранным блоком.';effects['effects']['riv']['description']='Создаёт канал из трёх источников воды по направлению взгляда.';js(effects_path,effects)
words_path=d/'kelmagic/magic/words.json';words=json.loads(words_path.read_text())
for word in words['words']:
 if word['word'] in effects['effects']:word['description']=effects['effects'][word['word']]['description']
js(words_path,words)
print('1.9: new light/table assets, 7 biome outfits, 10 archives, 5 pools, 14 new advancements')
