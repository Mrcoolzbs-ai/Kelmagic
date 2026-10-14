from pathlib import Path
import json,copy,uuid
BB=json.loads((Path(__file__).resolve().parents[1]/'reference/red_mage_gecko.bbmodel').read_text())
def make_bb(geo,name):
 bb=copy.deepcopy(BB);bb['name']='KelMagic E — '+name
 groups={};elements=[]
 for b in geo['minecraft:geometry'][0]['bones']:
  gid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:geo120:'+b['name']));groups[b['name']]={'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':gid,'children':[]}
  for i,c in enumerate(b.get('cubes',[])):
   uid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:E:'+b['name']+':'+str(i)))
   faces={f:{'uv':[v['uv'][0],v['uv'][1],v['uv'][0]+v['uv_size'][0],v['uv'][1]+v['uv_size'][1]],'texture':0} for f,v in c['uv'].items()}
   elements.append({'name':b['name'],'type':'cube','uuid':uid,'from':c['origin'],'to':[c['origin'][j]+c['size'][j] for j in range(3)],'origin':c.get('pivot',b['pivot']),'rotation':c.get('rotation',[0,0,0]),'autouv':0,'faces':faces})
   groups[b['name']]['children'].append(uid)
 for b in geo['minecraft:geometry'][0]['bones']:
  if 'parent' in b:groups[b['parent']]['children'].append(groups[b['name']])
 bb['elements']=elements;bb['outliner']=[groups['root']]
 # The embedded atlas source is retained verbatim; no image is re-encoded.
 return bb

