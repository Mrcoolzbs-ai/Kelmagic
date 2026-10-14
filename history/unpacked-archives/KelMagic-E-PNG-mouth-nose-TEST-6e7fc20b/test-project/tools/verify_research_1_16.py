from pathlib import Path
from PIL import Image
import json
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';nodes=json.loads((R/'data/kelmagic/research/catalog.json').read_text())['nodes'];by={n['id']:n for n in nodes};assert len(nodes)==len(by)==31;assert not any(n.get('tabs') or n['id'].endswith('_anchor') for n in nodes)
visited=set();active=set()
def visit(k):
 assert k in by and k not in active,k
 if k in visited:return
 active.add(k)
 for v in by[k]['parents']:visit(v)
 active.remove(k);visited.add(k)
for k in by:visit(k)
comments=0
for n in nodes:
 if n["icon"].startswith("minecraft:"):assert n["icon"]=="minecraft:potion"
 else:assert (R/f'assets/kelmagic/models/item/{n["icon"]}.json').exists()
 assert n['lore'] and n['thought'] and n['example']
 assert 'shift' not in n['example'].lower() and 'пкм' not in n['example'].lower()
 for st in n['steps']:
  assert st['kind'] in ['observation','material'];assert st['comment'].strip();comments+=1
  if st['kind']=='observation':assert st['signals'] and set(st['signals'])<=set(['self_change','living_change','world_change','record_spell','craft:minecraft:white_wool','craft:minecraft:gold_nugget'])
  else:
   assert st['item'].startswith('minecraft:')
   for sample in st.get('samples',[]):assert sample.startswith('minecraft:')
assert comments==51
for loc in ['ru_ru','en_us']:
 lang=json.loads((R/f'assets/kelmagic/lang/{loc}.json').read_text())
 for n in nodes:assert n['lore'] in lang
words={w['word'] for w in json.loads((R/'data/kelmagic/magic/words.json').read_text())['words']};assert len(words)==180
copper=0;ordinary=0
for p in (R/'data/kelmagic/recipe').glob('*.json'):
 d=json.loads(p.read_text());v=d.get('result',{});item=v.get('id','') if isinstance(v,dict) else v
 if not isinstance(item,str) or not item.startswith('kelmagic:'):continue
 k=item[9:]
 if 'inscribed_copper_' in k:copper+=1;assert k.split('inscribed_copper_')[1] in words
 else:ordinary+=1;assert k in by or k=='creative_codex',k
assert copper==1440
for chapter in ['origins','materials','sound','equipment','writing','great']:
 im=Image.open(R/f'assets/kelmagic/textures/gui/research/{chapter}.png');assert im.size==(256,256);assert all(im.getpixel((x,0))==im.getpixel((x,255)) for x in range(256));assert all(im.getpixel((0,y))==im.getpixel((255,y)) for y in range(256))
gold=by['golden_thread'];assert gold['steps'][0]['signals']==['craft:minecraft:white_wool'];assert gold['steps'][1]['signals']==['craft:minecraft:gold_nugget'];assert gold['steps'][2]['samples']==['minecraft:string','minecraft:gold_nugget'];assert len({s['comment'] for s in gold['steps']})==3
print(f'PASS: {len(nodes)} unique single-chapter acyclic nodes; {comments} stage thoughts; bilingual lore keys; {ordinary} ordinary recipes; {copper} copper recipes; six seamless animated-background textures; mundane gold-thread progression.')
