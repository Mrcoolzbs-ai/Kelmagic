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
  void draw(GuiGraphics g,float x,float angle,float partial){var m=Minecraft.getInstance();var p=g.pose();g.enableScissor((int)x-width/6+6,55,(int)x+width/6-6,height-28);p.pushPose();p.translate(x,210,50);p.scale(144,144,-144);p.translate(0,1.65,0);p.mulPose(new Quaternionf().rotationZ((float)Math.PI).rotateY((float)Math.toRadians(angle)));Lighting.setupForEntityInInventory();var d=m.getEntityRenderDispatcher();d.overrideCameraOrientation(new Quaternionf().rotateY((float)Math.PI));d.setRenderShadow(false);var bs=m.renderBuffers().bufferSource();com.mojang.blaze3d.systems.RenderSystem.runAsFancy(()->d.render(actor,0,0,0,0,partial,p,bs,0xF000F0));bs.endBatch();g.flush();d.setRenderShadow(true);p.popPose();g.disableScissor();Lighting.setupFor3DItems();}
  @Override public void render(GuiGraphics g,int mx,int my,float ignored){
   var m=Minecraft.getInstance();double sec=total/30.0;
   // One compact motion sequence, shared verbatim by baseline and candidate.
   yaw=curve(sec,new double[]{0,1,1.5,2,2.5,3,3.5,4,4.5,5.5,6},new double[]{0,0,45,45,65,65,-65,-65,0,0,0});
   pitch=roll=0;int stage=sec<.5?0:sec<2.5?1:sec<5.5?2:3;int f=total%30;
   actor.tickCount=1000+(int)(total*2/3.0);float partial=(total*2%3)/3f;actor.setDeltaMovement(false?.07:0,0,0);actor.walkAnimation.update(false?.7f:0,.5f);actor.setOnGround(true);
   var model=((RedMageRenderer)m.getEntityRenderDispatcher().getRenderer(actor)).getGeoModel();try{var clock=software.bernie.geckolib.model.GeoModel.class.getDeclaredField("animTime");clock.setAccessible(true);clock.setDouble(model,total*2/3.0);}catch(Exception ex){throw new RuntimeException(ex);}
   g.fill(0,0,width,height,0xFF191919);for(int col=0;col<3;col++)g.fill(col*width/3+6,55,(col+1)*width/3-6,height-28,0xFF26363A);
   g.drawString(font,"Hair v8.3 / NAPE ROOT / "+System.getProperty("kelmagic.rearHairQA.label"),14,12,0xFFFFFFFF,false);g.drawString(font,"REQUEST "+String.format(java.util.Locale.ROOT,"%+.1f",yaw)+" DEG / HARD CEILING +/-65 DEG",14,32,0xFF92C5FF,false);
   draw(g,width/6f,90,partial);check(model,stage,f);draw(g,width*.5f,180,partial);draw(g,width*5/6f,135,partial);
   g.drawString(font,"ACTUAL HEAD "+String.format(java.util.Locale.ROOT,"%+.2f",Math.toDegrees(model.getBone("head").orElseThrow().getRotY()))+" DEG",14,height-31,0xFF92C5FF,false);
   g.drawString(font,"side",14,60,0xFFCCCCCC,false);g.drawString(font,"rear",width/3+14,60,0xFFCCCCCC,false);g.drawString(font,"rear 45 deg",width*2/3+14,60,0xFFCCCCCC,false);
   g.drawString(font,"Native Minecraft + GeckoLib / actual head bone angles / 30Hz capture",14,height-17,0xFFCCCCCC,false);g.flush();
   if(frame>=0){try{var dir=java.nio.file.Path.of(System.getProperty("kelmagic.rearHairQA.output"));java.nio.file.Files.createDirectories(dir);try(var img=Screenshot.takeScreenshot(m.getMainRenderTarget())){img.writeToFile(dir.resolve(String.format("%04d.png",total)));}}catch(Exception ex){throw new RuntimeException(ex);}total++;}
   frame++;if(total>=180){System.out.println("HAIR_MOTION_COMPLETE frames="+total);m.stop();}
  }
  float curve(double time,double[] t,double[] v){for(int i=1;i<t.length;i++)if(time<=t[i]){double a=Math.clamp((time-t[i-1])/(t[i]-t[i-1]),0,1);a=a*a*(3-2*a);return (float)(v[i-1]+(v[i]-v[i-1])*a);}return (float)v[v.length-1];}
  void check(software.bernie.geckolib.model.GeoModel<?> model,int stage,int f){
   var head=model.getBone("head").orElseThrow();float neutral=head.getInitialSnapshot().getRotY();float actual=(float)Math.toDegrees(head.getRotY()-neutral);var matrix=HairRig.belowBody(head);float matrixYaw=(float)Math.toDegrees(Math.atan2(matrix.m20(),matrix.m00()));
   if(Math.abs(actual)>65.0001f||Math.abs(matrixYaw)>65.0001f)throw new IllegalStateException("head exceeded 65: "+actual+" matrix="+matrixYaw);
   if(Math.abs(actual-Math.clamp(yaw,-65,65))>.001f)throw new IllegalStateException("head did not track clamped request");
   System.out.println("HEAD65_SAMPLE frame="+total+" request="+yaw+" actual="+actual+" matrixYaw="+matrixYaw);
   // Read-only check of the production simulation's nine pinned scalp particles.
   try {
    var mf=RearHairFollow.class.getDeclaredField("MOTION");mf.setAccessible(true);
    var state=((java.util.Map<?,?>)mf.get(null)).get(actor);
    var cf=state.getClass().getDeclaredField("cloth");cf.setAccessible(true);var cloth=cf.get(state);
    var pf=RearHairCloth.class.getDeclaredField("pos");pf.setAccessible(true);var points=(Vector3f[])pf.get(cloth);
    float top=Float.parseFloat(System.getProperty("kelmagic.rearHairQA.rootTop","24.35"));
    var delta=new Matrix4f(matrix).translate(0,HeadSeat.DROP_PIXELS/16,0);float err=0;
    for(int c=0;c<9;c++){
     var expected=delta.transformPosition(new Vector3f((-4.2f+8.4f*c/8)/16,top/16,4.2f/16)).mul(16);
     err=Math.max(err,expected.distance(points[c]));
    }
    if(err>.0001f)throw new IllegalStateException("root detached: "+err);
    System.out.println("NAPE_BIND_SAMPLE frame="+total+" maxErrorPx="+err+" "+RearHairFollow.diagnostics(actor));
   }catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}

  }
 }
}
