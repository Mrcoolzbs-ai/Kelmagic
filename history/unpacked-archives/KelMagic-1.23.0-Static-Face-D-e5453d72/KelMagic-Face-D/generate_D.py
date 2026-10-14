"""Static D study. Read stock 1.23 generated model; write D only. No Java/clip edits."""
from pathlib import Path
import sys,json,copy,uuid,base64,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parent
P=Path('/data/kelmagic-face-variants/isolated-base/KelMagic')
assert 'mod_version=1.23.0' in (P/'gradle.properties').read_text()
sys.path.insert(0,str(P/'tools'))
from mascot_surface_fixes_1_21 import surface_mesh
base=json.loads((P/'art/red-mage/red_mage.geo.json').read_text());bbbase=json.loads((P/'art/red-mage/red_mage_gecko.bbmodel').read_text())
geo=copy.deepcopy(base);by={b['name']:b for b in geo['minecraft:geometry'][0]['bones']};old=copy.deepcopy(by)
tex=Image.open(P/'art/red-mage/red_mage_geo.png').convert('RGBA');originaltex=tex.copy();F=['north','south','west','east','up','down']
FACE={'head','nose','mouth','lower_lip'}|{p+s for p in ['eye_','pupil_','lid_','brow_'] for s in ['left','right']}
def mat(n,i=0):return copy.deepcopy(old[n]['cubes'][i]['uv'])
skin={f:copy.deepcopy(next(iter(mat('head').values()))) for f in F};dark=mat('pupil_left',1)
def cube(lo,hi,uv,rotation=None,pivot=None):
 c={'origin':list(lo),'size':[round(hi[i]-lo[i],8) for i in range(3)],'uv':copy.deepcopy(uv)}
 if rotation:c.update(rotation=rotation,pivot=pivot)
 return c
def box(x0,x1,y0,y1,z0,z1,uv,s=1,**kw):
 x0,x1=sorted([s*x0,s*x1]);return cube([x0,y0,z0],[x1,y1,z1],uv,**kw)
# Same verified free region as C; never overwrite a texel sampled by stock UV.
ux,uy=896,896;used=Image.new('1',tex.size);ud=ImageDraw.Draw(used)
for b in old.values():
 for c in b.get('cubes',[]):
  for t in c['uv'].values():
   x,y=t['uv'];w,h=t['uv_size'];ud.rectangle([min(x,x+w),min(y,y+h),max(x,x+w),max(y,y+h)],fill=1)
assert not used.crop((ux,uy,ux+128,uy+128)).getbbox()
face=Image.new('RGBA',(64,64),(250,224,226,255));fd=ImageDraw.Draw(face)
# Feathered pixel clusters, not rectangular cheek decals. Same original skin-derived palette.
cluster=[(-3,0),(-2,-1),(-2,0),(-2,1),(-1,-1),(-1,0),(-1,1),(0,-1),(0,0),(0,1),(0,2),(1,-1),(1,0),(1,1),(2,0),(2,1),(3,0)]
for center in [12,51]:
 for dx,dy in cluster:
  fd.point((center+dx,43+dy),fill=(246,212,219,255) if abs(dx)>1 or abs(dy)>0 else (243,204,215,255))
 for dx,dy in [(-4,-1),(4,1),(-3,2),(3,2)]:fd.point((center+dx,43+dy),fill=(248,219,223,255))
# Tiny corner/under-eye accents; no horizontal jaw line or painted-on nose.
for x in [7,54]:fd.point((x,38),fill=(242,212,218,255));fd.point((x+1,39),fill=(247,218,223,255))
tex.paste(face,(ux,uy))
# Coarser massing than B. The lower transition will be one tilted solid bevel, not a shelf.
bands=[(24,25.5,3.85,-3.88),(25.5,27.2,3.85,-4.14),(27.2,29.2,3.92,-3.98),(29.2,32,4,-3.85)]
xmin,xmax=.55,2.85;bottom,top=27.35,29.05;mw=.85;my=25.94;mh=.18
xs=sorted(set([-4,4,-mw,mw]+[s*x for s in [-1,1] for x in [xmin,xmin+.18,xmax-.20,xmax]]+[s*b[2] for s in [-1,1] for b in bands]))
ys=sorted(set([24,32,bottom,bottom+.20,top-.14,top,my,my+mh]+[v for b in bands for v in b[:2]]))
def opening(x,y):
 if abs(x)<mw and my<y<my+mh:return True
 a=abs(x)
 return xmin<a<xmax and bottom<y<top and not(a<xmin+.18 and y>top-.14 or a>xmax-.20 and y<bottom+.20)
parts=[]
for x0,x1 in zip(xs,xs[1:]):
 for y0,y1 in zip(ys,ys[1:]):
  xm,ym=(x0+x1)/2,(y0+y1)/2;a,b,w,z=next(v for v in bands if v[0]<=ym<=v[1])
  if abs(xm)<w:parts.append(([x0,y0,-3.06 if opening(xm,ym) else z],[x1,y1,4],'wood',None))
head=[]
for e in surface_mesh(parts,True):
 lo=e['from'].copy();hi=e['to'].copy();lo[1]+=16;hi[1]+=16;f=next(iter(e['faces']));uv={f:skin[f]}
 if f=='north':uv={f:{'uv':[ux+(hi[0]+4)*8,uy+(32-hi[1])*8],'uv_size':[-(hi[0]-lo[0])*8,(hi[1]-lo[1])*8]}}
 head.append(cube(lo,hi,uv))
# One broad cuboid connected into core, inclined 9.2 degrees to merge cheek/chin.
# Its back intersects the core, front upper edge shares the cheek's exact boundary.
bevel_angle=math.degrees(math.asin(.24/1.5))
head.append(box(-3.85,3.85,24,25.5,-4.14,-3.80,skin,rotation=[bevel_angle,0,0],pivot=[0,25.5,-4.14]))
# Ray/mesh aperture audit found hair cutoff at |x|=2.85.
# White bounds [.56,2.84] therefore have exact visible center 1.70.
visible_outer=2.85;white_inner=.56;white_outer=visible_outer-.01
iris_center=(white_inner+white_outer)/2
assert abs(iris_center-1.70)<1e-8
target=[0,28.2,-128.0];eyeinfo={} 
for side,sign in [('left',1),('right',-1)]:
 center=[sign*iris_center,28.2,-3.91]
 angle=sign*math.degrees(math.atan(iris_center/(128-3.91)))
 # Full aperture/iris center equality. Tiny, analytically derived static convergence.
 by['eye_'+side]['cubes']=[box(white_inner,white_outer,27.36,29.04,-3.79,-3.60,mat('eye_'+side),sign)]
 tile=Image.new('RGBA',(20,20),(201,61,155,255) if sign==1 else (116,119,127,255));td=ImageDraw.Draw(tile)
 col=(201,61,155) if sign==1 else (116,119,127)
 td.rectangle((0,0,19,3),fill=tuple(round(v*.85) for v in col)+(255,));td.rectangle((0,17,19,19),fill=tuple(min(255,round(v*1.09)) for v in col)+(255,))
 # Pixel bounds [8,12)x[4,16) have exact center (10,10), not C's (8.5,8.5) in 16x16.
 td.rectangle((8,4,11,15),fill=(44,32,48,255))
 # UV reflection is explicit; highlights keep same screen/world light side on both eyes.
 for x,y,w,h in [(13,3,4,4),(4,16,2,2)]:
  if sign==1:x=20-x-w
  td.rectangle((x,y,x+w-1,y+h-1),fill=(253,250,242,255))
 tx=ux+(64 if sign==1 else 88);ty=uy+72;tex.paste(tile,(tx,ty))
 uv=copy.deepcopy(mat('pupil_'+side))
 uv['north']={'uv':[tx+20,ty],'uv_size':[-20,20]} if sign==1 else {'uv':[tx,ty],'uv_size':[20,20]}
 by['pupil_'+side]['cubes']=[box(iris_center-.62,iris_center+.62,27.49,28.91,-3.91,-3.87,uv,sign,rotation=[0,angle,0],pivot=center)]
 head +=[box(.74,2.63,28.96,29.15,-4.02,-3.96,dark,sign),box(.55,.74,28.91,29.07,-4.02,-3.96,dark,sign),box(2.63,2.85,29.04,29.24,-4.02,-3.96,dark,sign)]
 by['lid_'+side]['cubes']=[box(.55,2.85,27.35,29.3,-3.965,-3.90,skin,sign)]
 by['brow_'+side]['cubes']=[box(.8,3.12,29.52,29.70,-4.025,-3.80,mat('brow_'+side),sign)]
 eyeinfo[side]={'bone_parent':by['pupil_'+side]['parent'],'geometry_center_relative_to_bone_pivot':[sign*(iris_center-1.95),0,-.30],'iris_front_center':center,'cube_rotation':[0,angle,0],'target':target,'north_uv':uv['north'],'pupil_pixel_bounds':[8,4,12,16],'iris_pixels':[20,20]}
by['head']['cubes']=head
by['nose']['cubes']=[box(-.20,.20,26.89,27.48,-4.32,-4.10,skin),box(-.29,.29,26.69,26.94,-4.48,-4.10,skin)]
by['mouth']['cubes']=[box(-mw,mw,my,my+mh,-4.025,-3.94,mat('mouth')),box(-.58,.58,my+.12,my+.15,-4.05,-4.025,mat('mouth',2))]
ly=25.25+(my-.025-25.25)/.8896
by['lower_lip']['cubes']=[box(-.76,.76,ly,ly+.06,-4.165,-4.14,mat('lower_lip',1))]
# The stock 1.23 exporter convention, with cube pivot/rotation preserved in this D-only adapter.
bb=copy.deepcopy(bbbase);els=[];groups={}
for b in by.values():
 gid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:geo120:'+b['name']));groups[b['name']]={'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':gid,'children':[]}
 for i,c in enumerate(b.get('cubes',[])):
  eid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:cube120:'+b['name']+str(i)));faces={f:{'uv':[v['uv'][0],v['uv'][1],v['uv'][0]+v['uv_size'][0],v['uv'][1]+v['uv_size'][1]],'texture':0} for f,v in c['uv'].items()}
  els.append({'name':b['name'],'type':'cube','uuid':eid,'from':c['origin'],'to':[c['origin'][j]+c['size'][j] for j in range(3)],'origin':c.get('pivot',b['pivot']),'rotation':c.get('rotation',[0,0,0]),'autouv':0,'faces':faces});groups[b['name']]['children'].append(eid)
for b in by.values():
 if 'parent'in b:groups[b['parent']]['children'].append(groups[b['name']])
out=R/'D';out.mkdir(exist_ok=True);tex.save(out/'red_mage_geo.png');bb.update(name='KelMagic 1.23.0 — Static Face D',elements=els,outliner=[groups['root']]);bb['textures'][0]['source']='data:image/png;base64,'+base64.b64encode((out/'red_mage_geo.png').read_bytes()).decode()
(out/'red_mage.geo.json').write_text(json.dumps(geo,indent=2));(out/'KelMagic-Face-D.bbmodel').write_text(json.dumps(bb,indent=2));(out/'red_mage.animation.json').write_bytes((P/'art/red-mage/red_mage.animation.json').read_bytes())
for n,b in by.items():
 assert {k:v for k,v in b.items() if k!='cubes'}=={k:v for k,v in old[n].items() if k!='cubes'}
 if n not in FACE:assert b==old[n],n
assert bb['animations']==bbbase['animations']
changed=np.any(np.array(tex)!=np.array(originaltex),axis=2);assert not(changed&(np.array(used)!=0)).any()
report={'version':'1.23.0','bones':len(by),'primitives':len(els),'face_primitives':sum(len(by[n].get('cubes',[])) for n in FACE),'clips_unchanged':len(bb['animations']),'eyes':eyeinfo,'bevel_angle':bevel_angle,'head_bands':bands,'changed_texels_in_previously_unused_region':int(changed.sum()),'new_dependencies':False,'main_project_edits':False}
(R/'generation-D.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
