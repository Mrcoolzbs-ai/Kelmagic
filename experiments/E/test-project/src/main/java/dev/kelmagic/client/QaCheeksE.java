package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.entity.RedMage;
import dev.kelmagic.client.face.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Opt-in developer-only QA, excluded from JAR by existing Qa* rule. No commands/gameplay added. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaCheeksE {
 static final String[] N={"neutral-front","neutral-45","normal-blink","squint-front","smile-squint-front","smile-squint-45","asymmetric-front","asymmetric-45","burst-squint-smile","return-squint-smile","head-turn","angry-speech"};
 static int stage,frame,id,wait;static boolean opened,requested,configured;static volatile boolean ready;
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.cheekQA"))return;var m=Minecraft.getInstance();
  if(stage>=N.length){System.out.println("CHEEK_QA_COMPLETE");m.stop();return;}
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicEyeQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;ready=false;int a=stage;m.getSingleplayerServer().execute(()->{prepare(m,a);ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;
   MageEyeStates.blinkProfile(e,stage<2?new EyeStateController.BlinkProfile(.10,.05,.16,1000,1000):stage==2?EyeStateController.NORMAL:new EyeStateController.BlinkProfile(.10,.05,.16,1.1,1.1));
   MageEyeStates.sustainedSquint(e,stage>=3?.4f:0);
   float l=stage>=4?.8f:0,r=l;if(stage==6||stage==7){l=1;r=.15f;}
   MageEyeStates.musclePose(e,l,r,l,r);
   System.out.println("CHEEK_STAGE "+N[stage]+" tick="+e.tickCount+" QAshortBlink="+(stage>2));
  }
  frame++;m.options.hideGui=true;
  if(frame==1){m.setScreen(null);m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.options.fov().set(60);m.options.bobView().set(false);m.getToasts().clear();}
  e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;e.setXRot(0);
  if(stage==10){e.yHeadRot=e.yHeadRotO=180+(float)(45*Math.sin(frame*.045));e.setXRot((float)(10*Math.sin(frame*.035)));}
  if(frame%20==0&&stage!=11)m.getSingleplayerServer().execute(()->{var a=m.getSingleplayerServer().overworld().getEntity(id);if(a instanceof RedMage r)r.scene(0);});
  if(stage==8&&frame==30)MageEyeStates.burst(e);
  if(frame%2==0&&frame>=20){
   var f=MageEyeStates.sample(e,0);var renderer=(software.bernie.geckolib.renderer.GeoEntityRenderer<RedMage>)m.getEntityRenderDispatcher().getRenderer(e);var model=renderer.getGeoModel();
   float lower=model.getBone("lower_lid_left").map(b->b.getPosY()).orElse(-999f);float upper=model.getBone("lid_left").map(b->b.getScaleY()).orElse(-999f);
   System.out.println("CHEEK_SAMPLE stage="+N[stage]+" frame="+frame+" tick="+e.tickCount+" open="+f.leftOpening()+" blink="+f.blinkClosure()+" burst="+f.burstActive()+" cheekL="+f.cheekLeft()+" cheekR="+f.cheekRight()+" lowerL="+f.lowerLeft()+" lowerR="+f.lowerRight()+" actualLowerY="+lower+" upperMask="+upper+" head="+e.yHeadRot+" mood="+e.expression());
   capture(m,String.format("%02d-%s-%03d",stage,N[stage],frame));
  }
  if(frame>=(stage==2?240:stage==10?160:100)){frame=0;requested=false;configured=false;stage++;}
 }
 static void prepare(Minecraft m,int a){var s=m.getSingleplayerServer();var p=s.getPlayerList().getPlayers().getFirst();var l=p.serverLevel();
  p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,s);
  if(a==0){for(var old:l.getEntitiesOfClass(RedMage.class,p.getBoundingBox().inflate(256)))old.discard();
   for(int x=40;x<=49;x++)for(int z=-13;z<=-3;z++)for(int y=-60;y<=-54;y++)l.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
   var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);id=e.getId();
  }
  var e=(RedMage)l.getEntity(id);if(e==null)throw new IllegalStateException("QA actor missing");e.scene(a==11?9:0);if(a==11){e.setHealth(18);e.updateCombatPhase();}
  double angle=Math.toRadians(a==1||a==5||a==7?45:0),d=1.45;p.connection.teleport(44.5+Math.sin(angle)*d,-59.86,-8-Math.cos(angle)*d,(float)Math.toDegrees(angle),0);
 }
 static void capture(Minecraft m,String name){var d=new java.io.File(System.getProperty("kelmagic.cheekQA.output"));d.mkdirs();Screenshot.grab(d,name+".png",m.getMainRenderTarget(),v->{});}
}
