from pathlib import Path
import json,numpy as np,math,itertools
P=Path(__file__).resolve().parents[1];A=P/'src/main/resources/assets/kelmagic'
bs={b['name']:b for b in json.loads((A/'geo/red_mage.geo.json').read_text())['minecraft:geometry'][0]['bones']};an=json.loads((A/'animations/red_mage.animation.json').read_text())['animations']
def sample(v,t):
 if isinstance(v,list):return np.array(v,float)
 vals=sorted((float(k),np.array(x,float))for k,x in v.items())
 if t<=vals[0][0]:return vals[0][1]
 for (a,x),(b,y) in zip(vals,vals[1:]):
  if a<=t<=b:return x+(y-x)*((t-a)/(b-a))
 return vals[-1][1]
def rot(v):
 x,y,z=np.radians(v)*[-1,-1,1];c=np.cos([x,y,z]);s=np.sin([x,y,z]);cx,cy,cz=c;sx,sy,sz=s
 return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
def tx(v):
 m=np.eye(4);m[:3,3]=v;return m
def matrices(a,t):
 out={};tracks=a['bones'];seat='throne_'in a['name']
 def one(n):
  if n in out:return out[n]
  b=bs[n];q=tracks.get(n,{});r=np.array(b.get('rotation',[0,0,0]),float)+sample(q.get('rotation',[0,0,0]),t);p=np.array(b['pivot'])*[-1,1,1];v=sample(q.get('position',[0,0,0]),t)*[-1,1,1]
  if n=='hair_back':r[0]=-(np.array(bs['head'].get('rotation',[0,0,0]))+sample(tracks.get('head',{}).get('rotation',[0,0,0]),t))[0]-(math.degrees(.1)if seat else 0)
  if n.startswith('hair_front_'):r[2]=24 if n.endswith('left')else -24;r[0]=b.get('rotation',[0,0,0])[0]-(sample(tracks.get('head',{}).get('rotation',[0,0,0]),t))[0]*.7
  rm=np.eye(4);rm[:3,:3]=rot(r);m=tx(v)@tx(p)@rm@tx(-p)
  if 'parent'in b:m=one(b['parent'])@m
  out[n]=m;return m
 for n in bs:one(n)
 return out
corners=np.array(list(itertools.product([-1,1],repeat=3)))
def obb(c,m):
 cen=(np.array(c['origin'])+np.array(c['size'])/2)*[-1,1,1];cen=(m@np.r_[cen,1])[:3];h=np.array(c['size'])/2;ax=m[:3,:3];return cen,h,ax

def overlaps(a,b):
 ca,ha,aa=a;cb,hb,ab=b;v=cb-ca
 if np.any(np.abs(v)>=np.abs(aa)@ha+np.abs(ab)@hb-.02):return False
 axes=np.vstack([aa.T,ab.T,np.array([np.cross(x,y)for x in aa.T for y in ab.T])]);norm=np.linalg.norm(axes,axis=1);axes=axes[norm>1e-7]/norm[norm>1e-7,None]
 return bool(np.all(np.abs(axes@v)<np.abs(axes@aa)@ha+np.abs(axes@ab)@hb-.02))
if __name__=='__main__':
 errors=[]
 for name,a in an.items():
  if name.endswith('kitten'):continue
  a['name']=name
  for t in np.arange(0,a['animation_length']+.01,.1):
   m=matrices(a,t);head=obb({'origin':[-4.25,24,-4.82],'size':[8.5,8.25,9.95]},m['head'])
   for side in ['left','right']:
    n=side+'_forearm'
    if overlaps(obb(bs[n]['cubes'][0],m[n]),head):errors.append((name,round(t,2),n))
 print('Forearm/head swept OBB intersections:',len(errors));print(errors[:20])
