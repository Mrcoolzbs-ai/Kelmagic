package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.audio.*;
import dev.kelmagic.network.TapeSelect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
public final class AudioScreens {
 @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static class Registration {
 @SubscribeEvent public static void properties(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{for(var item:java.util.stream.Stream.concat(MagicContent.PLATE_ITEMS.stream(),MagicContent.ROBE_ITEMS.stream()).toList())net.minecraft.client.renderer.item.ItemProperties.register(item.get(),ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"trimmed"),(s,l,p,seed)->TrimAppearance.index(s));net.minecraft.client.renderer.item.ItemProperties.register(MagicAudio.CASSETTE.get(),ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"recorded"),(s,l,p,seed)->CassetteItem.recorded(s)?1:0);
 net.minecraft.client.renderer.item.ItemProperties.register(MagicAudio.CASSETTE.get(),ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"pattern"),(s,l,p,seed)->CassetteItem.patternIndex(s));});}
@SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e){e.register(MagicStudy.TABLE_MENU.get(),ArchiveTableScreen::new);e.register(MagicAudio.PLAYER_MENU.get(),PlayerScreen::new);e.register(MagicAudio.DECK_MENU.get(),DeckScreen::new);}}
 @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
 public static class Input {@SubscribeEvent public static void scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent e){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||!mc.player.isShiftKeyDown()||e.getScrollDeltaY()==0)return;if(mc.player.getMainHandItem().getItem() instanceof TapePlayerItem||mc.player.getOffhandItem().getItem() instanceof TapePlayerItem){e.setCanceled(true);net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TapeSelect(e.getScrollDeltaY()>0?-1:1));}}}
 static ResourceLocation tex(String id){return ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"textures/gui/"+id+".png");}
 public static class PlayerScreen extends AbstractContainerScreen<TapeMenu>{
  public PlayerScreen(TapeMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=208;inventoryLabelY=114;titleLabelX=12;titleLabelY=9;}
  @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.blit(tex("tape_player"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);for(int n=0;n<8;n++)g.drawString(font,String.valueOf(n+1),leftPos+29+(n%4)*38,topPos+43+(n/4)*29,0xAA947D,false);int i=TapePlayerItem.active(menu.holder),x=leftPos+22+(i%4)*38,y=topPos+52+(i/4)*29;g.renderOutline(x,y,20,20,0xFFF2CA76);g.drawString(font,Component.translatable("kelmagic.tape.selected",i+1),leftPos+12,topPos+30,0xEDCDA0,false);}
  @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xFFE8BF,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0xBFA88A,false);}
  @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
 }
 public static class DeckScreen extends AbstractContainerScreen<DeckMenu>{
  public DeckScreen(DeckMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=197;titleLabelX=12;titleLabelY=10;inventoryLabelY=103;}
  @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.blit(tex("tape_deck"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);int total=menu.data.get(1),progress=menu.data.get(0);g.fill(leftPos+23,topPos+84,leftPos+153,topPos+88,0xFF29262E);if(total>0)g.fill(leftPos+23,topPos+84,leftPos+23+(int)(130L*progress/total),topPos+88,0xFFD8AC62);g.drawString(font,Component.translatable(!menu.machine.getItem(2).isEmpty()?"kelmagic.deck.done":total>0?"kelmagic.deck.remaining":"kelmagic.deck.idle",Math.max(0,(total-progress+19)/20)),leftPos+12,topPos+29,0xE7CDA6,false);}
  @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xFFE8BF,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0xBFA88A,false);}
  @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
 }
}
