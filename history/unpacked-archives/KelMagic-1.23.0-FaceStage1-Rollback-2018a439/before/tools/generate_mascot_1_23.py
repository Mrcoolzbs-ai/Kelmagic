from pathlib import Path
import json,math,copy,uuid,base64
from PIL import Image,ImageDraw
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic';art=P/'art/red-mage';tex=Image.new('RGBA',(256,256));tex.paste(Image.open(art/'red_mage.png'),(0,0));d=ImageDraw.Draw(tex)
# New individual facial and prop UV islands; body keeps its familiar atlas.
for u,v,col in [(130,0,(244,225,226,255)),(136,0,(252,250,241,255)),(142,0,(54,111,112,255)),(148,0,(245,216,219,255)),(154,0,(77,29,38,255)),(160,0,(172,97,103,255)),(130,16,(76,38,52,255)),(146,16,(224,192,123,255)),(162,16,(58,96,99,255)),(178,16,(240,225,191,255)),(194,16,(137,44,44,255)),(130,48,(235,209,194,255)),(146,48,(254,233,221,255)),(162,48,(207,130,153,255))]:d.rectangle((u,v,u+14,v+14),fill=col)
for yy in [19,22,25]:d.line((179,yy,189,yy),fill=(156,126,91,255),width=1)
# Keep original eye whites but remove baked pupils so their direction genuinely changes.
for x in range(9,15):
 for y in range(12,16):tex.putpixel((x,y),(250,224,226,255))
for x in [151,154]:tex.putpixel((x,53),(40,45,49,255))
tex.putpixel((153,55),(207,130,153,255))
tex.save(A/'textures/entity/red_mage_geo.png');tex.save(art/'red_mage_geo.png')
rig=json.load(open(art/'rig.json'));bones=[];world={'root':[0,0,0]}
bones.append({'name':'root','pivot':[0,0,0]})
# Convert Y-down humanoid into Bedrock geometry with articulated forearms and shins.
for q in rig['parts']:
 par=world[q['parent']];o=[par[i]+q['origin'][i] for i in range(3)];world[q['name']]=o;pv=[o[0],24-o[1],o[2]];b={'name':q['name'],'parent':q['parent'],'pivot':pv};rot=q.get('rotation',[0,0,0]);b['rotation']=[-rot[0]*180/math.pi,rot[1]*180/math.pi,-rot[2]*180/math.pi];cs=[]
 for c in q['cubes']:
  x,y,z,w,h,depth=c
  cs.append({'origin':[o[0]+x,24-o[1]-y-h,o[2]+z],'size':[w,h,depth],'uv':q['uv']})
 if q['name'].endswith('_arm'):
  c=cs[0];c['origin'][1]+=6;c['size'][1]=6
 if q['name'].endswith('_leg'):
  c=cs[0];c['origin'][1]+=6;c['size'][1]=6
 b['cubes']=cs;bones.append(b)
# Accessory descendants follow actual knees and elbows, rather than rigid sticks.
for side in ['right','left']:
 o=world[side+'_arm'];x=o[0];b={'name':side+'_forearm','parent':side+'_arm','pivot':[x,18,0],'cubes':[{'origin':[x+(-2 if side=='right' else -1),12,-2],'size':[3,6,4],'uv':[40,22] if side=='right' else [32,54]}]};bones.append(b)
 o=world[side+'_leg'];x=o[0];bones.append({'name':side+'_shin','parent':side+'_leg','pivot':[x,6,0],'cubes':[{'origin':[x-2,0,-2],'size':[4,6,4],'uv':[0,22] if side=='right' else [16,54]}]})
for b in bones:
 if b['name']=='bracelet':b['parent']='left_forearm'
 if b['name'] in ['left_boot','right_boot']:b['parent']=b['name'].replace('_boot','_shin')
# Upper body articulation uses the hips; head, hat and arms follow torso lean.
for b in bones:
 if b['name']=='body':b['pivot']=[0,12,0]
 if b['name'] in ['head','left_arm','right_arm']:b['parent']='body'
 if b['name']=='hat':b['parent']='head'
# Face parts stand just proud of the front surface; lid scales are independent.
def bone(name,parent,pivot,origin,size,uv,rotation=None):
 b={'name':name,'parent':parent,'pivot':pivot,'cubes':[{'origin':origin,'size':size,'uv':uv}]}
 if rotation:b['rotation']=rotation
 bones.append(b)
for side,x in [('left',1.8),('right',-1.8)]:
 bone('eye_'+side,'head',[x,28.3,-4.09],[x-.8,27.85,-4.09],[1.6,.9,.04],[136,0])
 bone('pupil_'+side,'head',[x,28.3,-4.15],[x-.26,28.05,-4.15],[.52,.55,.04],[142,0])
 bone('lid_'+side,'head',[x,28.8,-4.21],[x-.84,27.8,-4.21],[1.68,1.05,.04],[148,0])
 bone('brow_'+side,'head',[x,29.05,-4.18],[x-.85,29.0,-4.18],[1.7,.18,.04],[194,16])
bone('mouth','head',[0,26.3,-4.16],[-.72,26.04,-4.16],[1.44,.48,.05],[154,0])
bone('lower_lip','head',[0,26.0,-4.21],[-.74,25.98,-4.21],[1.48,.15,.04],[160,0])
# Three trailing sections bend individually.
back=next(b for b in bones if b['name']=='hair_back');back['cubes'][0]['size'][1]=3;back['cubes'][0]['origin'][1]=21.5
bone('hair_mid','hair_back',[0,21.5,4],[ -4.3,18.7,3.6],[8.6,2.8,.7],[64,47]);bone('hair_tip','hair_mid',[0,18.7,4],[-4.3,15,3.6],[8.6,3.7,.7],[64,50])
bone('book','root',[0,17,-7],[-4.5,16.8,-10],[9,.6,6],[130,16]);bone('book_page','book',[0,17.3,-7],[-4.2,17.4,-9.7],[8.4,.2,5.4],[178,16])
# Kitten is a real alternate rendered form of the same NPC, not a second entity.
bone('cat','root',[0,0,0],[-2.8,0,-3],[5.6,4.6,6],[130,48]);bone('cat_head','cat',[0,4,-2],[-2.7,3.2,-4.4],[5.4,4.3,4.2],[146,48])
for side,x in [('left',1.8),('right',-1.8)]:bone('cat_ear_'+side,'cat_head',[x,6.9,-2.3],[x-.7,6.5,-2.5],[1.4,2,1.3],[162,48]);bone('cat_paw_'+side,'cat',[x,1,-2.5],[x-.65,0,-3.6],[1.3,1.3,2],[146,48])
bone('cat_tail','cat',[0,2,2.5],[-.55,1.5,2.4],[1.1,1.1,5.5],[130,48],[-25,0,0]);bone('cat_bow','cat_head',[0,7,-2.5],[-1.5,7.1,-2.9],[3,.7,.5],[162,48])
from mascot_surface_fixes_1_21 import fix_rig,fix_poses,surface_mesh
from mascot_surface_fixes_1_23 import refine_rig,refine_poses
tex=fix_rig(bones,tex,A);tex=refine_rig(bones,tex,A);tex.save(A/"textures/entity/red_mage_geo.png");tex.save(art/"red_mage_geo.png")
geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.kelmagic.red_mage','texture_width':1024,'texture_height':1024,'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':bones}]}
(A/'geo').mkdir(exist_ok=True);(A/'animations').mkdir(exist_ok=True);(A/'geo/red_mage.geo.json').write_text(json.dumps(geo,indent=2));(art/'red_mage.geo.json').write_text(json.dumps(geo,indent=2))
# Every clip carries meaningful keyframes. Facial/physics layers are added procedurally.
anims={}
def clip(name,length,b,loop=True):anims['animation.red_mage.'+name]={'loop':loop,'animation_length':length,'bones':b}
def rot(v):return {'rotation':v}
def pos(v):return {'position':v}
def wave(axis,amp):return {str(t):[amp[i]*axis for i in range(3)] for t,axis in [(0,0),(.25,1),(.5,0),(.75,-1),(1,0)]}
def steps(length,amp,knee,bob):
 out={}
 for side,sign in [('left',1),('right',-1)]:
  out[side+'_leg']={'rotation':{str(length*f):[sign*a,0,0] for f,a in [(0,amp),(.25,0),(.5,-amp),(.75,0),(1,amp)]}}
  out[side+'_shin']={'rotation':{str(length*f):[a,0,0] for f,a in [(0,0),(.25,knee if sign==1 else 0),(.5,0),(.75,knee if sign==-1 else 0),(1,0)]}}
  out[side+'_arm']={'rotation':{str(length*f):[-sign*a*.7,0,sign*4] for f,a in [(0,amp),(.25,0),(.5,-amp),(.75,0),(1,amp)]}}
  out[side+'_forearm']=rot([-15 if knee<35 else -45,0,0])
 out['root']={'position':{str(length*f):[0,v,0] for f,v in [(0,0),(.25,bob),(.5,0),(.75,bob),(1,0)]}}
 return out
clip('idle',4,{'body':{'rotation':{'0':[0,0,0],'2':[0,0,1.2],'4':[0,0,0]}},'head':{'rotation':{'0':[0,0,0],'2':[0,-3,0],'4':[0,0,0]}}})
clip('walk',1.5,steps(1.5,24,26,.35));clip('run',.65,steps(.65,48,62,.85))
clip('crouch',2,{'root':pos([0,-4,0]),'body':rot([18,0,0]),'left_leg':rot([-32,0,-8]),'right_leg':rot([-32,0,8]),'left_shin':rot([54,0,0]),'right_shin':rot([54,0,0]),'left_arm':rot([-18,0,-10]),'right_arm':rot([-18,0,10])})
clip('jump',.8,{'body':rot([-8,0,0]),'left_arm':rot([-65,0,-18]),'right_arm':rot([-65,0,18]),'left_leg':rot([-22,0,0]),'right_leg':rot([14,0,0]),'left_shin':rot([38,0,0]),'right_shin':rot([22,0,0])})
clip('fall',1,{'body':rot([8,0,0]),'left_arm':rot([-18,0,-38]),'right_arm':rot([-18,0,38]),'left_leg':rot([14,0,-5]),'right_leg':rot([-14,0,5])})
clip('fly',2.4,{'root':{'position':{'0':[0,0,0],'1.2':[0,1,0],'2.4':[0,0,0]}},'body':rot([-12,0,0]),'left_arm':rot([-35,0,-28]),'right_arm':rot([-35,0,28]),'left_leg':rot([8,0,-5]),'right_leg':rot([-18,0,5]),'left_shin':rot([18,0,0]),'right_shin':rot([32,0,0])})
clip('swim',1.4,{'root':rot([80,0,0]),'left_arm':{'rotation':{'0':[-140,0,-20],'.7':[-30,0,-60],'1.4':[-140,0,-20]}},'right_arm':{'rotation':{'0':[-30,0,60],'.7':[-140,0,20],'1.4':[-30,0,60]}},'left_leg':{'rotation':{'0':[20,0,0],'.7':[-20,0,0],'1.4':[20,0,0]}},'right_leg':{'rotation':{'0':[-20,0,0],'.7':[20,0,0],'1.4':[-20,0,0]}}})
clip('climb',1.6,{'left_arm':{'rotation':{'0':[-145,0,-8],'.8':[-70,0,-8],'1.6':[-145,0,-8]}},'right_arm':{'rotation':{'0':[-70,0,8],'.8':[-145,0,8],'1.6':[-70,0,8]}},'left_leg':{'rotation':{'0':[-48,0,0],'.8':[-5,0,0],'1.6':[-48,0,0]}},'right_leg':{'rotation':{'0':[-5,0,0],'.8':[-48,0,0],'1.6':[-5,0,0]}},'left_shin':rot([45,0,0]),'right_shin':rot([45,0,0])})
clip('talk',4,{'right_arm':{'rotation':{'0':[-15,0,10],'1':[-45,-12,25],'2':[-25,0,12],'3':[-35,8,18],'4':[-15,0,10]}},'right_forearm':rot([-55,0,0]),'head':{'rotation':{'0':[0,0,0],'1':[3,-4,0],'2':[-2,3,0],'4':[0,0,0]}}},False)
clip('cast',3,{'left_arm':{'rotation':{'0':[0,0,0],'.5':[-65,0,-20],'2':[-90,0,-25],'3':[0,0,0]}},'right_arm':{'rotation':{'0':[0,0,0],'.5':[-75,0,25],'2':[-110,0,18],'3':[0,0,0]}},'head':{'rotation':{'0':[0,0,0],'1':[-12,0,0],'3':[0,0,0]}}},False)
# Eight standalone personality idles, with entrance and exit back into the default pose.
idleposes={'stretch':{'left_arm':[-165,0,-15],'right_arm':[-165,0,15],'body':[-10,0,0]},'read':{'left_arm':[-35,12,-10],'right_arm':[-35,-12,10],'left_forearm':[-55,0,0],'right_forearm':[-55,0,0],'head':[18,0,0]},'hat_play':{'right_arm':[-135,-10,30],'right_forearm':[-55,0,0],'head':[8,-10,0]},'hat_adjust':{'left_arm':[-125,0,-24],'left_forearm':[-75,0,0],'head':[7,0,0]},'inspect_nails':{'right_arm':[-50,-20,22],'right_forearm':[-75,0,0],'head':[10,-16,-5]},'look_around':{'head':[0,32,0],'body':[0,8,0]},'conjure_orb':{'left_arm':[-55,0,-25],'left_forearm':[-75,0,0],'head':[15,15,0]},'yawn':{'right_arm':[-65,0,18],'right_forearm':[-110,0,0],'head':[-12,0,0]}}
for name,pose in idleposes.items():
 out={b:{'rotation':{'0':[0,0,0],'1':v,'4':v,'5':[0,0,0]}} for b,v in pose.items()}
 if name=='read':out['book']={'scale':{'0':[0,0,0],'.8':[0,0,0],'1.2':[1,1,1],'4':[1,1,1],'4.8':[0,0,0],'5':[0,0,0]}}
 if name=='hat_play':out['hat']={'position':{'0':[0,0,0],'1':[0,2,0],'2':[0,4,0],'3':[0,2,0],'4':[0,1,0],'5':[0,0,0]},'rotation':{'0':[0,0,0],'1':[0,20,-10],'3':[0,-20,10],'5':[0,0,0]}}
 clip(name,5,out,False)
# Ten throne scenes, all based on an actual seated rig; separate bent knees/forearms.
seat={'root':pos([0,-4,0]),'left_leg':rot([-90,0,-5]),'right_leg':rot([-90,0,5]),'left_shin':rot([90,0,0]),'right_shin':rot([90,0,0]),'left_arm':rot([-12,0,-13]),'right_arm':rot([-12,0,13]),'left_forearm':rot([-72,0,0]),'right_forearm':rot([-72,0,0])}
variants=[('regal',{}),('crossed',{'left_leg':rot([-95,-24,-14]),'left_shin':rot([84,0,0]),'head':rot([0,-8,-3])}),('chin_rest',{'right_arm':rot([-62,0,18]),'right_forearm':rot([-122,0,0]),'head':rot([10,-12,-8])}),('side_lounge',{'body':rot([0,0,-18]),'head':rot([8,-10,8]),'left_arm':rot([-35,0,-38])}),('recline',{'root':pos([0,-3,-10]),'body':rot([-32,0,0]),'head':rot([18,0,0]),'left_leg':rot([-105,0,-8]),'right_leg':rot([-100,0,8]),'left_shin':rot([65,0,0]),'right_shin':rot([65,0,0])}),('read',{'left_arm':rot([-35,12,-10]),'right_arm':rot([-35,-12,10]),'left_forearm':rot([-55,0,0]),'right_forearm':rot([-55,0,0]),'head':rot([18,0,0]),'book':{'position':[0,1,0],'scale':{'0':[0,0,0],'1':[1,1,1],'7':[1,1,1],'8':[0,0,0]}}}),('hat',{'right_arm':rot([-125,0,24]),'right_forearm':rot([-70,0,0]),'head':rot([8,0,0])}),('bored',{'head':{'rotation':{'0':[8,-18,-4],'4':[8,18,4],'8':[8,-18,-4]}},'right_shin':{'rotation':{'0':[75,0,0],'2':[100,0,0],'4':[75,0,0],'6':[100,0,0],'8':[75,0,0]}}}),('stretch',{'left_arm':{'rotation':{'0':[-12,0,-13],'2':[-155,0,-20],'6':[-155,0,-20],'8':[-12,0,-13]}},'right_arm':{'rotation':{'0':[-12,0,13],'2':[-155,0,20],'6':[-155,0,20],'8':[-12,0,13]}}}),('kitten',{'root':pos([0,0,0]),'cat':{'position':[0,9,0]},'cat_tail':{'rotation':{'0':[-25,-15,0],'4':[-25,15,0],'8':[-25,-15,0]}},'cat_head':{'rotation':{'0':[0,-15,0],'4':[5,15,0],'8':[0,-15,0]}}})]
for name,more in variants:
 out=copy.deepcopy(seat);out.update(more);clip('throne_'+name,8,out)
fix_poses(anims);refine_poses(anims)
ani={'format_version':'1.8.0','animations':anims};(A/'animations/red_mage.animation.json').write_text(json.dumps(ani,indent=2));(art/'red_mage.animation.json').write_text(json.dumps(ani,indent=2))
# Detailed two-block throne: metal scrollwork, studded red upholstery, gem, crown, arms, paws.
for name,col in [('throne_gold',(190,139,50)),('throne_velvet',(134,27,45)),('throne_wood',(47,27,29)),('throne_gem',(40,123,124))]:
 im=Image.new('RGB',(32,32),col);dr=ImageDraw.Draw(im)
 for y in range(32):
  for x in range(32):
   n=((x*17+y*11)%13)-6;im.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in col))
 if name=='throne_velvet':
  for y in range(4,32,8):
   for x in range(4+(4 if y%16 else 0),32,8):dr.line((x-4,y,x,y+4,x+4,y,x,y-4,x-4,y),fill=(97,16,31));dr.rectangle((x,y,x+1,y+1),fill=(224,178,79))
 if name=='throne_gold':dr.line((0,0,31,0),fill=(247,215,137));dr.line((0,31,31,31),fill=(94,62,26))
 im.save(A/f'textures/block/{name}.png')
parts=[]
def cube(a,b,t,rot=None):parts.append((a,b,t,rot))
cube([0,0,0],[16,2,18],'gold');cube([1,2,1],[15,7,17],'wood');cube([2,6.7,5.9],[14,7,16.5],'gold');cube([2,7,5.9],[14,9,16.5],'velvet');cube([2,9,17],[14,28,18],'wood');cube([3,10,16.55],[13,26,17.05],'velvet')
for x in [0,14]:
 cube([x,2,1],[x+2,14,17],'gold');cube([x-.05,14,2],[x+2.05,15.5,13],'velvet');cube([x,11,16.5],[x+2,29,18],'gold');cube([x+.25,28,16.65],[x+1.75,31,17.85],'gold');cube([x+.25,7,1],[x+1.75,9,2],'gem')
for y,w in [(26,12),(28,10),(29.5,6),(31,2)]:cube([8-w/2,y,16.5],[8+w/2,min(32,y+1.5),18],'gold')
for x in [3,5,7,9,11]:
 for y in [12,16,20,24]:cube([x,y,16.38],[x+.45,y+.45,16.58],'gold')
cube([6,27,16.25],[10,29.5,16.65],'gem');cube([7,29.4,16.5],[9,32,17.5],'gold')
for x in [3,11]:cube([x,4,0],[x+2,5.5,1.5],'gold')
for upper in [False,True]:
 elements=surface_mesh(parts,upper)
 model={'parent':'minecraft:block/block','textures':{t:'kelmagic:block/throne_'+t for t in ['gold','velvet','wood','gem']},'elements':elements};model['textures']['particle']='kelmagic:block/throne_gold'
 if not upper:model['display']={'gui':{'rotation':[30,225,0],'translation':[0,-3,0],'scale':[.55,.55,.55]},'firstperson_righthand':{'rotation':[0,45,0],'translation':[0,0,0],'scale':[.35,.35,.35]},'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2.5,0],'scale':[.375,.375,.375]}}
 (A/f'models/block/mage_throne_{"upper" if upper else "lower"}.json').write_text(json.dumps(model,indent=2))
variants={}
for face,yaw in [('north',0),('east',90),('south',180),('west',270)]:
 for half in ['lower','upper']:variants[f'facing={face},half={half}']={'model':'kelmagic:block/mage_throne_'+half,'y':yaw}
(A/'blockstates/mage_throne.json').write_text(json.dumps({'variants':variants},indent=2));(A/'models/item/mage_throne.json').write_text(json.dumps({'parent':'kelmagic:block/mage_throne_lower'}))
# Inventory model includes the upper half, scaled to a normal slot.
model=json.loads((A/'models/block/mage_throne_lower.json').read_text());up=json.loads((A/'models/block/mage_throne_upper.json').read_text())
for e in up['elements']:e['from'][1]+=16;e['to'][1]+=16;model['elements'].append(e)
model['display']['gui']={'rotation':[30,225,0],'translation':[0,-4,0],'scale':[.42,.42,.42]};model['display']['firstperson_righthand']['scale']=[.28,.28,.28]
(A/'models/item/mage_throne.json').write_text(json.dumps(model,indent=2))
silk={'condition':'minecraft:match_tool','predicate':{'items':'#minecraft:pickaxes','predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}
base_conditions=[{'condition':'minecraft:block_state_property','block':'kelmagic:mage_throne','properties':{'half':'lower'}},{'condition':'minecraft:survives_explosion'}]
loot={'type':'minecraft:block','pools':[{'rolls':1,'conditions':base_conditions,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'kelmagic:mage_throne','conditions':[silk]},{'type':'minecraft:item','name':'minecraft:gold_block'}]}]},{'rolls':1,'conditions':base_conditions+[{'condition':'minecraft:inverted','term':silk}],'entries':[{'type':'minecraft:item','name':'minecraft:red_wool'}]}]};(R/'data/kelmagic/loot_table/blocks/mage_throne.json').write_text(json.dumps(loot,indent=2))
recipe={'type':'minecraft:crafting_shaped','pattern':['GAG','GCG','P P'],'key':{'G':{'item':'minecraft:gold_ingot'},'A':{'item':'minecraft:amethyst_shard'},'C':{'item':'minecraft:red_wool'},'P':{'tag':'minecraft:planks'}},'result':{'id':'kelmagic:mage_throne','count':1}};(R/'data/kelmagic/recipe/mage_throne.json').write_text(json.dumps(recipe,indent=2))
for lang,title in [('ru_ru','Трон чародейки'),('en_us','Sorceress Throne')]:
 p=A/f'lang/{lang}.json';j=json.load(open(p));j['block.kelmagic.mage_throne']=title;p.write_text(json.dumps(j,ensure_ascii=False,indent=2))
# Editable Blockbench outliner & clips using explicit UV faces, no generated frozen render.
els=[];groups={};children={b['name']:[] for b in bones}
for b in bones:
 gid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:geo120:'+b['name']));groups[b['name']]={'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':gid,'children':[]}
 for i,c in enumerate(b.get('cubes',[])):
  w,h,dep=c['size'];u,v=c['uv'] if isinstance(c['uv'],list) else [0,0];eid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:cube120:'+b['name']+str(i)));uvs={'north':[u+dep,v+dep,u+dep+w,v+dep+h],'east':[u,v+dep,u+dep,v+dep+h],'south':[u+2*dep+w,v+dep,u+2*dep+2*w,v+dep+h],'west':[u+dep+w,v+dep,u+2*dep+w,v+dep+h],'up':[u+dep,v,u+dep+w,v+dep],'down':[u+dep+w,v,u+dep+2*w,v+dep]}
  if isinstance(c['uv'],dict):uvs={f:[v['uv'][0],v['uv'][1],v['uv'][0]+v['uv_size'][0],v['uv'][1]+v['uv_size'][1]] for f,v in c['uv'].items()}
  els.append({'name':b['name'],'type':'cube','uuid':eid,'from':c['origin'],'to':[c['origin'][j]+c['size'][j] for j in range(3)],'origin':b['pivot'],'rotation':[0,0,0],'autouv':0,'faces':{face:{'uv':uv,'texture':0} for face,uv in uvs.items()}});groups[b['name']]['children'].append(eid)
for b in bones:
 if 'parent' in b:groups[b['parent']]['children'].append(groups[b['name']])
bbani=[]
for name,a in anims.items():
 animators={}
 for bn,tracks in a['bones'].items():
  keys=[]
  for channel,vals in tracks.items():
   if isinstance(vals,list):vals={'0':vals}
   for t,v in vals.items():keys.append({'channel':channel,'time':float(t),'interpolation':'linear','data_points':[{'x':str(v[0]),'y':str(v[1]),'z':str(v[2])}],'uuid':str(uuid.uuid4())})
  animators[groups[bn]['uuid']]={'name':bn,'type':'bone','keyframes':keys}
 bbani.append({'name':name,'loop':'loop' if a['loop'] else 'once','length':a['animation_length'],'uuid':str(uuid.uuid4()),'animators':animators})
bb={'meta':{'format_version':'4.10','model_format':'geckolib_model','box_uv':False},'name':'KelMagic mascot1.23','resolution':{'width':1024,'height':1024},'elements':els,'outliner':[groups['root']],'textures':[{'name':'red_mage_geo.png','id':'0','width':1024,'height':1024,'uv_width':1024,'uv_height':1024,'source':'data:image/png;base64,'+base64.b64encode((art/'red_mage_geo.png').read_bytes()).decode()}],'animations':bbani};(art/'red_mage_gecko.bbmodel').write_text(json.dumps(bb,indent=2))
print('Generated',len(bones),'bones',len(anims),'clips including10throne scenes; two-half ornate throne')
