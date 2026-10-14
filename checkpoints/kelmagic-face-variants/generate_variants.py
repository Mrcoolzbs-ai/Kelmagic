"""Isolated static art studies, layered on the unchanged KelMagic 1.23 pipeline.
Run the stock generator in isolated-base first. Never writes the live project.
"""
from pathlib import Path
import sys,json,copy,uuid,base64,io,zipfile,hashlib
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parent
P=ROOT/'isolated-base/KelMagic'
assert 'mod_version=1.23.0' in (P/'gradle.properties').read_text()
sys.path.insert(0,str(P/'tools'))
from mascot_surface_fixes_1_21 import surface_mesh
base=json.loads((P/'art/red-mage/red_mage.geo.json').read_text())
basebb=json.loads((P/'art/red-mage/red_mage_gecko.bbmodel').read_text())
texture=Image.open(P/'art/red-mage/red_mage_geo.png').convert('RGBA')
FACE={'head','nose','mouth','lower_lip'}|{p+s for p in ['eye_','pupil_','lid_','brow_'] for s in ['left','right']}
F=['north','south','west','east','up','down']
old={b['name']:b for b in base['minecraft:geometry'][0]['bones']}
def mat(n,i=0):return copy.deepcopy(old[n]['cubes'][i]['uv'])
skin={f:copy.deepcopy(next(iter(mat('head').values()))) for f in F}
dark=mat('pupil_left',1)
def cube(lo,hi,uv):return {'origin':list(lo),'size':[round(hi[i]-lo[i],6) for i in range(3)],'uv':copy.deepcopy(uv)}
def box(x0,x1,y0,y1,z0,z1,uv,sign=1):
 x0,x1=sorted([x0*sign,x1*sign]);return cube([x0,y0,z0],[x1,y1,z1],uv)
def patch_uv(x,y,w,h):return {f:{'uv':[x,y],'uv_size':[w,h]} for f in F}
# Find unused 128x128 atlas space, considering EVERY existing bone face.
used=Image.new('1',texture.size);d=ImageDraw.Draw(used)
for b in old.values():
 for c in b.get('cubes',[]):
  for v in c['uv'].values():
   x,y=v['uv'];w,h=v['uv_size'];d.rectangle([min(x,x+w),min(y,y+h),max(x,x+w),max(y,y+h)],fill=1)
free=None
for y in range(896,0,-128):
 for x in range(896,-1,-128):
  if not used.crop((x,y,x+128,y+128)).getbbox():free=(x,y);break
 if free:break
assert free
palette={'skin':(250,224,226,255),'white':(253,250,242,255),'dark':(44,32,48,255),'lip':(156,70,86,255),'gray':(116,119,127,255),'pink':(201,61,155,255)}
reports={}
for variant in 'ABC':
 geo=copy.deepcopy(base);by={b['name']:b for b in geo['minecraft:geometry'][0]['bones']};tex=texture.copy();td=ImageDraw.Draw(tex);ux,uy=free
 # B has real sculpted rectilinear head mass. C modest jaw shaping, A compact original block.
 bands=([(24,25.1,3.0,-3.62),(25.1,26.7,3.55,-4.28),(26.7,27.4,3.7,-4.18),(27.4,29.2,3.85,-3.98),(29.2,32,4,-3.82)] if variant=='B' else [(24,25.1,3.5,-3.91),(25.1,32,4,-4)] if variant=='C' else [(24,32,4,-4)])
 if variant=='A':xmin,xmax,bottom,top=.40,3.50,27.30,29.22;mouthw=.91;my=25.91;mh=.20
 elif variant=='B':xmin,xmax,bottom,top=.58,3.26,27.45,29.00;mouthw=1.06;my=25.86;mh=.24
 else:xmin,xmax,bottom,top=.42,3.45,27.52,28.94;mouthw=.85;my=26.00;mh=.16
 # Three silhouette bands per eye: not a plain rectangular aperture.
 cut=.28 if variant!='B' else .24
 def opening(x,y):
  ax=abs(x)
  if abs(x)<mouthw and my<y<my+mh:return True
  if not(xmin<ax<xmax and bottom<y<top):return False
  return not ((ax<xmin+cut and y<bottom+.25) or (ax>xmax-cut and y<bottom+.25) or (ax<xmin+.18 and y>top-.16))
 xs=sorted(set([-4,4,-mouthw,mouthw]+[s*v for s in [-1,1] for v in [xmin,xmax,xmin+cut,xmax-cut,xmin+.18]]+[s*w for s in [-1,1] for _,_,w,_ in bands]))
 ys=sorted(set([24,32,bottom,bottom+.25,top-.16,top,my,my+mh]+[v for a,b,_,_ in bands for v in [a,b]]))
 parts=[]
 for x0,x1 in zip(xs,xs[1:]):
  for y0,y1 in zip(ys,ys[1:]):
   xm,ym=(x0+x1)/2,(y0+y1)/2
   a,b,width,front=next(v for v in bands if v[0]<=ym<=v[1])
   if abs(xm)>=width:continue
   # Opening has a solid back, skin only is removed. One union, no coplanar skins.
   parts.append(([x0,y0,-3.06 if opening(xm,ym) else front],[x1,y1,4],'wood',None))
 head=[]
 for e in surface_mesh(parts,True):
  lo=e['from'].copy();hi=e['to'].copy();lo[1]+=16;hi[1]+=16;f=next(iter(e['faces']));uv={f:skin[f]}
  if variant=='C' and f=='north':
   # Original, painted face island. Map whole head consistently, not per-panel repeated.
   uv={f:{'uv':[ux+(hi[0]+4)*8,uy+(32-hi[1])*8],'uv_size':[-(hi[0]-lo[0])*8,(hi[1]-lo[1])*8]}}
  head.append(cube(lo,hi,uv))
 if variant=='C':
  face=Image.new('RGBA',(64,64),palette['skin']);fd=ImageDraw.Draw(face)
  # Palette-derived pixel shading; no borrowed atlas pixels.
  fd.rectangle((0,39,6,53),fill=(241,207,212,255));fd.rectangle((57,39,63,53),fill=(241,207,212,255))
  for x in [9,42]:
   fd.rectangle((x,42,x+12,44),fill=(235,173,187,255));fd.rectangle((x+2,45,x+10,46),fill=(242,194,202,255))
   fd.rectangle((x+2,42,x+3,45),fill=(222,151,171,255));fd.rectangle((x+8,42,x+9,45),fill=(222,151,171,255))
  fd.rectangle((31,39,31,41),fill=(229,191,198,255))
  tex.paste(face,free)
 for side,sign in [('left',1),('right',-1)]:
  white=mat('eye_'+side);iris,pupil,glint=[mat('pupil_'+side,i) for i in range(3)];brow=mat('brow_'+side)
  eyez={'A':-3.88,'B':-3.80,'C':-3.86}[variant];lashz={'A':-4.055,'B':-4.025,'C':-4.025}[variant]
  by['eye_'+side]['cubes']=[box(xmin+.01,xmax-.01,bottom+.01,top-.01,eyez,eyez+.12,white,sign)]
  if variant=='A':
   # Wide irises, narrow lower white, smaller slit pupils. Two readable square highlights.
   by['pupil_'+side]['cubes']=[box(1.25,2.67,27.47,29.06,-3.945,-3.90,iris,sign),box(1.83,2.09,27.63,28.88,-3.982,-3.95,pupil,sign),box(1.48,1.82,28.60,28.96,-4.011,-3.99,glint,sign),box(2.35,2.51,27.63,27.78,-4.012,-3.99,glint,sign)]
   head +=[box(.60,3.23,top-.16,top+.13,lashz,lashz+.035,dark,sign),box(3.23,3.53,top-.04,top+.30,lashz,lashz+.035,dark,sign),box(xmin,.60,top-.19,top-.05,lashz,lashz+.035,dark,sign)]
  elif variant=='B':
   by['pupil_'+side]['cubes']=[box(1.37,2.55,27.61,28.91,-3.89,-3.83,iris,sign),box(1.83,2.10,27.68,28.76,-3.928,-3.90,pupil,sign),box(1.51,1.83,28.53,28.83,-3.953,-3.93,glint,sign)]
   head +=[box(.72,3.06,top-.15,top+.08,lashz,lashz+.065,dark,sign),box(3.06,3.26,top-.06,top+.20,lashz,lashz+.065,dark,sign)]
  else:
   # The iris, pupil, glints and subtle lower light are ONE textured plane per eye.
   tile=Image.new('RGBA',(16,16),palette['pink' if side=='left' else 'gray']);it=ImageDraw.Draw(tile);col=palette['pink' if side=='left' else 'gray'];it.rectangle((0,0,15,3),fill=tuple(int(v*.72) for v in col[:3])+(255,));it.rectangle((0,12,15,15),fill=tuple(min(255,int(v*1.16)) for v in col[:3])+(255,));it.rectangle((7,3,9,13),fill=palette['dark']);it.rectangle((3,3,6,6),fill=palette['white']);it.rectangle((12,11,13,12),fill=palette['white']);tx=ux+(64 if sign==1 else 88);ty=uy+72;tex.paste(tile,(tx,ty))
   by['pupil_'+side]['cubes']=[box(1.22,2.70,27.64,28.86,-3.929,-3.87,patch_uv(tx,ty,16,16),sign)]
   head +=[box(.52,3.17,top-.13,top+.12,lashz,lashz+.025,dark,sign),box(3.17,3.45,top-.03,top+.22,lashz,lashz+.025,dark,sign)]
  # Geometry remains on the same original facial bones; no clips or pivots edited.
  by['lid_'+side]['cubes']=[box(xmin,xmax,bottom,29.3,lashz+.055,lashz+.095,skin,sign)]
  by['brow_'+side]['cubes']=[box(.80,3.12,29.52,29.70,-4.03,-3.80 if variant=='B' else -3.98,brow,sign)]
 by['head']['cubes']=head
 cavity,tongue,teeth=[mat('mouth',i) for i in range(3)]
 mz={'A':-3.87,'B':-4.14,'C':-3.91}[variant]
 by['mouth']['cubes']=[box(-mouthw,mouthw,my,my+mh,mz,mz+.08,cavity),box(-mouthw*.70,mouthw*.7,my+mh*.62,my+mh*.82,mz-.02,mz,teeth)]
 # A small geometric lower lip on original bone, authored for current neutral scale.
 ly=25.25+(my-.03-25.25)/.8896
 by['lower_lip']['cubes']=[box(-mouthw*.88,mouthw*.88,ly,ly+.07,-4.035 if variant!='B' else -4.305,-4.016 if variant!='B' else -4.29,mat('lower_lip',1))]
 if variant=='A':by['nose']['cubes']=[box(-.15,.15,26.72,27.2,-4.16,-4,skin),box(-.23,.23,26.65,26.79,-4.24,-4,skin)]
 elif variant=='B':by['nose']['cubes']=[box(-.24,.24,26.83,27.55,-4.37,-4.10,skin),box(-.36,.36,26.63,26.94,-4.57,-4.16,skin)]
 else:by['nose']['cubes']=[box(-.14,.14,26.74,27.13,-4.16,-4,skin),box(-.25,.25,26.66,26.8,-4.25,-4,skin)]
 # Export using stock 1.23 per-face UV/outliner convention. No cube rotation extension.
 bb=copy.deepcopy(basebb);els=[];groups={}
 for b in by.values():
  gid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:geo120:'+b['name']));groups[b['name']]={'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':gid,'children':[]}
  for i,c in enumerate(b.get('cubes',[])):
   eid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:cube120:'+b['name']+str(i)))
   faces={f:{'uv':[v['uv'][0],v['uv'][1],v['uv'][0]+v['uv_size'][0],v['uv'][1]+v['uv_size'][1]],'texture':0} for f,v in c['uv'].items()}
   els.append({'name':b['name'],'type':'cube','uuid':eid,'from':c['origin'],'to':[c['origin'][j]+c['size'][j] for j in range(3)],'origin':b['pivot'],'rotation':[0,0,0],'autouv':0,'faces':faces});groups[b['name']]['children'].append(eid)
 for b in by.values():
  if 'parent'in b:groups[b['parent']]['children'].append(groups[b['name']])
 out=ROOT/variant;out.mkdir(exist_ok=True);tex.save(out/'red_mage_geo.png');bb.update(name='KelMagic 1.23.0 — Face study '+variant,elements=els,outliner=[groups['root']]);bb['textures'][0]['source']='data:image/png;base64,'+base64.b64encode((out/'red_mage_geo.png').read_bytes()).decode()
 (out/'red_mage.geo.json').write_text(json.dumps(geo,indent=2));(out/f'KelMagic-Face-{variant}.bbmodel').write_text(json.dumps(bb,indent=2));(out/'red_mage.animation.json').write_bytes((P/'art/red-mage/red_mage.animation.json').read_bytes())
 for n,b in by.items():
  assert {k:v for k,v in b.items() if k!='cubes'}=={k:v for k,v in old[n].items() if k!='cubes'}
  if n not in FACE:assert b==old[n],n
 assert bb['animations']==basebb['animations']
 # Changed texels must not touch any existing UV (C only); A/B texture exactly retained.
 import numpy as np
 changed=np.any(np.array(tex)!=np.array(texture),axis=2);assert not (changed & (np.array(used)!=0)).any()
 reports[variant]={'bones':len(by),'cubes_and_surface_quads':len(els),'face_primitives':sum(len(by[n].get('cubes',[])) for n in FACE),'clips_unchanged':len(bb['animations']),'unchanged_nonface_bones':len(by)-len(FACE),'modified_texels':int(changed.sum()),'reserved_uv':free,'head_volume_bands':bands,'eye_aperture':[xmin,xmax,bottom,top],'pipeline':'stock generate_mascot_1_23.py + isolated static geometry adapter; mod_version=1.23.0'}
(ROOT/'validation.json').write_text(json.dumps(reports,indent=2))
print(json.dumps(reports,ensure_ascii=False))
