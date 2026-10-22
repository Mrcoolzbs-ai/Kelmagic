from pathlib import Path
from PIL import Image
import json,zipfile,io,itertools,hashlib,subprocess
r=Path('src/main/resources');a=r/'assets/kelmagic';d=r/'data/kelmagic';checks=0
out=[]
def ok(b,m):
 global checks
 checks+=1
 assert b,m
for p in r.rglob('*.json'):json.loads(p.read_text());checks+=1
adv={p.stem:json.loads(p.read_text()) for p in (d/'advancement').glob('*.json')}
for id in ['grove_leaves','threshold_travel','living_fiber','rite_threshold_link']:ok(id not in adv,'removed '+id)
for id,j in adv.items():
 par=j.get('parent','');ok(not par.startswith('kelmagic:') or par.split(':')[1] in adv,'parent '+id)
for id in ['threshold_pair','rite_grove_awakening']:
 j=adv[id];ok(j['parent']=='kelmagic:combination' and j['display']['frame']=='challenge','great rite branch '+id)
 for lang in ['ru_ru','en_us']:
  tr=json.loads((a/f'lang/{lang}.json').read_text());ok(tr[j['display']['description']['translate']]==tr[adv['rite_wings']['display']['description']['translate']],'same great rite description')
for id,par in [('magic_turf','rite_grove_awakening'),('archive_storage','midnight_wood'),('word_born_power','archive_table')]:ok(adv[id]['parent']=='kelmagic:'+par,'progression '+id)
for lang in ['ru_ru','en_us']:
 tr=json.loads((a/f'lang/{lang}.json').read_text())
 for id,j in adv.items():
  for k in ['title','description']:
   key=j.get('display',{}).get(k,{}).get('translate');ok(key is None or key in tr,'translated advancement '+id)
cat=json.loads((d/'research/catalog.json').read_text());ids=[j['id'] for j in cat['nodes']];ok(len(ids)==len(set(ids)),'unique research')
for id in ['threshold_link','grove_awakening']:
 j=next(j for j in cat['nodes'] if j['id']=='rite:'+id);ok(j['sealed'],'survival sealed '+id);ok(abs(j['x'])<=130 and abs(j['y'])<=55,'readable map coordinate')
z=zipfile.ZipFile('/data/.agent-service/files/25d82891-88e1-4fdc-b8c5-976788611130/KelMagic_R10_Dreams_TRANSFER.zip')
for name in ['midnight_leaves','golden_midnight_leaves']:
 suffix=f'src/main/resources/assets/kelmagic/textures/block/{name}.png';old=Image.open(io.BytesIO(z.read(next(p for p in z.namelist() if p.endswith(suffix))))).convert('RGBA');im=Image.open(a/f'textures/block/{name}.png').convert('RGBA');n=sum(p[3]>0 for p in old.getdata());now=sum(p[3]>0 for p in im.getdata());ok(.1<=(n-now)/n<=.15,'10-15 percent transparency');ok(all(x==y or (x[:3]==y[:3] and y[3]==0) for x,y in zip(old.getdata(),im.getdata())),'preserved original leaf pixels');out.append(f'{name}: {n} -> {now} opaque pixels; extra holes={(n-now)/n:.3%}')
for half in ['head','foot']:
 j=json.loads((a/f'models/block/otherworld_dream_bed_{half}.json').read_text())
 for e in j['elements']:ok(all(e['to'][i]>e['from'][i] for i in range(3)),'positive bed cuboid')
 for x,y in itertools.combinations(j['elements'],2):ok(not all(min(x['to'][i],y['to'][i])>max(x['from'][i],y['from'][i])+1e-6 for i in range(3)),'no intersecting bed cuboids '+half)
sounds=json.loads((a/'sounds.json').read_text());hashes=[]
for name in ['open','close','travel','hum']:
 p=a/f'sounds/threshold/{name}.ogg';hashes.append(hashlib.sha256(p.read_bytes()).hexdigest());ok('threshold_'+name in sounds,'sound registry json');subprocess.run(['ffmpeg','-v','error','-i',str(p),'-f','null','-'],check=True,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE);checks+=1
ok(len(set(hashes))==4,'four original distinct sounds')
z=zipfile.ZipFile('/data/.agent-service/files/bb426c57-1970-4ad9-bf0a-ff93dd2a127d/KelMagic_R11_Grove_TRANSFER.zip');protected=0
for p in Path('src').rglob('*'):
 if not p.is_file():continue
 if not any(k in p.name.lower() for k in ['face','hair','speech']):continue
 matches=[n for n in z.namelist() if n.endswith('/project/'+str(p))]
 if matches:ok(p.read_bytes()==z.read(matches[0]),'protected file unchanged '+str(p));protected+=1
out+=['advancements='+str(len(adv)),'protected_files='+str(protected),'R12_STATIC_COMPLETE checks='+str(checks)]
print('\n'.join(out))
