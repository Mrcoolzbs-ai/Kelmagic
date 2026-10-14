"""15 reproducible village archives: three architectural/interior layouts per biome."""
from pathlib import Path
import json,zipfile
from nbt19 import write
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources/data';z=zipfile.ZipFile(P/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
styles={'plains':('oak_planks','oak_log','oak_stairs','cobblestone','oak_slab'),'desert':('cut_sandstone','smooth_sandstone','sandstone_stairs','sandstone','smooth_sandstone_slab'),'taiga':('spruce_planks','spruce_log','spruce_stairs','mossy_cobblestone','spruce_slab'),'snowy':('spruce_planks','stripped_spruce_log','spruce_stairs','stone_bricks','spruce_slab'),'savanna':('acacia_planks','acacia_log','acacia_stairs','terracotta','acacia_slab')}
def js(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
for biome,(planks,log,stairs,stone,slab) in styles.items():
 for variant in range(1,4):
  w,d=[(11,13),(9,17),(15,11)][variant-1];offset=1;maxroof=6+w//2;height=maxroof+2;palette=[];blocks={};doorx=w//2 if variant<3 else 5
  def block(x,y,zz,name,props=None,nbt=None):
   state={'Name':(8,name if ':' in name else 'minecraft:'+name)}
   if props:state['Properties']=(10,{k:(8,v) for k,v in props.items()})
   if state not in palette:palette.append(state)
   b={'pos':(9,(3,[x+offset,y,zz+offset])),'state':(3,palette.index(state))}
   if nbt:b['nbt']=(10,nbt)
   blocks[x,y,zz]=b
  stair=lambda facing:{'facing':facing,'half':'bottom','shape':'straight','waterlogged':'false'}
  for x in range(w):
   for zz in range(d):
    block(x,0,zz,stone if x in [0,w-1] or zz in [0,d-1] else planks)
    for y in range(1,5):block(x,y,zz,'air')
    if x in [0,w-1] or zz in [0,d-1]:
     for y in range(1,5):block(x,y,zz,log if x in [0,w-1] and zz in [0,d-1] else planks)
  # Windows, real doors and full-block threshold (the jigsaw is replaced with floor).
  for zz in range(3,d-2,4):
   for x in [0,w-1]:
    for y in [2,3]:block(x,y,zz,'glass_pane',{'north':'true','south':'true','east':'false','west':'false','waterlogged':'false'})
  for x in [doorx-2,doorx+2]:
   if 0<x<w-1:block(x,2,0,'glass_pane',{'north':'false','south':'false','east':'true','west':'true','waterlogged':'false'})
  door='oak_door' if biome=='desert' else planks.replace('planks','door')
  for y,half in [(1,'lower'),(2,'upper')]:block(doorx,y,0,door,{'facing':'north','half':half,'hinge':'left','open':'false','powered':'false'})
  block(doorx,0,0,'jigsaw',{'orientation':'north_up'},{'id':(8,'minecraft:jigsaw'),'name':(8,'minecraft:building_entrance'),'target':(8,'minecraft:building_entrance'),'pool':(8,f'minecraft:village/{biome}/streets'),'joint':(8,'aligned'),'final_state':(8,'minecraft:'+stone)})
  if biome!='desert':
   # Gable with stair slopes, slab ridge and one-block eaves; no plank pyramid.
   for x in range(-1,w+1):
    rise=min(x+1,w-x);y=5+rise
    for zz in range(-1,d+1):
     if x==w//2:block(x,y,zz,slab,{'type':'bottom','waterlogged':'false'})
     else:block(x,y,zz,stairs,stair('east' if x<w//2 else 'west'))
    if 0<=x<w:
     for yy in range(5,y):
      for zz in [0,d-1]:block(x,yy,zz,planks)
  else:
   # Desert archive: flat sandstone terrace, parapet and variable skylight.
   for x in range(w):
    for zz in range(d):
     block(x,5,zz,'smooth_sandstone')
     if x in [0,w-1] or zz in [0,d-1]:block(x,6,zz,'sandstone_wall',{'east':'none','north':'none','south':'none','west':'none','up':'true','waterlogged':'false'})
   for x in range(doorx-1,doorx+2):
    for zz in range(3,5):block(x,5,zz,'glass')
  # Layout 1: reading hall; layout 2: long double stacks; layout 3: hall + enclosed study.
  if variant==1:
   for zz in range(2,d-1):
    for y in [1,2,3]:block(1,y,zz,'bookshelf')
   for x in range(2,w-2):
    for y in [1,2]:block(x,y,d-2,'bookshelf')
   for zz in [3,6,9]:block(w-3,1,zz,stairs,stair('west'))
   tables=[(w//2-1,d-3)]
  elif variant==2:
   for zz in range(2,d-2):
    for x in [1,w-2]:
     for y in [1,2,3]:block(x,y,zz,'bookshelf')
   for zz in [4,8,12]:block(w//2+1,1,zz,stairs,stair('west'))
   tables=[(w//2-1,d-3)]
  else:
   for zz in range(1,d-1):
    for y in [1,2,3,4]:block(9,y,zz,planks)
   for y,half in [(1,'lower'),(2,'upper')]:block(9,y,5,door,{'facing':'west','half':half,'hinge':'left','open':'false','powered':'false'})
   for x in range(1,w-1):
    for y in [1,2,3]:block(x,y,d-2,'bookshelf')
   for zz in range(2,d-3):
    for y in [1,2,3]:block(1,y,zz,'bookshelf')
   for zz in [3,6]:block(7,1,zz,stairs,stair('west'))
   block(12,1,3,stairs,stair('south'));tables=[(4,d-3),(11,d-3)]
  for x,zz in tables:block(x,1,zz,'kelmagic:archivist_table')
  for zz in [3,d-4]:
   if biome!='desert':
    for x in range(w):block(x,5,zz,log,{'axis':'x'})
   block(doorx,4,zz,'lantern',{'hanging':'true','waterlogged':'false'})
  block(w-3,1,d-3,'chest',{'facing':'north','type':'single','waterlogged':'false'},{'id':(8,'minecraft:chest'),'LootTable':(8,f'kelmagic:chests/archive_{biome}')})
  decoration={'plains':'potted_oxeye_daisy','desert':'potted_cactus','taiga':'potted_fern','snowy':'potted_spruce_sapling','savanna':'potted_acacia_sapling'}[biome]
  block(w-3,1,2,planks);block(w-3,2,2,decoration)
  entities=[{'pos':(9,(6,[doorx+1.5,1.,d-3.5])),'blockPos':(9,(3,[doorx+1,1,d-4])),'nbt':(10,{'id':(8,'minecraft:villager'),'VillagerData':(10,{'type':(8,'minecraft:snow' if biome=='snowy' else 'minecraft:'+biome),'profession':(8,'minecraft:none'),'level':(3,1)})})}]
  root={'DataVersion':(3,3955),'size':(9,(3,[w+2,height,d+2])),'palette':(9,(10,palette)),'blocks':(9,(10,list(blocks.values()))),'entities':(9,(10,entities))}
  p=R/f'kelmagic/structure/archive/{biome}_{variant}.nbt';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(write(root))
 pool=json.loads(z.read(f'data/minecraft/worldgen/template_pool/village/{biome}/houses.json'))
 # More frequent archives: three entries weight3 vs unscaled vanilla weights.
 # Vanilla house weights remain unscaled.
 for variant in range(1,4):pool['elements'].append({'weight':3,'element':{'element_type':'minecraft:single_pool_element','location':f'kelmagic:archive/{biome}_{variant}','processors':{'processors':[]},'projection':'rigid'}})
 js(R/f'minecraft/worldgen/template_pool/village/{biome}/houses.json',pool)
print('15 archives generated: full thresholds, wooden stair roofs, 3 distinct layouts per village biome')
