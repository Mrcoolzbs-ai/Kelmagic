from pathlib import Path
import json
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';nodes=json.loads((R/'data/kelmagic/research/catalog.json').read_text())['nodes'];by={n['id']:n for n in nodes};assert len(by)==len(nodes)
words={w['word'] for w in json.loads((R/'data/kelmagic/magic/words.json').read_text())['words']};assert len(words)==180
visiting=set();visited=set()
def walk(k):
 assert k in by
 assert k not in visiting,k
 if k in visited:return
 visiting.add(k)
 for parent in by[k].get('parents',[]):walk(parent)
 visiting.remove(k);visited.add(k)
for k in by:walk(k)
for n in nodes:
 for field in ['lore','thought','example']:assert n.get(field),n['id']
 assert (R/f'assets/kelmagic/models/item/{n["icon"]}.json').exists(),n['icon']
 for step in n['steps']:
  assert step['kind'] in ['material','observation']
  if step['kind']=='material':assert step['item'].startswith('minecraft:')
  else:assert step['signals'] and set(step['signals'])<=set(['self_change','living_change','world_change','record_spell'])
for locale in ['ru_ru','en_us']:
 lang=json.loads((R/f'assets/kelmagic/lang/{locale}.json').read_text())
 for n in nodes:assert n['lore'] in lang,n['lore']
copper=0;other=0
for p in (R/'data/kelmagic/recipe').glob('*.json'):
 d=json.loads(p.read_text());r=d.get('result',{});item=r.get('id','') if isinstance(r,dict) else r
 if not isinstance(item,str) or not item.startswith('kelmagic:'):continue
 k=item[9:]
 if 'inscribed_copper_' in k:
  copper+=1;assert k.split('inscribed_copper_')[1] in words;assert d['type']=='minecraft:stonecutting'
 else:other+=1;assert k in by or k=='creative_codex',k
assert copper==1440,copper
assert by['mana_core']['free'] and by['archivists_codex']['free'] and by['archivist_table']['free']
assert all(n.get('sealed',False) for n in nodes if n['id'].startswith('rite:'))
print(f'PASS: {len(nodes)} unique acyclic nodes; bilingual lore; real icons; supported experiments; {other} non-copper recipes covered; {copper} copper recipes for all {len(words)} words.')
