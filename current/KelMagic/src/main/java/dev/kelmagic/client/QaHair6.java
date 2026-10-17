package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.client.face.*;
import net.minecraft.client.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.*;import net.minecraft.client.gui.screens.inventory.InventoryScreen;import net.minecraft.network.chat.Component;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
/** Opt-in proof: hide base cubes WITHOUT hiding children; same cameras and actor for A / B / AB. Excluded from JAR. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaHair6 {
 record Stage(String name,int mode,float body,float yaw,float pitch,boolean noHat,boolean world){}
 static final java.util.List<Stage> stages=new java.util.ArrayList<>();
 static {if(Boolean.getBoolean("kelmagic.hair6Motion")){
  stages.add(new Stage("head45",0,0,45,0,false,false));stages.add(new Stage("headMinus45",0,0,-45,0,false,false));stages.add(new Stage("head90",0,0,90,0,false,false));stages.add(new Stage("turnReturn",0,0,0,0,false,false));stages.add(new Stage("down",0,0,0,25,false,false));stages.add(new Stage("up",0,0,0,-25,false,false));stages.add(new Stage("worldWalk",0,0,0,0,false,true));
 }else{
  for(int i=0;i<4;i++)stages.add(new Stage(new String[]{"front","quarter","side","rear"}[i],0,new float[]{0,45,90,180}[i],0,0,false,false));
  stages.add(new Stage("base_quarter",1,45,0,0,true,false));stages.add(new Stage("layers_quarter",2,45,0,0,true,false));stages.add(new Stage("crown",0,0,0,-25,true,false));
 }}
 static int idle,frame,id,index,shot;static boolean opened,requested,configured;static volatile boolean ready;
 static Stage stage(){return stages.get(Math.min(index,stages.size()-1));}
 static double yaw(){return stage().name.equals("turnReturn")?90*Math.sin(frame*Math.PI/50):stage().yaw;}
 static final String[] HAIR={"hair_crown","hair_back","hair_right","hair_left","hair_mid","hair_tip","hair_front_left","hair_front_right","bang_0","bang_1","bang_2","bang_3","bang_4","hair_end_left","hair_end_right","hair_chest_root_left","hair_chest_root_right","hair4_sheet_bang_under","hair4_sheet_bang_over","hair4_sheet_side_left_under","hair4_sheet_corner_left_front_under","hair4_sheet_corner_left_rear_under","hair4_sheet_temple_left_under","hair4_sheet_side_left_over","hair4_sheet_corner_left_front_over","hair4_sheet_corner_left_rear_over","hair4_sheet_temple_left_over","hair4_sheet_side_right_under","hair4_sheet_corner_right_front_under","hair4_sheet_corner_right_rear_under","hair4_sheet_temple_right_under","hair4_sheet_side_right_over","hair4_sheet_corner_right_front_over","hair4_sheet_corner_right_rear_over","hair4_sheet_temple_right_over","hair4_sheet_crown_under","hair4_sheet_nape_under","hair4_sheet_crown_over","hair4_sheet_nape_over","hair4_sheet_hair_back_under","hair4_sheet_hair_back_edge_left_under","hair4_sheet_hair_back_edge_right_under","hair4_sheet_hair_back_over","hair4_sheet_hair_back_edge_left_over","hair4_sheet_hair_back_edge_right_over","hair4_sheet_hair_mid_under","hair4_sheet_hair_mid_edge_left_under","hair4_sheet_hair_mid_edge_right_under","hair4_sheet_hair_mid_over","hair4_sheet_hair_mid_edge_left_over","hair4_sheet_hair_mid_edge_right_over","hair4_sheet_hair_tip_under","hair4_sheet_hair_tip_edge_left_under","hair4_sheet_hair_tip_edge_right_under","hair4_sheet_hair_tip_over","hair4_sheet_hair_tip_edge_left_over","hair4_sheet_hair_tip_edge_right_over","hair4_sheet_neck_left_under","hair4_sheet_chest_left_under","hair4_sheet_neck_left_over","hair4_sheet_chest_left_over","hair4_sheet_neck_right_under","hair4_sheet_chest_right_under","hair4_sheet_neck_right_over","hair4_sheet_chest_right_over"};
 @SubscribeEvent public static void layers(software.bernie.geckolib.event.GeoRenderEvent.Entity.CompileRenderLayers event){
  if(!Boolean.getBoolean("kelmagic.hair6QA")||!(event.getRenderer() instanceof RedMageRenderer))return;
  event.addLayer(new software.bernie.geckolib.renderer.layer.GeoRenderLayer<RedMage>((RedMageRenderer)event.getRenderer()){
   @Override public void renderForBone(com.mojang.blaze3d.vertex.PoseStack p,RedMage e,software.bernie.geckolib.cache.object.GeoBone bone,net.minecraft.client.renderer.RenderType type,net.minecraft.client.renderer.MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer vertices,float partial,int light,int overlay){
    if(!bone.getName().equals("body")||e.getId()!=id)return;var model=getGeoModel();
    model.getBone("head").ifPresent(h->{h.setRotY((float)Math.toRadians(yaw()));h.setRotX((float)Math.toRadians(stage().pitch));});
    for(String n:HAIR)model.getBone(n).ifPresent(b->{boolean sheet=n.startsWith("hair4_sheet_");b.setHidden(sheet?stage().mode==1:stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_base_shoulder").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_side_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_turn_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_shoulder_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_side_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_turn_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_left_shoulder_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_root_closure_left").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_rear_side_left").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_rear_side_left_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_rear_side_left_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_base_shoulder").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_side_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_turn_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_shoulder_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_side_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_turn_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_sample_right_shoulder_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_root_closure_right").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_rear_side_right").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_rear_side_right_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_rear_side_right_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_upper_bevel_1").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_upper_bevel_1_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_upper_bevel_1_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_upper_bevel_-1").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_upper_bevel_-1_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_upper_bevel_-1_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_nape_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_nape_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_nape_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_back_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_back_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_back_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_mid_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_mid_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_mid_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_tip_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_tip_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_hair_tip_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_front_left_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_front_left_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_front_left_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_front_right_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_front_right_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_front_right_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_0_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_0_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_0_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_1_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_1_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_1_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_2_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_2_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_2_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_3_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_3_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_left_3_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_0_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_0_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_0_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_1_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_1_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_1_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_2_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_2_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_2_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_3_base").ifPresent(b->{b.setHidden(stage().mode==2);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_3_under").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hair6_handoff_right_3_over").ifPresent(b->{b.setHidden(stage().mode==1);b.setChildrenHidden(false);});
    model.getBone("hat").ifPresent(b->{b.setHidden(stage().noHat);b.setChildrenHidden(stage().noHat);});
   }
  });
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.hair6QA"))return;var m=Minecraft.getInstance();
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++idle>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicHairQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;m.getSingleplayerServer().execute(()->{var l=m.getSingleplayerServer().overworld();var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);p.connection.teleport(44.5,-59.86,-11.4,0,0);for(var old:l.getEntitiesOfClass(RedMage.class,p.getBoundingBox().inflate(128)))old.discard();var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);id=e.getId();ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;MageEyeStates.blinkProfile(e,new EyeStateController.BlinkProfile(.1,.05,.16,1000,1000));m.options.guiScale().set(2);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.options.fov().set(60);m.options.bobView().set(false);m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.resizeDisplay();m.setScreen(new Preview(e));}
  frame++;boolean motion=stage().name.equals("turnReturn");if(frame>=(motion?116:40)&&shot>=(motion?5:2)){index++;frame=0;shot=0;if(index>=stages.size()){System.out.println("HAIR6_COMPLETE states="+stages.size());m.stop();index--;return;}
   if(stage().world){m.setScreen(null);m.options.hideGui=true;String n=stage().name;m.getSingleplayerServer().execute(()->{var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();if(n.equals("worldQuarter"))p.connection.teleport(42.1,-59.86,-10.4,-45,0);else if(n.equals("worldSide"))p.connection.teleport(41.1,-59.86,-8,-90,0);else p.connection.teleport(44.5,-59.86,-11.4,0,0);});}}
  float body=180+stage().body;e.yBodyRot=e.yBodyRotO=body;e.setYRot(body);e.yHeadRot=e.yHeadRotO=body+(float)yaw();e.setXRot(stage().pitch);e.setOnGround(true);e.setShiftKeyDown(stage().name.equals("crouch"));e.setPose(stage().name.equals("crouch")?net.minecraft.world.entity.Pose.CROUCHING:net.minecraft.world.entity.Pose.STANDING);boolean walk=stage().name.toLowerCase(java.util.Locale.ROOT).contains("walk");e.setDeltaMovement(walk?new net.minecraft.world.phys.Vec3(.07,0,0):net.minecraft.world.phys.Vec3.ZERO);e.walkAnimation.update(walk?.7f:0,.5f);
  if(stage().name.equals("worldWalk")){int f=frame;m.getSingleplayerServer().execute(()->{var a=m.getSingleplayerServer().overworld().getEntity(id);if(a instanceof RedMage mage){mage.setPos(44.5+Math.sin(f*.12)*.65,-60,-8);mage.setDeltaMovement(.07,0,0);mage.setOnGround(true);}});}
  if(stage().world&&shot<2&&frame>=16+shot*10)capture(m,e);
 }
 static boolean due(){return shot<(stage().name.equals("turnReturn")?5:2)&&frame>=(stage().name.equals("turnReturn")?new int[]{12,25,50,75,100}[shot]:14+shot*10);}
 static void capture(Minecraft m,RedMage e){shot++;var dir=new java.io.File(System.getProperty("kelmagic.hair6QA.output"));dir.mkdirs();String name=stage().name+"-"+frame;Screenshot.grab(dir,name+".png",m.getMainRenderTarget(),msg->{});
  var model=((RedMageRenderer)m.getEntityRenderDispatcher().getRenderer(e)).getGeoModel();int hiddenBase=0,hiddenSheets=0;for(String n:HAIR){var b=model.getBone(n).orElseThrow();if(b.isHidden()){if(n.startsWith("hair4_sheet_"))hiddenSheets++;else hiddenBase++;}}
  var ctl=e.getAnimatableInstanceCache().getManagerForId(e.getId()).getAnimationControllers().get("body");String anim=ctl==null||ctl.getCurrentAnimation()==null?"none":ctl.getCurrentAnimation().animation().name();
  System.out.println("HAIR6_CAPTURE "+name+" mode="+stage().mode+" hiddenBase="+hiddenBase+" hiddenSheets="+hiddenSheets+" headYaw="+model.getBone("head").map(b->Math.toDegrees(b.getRotY())).orElse(-999d)+" targetYaw="+yaw()+" animation="+anim+" x="+e.getX());
 }
 static class Preview extends Screen {final RedMage e;Preview(RedMage e){super(Component.literal("Hair4 construction QA"));this.e=e;}public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xFF26363A);g.drawCenteredString(font,"F3 hair6 · "+stage().name+" · "+(stage().noHat?"QA: hat hidden":"native GeckoLib"),width/2,12,0xFFF3E2AF);InventoryScreen.renderEntityInInventory(g,width/2f,height/2f,110,new org.joml.Vector3f(0,1.25f,0),new org.joml.Quaternionf().rotationZ((float)Math.PI),new org.joml.Quaternionf(),e);g.flush();if(due())capture(Minecraft.getInstance(),e);}
 }
}
