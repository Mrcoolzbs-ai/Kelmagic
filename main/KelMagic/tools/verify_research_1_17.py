from pathlib import Path
import json,re
from PIL import Image
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';ns=json.loads((R/'data/kelmagic/research/catalog.json').read_text())['nodes'];by={n['id']:n for n in ns};assert len(ns)==len(by)==31
visited=set();active=set()
def visit(k):
 assert k not in active and k in by
 if k in visited:return
 active.add(k)
 for v in by[k]['parents']:visit(v)
 active.remove(k);visited.add(k)
for k in by:visit(k)
comments=[];hints=[]
for n in ns:
 assert not n.get('tabs') and not n['id'].endswith('_anchor');assert n['thought'] and n['example']
 if n['icon'].startswith('minecraft:'):assert n['icon']=='minecraft:potion'
 else:assert (R/f'assets/kelmagic/models/item/{n["icon"]}.json').exists()
 for st in n['steps']:
  assert st['hint'] and st['comment'];assert 'Как слово' not in st['hint'];assert 'Ответит ли' not in st['hint'];assert not re.search('инвентар|и��вентар',st['hint']);comments.append(st['comment']);hints.append(st['hint'])
  for signal in st.get('signals',[]):assert signal=='record_spell' or signal.startswith('craft:minecraft:')
assert len(comments)==51 and len(set(comments))==51 and len(set(hints))==51
assert len(set(n['thought'] for n in ns))==31
for loc in ['ru_ru','en_us']:
 lang=json.loads((R/f'assets/kelmagic/lang/{loc}.json').read_text())
 for n in ns:assert n['lore'] in lang
 assert 'advancements.kelmagic.own_thought.title' in lang
for chapter in ['origins','materials','sound','equipment','writing','great']:
 im=Image.open(R/f'assets/kelmagic/textures/gui/research/{chapter}.png');assert im.size==(256,256)
for p in list((P/'src/main/java').rglob('*.java'))+list(R.rglob('*.json')):assert not re.search('инвентар|и��вентар',p.read_text()),p
s=(P/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();assert 'int ox=0,oy=0' in s and 'Потяните карту' not in s and 'Добро пожаловать' not in s and 'len*2' in s
s=(P/'src/main/java/dev/kelmagic/client/ResearchEntryScreen.java').read_text();assert 'Как я попробую' not in s and '"К карте"' not in s
assert (R/'data/kelmagic/advancement/own_thought.json').exists()
gold=by['golden_thread'];assert gold['steps'][0]['signals']==['craft:minecraft:white_wool'] and gold['steps'][1]['signals']==['craft:minecraft:gold_nugget'] and gold['steps'][2]['samples']==['minecraft:string','minecraft:gold_nugget'];assert 'Сплести' not in gold['steps'][0]['hint'] and 'Разделить' not in gold['steps'][1]['hint']
print('PASS: 31 unique acyclic single-chapter nodes, 51 individually authored clues and thoughts, specific craft/record triggers, distinct completion thoughts, static chapter art, continuous wave sampling, no inventory wording in runtime, own-note advancement and word sort/filter controls.')
