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
  frame(g,x,y,w);
  if(mc.options.keyShift.isDown())g.drawString(mc.font,dev.kelmagic.core.ManaFormat.number(s.mana())+" / "+dev.kelmagic.core.ManaFormat.number(s.maximum()),x+w+27,y+5,0xB7F4FF,true);
 }
 // The old implementation issued one immediate GPU draw per 8px tile (up to 227).
 // Match vanilla innerBlit UVs, but submit the entire frame in one draw call.
 private static void frame(net.minecraft.client.gui.GuiGraphics g,int x,int y,int w){
  g.flush();com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0,TEX);com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
  com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
  var b=com.mojang.blaze3d.vertex.Tesselator.getInstance().begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);var matrix=g.pose().last().pose();
  for(int i=0;i<w;i+=8)quad(b,matrix,x+10+i,y,Math.min(8,w-i),18,0,18);quad(b,matrix,x,y,10,18,0,0);quad(b,matrix,x+10+w,y,12,18,10,0);
  com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(b.buildOrThrow());com.mojang.blaze3d.systems.RenderSystem.disableBlend();
 }
 private static void quad(com.mojang.blaze3d.vertex.BufferBuilder b,org.joml.Matrix4f m,int x,int y,int w,int h,int u,int v){float u0=u/32f,u1=(u+w)/32f,v0=v/36f,v1=(v+h)/36f;b.addVertex(m,x,y,0).setUv(u0,v0);b.addVertex(m,x,y+h,0).setUv(u0,v1);b.addVertex(m,x+w,y+h,0).setUv(u1,v1);b.addVertex(m,x+w,y,0).setUv(u1,v0);}
 private ManaHud(){}
}
