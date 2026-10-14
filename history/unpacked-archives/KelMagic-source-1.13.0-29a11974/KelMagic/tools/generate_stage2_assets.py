from pathlib import Path
from PIL import Image,ImageDraw
import json,random
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';data=r/'src/main/resources/data';out=a/'textures/item';out.mkdir(parents=True,exist_ok=True)
items=['mana_core','magic_ingot','magic_cloth','dragon_heart','scripter']+[f'{p}_{t}' for p in ['arcane','mage'] for t in ['helmet','chestplate','leggings','boots']]
for n in items:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);edge='#0b1726';gold='#c49a54';shine='#f5d49c';cyan='#79f2e6';teal='#297d85';dark='#253e54';mid='#52677e'
 if n=='mana_core':
  d.polygon([(16,2),(28,10),(28,23),(16,30),(4,23),(4,10)],fill=edge)
  d.polygon([(16,4),(26,11),(26,22),(16,28),(6,22),(6,11)],fill=gold)
  d.polygon([(16,7),(23,12),(23,21),(16,25),(9,21),(9,12)],fill=teal)
  d.polygon([(16,8),(16,23),(10,19),(10,12)],fill=cyan);d.polygon([(16,8),(22,12),(22,20),(16,24)],fill='#429ba7');d.line([(8,11),(16,6),(24,11)],fill=shine,width=1)
 elif n=='magic_ingot':
  d.polygon([(3,14),(9,7),(26,7),(30,14),(26,23),(5,24),(2,19)],fill=edge)
  d.polygon([(4,14),(10,9),(25,9),(28,14),(23,17),(6,17)],fill='#86b5c2');d.polygon([(6,18),(23,18),(26,22),(6,22)],fill=dark);d.polygon([(24,17),(28,15),(26,21),(24,22)],fill=teal);d.line([(7,13),(23,13)],fill=cyan,width=1);d.line([(5,23),(24,23)],fill=gold)
 elif n=='magic_cloth':
  d.polygon([(5,4),(28,6),(26,28),(2,25)],fill=edge);d.polygon([(6,6),(26,8),(24,25),(4,23)],fill=dark)
  for y in [8,12,16,20,24]:d.line([(6,y),(24,y+1)],fill=mid)
  d.line([(6,6),(25,8),(23,25),(4,23),(6,6)],fill=gold,width=1);d.line([(16,7),(13,24)],fill=teal,width=2)
 elif n=='dragon_heart':
  d.polygon([(4,9),(7,4),(13,4),(16,8),(20,3),(26,5),(30,11),(27,21),(16,29),(5,21)],fill=edge)
  d.polygon([(6,10),(9,6),(13,6),(17,11),(21,6),(25,7),(27,12),(24,20),(16,26),(8,19)],fill='#66358c')
  d.polygon([(10,9),(14,8),(17,14),(22,8),(25,12),(21,20),(16,24),(11,18)],fill='#a86be3');d.line([(16,10),(13,16),(18,18),(16,24)],fill='#e5ccff',width=2)
  d.line([(5,19),(8,22),(16,28),(24,22),(27,19)],fill=gold,width=1)
 elif n=='scripter':
  d.rectangle((5,4,25,28),fill=edge);d.rectangle((7,6,23,26),fill=dark);d.rectangle((8,7,10,25),fill=gold);d.line([(12,8),(21,8),(21,23),(12,23)],fill=shine)
  d.ellipse((13,12,20,19),fill=teal,outline=gold);d.line([(16,13),(16,18)],fill=cyan,width=2);d.rectangle((3,9,6,13),fill=gold);d.rectangle((3,19,6,23),fill=gold)
 else:
  robe=n.startswith('mage');base='#bdc5ce' if robe else dark;hi='#e7ecf1' if robe else '#5c8294'
  if n.endswith('helmet'):
   d.polygon([(5,25),(5,10),(10,4),(23,4),(27,10),(27,25),(22,25),(22,15),(10,15),(10,25)],fill=edge)
   d.polygon([(7,23),(7,11),(11,6),(22,6),(25,11),(25,23),(23,23),(23,13),(9,13),(9,23)],fill=base);d.line([(9,10),(13,7),(22,8)],fill=hi,width=2)
  elif n.endswith('chestplate'):
   d.polygon([(2,8),(10,4),(13,8),(19,8),(23,4),(30,8),(28,18),(25,16),(25,29),(7,29),(7,16),(4,18)],fill=edge)
   d.polygon([(4,9),(10,6),(13,10),(19,10),(23,6),(28,9),(26,15),(23,13),(23,27),(9,27),(9,13),(6,15)],fill=base);d.line([(10,11),(10,24)],fill=hi,width=2)
  elif n.endswith('leggings'):
   d.rectangle((6,4,25,29),fill=edge);d.rectangle((8,6,23,27),fill=base);d.rectangle((14,14,17,30),fill=(0,0,0,0));d.line([(9,8),(9,26)],fill=hi,width=2);d.line([(19,8),(19,26)],fill=hi,width=2)
  else:
   d.polygon([(5,4),(13,4),(13,21),(16,23),(16,29),(2,29),(2,22),(5,20)],fill=edge);d.polygon([(20,4),(28,4),(28,21),(31,23),(31,29),(17,29),(17,22),(20,20)],fill=edge)
   d.rectangle((7,6,11,24),fill=base);d.rectangle((22,6,26,24),fill=base);d.rectangle((4,24,13,26),fill=base);d.rectangle((19,24,28,26),fill=base)
  ov=Image.new('RGBA',(32,32));od=ImageDraw.Draw(ov)
  if n.endswith('helmet'):od.line([(8,12),(16,9),(24,12)],fill=gold,width=2);od.point((16,10),fill=cyan)
  elif n.endswith('chestplate'):od.rectangle((10,23,22,24),fill=gold);od.ellipse((13,12,19,18),fill=gold);od.rectangle((15,13,17,17),fill=cyan)
  elif n.endswith('leggings'):od.rectangle((8,7,23,8),fill=gold);od.rectangle((8,22,12,23),fill=gold);od.rectangle((19,22,23,23),fill=gold)
  else:od.line([(4,27),(13,27)],fill=gold,width=1);od.line([(19,27),(28,27)],fill=gold,width=1)
  if robe:ov.save(out/(n+'_overlay.png'))
  else:im.alpha_composite(ov)
 im.save(out/(n+'.png'))
 model={'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/'+n}}
 if n.startswith('mage'):model['textures']['layer1']='kelmagic:item/'+n+'_overlay'
 p=a/'models/item'/f'{n}.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(model,indent=2)+'\n')
# Full 128 atlas justified by articulated multi-part 3D models, rather than oversized item textures.
afolder=a/'textures/models/armor';afolder.mkdir(parents=True,exist_ok=True)
for robe in [False,True]:
 tex=Image.new('RGBA',(128,128));d=ImageDraw.Draw(tex);rng=random.Random(42)
 for y in range(128):
  for x in range(64):
   noise=rng.choice([-3,-2,0,0,2]);color=(max(0,161+noise-(y%32)//3),max(0,170+noise-(y%32)//3),max(0,179+noise-(y%32)//3),255) if robe else (max(0,27+noise+(y%24)//2),max(0,52+noise+(y%24)//2),max(0,66+noise+(y%24)//2),255)
   tex.putpixel((x,y),color)
 for y in range(0,112,8):d.line([(0,y),(63,y)],fill=(121,135,149,255) if robe else '#598494')
 if not robe:
  for x in [4,12,20,28,36,44,52]:d.line([(x,0),(x,100)],fill='#1a2c3d')
 overlay=Image.new('RGBA',(128,128));od=ImageDraw.Draw(overlay)
 # Gold trim UV region; base stays neutral and undyed decoration is a distinct texture layer.
 od.rectangle((64,0,127,30),fill='#bb985f')
 for y in range(0,31,3):od.line([(64,y),(127,y)],fill='#f0d198' if y%6==0 else '#6f5845')
 od.rectangle((80,32,96,45),fill='#183b4c');od.rectangle((81,33,87,39),outline='#ddb773')
 glyph=Image.open(a/'textures/glyph/p.png').convert('RGBA');glyph.thumbnail((4,5),Image.Resampling.LANCZOS)
 tint=Image.new('RGBA',glyph.size,'#74eada');tint.putalpha(glyph.getchannel('A'));overlay.alpha_composite(tint,(82,34))
 for layer in [1,2]:
  if robe:
   tex.save(afolder/f'mage_robes_layer_{layer}.png');overlay.save(afolder/f'mage_robes_layer_{layer}_overlay.png')
  else:
   plate=tex.copy();plate.alpha_composite(overlay);plate.save(afolder/f'arcane_plate_layer_{layer}.png')
for name in ['mana_flow','mana_restore']:
 p=a/'textures/mob_effect'/f'{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im);d.ellipse((1,1,16,16),fill='#183f50',outline='#d9b874');d.line([(10,3),(6,9),(10,9),(7,15)],fill='#80f6e4',width=2);im.save(p)
# Correct singular Minecraft 1.21 data/recipe directory.
recipes=data/'kelmagic/recipe';recipes.mkdir(parents=True,exist_ok=True)
def shaped(name,pattern,key,count=1):
 obj={'type':'minecraft:crafting_shaped','category':'equipment' if any(v in name for v in ['helmet','chestplate','leggings','boots']) else 'misc','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':'kelmagic:'+name,'count':count}}
 (recipes/f'{name}.json').write_text(json.dumps(obj,indent=2)+'\n')
shaped('mana_core',['LRL','RDR','LRL'],{'L':'minecraft:lapis_lazuli','R':'minecraft:redstone','D':'minecraft:diamond'})
shaped('magic_ingot',['III','ICI','III'],{'I':'minecraft:iron_ingot','C':'kelmagic:mana_core'},8)
shaped('magic_cloth',['LSL','SCS','LSL'],{'L':'minecraft:leather','S':'minecraft:string','C':'kelmagic:mana_core'},8)
for prefix,material in [('arcane','magic_ingot'),('mage','magic_cloth')]:
 for name,pattern in [('helmet',['MMM','M M']),('chestplate',['M M','MMM','MMM']),('leggings',['MMM','M M','M M']),('boots',['M M','M M'])]:shaped(prefix+'_'+name,pattern,{'M':'kelmagic:'+material})
shaped('dragon_heart',['NCA','HES','ACN'],{'N':'minecraft:netherite_ingot','C':'minecraft:end_crystal','A':'minecraft:amethyst_block','H':'minecraft:heart_of_the_sea','E':'minecraft:dragon_egg','S':'minecraft:nether_star'})
shaped('scripter',['IGI','BCB','IRI'],{'I':'kelmagic:magic_ingot','G':'minecraft:gold_ingot','B':'minecraft:book','C':'kelmagic:mana_core','R':'minecraft:redstone_block'})
dye=data/'minecraft/tags/item/dyeable.json';dye.parent.mkdir(parents=True,exist_ok=True);dye.write_text(json.dumps({'replace':False,'values':['kelmagic:'+n for n in items if n.startswith('mage')]},indent=2)+'\n')
translations={'mana_core':('Ядро маны','Mana Core'),'magic_ingot':('Магический слиток','Arcane Ingot'),'magic_cloth':('Магическая ткань','Magic Cloth'),'dragon_heart':('Сердце дракона','Dragon Heart'),'scripter':('Скриптер','Spell Scripter')}
for n in items:
 if n.startswith('arcane'):translations[n]=({'helmet':'Шлем','chestplate':'Кираса','leggings':'Поножи','boots':'Сапоги'}[n.split('_')[-1]]+' магической брони','Arcane '+n.split('_')[-1].title())
 if n.startswith('mage'):translations[n]=({'helmet':'Капюшон мага','chestplate':'Одеяние мага','leggings':'Поножи мага','boots':'Сапоги мага'}[n.split('_')[-1]],'Mage '+n.split('_')[-1].title())
extra={
 'itemGroup.kelmagic':('Кэл — магия','Kel Magic'),
 'effect.kelmagic.mana_flow':('Поток маны','Mana Flow'), 'effect.kelmagic.mana_restore':('Восстановление маны','Mana Restoration'),
 'item.minecraft.potion.effect.mana':('Бутылка маны','Bottled Mana'), 'item.minecraft.splash_potion.effect.mana':('Взрывная мана','Splash Mana'), 'item.minecraft.lingering_potion.effect.mana':('Туман маны','Lingering Mana'), 'item.minecraft.tipped_arrow.effect.mana':('Стрела маны','Arrow of Mana'),
 'kelmagic.equipment_bonus':('+%s к максимуму маны','+%s maximum mana'),
 'kelmagic.heart_already':('Сердце дракона уже пробуждено. Предмет не расходуется.','Your dragon heart is already awakened. Item not consumed.'),
 'kelmagic.heart_used':('Сердце дракона пробуждено: базовая мана 15, восстановление раз в 5 секунд.','Dragon heart awakened: base mana 15, regeneration every 5 seconds.'),
 'kelmagic.scripter_invalid':('Подготовьте определённое заклинание в чате, затем запишите его ПКМ.','Prepare a defined spell in chat, then right-click to record it.'),
 'kelmagic.scripter_saved':('Записано %s слов. Цена применения: %s маны.','Recorded %s words. Cast cost: %s mana.'),
 'kelmagic.scripter_blocked':('Скриптер пуст, запись недопустима или вы уже готовите заклинание.','Scripter empty, invalid spell, or another spell is being prepared.'),
 'kelmagic.scripter_controls':('ПКМ — запись; Shift+ПКМ — применение.','Right-click: record; Shift+right-click: cast.'),
 'kelmagic.scripter_cost':('Стоимость: %s маны (половина, с округлением вверх)','Cost: %s mana (half, rounded up)')
}
for locale,idx in [('ru_ru',0),('en_us',1)]:
 p=a/'lang'/f'{locale}.json';obj=json.loads(p.read_text());obj.update({'item.kelmagic.'+k:v[idx] for k,v in translations.items()});obj.update({k:v[idx] for k,v in extra.items()});p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
# Contact sheet for pixel-art QA.
sheet=Image.new('RGB',(512,190),'#101c29');sd=ImageDraw.Draw(sheet)
for i,n in enumerate(items):
 x=12+(i%7)*72;y=12+(i//7)*88;im=Image.open(out/(n+'.png')).convert('RGBA')
 if n.startswith('mage'):im.alpha_composite(Image.open(out/(n+'_overlay.png')))
 im=im.resize((64,64),Image.Resampling.NEAREST);sheet.paste(im,(x,y),im);sd.text((x,y+67),n[:11],fill='#d5b986')
sheet.save(r/'art/generated/stage2-icons.png')
print('13 original 32x32 icons, two articulated armor texture sets, 13 recipes, dye tag and RU/EN resources generated.')
