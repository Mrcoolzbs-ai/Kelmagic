from pathlib import Path
import json,zipfile
from PIL import Image
from nbt19 import read
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';d=r/'src/main/resources/data';checks=0
def ck(c,m):
 global checks
 assert c,m;checks+=1
for p in (r/'src/main/resources').rglob('*.json'):json.loads(p.read_text());checks+=1
v=json.loads((d/'kelmagic/magic/rarities.json').read_text())['words'];words=json.loads((d/'kelmagic/magic/words.json').read_text())['words'];ck(set(v)=={w['word'] for w in words},'all individual words');ck(v['vak']=='common' and v['riv']=='rare' and v['mar']=='epic','three water tiers')
for k,t in v.items():ck(t in ['common','uncommon','rare','epic'],'valid tier '+k)
ck(not (d/'kelmagic/recipe/add_manuscript.json').exists() and not (d/'kelmagic/recipe/copy_codex.json').exists(),'table-exclusive operations')
for n in ['magic_torch','magic_lantern','temporary_end_gate']:
 ck(not (d/f'kelmagic/recipe/{n}.json').exists(),'no recipe '+n);ck(not (d/f'kelmagic/loot_table/blocks/{n}.json').exists(),'no drop '+n);ck(Image.open(a/f'textures/block/{n}.png').size==(16,16),'light texture '+n)
for biome in ['plains','desert','taiga','snow','savanna','jungle','swamp']:ck(Image.open(a/f'textures/entity/villager/profession/archivist_{biome}.png').size==(64,64),'profession outfit '+biome)
for p in (a/'models').rglob('*.json'):
 vv=json.loads(p.read_text())
 for tex in vv.get('textures',{}).values():
  if tex.startswith('kelmagic:'):ck((a/'textures'/(tex.split(':')[1]+'.png')).exists(),'model texture '+str(p))
adv=list((d/'kelmagic/advancement').glob('*.json'));ck(len(adv)==32,'32 achievements')
for p in adv:
 vv=json.loads(p.read_text());ck(vv['requirements']==[['done']],'requirements '+p.stem)
 for lang in ['ru_ru','en_us']:
  l=json.loads((a/f'lang/{lang}.json').read_text())
  for k in ['title','description']:ck(vv['display'][k]['translate'] in l,'achievement locale '+k)
loot=json.loads((d/'kelmagic/loot_table/gameplay/golden_wool_barter.json').read_text());ck(all(e['name']!='kelmagic:golden_thread' for e in loot['pools'][0]['entries']),'no thread barter')
ck(len(json.loads((d/'minecraft/tags/item/trimmable_armor.json').read_text())['values'])==8,'all magic garments trimmable')
ck('kelmagic:archivist' in json.loads((d/'minecraft/tags/point_of_interest_type/acquirable_job_site.json').read_text())['values'],'villager job discovery')
z=zipfile.ZipFile(r/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar')
for biome in ['plains','desert','taiga','snowy','savanna']:
 pool=json.loads((d/f'minecraft/worldgen/template_pool/village/{biome}/houses.json').read_text());original=json.loads(z.read(f'data/minecraft/worldgen/template_pool/village/{biome}/houses.json'));ck(len(pool['elements'])==len(original['elements'])+2,'two archive choices '+biome)
 for old,new in zip(original['elements'],pool['elements']):ck(old['element']==new['element'] and new['weight']==old['weight']*8,'vanilla preserved '+biome)
 for index in [1,2]:
  nbt=read((d/f'kelmagic/structure/archive/{biome}_{index}.nbt').read_bytes());palette=nbt['palette'][1][1];blocks=nbt['blocks'][1][1];ck(any(p['Name'][1]=='kelmagic:archivist_table' for p in palette),'table in archive');ck(any(p['Name'][1]=='minecraft:bookshelf' for p in palette),'bookshelves');ck(any(p['Name'][1]=='minecraft:jigsaw' for p in palette),'village entrance')
  for b in blocks:
   ck(0<=b['state'][1]<len(palette),'palette index');ck(all(0<=p<size for p,size in zip(b['pos'][1][1],nbt['size'][1][1])),'block within structure')
  chests=[b for b in blocks if palette[b['state'][1]]['Name'][1]=='minecraft:chest'];ck(len(chests)==1 and chests[0]['nbt'][1]['LootTable'][1]==f'kelmagic:chests/archive_{biome}','correct archive chest')
print('REVISION 1.9 RESOURCE CHECKS PASSED:',checks)
