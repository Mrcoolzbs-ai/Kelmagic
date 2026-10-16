package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.entity.RedMage;
import dev.kelmagic.client.face.*;
import dev.kelmagic.speech.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Automated native GeckoLib renderer + integrated-server smoke test, NOT manual gameplay approval. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaF3Speech {
 static int wait,frame,id;static boolean opened,requested,configured;static volatile boolean ready;
 static boolean sawSpeech,sawReturn,sawCancel,sawReplacement;static final java.util.Set<String> renderedTextures=new java.util.HashSet<>();static int checks;static int firstId,replacementId;
 static void ok(boolean c,String s){checks++;if(!c)throw new AssertionError("F3_NATIVE: "+s);}
 static void server(java.util.function.Consumer<RedMage> action){var m=Minecraft.getInstance();m.getSingleplayerServer().execute(()->action.accept((RedMage)m.getSingleplayerServer().overworld().getEntity(id)));}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.f3QA"))return;var m=Minecraft.getInstance();
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++wait>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicF3QA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;m.getSingleplayerServer().execute(()->{
   var l=m.getSingleplayerServer().overworld();var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
   p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);p.connection.teleport(44.5,-60,-12,0,0);
   var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setNoGravity(false);e.setPos(44.5,-60,-8);l.addFreshEntity(e);id=e.getId();ready=true;
  });return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;MouthStates.selectEmotion(e,MouthTimeline.Emotion.SMILE);MageEyeStates.blinkProfile(e,new EyeStateController.BlinkProfile(.1,.05,.16,1000,1000));m.options.guiScale().set(2);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.resizeDisplay();m.getToasts().clear();m.setScreen(new Preview(e));}
  frame++;
  if(frame==4)server(a->a.beginCharacterSpeech("mina rap",RedMage.SpeechKind.KEL));
  if(frame==12)MouthStates.selectEmotion(e,MouthTimeline.Emotion.LAUGH);
  var f=MouthStates.frame(e,0);var eyes=MageEyeStates.sample(e,0);
  ok(Math.abs(MouthStates.noseOffset(e.tickCount,true)-BreathingPresentation.noseY(RedMage.breathing(e.tickCount),true))<.000001,"breath unaffected by speech");
  ok(eyes.leftOpening()>=0&&eyes.leftOpening()<=1,"independent eye state remains valid");
  if(frame>=6&&frame<=27&&f.speaking())sawSpeech=true;
  if(frame==30){ok(sawSpeech,"synced NPC line reached client and became visemes");ok(!f.speaking()&&f.baseEmotion()==MouthTimeline.Emotion.LAUGH,"finish returns CURRENT emotion");sawReturn=true;}
  if(frame==33)server(a->a.beginCharacterSpeech("palvāk",RedMage.SpeechKind.KEL));
  if(frame==35)firstId=e.characterSpeech().id();
  if(frame==36)server(a->a.beginCharacterSpeech("mina liv",RedMage.SpeechKind.KEL));
  if(frame==39){replacementId=e.characterSpeech().id();ok(replacementId!=firstId&&e.characterSpeech().text().equals("mina liv"),"replacement crosses native entity synchronization");sawReplacement=true;server(RedMage::stopCharacterSpeech);}
  if(frame==43){ok(!f.speaking()&&e.characterSpeech().text().isEmpty(),"server cancellation reaches client");sawCancel=true;server(a->a.beginCharacterSpeech("",RedMage.SpeechKind.KEL));}
  if(frame==47)ok(!f.speaking(),"blank remains at base");
  if(frame%2==0&&frame<=48)System.out.println("F3_NATIVE_SAMPLE frame="+frame+" text="+e.characterSpeech().text()+" texture="+MouthStates.texture(e,0)+" speaking="+f.speaking()+" base="+f.baseEmotion()+" eyes="+eyes.leftOpening()+" cheek="+eyes.cheekLeft()+" nose="+MouthStates.noseOffset(e.tickCount,true));
  if(frame==8||frame==14||frame==22||frame==30||frame==43)capture(m,"speech-"+frame);
  if(frame>=50){int index=(frame-50)/16;
   if(index<9&&(frame-50)%16==0)MouthStates.selectEmotion(e,MouthTimeline.Emotion.values()[index]);
   if(index<9&&(frame-50)%16==10){ok(!f.speaking(),"emotion gallery idle");capture(m,"emotion-"+MouthTimeline.Emotion.values()[index].assetId());}
   if(frame==194)server(a->a.beginCharacterSpeech("aaaaaaaaaaaa oooooooooooo uuuuuuuuuuuu",RedMage.SpeechKind.KEL));
   if(frame>=315){ok(sawSpeech&&sawReturn&&sawCancel&&sawReplacement,"all native scenarios observed");ok(renderedTextures.stream().filter(t->t.startsWith("visemes/")).count()>=3,"at least three actual rendered visemes");System.out.println("F3_NATIVE_COMPLETE checks="+checks+"; automated rendering/integrated server only");m.stop();}
  }
 }
 static void capture(Minecraft m,String name){var d=new java.io.File(System.getProperty("kelmagic.f3QA.output"));d.mkdirs();Screenshot.grab(d,name+".png",m.getMainRenderTarget(),v->{});}
 static class Preview extends Screen {
  final RedMage e;Preview(RedMage e){super(Component.literal("F3 native speech QA"));this.e=e;}public boolean isPauseScreen(){return false;}
  public void render(GuiGraphics g,int x,int y,float pt){g.fill(0,0,width,height,0xFF26363A);var f=MouthStates.frame(e,pt);
   g.drawCenteredString(font,"F3 · native GeckoLib · "+f.layer()+"/"+f.assetId(),width/2,12,0xFFF3E2AF);
   e.yBodyRot=e.yBodyRotO=180;e.yHeadRot=e.yHeadRotO=180;e.setYRot(180);e.setOnGround(true);e.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
   InventoryScreen.renderEntityInInventory(g,width/2f,height/2f,320,new org.joml.Vector3f(0,1.85f,0),new org.joml.Quaternionf().rotationZ((float)Math.PI),new org.joml.Quaternionf(),e);
   g.flush();String rendered=f.layer()+"/"+f.assetId();
   if(renderedTextures.add(rendered)){capture(Minecraft.getInstance(),"rendered-"+rendered.replace('/','-'));System.out.println("F3_NATIVE_RENDERED "+rendered+" frame="+frame);}
  }
 }
}
