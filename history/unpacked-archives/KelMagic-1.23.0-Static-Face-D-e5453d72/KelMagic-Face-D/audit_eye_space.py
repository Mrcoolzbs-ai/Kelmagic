from pathlib import Path
import json,math
import numpy as np
from renderer import rot,V
R=Path(__file__).resolve().parent

def faces(geo):
 bs={b['name']:b for b in geo['minecraft:geometry'][0]['bones']};ms={}
 def matrix(n):
  if n in ms:return ms[n]
  b=bs[n];p=np.array(b['pivot'])*[-1,1,1];r=b.get('rotation',[0,0,0]).copy();scale=np.ones(3)
  if n.startswith('lid_'):scale[1]=.07
  if n=='lower_lip':scale[1]=.8896
  if n.startswith('hair_front_'):r[2]=24 if n.endswith('left')else -24
  if n=='hair_back':r[0]=0
  m=np.eye(4);m[:3,:3]=rot(r)@np.diag(scale);m[:3,3]=p-m[:3,:3]@p
  if 'parent'in b:m=matrix(b['parent'])@m
  ms[n]=m;return m
 out=[]
 for n,b in bs.items():
  parent=n
  while parent in bs and parent!='head':parent=bs[parent].get('parent')
  if parent!='head':continue
  for c in b.get('cubes',[]):
   cm=np.eye(4)
   if 'rotation'in c:
    p=np.array(c.get('pivot',b['pivot']))*[-1,1,1];cm[:3,:3]=rot(c['rotation']);cm[:3,3]=p-cm[:3,:3]@p
   m=matrix(n)@cm
   for f in c['uv']:
    raw=(np.array(c['origin'])+np.array(V[f])*np.array(c['size']))*[-1,1,1];v=(np.c_[raw,np.ones(4)]@m.T)[:,:3]
    for ids in [(0,1,2),(0,2,3)]:out.append((n,v[list(ids)]))
 return out

def visible(geo,y=28.2):
 x=np.linspace(-4,4,8001);depth=np.full(x.shape,np.inf);names=np.full(x.shape,'',dtype=object)
 for n,q in faces(geo):
  den=(q[1,1]-q[2,1])*(q[0,0]-q[2,0])+(q[2,0]-q[1,0])*(q[0,1]-q[2,1])
  if abs(den)<1e-9:continue
  w0=((q[1,1]-q[2,1])*(x-q[2,0])+(q[2,0]-q[1,0])*(y-q[2,1]))/den;w1=((q[2,1]-q[0,1])*(x-q[2,0])+(q[0,0]-q[2,0])*(y-q[2,1]))/den;w2=1-w0-w1
  z=w0*q[0,2]+w1*q[1,2]+w2*q[2,2];ok=(w0>=-1e-8)&(w1>=-1e-8)&(w2>=-1e-8)&(z<depth-1e-8);depth[ok]=z[ok];names[ok]=n
 result={}
 for side in ['left','right']:
  pts=-x[np.isin(names,['eye_'+side,'pupil_'+side])]
  result[side]={'visible_interval_geo_x':[round(float(pts.min()),5),round(float(pts.max()),5)],'visible_center_geo_x':round(float((pts.min()+pts.max())/2),6),'y':y}
 return result
if __name__=='__main__':
 report={s:visible(json.loads((R/s/'red_mage.geo.json').read_text())) for s in ['Original','B','C','D']}
 (R/'eye-aperture-audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
