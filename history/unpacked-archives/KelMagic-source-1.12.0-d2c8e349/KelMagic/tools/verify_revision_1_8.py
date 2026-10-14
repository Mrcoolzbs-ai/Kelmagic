from pathlib import Path
import runpy,json,re
from PIL import Image
r=Path(__file__).resolve().parents[1]
runpy.run_path(str(r/'tools/verify_revision_1_7.py'),run_name='__main__')
a=r/'src/main/resources/assets/kelmagic';d=r/'src/main/resources/data/kelmagic';checks=0
def ck(c,msg):
 global checks
 assert c,msg;checks+=1
for p in (r/'src/main/resources').rglob('*.json'):
 v=json.loads(p.read_text());ck(isinstance(v,(dict,list)),str(p))
for p in (a/'models').rglob('*.json'):
 v=json.loads(p.read_text())
 for name in v.get('textures',{}).values():
  if name.startswith('kelmagic:'):ck((a/'textures'/ (name.split(':')[1]+'.png')).exists(),'texture '+name)
 for o in v.get('overrides',[]):
  name=o['model']
  if name.startswith('kelmagic:'):ck((a/'models'/ (name.split(':')[1]+'.json')).exists(),'override '+name)
for name in ['golden_thread','tape_casing','tape_trim_recorded']+[f'tape_pattern_{i}' for i in range(1,10)]:ck(Image.open(a/f'textures/item/{name}.png').size==(32,32),'icon '+name)
ck(Image.open(a/'textures/block/golden_wool.png').size==(16,16),'wool texture')
rec=json.loads((d/'recipe/golden_thread.json').read_text());ck(rec['pattern']==['SSS','SGS','SSS'] and rec['result']['count']==8,'thread recipe');ck(rec['key']['G']['item']=='minecraft:gold_ingot' and rec['key']['S']['item']=='minecraft:string','thread ingredients')
rec=json.loads((d/'recipe/golden_wool.json').read_text());ck(rec['pattern']==['TT','TT'] and rec['result']['count']==1,'wool recipe');ck(rec['key']['T']['item']=='kelmagic:golden_thread','wool ingredient')
rec=json.loads((d/'recipe/magic_cloth.json').read_text());ck(rec['key']['S']['item']=='kelmagic:golden_thread' and rec['result']['count']==8,'cloth new ingredient')
ck(json.loads((d/'recipe/decorate_tape.json').read_text())['type']=='kelmagic:decorate_tape','custom decorate serializer')
adv=list((d/'advancement').glob('*.json'));ck(len(adv)==18,'18 advancements')
for p in adv:
 v=json.loads(p.read_text());ck(v['requirements']==[['done']] and v['criteria']['done']['trigger']=='minecraft:impossible','explicit impossible criterion '+p.stem);ck(v['display']['hidden']==(p.stem in {'complete_codex','overload_101'}),'secret visibility '+p.stem)
 if 'parent' in v:ck((d/'advancement'/(v['parent'].split(':')[1]+'.json')).exists(),'parent exists '+p.stem)
 for lang in ['ru_ru','en_us']:
  l=json.loads((a/f'lang/{lang}.json').read_text())
  for key in ['title','description']:ck(v['display'][key]['translate'] in l,'adv localization '+p.stem+lang+key)
for lang in ['ru_ru','en_us']:
 l=json.loads((a/f'lang/{lang}.json').read_text());ck('30%' not in l['kelmagic.tape.cost'],'short price tooltip')
 for key in ['item.kelmagic.golden_thread','block.kelmagic.golden_wool','kelmagic.tape.pattern']:ck(bool(l.get(key)),'material localization '+key)
loot=json.loads((d/'loot_table/gameplay/golden_wool_barter.json').read_text());ck(loot['type']=='minecraft:barter','barter context')
cat=json.loads((r/'docs/kel-language/CATALOG-1.8.0.json').read_text());ck(bool(cat),'new catalog')
print('REVISION 1.8 ADDITIONAL RESOURCE CHECKS PASSED:',checks)
