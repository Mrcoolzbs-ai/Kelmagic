package dev.kelmagic.client;
import dev.kelmagic.KelMagic;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
public final class CodexPaint {
 public static final ResourceLocation BOOK=ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"textures/gui/codex.png");
 public static final int W=360,H=216,INK=0x493427;
 public static Button button(Font font,String title,int x,int y,int w,int h,Runnable action){return new Button(x,y,w,h,Component.literal(title),b->action.run(),s->s.get()){
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){int xx=getX(),yy=getY();g.fill(xx,yy,xx+w,yy+h,0xFF38252E);g.fill(xx+1,yy+1,xx+w-1,yy+h-1,isHoveredOrFocused()?0xFF936748:0xFF674344);g.fill(xx+2,yy+1,xx+w-2,yy+2,0xFFC49C61);g.drawCenteredString(font,getMessage(),xx+w/2,yy+(h-8)/2,isHoveredOrFocused()?0xFFFFE7B2:0xFFE0C59A);}
 };}
 public static void book(GuiGraphics g,int x,int y){g.blit(BOOK,x,y,0,0,W,H,W,H);}
 public static void text(GuiGraphics g,Font font,String text,int x,int y,int w,int maxLines){var lines=font.split(Component.literal(text),w);for(int i=0;i<Math.min(maxLines,lines.size());i++)g.drawString(font,lines.get(i),x,y+i*10,INK,false);}
 public static ItemStack icon(String id){if(id.equals("minecraft:potion"))return net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,dev.kelmagic.MagicContent.MANA);return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.contains(":")?id:"kelmagic:"+id)));}
 /** Low contrast drifting ink/dust for parchment pages. */
 public static void dust(GuiGraphics g,int x,int y,long time){for(int i=0;i<18;i++){int xx=x+23+Math.floorMod(i*71+(int)(time/240),310),yy=y+37+Math.floorMod(i*37+(int)(time/390),132);if(xx>x+160&&xx<x+194)continue;g.fill(xx,yy,xx+1,yy+1,0x183E6B69);}}
 private CodexPaint(){}
}
