package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.client.face.*;
import dev.kelmagic.client.diag.FrameAllocation;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.phys.Vec3;import net.minecraft.core.*;import net.minecraft.world.entity.MoverType;import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
import java.nio.file.*;import java.io.*;
/** Opt-in IN-WORLD integrated-server exercise. Identical source is also compiled into baseline
 * development classpath. Never shipped in the TEST jar (existing Qa* exclusion).
 * Server movement is deterministic QA input, not new NPC navigation/gameplay.
 */
@EventBusSubscriber(modid="kelmagic",value=Dist.CLIENT)
public final class QaMotion {
 static final boolean ENABLED=Boolean.getBoolean("kelmagic.motionQA");
 static final String[] STAGES={"idle","walk","run","slow-stop","backward","strafe","head65","wrap180","jump-land","teleport-short","teleport-far-yaw","teleport-series","talk-walk","cast-interrupt","two-mages","table-codex"};
 static int wait,frame,id,id2,stage=-1,repeat;static boolean opened,requested,configured;static volatile boolean ready;static int stageTicks;
 static double vertical; static long pre,mem,last;static PrintWriter csv;static Path output;
 @jdk.jfr.Name("kelmagic.MotionWindow") static class Window extends jdk.jfr.Event {public int repeat;public String config;}
 static Window window;
 static void server(java.util.function.BiConsumer<net.minecraft.server.level.ServerLevel,RedMage> a){var m=Minecraft.getInstance();m.getSingleplayerServer().execute(()->{var l=m.getSingleplayerServer().overworld();if(l.getEntity(id) instanceof RedMage e)a.accept(l,e);});}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post ev){
  if(!ENABLED)return;var m=Minecraft.getInstance();
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>20){opened=true;m.createWorldOpenFlows().openWorld("KelMagicMotionQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;m.getSingleplayerServer().execute(()->{
   var l=m.getSingleplayerServer().overworld();l.setDayTime(6000);l.setWeatherParameters(0,12000,false,false);
   for(var item:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(15,-64,-35,85,-40,25)))item.discard();
   for(var old:l.getEntitiesOfClass(RedMage.class,new net.minecraft.world.phys.AABB(15,-64,-35,85,-40,25)))old.discard();
   for(BlockPos b:BlockPos.betweenClosed(18,-61,-32,80,-61,20))l.setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
   var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);p.connection.teleport(44.5,-59.5,-12.5,0,5);
   for(int n=0;n<2;n++){var e=MagicCreatures.RED_MAGE.get().create(l);e.setUUID(new java.util.UUID(100,100+n));e.awaken();e.setNoAi(true);e.setPos(44.5+n*2,-60,-8);e.setYRot(180);e.yBodyRot=e.yBodyRotO=e.yHeadRot=e.yHeadRotO=180;l.addFreshEntity(e);if(n==0)id=e.getId();else{id2=e.getId();e.setInvisible(true);}}
   ready=true;
  });return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;
   output=Path.of(System.getProperty("kelmagic.motionQA.output"));try{Files.createDirectories(output);csv=new PrintWriter(Files.newBufferedWriter(output.resolve("native-frames.csv")));csv.println("repeat,stage,tick,frame_interval_ns,render_elapsed_ns,client_allocated_bytes,npc_x,npc_y,npc_z,npc_tick");}catch(IOException ex){throw new RuntimeException(ex);}
   m.options.enableVsync().set(false);m.options.framerateLimit().set(120);m.options.renderDistance().set(3);m.options.simulationDistance().set(5);m.options.guiScale().set(2);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.options.hideGui=true;m.getToasts().clear();m.setScreen(null);m.resizeDisplay();
  }
  int next=frame/640;if(next>Integer.getInteger("kelmagic.motionQA.maxRepeat",3)){if(window!=null)window.commit();csv.close();System.out.println("MOTION_QA_COMPLETE configured_repeats; IN_WORLD; softwareGL");m.stop();return;}
  if(next!=repeat||window==null){if(window!=null)window.commit();repeat=next;window=new Window();window.repeat=repeat;window.config=System.getProperty("kelmagic.motionQA.label");window.begin();}
  int s=frame/40%STAGES.length;stageTicks=frame%40;
  if(s!=stage){stage=s;vertical=-.08;int index=s;
   server((l,a)->{a.scene(0);a.stopCharacterSpeech();a.setDeltaMovement(Vec3.ZERO);a.setPos(44.5,-60,-8);a.setYRot(180);a.yBodyRot=a.yBodyRotO=a.yHeadRot=a.yHeadRotO=180;
    if(l.getEntity(id2) instanceof RedMage b){b.setInvisible(false);b.setPos(index==14?46.5:74.5,-60,-8);}
    var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.connection.teleport(44.5,-59.5,-12.5,0,5);
    if(index==12){a.scene(9);a.beginCharacterSpeech("Мама попросила папу. Please bring my blue book.",RedMage.SpeechKind.DIALOGUE);}
    if(index==13)a.scene(10);
    if(index==15){var at=new BlockPos(44,-60,-8);l.setBlockAndUpdate(at,MagicStudy.TABLE.get().defaultBlockState());if(l.getBlockEntity(at) instanceof dev.kelmagic.archive.ArchiveTableEntity t)t.mount(new net.minecraft.world.item.ItemStack(MagicArchive.CREATIVE_CODEX.get()));a.setInvisible(false);a.setPos(76.5,-60,5);p.connection.teleport(44.5,-60,-5.6,180,23);}
    else {a.setInvisible(false);var at=new BlockPos(44,-60,-8);if(l.getBlockEntity(at) instanceof dev.kelmagic.archive.ArchiveTableEntity table)table.unmount();l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());}
   });
   MageEyeStates.sustainedSquint(e,s==12?.28f:0);if(s==12)MageEyeStates.burst(e);
   if(s==6||s==7)MageGazeStates.selectPoint(e,e.getEyePosition().add(s==6?4:-4,0,-2),"motion QA head",2);else MageGazeStates.clearSelection(e);
   System.out.println("MOTION_STAGE repeat="+repeat+" stage="+STAGES[s]);
  }
  int t=stageTicks;
  server((l,a)->{
   double dz=stage==1?-.085:stage==2?-.19:stage==3?(t<20?-.19*(1-t/20.):0):stage==4?.085:stage==12?-.06:0;
   // NoAI QA must move with downward contact as normal travel does; dy=0 clears grounded.
   if(stage==8&&t==6)vertical=.42;
   else vertical=a.onGround()&&vertical<=0?-.08:(vertical-.08)*.98;
   a.move(MoverType.SELF,new Vec3(stage==5?.085:0,vertical,dz));if(a.onGround())vertical=-.08;
   if(t==26)System.out.println("GROUND stage="+STAGES[stage]+" server="+a.onGround()+" y="+a.getY());
   // Preserve requested backward/strafe facing in QA. No production field mutation by controller.
   a.setYRot(stage==10&&t>=12?90:180);a.yBodyRot=a.getYRot();a.yHeadRot=a.getYRot();
   if(stage==7){a.setYRot(t<20?179:-179);a.yBodyRot=a.yHeadRot=a.getYRot();}
   
   if(stage==9&&t==12){boolean success=a.randomTeleport(a.getX()+1,a.getY()+1,a.getZ(),true);System.out.println("SHORT_TELEPORT success="+success);}
   if(stage==10&&t==12){a.randomTeleport(a.getX()+12,a.getY()+2,a.getZ(),true);a.setYRot(90);a.yBodyRot=a.yHeadRot=90;var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.connection.teleport(a.getX()-4.5,-59.5,a.getZ(),-90,5);}
   if(stage==11&&(t==8||t==16||t==24)){a.randomTeleport(a.getX()+(t==16?-1:1),a.getY()+2,a.getZ(),true);}
   if(stage==14&&l.getEntity(id2) instanceof RedMage b){a.move(MoverType.SELF,new Vec3(0,-.08,.04));b.move(MoverType.SELF,new Vec3(0,-.08,-.04));}
   if(stage==15&&t%10==0){var pos=new BlockPos(44,-60,-8);var face=Direction.from2DDataValue(t/10);l.setBlockAndUpdate(pos,MagicStudy.TABLE.get().defaultBlockState().setValue(dev.kelmagic.archive.ArchiveTableBlock.FACING,face));if(l.getBlockEntity(pos) instanceof dev.kelmagic.archive.ArchiveTableEntity table&&!table.hasCodex())table.mount(new net.minecraft.world.item.ItemStack(MagicArchive.CREATIVE_CODEX.get()));System.out.println("TABLE facing="+face+" bookCenter="+dev.kelmagic.archive.TableOrientation.bookCenter(pos,l.getBlockState(pos)));}
  });
  // QA camera only. The NPC actually moves on the integrated server; not an inventory pose.
  if(s!=15){double angle=Math.toRadians(s==10&&t>=14?90:s==7?(t<20?179:-179):180);if(Boolean.getBoolean("kelmagic.motionQA.quarter"))angle+=Math.PI/3;double cx=e.getX()-Math.sin(angle)*4.5,cz=e.getZ()+Math.cos(angle)*4.5;
   m.player.setPos(cx,e.getY()+.5,cz);m.player.xo=cx;m.player.yo=e.getY()+.5;m.player.zo=cz;m.player.setYRot((float)Math.toDegrees(angle)+180);m.player.yRotO=m.player.getYRot();m.player.setXRot(6);m.player.xRotO=6;
  }
  if(s==15){
   var at=new BlockPos(44,-60,-8);var bs=m.level.getBlockState(at);var target=dev.kelmagic.archive.TableOrientation.bookCenter(at,bs);var dir=dev.kelmagic.archive.TableOrientation.rotate(bs.hasProperty(dev.kelmagic.archive.ArchiveTableBlock.FACING)?bs.getValue(dev.kelmagic.archive.ArchiveTableBlock.FACING):Direction.NORTH,new Vec3(0,0,1));
   if(t>=30)dir=new Vec3(dir.x*.5-dir.z*.8660254,0,dir.x*.8660254+dir.z*.5);
   double cx=target.x+dir.x*1.5,cz=target.z+dir.z*1.5,cy=target.y-.75;
   m.player.setPos(cx,cy,cz);m.player.xo=cx;m.player.yo=cy;m.player.zo=cz;
   m.player.setYRot((float)Math.toDegrees(Math.atan2(dir.x,-dir.z)));m.player.yRotO=m.player.getYRot();
   m.player.setXRot((float)Math.toDegrees(Math.atan2(m.player.getEyeY()-target.y,1.5)));m.player.xRotO=m.player.getXRot();
   if(t==26||t==34){var hit=TableBookClient.hit();System.out.println("PHYSICAL_PAGE_HIT facing="+bs+" hit="+(hit==null?"NONE":hit.x()+","+hit.y()+" distance="+hit.distance()));}
  }
  if(repeat==1&&(t==26||((s==1||s==2||s==12||s==15)&&(t==10||t==18||t==34))||((s==9||s==10||s==11||s==8)&&(t==12||t==14||t==18||t==30))))capture(m,STAGES[s]+"-"+t);
  if(s==15&&t==26){var b=m.level.getBlockEntity(new BlockPos(44,-60,-8));System.out.println("TABLE_CLIENT "+b+" mounted="+(b instanceof dev.kelmagic.archive.ArchiveTableEntity tab&&tab.hasCodex()));}
  frame++;
 }
 static void capture(Minecraft m,String n){
  try{Class<?> c=Class.forName("dev.kelmagic.client.motion.MageMotion");var f=c.getDeclaredField("STATES");f.setAccessible(true);Object state=((java.util.Map<?,?>)f.get(null)).get(m.level.getEntity(id));
   if(state!=null){var sc=state.getClass();System.out.println("POSE "+n+" clientGround="+m.level.getEntity(id).onGround()+" speed="+sc.getField("speed").get(state)+" move="+sc.getField("move").get(state)+" run="+sc.getField("run").get(state)+" weight="+sc.getField("weight").get(state)+" left_leg="+java.util.Arrays.toString(((float[][])sc.getField("pose").get(state))[3]));}
  }catch(ReflectiveOperationException ignored){}
  if(m.level.getEntity(id) instanceof RedMage mage && m.getEntityRenderDispatcher().getRenderer(mage) instanceof RedMageRenderer renderer){
   for(String bone:new String[]{"left_arm","right_arm","left_forearm","right_forearm","left_shin","right_shin"})renderer.getGeoModel().getBone(bone).ifPresent(b->System.out.println("ACTUAL_BONE "+n+" "+bone+"="+b.getRotX()+","+b.getRotY()+","+b.getRotZ()));
  }
  Screenshot.grab(output.toFile(),n+".png",m.getMainRenderTarget(),c->{});}
 @SubscribeEvent public static void pre(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre e){if(!ENABLED||!configured)return;pre=System.nanoTime();mem=FrameAllocation.read();}
 @SubscribeEvent public static void post(net.neoforged.neoforge.client.event.RenderFrameEvent.Post e){if(!ENABLED||!configured||csv==null)return;var m=Minecraft.getInstance();long now=System.nanoTime();if(m.level!=null&&m.level.getEntity(id) instanceof RedMage a){csv.printf(java.util.Locale.ROOT,"%d,%s,%d,%d,%d,%d,%.5f,%.5f,%.5f,%d%n",repeat,STAGES[Math.max(0,stage)],stageTicks,last==0?0:pre-last,now-pre,FrameAllocation.read()-mem,a.getX(),a.getY(),a.getZ(),a.tickCount);}last=pre;}
}
