package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.core.GlyphWriting;import net.minecraft.client.Minecraft;import net.minecraft.client.gui.Font;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.EntityRendererProvider;import net.minecraft.network.chat.*;import net.minecraft.resources.ResourceLocation;import com.mojang.blaze3d.vertex.PoseStack;import software.bernie.geckolib.renderer.GeoEntityRenderer;
public final class RedMageRenderer extends GeoEntityRenderer<RedMage>{
 public RedMageRenderer(EntityRendererProvider.Context c){super(c,new RedMageGeoModel());shadowRadius=.38f;}
 // Apply per draw, not only per animation evaluation: GeckoLib caches animation evaluations
 // and shares baked geometry across entities. Always start each head draw from frozen rest quads.
 @Override public void renderRecursively(PoseStack pose,RedMage e,software.bernie.geckolib.cache.object.GeoBone bone,RenderType type,MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer vertices,boolean reRender,float partial,int light,int overlay,int color){
  if(bone.getName().equals("head"))dev.kelmagic.client.face.CheekRig.apply(getGeoModel(),dev.kelmagic.client.face.MageEyeStates.sample(e,partial));
  super.renderRecursively(pose,e,bone,type,buffers,vertices,reRender,partial,light,overlay,color);
 }
 @Override public void render(RedMage e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){super.render(e,yaw,partial,pose,buffers,light);var words=e.visibleWords();if(words.isEmpty())return;var mc=Minecraft.getInstance();var text=Component.literal(String.join(" ",words.stream().map(GlyphWriting::write).toList())).withStyle(Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"kel")));pose.pushPose();pose.translate(0,e.getBbHeight()+.35,0);pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());pose.scale(.022f,-.022f,.022f);mc.font.drawInBatch(text,-mc.font.width(text)/2f,0,0xFFF3E2AF,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,0xF000F0);pose.popPose();}
}
