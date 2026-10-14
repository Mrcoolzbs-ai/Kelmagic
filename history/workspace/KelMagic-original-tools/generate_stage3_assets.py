from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic';out=a/'textures';rng=random.Random(140)
def save(im,p):f=out/p;f.parent.mkdir(parents=True,exist_ok=True);im.save(f)
def js(p,o):f=a/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps(o,indent=2)+'\n')
# Own, explicitly UV-packed articulated garment. Not imported Thaumcraft geometry.
allmodels=[]
for robe in [True,False]:
 cubes=[]
 def add(slot,bone,name,x,y,z,w,h,d,mat='cloth',pivot=(0,0,0),rot=(0,0,0)):
  cubes.append(dict(slot=slot,bone=bone,name=name,pos=(x,y,z),size=(w,h,d),mat=mat,pivot=pivot,rot=rot))
 # Hood rounded with stepped nape; actual open face, not a cube painted with a face hole.
 add('HEAD','head','crown',-4.5,-9,-4.5,9,2,9)
 add('HEAD','head','left_cheek',-4.5,-7,-4.5,1,7,9)
 add('HEAD','head','right_cheek',3.5,-7,-4.5,1,7,9)
 add('HEAD','head','nape',-3.5,-7,3.5,7,7,1)
 add('HEAD','head','hood_tail',-3.5,-9,3.5,7,5,2,pivot=(0,0,0),rot=(-.15,0,0))
 add('HEAD','head','tail_tip',-2.5,-8,4.7,5,4,2,rot=(-.3,0,0))
 add('HEAD','head','brow_thread',-3.5,-7.1,-4.7,7,1,.2,'gold')
 for s in [-1,1]:add('HEAD','head','edge'+str(s),-4.55 if s<0 else 4.35,-6,-4.65,.2,6,.3,'gold')
 if not robe:
  add('HEAD','head','iron_brow',-4.1,-8.8,-5,8,1,.7,'metal')
  add('HEAD','head','temple_left',-4.9,-6.8,-2,1,4,4,'metal',rot=(0,0,-.08))
  add('HEAD','head','temple_right',3.9,-6.8,-2,1,4,4,'metal',rot=(0,0,.08))
  add('HEAD','head','crest_seal',-.5,-9.2,-4.8,1,1,.3,'cyan')
 # Same long robe underneath both variants; plates are separate thin additions.
 add('CHEST','body','coat',-4.25,-.2,-2.35,8.5,12,4.7)
 add('CHEST','body','mantle',-4.6,0,-2.8,9.2,3,5.6)
 for s in [-1,1]:
  add('CHEST','body','lapel'+str(s),-1.45,-.2,0,2.9,9,.5,'brocade',pivot=(s*2.6,0,-2.9),rot=(0,0,s*.055))
 add('CHEST','body','sash',-4.5,8.5,-2.8,9,2,5.6,'leather')
 add('CHEST','body','buckle',-1,8.8,-3.4,2,1.5,.6,'gold')
 add('CHEST','body','crystal',-.35,9,-3.55,.7,.9,.2,'cyan')
 add('CHEST','body','scroll_case',-4.6,9.3,-3.6,1.4,4,1.2,'leather',rot=(0,0,.09))
 add('CHEST','body','scroll_band',-4.65,10,-3.65,1.5,.6,1.3,'gold',rot=(0,0,.09))
 add('CHEST','body','folio',2.9,9.5,-3.3,2.2,3.1,.8,'leather')
 add('CHEST','body','folio_clasp',3.7,10.5,-3.5,.6,.9,.2,'gold')
 for s in [-1,1]:
  # Garment panels hang from waist, not rigid trouser cuboids. Their children keep independent animation.
  add('CHEST','body','front'+str(s),-2.2,0,0,4.4,10.5,.55,'brocade',pivot=(s*2.25,11,-2.9),rot=(-.10,s*.035,s*.055))
  add('CHEST','body','front_hem'+str(s),-2.2,9.6,-.05,4.4,.7,.65,'gold',pivot=(s*2.25,11,-2.9),rot=(-.10,s*.035,s*.055))
  add('CHEST','body','rear'+str(s),-2.2,0,0,4.4,10,.55,'cloth',pivot=(s*2.2,11,2.8),rot=(.13,-s*.04,s*.06))
  add('CHEST','body','side'+str(s),-.2,0,-2.6,.6,10.2,5.2,'cloth',pivot=(s*4.1,11,0),rot=(0,0,-s*.15))
 for bone,s in [('right_arm',-1),('left_arm',1)]:
  cx=-1 if s<0 else 1
  add('CHEST',bone,'sleeve',cx-2.2,-2.1,-2.2,4.4,8,4.4)
  add('CHEST',bone,'bell_cuff',cx-2.5,5.6,-2.5,5,3.8,5,'brocade')
  add('CHEST',bone,'cuff_thread',cx-2.55,8.8,-2.55,5.1,.7,5.1,'gold')
  if not robe:
   for k in range(3):
    add('CHEST',bone,'lamella'+str(k),cx-2.8+k*.18,-2.9+k*1.1,-2.75+k*.1,5.6-k*.3,1.5,5.5-k*.2,'metal',rot=(0,0,s*.12))
   add('CHEST',bone,'shoulder_seal',cx-.6,-3.1,-2.8,1.2,1.4,.3,'gold')
   add('CHEST',bone,'bracer',cx-2.35,4.2,-2.65,4.7,3,.6,'metal')
 # Armor torso uses segmented chest and back plates, robe remains visible below/around them.
 if not robe:
  add('CHEST','body','sternum',-2.9,1.5,-3.6,5.8,3.7,.7,'metal')
  for s in [-1,1]:
   add('CHEST','body','rib'+str(s),-1.8,0,0,3.6,2.4,.6,'metal',pivot=(s*2.3,5.1,-3.1),rot=(0,s*.15,s*.06))
   add('CHEST','body','hip_plate'+str(s),-1.8,0,0,3.6,3.5,.6,'metal',pivot=(s*3.15,10.6,-3.45),rot=(-.13,0,s*.10))
  add('CHEST','body','spine_plate',-3.2,1,3,6.4,6.7,.6,'metal')
  add('CHEST','body','seal',-.6,2.4,-4.4,1.2,1.5,.2,'cyan')
 add('LEGS','body','trouser_waist',-4.1,10.5,-2.15,8.2,1.4,4.3,'leather')
 for bone in ['right_leg','left_leg']:
  add('LEGS',bone,'trouser',-2.08,0,-2.12,4.16,10.9,4.24)
  if not robe:add('LEGS',bone,'greave',-1.5,4.3,-2.5,3,4.6,.5,'metal')
  add('FEET',bone,'boot',-2.15,8,-2.35,4.3,4.3,4.7,'leather')
  add('FEET',bone,'turned_toe',-2.15,10.1,-3.35,4.3,2.2,1.7,'leather')
  add('FEET',bone,'boot_binding',-2.23,8.9,-2.43,4.46,.7,4.86,'gold')
  if not robe:add('FEET',bone,'toe_plate',-1.7,10,-3.55,3.4,1.6,.35,'metal')
 # Allocate a distinct UV island per model piece with margin; paints detailed surface rather than shared grid.
 base=Image.new('RGBA',(256,256));ov=Image.new('RGBA',(256,256));bx=2;by=2;rowh=0
 for q in cubes:
  w,h,d=[max(1,math.ceil(v)) for v in q['size']];q['uvsize']=(w,h,d);pw=2*(w+d)+2;ph=h+d+2
  if bx+pw>254:bx=2;by+=rowh+2;rowh=0
  assert by+ph<256,(q['name'],by)
  q['uv']=(bx,by);rowh=max(rowh,ph)
  im=base if q['mat'] in ['cloth','brocade'] else ov;dr=ImageDraw.Draw(im)
  palette={'cloth':(194,188,204) if robe else (42,35,64),'brocade':(209,200,218) if robe else (61,45,83),'metal':(115,129,159),'gold':(179,133,60),'leather':(49,31,44),'cyan':(71,222,223)}
  cr,cg,cb=palette[q['mat']]
  for yy in range(ph):
   for xx in range(pw):
    shade=round(12*math.sin(xx*.7)-yy*.6) if q['mat'] in ['cloth','brocade'] else round(30*(1-yy/max(ph,1))+(6 if xx%6<3 else -3))
    noise=rng.choice([-2,0,1]);im.putpixel((bx+xx,by+yy),(max(0,min(255,cr+shade+noise)),max(0,min(255,cg+shade+noise)),max(0,min(255,cb+shade+noise)),255))
  fx=bx+int(d);fy=by+int(d);fw=max(1,int(w));fh=max(1,int(h))
  if q['mat']=='metal' and fw>=3 and fh>=2:
   dr.rectangle((fx,fy,fx+fw-1,fy+fh-1),outline='#46465c');dr.line((fx+1,fy,fx+fw-2,fy),fill='#c4cdde')
   if fw>4:dr.line((fx+1,fy+fh-2,fx+fw-2,fy+fh-2),fill='#bd9452')
  if q['mat']=='brocade' and fw>=2:
   for yy in range(fy,fy+fh,3):dr.point((fx,yy),fill='#ebd9b8' if robe else '#d1ab62')
   dr.line((fx+fw-1,fy,fx+fw-1,fy+fh-1),fill='#af9879' if robe else '#957847')
  if q['mat']=='gold':dr.line((bx,by,bx+pw-1,by),fill='#f7df99');dr.line((bx,by+ph-1,bx+pw-1,by+ph-1),fill='#5b3c2b')
  bx+=pw+2
 for layer in [1,2]:
  if robe:save(base,Path(f'models/armor/mage_robes_layer_{layer}.png'));save(ov,Path(f'models/armor/mage_robes_layer_{layer}_overlay.png'))
  else:
   full=base.copy();full.alpha_composite(ov);save(full,Path(f'models/armor/arcane_plate_layer_{layer}.png'))
 allmodels.append((robe,cubes))
# Build Java from our structured blueprint. Original reference Java never becomes a dependency.
java='''package dev.kelmagic.client;
import java.util.*;
import dev.kelmagic.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.*;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MagicArmorModels {
 private static final Map<String,GarmentModel> MODELS=new HashMap<>();
 @SubscribeEvent public static void register(RegisterClientExtensionsEvent e){for(var i:MagicContent.PLATE_ITEMS)e.registerItem(extension(false),i);for(var i:MagicContent.ROBE_ITEMS)e.registerItem(extension(true),i);}
 private static IClientItemExtensions extension(boolean robe){return new IClientItemExtensions(){
  @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){var m=MODELS.computeIfAbsent(robe+":"+slot,k->new GarmentModel(bake(robe,slot)));m.animateCloth(entity);return m;}
  @Override public int getDefaultDyeColor(ItemStack stack){return robe?0xFF000000|net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack,0x635875):0xFFFFFFFF;}
 };}
 private static class GarmentModel extends HumanoidModel<LivingEntity>{
  GarmentModel(ModelPart root){super(root);}
  void animateCloth(LivingEntity e){
   float motion=Math.min(.8f,e.walkAnimation.speed())*Math.abs((float)Math.sin(e.walkAnimation.position()*.6662f));
   for(int sign:new int[]{-1,1}){
    for(String n:new String[]{"front","front_hem"})if(body.hasChild(n+sign))body.getChild(n+sign).xRot=-.10f-motion*.45f;
    if(body.hasChild("rear"+sign))body.getChild("rear"+sign).xRot=.13f+motion*.35f;
   }
  }
 }
 private static void piece(PartDefinition bone,String name,int u,int v,float x,float y,float z,float w,float h,float d,float px,float py,float pz,float rx,float ry,float rz){float iw=(float)Math.ceil(w),ih=(float)Math.ceil(h),id=(float)Math.ceil(d);bone.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x*iw/w,y*ih/h,z*id/d,iw,ih,id),PartPose.offsetAndRotation(px,py,pz,rx,ry,rz));}
 private static LayerDefinition mesh(boolean robe,EquipmentSlot slot){
  var mesh=new MeshDefinition();var root=mesh.getRoot();
  var head=root.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
  var body=root.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);
  var right_arm=root.addOrReplaceChild("right_arm",CubeListBuilder.create(),PartPose.offset(-5,2,0));var left_arm=root.addOrReplaceChild("left_arm",CubeListBuilder.create(),PartPose.offset(5,2,0));
  var right_leg=root.addOrReplaceChild("right_leg",CubeListBuilder.create(),PartPose.offset(-1.9f,12,0));var left_leg=root.addOrReplaceChild("left_leg",CubeListBuilder.create(),PartPose.offset(1.9f,12,0));
'''
def fl(x):return f'{float(x):.4f}f'
for robe,cubes in allmodels:
 java+=f'  if(robe=={str(robe).lower()}) {{\n'
 for slot in ['HEAD','CHEST','LEGS','FEET']:
  java+=f'   if(slot==EquipmentSlot.{slot}){{\n'
  for q in cubes:
   if q['slot']!=slot:continue
   u,v=q['uv'];args=','.join(map(fl,(*q['pos'],*q['size'],*q['pivot'],*q['rot'])))
   java+=f'    piece({q["bone"]},"{q["name"]}",{u},{v},{args});\n'
  java+='   }\n'
 java+='  }\n'
java+='  return LayerDefinition.create(mesh,256,256);\n }\n private static ModelPart bake(boolean robe,EquipmentSlot slot){var root=mesh(robe,slot).bakeRoot();\n'
for robe,cubes in allmodels:
 java+=f'  if(robe=={str(robe).lower()}) {{\n'
 for q in cubes:
  scales=','.join(fl(v/u) for v,u in zip(q['size'],q['uvsize']))
  java+=f'   if(slot==EquipmentSlot.{q["slot"]})scale(root.getChild("{q["bone"]}").getChild("{q["name"]}"),{scales});\n'
 java+='  }\n'
java+=' return root; }\n private static void scale(ModelPart p,float x,float y,float z){p.xScale=x;p.yScale=y;p.zScale=z;}\n private MagicArmorModels(){}\n}\n'
(r/'src/main/java/dev/kelmagic/client/MagicArmorModels.java').write_text(java)
(r/'art/generated/garment-blueprint-1.5.0.json').write_text(json.dumps([{'robe':b,'pieces':q} for b,q in allmodels],indent=2))
# Detailed new hand pixel-art icons. Mana core intentionally unchanged.
edge='#141222';gold='#bc8a48';shine='#f4d9a0';cyan='#8efbf0'
for n in ['magic_ingot','magic_cloth','archivists_codex','ancient_manuscript']+[f'{p}_{s}' for p in ['arcane','mage'] for s in ['helmet','chestplate','leggings','boots']]+['manuscript_'+w for w in ['mina','tuna','sila','pir','no','lu']]:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);overlay=None
 if n=='magic_ingot':
  # A forged metal bar, not a recessed electronic panel: broad top bevel and crystalline inclusions.
  d.polygon([(2,15),(9,7),(25,7),(30,13),(26,24),(8,27),(2,22)],fill=edge)
  d.polygon([(4,15),(10,9),(24,9),(27,13),(22,19),(6,21)],fill='#b9b4d4')
  d.polygon([(6,21),(22,19),(26,22),(9,25),(4,21)],fill='#66617e')
  d.polygon([(22,19),(27,13),(26,21),(23,23)],fill='#85809c')
  d.line([(5,15),(11,10),(23,10)],fill='#eee7f7',width=2)
  d.line([(9,24),(23,21)],fill='#a59abb')
  d.line([(12,12),(14,14),(13,17),(17,15),(19,12)],fill='#8880a6')
  for x,y in [(10,15),(16,13),(21,15)]:
   d.point((x,y),fill='#b4faf3');d.point((x+1,y+1),fill='#718fbc')
  d.point((8,20),fill='#ded7ea');d.point((20,10),fill='#ffffff')
 elif n=='magic_cloth':
  d.polygon([(5,4),(25,5),(29,11),(26,29),(4,27),(1,20)],fill=edge)
  d.polygon([(6,6),(24,7),(27,11),(24,26),(5,24),(3,20)],fill='#3e3765')
  d.polygon([(6,6),(12,9),(10,22),(5,24),(3,19)],fill='#75678c');d.polygon([(14,8),(18,9),(16,23),(11,24)],fill='#54456f');d.polygon([(20,9),(24,10),(22,25),(17,24)],fill='#83729b')
  d.line([(6,6),(23,7),(26,11)],fill=shine);d.line([(5,23),(23,25)],fill=gold,width=2)
  for x,y in [(9,12),(17,15),(21,18)]:d.line([(x-1,y),(x,y-2),(x+1,y),(x,y+2),(x-1,y)],fill=cyan)
  for x in range(6,24,3):d.point((x,24),fill=shine)
 elif n=='archivists_codex':
  d.polygon([(6,3),(26,4),(29,8),(28,28),(7,30),(2,26),(2,7)],fill=edge);d.rectangle((5,5,25,27),fill='#5d3040');d.rectangle((7,6,23,25),fill='#813e45');d.rectangle((3,7,5,25),fill=gold)
  d.line([(8,7),(23,7),(23,24),(8,24),(8,7)],fill=gold);d.line([(7,28),(26,26)],fill=shine)
  d.polygon([(17,9),(20,8),(21,11),(18,17),(13,21),(13,17)],fill='#d8e3d5');d.line([(13,21),(19,10)],fill=gold);d.rectangle((23,15,28,18),fill=gold);d.point((25,16),fill=cyan)
  for x,y in [(8,7),(21,7),(8,22),(21,22)]:d.rectangle((x,y,x+1,y+1),fill=shine)
 elif n=='ancient_manuscript':
  d.polygon([(5,4),(25,3),(27,8),(25,27),(8,29),(4,25)],fill=edge);d.polygon([(7,5),(24,5),(24,25),(8,27),(6,23)],fill='#c4a171');d.rectangle((7,6,22,22),fill='#e4cca0');d.rectangle((5,4,24,7),fill='#a47b54');d.line([(7,5),(23,5)],fill=shine)
  for y in [10,13,16,19]:d.line((10,y,21,y-1),fill='#97765c')
  d.ellipse((11,19,21,28),fill='#713647',outline=gold);d.line([(15,22),(17,22),(16,25)],fill=shine)
 elif n.startswith('manuscript_'):
  w=n.split('_')[1];d.polygon([(6,2),(22,2),(27,7),(26,29),(7,30),(3,25)],fill=edge);d.polygon([(7,4),(21,4),(24,8),(24,27),(7,28),(5,24)],fill='#e5cda0');d.polygon([(21,4),(21,9),(25,9)],fill='#ad825b')
  d.line((8,23,21,23),fill='#a27859');d.line((8,25,18,25),fill='#a27859')
  # Verified canonical Kel glyph letters only; no imported Thaumcraft marks.
  from importlib.machinery import SourceFileLoader
  # word glyphs from existing font mask, composed letter by letter using source glyph files
  glyphs=[]
  for c in w:
   f=out/'glyph'/f'{c}.png'
   if f.exists():
    g=Image.open(f).convert('RGBA');bbox=g.getbbox();g=g.crop(bbox) if bbox else g;g.thumbnail((4,8));glyphs.append(g)
  xx=max(7,16-sum(g.width+1 for g in glyphs)//2)
  for g in glyphs:
   tint=Image.new('RGBA',g.size,'#555a60');tint.putalpha(g.getchannel('A'));im.alpha_composite(tint,(xx,11));xx+=g.width+1
 else:
  robe=n.startswith('mage');color='#c9c2d5' if robe else '#362a51';mid='#e4d9e8' if robe else '#635177';steel='#8695b3';slot=n.split('_')[1]
  if slot=='helmet':
   d.polygon([(5,26),(3,13),(7,5),(15,2),(24,6),(28,13),(26,27),(22,28),(21,13),(11,12),(10,27)],fill=edge)
   d.polygon([(7,24),(5,13),(9,7),(15,4),(22,8),(26,14),(24,25),(23,12),(10,10),(8,24)],fill=color);d.line([(9,8),(15,5),(21,8)],fill=mid,width=2)
   if not robe:d.polygon([(7,10),(15,7),(24,11),(24,14),(15,11),(7,13)],fill=steel);d.rectangle((6,15,8,21),fill='#586887');d.rectangle((23,15,25,21),fill='#586887')
  elif slot=='chestplate':
   d.polygon([(1,9),(8,5),(12,7),(20,7),(24,5),(31,9),(29,23),(24,21),(26,30),(6,30),(8,21),(3,23)],fill=edge)
   d.polygon([(3,10),(9,7),(13,9),(19,9),(23,7),(29,10),(27,20),(23,18),(24,28),(8,28),(9,18),(5,20)],fill=color)
   for x in [10,15,21]:d.line((x,11,x-1,27),fill=mid)
   if not robe:
    d.polygon([(9,11),(16,10),(23,11),(22,16),(16,18),(10,16)],fill=steel);d.polygon([(2,9),(9,6),(11,9),(8,13),(3,13)],fill=steel);d.polygon([(21,9),(24,6),(30,9),(29,13),(24,13)],fill=steel)
    d.rectangle((11,21,14,24),fill='#8390a9');d.rectangle((18,21,21,24),fill='#8390a9')
  elif slot=='leggings':
   d.polygon([(5,4),(26,4),(27,29),(17,29),(16,20),(14,29),(4,29)],fill=edge);d.polygon([(7,6),(24,6),(24,27),(18,27),(17,15),(14,15),(12,27),(6,27)],fill=color)
   d.line((8,8,8,26),fill=mid);d.line((21,8,21,26),fill=mid)
   if not robe:d.rectangle((7,16,11,22),fill=steel);d.rectangle((19,16,23,22),fill=steel)
  else:
   for x in [2,17]:
    d.polygon([(x+4,3),(x+11,3),(x+11,21),(x+13,25),(x+12,29),(x,29),(x,25),(x+3,21)],fill=edge);d.rectangle((x+5,5,x+9,24),fill=color);d.polygon([(x+4,23),(x+10,23),(x+11,26),(x+2,27)],fill=mid)
    if not robe:d.rectangle((x+4,24,x+10,26),fill=steel)
  overlay=Image.new('RGBA',(32,32));od=ImageDraw.Draw(overlay)
  if slot=='helmet':od.line([(9,11),(15,8),(23,12)],fill=gold);od.point((16,9),fill=cyan)
  elif slot=='chestplate':
   od.line((10,19,23,19),fill=gold,width=2);od.rectangle((15,18,17,21),fill=shine);od.point((16,19),fill=cyan)
   od.line((5,20,8,20),fill=gold);od.line((25,20,28,20),fill=gold);od.line((9,27,23,27),fill=gold)
   od.rectangle((9,20,11,23),fill='#624334');od.rectangle((20,20,22,23),fill='#624334')
  elif slot=='leggings':od.line((8,8,23,8),fill=gold);od.point((15,8),fill=cyan)
  else:
   for x in [2,17]:od.line((x+5,18,x+10,18),fill=gold);od.line((x+2,27,x+10,27),fill=gold)
  if robe:save(overlay,Path(f'item/{n}_overlay.png'))
  else:im.alpha_composite(overlay)
 save(im,Path(f'item/{n}.png'));textures={'layer0':f'kelmagic:item/{n}'}
 if n.startswith('mage'):textures['layer1']=f'kelmagic:item/{n}_overlay'
 js(Path(f'models/item/{n}.json'),{'parent':'minecraft:item/generated','textures':textures})
# Animated purple fire, original 16-frame sheet, diagonal wisps and brighter inner tongues.
for kind in [0,1]:
 im=Image.new('RGBA',(32,32*16))
 for frame in range(16):
  for x in range(32):
   for y in range(32):
    heat=0;edgefade=0
    for k in range(5):
     height=18+int(8*math.sin(k*1.7+frame*.4+kind));progress=1-(31-y)/max(1,height)
     if progress<0:continue
     center=k*7+2+3*math.sin((31-y)*.18+frame*.4+k)
     radius=1+3*progress;distance=abs(x-center)
     if distance<radius:heat=max(heat,progress);edgefade=max(edgefade,1-distance/radius)
    if edgefade>0:
     core=max(0,(edgefade-.5)*2)*heat
     im.putpixel((x,y+32*frame),(int(116+120*core+22*heat),int(13+177*core),int(205+50*core),255))
 save(im,Path(f'block/magic_fire_{kind}.png'));(out/f'block/magic_fire_{kind}.png.mcmeta').write_text(json.dumps({'animation':{'frametime':2,'interpolate':False}}))
js(Path('models/block/magic_fire.json'),{'ambientocclusion':False,'render_type':'minecraft:cutout','textures':{'particle':'kelmagic:block/magic_fire_0','flame':'kelmagic:block/magic_fire_0','cross':'kelmagic:block/magic_fire_1'},'elements':[{'from':[0,0,7.5],'to':[16,16,8.5],'rotation':{'origin':[8,8,8],'axis':'y','angle':45,'rescale':True},'shade':False,'faces':{'north':{'uv':[0,0,16,16],'texture':'#flame'},'south':{'uv':[0,0,16,16],'texture':'#flame'}}},{'from':[7.5,0,0],'to':[8.5,16,16],'rotation':{'origin':[8,8,8],'axis':'y','angle':45,'rescale':True},'shade':False,'faces':{'east':{'uv':[0,0,16,16],'texture':'#cross'},'west':{'uv':[0,0,16,16],'texture':'#cross'}}}]})
js(Path('blockstates/magic_fire.json'),{'variants':{'':{'model':'kelmagic:block/magic_fire'}}})
# Heart mote small soft diamond, color comes from own particle provider.
im=Image.new('RGBA',(16,16));
for y in range(16):
 for x in range(16):
  dist=abs(x-7.5)+abs(y-7.5);alpha=int(255*max(0,1-dist/9)**1.5);im.putpixel((x,y),(255,255,255,alpha))
save(im,Path('particle/heart_mote.png'));js(Path('particles/heart_mote.json'),{'textures':['kelmagic:heart_mote']})
# Tiled decorated vial caps/frame. Gold endpoints, transparent glass interior.
im=Image.new('RGBA',(32,36));d=ImageDraw.Draw(im)
for x,w in [(0,10),(10,12)]:
 d.polygon([(x+2,1),(x+w-3,1),(x+w-1,5),(x+w-1,12),(x+w-3,16),(x+2,16),(x,12),(x,5)],fill='#142838',outline='#a67834')
 d.line((x+2,2,x+w-3,2),fill='#f2d497');d.line((x+2,15,x+w-3,15),fill='#664222');d.rectangle((x+3,5,x+w-4,12),fill='#7acae2');d.line((x+3,6,x+w-4,6),fill='#dcffff');d.point((x+1,8),fill='#fff0b1')
d.line((0,20,7,20),fill='#d3ba78');d.line((0,21,7,21),fill='#6b5842');d.line((0,31,7,31),fill='#546f82');d.line((0,32,7,32),fill='#c5a164');d.line((0,34,7,34),fill='#674e32');d.line((1,22,6,22),fill='#a4e2f0');d.line((2,19,3,19),fill='#f4d9a0');d.point((4,33),fill='#f4d9a0')
save(im,Path('gui/mana_tube.png'))
# Own 360x216 leather binding and weathered parchment, no external artwork.
im=Image.new('RGBA',(360,216));d=ImageDraw.Draw(im);d.rounded_rectangle((2,0,357,215),radius=10,fill='#2a1720',outline='#b98a4d',width=2);d.rounded_rectangle((6,4,353,210),radius=7,fill='#623941',outline='#8d6050',width=2)
for y in range(5,211):
 for x in range(7,353):
  if rng.randrange(35)==0:d.point((x,y),fill='#744751')
d.polygon([(17,39),(169,37),(178,45),(178,185),(165,195),(19,192),(13,181)],fill='#b1915e');d.polygon([(182,44),(194,37),(344,39),(347,180),(339,195),(190,193),(182,186)],fill='#b1915e')
for left,right in [(20,174),(186,340)]:
 for y in range(41,188):
  for x in range(left,right):
   border=min(x-left,right-x,y-40,188-y);noise=rng.choice([-3,-1,0,2]);age=max(0,12-border)*2;im.putpixel((x,y),(max(0,244-age+noise),max(0,223-age+noise),max(0,175-age+noise),255))
 for k in range(12):
  x=rng.randrange(left,right);y=rng.randrange(45,186);d.ellipse((x,y,x+2,y+1),fill='#c7ad79')
d.rectangle((177,38,183,191),fill='#614832');d.line((179,40,179,186),fill='#36271d');d.line((184,44,184,186),fill='#dcc393')
for x in [8,350]:
 for y in range(14,205,8):d.point((x,y),fill='#d6aa65')
for x,y in [(9,9),(345,9),(9,198),(345,198)]:d.polygon([(x,y+3),(x+3,y),(x+6,y+3),(x+3,y+6)],fill='#c59a54');d.point((x+3,y+3),fill='#e9cb90')
save(im,Path('gui/codex.png'))
# Own QA contact sheet, all new icons except unchanged core included for comparison.
names=['mana_core','magic_ingot','magic_cloth','archivists_codex','ancient_manuscript']+[f'{p}_{s}' for p in ['arcane','mage'] for s in ['helmet','chestplate','leggings','boots']]+['manuscript_'+w for w in ['mina','tuna','sila','pir','no','lu']]
sheet=Image.new('RGB',(720,400),'#161726');sd=ImageDraw.Draw(sheet)
for i,n in enumerate(names):
 im=Image.open(out/f'item/{n}.png').convert('RGBA')
 if n.startswith('mage'):
  for yy in range(32):
   for xx in range(32):
    rr,g,b,aa=im.getpixel((xx,yy));im.putpixel((xx,yy),(rr*99//255,g*88//255,b*117//255,aa))
  im.alpha_composite(Image.open(out/f'item/{n}_overlay.png'))
 x=8+(i%6)*120;y=5+(i//6)*98;im=im.resize((72,72),Image.Resampling.NEAREST);sheet.paste(im,(x,y),im);sd.text((x,y+76),n[:18],fill='#d9bd85')
sheet.save(r/'art/generated/stage3-icons.png')
print('garment pieces',[(b,len(c)) for b,c in allmodels]);print('Original art and UI created')
