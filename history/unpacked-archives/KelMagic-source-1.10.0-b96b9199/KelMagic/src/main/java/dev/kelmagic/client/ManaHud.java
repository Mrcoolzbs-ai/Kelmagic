package dev.kelmagic.client;
import dev.kelmagic.KelMagic;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class ManaHud {
 private static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"textures/gui/mana_tube.png");
 /** Bounded by available screen space even for modded/configured large maxima. */
 public static int width(double maximum,int screen){return Math.min(Math.max(36,screen-120),(int)Math.round(40+Math.max(0,Math.min(maximum,1024)-5)*3));}
 @SubscribeEvent public static void render(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();var s=ClientSpellState.own;if(mc.player==null || s==null || mc.options.hideGui)return;
  var g=e.getGuiGraphics();int x=8,y=9,w=width(s.maximum(),g.guiWidth());
  int fill=s.maximum()<=0?0:(int)Math.round(w*Math.clamp((double)s.mana()/s.maximum(),0,1));
  g.fill(x+10,y+4,x+10+w,y+13,0xD016293E);
  if(fill>0){g.fill(x+10,y+5,x+10+fill,y+12,0xFF248DCC);g.fill(x+10,y+5,x+10+fill,y+7,0xFF9EF6FF);g.fill(x+10,y+10,x+10+fill,y+12,0xFF3ECBDD);}
  // Pixel-art end caps and tiled engraved glass frame, not a stretched texture.
  for(int i=0;i<w;i+=8)g.blit(TEX,x+10+i,y,0,18,Math.min(8,w-i),18,32,36);
  g.blit(TEX,x,y,0,0,10,18,32,36);g.blit(TEX,x+10+w,y,10,0,12,18,32,36);
  if(mc.options.keyShift.isDown())g.drawString(mc.font,dev.kelmagic.core.ManaFormat.number(s.mana())+" / "+dev.kelmagic.core.ManaFormat.number(s.maximum()),x+w+27,y+5,0xB7F4FF,true);
 }
 private ManaHud(){}
}
