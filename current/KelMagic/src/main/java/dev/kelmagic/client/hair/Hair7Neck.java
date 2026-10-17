package dev.kelmagic.client.hair;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;import net.minecraft.resources.ResourceLocation;import org.joml.Vector3f;
/** F3-hair7. Original flat neck quads, rendered in the live root/body bone pose.
 * Geometry/materials are preserved; no head-following or independent animation input. */
public final class Hair7Neck {
 private record Patch(String texture,float[][] q){}
 private static final Patch[] PATCHES={
 new Patch("side_under",new float[][]{{3.2239069f,24.9800000f,-3.9423830f},{4.2160931f,24.9800000f,-3.8176170f},{3.7960931f,25.0050000f,-1.5376170f},{2.8039069f,25.0050000f,-1.6623830f}}),
 new Patch("side_under",new float[][]{{2.8004060f,25.0050000f,-1.6201449f},{3.7995940f,25.0050000f,-1.5798551f},{3.6495940f,25.0050000f,2.1401449f},{2.6504060f,25.0050000f,2.0998551f}}),
 new Patch("side_under",new float[][]{{-4.2160931f,24.9800000f,-3.8176170f},{-3.2239069f,24.9800000f,-3.9423830f},{-2.8039069f,25.0050000f,-1.6623830f},{-3.7960931f,25.0050000f,-1.5376170f}}),
 new Patch("side_under",new float[][]{{-3.7995940f,25.0050000f,-1.5798551f},{-2.8004060f,25.0050000f,-1.6201449f},{-2.6504060f,25.0050000f,2.0998551f},{-3.6495940f,25.0050000f,2.1401449f}}),
 new Patch("back_under",new float[][]{{3.2500000f,25.0050000f,1.6200000f},{3.2500000f,25.0050000f,2.6200000f},{-3.2500000f,25.0050000f,2.6200000f},{-3.2500000f,25.0050000f,1.6200000f}}),
 new Patch("side_over",new float[][]{{3.3528911f,24.9920000f,-3.9261634f},{4.0871089f,24.9920000f,-3.8338366f},{3.6671089f,25.0170000f,-1.5538366f},{2.9328911f,25.0170000f,-1.6461634f}}),
 new Patch("side_over",new float[][]{{2.9303004f,25.0170000f,-1.6149072f},{3.6696996f,25.0170000f,-1.5850928f},{3.5196996f,25.0170000f,2.1349072f},{2.7803004f,25.0170000f,2.1050928f}}),
 new Patch("side_over",new float[][]{{-4.0871089f,24.9920000f,-3.8338366f},{-3.3528911f,24.9920000f,-3.9261634f},{-2.9328911f,25.0170000f,-1.6461634f},{-3.6671089f,25.0170000f,-1.5538366f}}),
 new Patch("side_over",new float[][]{{-3.6696996f,25.0170000f,-1.5850928f},{-2.9303004f,25.0170000f,-1.6149072f},{-2.7803004f,25.0170000f,2.1050928f},{-3.5196996f,25.0170000f,2.1349072f}}),
 new Patch("back_over",new float[][]{{3.2500000f,25.0170000f,1.7500000f},{3.2500000f,25.0170000f,2.4900000f},{-3.2500000f,25.0170000f,2.4900000f},{-3.2500000f,25.0170000f,1.7500000f}}),
 };
 public static void render(PoseStack pose,MultiBufferSource buffers,int light,int overlay,software.bernie.geckolib.model.GeoModel<?> model){
  Hair7NeckVolume.render(pose,buffers,light,overlay,java.util.Arrays.stream(PATCHES).limit(5).map(Patch::q).toArray(float[][][]::new),model);
  render(pose,buffers,light,overlay);
 }
 public static void render(PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  for(var patch:PATCHES){var type=RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/hair7/"+patch.texture+".png"));var out=buffers.getBuffer(type);var q=patch.q;
   var n=new Vector3f(-(q[1][0]-q[0][0]),q[1][1]-q[0][1],q[1][2]-q[0][2]).cross(new Vector3f(-(q[2][0]-q[0][0]),q[2][1]-q[0][1],q[2][2]-q[0][2]));if(n.lengthSquared()>0)n.normalize();
   for(int i=0;i<4;i++){var a=q[i];out.addVertex(pose.last(),-a[0]/16,a[1]/16,a[2]/16).setColor(255,255,255,255).setUv(i==1||i==2?1:0,i>=2?1:0).setOverlay(overlay).setLight(light).setNormal(pose.last(),n.x,n.y,n.z);}
  }
 }
 public static String fingerprint(){return "34168f758871bf2b938d8b9bac713b02e569e3d58c9ce04041bd41a7c01dbc46";}
}
