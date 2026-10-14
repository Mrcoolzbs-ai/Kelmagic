from pathlib import Path
from PIL import Image
import json,zipfile
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic';checks=0
def ck(v,label):
 global checks
 assert v,label
 checks+=1
for p in R.rglob('*.json'):json.loads(p.read_text());checks+=1
words=json.loads((R/'data/kelmagic/magic/rarities.json').read_text())['words'];ck(len(words)==180 and words['pir']=='epic','word rarity')
for p in (R/'data/kelmagic/advancement').glob('*.json'):
 d=json.loads(p.read_text());ck(d['display']['show_toast'],'toast');ck(d['display']['announce_to_chat']==(p.stem!='root'),'chat '+p.stem);ck(d.get('requirements')==[['done']],'requirements')
ck(len(list((R/'data/kelmagic/advancement').glob('*.json')))==33,'33 achievements')
ck(Image.open(A/'textures/block/magic_torch.png').size==(16,16),'native torch UV')
ck(Image.open(A/'textures/block/magic_lantern.png').size==(16,80),'native 5frame lantern');ck((A/'textures/block/magic_lantern.png.mcmeta').exists(),'animation')
ck(json.loads((A/'models/block/magic_torch.json').read_text())['parent']=='minecraft:block/template_torch','torch geometry')
ck(json.loads((A/'models/block/magic_wall_torch.json').read_text())['parent']=='minecraft:block/template_torch_wall','wall geometry')
ck(json.loads((A/'blockstates/temporary_end_gate.json').read_text())['variants']['']['model']=='minecraft:block/end_portal','native portal')
for b in ['magic_torch','magic_lantern','magic_wall_torch']:
 ck(not (R/f'data/kelmagic/loot_table/blocks/{b}.json').exists(),'no drop '+b);ck(not (R/f'data/kelmagic/recipe/{b}.json').exists(),'no recipe '+b)
files=list((A/'textures/trims').rglob('*.png'));ck(len(files)==1440,'18x10x8trim textures')
for p in files:
 im=Image.open(p);ck(im.size==(256,256),'UV '+str(p));ck(im.getbbox() is not None,'visible pattern '+str(p))
for prefix in ['mage','arcane']:
 for slot in ['helmet','chestplate','leggings','boots']:
  d=json.loads((A/f'models/item/{prefix}_{slot}.json').read_text());ck(len(d['overrides'])==180,'pattern/material item mapping')
  for i,v in enumerate(d['overrides']):
   ck(v['predicate']['kelmagic:trimmed']==i+1,'predicate');model=json.loads((A/('models/'+v['model'].split(':')[1]+'.json')).read_text());ck('layer2' in model['textures'],'trim layer')
for biome in ['plains','desert','taiga','snow','savanna','jungle','swamp']:
 p=A/f'textures/entity/villager/profession/archivist_{biome}.png';im=Image.open(p);ck(im.size==(64,64),'villager UV');ck(im.crop((24,0,30,8)).getbbox() is None,'nose transparent');ck(im.crop((30,48,64,64)).getbbox() is None,'hat rim transparent');ck(im.crop((20,44,28,64)).getbbox() is not None,'jacket back');ck(json.loads(Path(str(p)+'.mcmeta').read_text())['villager']['hat']=='full','profession hat prevents biome cap')
ck(json.loads((A/'models/item/archivist_table.json').read_text())['display']['firstperson_righthand']['scale']==[.4]*3,'standard held block scale')
print(f'RESOURCES PASSED: {checks} checks; 1440 equipped pattern/material/slot maps,33achievements, native UV/animation/geometry')
