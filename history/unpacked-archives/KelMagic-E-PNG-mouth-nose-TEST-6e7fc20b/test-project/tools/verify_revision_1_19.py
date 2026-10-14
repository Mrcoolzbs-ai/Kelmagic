from pathlib import Path
import json
from PIL import Image
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';c=json.loads((R/'data/kelmagic/research/catalog.json').read_text());ns=c['nodes'];by={n['id']:n for n in ns}
assert len(by)==len(ns)==27
assert len([n for n in ns if n['tab']=='equipment'])==2
assert {n['id'] for n in ns if n['tab']=='sound'}=={'scripter','cassette','cassette_player','cassette_deck'}
assert by['transformation']['parents']==[] and by['resonance_station']['parents']==['transformation']
assert by['golden_thread']['steps'][-1]['kind']=='material' and by['golden_wool']['steps'][-1]['kind']=='material'
assert 'craft:minecraft:white_wool' not in by['magic_cloth']['steps'][0]['signals']
rituals=[n for n in ns if n['steps'] and n['steps'][-1]['kind']=='resonance'];assert len(rituals)==8
def ancestors(n):
 out=set(n['parents'])
 for parent in n['parents']:out.update(ancestors(by[parent]))
 return out
for n in rituals:assert 'resonance_station' in ancestors(n)
for n in ns:
 assert len(n['parents'])==len(set(n['parents']))
 for parent in n['parents']:assert parent in by
 assert -150<=n['x']<=150 and -70<=n['y']<=70
 def walk(id,seen):
  assert id not in seen,'cyclic research';
  for parent in by[id]['parents']:walk(parent,seen|{id})
 walk(n['id'],set())
 for s in n['steps']:assert not s.get('word') or s['word'] not in s['hint'].split()
model=json.loads((R/'assets/kelmagic/models/block/resonance_station.json').read_text());table=json.loads((R/'assets/kelmagic/models/block/archivist_table.json').read_text());assert model['parent']=='minecraft:block/block' and model['display']==table['display']
for name in ['codex_entry','codex_words']:assert Image.open(R/f'assets/kelmagic/textures/gui/{name}.png').size==(360,216)
rig=json.loads((P/'art/red-mage/rig.json').read_text());assert len(rig['parts'])==26
for p in rig['parts']:
 if p['name'].startswith('chest_'):assert p['rotation'][0]<0 and p['cubes'][0][-1]==4
for key in ['transformed_magic_torch','transformed_magic_wall_torch','transformed_magic_lantern']:assert (R/f'data/kelmagic/loot_table/blocks/{key}.json').exists()
print('PASS:27 unique bounded acyclic nodes,8 magical/group paths, mundane/sound splits,3D model and matching hand scale, raised pages,26part rig, durable lights')
