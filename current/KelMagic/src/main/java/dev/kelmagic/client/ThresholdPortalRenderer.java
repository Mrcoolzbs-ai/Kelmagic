package dev.kelmagic.client;
import dev.kelmagic.entity.ThresholdPortal;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
public final class ThresholdPortalRenderer extends EntityRenderer<ThresholdPortal> {
 private static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/threshold_portal.png");
 public ThresholdPortalRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=0;}
 @Override public ResourceLocation getTextureLocation(ThresholdPortal e){return TEX;}
 @Override public void render(ThresholdPortal e,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){p.pushPose();p.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));int frame=((int)((e.tickCount+partial)/3))%16;float u=frame%4/4f,v=frame/4/4f;var b=out.getBuffer(RenderType.entityTranslucent(TEX));var pose=p.last();vertex(b,pose,-.5f,0,u,v+.25f,e.color());vertex(b,pose,.5f,0,u+.25f,v+.25f,e.color());vertex(b,pose,.5f,2,u+.25f,v,e.color());vertex(b,pose,-.5f,2,u,v,e.color());p.popPose();}
 private static void vertex(VertexConsumer b,PoseStack.Pose p,float x,float y,float u,float v,int c){b.addVertex(p,x,y,0).setColor((c>>16)&255,(c>>8)&255,c&255,220).setUv(u,v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(0xf000f0).setNormal(p,0,0,1);}
}
