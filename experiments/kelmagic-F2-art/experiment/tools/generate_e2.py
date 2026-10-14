from pathlib import Path
import json,copy,hashlib,shutil,uuid
R=Path(__file__).resolve().parents[1];REF=R/'reference'
base=json.loads((REF/'red_mage.geo.json').read_text());g=copy.deepcopy(base);bones=g['minecraft:geometry'][0]['bones'];by={b['name']:b for b in bones};head=by['head'];old=copy.deepcopy(head['cubes'])
# Correct a LOCAL horizontal reversal, preserving exact original UV interval.
c=old[63];t=c['uv']['north'];before=copy.deepcopy(t);t['uv'][0]+=t['uv_size'][0];t['uv_size'][0]*=-1
fix=copy.deepcopy(g);next(b for b in fix['minecraft:geometry'][0]['bones'] if b['name']=='head')['cubes']=copy.deepcopy(old)
(R/'reference-fixed.geo.json').write_text(json.dumps(fix,indent=2))
for side,s in [('left',1),('right',-1)]:
 bones.append({'name':'cheek_'+side,'parent':'head','pivot':[s*1.9,26.6,-4.14],'cubes':[]})
 bones.append({'name':'lower_lid_'+side,'parent':'head','pivot':[s*1.7,27.35,-3.965],'cubes':[]})
by={b['name']:b for b in bones};head['cubes']=[];provenance=[]
xs=sorted([-v for v in [3.85,3.56,3.28,3.,1.5,1.3,1.1,.9]]+[0]+[.9,1.1,1.3,1.5,3.,3.28,3.56,3.85]);ys=[25.5,25.73,25.96,26.20,26.85,26.97,27.08,27.2]
def sub(c,x0,x1,y0,y1):
 ox,oy,z=c['origin'];w,h,_=c['size'];t=c['uv']['north'];u,v=t['uv'];du,dv=t['uv_size'];return {'north':{'uv':[u+du*(x0-ox)/w,v+dv*(oy+h-y1)/h],'uv_size':[du*(x1-x0)/w,dv*(y1-y0)/h]}}
for i,c in enumerate(old):
 if i not in [61,62,63,64]:head['cubes'].append(c);continue
 ox,oy,z=c['origin'];w,h,_=c['size'];xx=sorted(set([ox,ox+w]+[x for x in xs if ox<x<ox+w]));yy=sorted(set([oy,oy+h]+[y for y in ys if oy<y<oy+h]))
 for x0,x1 in zip(xx,xx[1:]):
  for y0,y1 in zip(yy,yy[1:]):
   x=(x0+x1)/2;y=(y0+y1)/2
   name='cheek_'+('left' if x>0 else 'right') if .9<abs(x)<3.85 and 25.5<y<27.2 else 'head'
   c2={'origin':[x0,y0,z],'size':[x1-x0,y1-y0,0],'uv':sub(c,x0,x1,y0,y1)};by[name]['cubes'].append(c2)
   provenance.append({'source':i,'bone':name,'rect':[x0,x1,y0,y1],'uv':c2['uv']})
skin=copy.deepcopy(by['lid_left']['cubes'][0]['uv']['north'])
for side,s in [('left',1),('right',-1)]:
 for x0,x1 in zip([.55,.67,.83,2.57,2.73],[.67,.83,2.57,2.73,2.85]):
  edge=min((x0+x1)/2-.55,2.85-(x0+x1)/2);trim=.28 if edge<.12 else .2 if edge<.28 else 0;lo,hi=sorted([s*x0,s*x1]);bottom=27.35+trim
  by['lower_lid_'+side]['cubes'].append({'origin':[lo,bottom-.30,-3.965],'size':[hi-lo,.30,0],'uv':{'north':copy.deepcopy(skin)}})
# Reuse deterministic neutral BB export, without executing the old experiment generator.
from bb_export import make_bb
for name,vals in {'E0-neutral':[0,0,0,0],'E1-subtle':[.35,.35,.35,.35],'E2-smile-lift':[.8,.8,.8,.8],'E3-asymmetric':[1,.15,1,.15]}.items():
 d=R/'states'/name;d.mkdir(parents=True,exist_ok=True);(d/'red_mage.geo.json').write_text(json.dumps(g,indent=2));(d/(name+'.bbmodel')).write_text(json.dumps(make_bb(g,name),ensure_ascii=False,indent=2))
 for f in ['red_mage_geo.png','red_mage.animation.json']:shutil.copy2(REF/f,d/f)
 (d/'pose.json').write_text(json.dumps({'normalizedValues':dict(zip(['cheekLeft','cheekRight','lowerLeft','lowerRight'],vals)),'runtimeVertexMorph':True,'restGeometry':True,'maxCheekY':.16,'maxCheekZ':.04,'maxLowerY':.22},indent=2))
(R/'report/generation-v2.json').write_text(json.dumps({'uvCorrection':{'sourceHeadCube':63,'before':before,'after':old[63]['uv']['north']},'bones':len(bones),'primitives':sum(len(b.get('cubes',[])) for b in bones),'sourcePanels':[61,62,63,64],'provenance':provenance},indent=2))
print('GENERATED',len(bones),'bones',sum(len(b.get('cubes',[])) for b in bones),'primitives')
