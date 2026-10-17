package dev.kelmagic.client.face;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.*;import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
/** Samples the CURRENT CheekRig-deformed quads. No second deformation writer, no skin edits. */
public final class CheekBlushLayer {
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/face/cheek_blush.png");
 public static void render(PoseStack p,MultiBufferSource buffers,GeoBone cheek,float intensity,int light,int overlay){
  if(intensity<=0)return;var out=buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
  for(var cube:cheek.getCubes())for(var q:cube.quads())if(q!=null){var n=q.normal();
   for(var v:q.vertices()){var a=v.position();float u=(Math.abs(a.x()*16)-.9f)/2.95f,w=(27.2f-a.y()*16)/1.7f;
    out.addVertex(p.last(),a.x(),a.y(),a.z()-.000125f).setColor(255,255,255,Math.round(255*intensity)).setUv(u,w).setOverlay(overlay).setLight(light).setNormal(p.last(),n.x(),n.y(),n.z());
   }
  }
 }
 private CheekBlushLayer(){}
}
