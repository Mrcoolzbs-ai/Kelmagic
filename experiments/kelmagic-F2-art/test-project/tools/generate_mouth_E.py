"""E-only mouth presentation. Frozen cheek rest source; original atlas/clips never rewritten."""
from pathlib import Path
import sys,json,copy,base64,hashlib
from PIL import Image,ImageDraw
P=Path(__file__).resolve().parents[1];E=P.parent/'experiment';sys.path.insert(0,str(E/'tools'));from bb_export import make_bb
A=P/'src/main/resources/assets/kelmagic';O=A/'textures/entity/mouth_e';O.mkdir(parents=True,exist_ok=True)
colors={'ink':(89,32,46,255),'lip':(155,72,88,255),'inside':(137,58,78,255),'tongue':(205,113,135,255),'tooth':(246,222,217,255)}
for name in ['neutral','smile','speech','surprised']:
 im=Image.new('RGBA',(32,16),(0,0,0,0));d=ImageDraw.Draw(im)
 if name=='neutral':
  d.line([(6,8),(8,9),(24,9),(25,8)],fill=colors['ink'],width=1);d.line((10,10,22,10),fill=colors['lip'])
 elif name=='smile':
  d.line([(6,6),(7,8),(10,9),(13,10),(20,10),(23,9),(25,7),(25,6)],fill=colors['ink']);d.line((12,11,21,11),fill=colors['lip'])
 elif name=='speech':
  d.rectangle((9,6,23,11),fill=colors['inside']);d.line([(8,7),(9,6),(23,6),(24,7),(24,10),(23,11),(10,11),(8,10),(8,7)],fill=colors['ink']);d.line((11,7,21,7),fill=colors['tooth']);d.rectangle((12,10,21,10),fill=colors['tongue'])
 else:
  d.rectangle((13,4,19,11),fill=colors['inside']);d.line([(14,3),(18,3),(20,5),(20,10),(18,12),(14,12),(12,10),(12,5),(14,3)],fill=colors['ink']);d.line((15,11,18,11),fill=colors['lip'])
 im.save(O/(name+'.png'));out=P/'art/red-mage/mouth_e';out.mkdir(exist_ok=True);im.save(out/(name+'.png'))
g=json.loads((E/'states/E0-neutral/red_mage.geo.json').read_text());bones=g['minecraft:geometry'][0]['bones'];by={b['name']:b for b in bones}
# Original cavity is retained, hidden at runtime. Bridge only the precise original mouth cutout.
by['head']['cubes'].append({'origin':[-.85,25.94,-4.14],'size':[1.7,.18,0],'uv':{'north':{'uv':[674,738],'uv_size':[28,28]}}})
# No shape/UV changes: only reparent the existing lower nose cube; rest matches exactly.
base=by['nose']['cubes'].pop(1);bones.append({'name':'nose_lower','parent':'nose','pivot':[0,26.815,-4.29],'cubes':[base]})
bones.append({'name':'mouth_png','parent':'head','pivot':[0,26.01,-4.16],'cubes':[{'origin':[-1,25.51,-4.16],'size':[2,1,0],'uv':{'north':{'uv':[0,0],'uv_size':[1024,1024]}}}]})
# Body .geo uses 1024 UV units. Runtime texture binding switches ONLY mouth_png to a full RGBA PNG.
(A/'geo/red_mage.geo.json').write_text(json.dumps(g,indent=2));(P/'art/red-mage/red_mage.geo.json').write_text(json.dumps(g,indent=2))
bb=make_bb(g,'E — PNG mouth / lower nose breathing');bb['name']='KelMagic E — PNG mouth and lower nose'
png=(O/'neutral.png').read_bytes();bb['textures'].append({'name':'mouth_e/neutral.png','id':'1','width':32,'height':16,'uv_width':32,'uv_height':16,'source':'data:image/png;base64,'+base64.b64encode(png).decode()})
for el in bb['elements']:
 if el['name']=='mouth_png':el['faces']['north']={'uv':[0,0,32,16],'texture':1}
 def_dummy=None

def groups(xs):
 for v in xs:
  if isinstance(v,dict):yield v;yield from groups(v.get('children',[]))
for group in groups(bb['outliner']):
 if group['name'] in ['mouth','lower_lip']:group['visibility']=False
(P/'art/red-mage/red_mage_gecko.bbmodel').write_text(json.dumps(bb,ensure_ascii=False,indent=2))
meta={'mouthPlane':{'origin':[-1,25.51,-4.16],'size':[2,1,0],'frontOffsetFromSkin':.02,'textureSize':[32,16],'geoUVUnits':[1024,1024],'bbPNGUVUnits':[32,16]},'skinClosure':{'origin':[-.85,25.94,-4.14],'size':[1.7,.18,0]},'noseLower':base,'maxNoseY':.012,'atlasSHA256':hashlib.sha256((A/'textures/entity/red_mage_geo.png').read_bytes()).hexdigest(),'PNGsha256':{n:hashlib.sha256((O/(n+'.png')).read_bytes()).hexdigest() for n in ['neutral','smile','speech','surprised']}}
(P.parent/'report/mouth-export.json').write_text(json.dumps(meta,indent=2));print('MOUTH_E_GENERATED 4 original PNGs; cavity retained; nose lower reparented; atlas untouched')
