"""1.10 resource overlay. Reads local vanilla assets for matching UV/palettes."""
from pathlib import Path
from PIL import Image,ImageDraw
import zipfile,json,io,re,math,colorsys
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic';z=zipfile.ZipFile(P/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
def vanilla(n):return Image.open(io.BytesIO(z.read('assets/minecraft/textures/'+n+'.png'))).convert('RGBA')
def save(im,n):p=A/'textures'/n;p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def js(n,d):p=A/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2))
# The original native torch model expects only two texture columns at x7..8, not a giant card.
torch=vanilla('block/torch');px=torch.load()
for y in range(16):
 for x in range(16):
  r,g,b,a=px[x,y]
  if a and y<9:
   v=max(r,g,b)/255;px[x,y]=(int(160+80*v),int(105+90*v),int(220+35*v),a)
save(torch,'block/magic_torch.png')
js('models/block/magic_torch.json',{'parent':'minecraft:block/template_torch','textures':{'torch':'kelmagic:block/magic_torch'}})
js('models/block/magic_wall_torch.json',{'parent':'minecraft:block/template_torch_wall','textures':{'torch':'kelmagic:block/magic_torch'}})
js('blockstates/magic_wall_torch.json',{'variants':{f'facing={face}':dict(model='kelmagic:block/magic_wall_torch',y=y) for face,y in [('east',0),('south',90),('west',180),('north',270)]}})
js('models/item/magic_torch.json',{'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:block/magic_torch'}})
js('models/item/magic_lantern.json',{'parent':'kelmagic:block/magic_lantern'})
lantern=vanilla('block/sea_lantern');px=lantern.load()
for y in range(lantern.height):
 for x in range(16):
  r,g,b,a=px[x,y];v=(r+g+b)/765;px[x,y]=(round(65+185*v),round(40+175*v),round(100+155*v),a)
save(lantern,'block/magic_lantern.png');(A/'textures/block/magic_lantern.png.mcmeta').write_bytes(z.read('assets/minecraft/textures/block/sea_lantern.png.mcmeta'))
js('models/block/magic_lantern.json',{'parent':'minecraft:block/cube_all','textures':{'all':'kelmagic:block/magic_lantern'}})
# Standard block transforms inherited by custom block geometry.
p=A/'models/block/archivist_table.json';d=json.loads(p.read_text());d['parent']='minecraft:block/block';p.write_text(json.dumps(d,indent=2))
js('models/item/archivist_table.json',{'parent':'kelmagic:block/archivist_table','display':{'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.375]*3},'thirdperson_lefthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.375]*3},'firstperson_righthand':{'rotation':[0,45,0],'translation':[0,0,0],'scale':[.4]*3},'firstperson_lefthand':{'rotation':[0,225,0],'translation':[0,0,0],'scale':[.4]*3},'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625]*3},'ground':{'translation':[0,3,0],'scale':[.25]*3},'fixed':{'rotation':[0,0,0],'scale':[.5]*3}}})
# Native elytra wing surface, violet translucent feathers; folded/flight geometry is vanilla ElytraModel.
wings=vanilla('entity/elytra');px=wings.load()
for y in range(wings.height):
 for x in range(wings.width):
  r,g,b,a=px[x,y]
  if a:
   v=(r+g+b)/765;px[x,y]=(round(100+100*v),round(70+110*v),round(170+85*v),210)
save(wings,'entity/spectral_wings.png')
# Real villager UV net: keep nose/hands/face transparent; hat rim UV30,47 must be blank.
colors={'plains':(58,85,83),'desert':(169,117,67),'taiga':(58,89,65),'snow':(105,145,160),'savanna':(163,72,45),'jungle':(58,99,75),'swamp':(84,66,112)}
def faces(u,v,w,h,d):return {'top':(u+d,v,u+d+w,v+d),'bottom':(u+d+w,v,u+d+w*2,v+d),'right':(u,v+d,u+d,v+d+h),'front':(u+d,v+d,u+d+w,v+d+h),'left':(u+d+w,v+d,u+2*d+w,v+d+h),'back':(u+2*d+w,v+d,u+2*d+2*w,v+d+h)}
for biome,color in colors.items():
 im=Image.new('RGBA',(64,64));draw=ImageDraw.Draw(im);gold=(193,165,102,255);cloth=(*color,255)
 # jacket opaque all faces and correct continuity, 20px down from shoulders
 for name,rect in faces(0,38,8,20,6).items():
  draw.rectangle((rect[0],rect[1],rect[2]-1,rect[3]-1),fill=cloth)
  if name in ['front','back','left','right']:
   draw.rectangle((rect[0],rect[1]+10,rect[2]-1,rect[1]+11),fill=gold)
   draw.line((rect[0],rect[3]-1,rect[2]-1,rect[3]-1),fill=gold)
 draw.rectangle((6,44,13,46),fill=(213,198,155,255));draw.rectangle((9,48,10,52),fill=gold);draw.rectangle((7,57,9,59),fill=(28,46,46,255));draw.rectangle((8,58,9,58),fill=(234,219,178,255))
 # underlying torso same cloth. Folded arms: sleeves only upper6px, skin remains native.
 for rect in faces(16,20,8,12,6).values():draw.rectangle((rect[0],rect[1],rect[2]-1,rect[3]-1),fill=cloth)
 for name,rect in faces(44,22,4,8,4).items():
  if name not in ['top','bottom']:draw.rectangle((rect[0],rect[1],rect[2]-1,rect[1]+4),fill=cloth);draw.line((rect[0],rect[1]+4,rect[2]-1,rect[1]+4),fill=gold)
 # no painting bridge40,38: hands and overlap stay clean
 for rect in faces(0,22,4,12,4).values():draw.rectangle((rect[0],rect[1],rect[2]-1,rect[3]-1),fill=cloth)
 # cap mapped only to vanilla outer hat; no nose/base-head/rim opacity
 for name,rect in faces(32,0,8,10,8).items():
  if name=='top':draw.rectangle((rect[0],rect[1],rect[2]-1,rect[3]-1),fill=cloth)
  elif name in ['front','back','left','right']:
   draw.rectangle((rect[0],rect[1],rect[2]-1,rect[1]+2),fill=cloth);draw.line((rect[0],rect[1]+3,rect[2]-1,rect[1]+3),fill=gold)
 save(im,f'entity/villager/profession/archivist_{biome}.png');(A/f'textures/entity/villager/profession/archivist_{biome}.png.mcmeta').write_text('{"villager":{"hat":"full"}}')
# default alias cache and snow alias
for alias,source in [('archivist','archivist_plains'),('archivist_snowy','archivist_snow')]:
 for ext in ['.png','.png.mcmeta']:(A/f'textures/entity/villager/profession/{alias}{ext}').write_bytes((A/f'textures/entity/villager/profession/{source}{ext}').read_bytes())
# Remap actual vanilla 18 patterns and exact 8-color material palettes onto custom garment UV.
patterns=['sentry','dune','coast','wild','ward','eye','vex','tide','snout','rib','spire','wayfinder','shaper','silence','raiser','host','flow','bolt']
materials=['quartz','iron','netherite','redstone','copper','gold','emerald','diamond','lapis','amethyst']
palette=list(vanilla('trims/color_palettes/trim_palette').getdata())
def colorize(im,material):
 mp=dict(zip(palette,list(vanilla('trims/color_palettes/'+material).getdata())));out=im.copy();out.putdata([mp.get(c,c) if c[3] else c for c in im.getdata()]);return out
code=(P/'src/main/java/dev/kelmagic/client/MagicArmorModels.java').read_text()
parts={}
for robe,prefix in [('true','mage'),('false','arcane')]:
 section=code.split('if(robe=='+robe+')')[1].split('  if(robe==')[0]
 for slot in ['HEAD','CHEST','LEGS','FEET']:
  region=section.split('if(slot==EquipmentSlot.'+slot+'){')[1].split('   }')[0];entries=[]
  for line in region.splitlines():
   m=re.search(r'piece\((\w+),"([^"]+)",(\d+),(\d+),(.+)\);',line)
   if not m:continue
   bone,name,u,v,values=m.groups();vals=[float(x.strip().replace('f','')) for x in values.split(',')];w,h,d=[math.ceil(x) for x in vals[3:6]]
   # Ornaments/straps remain original: trim goes on continuous garment surfaces.
   if name not in ['coat','mantle','sleeve','bell_cuff','crown','left_cheek','right_cheek','nape','hood_tail','tail_tip','trouser','trouser_waist','boot','front-1','front1','rear-1','rear1','side-1','side1','sternum','rib-1','rib1','spine_plate','hip_plate-1','hip_plate1','greave','lamella0','lamella1','lamella2','bracer','toe_plate']:continue
   native=(0,0,8,8,8) if bone=='head' else (16,16,8,12,4) if bone=='body' else (40,16,4,12,4) if 'arm' in bone else (0,16,4,12,4)
   entries.append((int(u),int(v),w,h,d,native,name))
  parts[prefix,slot.lower()]=entries
for pattern in patterns:
 for material in materials:
  sources=[colorize(vanilla('trims/models/armor/'+pattern+suffix),material) for suffix in ['', '_leggings']]
  for (prefix,slot),entries in parts.items():
   out=Image.new('RGBA',(256,256));src=sources[slot=='legs']
   for u,v,w,h,d,native,name in entries:
    target=faces(u,v,w,h,d);original=faces(*native)
    for face,rect in target.items():
     tile=src.crop(original[face]);tile=tile.resize((rect[2]-rect[0],rect[3]-rect[1]),Image.Resampling.NEAREST);out.paste(tile,rect[:2])
   save(out,f'trims/{prefix}_{slot}/{pattern}_{material}.png')
# Inventory: pattern-specific vanilla item motifs, exact material palette. Predicate encodes pattern+material.
for prefix in ['arcane','mage']:
 for armor in ['helmet','chestplate','leggings','boots']:
  key=prefix+'_'+armor;model={'parent':'minecraft:item/generated','textures':{'layer0':'kelmagic:item/'+key}};model['overrides']=[]
  if prefix=='mage':model['textures']['layer1']='kelmagic:item/'+key+'_overlay'
  for pidx,pattern in enumerate(patterns):
   # vanilla inventory shapes are deliberately template-independent; real pattern lives on equipped armor.
   for midx,material in enumerate(materials):
    trim=colorize(vanilla('trims/items/'+armor+'_trim'),material);save(trim,f'item/vanilla_trims/{armor}_{material}.png')
    name=key+'_'+pattern+'_'+material;tex=model['textures'].copy();tex['layer2']='kelmagic:item/vanilla_trims/'+armor+'_'+material
    js('models/item/'+name+'.json',{'parent':'minecraft:item/generated','textures':tex});model['overrides'].append({'predicate':{'kelmagic:trimmed':1+pidx*10+midx},'model':'kelmagic:item/'+name})
  js('models/item/'+key+'.json',model)
# Status icon
im=Image.new('RGBA',(18,18));dr=ImageDraw.Draw(im);dr.polygon([(9,1),(15,7),(9,16),(3,7)],fill=(159,109,238,255));dr.line([(3,10),(6,8),(9,10),(12,8),(15,10)],fill=(149,241,239,255),width=2);save(im,'mob_effect/mana_tide.png')
for lang in ['ru_ru','en_us']:
 p=A/f'lang/{lang}.json';d=json.loads(p.read_text());ru=lang=='ru_ru'
 d.update({'effect.kelmagic.mana_tide':'Прилив маны' if ru else 'Mana tide','block.kelmagic.magic_torch':'Магический факел' if ru else 'Magic torch','block.kelmagic.magic_wall_torch':'Магический настенный факел' if ru else 'Magic wall torch','block.kelmagic.magic_lantern':'Магический светильник' if ru else 'Magic lantern','commands.kelmagic.updated':'Обновлено игроков: %s' if ru else 'Updated players: %s','commands.kelmagic.cast':'Заклинание %s исполнено игроком %s' if ru else 'Rite %s cast by %s','commands.kelmagic.unknown_rite':'Неизвестное великое заклинание.' if ru else 'Unknown grand rite.','commands.kelmagic.word_limit':'В заклинании должно быть не более 100 слов.' if ru else 'Rites may not exceed 100 words.','commands.kelmagic.archive_not_found':'В известных началах деревень в радиусе %s чанков архив не найден. Новые чанки команда не генерирует.' if ru else 'No archive in known village starts within %s chunks. No new chunks generated.','commands.kelmagic.archive_found':'Архив: %s %s %s (нажмите для команды телепортации)' if ru else 'Archive: %s %s %s (click for teleport suggestion)','commands.kelmagic.help':'/kelmagic admin mana current <игроки> <число> | mana maximum <игроки> <число/reset> | heart <игроки> <true/false> | status <игрок> | cast <игрок> <rite> [muk] | locate archive [чанки]. Требуются права оператора 2.' if ru else '/kelmagic admin mana current <players> <value> | mana maximum <players> <value/reset> | heart <players> <true/false> | status <player> | cast <player> <rite> [muk] | locate archive [chunks]. Requires operator level 2.'})
 p.write_text(json.dumps(d,ensure_ascii=False,indent=2))
# keep the word effect hint truthful
p=R/'data/kelmagic/magic/effects.json';d=json.loads(p.read_text());print('effects structure',type(d).__name__)
print('1.10: torch16px, lantern5frame, villager7fullUV, 1440equipped trim resources + vanilla inventory palettes')
