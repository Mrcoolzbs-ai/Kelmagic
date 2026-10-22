package dev.kelmagic.client;
import dev.kelmagic.entity.InkMark;import dev.kelmagic.content.InkPatterns;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.*;import net.minecraft.resources.ResourceLocation;
public final class InkMarkRenderer extends EntityRenderer<InkMark> {
 public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/ink/patterns.png");
 public InkMarkRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
 @Override public ResourceLocation getTextureLocation(InkMark e){return ATLAS;}
 @Override public void render(InkMark e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){p.pushPose();switch(e.face()){case NORTH->p.mulPose(Axis.YP.rotationDegrees(180));case WEST->p.mulPose(Axis.YP.rotationDegrees(-90));case EAST->p.mulPose(Axis.YP.rotationDegrees(90));case UP->p.mulPose(Axis.XP.rotationDegrees(-90));case DOWN->p.mulPose(Axis.XP.rotationDegrees(90));default->{}}if(e.face().getAxis().isVertical())p.mulPose(Axis.ZP.rotationDegrees(e.rotation()*90));int t=InkPatterns.get(e.pattern()).tile();float u=(t%16)/16f,v=(t/16)/16f,U=u+1/16f,V=v+1/16f;int c=e.inkColor();var b=out.getBuffer(RenderType.entityCutoutNoCull(ATLAS));var pose=p.last();vertex(b,pose,-.43f,.43f,u,v,c);vertex(b,pose,-.43f,-.43f,u,V,c);vertex(b,pose,.43f,-.43f,U,V,c);vertex(b,pose,.43f,.43f,U,v,c);p.popPose();}
 static void vertex(com.mojang.blaze3d.vertex.VertexConsumer b,PoseStack.Pose p,float x,float y,float u,float v,int c){b.addVertex(p.pose(),x,y,0).setColor((c>>16)&255,(c>>8)&255,c&255,255).setUv(u,v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p,0,0,1);}
}
