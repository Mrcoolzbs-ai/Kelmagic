package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.entity.RedMage;
import dev.kelmagic.client.face.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Opt-in disposable light QA. Uses actual world blocks and native renderer; never in JAR. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaLightExposure {
 static final String[] N={"ordinary-day","direct-sun","sun-behind-wall","lava-visible","lava-outside-gaze","lava-behind-wall","head-turn-lava","angry-speech-lava","lava-removed"};
 static final int[] LENGTH={100,140,100,140,100,100,160,140,140};
 static int stage,frame,id,wait;static boolean opened,requested,configured;static volatile boolean ready;
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.lightQA"))return;var m=Minecraft.getInstance();
  if(stage>=N.length){System.out.println("LIGHT_QA_COMPLETE");m.stop();return;}
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicLightQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;ready=false;int a=stage;m.getSingleplayerServer().execute(()->{prepare(m,a);ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;MageEyeStates.blinkProfile(e,stage==0?EyeStateController.NORMAL:new EyeStateController.BlinkProfile(.1,.05,.16,1.2,1.2));System.out.println("LIGHT_STAGE "+N[stage]+" tick="+e.tickCount);}
  frame++;m.options.hideGui=true;
  // Freeze only the QA actor's authoritative pose; production controls/clips remain untouched.
  float qaYaw=stage==1||stage==2?-90:180;
  e.setYRot(qaYaw);e.yBodyRot=e.yBodyRotO=qaYaw;e.setXRot(stage==1||stage==2?20:0);
  if(stage!=4&&stage!=6)e.yHeadRot=e.yHeadRotO=qaYaw;
  if(frame==1){m.setScreen(null);m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.options.fov().set(60);m.options.bobView().set(false);m.getToasts().clear();}
  if(frame%20==0&&stage!=7)m.getSingleplayerServer().execute(()->{var l=m.getSingleplayerServer().overworld();var a=l.getEntity(id);if(a instanceof RedMage r)r.scene(0);});
  if(stage==4){e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=100;e.setXRot(0);}
  if(stage==6){e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180+(float)(85*Math.sin(frame*Math.PI/80));e.setXRot(0);}
  if(stage>=3&&stage<=7&&(frame==10||frame==30)){boolean overview=frame==10;m.getSingleplayerServer().execute(()->camera(m,stage,overview));}
  if(frame%2==0&&(stage!=0||frame>=30)){
   var l=MageEyeStates.lightReading(e);var eyes=MageEyeStates.sample(e,0);var renderer=(software.bernie.geckolib.renderer.GeoEntityRenderer<RedMage>)m.getEntityRenderDispatcher().getRenderer(e);var model=renderer.getGeoModel();float mask=model.getBone("lid_left").map(b->b.getScaleY()).orElse(-1f);
   System.out.println("LIGHT_SAMPLE stage="+N[stage]+" frame="+frame+" tick="+e.tickCount+" sun="+l.sun()+" lava="+l.lava()+" raw="+l.raw()+" filtered="+l.filtered()+" rays="+l.testedRays()+" candidates="+l.lavaCandidates()+" open="+eyes.leftOpening()+" blink="+eyes.blinkClosure()+" actualMask="+mask+" mood="+e.expression()+" headYaw="+e.yHeadRot+" bodyYaw="+e.yBodyRot+" scene="+e.scene()+" dirX="+l.direction().x+" dirY="+l.direction().y+" dirZ="+l.direction().z);
   capture(m,String.format("%02d-%s-%03d",stage,N[stage],frame));
  }
  if(frame>=LENGTH[stage]){frame=0;requested=false;configured=false;stage++;}
 }
 static void camera(Minecraft m,int a,boolean overview){var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
  if(overview)p.connection.teleport(48,-59.86,-9.5,90,0);
  else if(a==1||a==2)p.connection.teleport(45.95,-59.86,-8,90,0);
  else p.connection.teleport(44.5,-59.86,-9.45,0,0);
 }
 static void prepare(Minecraft m,int a){var s=m.getSingleplayerServer();var p=s.getPlayerList().getPlayers().getFirst();var l=p.serverLevel();p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);l.setDayTime(a==1||a==2?0:6000);l.setWeatherParameters(6000,0,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,s);
  for(int x=37;x<=53;x++)for(int z=-16;z<=0;z++)for(int y=-60;y<=-54;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
  if(a>=3&&a<=7){
   l.setBlockAndUpdate(new BlockPos(44,-60,-11),Blocks.STONE.defaultBlockState());
   for(var q:new BlockPos[]{new BlockPos(43,-59,-11),new BlockPos(45,-59,-11),new BlockPos(44,-59,-10),new BlockPos(44,-59,-12)})l.setBlockAndUpdate(q,Blocks.STONE_SLAB.defaultBlockState());
   l.setBlockAndUpdate(new BlockPos(44,-59,-11),Blocks.LAVA.defaultBlockState());
  }
  if(a==2)for(int z=-10;z<=-6;z++)for(int y=-59;y<=-54;y++)l.setBlockAndUpdate(new BlockPos(47,y,z),Blocks.STONE.defaultBlockState());
  if(a==5)for(int x=42;x<=46;x++)for(int y=-59;y<=-56;y++)l.setBlockAndUpdate(new BlockPos(x,y,-10),Blocks.STONE.defaultBlockState());
  RedMage e;
  if(a==0){for(var old:l.getEntitiesOfClass(RedMage.class,p.getBoundingBox().inflate(256)))old.discard();e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);l.addFreshEntity(e);id=e.getId();}
  else e=(RedMage)l.getEntity(id);
  if(e==null)throw new IllegalStateException("QA entity missing");
  float yaw=a==1||a==2?-90:180;e.setYRot(yaw);e.yBodyRot=e.yBodyRotO=yaw;e.yHeadRot=e.yHeadRotO=yaw;e.setXRot(a==1||a==2?20:0);e.scene(0);
  if(a==7){e.setHealth(18);e.updateCombatPhase();e.scene(9);} // existing angry expression + speech clip, no new expression.
  camera(m,a,false);System.out.println("LIGHT_WORLD stage="+N[a]+" day="+l.getDayTime()+" sunAngle="+l.getSunAngle(1)+" eyeY="+e.getEyeY()+" loadedLava="+l.getFluidState(new BlockPos(44,-59,-11)).is(net.minecraft.tags.FluidTags.LAVA));
 }
 static void capture(Minecraft m,String name){var d=new java.io.File(System.getProperty("kelmagic.lightQA.output"));d.mkdirs();Screenshot.grab(d,name+".png",m.getMainRenderTarget(),v->{});}
}
