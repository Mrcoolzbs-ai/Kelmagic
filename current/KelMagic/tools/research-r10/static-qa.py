from pathlib import Path
import json,re,zipfile,hashlib
root=Path('.');r=root/'src/main/resources';checks=0
def ok(b,label):
 global checks
 checks+=1
 if not b:raise AssertionError(label)
for p in r.rglob('*.json'):json.loads(p.read_text());checks+=1
for name in ['midnight_leaves','golden_midnight_leaves']:
 s=(r/f'data/kelmagic/loot_table/blocks/{name}.json').read_text();ok('magic_sapling' not in s,'no sapling loot '+name);ok('shears' in s,'shears '+name)
recipe=json.loads((r/'data/kelmagic/recipe/otherworld_dream_bed.json').read_text());ok(recipe['pattern']==['WWW','PMP'],'bed pattern');ok({k:v['item'] for k,v in recipe['key'].items()}=={'W':'kelmagic:golden_wool','P':'kelmagic:midnight_planks','M':'kelmagic:mana_block'},'exact bed ingredients')
for p in (r/'assets/kelmagic/models').rglob('*.json'):
 j=json.loads(p.read_text())
 for el in j.get('elements',[]):
  for key in ['from','to']:ok(all(-16<=v<=32 for v in el[key]),str(p)+' model bounds')
for p in (r/'assets/kelmagic/models/block').glob('midnight_door_*.json'):
 j=json.loads(p.read_text());ok('elements' in j,'explicit door edge '+str(p))
 for el in j['elements']:
  for f,d in el['faces'].items():
   if f not in ['east','west']:ok(d['texture']=='#edge','door thin edge')
c=json.loads((r/'data/kelmagic/research/catalog.json').read_text());lang=json.loads((r/'assets/kelmagic/lang/ru_ru.json').read_text())
for n in c['nodes']:
 for key in ['thought','example']:ok(not re.search(r'[0-9%]|\bnov\b|NBT|Curios|клиент|сервер',n.get(key,'')),'ordinary prose '+n['id'])
 ok(not re.search(r'[0-9%]|\bnov\b|NBT|Curios|клиент|сервер',lang.get(n.get('lore',''),'')),'ordinary lore '+n['id'])
# Baseline diff is optional; pass the delivered R9 ZIP as argv[1].
import sys
report={'result':'PASS','static_checks':checks}
if len(sys.argv)>1:
 z=zipfile.ZipFile(sys.argv[1]);changed=[];same=0;new=[];protected=0
 for p in (root/'src/main/java').rglob('*.java'):
  key=next((n for n in z.namelist() if n.endswith('/'+p.as_posix())),None)
  if key is None:new.append(str(p));continue
  eq=z.read(key)==p.read_bytes()
  if eq:same+=1
  else:changed.append(str(p))
  if '/face/' in str(p) or '/hair/' in str(p) or '/speech/' in str(p) or p.name in ['KelRenderer.java','KelModel.java']:
   ok(eq,'protected '+str(p));protected+=1
 report.update(unchanged_java=same,changed_java=changed,new_java=new,protected_unchanged=protected,static_checks=checks)
Path('reports/static-r10.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n');print(json.dumps(report,ensure_ascii=False))
