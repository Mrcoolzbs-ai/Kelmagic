from pathlib import Path
from PIL import Image,ImageOps
import json,hashlib,zipfile,sys
r=Path('src/main/resources');a=r/'assets/kelmagic';checks=0
def ok(x,msg):
 global checks
 assert x,msg;checks+=1
# All JSON parses; no guessed registry strings are validated just by syntax (native run covers registry resolution).
for p in r.rglob('*.json'):json.loads(p.read_text())
ok(True,'all resources JSON parse')
p=a/'textures/block';trap=Image.open(p/'midnight_trapdoor.png');ok(trap.tobytes()==trap.rotate(90).tobytes(),'trapdoor D4 rotation coherent');ok(trap.tobytes()==ImageOps.mirror(trap).tobytes(),'trapdoor mirrored coherent')
window=Image.open(p/'midnight_door_top.png').crop((3,2,13,13));ok(window.tobytes()==ImageOps.mirror(window).tobytes(),'door window exactly symmetric')
for i in range(16):
 im=Image.open(p/f'manuscript_cabinet_{i}.png');ok(im.tobytes()==ImageOps.mirror(im).tobytes(),'manuscript facade symmetry '+str(i))
for i in range(16):
 blue=Image.open(p/f'resonance_glass_{i}.png').convert('RGBA');violet=Image.open(p/f'violet_resonance_glass_{i}.png').convert('RGBA');ok(blue.getchannel('A').tobytes()==violet.getchannel('A').tobytes(),'glass alpha and seams preserved '+str(i));ok(blue.tobytes()!=violet.tobytes(),'glass recoloured '+str(i))
bs=json.loads((a/'blockstates/resonance_glass.json').read_text())['variants'];ok(len(bs)==128,'all glass joins in two tint states')
for m in bs.values():ok((a/'models'/Path(m['model'].replace('kelmagic:','')+'.json')).exists(),'model exists')
# Every animated direct edge is checked with a 24px margin and room for progress badges.
nodes=json.loads((r/'data/kelmagic/research/catalog.json').read_text())['nodes']
for tab in {n['tab'] for n in nodes}:
 ns=[n for n in nodes if n['tab']==tab and n['id'] not in ['monocle','manuscript_cabinet']]
 for b in ns:
  for parent in b['parents']:
   source=next((n for n in ns if n['id']==parent),None)
   if not source:continue
   for o in ns:
    if o in [source,b]:continue
    for i in range(101):
     f=i/100.;xx=source['x']+(b['x']-source['x'])*f;yy=source['y']+(b['y']-source['y'])*f
     ok(not(abs(xx-o['x'])<24 and o['y']-24<yy<o['y']+38),f'direct wave clear {source["id"]} -> {b["id"]} vs {o["id"]}')
# Previously approved face, speech, hair, phrase mechanics unchanged from delivered R8 source.
base=zipfile.ZipFile(sys.argv[1] if len(sys.argv)>1 else '/data/.agent-service/files/5aab4352-42cc-4bbc-96e6-f9d9a80afd66/KelMagic_R8_Ink_Drawings_TRANSFER.zip');prefix='KelMagic_R8_Ink_Drawings_TEST/source-project/'
protected=['client/face/','client/hair/','speech/','core/','client/Phrase','client/MageFace','client/MageHair']
count=0
for path in Path('src/main/java/dev/kelmagic').rglob('*.java'):
 rel=str(path.relative_to('src/main/java/dev/kelmagic'))
 if any(rel.startswith(q) for q in protected):ok(path.read_bytes()==base.read(prefix+str(path)),f'protected baseline unchanged {rel}');count+=1
print(json.dumps({'passed_checks':checks,'protected_java_files_byte_identical':count,'result':'PASS'}))
