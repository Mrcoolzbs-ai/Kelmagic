from pathlib import Path
from PIL import Image,ImageDraw
import json,random,math,copy,zipfile
r=Path('src/main/resources');a=r/'assets/kelmagic';d=r/'data/kelmagic';t=a/'textures/block';models=a/'models/block'
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n')
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
# Turf uses fine, irregular blade clusters, not a stretched plank/noise image.
rng=random.Random(1132)
im=Image.open(z.open('assets/minecraft/textures/block/grass_block_top.png')).convert('RGBA')
# Native pixel-art blade patches, with restrained luminance range for the moving dye.
im.putdata([(int(150+p[0]*.36),)*3+(255,) for p in im.getdata()])
im.save(t/'magic_grass_wave.png')
fallback=im.copy();fallback.putdata([tuple(int(c*k/255) for c,k in zip((36,48,94),p[:3]))+(255,) for p in im.getdata()]);fallback.save(t/'magic_grass_top.png')
dirt=Image.open(z.open('assets/minecraft/textures/block/dirt.png')).convert('RGBA');overlay=Image.new('RGBA',(16,16));draw=ImageDraw.Draw(overlay)
for x in range(16):
 depth=rng.choice([2,2,3,3,4,5]);
 for y in range(depth):
  v=rng.randrange(170,230);overlay.putpixel((x,y),(v,v,v,255));dirt.putpixel((x,y),(int(v*36/255),int(v*48/255),int(v*94/255),255))
overlay.save(t/'magic_grass_overlay.png');dirt.save(t/'magic_grass_side.png')
# Renderer UV spans 6 pixels of a full-size side texture: keep jagged turf fringe, transparent dirt.
# side renderer 6 pixels high -> remap V=0..1 to six rows in separate atlas.
overlay.crop((0,0,16,6)).resize((16,16),Image.Resampling.NEAREST).save(t/'magic_grass_overlay.png')
# Snow remains a vanilla snowy turf model (the coloured renderer does not draw over solid snow).
write(a/'blockstates/magic_grass_block.json',{'variants':{'snowy=false':{'model':'kelmagic:block/magic_grass_block'},'snowy=true':{'model':'minecraft:block/grass_block_snow'}}})
# Purpose-built frame grain. Rails use geometry-proportional UVs, no full 16px plank crammed into a 2px rail.
for name,base in [('dream_frame',(23,33,52)),('dream_endgrain',(29,42,62))]:
 im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(16):
   n=rng.randrange(-3,4)+(3 if y in (2,8,13) and x%7<5 else 0);dr.point((x,y),fill=tuple(c+n for c in base)+(255,))
 if name.endswith('endgrain'):
  for k in [2,5]:dr.rectangle((k,k,15-k,15-k),outline=(38+k,49+k,68+k))
 im.save(t/f'{name}.png')
im=Image.new('RGBA',(16,16),(45,87,103,255));dr=ImageDraw.Draw(im);dr.rectangle((1,1,14,14),outline=(81,142,155),width=2);dr.rectangle((4,4,11,11),fill=(130,205,200));dr.rectangle((6,6,9,9),fill=(198,237,215));im.save(t/'dream_mana.png')
def box(lo,hi,tex):
 dx,dy,dz=[hi[i]-lo[i] for i in range(3)];faces={}
 for face,w,h in [('up',dx,dz),('down',dx,dz),('north',dx,dy),('south',dx,dy),('east',dz,dy),('west',dz,dy)]:faces[face]={'uv':[0,0,min(16,w),min(16,h)],'texture':'#'+tex}
 return {'from':lo,'to':hi,'faces':faces}
for half in ['head','foot']:
 # Separate rails, mattress and endboards; no shared exterior planes.
 els=[box([0,3,0],[2,5,16],'wood'),box([14,3,0],[16,5,16],'wood')]
 start=2.02 if half=='head' else 0;end=16 if half=='head' else 13.98
 els.append(box([2,4.9,start],[14,8.8,end],'blanket'))
 zz=0 if half=='head' else 14
 for x in [0,14]:els.append(box([x,0,zz],[x+2,6,zz+2],'wood'))
 if half=='head':els.extend([box([0,4,0],[16,10,2],'wood'),box([3,8.81,3],[13,9.8,7],'pillow')])
 else:
  els.append(box([0,4,14],[16,8,16],'wood'))
  # Front-only gem floats 0.02 model pixels ahead, no coplanar or internal faces.
  els.append({'from':[6,5,16.02],'to':[10,7,16.04],'faces':{'south':{'uv':[0,0,16,16],'texture':'#mana'}}})
 write(models/f'otherworld_dream_bed_{half}.json',{'textures':{'particle':'kelmagic:block/dream_frame','wood':'kelmagic:block/dream_frame','blanket':'kelmagic:block/dream_blanket','pillow':'kelmagic:block/dream_pillow','mana':'kelmagic:block/dream_mana'},'elements':els})
head=json.loads((models/'otherworld_dream_bed_head.json').read_text());foot=json.loads((models/'otherworld_dream_bed_foot.json').read_text());whole=copy.deepcopy(head)
for el in foot['elements']:
 el=copy.deepcopy(el)
 for k in ['from','to']:el[k][2]+=16
 whole['elements'].append(el)
for el in whole['elements']:
 for k in ['from','to']:el[k][2]-=8
old=json.loads((a/'models/item/otherworld_dream_bed.json').read_text());whole['display']=old['display'];write(a/'models/item/otherworld_dream_bed.json',whole)
# Leaf clusters with deliberate sky holes, binary cutout works on Fast and Fancy.
for name,gold in [('midnight_leaves',False),('golden_midnight_leaves',True)]:
 im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im)
 for cx,cy in [(1,2),(7,1),(12,4),(4,8),(10,10),(1,13),(14,14),(7,15)]:
  for dx,dy in [(-1,0),(0,0),(1,0),(0,-1),(0,1),(1,1)]:
   x=(cx+dx)%16;y=(cy+dy)%16;n=rng.randrange(-5,7);base=(141,118,51) if gold else (26,43,70);dr.point((x,y),fill=tuple(c+n for c in base)+(255,))
 # Sparse connected twigs, never the formerly near-opaque square.
 for x,y in [(3,4),(4,5),(5,6),(8,4),(9,5),(11,7),(5,11),(6,12)]:dr.point((x,y),fill=(68,72,45,255) if gold else (22,30,46,255))
 im.save(t/f'{name}.png')
for name,src in [('natural_midnight_leaves','midnight_leaves'),('natural_golden_midnight_leaves','golden_midnight_leaves')]:
 write(a/f'blockstates/{name}.json',{'variants':{'':{'model':'kelmagic:block/'+src}}});write(a/f'models/item/{name}.json',{'parent':'kelmagic:block/'+src})
 loot=json.loads((d/f'loot_table/blocks/{src}.json').read_text());loot['pools'][0]['entries'][0]['children'][0]['name']='kelmagic:'+name
 loot['pools'].append({'rolls':1,'conditions':copy.deepcopy(loot['pools'][1]['conditions']),'entries':[{'type':'minecraft:item','name':'kelmagic:magic_sapling','conditions':[{'condition':'minecraft:survives_explosion'},{'condition':'minecraft:table_bonus','enchantment':'minecraft:fortune','chances':[.05,.0625,.083333336,.1]}]}]});write(d/f'loot_table/blocks/{name}.json',loot)
# Canonical 1.21 tag directory; do not silently rely on obsolete tags/blocks aliases.
for ns,tag,values in [('minecraft','dirt',['kelmagic:magic_grass_block']),('minecraft','leaves',['kelmagic:'+n for n in ['midnight_leaves','golden_midnight_leaves','natural_midnight_leaves','natural_golden_midnight_leaves']]),('minecraft','logs',['kelmagic:midnight_log','kelmagic:midnight_wood','kelmagic:stripped_midnight_log','kelmagic:stripped_midnight_wood']),('minecraft','mineable/hoe',['kelmagic:natural_midnight_leaves','kelmagic:natural_golden_midnight_leaves']),('minecraft','mineable/shovel',['kelmagic:magic_grass_block'])]:
 p=r/f'data/{ns}/tags/block/{tag}.json';j=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};j['values']=list(dict.fromkeys(j['values']+values));write(p,j)
write(d/'worldgen/configured_feature/natural_midnight_tree.json',{'type':'kelmagic:natural_midnight_tree','config':{}})
biome=json.loads(z.read('data/minecraft/worldgen/biome/forest.json')) if 'data/minecraft/worldgen/biome/forest.json' in z.namelist() else {'has_precipitation':True,'temperature':.6,'downfall':.8,'effects':{'sky_color':7972607,'fog_color':12638463,'water_color':4159204,'water_fog_color':329011},'spawners':{},'spawn_costs':{},'carvers':{},'features':[[] for _ in range(11)]}
biome['effects'].update(grass_color=0x46518a,foliage_color=0x263755,water_color=0x425b96);write(d/'worldgen/biome/magical_grove.json',biome)
# Distinct animated threshold, 16 frames in a 4x4 atlas. White luminosity is dyed per owner pair.
im=Image.new('RGBA',(128,256))
for f in range(16):
 for x in range(32):
  for y in range(64):
   edge=min(x,31-x,y/2,(63-y)/2);angle=math.atan2(y/2-15.5,x-15.5);rad=math.hypot(x-15.5,y/2-15.5)
   wave=(math.sin(rad*.72+angle*2-f*math.pi/8)+1)/2;v=int(90+140*wave);alpha=240 if edge<1.5 else 155+int(60*wave)
   if edge<1.5:v=245
   im.putpixel((f%4*32+x,f//4*64+y),(v,v,v,alpha))
p=a/'textures/entity/threshold_portal.png';p.parent.mkdir(exist_ok=True);im.save(p)
# New rites are sealed research nodes, creative/admin can test, no survival unlock.
p=d/'research/catalog.json';j=json.loads(p.read_text());old=next(n for n in j['nodes'] if n['id']=='rite:end_gate');j['nodes']=[n for n in j['nodes'] if n['id'] not in ['rite:threshold_link','rite:grove_awakening']]
for id,title,x,y,example in [('threshold_link','Связь двух порогов',300,350,'Первое произнесение оставляет порог, второе связывает его с новым. Чужой голос не завершит мою пару. Между мирами расстояние исчезает, но любой удар разрывает оба конца.'),('grove_awakening','Пробуждение рощи',500,350,'Мне нужны живые образцы былых лет. На равнинах и в лесах они способны пробудить почву и вернуть сумеречные кроны. Листья такой рощи снова дают потомство.')]:
 n=copy.deepcopy(old);n.update(id='rite:'+id,x=x,y=y,sealed=True,example=example,lore=title,thought='Это высказывание ещё не стало доступным знанием.',steps=[]);j['nodes'].append(n)
write(p,j)
# Audit: retain all old advancements, add progress for R9-R11 objects/actions.
adv={
'midnight_wood':('kelmagic:midnight_log','Сумрак в ладонях','Получите сумеречную древесину.','Midnight in your hands','Obtain twilight wood.'),
'living_fiber':('kelmagic:living_fiber','Живое волокно','Получите живое волокно.','Living fibre','Obtain living fibre.'),
'magic_turf':('kelmagic:magic_grass_block','Память почвы','Получите магический дёрн.','Memory of the soil','Obtain magic turf.'),
'dream_bed':('kelmagic:otherworld_dream_bed','За гранью снов','Создайте кровать потусторонних снов.','Beyond dreams','Obtain a bed of otherworldly dreams.'),
'grove_leaves':('kelmagic:natural_midnight_leaves','Лес помнит продолжение','Получите плодородную листву магической рощи.','The forest remembers','Collect fertile leaves from an awakened grove.'),
'archive_storage':('kelmagic:archivist_cabinet','Каждой вещи — место','Получите шкаф архивиста.','A place for everything','Obtain an archivist cabinet.'),
'rite_threshold_link':('kelmagic:manuscript_dor','Первый порог','Сотворите порог между мирами.','First threshold','Create a threshold.'),
'threshold_pair':('kelmagic:manuscript_dor','Два порога — один путь','Свяжите два своих портала.','Two thresholds, one path','Link two of your portals.'),
'threshold_travel':('kelmagic:manuscript_lok','Шаг сквозь расстояние','Пройдите через связанный порог.','A step across distance','Travel through a linked threshold.'),
'threshold_dimensions':('minecraft:ender_pearl','Мирам не разлучить','Пройдите сквозь порог в другое измерение.','Worlds cannot part us','Cross dimensions through a threshold.'),
'threshold_break':('minecraft:wooden_sword','Оборванный путь','Разрушьте портал ударом.','Severed path','Break a threshold with a hit.'),
'rite_grove_awakening':('kelmagic:magic_sapling','Память былых лет','Завершите пробуждение магической рощи.','Memory of ancient years','Complete a grove awakening.')}
for id,(icon,ru,desc,en,edesc) in adv.items():
 write(d/f'advancement/{id}.json',{'parent':'kelmagic:root','display':{'icon':{'id':icon},'title':{'translate':'advancements.kelmagic.'+id+'.title'},'description':{'translate':'advancements.kelmagic.'+id+'.description'},'frame':'challenge' if id.startswith('rite_') or id=='threshold_dimensions' else 'task','show_toast':True,'announce_to_chat':True,'hidden':id.startswith('rite_')},'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']]})
for lang in ['ru_ru','en_us']:
 p=a/f'lang/{lang}.json';j=json.loads(p.read_text())
 for id,(_,ru,desc,en,edesc) in adv.items():j['advancements.kelmagic.'+id+'.title']=ru if lang=='ru_ru' else en;j['advancements.kelmagic.'+id+'.description']=desc if lang=='ru_ru' else edesc
 for key,ru,en in [('natural_midnight_leaves','Плодородная сумеречная листва','Fertile Twilight Leaves'),('natural_golden_midnight_leaves','Плодородная золотая листва','Fertile Golden Leaves')]:j['block.kelmagic.'+key]=ru if lang=='ru_ru' else en
 j['biome.kelmagic.magical_grove']='Магическая роща' if lang=='ru_ru' else 'Magical Grove';j['entity.kelmagic.threshold_portal']='Связанный порог' if lang=='ru_ru' else 'Linked Threshold'
 j['rite.kelmagic.threshold_link']='Связь двух порогов' if lang=='ru_ru' else 'Bond of Two Thresholds';j['rite.kelmagic.grove_awakening']='Пробуждение рощи' if lang=='ru_ru' else 'Awakening of the Grove'
 write(p,j)
