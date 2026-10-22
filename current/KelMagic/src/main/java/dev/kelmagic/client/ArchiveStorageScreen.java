package dev.kelmagic.client;
import dev.kelmagic.content.ArchiveStorageMenu;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
/** A 54-slot viewport over the whole modular cabinet. */
public final class ArchiveStorageScreen extends AbstractContainerScreen<ArchiveStorageMenu> {
 public ArchiveStorageScreen(ArchiveStorageMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=244;titleLabelX=9;titleLabelY=8;inventoryLabelY=146;}
 @Override protected void init(){super.init();addRenderableWidget(CodexPaint.button(font,"‹",leftPos+8,topPos+16,20,14,()->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)));addRenderableWidget(CodexPaint.button(font,"›",leftPos+148,topPos+16,20,14,()->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1)));}
 @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){int x=leftPos,y=topPos;g.fill(x,y,x+176,y+244,0xFFB99B65);g.fill(x+2,y+2,x+174,y+242,0xFF263A43);g.fill(x+5,y+5,x+171,y+239,0xFFDFCE9E);for(int n=0;n<54;n++)slot(g,x+7+n%9*18,y+31+n/9*18,menu.slotAvailable(n));for(int n=0;n<27;n++)slot(g,x+7+n%9*18,y+157+n/9*18,true);for(int n=0;n<9;n++)slot(g,x+7+n*18,y+215,true);g.drawCenteredString(font,Component.translatable("kelmagic.storage_page",menu.page()+1,menu.pages(),menu.modules()),x+88,y+20,0xFF493D32);}
 private void slot(GuiGraphics g,int x,int y,boolean active){g.fill(x,y,x+18,y+18,active?0xFF87764F:0xFFBBAF88);g.fill(x+1,y+1,x+17,y+17,active?0xFFC9BB8F:0xFFBBAF88);g.fill(x+1,y+17,x+18,y+18,0xFFF5E3B5);g.fill(x+17,y+1,x+18,y+18,0xFFF5E3B5);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
