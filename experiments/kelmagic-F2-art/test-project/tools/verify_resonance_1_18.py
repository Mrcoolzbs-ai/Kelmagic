from pathlib import Path
import json,base64,re
from PIL import Image
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic';nodes=json.loads((R/'data/kelmagic/research/catalog.json').read_text())['nodes'];by={n['id']:n for n in nodes};assert len(nodes)==len(by)==31
words={v['word'] for v in json.loads((R/'data/kelmagic/magic/words.json').read_text())['words']};done=set()
def visit(k,path):
 assert k not in path
 for q in by[k]['parents']:visit(q,path|{k})
 done.add(k)
for k in by:visit(k,set())
rituals=[n for n in nodes if not n['free'] and not n.get('sealed')];assert len(rituals)==18
for n in rituals:
 st=n['steps'][-1];assert st['kind']=='resonance' and st['word'] in words and 1<=len(st['samples'])<=8 and st['center'];assert st['word'] not in st['hint'].split();assert 'Огненный стержень' not in st['hint'];assert all(s['hint'] and s['comment'] for s in n['steps']);assert all(s['kind']=='observation' for s in n['steps'][:-1])
assert by['magic_cloth']['steps'][0]['signals']==['craft:minecraft:loom'];assert by['golden_thread']['steps'][0]['signals']==['craft:minecraft:white_wool']
rig=json.loads((P/'art/red-mage/rig.json').read_text());model=json.loads((P/'art/red-mage/red_mage.bbmodel').read_text());assert len(rig['parts'])==len(model['elements'])==26
assert (P/'art/red-mage/red_mage.png').read_bytes()==(A/'textures/entity/red_mage.png').read_bytes();assert base64.b64decode(model['textures'][0]['source'].split(',')[1])==(A/'textures/entity/red_mage.png').read_bytes()
original=Image.open(P/'art/red-mage/original-skin.png').convert('RGBA');atlas=Image.open(P/'art/red-mage/red_mage.png');assert atlas.crop((0,0,64,16)).tobytes()==original.crop((0,0,64,16)).tobytes()
for e in model['elements']:
 for face in e['faces'].values():assert all(0<=v<=128 for v in face['uv'])
assert abs(max(e['to'][1] for e in model['elements'])-min(e['from'][1] for e in model['elements'])-40)<.001
tex=Image.open(A/'textures/block/mana_block.png');assert tex.size==(32,1024);assert tex.crop((0,0,32,32)).tobytes()!=tex.crop((0,512,32,544)).tobytes();assert json.loads(Path(str(A/'textures/block/mana_block.png')+'.mcmeta').read_text())['animation']['interpolate']
rec=json.loads((R/'data/kelmagic/recipe/mana_block.json').read_text());assert rec['pattern']==['MMM']*3 and rec['key']['M']['item']=='kelmagic:mana_core'
recipe=json.loads((R/'data/kelmagic/recipe/resonance_station.json').read_text());assert len(recipe['key'])==7 and recipe['key']['L']['tag']=='minecraft:logs'
s=(P/'src/main/java/dev/kelmagic/client/CodexScreen.java').read_text();assert 'int ox=0,oy=0' in s and 'x+10,y+18,x+350,y+200' in s;assert 'У меня пока нет мыслей' in s;assert 'DDD2BA' not in (P/'src/main/java/dev/kelmagic/client/CodexPaint.java').read_text()
print('PASS: 31 acyclic nodes, 18 word-gated final rituals, no repeated wool craft, valid words/resources, preserved skin head, 26-part Blockbench rig with embedded atlas and bounded UVs, 40px height, animated mana texture, 9-core recipe and 7 station ingredient types, widened clean map edges and lifted entry.')
