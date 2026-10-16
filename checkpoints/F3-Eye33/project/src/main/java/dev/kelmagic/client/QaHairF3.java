package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.client.face.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.Screenshot;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.screens.Screen;import net.minecraft.client.gui.screens.TitleScreen;import net.minecraft.client.gui.screens.inventory.InventoryScreen;import net.minecraft.network.chat.Component;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
/** Opt-in native before/after geometry viewer. No production behavior; excluded from distributed JAR. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class QaHairF3 {
 static int idle,frame,id,index,shot;static boolean opened,requested,configured;static volatile boolean ready;static final java.util.Set<String> captured=new java.util.HashSet<>();
 static final String[] STATES={"front","quarter","side","rear","head-left","head-right","head-down","head-up","walk","crouch","cast","seated","turn-sweep","turn-sweep-quarter"};
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.hairQA"))return;var m=Minecraft.getInstance();
  if(m.level==null){if(!opened&&m.screen instanceof TitleScreen&&++idle>30){opened=true;m.createWorldOpenFlows().openWorld("KelMagicHairQA",()->{});}return;}
  if(m.player==null||m.getSingleplayerServer()==null)return;
  if(!requested){requested=true;m.getSingleplayerServer().execute(()->{var l=m.getSingleplayerServer().overworld();var p=m.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SPECTATOR);p.connection.teleport(44.5,-60,-12,0,0);var e=MagicCreatures.RED_MAGE.get().create(l);e.awaken();e.setNoAi(true);e.setPos(44.5,-60,-8);l.addFreshEntity(e);id=e.getId();ready=true;});return;}
  if(!ready||!(m.level.getEntity(id) instanceof RedMage e))return;
  if(!configured){configured=true;MageEyeStates.blinkProfile(e,new EyeStateController.BlinkProfile(.1,.05,.16,1000,1000));m.options.guiScale().set(2);m.options.tutorialStep=net.minecraft.client.tutorial.TutorialSteps.NONE;m.resizeDisplay();m.setScreen(new Preview(e));}
  frame++;if(frame>=45&&shot>=4){index++;frame=0;shot=0; if(index<STATES.length){int stage=index;m.getSingleplayerServer().execute(()->configurePose(m,stage));}}
  int count=Boolean.getBoolean("kelmagic.hairQA.baseline")?4:STATES.length;
  if(index>=count){System.out.println("HAIR_NATIVE_COMPLETE mode="+System.getProperty("kelmagic.hairQA.baseline")+" captures="+captured.size());m.stop();index=count-1;return;}
 }
 static void configurePose(Minecraft m,int stage){
  var l=m.getSingleplayerServer().overworld();if(!(l.getEntity(id) instanceof RedMage r))return;
  if(stage==10)r.scene(10);if(stage>=12){if(r.throne().isPresent())r.releaseThrone();r.scene(0);}
  if(stage==11){var pos=r.blockPosition();var st=MagicMascot.THRONE.get().defaultBlockState();l.setBlockAndUpdate(pos,st.setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));l.setBlockAndUpdate(pos.above(),st.setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));r.bindThrone(pos);r.scene(20);}
 }
 static class Preview extends Screen {
  final RedMage e;Preview(RedMage e){super(Component.literal("F3 hair QA"));this.e=e;}public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int mx,int my,float partial){
   g.fill(0,0,width,height,0xFF26363A);String name=STATES[index];float yaw=180+(index==1||index==13?45:index==2?90:index==3?180:0);
   e.yBodyRot=e.yBodyRotO=yaw;e.setYRot(yaw);e.yHeadRot=e.yHeadRotO=yaw+(index==4?-55:index==5?55:0);if(index>=12)e.yHeadRot=e.yHeadRotO=yaw+(float)(55*Math.sin(frame*Math.PI/28));e.setXRot(index==6?25:index==7?-25:0);e.setOnGround(true);e.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);e.setShiftKeyDown(index==9);e.setPose(index==9?net.minecraft.world.entity.Pose.CROUCHING:net.minecraft.world.entity.Pose.STANDING);
   e.walkAnimation.update(index==8?.6f:0,.5f);
   g.drawCenteredString(font,"F3 hair · "+name,width/2,12,0xFFF3E2AF);
   InventoryScreen.renderEntityInInventory(g,width/2f,height/2f,110,new org.joml.Vector3f(0,1.25f,0),new org.joml.Quaternionf().rotationZ((float)Math.PI),new org.joml.Quaternionf(),e);g.flush();
   if(shot<4&&frame>=8+shot*8){shot++;
    var dir=new java.io.File(System.getProperty("kelmagic.hairQA.output"));dir.mkdirs();Screenshot.grab(dir,name+"-"+frame+".png",Minecraft.getInstance().getMainRenderTarget(),msg->{});captured.add(name);System.out.println("HAIR_CAPTURE "+name+" shot="+shot+" frame="+frame+" tick="+e.tickCount+" yaw="+yaw+" actualClip="+e.animationName(index==8)+" scene="+e.scene()+" throne="+e.throne().isPresent());
   }
  }
 }
}
