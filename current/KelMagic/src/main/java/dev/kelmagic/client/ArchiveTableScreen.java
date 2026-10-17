package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.archive.ArchiveTableMenu;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.world.entity.player.Inventory;import net.minecraft.network.chat.Component;import net.minecraft.resources.ResourceLocation;
public final class ArchiveTableScreen extends AbstractContainerScreen<ArchiveTableMenu>{
 public ArchiveTableScreen(ArchiveTableMenu m,Inventory i,Component t){super(m,i,t);imageWidth=210;imageHeight=194;titleLabelX=16;titleLabelY=10;inventoryLabelX=24;inventoryLabelY=101;}
 protected void renderBg(GuiGraphics g,float p,int x,int y){g.blit(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/gui/archive_table.png"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);g.drawString(font,Component.translatable("kelmagic.table.hint"),leftPos+16,topPos+30,0xA9C6C7,false);}
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xEAF4D4,false);g.drawString(font,playerInventoryTitle,24,101,0x9CA995,false);}
 public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}