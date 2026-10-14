from pathlib import Path
import json,hashlib,math,base64,io
from PIL import Image
ROOT=Path(__file__).resolve().parent
main=Path('/data/KelMagic');before=json.loads((ROOT/'working-project-before.sha256.json').read_text());diff=[]
for name,digest in before.items():
 p=main/name
 if not p.exists() or hashlib.sha256(p.read_bytes()).hexdigest()!=digest:diff.append(name)
assert not diff,diff
# Exact per-face UV round trip between authored BB and generated runtime geometry.
checks={}
for s in 'ABC':
 geo=json.loads((ROOT/s/'red_mage.geo.json').read_text());bb=json.loads((ROOT/s/f'KelMagic-Face-{s}.bbmodel').read_text());bones=geo['minecraft:geometry'][0]['bones'];flat=[(b,c) for b in bones for c in b.get('cubes',[])];assert len(flat)==len(bb['elements'])
 for (b,c),e in zip(flat,bb['elements']):
  assert c['origin']==e['from'];assert e['to']==[c['origin'][j]+c['size'][j] for j in range(3)]
  assert all(math.isfinite(v) and v>=0 for v in c['size'])
  assert set(c['uv'])==set(e['faces'])
  for f,v in c['uv'].items():
   assert e['faces'][f]['uv']==[v['uv'][0],v['uv'][1],v['uv'][0]+v['uv_size'][0],v['uv'][1]+v['uv_size'][1]]
   assert all(0<=x<=1024 for x in e['faces'][f]['uv'])
 assert (ROOT/s/'red_mage.animation.json').read_bytes()==(main/'art/red-mage/red_mage.animation.json').read_bytes()
 assert bb['animations']==json.loads((main/'art/red-mage/red_mage_gecko.bbmodel').read_text())['animations']
 embedded=Image.open(io.BytesIO(base64.b64decode(bb['textures'][0]['source'].split(',')[1]))).convert('RGBA');external=Image.open(ROOT/s/'red_mage_geo.png').convert('RGBA');assert embedded.tobytes()==external.tobytes()
 checks[s]={'geometry_bb_roundtrip':'PASS','uv_bounds':'PASS','embedded_external_texture':'PASS','runtime_animation_bytes':'unchanged','bb_animation_objects':'unchanged'}
summary={'working_source_sha256_verified_files':len(before),'working_source_changes':diff,'variants':checks,'scope':'static test models only; no live integration or animation behavior validation'}
(ROOT/'verification.json').write_text(json.dumps(summary,indent=2));print(json.dumps(summary))
