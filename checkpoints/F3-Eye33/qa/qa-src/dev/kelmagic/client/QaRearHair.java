package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.client.hair.*;
import net.minecraft.client.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.*;import net.minecraft.network.chat.Component;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;import org.joml.Matrix4f;import org.joml.Vector3f;import org.joml.Quaternionf;import com.mojang.blaze3d.platform.Lighting;
/** External pose test: unmodified native GeckoLib/Minecraft renderer, no geometry hidden. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaRearHair {
 static final String[] N={"0 deg / neutral","+45 deg / hold","-45 deg / hold","+90 deg / stress pose","90 -> 0 / return","pitch +20 deg","pitch -20 deg","walk / +/-45 deg"};
 static int idle,frame=-10,total;static boolean opened,configured;static RedMage actor;static float yaw,pitch,roll;
 @SubscribeEvent public static void layers(software.bernie.geckolib.event.GeoRenderEvent.Entity.CompileRenderLayers ev){
  if(!Boolean.getBoolean("kelmagic.rearHairQA")||!(ev.getRenderer() instanceof RedMageRenderer))return;
  ev.addLayer(new software.bernie.geckolib.renderer.layer.GeoRenderLayer<RedMage>((RedMageRenderer)ev.getRenderer()){
   @Override public void renderForBone(com.mojang.blaze3d.vertex.PoseStack p,RedMage e,software.bernie.geckolib.cache.object.GeoBone b,net.minecraft.client.renderer.RenderType ty,net.minecraft.client.renderer.MultiBufferSource bs,com.mojang.blaze3d.vertex.VertexConsumer vc,float pt,int li,int ov){if(e!=actor||!b.getName().equals("body"))return;getGeoModel().getBone("head").ifPresent(h->{h.setRotY((float)Math.toRadians(yaw));h.setRotX((float)Math.toRadians(pitch));h.setRotZ((float)Math.toRadians(roll));});}
  });
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post ev){
  if(!Boolean.getBoolean("kelmagic.rearHairQA"))return;var m=Minecraft.getInstance();if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++idle>25){opened=true;if(java.nio.file.Files.exists(m.gameDirectory.toPath().resolve("saves/HairMotionPreview/level.dat")))m.createWorldOpenFlows().openWorld("HairMotionPreview",()->{});else m.createWorldOpenFlows().createFreshLevel("HairMotionPreview",new net.minecraft.world.level.LevelSettings("Hair motion preview",net.minecraft.world.level.GameType.CREATIVE,false,net.minecraft.world.Difficulty.PEACEFUL,true,new net.minecraft.world.level.GameRules(),net.minecraft.world.level.WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(7L,false,false),r->r.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getHolderOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).value().createWorldDimensions(),m.screen);}return;}
  if(m.player==null||configured)return;configured=true;try{MageClientConfig.class.getField("HAIR_HEAD_YAW_LIMIT").get(null).getClass().getMethod("set",Object.class).invoke(MageClientConfig.class.getField("HAIR_HEAD_YAW_LIMIT").get(null),Double.valueOf(System.getProperty("kelmagic.hair.testLimit","180")));}catch(NoSuchFieldException ex){}catch(Exception ex){throw new RuntimeException(ex);}m.options.guiScale().set(2);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.options.framerateLimit().set(120);m.resizeDisplay();
  actor=MagicCreatures.RED_MAGE.get().create(m.level);actor.awaken();actor.setNoAi(true);actor.setOnGround(true);actor.setPos(.5,-60,.5);actor.xo=.5;actor.yo=-60;actor.zo=.5;actor.yBodyRot=actor.yBodyRotO=180;actor.yHeadRot=actor.yHeadRotO=180;actor.setYRot(180);m.setScreen(new Preview());
 }
 static class Preview extends Screen {
  Preview(){super(Component.literal("Rear Hair7"));}public boolean isPauseScreen(){return true;}
  void draw(GuiGraphics g,float x,float angle,float partial){var m=Minecraft.getInstance();var p=g.pose();g.enableScissor((int)x-width/4+8,55,(int)x+width/4-8,height-28);p.pushPose();p.translate(x,210,50);p.scale(160,160,-160);p.translate(0,1.65,0);p.mulPose(new Quaternionf().rotationZ((float)Math.PI).rotateY((float)Math.toRadians(angle)));Lighting.setupForEntityInInventory();var d=m.getEntityRenderDispatcher();d.overrideCameraOrientation(new Quaternionf().rotateY((float)Math.PI));d.setRenderShadow(false);var bs=m.renderBuffers().bufferSource();com.mojang.blaze3d.systems.RenderSystem.runAsFancy(()->d.render(actor,0,0,0,0,partial,p,bs,0xF000F0));bs.endBatch();g.flush();d.setRenderShadow(true);p.popPose();g.disableScissor();Lighting.setupFor3DItems();}
  @Override public void render(GuiGraphics g,int mx,int my,float ignored){
   var m=Minecraft.getInstance();double sec=total/30.0;
   // One compact motion sequence, shared verbatim by baseline and candidate.
   yaw=curve(sec,new double[]{0,.5,1,1.5,2,2.5,2.8,3.8,4.0,4.5,5,8.4,8.8,9.2,9.6,10,10.4,10.8,11.3,12.5},new double[]{0,0,45,45,-45,-45,90,90,-90,-90,0,0,45,-45,45,-45,45,-45,0,0});
   pitch=curve(sec,new double[]{0,5.8,6.2,6.5,6.8,7.1,7.4,12.5},new double[]{0,0,20,20,-20,-20,0,0});
   roll=curve(sec,new double[]{0,7.4,7.7,8,8.4,12.5},new double[]{0,0,15,-15,0,0});
   int stage=sec<2.5?0:sec<5?1:sec<5.8?2:sec<8.4?3:sec<11.3?4:5;int f=total%30;
   actor.tickCount=1000+(int)(total*2/3.0);float partial=(total*2%3)/3f;actor.setDeltaMovement(stage==4?.07:0,0,0);actor.walkAnimation.update(stage==4?.7f:0,.5f);actor.setOnGround(true);
   var model=((RedMageRenderer)m.getEntityRenderDispatcher().getRenderer(actor)).getGeoModel();try{var clock=software.bernie.geckolib.model.GeoModel.class.getDeclaredField("animTime");clock.setAccessible(true);clock.setDouble(model,total*2/3.0);}catch(Exception ex){throw new RuntimeException(ex);}
   g.fill(0,0,width,height,0xFF191919);g.fill(8,55,width/2-4,height-28,0xFF26363A);g.fill(width/2+4,55,width-8,height-28,0xFF26363A);
   g.drawString(font,"Hair7 / SECONDARY MOTION / "+System.getProperty("kelmagic.rearHairQA.label"),14,12,0xFFFFFFFF,false);g.drawString(font,(stage+1)+" / 6  "+new String[]{"0 / +45 / -45","+90 / -90 / FAST REVERSAL","RETURN / SETTLE","PITCH +/-20 / ROLL +/-15","NATIVE WALK / HEAD TURNS","STOP / NEUTRAL SETTLE"}[stage],14,32,0xFF92C5FF,false);
   draw(g,width*.25f,180,partial);check(model,stage,f);draw(g,width*.75f,110,partial);
   g.drawString(font,"rear",14,60,0xFFCCCCCC,false);g.drawString(font,"rear / side",width/2+14,60,0xFFCCCCCC,false);
   g.drawString(font,"Native Minecraft + GeckoLib / actual head bone angles / 30Hz capture",14,height-17,0xFFCCCCCC,false);g.flush();
   if(frame>=0){try{var dir=java.nio.file.Path.of(System.getProperty("kelmagic.rearHairQA.output"));java.nio.file.Files.createDirectories(dir);try(var img=Screenshot.takeScreenshot(m.getMainRenderTarget())){img.writeToFile(dir.resolve(String.format("%04d.png",total)));}}catch(Exception ex){throw new RuntimeException(ex);}total++;}
   frame++;if(total>=(System.getProperty("kelmagic.rearHairQA.label").equals("LIMIT60")?174:375)){System.out.println("HAIR_MOTION_COMPLETE frames="+total);m.stop();}
  }
  float curve(double time,double[] t,double[] v){for(int i=1;i<t.length;i++)if(time<=t[i]){double a=Math.clamp((time-t[i-1])/(t[i]-t[i-1]),0,1);a=a*a*(3-2*a);return (float)(v[i-1]+(v[i]-v[i-1])*a);}return (float)v[v.length-1];}
  void check(software.bernie.geckolib.model.GeoModel<?> model,int stage,int f){
   int samples=0,inside=0,armInside=0;var armInv=new java.util.ArrayList<Matrix4f>();for(String n:new String[]{"left_arm","right_arm"})armInv.add(HairRig.belowBody(model.getBone(n).orElseThrow()).invert());for(String segment:new String[]{"back","mid","tip"})for(String layer:new String[]{"base","under","over"}){var b=model.getBone("hair6_hair_"+segment+"_"+layer).orElseThrow();var mat=HairRig.belowBody(b);for(var c:b.getCubes())for(var q:c.quads())if(q!=null){var test=new java.util.ArrayList<Vector3f>();for(var v:q.vertices())test.add(new Vector3f(v.position()));for(int[] tri:new int[][]{{0,1,2},{0,2,3}}){test.add(new Vector3f(q.vertices()[tri[0]].position()).add(q.vertices()[tri[1]].position()).add(q.vertices()[tri[2]].position()).div(3));}for(var vp:test){var p=mat.transformPosition(vp).mul(16);samples++;if(Math.abs(p.x)<4.01&&p.y>12&&p.y<24.01&&Math.abs(p.z)<2.01)inside++;for(int ai=0;ai<2;ai++){var ap=armInv.get(ai).transformPosition(new Vector3f(p).div(16)).mul(16);float lo=ai==0?-7:4;if(ap.x>lo&&ap.x<lo+3&&ap.y>18.04&&ap.y<24&&ap.z>-2&&ap.z<2)armInside++;}}}}
   if(f==18||inside>0||armInside>0){var ctl=actor.getAnimatableInstanceCache().getManagerForId(actor.getId()).getAnimationControllers().get("body");System.out.println("REAR_HAIR_POSE stage="+stage+" frame="+f+" yaw="+yaw+" actual="+(float)Math.toDegrees(model.getBone("head").orElseThrow().getRotY())+" pitch="+pitch+" samples="+samples+" insideTorso="+inside+" insideArms="+armInside+" clip="+(ctl.getCurrentAnimation()==null?"none":ctl.getCurrentAnimation().animation().name()));}
  }
 }
}
