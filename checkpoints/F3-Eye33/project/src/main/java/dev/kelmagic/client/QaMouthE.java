package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.client.face.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.Screenshot;import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.GameType;import net.minecraft.core.BlockPos;import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
/** Explicit opt-in disposable QA; excluded by existing Qa* JAR rule. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaMouthE {
 static final String[] N={"neutral-breath","neutral-45","smile-front","smile-45","speech-front","speech-45","surprised-front","surprised-45","eyes-cheeks-independent","head-turn","breathing-off","breathing-restored"};
 static int stage,frame,id,wait;static boolean opened,requested,configured;static volatile boolean ready;
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.mouthQA"))return;var m=Minecraft.getInstance();
  if(stage>=N.length){System.out.println("MOUTH_QA_COMPLETE");m.stop();return;}
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicMouthQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;ready=false;int s=stage;m.getSingleplayerServer().execute(()->{prepare(m,s);ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;MouthStates.select(e,stage==2||stage==3||stage==8?MouthStates.State.SMILE:stage==4||stage==5?MouthStates.State.SPEECH:stage==6||stage==7?MouthStates.State.SURPRISED:MouthStates.State.NEUTRAL);MouthStates.breathingOverride(e,stage!=10);MageEyeStates.sustainedSquint(e,stage==8?.4f:0);MageEyeStates.musclePose(e,stage==8?.8f:0,stage==8?.8f:0,stage==8?.8f:0,stage==8?.8f:0);MageEyeStates.blinkProfile(e,new EyeStateController.BlinkProfile(.1,.05,.16,stage==8?1.1:1000,stage==8?1.1:1000));System.out.println("MOUTH_STAGE "+N[stage]+" tick="+e.tickCount);}
  frame++;m.options.hideGui=true;
  if(frame==1){m.setScreen(null);m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.options.fov().set(60);m.options.bobView().set(false);m.getToasts().clear();}
  e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;e.setXRot(0);
  if(stage==9){e.yHeadRot=e.yHeadRotO=180+(float)(45*Math.sin(frame*.045));e.setXRot((float)(8*Math.sin(frame*.035)));}
  if(frame%20==0)m.getSingleplayerServer().execute(()->{var r=m.getSingleplayerServer().overworld().getEntity(id);if(r instanceof RedMage mage)mage.scene(0);});
  if(stage==8&&frame==35)MageEyeStates.burst(e);
  if(frame>=20&&frame%2==0){
   var renderer=(software.bernie.geckolib.renderer.GeoEntityRenderer<RedMage>)m.getEntityRenderDispatcher().getRenderer(e);var model=renderer.getGeoModel();
   float nose=model.getBone("nose_lower").map(b->b.getPosY()-b.getInitialSnapshot().getOffsetY()).orElse(-999f),chestY=model.getBone("chest_left").map(b->b.getScaleY()).orElse(-999f),chestZ=model.getBone("chest_left").map(b->b.getScaleZ()).orElse(-999f);boolean hidden=model.getBone("mouth").map(b->b.isHidden()).orElse(false);var eyes=MageEyeStates.sample(e,0);
   System.out.println("MOUTH_SAMPLE stage="+N[stage]+" frame="+frame+" tick="+e.tickCount+" selected="+MouthStates.state(e)+" breath="+MouthStates.breathingEnabled(e)+" phase="+RedMage.breathing(e.tickCount)+" noseY="+nose+" chestY="+chestY+" chestZ="+chestZ+" legacyOpening="+e.mouth(0)+" oldMouthHidden="+hidden+" eyes="+eyes.leftOpening()+" cheeks="+eyes.cheekLeft()+" head="+e.yHeadRot);
   capture(m,String.format("%02d-%s-%03d",stage,N[stage],frame));
  }
  int len=stage==0||stage==9||stage==11?140:100;if(frame>=len){frame=0;requested=false;configured=false;stage++;}
 }
 static void prepare(Minecraft m,int a){var server=m.getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();var l=p.serverLevel();p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,server);
  if(a==0){for(int cx=2;cx<=3;cx++)for(int cz=-1;cz<=0;cz++)l.getChunk(cx,cz);for(var old:l.getEntitiesOfClass(RedMage.class,new net.minecraft.world.phys.AABB(32,-64,-16,64,0,16)))old.discard();
   for(int x=35;x<=54;x++)for(int z=-18;z<=2;z++){l.setBlockAndUpdate(new BlockPos(x,-61,z),Blocks.GRASS_BLOCK.defaultBlockState());for(int y=-60;y<=-54;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
   var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);id=e.getId();}
  var e=(RedMage)l.getEntity(id);if(e==null)throw new IllegalStateException("QA missing actor");e.scene(0);
  double angle=Math.toRadians(a==1||a==3||a==5||a==7?45:0),d=1.20;p.connection.teleport(44.5+Math.sin(angle)*d,-59.86,-8-Math.cos(angle)*d,(float)Math.toDegrees(angle),0);
 }
 static void capture(Minecraft m,String name){var d=new java.io.File(System.getProperty("kelmagic.mouthQA.output"));d.mkdirs();Screenshot.grab(d,name+".png",m.getMainRenderTarget(),v->{});}
}
