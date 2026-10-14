from pathlib import Path
from PIL import Image,ImageDraw
import math,json
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic'
def save(path,data):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
# A crystalline, slowly breathing reservoir. 32 seamless frames, 4 s cycle.
tex=Image.new('RGBA',(32,32*32));
for f in range(32):
 pulse=(1-math.cos(f*math.tau/32))/2
 for y in range(32):
  for x in range(32):
   edge=min(x,y,31-x,31-y);r=math.hypot(x-15.5,y-15.5);facet=((x//4+y//4)%3)*7
   val=max(0,1-r/20)*pulse;bright=(edge in [1,2] or (abs(x-15.5)+abs(y-15.5)<10 and abs((x+y)%8-4)<1.5));k=22 if bright else 0
   tex.putpixel((x,f*32+y),(int(18+20*val+facet/3+k),int(55+95*val+facet+k),int(65+90*val+facet+k),255))
p=A/'textures/block/mana_block.png';p.parent.mkdir(parents=True,exist_ok=True);tex.save(p);Path(str(p)+'.mcmeta').write_text(json.dumps({'animation':{'frametime':3,'interpolate':True}}))
# Metal plate: nine wells arranged as an eight-point ring, not a 3x3 workbench.
im=Image.new('RGBA',(64,64),(115,73,46));dr=ImageDraw.Draw(im);dr.rectangle((1,1,62,62),outline=(196,147,91),width=2);dr.rectangle((4,4,59,59),outline=(60,76,79),width=2)
coords=[(32,32)]+[(32+math.sin(i*math.pi/4)*19,32-math.cos(i*math.pi/4)*19) for i in range(8)]
for i,(x,y) in enumerate(coords):
 dr.ellipse((x-6,y-6,x+6,y+6),fill=(22,37,43),outline=(206,175,107) if i==0 else (136,159,166),width=2);dr.ellipse((x-2,y-2,x+2,y+2),fill=(66,137,138) if i==0 else (83,65,116))
for i in range(8):
 x,y=coords[i+1];dr.line((x+(32-x)*.36,y+(32-y)*.36,32+(x-32)*.29,32+(y-32)*.29),fill=(86,122,123),width=1)
im.save(A/'textures/block/resonance_top.png')
# Fixed body, resources stay ordinary Minecraft textures except the authored top and reservoir.
textures={'stone':'minecraft:block/smooth_stone','wood':'minecraft:block/stripped_dark_oak_log','iron':'minecraft:block/iron_block','copper':'minecraft:block/copper_block','top':'kelmagic:block/resonance_top','mana':'kelmagic:block/mana_block','purple':'minecraft:block/amethyst_block','particle':'kelmagic:block/resonance_top'};elements=[]
def box(fr,to,t,up=None):
 faces={s:{'texture':'#'+(up if s=='up' and up else t),'uv':[0,0,16,16]} for s in ['north','south','east','west','up','down']};elements.append({'from':fr,'to':to,'faces':faces})
box([0,0,0],[16,2,16],'stone');box([1,2,1],[15,10,15],'wood');box([0,10,0],[16,12,16],'iron');box([0,12,0],[16,14,16],'copper','top')
for x in [1,13]:
 for z in [1,13]:box([x,2,z],[x+2,10,z+2],'iron')
for a,b in [([5,3,.9],[11,8,1.1]),([5,3,14.9],[11,8,15.1]),([.9,3,5],[1.1,8,11]),([14.9,3,5],[15.1,8,11])]:box(a,b,'mana')
save('assets/kelmagic/models/block/resonance_station.json',{'textures':textures,'elements':elements});save('assets/kelmagic/models/block/mana_block.json',{'parent':'minecraft:block/cube_all','textures':{'all':'kelmagic:block/mana_block'}})
for name in ['mana_block','resonance_station']:
 save(f'assets/kelmagic/blockstates/{name}.json',{'variants':{'':{'model':'kelmagic:block/'+name}}});save(f'assets/kelmagic/models/item/{name}.json',{'parent':'kelmagic:block/'+name});save(f'data/kelmagic/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'kelmagic:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
save('data/kelmagic/recipe/mana_block.json',{'type':'minecraft:crafting_shaped','category':'building','pattern':['MMM','MMM','MMM'],'key':{'M':{'item':'kelmagic:mana_core'}},'result':{'id':'kelmagic:mana_block','count':1}})
save('data/kelmagic/recipe/mana_core_from_block.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':'kelmagic:mana_block'}],'result':{'id':'kelmagic:mana_core','count':9}})
save('data/kelmagic/recipe/resonance_station.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['IAI','LML','SBC'],'key':{'I':{'item':'minecraft:iron_block'},'A':{'item':'minecraft:amethyst_block'},'L':{'tag':'minecraft:logs'},'C':{'item':'minecraft:copper_block'},'B':{'item':'minecraft:ink_sac'},'S':{'item':'minecraft:smooth_stone'},'M':{'item':'kelmagic:mana_block'}},'result':{'id':'kelmagic:resonance_station','count':1}})
for loc in ['en_us','ru_ru']:
 p=A/f'lang/{loc}.json';d=json.loads(p.read_text());d['block.kelmagic.mana_block']='Блок маны' if loc=='ru_ru' else 'Mana Block';d['block.kelmagic.resonance_station']='Станция наполнения резонансом' if loc=='ru_ru' else 'Resonance Infusion Station';p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
# Lift parchment only; retain the outer cover and the footer for fixed controls.
im=Image.open(A/'textures/gui/codex.png').convert('RGBA');original=im.copy();dr=ImageDraw.Draw(im);dr.rectangle((14,22,345,201),fill=original.getpixel((40,20)));im.paste(original.crop((14,30,346,194)),(14,22));im.save(A/'textures/gui/codex_entry.png')
# Correct tool tags: pickaxes for both blocks.
p=R/'data/minecraft/tags/block/mineable/pickaxe.json';d=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
for id in ['kelmagic:mana_block','kelmagic:resonance_station']:
 if id not in d['values']:d['values'].append(id)
p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,indent=2))
print('Generated animated reservoir, 9-well station, recipes, loot, lifted entry parchment')
