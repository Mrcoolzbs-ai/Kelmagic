from pathlib import Path
import json,zipfile,copy,random
from PIL import Image,ImageDraw
r=Path('src/main/resources');a=r/'assets/kelmagic';data=r/'data/kelmagic';t=a/'textures/block';models=a/'models/block'
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
write(data/'worldgen/configured_feature/midnight_tree.json',{'type':'kelmagic:midnight_tree','config':{}})
# Remove sapling entries outright, not just set their chance to zero. Preserve shears, sticks, and golden fruit.
for name in ['midnight_leaves','golden_midnight_leaves']:
 p=data/f'loot_table/blocks/{name}.json';j=json.loads(p.read_text());j['pools'][0]['entries'][0]['children']=[c for c in j['pools'][0]['entries'][0]['children'] if c.get('name')!='kelmagic:magic_sapling'];write(p,j)
# Explicit, physically matching thickness faces instead of cropping the ornament into the thin edges.
for name,vertical in [('midnight_edge_vertical',True),('midnight_edge_horizontal',False)]:
 im=Image.new('RGBA',(16,16));pix=im.load();bands=[(17,25,42,255),(48,66,85,255),(17,25,42,255)]
 for x in range(16):
  for y in range(16):pix[x,y]=bands[(x if vertical else y)%3]
 im.save(t/f'{name}.png')
z=zipfile.ZipFile('/data/KelMagic/build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
for p in models.glob('midnight_door_*.json'):
 old=json.loads(p.read_text());parent=old.get('parent','').split('/')[-1]
 if not parent.startswith('door_'):continue
 j=json.loads(z.read('assets/minecraft/models/block/'+parent+'.json'));j['textures'].update(old['textures']);j['textures']['edge']='kelmagic:block/midnight_edge_vertical'
 for el in j['elements']:
  for face,desc in el['faces'].items():
   if face not in ['east','west']:desc.update(texture='#edge',uv=[0,0,3,16]);desc.pop('rotation',None)
 write(p,j)
for suffix in ['bottom','top','open']:
 j=json.loads(z.read(f'assets/minecraft/models/block/template_trapdoor_{suffix}.json'));j['textures'].update(texture='kelmagic:block/midnight_trapdoor',edge_h='kelmagic:block/midnight_edge_horizontal',edge_v='kelmagic:block/midnight_edge_vertical')
 for el in j['elements']:
  for face,desc in el['faces'].items():
   narrow=face not in (['north','south'] if suffix=='open' else ['up','down'])
   if narrow:
    vertical=suffix=='open' and face in ['east','west'];desc.update(texture='#edge_v' if vertical else '#edge_h',uv=[0,0,3,16] if vertical else [0,0,16,3]);desc.pop('rotation',None)
 write(models/f'midnight_trapdoor_{suffix}.json',j)
# Bed: midnight wooden frame, warm woven golden cover, ivory pillow, a small mana jewel in the footboard.
rng=random.Random(310)
for name,base,noise in [('dream_blanket',(172,137,65),12),('dream_pillow',(224,215,187),5),('magic_grass_wave',(233,233,233),19),('magic_grass_side',(32,36,54),9)]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 for x in range(16):
  for y in range(16):
   n=rng.randint(-noise,noise);color=tuple(max(0,min(255,v+n)) for v in base)+(255,);d.point((x,y),fill=color)
 if name=='dream_blanket':
  for x in [1,14]:d.line((x,0,x,15),fill=(227,202,120));
  for y in [1,14]:d.line((0,y,15,y),fill=(227,202,120))
  d.line((6,7,8,5,10,7,8,10,6,7),fill=(236,212,132),width=1)
 im.save(t/f'{name}.png')
# Side soil uses vanilla dirt below a stationary dark cap; dynamic renderer lays the coloured rim over it.
dirt=Image.open(z.open('assets/minecraft/textures/block/dirt.png')).convert('RGBA');im=dirt.copy();d=ImageDraw.Draw(im);d.rectangle((0,0,15,2),fill=(18,30,66));im.save(t/'magic_grass_side.png')
write(models/'magic_grass_block.json',{'parent':'minecraft:block/cube','textures':{'particle':'kelmagic:block/magic_grass_side','up':'kelmagic:block/magic_grass_top','down':'minecraft:block/dirt',**{f:'kelmagic:block/magic_grass_side' for f in ['north','south','east','west']}}})
# A static dark-blue top is also the far-distance fallback, not a white square when BER is culled.
im=Image.open(t/'magic_grass_wave.png').convert('RGBA');im.putdata([tuple(int(c*k/255) for c,k in zip((18,30,66),p[:3]))+(255,) for p in im.getdata()]);im.save(t/'magic_grass_top.png')
write(a/'blockstates/magic_grass_block.json',{'variants':{'':{'model':'kelmagic:block/magic_grass_block'}}});write(a/'models/item/magic_grass_block.json',{'parent':'kelmagic:block/magic_grass_block'})
def box(lo,hi,tex):return {'from':lo,'to':hi,'faces':{f:{'uv':[0,0,16,16],'texture':'#'+tex} for f in ['down','up','north','south','west','east']}}
for half in ['head','foot']:
 els=[box([0,3,0],[16,5,16],'wood'),box([1,5,0],[15,9,16],'blanket')]
 # only the far legs of each half, so there is no extra pair in the middle of the bed
 zz=1 if half=='head' else 13
 for x in [1,13]:els.append(box([x,0,zz],[x+2,4,zz+2],'wood'))
 if half=='head':els.extend([box([0,4,0],[16,10,2],'wood'),box([2,9,3],[14,10,8],'pillow')])
 else:els.extend([box([0,4,14],[16,8,16],'wood'),box([6,5,16.01],[10,7,16.03],'mana')])
 write(models/f'otherworld_dream_bed_{half}.json',{'textures':{'particle':'kelmagic:block/midnight_planks','wood':'kelmagic:block/midnight_planks','blanket':'kelmagic:block/dream_blanket','pillow':'kelmagic:block/dream_pillow','mana':'kelmagic:block/archive_magic'},'elements':els})
variants={}
for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
 for half in ['head','foot']:
  for occupied in [False,True]:variants[f'facing={facing},occupied={str(occupied).lower()},part={half}']={'model':f'kelmagic:block/otherworld_dream_bed_{half}','y':angle}
write(a/'blockstates/otherworld_dream_bed.json',{'variants':variants})
# Inventory miniature is a real model of both halves, not only half a bed.
head=json.loads((models/'otherworld_dream_bed_head.json').read_text());foot=json.loads((models/'otherworld_dream_bed_foot.json').read_text())
whole=copy.deepcopy(head)
for el in foot['elements']:
 el=copy.deepcopy(el)
 for k in ['from','to']:el[k][2]+=16
 whole['elements'].append(el)
for el in whole['elements']:
 for k in ['from','to']:el[k][2]-=8
whole['display']={'gui':{'rotation':[30,45,0],'translation':[0,1,-3],'scale':[.45,.45,.45]},'ground':{'translation':[0,3,0],'scale':[.3,.3,.3]},'fixed':{'rotation':[0,90,0],'scale':[.5,.5,.5]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2,0],'scale':[.3,.3,.3]},'firstperson_righthand':{'rotation':[0,45,0],'translation':[0,0,0],'scale':[.4,.4,.4]}}
write(a/'models/item/otherworld_dream_bed.json',whole)
write(data/'recipe/otherworld_dream_bed.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['WWW','PMP'],'key':{'W':{'item':'kelmagic:golden_wool'},'P':{'item':'kelmagic:midnight_planks'},'M':{'item':'kelmagic:mana_block'}},'result':{'id':'kelmagic:otherworld_dream_bed','count':1}})
write(data/'recipe/magic_grass_block.json',{'type':'minecraft:crafting_shapeless','category':'building','ingredients':[{'item':'minecraft:grass_block'},{'item':'kelmagic:living_fiber'}],'result':{'id':'kelmagic:magic_grass_block','count':1}})
for name in ['otherworld_dream_bed','magic_grass_block']:
 conditions=[{'condition':'minecraft:block_state_property','block':'kelmagic:'+name,'properties':{'part':'head'}}] if name.endswith('bed') else []
 write(data/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':conditions,'entries':[{'type':'minecraft:item','name':'kelmagic:'+name}],'functions':[{'function':'minecraft:explosion_decay'}]}]})
for tag,items in [('minecraft:blocks/beds',['kelmagic:otherworld_dream_bed']),('minecraft:blocks/mineable/axe',['kelmagic:otherworld_dream_bed']),('minecraft:blocks/mineable/shovel',['kelmagic:magic_grass_block']),('minecraft:blocks/dirt',['kelmagic:magic_grass_block'])]:
 ns,key=tag.split(':');p=r/f'data/{ns}/tags/{key}.json';j=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};j['values']+=items;write(p,j)
for lang,names in [('ru_ru',['Кровать потусторонних снов','Магическая трава']),('en_us',['Bed of Otherworldly Dreams','Magic Grass Block'])]:
 p=a/f'lang/{lang}.json';j=json.loads(p.read_text());j['block.kelmagic.otherworld_dream_bed']=names[0];j['block.kelmagic.magic_grass_block']=names[1];write(p,j)
