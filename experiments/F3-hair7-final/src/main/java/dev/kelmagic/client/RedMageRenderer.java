package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import dev.kelmagic.core.GlyphWriting;import net.minecraft.client.Minecraft;import net.minecraft.client.gui.Font;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.EntityRendererProvider;import net.minecraft.network.chat.*;import net.minecraft.resources.ResourceLocation;import com.mojang.blaze3d.vertex.PoseStack;import software.bernie.geckolib.renderer.GeoEntityRenderer;
public final class RedMageRenderer extends GeoEntityRenderer<RedMage>{
 public RedMageRenderer(EntityRendererProvider.Context c){super(c,new RedMageGeoModel());shadowRadius=.38f;}
 // Apply per draw, not only per animation evaluation: GeckoLib caches animation evaluations
 // and shares baked geometry across entities. Always start each head draw from frozen rest quads.
 @Override public void renderRecursively(PoseStack pose,RedMage e,software.bernie.geckolib.cache.object.GeoBone bone,RenderType type,MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer vertices,boolean reRender,float partial,int light,int overlay,int color){
  if(bone.getName().equals("head")){dev.kelmagic.client.hair.HeadSeat.apply(bone);dev.kelmagic.client.face.CheekRig.apply(getGeoModel(),dev.kelmagic.client.face.MageEyeStates.sample(e,partial));dev.kelmagic.client.face.MouthStates.apply(getGeoModel(),e,partial);}
  if(bone.getName().equals("mouth_png")){var pngType=RenderType.entityCutoutNoCull(dev.kelmagic.client.face.MouthStates.texture(e,partial));super.renderRecursively(pose,e,bone,pngType,buffers,buffers.getBuffer(pngType),reRender,partial,light,overlay,color);return;}
  if(bone.getName().startsWith("hair6_")){
   dev.kelmagic.client.hair.Hair6Meshes.apply(getGeoModel(),bone);
   var material=RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/hair6/"+dev.kelmagic.client.hair.Hair6Meshes.texture(bone.getName())+".png"));
   super.renderRecursively(pose,e,bone,material,buffers,buffers.getBuffer(material),reRender,partial,light,overlay,color);return;
  }
  if(bone.getName().startsWith("hair4_sheet_")){
   var hairType=RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/hair4/"+dev.kelmagic.client.hair.Hair4Materials.texture(bone.getName())+".png"));
   super.renderRecursively(pose,e,bone,hairType,buffers,buffers.getBuffer(hairType),reRender,partial,light,overlay,color);return;
  }
  super.renderRecursively(pose,e,bone,type,buffers,vertices,reRender,partial,light,overlay,color);
 }
 // Entity-model matrix only, captured by GeckoLib BEFORE ANY bone traversal. Neck surfaces
 // are drawn once outside renderRecursively; they have no GeoBone, parent or animation data.
 @Override public void applyRenderLayers(PoseStack pose,RedMage e,software.bernie.geckolib.cache.object.BakedGeoModel model,RenderType type,MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer vertices,float partial,int light,int overlay){
  super.applyRenderLayers(pose,e,model,type,buffers,vertices,partial,light,overlay);
  if(!e.isInvisible()) {pose.pushPose();pose.last().pose().set(this.modelRenderTranslations);pose.last().normal().set(new org.joml.Matrix3f(this.modelRenderTranslations).invert().transpose());dev.kelmagic.client.hair.Hair7Neck.render(pose,buffers,light,overlay);pose.popPose();}
 }
 @Override public void render(RedMage e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){super.render(e,yaw,partial,pose,buffers,light);var words=e.visibleWords();if(words.isEmpty())return;var mc=Minecraft.getInstance();var text=Component.literal(String.join(" ",words.stream().map(GlyphWriting::write).toList())).withStyle(Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"kel")));pose.pushPose();pose.translate(0,e.getBbHeight()+.35,0);pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());pose.scale(.022f,-.022f,.022f);mc.font.drawInBatch(text,-mc.font.width(text)/2f,0,0xFFF3E2AF,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,0xF000F0);pose.popPose();}
}
