from pathlib import Path
from PIL import Image,ImageDraw
import json,uuid,base64,shutil
P=Path(__file__).resolve().parents[1];out=P/'art/red-mage';out.mkdir(exist_ok=True)
source=Path('/data/.agent-service/files/87ccda51-289a-452c-8bf9-e8629b5b999d/Скин - red hair girl-(ru-minecraft.ru).png');ref=Path('/data/.agent-service/files/a5cdb11b-db6d-4c64-b4e6-53b495517244/tool-output-0.jpg')
if source.exists():shutil.copy2(source,out/'original-skin.png')
if ref.exists():shutil.copy2(ref,out/'concept.jpg')
src=Image.open(out/'original-skin.png').convert('RGBA');im=Image.new('RGBA',(128,128),(0,0,0,0));im.paste(src.crop((0,0,64,16)),(0,0));draw=ImageDraw.Draw(im)
teal=(25,63,66,255);dark=(14,35,39,255);gold=(211,174,87,255);skin=(248,223,225,255);hair=(128,35,35,255)
def face(x,y,w,h,color):
 x,y,w,h=map(int,(x,y,w,h));draw.rectangle((x,y,x+w-1,y+h-1),fill=color)
 for yy in range(y,y+h):
  for xx in range(x,x+w):
   if (xx*3+yy*5)%11==0:im.putpixel((xx,yy),tuple(max(0,c-7) for c in color[:3])+(255,))
def patch(u,v,w,h,d,col):
 w,h,d=map(int,(w,h,d));face(u+d,v,w,d,col);face(u+d+w,v,w,d,tuple(max(0,c-15) for c in col[:3])+(255,));face(u,v+d,d,h,tuple(max(0,c-10) for c in col[:3])+(255,));face(u+d,v+d,w,h,col);face(u+d+w,v+d,d,h,tuple(max(0,c-18) for c in col[:3])+(255,));face(u+2*d+w,v+d,w,h,col)
patch(16,16,8,12,4,teal)
# Tunic seams and a narrow leather belt, not a bulky robe.
for x in [20,27]:draw.line((x,22,x,28),fill=gold)
draw.line((22,20,25,20),fill=gold);draw.line((21,21,26,21),fill=gold)
for start,width in [(16,24)]:draw.rectangle((start,29,start+width-1,30),fill=(82,55,40,255))
draw.rectangle((23,29,24,30),fill=(52,198,192,255))
for u,v in [(40,16),(32,48)]:
 patch(u,v,3,12,4,skin)
 draw.rectangle((u+4,v,u+9,v+3),fill=teal)
 for x in range(u,u+14):
  for y in range(v+4,v+6):im.putpixel((x,y),teal)
for u,v in [(0,16),(16,48)]:
 patch(u,v,4,12,4,dark)
 for x in range(u,u+16):
  for yy in range(3,5):im.putpixel((x,v+4+yy),skin)
  for yy in range(5,12):im.putpixel((x,v+4+yy),teal if (x+yy)%4 else dark)
 for x in [u+4,u+7]:
  for yy in [6,10]:im.putpixel((x,v+4+yy),gold)
# Atlas islands for 3-D accessories and cloth-covered volume.
patch(64,0,14,1,12,teal);patch(64,14,8,3,8,dark);patch(64,25,6,2,6,teal);patch(64,34,4,2,4,teal);patch(64,116,9,1,9,gold)
patch(64,44,9,9,1,hair);patch(86,44,2,11,1,hair)
patch(64,64,3,5,1,teal);patch(78,64,3,5,1,teal);patch(92,64,8,4,1,teal)
for u in [64,78]:draw.line((u+1,65,u+1,69),fill=gold)
patch(64,80,4,5,4,teal);patch(96,80,4,5,4,teal)
patch(64,92,4,2,5,teal);patch(84,92,3,1,4,gold);patch(100,92,2,2,1,(53,185,180,255));patch(64,104,3,3,2,(88,58,42,255));patch(92,104,1,3,2,(88,58,42,255))
draw.rectangle((67,107,69,107),fill=gold)
tex=P/'src/main/resources/assets/kelmagic/textures/entity/red_mage.png';tex.parent.mkdir(exist_ok=True);im.save(tex);im.save(out/'red_mage.png');im.resize((768,768),Image.Resampling.NEAREST).save(out/'texture-preview.png')
# Unified rig descriptor feeds both the actual Java mesh and editable Blockbench asset.
parts=[]
def part(name,parent,origin,cubes,uv,rotation=(0,0,0)):
 parts.append(dict(name=name,parent=parent,origin=origin,cubes=cubes,uv=uv,rotation=rotation))
part('head','root',[0,0,0],[[-4,-8,-4,8,8,8]],[0,0])
part('hair_crown','head',[0,0,0],[[-4,-8,-4,8,8,8]],[32,0]);parts[-1]['deform']=.12
part('hair_back','head',[0,0,0],[[-4.3,-.5,3.6,8.6,8.5,.7]],[64,44])
part('hair_right','head',[0,0,0],[[-3.8,-.3,-2.7,1.6,10.2,.65]],[86,44])
part('hair_left','head',[0,0,0],[[2.2,-.3,-2.7,1.6,10.2,.65]],[86,44])
part('hat','root',[0,0,0],[[-7,-9,-6,14,1,12]],[64,0])
part('hat_crown','hat',[0,0,0],[[-4,-12,-4,8,3,8]],[64,14])
part('hat_band','hat',[0,0,0],[[-4.25,-10,-4.25,8.5,1,8.5]],[64,116])
part('hat_tier','hat',[0,0,0],[[-2.6,-14,-3,6,2,6]],[64,25])
part('hat_tip','hat',[0,0,0],[[0,-16,-2,4,2,4]],[64,34])
part('body','root',[0,0,0],[[-4,0,-2,8,12,4]],[16,16])
part('chest_left','body',[1.9,2,-1.6],[[-1.9,0,-3.9,3.8,5,4]],[64,80],(-.38,-.07,0))
part('chest_right','body',[-1.9,2,-1.6],[[-1.9,0,-3.9,3.8,5,4]],[96,80],(-.38,.07,0))
part('tunic_left','body',[2.2,10,-2.25],[[-1.5,0,-.3,3,5,.6]],[64,64])
part('tunic_right','body',[-2.2,10,-2.25],[[-1.5,0,-.3,3,5,.6]],[78,64])
part('tunic_back','body',[0,10,2.25],[[-4,0,-.3,8,4,.6]],[92,64])
part('belt_gem','body',[0,9.7,-2.25],[[-.7,-.7,-.8,1.4,1.4,.8]],[100,92])
part('pouch','body',[4.1,12.3,.4],[[-.8,0,-1.1,2.5,3,2.2]],[64,104])
part('pouch_strap','body',[4.15,9.8,.4],[[-.25,0,-1,.5,2.6,2]],[92,104])
part('right_arm','root',[-5,2,0],[[-2,-2,-2,3,12,4]],[40,16])
part('left_arm','root',[5,2,0],[[-1,-2,-2,3,12,4]],[32,48])
part('bracelet','left_arm',[0,7.2,0],[[-1.1,0,-2.1,3.2,.8,4.2]],[84,92])
part('right_leg','root',[-1.9,12,0],[[-2,0,-2,4,12,4]],[0,16])
part('left_leg','root',[1.9,12,0],[[-2,0,-2,4,12,4]],[16,48])
part('right_boot','right_leg',[0,4.7,0],[[-2.1,0,-2.1,4.2,1.2,4.2]],[64,92])
part('left_boot','left_leg',[0,4.7,0],[[-2.1,0,-2.1,4.2,1.2,4.2]],[64,92])
(out/'rig.json').write_text(json.dumps({'height':2.5,'texture_size':[128,128],'parts':parts},indent=2))
def ff(v):return (str(float(v))+'F')
lines=[]
for q in parts:
 cs=''.join('.texOffs('+','.join(map(str,q['uv']))+').addBox('+','.join(ff(v) for v in box)+')' for box in q['cubes'])
 pose='PartPose.offsetAndRotation('+','.join(ff(v) for v in q['origin']+list(q['rotation']))+')'
 lines.append('  var '+q['name']+'='+q['parent']+'.addOrReplaceChild("'+q['name']+'",CubeListBuilder.create()'+cs+','+pose+');')
java='''package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import net.minecraft.client.model.HumanoidModel;import net.minecraft.client.model.geom.*;import net.minecraft.client.model.geom.builders.*;import net.minecraft.resources.ResourceLocation;
/** Generated mesh from art/red-mage/rig.json. Base skin identity is preserved. */
public final class RedMageModel extends HumanoidModel<RedMage> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"red_mage"),"main");
 private final ModelPart chestLeft,chestRight,hairBack,tunicLeft,tunicRight;
 public RedMageModel(ModelPart root){super(root);chestLeft=body.getChild("chest_left");chestRight=body.getChild("chest_right");hairBack=head.getChild("hair_back");tunicLeft=body.getChild("tunic_left");tunicRight=body.getChild("tunic_right");}
 public static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();
'''+ '\n'.join(lines)+'''
  return LayerDefinition.create(mesh,128,128);
 }
 @Override public void setupAnim(RedMage e,float walk,float speed,float age,float yaw,float pitch){super.setupAnim(e,walk,speed,age,yaw,pitch);float partial=net.minecraft.util.Mth.clamp(age-e.tickCount,0,1);chestLeft.y=chestRight.y=2F;chestLeft.x=1.9F;chestRight.x=-1.9F;chestLeft.xRot=chestRight.xRot=-.38F;chestLeft.yRot=-.07F;chestRight.yRot=.07F;if(MageClientConfig.PHYSICS.get()){chestLeft.y+=e.chestLeft.lift(partial);chestRight.y+=e.chestRight.lift(partial);chestLeft.xRot+=e.chestLeft.pitch(partial);chestRight.xRot+=e.chestRight.pitch(partial);chestLeft.x+=e.chestLeft.sway(partial);chestRight.x+=e.chestRight.sway(partial);}hairBack.xRot=tunicLeft.xRot=tunicRight.xRot=0;if(MageClientConfig.ACCESSORIES.get()){hairBack.xRot=(float)Math.sin(age*.055)*.018F+Math.min(speed,.6F)*.08F;tunicLeft.xRot=Math.abs(leftLeg.xRot)*.13F;tunicRight.xRot=Math.abs(rightLeg.xRot)*.13F;}}
}
'''
(P/'src/main/java/dev/kelmagic/client/RedMageModel.java').write_text(java)
# Blockbench: explicit per-face UVs, real skeletal groups, same cubes and pivots.
texid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:red_mage:texture'));elements=[];groups={};world={'root':[0,0,0]}
for q in parts:
 parent=world[q['parent']];o=[parent[i]+q['origin'][i] for i in range(3)];world[q['name']]=o;gid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kelmagic:red_mage:'+q['name']));groups[q['name']]={'name':q['name'],'origin':[o[0],24-o[1],o[2]],'rotation':[q['rotation'][0]*180/3.14159265,-q['rotation'][1]*180/3.14159265,-q['rotation'][2]*180/3.14159265],'uuid':gid,'children':[]}
 for j,c in enumerate(q['cubes']):
  x,y,z,w,h,dep=c;inflate=q.get('deform',0);u,v=q['uv'];eid=str(uuid.uuid5(uuid.NAMESPACE_URL,gid+str(j)));faces={}
  uv={'north':[u+dep,v+dep,u+dep+w,v+dep+h],'south':[u+2*dep+w,v+dep,u+2*dep+2*w,v+dep+h],'west':[u,v+dep,u+dep,v+dep+h],'east':[u+dep+w,v+dep,u+2*dep+w,v+dep+h],'up':[u+dep,v,u+dep+w,v+dep],'down':[u+dep+w,v,u+dep+2*w,v+dep]}
  for name,r in uv.items():faces[name]={'uv':r,'texture':0}
  elements.append({'name':q['name'],'from':[o[0]+x-inflate,24-(o[1]+y+h)-inflate,o[2]+z-inflate],'to':[o[0]+x+w+inflate,24-(o[1]+y)+inflate,o[2]+z+dep+inflate],'origin':[o[0],24-o[1],o[2]],'uuid':eid,'faces':faces,'box_uv':False,'type':'cube','rotation':[0,0,0]});groups[q['name']]['children'].append(eid)
for q in reversed(parts):
 if q['parent']!='root':groups[q['parent']]['children'].append(groups[q['name']])
model={'meta':{'format_version':'4.10','model_format':'free','box_uv':False},'name':'KelMagic — Red Mage','resolution':{'width':128,'height':128},'elements':elements,'outliner':[groups[q['name']] for q in parts if q['parent']=='root'],'textures':[{'name':'red_mage.png','uuid':texid,'id':'0','width':128,'height':128,'source':'data:image/png;base64,'+base64.b64encode(tex.read_bytes()).decode()}]}
(out/'red_mage.bbmodel').write_text(json.dumps(model,ensure_ascii=False,indent=2))
print('Generated texture, Java mesh, articulated rig and editable Blockbench model:',len(parts),'parts')
