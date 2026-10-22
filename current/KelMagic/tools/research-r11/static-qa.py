from pathlib import Path
import json,math
from PIL import Image
r=Path('src/main/resources');a=r/'assets/kelmagic';d=r/'data/kelmagic';checks=0
def ok(b,m):
 global checks
 checks+=1
 assert b,m
for p in r.rglob('*.json'):json.loads(p.read_text());checks+=1
cat=json.loads((d/'research/catalog.json').read_text());ids=[n['id'] for n in cat['nodes']];ok(len(ids)==len(set(ids)),'research unique')
for id in ['threshold_link','grove_awakening']:ok(next(n for n in cat['nodes'] if n['id']=='rite:'+id)['sealed'],'survival sealed '+id)
for lang in ['ru_ru','en_us']:
 j=json.loads((a/f'lang/{lang}.json').read_text())
 for p in (d/'advancement').glob('*.json'):
  adv=json.loads(p.read_text())
  for part in ['title','description']:
   tr=adv.get('display',{}).get(part,{}).get('translate');ok(tr is None or tr in j,f'{lang} advancement translation {p.name} {part}')
for name in ['midnight_leaves','golden_midnight_leaves']:
 im=Image.open(a/f'textures/block/{name}.png').convert('RGBA');alpha=sum(p[3]>0 for p in im.getdata())/(im.width*im.height);ok(.15<alpha<.5,'leaf cutout holes '+name);print(name,'opaque fraction',alpha)
for name in ['natural_midnight_leaves','natural_golden_midnight_leaves']:
 loot=(d/f'loot_table/blocks/{name}.json').read_text();ok('kelmagic:magic_sapling' in loot,'fertile leaf loot')
for name in ['midnight_leaves','golden_midnight_leaves']:ok('kelmagic:magic_sapling' not in (d/f'loot_table/blocks/{name}.json').read_text(),'barren leaf loot')
for half in ['head','foot']:
 j=json.loads((a/f'models/block/otherworld_dream_bed_{half}.json').read_text());m=next(e for e in j['elements'] if any(f['texture']=='#blanket' for f in e['faces'].values()));ok(m['from'][2]>2 if half=='head' else m['to'][2]<14,'mattress does not overlap endboard')
 for e in j['elements']:
  ok(all(e['from'][i]<e['to'][i] for i in range(3)),'positive bed cuboid')
  if any(f['texture']=='#wood' for f in e['faces'].values()):
   for f in e['faces'].values():ok(f['uv'][3]<=16,'proportional wood UV')
ok('kelmagic:magic_grass_block' in json.loads((r/'data/minecraft/tags/block/dirt.json').read_text())['values'],'1.21 dirt tag supports plants')
print('R11_STATIC_COMPLETE checks='+str(checks))
