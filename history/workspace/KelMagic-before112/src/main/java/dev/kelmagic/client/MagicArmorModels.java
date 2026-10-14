package dev.kelmagic.client;
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
  @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){var m=MODELS.computeIfAbsent(robe+":"+slot,k->new GarmentModel(bake(robe,slot)));m.animateCloth(entity);m.trimTexture=stack.has(net.minecraft.core.component.DataComponents.TRIM)?TrimAppearance.texture(stack,robe,slot):null;return m;}
  @Override public int getDefaultDyeColor(ItemStack stack){return robe?0xFF000000|net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack,0x635875):0xFFFFFFFF;}
 };}
 public static class GarmentModel extends HumanoidModel<LivingEntity>{
  public net.minecraft.resources.ResourceLocation trimTexture;
  GarmentModel(ModelPart root){super(root);}
  void animateCloth(LivingEntity e){
   float motion=Math.min(.8f,e.walkAnimation.speed())*Math.abs((float)Math.sin(e.walkAnimation.position()*.6662f));
   for(int sign:new int[]{-1,1}){
    for(String n:new String[]{"front","front_hem"})if(body.hasChild(n+sign))body.getChild(n+sign).xRot=-.10f-motion*.45f;
    if(body.hasChild("rear"+sign))body.getChild("rear"+sign).xRot=.13f+motion*.35f;
   }
  }
 }
 private static void piece(PartDefinition bone,String name,int u,int v,float x,float y,float z,float w,float h,float d,float px,float py,float pz,float rx,float ry,float rz){float iw=(float)Math.ceil(w),ih=(float)Math.ceil(h),id=(float)Math.ceil(d);bone.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).mirror(name.equals("lapel1") || name.equals("front1")).addBox(x*iw/w,y*ih/h,z*id/d,iw,ih,id),PartPose.offsetAndRotation(px,py,pz,rx,ry,rz));}
 private static LayerDefinition mesh(boolean robe,EquipmentSlot slot){
  var mesh=new MeshDefinition();var root=mesh.getRoot();
  var head=root.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
  var body=root.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);
  var right_arm=root.addOrReplaceChild("right_arm",CubeListBuilder.create(),PartPose.offset(-5,2,0));var left_arm=root.addOrReplaceChild("left_arm",CubeListBuilder.create(),PartPose.offset(5,2,0));
  var right_leg=root.addOrReplaceChild("right_leg",CubeListBuilder.create(),PartPose.offset(-1.9f,12,0));var left_leg=root.addOrReplaceChild("left_leg",CubeListBuilder.create(),PartPose.offset(1.9f,12,0));
  if(robe==true) {
   if(slot==EquipmentSlot.HEAD){
    piece(head,"crown",2,2,-4.5000f,-9.0000f,-4.5000f,9.0000f,2.0000f,9.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"left_cheek",42,2,-4.5000f,-7.0000f,-4.5000f,1.0000f,7.0000f,9.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"right_cheek",66,2,3.5000f,-7.0000f,-4.5000f,1.0000f,7.0000f,9.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"nape",90,2,-3.5000f,-7.0000f,3.5000f,7.0000f,7.0000f,1.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"hood_tail",110,2,-3.5000f,-9.0000f,3.5000f,7.0000f,5.0000f,2.0000f,0.0000f,0.0000f,0.0000f,-0.1500f,0.0000f,0.0000f);
    piece(head,"tail_tip",132,2,-2.5000f,-8.0000f,4.7000f,5.0000f,4.0000f,2.0000f,0.0000f,0.0000f,0.0000f,-0.3000f,0.0000f,0.0000f);
    piece(head,"brow_thread",150,2,-3.5000f,-7.1000f,-4.7000f,7.0000f,1.0000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"edge-1",170,2,-4.5500f,-6.0000f,-4.6500f,0.2000f,6.0000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"edge1",178,2,4.3500f,-6.0000f,-4.6500f,0.2000f,6.0000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
   if(slot==EquipmentSlot.CHEST){
    piece(body,"coat",186,2,-4.2500f,-0.2000f,-2.3500f,8.5000f,12.0000f,4.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"mantle",218,2,-4.6000f,0.0000f,-2.8000f,9.2000f,3.0000f,5.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"lapel-1",2,23,-1.4500f,-0.2000f,0.0000f,2.9000f,9.0000f,0.5000f,-2.6000f,0.0000f,-2.9000f,0.0000f,0.0000f,-0.0550f);
    piece(body,"lapel1",14,23,-1.4500f,-0.2000f,0.0000f,2.9000f,9.0000f,0.5000f,2.6000f,0.0000f,-2.9000f,0.0000f,0.0000f,0.0550f);
    piece(body,"sash",26,23,-4.5000f,8.5000f,-2.8000f,9.0000f,2.0000f,5.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"buckle",60,23,-1.0000f,8.8000f,-3.4000f,2.0000f,1.5000f,0.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"crystal",70,23,-0.3500f,9.0000f,-3.5500f,0.7000f,0.9000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"scroll_case",78,23,-4.6000f,9.3000f,-3.6000f,1.4000f,4.0000f,1.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0900f);
    piece(body,"scroll_band",90,23,-4.6500f,10.0000f,-3.6500f,1.5000f,0.6000f,1.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0900f);
    piece(body,"folio",102,23,2.9000f,9.5000f,-3.3000f,2.2000f,3.1000f,0.8000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"folio_clasp",114,23,3.7000f,10.5000f,-3.5000f,0.6000f,0.9000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"front-1",122,23,-2.2000f,0.0000f,0.0000f,4.4000f,10.5000f,0.5500f,-2.2500f,11.0000f,-2.9000f,-0.1000f,-0.0350f,-0.0550f);
    piece(body,"front_hem-1",138,23,-2.2000f,9.6000f,-0.0500f,4.4000f,0.7000f,0.6500f,-2.2500f,11.0000f,-2.9000f,-0.1000f,-0.0350f,-0.0550f);
    piece(body,"rear-1",154,23,-2.2000f,0.0000f,0.0000f,4.4000f,10.0000f,0.5500f,-2.2000f,11.0000f,2.8000f,0.1300f,0.0400f,-0.0600f);
    piece(body,"side-1",170,23,-0.2000f,0.0000f,-2.6000f,0.6000f,10.2000f,5.2000f,-4.1000f,11.0000f,0.0000f,0.0000f,0.0000f,0.1500f);
    piece(body,"front1",188,23,-2.2000f,0.0000f,0.0000f,4.4000f,10.5000f,0.5500f,2.2500f,11.0000f,-2.9000f,-0.1000f,0.0350f,0.0550f);
    piece(body,"front_hem1",204,23,-2.2000f,9.6000f,-0.0500f,4.4000f,0.7000f,0.6500f,2.2500f,11.0000f,-2.9000f,-0.1000f,0.0350f,0.0550f);
    piece(body,"rear1",220,23,-2.2000f,0.0000f,0.0000f,4.4000f,10.0000f,0.5500f,2.2000f,11.0000f,2.8000f,0.1300f,-0.0400f,0.0600f);
    piece(body,"side1",236,23,-0.2000f,0.0000f,-2.6000f,0.6000f,10.2000f,5.2000f,4.1000f,11.0000f,0.0000f,0.0000f,0.0000f,-0.1500f);
    piece(right_arm,"sleeve",2,44,-3.2000f,-2.1000f,-2.2000f,4.4000f,8.0000f,4.4000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_arm,"bell_cuff",26,44,-3.5000f,5.6000f,-2.5000f,5.0000f,3.8000f,5.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_arm,"cuff_thread",50,44,-3.5500f,8.8000f,-2.5500f,5.1000f,0.7000f,5.1000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"sleeve",78,44,-1.2000f,-2.1000f,-2.2000f,4.4000f,8.0000f,4.4000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"bell_cuff",102,44,-1.5000f,5.6000f,-2.5000f,5.0000f,3.8000f,5.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"cuff_thread",126,44,-1.5500f,8.8000f,-2.5500f,5.1000f,0.7000f,5.1000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
   if(slot==EquipmentSlot.LEGS){
    piece(body,"trouser_waist",154,44,-4.1000f,10.5000f,-2.1500f,8.2000f,1.4000f,4.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"trouser",186,44,-2.0800f,0.0000f,-2.1200f,4.1600f,10.9000f,4.2400f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"trouser",26,64,-2.0800f,0.0000f,-2.1200f,4.1600f,10.9000f,4.2400f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
   if(slot==EquipmentSlot.FEET){
    piece(right_leg,"boot",210,44,-2.1500f,8.0000f,-2.3500f,4.3000f,4.3000f,4.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"turned_toe",234,44,-2.1500f,10.1000f,-3.3500f,4.3000f,2.2000f,1.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"boot_binding",2,64,-2.2300f,8.9000f,-2.4300f,4.4600f,0.7000f,4.8600f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"boot",50,64,-2.1500f,8.0000f,-2.3500f,4.3000f,4.3000f,4.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"turned_toe",74,64,-2.1500f,10.1000f,-3.3500f,4.3000f,2.2000f,1.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"boot_binding",92,64,-2.2300f,8.9000f,-2.4300f,4.4600f,0.7000f,4.8600f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
  }
  if(robe==false) {
   if(slot==EquipmentSlot.HEAD){
    piece(head,"crown",2,2,-4.5000f,-9.0000f,-4.5000f,9.0000f,2.0000f,9.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"left_cheek",42,2,-4.5000f,-7.0000f,-4.5000f,1.0000f,7.0000f,9.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"right_cheek",66,2,3.5000f,-7.0000f,-4.5000f,1.0000f,7.0000f,9.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"nape",90,2,-3.5000f,-7.0000f,3.5000f,7.0000f,7.0000f,1.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"hood_tail",110,2,-3.5000f,-9.0000f,3.5000f,7.0000f,5.0000f,2.0000f,0.0000f,0.0000f,0.0000f,-0.1500f,0.0000f,0.0000f);
    piece(head,"tail_tip",132,2,-2.5000f,-8.0000f,4.7000f,5.0000f,4.0000f,2.0000f,0.0000f,0.0000f,0.0000f,-0.3000f,0.0000f,0.0000f);
    piece(head,"brow_thread",150,2,-3.5000f,-7.1000f,-4.7000f,7.0000f,1.0000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"edge-1",170,2,-4.5500f,-6.0000f,-4.6500f,0.2000f,6.0000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"edge1",178,2,4.3500f,-6.0000f,-4.6500f,0.2000f,6.0000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"iron_brow",186,2,-4.1000f,-8.8000f,-5.0000f,8.0000f,1.0000f,0.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(head,"temple_left",208,2,-4.9000f,-6.8000f,-2.0000f,1.0000f,4.0000f,4.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,-0.0800f);
    piece(head,"temple_right",222,2,3.9000f,-6.8000f,-2.0000f,1.0000f,4.0000f,4.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0800f);
    piece(head,"crest_seal",236,2,-0.5000f,-9.2000f,-4.8000f,1.0000f,1.0000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
   if(slot==EquipmentSlot.CHEST){
    piece(body,"coat",2,22,-4.2500f,-0.2000f,-2.3500f,8.5000f,12.0000f,4.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"mantle",34,22,-4.6000f,0.0000f,-2.8000f,9.2000f,3.0000f,5.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"lapel-1",70,22,-1.4500f,-0.2000f,0.0000f,2.9000f,9.0000f,0.5000f,-2.6000f,0.0000f,-2.9000f,0.0000f,0.0000f,-0.0550f);
    piece(body,"lapel1",82,22,-1.4500f,-0.2000f,0.0000f,2.9000f,9.0000f,0.5000f,2.6000f,0.0000f,-2.9000f,0.0000f,0.0000f,0.0550f);
    piece(body,"sash",94,22,-4.5000f,8.5000f,-2.8000f,9.0000f,2.0000f,5.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"buckle",128,22,-1.0000f,8.8000f,-3.4000f,2.0000f,1.5000f,0.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"crystal",138,22,-0.3500f,9.0000f,-3.5500f,0.7000f,0.9000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"scroll_case",146,22,-4.6000f,9.3000f,-3.6000f,1.4000f,4.0000f,1.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0900f);
    piece(body,"scroll_band",158,22,-4.6500f,10.0000f,-3.6500f,1.5000f,0.6000f,1.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0900f);
    piece(body,"folio",170,22,2.9000f,9.5000f,-3.3000f,2.2000f,3.1000f,0.8000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"folio_clasp",182,22,3.7000f,10.5000f,-3.5000f,0.6000f,0.9000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"front-1",190,22,-2.2000f,0.0000f,0.0000f,4.4000f,10.5000f,0.5500f,-2.2500f,11.0000f,-2.9000f,-0.1000f,-0.0350f,-0.0550f);
    piece(body,"front_hem-1",206,22,-2.2000f,9.6000f,-0.0500f,4.4000f,0.7000f,0.6500f,-2.2500f,11.0000f,-2.9000f,-0.1000f,-0.0350f,-0.0550f);
    piece(body,"rear-1",222,22,-2.2000f,0.0000f,0.0000f,4.4000f,10.0000f,0.5500f,-2.2000f,11.0000f,2.8000f,0.1300f,0.0400f,-0.0600f);
    piece(body,"side-1",238,22,-0.2000f,0.0000f,-2.6000f,0.6000f,10.2000f,5.2000f,-4.1000f,11.0000f,0.0000f,0.0000f,0.0000f,0.1500f);
    piece(body,"front1",2,43,-2.2000f,0.0000f,0.0000f,4.4000f,10.5000f,0.5500f,2.2500f,11.0000f,-2.9000f,-0.1000f,0.0350f,0.0550f);
    piece(body,"front_hem1",18,43,-2.2000f,9.6000f,-0.0500f,4.4000f,0.7000f,0.6500f,2.2500f,11.0000f,-2.9000f,-0.1000f,0.0350f,0.0550f);
    piece(body,"rear1",34,43,-2.2000f,0.0000f,0.0000f,4.4000f,10.0000f,0.5500f,2.2000f,11.0000f,2.8000f,0.1300f,-0.0400f,0.0600f);
    piece(body,"side1",50,43,-0.2000f,0.0000f,-2.6000f,0.6000f,10.2000f,5.2000f,4.1000f,11.0000f,0.0000f,0.0000f,0.0000f,-0.1500f);
    piece(right_arm,"sleeve",68,43,-3.2000f,-2.1000f,-2.2000f,4.4000f,8.0000f,4.4000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_arm,"bell_cuff",92,43,-3.5000f,5.6000f,-2.5000f,5.0000f,3.8000f,5.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_arm,"cuff_thread",116,43,-3.5500f,8.8000f,-2.5500f,5.1000f,0.7000f,5.1000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_arm,"lamella0",144,43,-3.8000f,-2.9000f,-2.7500f,5.6000f,1.5000f,5.5000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,-0.1200f);
    piece(right_arm,"lamella1",172,43,-3.6200f,-1.8000f,-2.6500f,5.3000f,1.5000f,5.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,-0.1200f);
    piece(right_arm,"lamella2",200,43,-3.4400f,-0.7000f,-2.5500f,5.0000f,1.5000f,5.1000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,-0.1200f);
    piece(right_arm,"shoulder_seal",226,43,-1.6000f,-3.1000f,-2.8000f,1.2000f,1.4000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_arm,"bracer",236,43,-3.3500f,4.2000f,-2.6500f,4.7000f,3.0000f,0.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"sleeve",2,64,-1.2000f,-2.1000f,-2.2000f,4.4000f,8.0000f,4.4000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"bell_cuff",26,64,-1.5000f,5.6000f,-2.5000f,5.0000f,3.8000f,5.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"cuff_thread",50,64,-1.5500f,8.8000f,-2.5500f,5.1000f,0.7000f,5.1000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"lamella0",78,64,-1.8000f,-2.9000f,-2.7500f,5.6000f,1.5000f,5.5000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.1200f);
    piece(left_arm,"lamella1",106,64,-1.6200f,-1.8000f,-2.6500f,5.3000f,1.5000f,5.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.1200f);
    piece(left_arm,"lamella2",134,64,-1.4400f,-0.7000f,-2.5500f,5.0000f,1.5000f,5.1000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.1200f);
    piece(left_arm,"shoulder_seal",160,64,0.4000f,-3.1000f,-2.8000f,1.2000f,1.4000f,0.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_arm,"bracer",170,64,-1.3500f,4.2000f,-2.6500f,4.7000f,3.0000f,0.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"sternum",186,64,-2.9000f,1.5000f,-3.6000f,5.8000f,3.7000f,0.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"rib-1",204,64,-1.8000f,0.0000f,0.0000f,3.6000f,2.4000f,0.6000f,-2.3000f,5.1000f,-3.1000f,0.0000f,-0.1500f,-0.0600f);
    piece(body,"hip_plate-1",218,64,-1.8000f,0.0000f,0.0000f,3.6000f,3.5000f,0.6000f,-3.1500f,10.6000f,-3.4500f,-0.1300f,0.0000f,-0.1000f);
    piece(body,"rib1",232,64,-1.8000f,0.0000f,0.0000f,3.6000f,2.4000f,0.6000f,2.3000f,5.1000f,-3.1000f,0.0000f,0.1500f,0.0600f);
    piece(body,"hip_plate1",2,81,-1.8000f,0.0000f,0.0000f,3.6000f,3.5000f,0.6000f,3.1500f,10.6000f,-3.4500f,-0.1300f,0.0000f,0.1000f);
    piece(body,"spine_plate",16,81,-3.2000f,1.0000f,3.0000f,6.4000f,6.7000f,0.6000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(body,"seal",36,81,-0.6000f,2.4000f,-4.4000f,1.2000f,1.5000f,0.2000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
   if(slot==EquipmentSlot.LEGS){
    piece(body,"trouser_waist",46,81,-4.1000f,10.5000f,-2.1500f,8.2000f,1.4000f,4.3000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"trouser",78,81,-2.0800f,0.0000f,-2.1200f,4.1600f,10.9000f,4.2400f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"greave",102,81,-1.5000f,4.3000f,-2.5000f,3.0000f,4.6000f,0.5000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"trouser",194,81,-2.0800f,0.0000f,-2.1200f,4.1600f,10.9000f,4.2400f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"greave",218,81,-1.5000f,4.3000f,-2.5000f,3.0000f,4.6000f,0.5000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
   if(slot==EquipmentSlot.FEET){
    piece(right_leg,"boot",114,81,-2.1500f,8.0000f,-2.3500f,4.3000f,4.3000f,4.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"turned_toe",138,81,-2.1500f,10.1000f,-3.3500f,4.3000f,2.2000f,1.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"boot_binding",156,81,-2.2300f,8.9000f,-2.4300f,4.4600f,0.7000f,4.8600f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(right_leg,"toe_plate",180,81,-1.7000f,10.0000f,-3.5500f,3.4000f,1.6000f,0.3500f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"boot",230,81,-2.1500f,8.0000f,-2.3500f,4.3000f,4.3000f,4.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"turned_toe",2,101,-2.1500f,10.1000f,-3.3500f,4.3000f,2.2000f,1.7000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"boot_binding",20,101,-2.2300f,8.9000f,-2.4300f,4.4600f,0.7000f,4.8600f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
    piece(left_leg,"toe_plate",44,101,-1.7000f,10.0000f,-3.5500f,3.4000f,1.6000f,0.3500f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f,0.0000f);
   }
  }
  return LayerDefinition.create(mesh,256,256);
 }
 private static ModelPart bake(boolean robe,EquipmentSlot slot){var root=mesh(robe,slot).bakeRoot();
  if(robe==true) {
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("crown"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("left_cheek"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("right_cheek"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("nape"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("hood_tail"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("tail_tip"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("brow_thread"),1.0000f,1.0000f,0.2000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("edge-1"),0.2000f,1.0000f,0.3000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("edge1"),0.2000f,1.0000f,0.3000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("coat"),0.9444f,1.0000f,0.9400f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("mantle"),0.9200f,1.0000f,0.9333f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("lapel-1"),0.9667f,1.0000f,0.5000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("lapel1"),0.9667f,1.0000f,0.5000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("sash"),1.0000f,1.0000f,0.9333f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("buckle"),1.0000f,0.7500f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("crystal"),0.7000f,0.9000f,0.2000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("scroll_case"),0.7000f,1.0000f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("scroll_band"),0.7500f,0.6000f,0.6500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("folio"),0.7333f,0.7750f,0.8000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("folio_clasp"),0.6000f,0.9000f,0.2000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front-1"),0.8800f,0.9545f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front_hem-1"),0.8800f,0.7000f,0.6500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("rear-1"),0.8800f,1.0000f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("side-1"),0.6000f,0.9273f,0.8667f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front1"),0.8800f,0.9545f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front_hem1"),0.8800f,0.7000f,0.6500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("rear1"),0.8800f,1.0000f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("side1"),0.6000f,0.9273f,0.8667f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("sleeve"),0.8800f,1.0000f,0.8800f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("bell_cuff"),1.0000f,0.9500f,1.0000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("cuff_thread"),0.8500f,0.7000f,0.8500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("sleeve"),0.8800f,1.0000f,0.8800f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("bell_cuff"),1.0000f,0.9500f,1.0000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("cuff_thread"),0.8500f,0.7000f,0.8500f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("body").getChild("trouser_waist"),0.9111f,0.7000f,0.8600f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("right_leg").getChild("trouser"),0.8320f,0.9909f,0.8480f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("boot"),0.8600f,0.8600f,0.9400f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("turned_toe"),0.8600f,0.7333f,0.8500f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("boot_binding"),0.8920f,0.7000f,0.9720f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("left_leg").getChild("trouser"),0.8320f,0.9909f,0.8480f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("boot"),0.8600f,0.8600f,0.9400f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("turned_toe"),0.8600f,0.7333f,0.8500f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("boot_binding"),0.8920f,0.7000f,0.9720f);
  }
  if(robe==false) {
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("crown"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("left_cheek"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("right_cheek"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("nape"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("hood_tail"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("tail_tip"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("brow_thread"),1.0000f,1.0000f,0.2000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("edge-1"),0.2000f,1.0000f,0.3000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("edge1"),0.2000f,1.0000f,0.3000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("iron_brow"),1.0000f,1.0000f,0.7000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("temple_left"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("temple_right"),1.0000f,1.0000f,1.0000f);
   if(slot==EquipmentSlot.HEAD)scale(root.getChild("head").getChild("crest_seal"),1.0000f,1.0000f,0.3000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("coat"),0.9444f,1.0000f,0.9400f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("mantle"),0.9200f,1.0000f,0.9333f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("lapel-1"),0.9667f,1.0000f,0.5000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("lapel1"),0.9667f,1.0000f,0.5000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("sash"),1.0000f,1.0000f,0.9333f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("buckle"),1.0000f,0.7500f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("crystal"),0.7000f,0.9000f,0.2000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("scroll_case"),0.7000f,1.0000f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("scroll_band"),0.7500f,0.6000f,0.6500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("folio"),0.7333f,0.7750f,0.8000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("folio_clasp"),0.6000f,0.9000f,0.2000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front-1"),0.8800f,0.9545f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front_hem-1"),0.8800f,0.7000f,0.6500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("rear-1"),0.8800f,1.0000f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("side-1"),0.6000f,0.9273f,0.8667f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front1"),0.8800f,0.9545f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("front_hem1"),0.8800f,0.7000f,0.6500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("rear1"),0.8800f,1.0000f,0.5500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("side1"),0.6000f,0.9273f,0.8667f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("sleeve"),0.8800f,1.0000f,0.8800f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("bell_cuff"),1.0000f,0.9500f,1.0000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("cuff_thread"),0.8500f,0.7000f,0.8500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("lamella0"),0.9333f,0.7500f,0.9167f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("lamella1"),0.8833f,0.7500f,0.8833f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("lamella2"),1.0000f,0.7500f,0.8500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("shoulder_seal"),0.6000f,0.7000f,0.3000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("right_arm").getChild("bracer"),0.9400f,1.0000f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("sleeve"),0.8800f,1.0000f,0.8800f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("bell_cuff"),1.0000f,0.9500f,1.0000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("cuff_thread"),0.8500f,0.7000f,0.8500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("lamella0"),0.9333f,0.7500f,0.9167f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("lamella1"),0.8833f,0.7500f,0.8833f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("lamella2"),1.0000f,0.7500f,0.8500f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("shoulder_seal"),0.6000f,0.7000f,0.3000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("left_arm").getChild("bracer"),0.9400f,1.0000f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("sternum"),0.9667f,0.9250f,0.7000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("rib-1"),0.9000f,0.8000f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("hip_plate-1"),0.9000f,0.8750f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("rib1"),0.9000f,0.8000f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("hip_plate1"),0.9000f,0.8750f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("spine_plate"),0.9143f,0.9571f,0.6000f);
   if(slot==EquipmentSlot.CHEST)scale(root.getChild("body").getChild("seal"),0.6000f,0.7500f,0.2000f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("body").getChild("trouser_waist"),0.9111f,0.7000f,0.8600f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("right_leg").getChild("trouser"),0.8320f,0.9909f,0.8480f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("right_leg").getChild("greave"),1.0000f,0.9200f,0.5000f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("boot"),0.8600f,0.8600f,0.9400f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("turned_toe"),0.8600f,0.7333f,0.8500f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("boot_binding"),0.8920f,0.7000f,0.9720f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("right_leg").getChild("toe_plate"),0.8500f,0.8000f,0.3500f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("left_leg").getChild("trouser"),0.8320f,0.9909f,0.8480f);
   if(slot==EquipmentSlot.LEGS)scale(root.getChild("left_leg").getChild("greave"),1.0000f,0.9200f,0.5000f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("boot"),0.8600f,0.8600f,0.9400f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("turned_toe"),0.8600f,0.7333f,0.8500f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("boot_binding"),0.8920f,0.7000f,0.9720f);
   if(slot==EquipmentSlot.FEET)scale(root.getChild("left_leg").getChild("toe_plate"),0.8500f,0.8000f,0.3500f);
  }
 return root; }
 private static void scale(ModelPart p,float x,float y,float z){p.xScale=x;p.yScale=y;p.zScale=z;}
 private MagicArmorModels(){}
}
