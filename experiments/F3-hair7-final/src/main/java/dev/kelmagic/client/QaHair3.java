package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.client.face.*;
import net.minecraft.client.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.*;import net.minecraft.client.gui.screens.inventory.InventoryScreen;import net.minecraft.network.chat.Component;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
/** Developer-only native geometry viewer, excluded from JAR. True bone angles, not clamped gameplay input angles. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaHair3 {
 static int idle,frame,id,index,shot;static boolean opened,requested,configured;static volatile boolean ready;
 static final String[] STATES={"front","quarter","side","rear","head-22","head-45","head-67","head-90","head-minus90","head-down","head-up","walk","turn-motion","world-front","world-quarter","world-side","world-head90"};
 static double yaw(){return index==4?22.5:index==5?45:index==6?67.5:index==7||index==16?90:index==8?-90:index==12?90*Math.sin(frame*Math.PI/50):0;}
 static double pitch(){return index==9?25:index==10?-25:0;}
 @SubscribeEvent public static void layers(software.bernie.geckolib.event.GeoRenderEvent.Entity.CompileRenderLayers event){
  if(!Boolean.getBoolean("kelmagic.hair3QA")||!(event.getRenderer() instanceof RedMageRenderer))return;
  event.addLayer(new software.bernie.geckolib.renderer.layer.GeoRenderLayer<RedMage>((RedMageRenderer)event.getRenderer()){
   @Override public void renderForBone(com.mojang.blaze3d.vertex.PoseStack p,RedMage e,software.bernie.geckolib.cache.object.GeoBone bone,net.minecraft.client.renderer.RenderType type,net.minecraft.client.renderer.MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer vertices,float partial,int light,int overlay){
    if(!bone.getName().equals("body")||e.getId()!=id)return;
    var model=getGeoModel();model.getBone("head").ifPresent(h->{h.setRotY((float)Math.toRadians(yaw()));h.setRotX((float)Math.toRadians(pitch()));});
    model.getBone("hair_back").ifPresent(b->b.setRotX(Boolean.getBoolean("kelmagic.hair3QA.oldRig")?(float)-Math.toRadians(pitch()):0));
    if(Boolean.getBoolean("kelmagic.hair3QA.oldRig"))dev.kelmagic.client.hair.HairRig.apply(model);
   }
  });
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.hair3QA"))return;var m=Minecraft.getInstance();
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++idle>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicHairQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;m.getSingleplayerServer().execute(()->{var l=m.getSingleplayerServer().overworld();var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);p.connection.teleport(44.5,-59.86,-11.4,0,0);for(var old:l.getEntitiesOfClass(RedMage.class,p.getBoundingBox().inflate(128)))old.discard();var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);id=e.getId();ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;MageEyeStates.blinkProfile(e,new EyeStateController.BlinkProfile(.1,.05,.16,1000,1000));m.options.guiScale().set(2);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.options.fov().set(60);m.options.bobView().set(false);m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.resizeDisplay();m.setScreen(new Preview(e));}
  frame++;if(frame>=(index==12?116:52)&&shot>=(index==12?6:4)){index++;frame=0;shot=0;if(index>=STATES.length){System.out.println("HAIR3_COMPLETE states="+STATES.length);m.stop();index--;return;}
   if(index>=13){int stage=index;m.setScreen(null);m.options.hideGui=true;m.getSingleplayerServer().execute(()->{var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();if(stage==14)p.connection.teleport(42.1,-59.86,-10.4,-45,0);else if(stage==15)p.connection.teleport(41.1,-59.86,-8,-90,0);else p.connection.teleport(44.5,-59.86,-11.4,0,0);});}}
  float body=180+(index==1?45:index==2?90:index==3?180:0);e.yBodyRot=e.yBodyRotO=body;e.setYRot(body);e.yHeadRot=e.yHeadRotO=body+(float)yaw();e.setXRot((float)pitch());e.setOnGround(true);e.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);e.walkAnimation.update(index==11?.6f:0,.5f);
  if(index>=13&&frame>12&&shot<4&&frame>=14+shot*8)capture(m,e);
 }
 static void capture(Minecraft m,RedMage e){shot++;var dir=new java.io.File(System.getProperty("kelmagic.hair3QA.output"));dir.mkdirs();String name=STATES[index]+"-"+frame;Screenshot.grab(dir,name+".png",m.getMainRenderTarget(),msg->{});
  var renderer=(RedMageRenderer)m.getEntityRenderDispatcher().getRenderer(e);var model=renderer.getGeoModel();StringBuilder scales=new StringBuilder();for(String n:new String[]{"hair_front_left","hair_front_right"})model.getBone(n).ifPresent(b->scales.append(n).append(':').append(b.getScaleX()).append(',').append(b.getScaleY()).append(',').append(b.getScaleZ()).append(';'));
  System.out.println("HAIR3_CAPTURE "+name+" targetYaw="+yaw()+" actualHeadYaw="+model.getBone("head").map(b->Math.toDegrees(b.getRotY())).orElse(-999d)+" targetPitch="+pitch()+" scales="+scales);
 }
 static class Preview extends Screen {
  final RedMage e;Preview(RedMage e){super(Component.literal("Hair3 QA"));this.e=e;}
  public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xFF26363A);g.drawCenteredString(font,"F3 hair3 QA · "+STATES[index]+" · head "+String.format(java.util.Locale.ROOT,"%.1f",yaw()),width/2,12,0xFFF3E2AF);InventoryScreen.renderEntityInInventory(g,width/2f,height/2f,110,new org.joml.Vector3f(0,1.25f,0),new org.joml.Quaternionf().rotationZ((float)Math.PI),new org.joml.Quaternionf(),e);g.flush();if(shot<(index==12?6:4)&&frame>=(index==12?new int[]{12,25,50,75,100,110}[shot]:12+shot*8))capture(Minecraft.getInstance(),e);}
 }
}
