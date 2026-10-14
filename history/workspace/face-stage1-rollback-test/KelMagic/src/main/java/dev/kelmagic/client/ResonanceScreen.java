package dev.kelmagic.client;
import dev.kelmagic.resonance.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.world.entity.player.Inventory;import net.minecraft.network.chat.Component;
public final class ResonanceScreen extends AbstractContainerScreen<ResonanceMenu>{
 public ResonanceScreen(ResonanceMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=222;titleLabelX=9;titleLabelY=8;inventoryLabelY=128;}
 protected void renderBg(GuiGraphics g,float p,int x,int y){int a=leftPos,b=topPos;g.fill(a,b,a+176,b+222,0xFF17292E);g.renderOutline(a,b,176,222,0xFFAD8457);g.fill(a+3,b+20,a+173,b+119,0xFF223E42);for(int j=0;j<9;j++){int xx=a+ResonanceMenu.slotX(j),yy=b+ResonanceMenu.slotY(j);g.fill(xx-1,yy-1,xx+17,yy+17,j==0?0xFFD5AE6A:0xFF74878A);g.fill(xx,yy,xx+16,yy+16,0xFF17292E);}g.fill(a+9,b+115,a+167,b+120,0xFF101B20);if(menu.data.get(1)>0)g.fill(a+9,b+115,a+9+158*menu.data.get(0)/menu.data.get(1),b+120,0xFF66C9C4);for(var s:menu.slots)if(s.index>=9){g.fill(a+s.x-1,b+s.y-1,a+s.x+17,b+s.y+17,0xFF697779);g.fill(a+s.x,b+s.y,a+s.x+16,b+s.y+16,0xFF18292B);}}
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,"Наполнение резонансом",9,8,0xE3C997,false);g.drawString(font,menu.data.get(2)>0?"Ожидает автора опыта":menu.active()?"Отклик · "+((menu.data.get(1)-menu.data.get(0)+19)/20)+" с":"Образцы ждут моего голоса",9,123,0x97C8C8,false);}
 public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}
