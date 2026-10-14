package dev.kelmagic.client;
import dev.kelmagic.KelMagic;import net.minecraft.client.*;import net.minecraft.client.gui.screens.TitleScreen;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
/** Reopens, photographs and cleanly closes the existing save; never generates buildings. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)public final class QaWorldInspect {
 static boolean opened;static int idle,stage=Integer.getInteger("kelmagic.qaStart",0),frames;static final double[][] VIEWS={{-8,-52,-10,-45,20},{47,-49,144,0,26},{44,-60,43,-15,8},{154,-54,-13,0,24}};
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){if(!Boolean.getBoolean("kelmagic.worldInspectQA")||stage>=5)return;var mc=Minecraft.getInstance();if(mc.level==null){if(!opened&&mc.screen instanceof TitleScreen&&++idle>40){opened=true;mc.createWorldOpenFlows().openWorld(System.getProperty("kelmagic.qaWorld","KelMagicShowcase114"),()->{});}return;}if(mc.player==null||mc.getSingleplayerServer()==null)return;if(Boolean.getBoolean("kelmagic.showcaseQA")&&!QaShowcase.ready)return;
 if(frames++==0){mc.setScreen(null);mc.options.hideGui=true;final int at=stage;mc.getSingleplayerServer().execute(()->{for(var p:mc.getSingleplayerServer().getPlayerList().getPlayers()){if(at==4){p.getAbilities().flying=false;p.onUpdateAbilities();p.connection.teleport(42,-60,-12,0,0);}else{var v=VIEWS[at];p.getAbilities().flying=true;p.onUpdateAbilities();p.connection.teleport(v[0],v[1],v[2],(float)v[3],(float)v[4]);}}});}
 if(stage<4&&frames==80){var dir=new java.io.File("/data/native-world-1.14.1");dir.mkdirs();Screenshot.grab(dir,"world-"+stage+".png",mc.getMainRenderTarget(),m->{});System.out.println("REOPENED_WORLD_SCREENSHOT_"+stage);}
 if(frames>=100){frames=0;if(++stage==5){System.out.println("SHOWCASE_114_REOPENED_AND_SAVED");mc.stop();}}}
 private QaWorldInspect(){}
}
