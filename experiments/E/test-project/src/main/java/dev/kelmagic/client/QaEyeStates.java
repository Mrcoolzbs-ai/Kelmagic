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
/** Opt-in developer-only native QA. Excluded from distributed JAR. No new game commands/mechanics. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaEyeStates {
 static int stage,frame,wait,id;static boolean opened,requested,configured;static volatile boolean ready;
 static final String[] N={"normal","squint","burst-over-squint","return-to-squint","head-turn","existing-angry","existing-talk","existing-head-clip","default-random-neutral"};
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post ev){
  if(!Boolean.getBoolean("kelmagic.eyeQA"))return;var m=Minecraft.getInstance();
  if(stage>=N.length){System.out.println("EYE_QA_COMPLETE");m.stop();return;}
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicEyeQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;ready=false;int a=stage;m.getSingleplayerServer().execute(()->{prepare(m,a);ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;
   if(stage<8)MageEyeStates.blinkProfile(e,new EyeStateController.BlinkProfile(.10,.05,.16,1.10,1.10));
   else MageEyeStates.blinkProfile(e,EyeStateController.NORMAL); // QA only: fixed short wait exposes complete cycles.
   MageEyeStates.sustainedSquint(e,stage>=1&&stage<=4?.45f:0);
   System.out.println("EYE_STAGE "+N[stage]+" tick="+e.tickCount+" interval="+(stage<8?"QA-fixed-1.10":"DEFAULT-random-2.5..5.5"));
  }
  frame++;m.options.hideGui=true;
  if(frame%20==0&&stage!=6&&stage!=7)m.getSingleplayerServer().execute(()->{var entity=m.getSingleplayerServer().overworld().getEntity(id);if(entity instanceof RedMage r)r.scene(0);});
  if(frame==1){m.setScreen(null);m.options.hideGui=true;m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.options.fov().set(60);m.options.bobView().set(false);m.getToasts().clear();}
  if(stage==2&&frame==22)MageEyeStates.burst(e);
  if(stage==4){e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180+(float)(30*Math.sin(frame*.035));e.setXRot((float)(8*Math.sin(frame*.05)));}
  if(frame%2==0&&(stage!=0||frame>=30)){var f=MageEyeStates.sample(e,0);var renderer=(software.bernie.geckolib.renderer.GeoEntityRenderer<RedMage>)m.getEntityRenderDispatcher().getRenderer(e);var model=renderer.getGeoModel();
   float l=model.getBone("lid_left").map(b->b.getScaleY()).orElse(-1f),r=model.getBone("lid_right").map(b->b.getScaleY()).orElse(-1f);
   System.out.println("EYE_SAMPLE stage="+N[stage]+" frame="+frame+" tick="+e.tickCount+" openL="+f.leftOpening()+" openR="+f.rightOpening()+" closure="+f.blinkClosure()+" burst="+f.burstActive()+" actualMaskL="+l+" actualMaskR="+r+" expression="+e.expression()+" head="+e.yHeadRot);
   capture(m,String.format("%02d-%s-%03d",stage,N[stage],frame));
  }
  if(frame>=(stage==8?240:120)){frame=0;requested=false;configured=false;stage++;}
 }
 static void prepare(Minecraft m,int a){var s=m.getSingleplayerServer();var p=s.getPlayerList().getPlayers().getFirst();var l=p.serverLevel();
  p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,s);
  RedMage e;if(a==0||a==6||a==8){for(var old:l.getEntitiesOfClass(RedMage.class,p.getBoundingBox().inflate(128)))old.discard();
   e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);id=e.getId();
  }else e=(RedMage)l.getEntity(id);
  if(e==null)throw new IllegalStateException("QA entity missing");
  if(a==5){e.setHealth(18);e.updateCombatPhase();}if(a==6)e.scene(9);if(a==7)e.scene(1);
  p.connection.teleport(44.5,-59.86,-9.45,0,0);
 }
 static void capture(Minecraft m,String name){var d=new java.io.File(System.getProperty("kelmagic.eyeQA.output"));d.mkdirs();Screenshot.grab(d,name+".png",m.getMainRenderTarget(),v->{});}
}
