"""Change only six chest-attached front lock meshes. Original PNGs and rear rig untouched.
Author in body-rest space, inverse-transform through existing 22-degree chest rest.
No frame cache, extra bones, simulation changes or generator dependency.
"""
from pathlib import Path
import json,math,re,copy
P=Path(__file__).resolve().parents[2]
JAVA=P/'src/main/java/dev/kelmagic/client/hair/Hair6Meshes.java';GEO=P/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json'
s=JAVA.read_text();g=json.loads(GEO.read_text())
C=math.cos(math.radians(22));S=math.sin(math.radians(22))
def curve(ps,t):return [sum(ps[k][j]*[ (1-t)**3,3*(1-t)**2*t,3*(1-t)*t*t,t**3][k] for k in range(4)) for j in range(3)]
upper=[(3.75,24,-3.88),(3.95,22.3,-3.86),(3.95,21.95,-5.63),(3.84,21.407923,-5.65)]
lower=[upper[-1],(3.5,20.8,-5.65),(1.85,18.5,-4.55),(1.55,17.65,-4.1)]
pts=[curve(upper,i/3) for i in range(4)]+[curve(lower,i/3) for i in range(1,4)]
lengths=[0.]
for a,b in zip(pts,pts[1:]):lengths.append(lengths[-1]+math.dist(a,b))
vs=[x/lengths[-1] for x in lengths]
# Keep original root/tip widths, taper gently through the new shoulder arc.
bw=[1.0368,1.13,1.23,1.3248,1.18,.94,.72]
lw=[1.44,1.56,1.71,1.84,1.64,1.3,1.0]
def vertex(i,xoffset,zoffset,mirror,u,v):
 x,y,z=pts[i];dy=y-23;dz=z+zoffset+1.6
 return [mirror*(x+xoffset),23+C*dy-S*dz,-1.6+S*dy+C*dz,u,v]
def ring(i,mirror):
 w=bw[i]/2
 return [vertex(i,-w,0,mirror,0,vs[i]),vertex(i,w,0,mirror,1,vs[i]),vertex(i,w,.22,mirror,1,vs[i]),vertex(i,-w,.22,mirror,0,vs[i])]
meshes={}
for mirror,start in [(1,42),(-1,45)]:
 rings=[ring(i,mirror) for i in range(7)];faces=[]
 for i in range(6):
  a,b=rings[i],rings[i+1]
  for j,k in [(0,1),(2,3),(1,2),(3,0)]:faces.append([a[j],a[k],b[k],b[j]])
 faces.extend([[rings[0][3],rings[0][2],rings[0][1],rings[0][0]],rings[-1]])
 meshes[start]=faces
 for n,offset in [(1,-.06),(2,-.19)]:
  f=[]
  for i in range(6):
   w0,w1=lw[i]/2,lw[i+1]/2
   f.append([vertex(i,-w0,offset,mirror,0,vs[i]),vertex(i,w0,offset,mirror,1,vs[i]),vertex(i+1,w1,offset,mirror,1,vs[i+1]),vertex(i+1,-w1,offset,mirror,0,vs[i+1])])
  meshes[start+n]=f
for n,faces in meshes.items():
 def fmt(x):
  if isinstance(x,list):return '{'+','.join(fmt(v) for v in x)+'}'
  return f'{x:.7f}f'
 regex=r'(private static float\[\]\[\]\[\] mesh'+str(n)+r'\(\)\{return new float\[\]\[\]\[\]).*?(;})'
 s,count=re.subn(regex,lambda m:m[1]+fmt(faces)+m[2],s,flags=re.S);assert count==1
 # Normal orientation reference follows the changed solid shape (render x is negated).
 verts=[v for face in faces for v in face];center=[sum(v[j] for v in verts)/len(verts)/16 for j in range(3)];center[0]*=-1
 regex=r'(new Mesh\(mesh'+str(n)+r'\(\),.*?new Vector3f\()[^)]*(\)\)\);)'
 s,count=re.subn(regex,lambda m:m[1]+','.join(f'{v:.7f}f' for v in center)+m[2],s);assert count==1
 side='left' if n<45 else 'right';kind=['base','under','over'][(n-42)%3];name=f'hair6_front_{side}_{kind}'
 b=next(b for b in g['minecraft:geometry'][0]['bones'] if b['name']==name)
 proto=b['cubes'][0];b['cubes']=[copy.deepcopy(proto) for _ in faces]
JAVA.write_text(s);GEO.write_text(json.dumps(g,indent=2,ensure_ascii=False)+'\n')
report={'body_rest_curve':pts,'base_widths':bw,'layer_widths':lw,'uv_v':vs,'quads_per_side':{'before':24,'after':38},'layer_depths_model_units':[-.06,-.19],'textures_changed':False}
(P.parent/'reports/front-hair-design.json').write_text(json.dumps(report,indent=2))
print('Front hair only: 3→6 curve segments, 24→38 quads/side, root and tip retained')
