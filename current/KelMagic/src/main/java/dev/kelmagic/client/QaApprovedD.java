package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.entity.RedMage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Disposable native world-camera test. No model/controller/clip modifications. Excluded from release JAR by Qa* rule. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaApprovedD {
 static int stage=3,frame,wait,id;static boolean opened,requested,blink;static volatile boolean ready;
 static final String[] N={"front-distance","angle45-distance","profile-distance","front-close","angle45-close","profile-close","head-turn-existing","talk-existing","read-existing","angry-existing"};
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post ev){
  if(!Boolean.getBoolean("kelmagic.approvedDQA"))return;var m=Minecraft.getInstance();
  if(stage>=N.length){System.out.println("APPROVED_D_QA_COMPLETE");m.stop();return;}
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicApprovedD",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;ready=false;int a=stage;m.getSingleplayerServer().execute(()->{prepare(m,a);ready=true;});return;}
  if(!ready)return;frame++;
  if(frame==10){m.setScreen(null);m.options.hideGui=true;m.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);m.options.fov().set(60);m.options.bobView().set(false);m.getToasts().clear();}
  var e=m.level.getEntity(id);
  if(e instanceof RedMage r){
   if(stage==6){r.yBodyRot=r.yBodyRotO=180;r.yHeadRot=r.yHeadRotO=225;r.setXRot(10);}
   int phase=(r.tickCount+r.getId()*13)%83;
   if((phase<=6&&frame>25)||frame==60){
    var renderer=(software.bernie.geckolib.renderer.GeoEntityRenderer<RedMage>)m.getEntityRenderDispatcher().getRenderer(r);
    var model=renderer.getGeoModel();
    model.getBone("lid_left").ifPresent(b->System.out.println("D_NATIVE_LID stage="+N[stage]+" phase="+phase+" scaleY="+b.getScaleY()+" pivotY="+b.getPivotY()+" posY="+b.getPosY()));
    model.getBone("pupil_left").ifPresent(b->System.out.println("D_NATIVE_PUPIL stage="+N[stage]+" phase="+phase+" posX="+b.getPosX()+" posY="+b.getPosY()));
    if(phase<=6)capture(m,N[stage]+"-phase"+phase);
   }
   if(frame%10==0)System.out.println("D_SAMPLE "+N[stage]+" frame="+frame+" tick="+r.tickCount+" blink="+((r.tickCount+r.getId()*13)%83<5)+" scene="+r.scene()+" mood="+r.expression()+" head="+r.yHeadRot+" body="+r.yBodyRot+" mouth="+r.mouth(0));
   if(frame>25&&!blink&&(r.tickCount+r.getId()*13)%83<5){capture(m,N[stage]+"-blink");blink=true;}
  }
  if(frame==40||frame==42||frame==70||frame==95)capture(m,N[stage]+"-"+frame);
  if(frame>=110){frame=0;requested=false;blink=false;stage++;}
 }
 static void prepare(Minecraft m,int a){var s=m.getSingleplayerServer();var p=s.getPlayerList().getPlayers().getFirst();var l=p.serverLevel();
  p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);l.setDayTime(6000);l.setWeatherParameters(6000,0,false,false);
  l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DAYLIGHT).set(false,s);
  if(a==0){for(var e:l.getEntitiesOfClass(RedMage.class,p.getBoundingBox().inflate(128)))e.discard();
   for(int x=40;x<49;x++)for(int z=-14;z<-2;z++)for(int y=-60;y<-55;y++)l.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
  if(a>0){var old=l.getEntity(id);if(old!=null)old.discard();}
  var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);id=e.getId();
  int view=a<3?a:a<6?a-3:0;double d=a<3?5.0:1.45;double theta=Math.toRadians(view==1?45:view==2?90:0);
  p.connection.teleport(44.5+Math.sin(theta)*d,-59.86,-8-Math.cos(theta)*d,(float)Math.toDegrees(theta),0);
  if(a==6){e.setYHeadRot(225);e.yHeadRotO=225;}
  if(a==7)e.scene(9);if(a==8)e.scene(2);if(a==9){e.setHealth(18);e.updateCombatPhase();}
  System.out.println("APPROVED_D_STAGE "+N[a]+" distance="+d+" yaw="+Math.toDegrees(theta)+" shader=none noon=6000");
 }
 static void capture(Minecraft m,String name){var d=new java.io.File(System.getProperty("kelmagic.approvedD.output"));d.mkdirs();Screenshot.grab(d,name+".png",m.getMainRenderTarget(),v->{});System.out.println("D_SCREENSHOT "+name);}
}
