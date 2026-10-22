package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.core.*;import dev.kelmagic.network.*;import dev.kelmagic.server.*;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.TitleScreen;import net.minecraft.core.*;import net.minecraft.world.level.block.Blocks;import net.minecraft.world.phys.*;import net.minecraft.server.level.ServerPlayer;
import java.nio.file.*;import java.util.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="kelmagic",value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class QaR6Phrases {
 static final Path OUT=Path.of(System.getProperty("kelmagic.qaOutput","qa-r6"));static final BlockPos TARGET=new BlockPos(44,-61,-8);
 static final String[] NAMES={"fire-ring","storm","guardian","failed-target","cancelled","duplicate","minimal","spam","expiry"};
 static int wait,frame,stage=-1,checks,baseline,shots;static boolean opened,requested,configured;static volatile boolean ready,positioned;static int savedParticle;
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError("R6 "+s);}
 static void server(java.util.function.Consumer<ServerPlayer> f){var m=Minecraft.getInstance();m.getSingleplayerServer().execute(()->{try{f.accept(m.getSingleplayerServer().getPlayerList().getPlayers().getFirst());}catch(Throwable e){e.printStackTrace();System.out.println("R6_SERVER_FAILURE "+e);m.execute(()->{throw new RuntimeException(e);});}});}
 static void aim(ServerPlayer p,Vec3 target){Vec3 v=target.subtract(p.getEyePosition());float yaw=(float)Math.toDegrees(Math.atan2(-v.x,v.z)),pitch=(float)-Math.toDegrees(Math.atan2(v.y,Math.sqrt(v.x*v.x+v.z*v.z)));p.connection.teleport(p.getX(),p.getY(),p.getZ(),yaw,pitch);}
 static void cast(ServerPlayer p,int kind,boolean cancel,boolean duplicate){
  List<String> w=kind==0?List.of("vit","pir"):kind==1?List.of("vin","dun"):List.of("kor","ten");var session=p.getData(MagicAttachments.SESSION);session.clear();for(String word:w)session.append(word);var action=new SpellAction(session.generation(),session.revision(),cancel);MagicServer.action(p,action);ok(!session.active(),"session consumed/cancelled");if(duplicate)MagicServer.action(p,action);
  if(!cancel&&kind==2)ok(p.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE)&&p.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),"guardian gameplay buffs retained");
 }
 static void codec(ServerPlayer p){
  var b=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),p.registryAccess());try{var v=new PhraseVisual(2,44.5,-60,-7.5,1,42,p.getId());PhraseVisual.CODEC.encode(b,v);ok(PhraseVisual.CODEC.decode(b).equals(v),"network roundtrip");}finally{b.release();}
  ok(!new PhraseVisual(3,0,0,0,1,0,-1).valid(),"invalid kind");ok(!new PhraseVisual(0,Double.NaN,0,0,1,0,-1).valid(),"NaN position");ok(!new PhraseVisual(0,0,0,0,Float.POSITIVE_INFINITY,0,-1).valid(),"infinite scale");
  System.out.println("R6_SERVER_CODEC_PASS");
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
  if(!Boolean.getBoolean("kelmagic.r6QA"))return;var m=Minecraft.getInstance();if(m.getOverlay()!=null)return;
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicPhrasesQA",()->{});}return;}if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;server(p->{QaR3Checks.server(p);codec(p);p.closeContainer();p.getInventory().clearContent();p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();var l=p.serverLevel();l.setDayTime(11500);l.setWeatherParameters(12000,12000,false,false);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_WEATHER_CYCLE).set(false,l.getServer());l.setRainLevel(0);l.setThunderLevel(0);l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,l.getServer());l.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK).set(false,l.getServer());p.connection.teleport(44.5,-60,0,180,40);ready=true;});return;}if(!ready)return;m.level.setRainLevel(0);m.level.setThunderLevel(0);
  if(!configured){configured=true;try{Files.createDirectories(OUT);}catch(Exception x){throw new RuntimeException(x);}m.setScreen(null);m.options.enableVsync().set(false);m.options.framerateLimit().set(60);m.options.renderDistance().set(3);m.options.simulationDistance().set(5);m.options.guiScale().set(2);m.resizeDisplay();m.options.fov().set(70);m.options.hideGui=true;m.options.particles().set(ParticleStatus.ALL);m.getToasts().clear();m.options.setCameraType(CameraType.FIRST_PERSON);}
  int s=frame/110,t=frame%110;if(s>=NAMES.length){System.out.println("R6_QA_COMPLETE curios="+net.neoforged.fml.ModList.get().isLoaded("curios")+" checks="+checks+" realServerCasts/S2C/expiry/budgets");m.stop();return;}
  if(stage!=s){stage=s;shots=0;baseline=PhraseEffects.accepted;positioned=false;System.out.println("R6_STAGE "+s+" "+NAMES[s]);m.options.setCameraType(s==2||s==5?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);server(p->{var l=p.serverLevel();for(var b:BlockPos.betweenClosed(35,-60,-17,53,-55,6)){if(l.getBlockEntity(b) instanceof dev.kelmagic.archive.ArchiveTableEntity table)table.unmount();l.setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());}for(var b:BlockPos.betweenClosed(35,-61,-17,53,-61,6))l.setBlockAndUpdate(b,Blocks.DEEPSLATE_TILES.defaultBlockState());for(var entity:l.getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,new AABB(TARGET).inflate(32)))entity.discard();p.removeAllEffects();p.connection.teleport(44.5,s==2||s==5?-60:-58.6,s==2||s==5?-3.5:-1.5,180,s==2||s==5?12:40);if(s!=2&&s!=5)aim(p,Vec3.atCenterOf(TARGET));positioned=true;});}
  if(!positioned)return;
  if(t==15){if(s==0||s==1||s==2||s==5)server(p->cast(p,s==0?0:s==1?1:2,false,s==5));if(s==3)server(p->{p.connection.teleport(p.getX(),p.getY(),p.getZ(),180,-80);cast(p,0,false,false);});if(s==4)server(p->cast(p,2,true,false));
   if(s==8){int before=PhraseEffects.accepted;PhraseEffects.accept(new PhraseVisual(9,0,0,0,1,0,-1));PhraseEffects.accept(new PhraseVisual(0,Double.NaN,0,0,1,0,-1));PhraseEffects.accept(new PhraseVisual(0,10000,0,10000,1,0,-1));ok(PhraseEffects.accepted==before,"invalid and distant packets ignored");}
   if(s==6||s==7){m.options.particles().set(s==6?ParticleStatus.MINIMAL:ParticleStatus.ALL);var at=m.player.position().add(0,0,-5);int n=s==7?100:1;for(int i=0;i<n;i++)PhraseEffects.accept(new PhraseVisual(i%3,at.x,at.y,at.z,1,i,-1));}
  }
  if(t==40){if(s==0||s==1||s==2||s==5)ok(PhraseEffects.accepted-baseline==1,"exactly one S2C cue "+NAMES[s]+" accepted="+(PhraseEffects.accepted-baseline));if(s==3||s==4)ok(PhraseEffects.accepted==baseline,"no cue on failure/cancel");if(s==6)ok(PhraseEffects.spawnedLastTick<=24,"minimal budget");if(s==7)ok(PhraseEffects.active()<=12&&PhraseEffects.spawnedLastTick<=192,"spam bounds");System.out.println("R6_CHECK "+NAMES[s]+" accepted="+(PhraseEffects.accepted-baseline)+" active="+PhraseEffects.active()+" spawned="+PhraseEffects.spawnedLastTick);}
  if(s<=2&&PhraseEffects.oldestAge()>=(shots==0?12:22)&&shots<2){int phase=shots++==0?12:22;System.out.println("R6_CAPTURE "+NAMES[s]+" age="+PhraseEffects.oldestAge());Screenshot.grab(OUT.toFile(),NAMES[s]+"-phase-"+phase+".png",m.getMainRenderTarget(),c->{});}
  if(t==96){ok(PhraseEffects.active()==0,"all cues expired "+NAMES[s]);if(s==6)m.options.particles().set(ParticleStatus.ALL);}
  frame++;
 }
}
