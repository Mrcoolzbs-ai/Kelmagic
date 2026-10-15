package dev.kelmagic.client.hair;
import com.mojang.blaze3d.vertex.*;import net.minecraft.client.renderer.*;import net.minecraft.resources.ResourceLocation;import org.joml.Vector3f;
/** F3-hair7. Fixed entity-local flat neck quads. NO GeoBone, model, animation or head input. */
public final class Hair7Neck {
 private record Patch(String texture,float[][] q){}
 private static final Patch[] PATCHES={
  new Patch("side_under",new float[][]{{3.930000f,25.900000f,-4.500000f},{3.930000f,25.900000f,3.950000f},{3.930000f,23.400000f,3.950000f},{3.930000f,23.400000f,-4.500000f}}),
  new Patch("side_under",new float[][]{{2.900000f,25.700000f,-4.680000f},{4.400000f,25.700000f,-4.680000f},{4.350000f,23.500000f,-4.820000f},{3.000000f,23.500000f,-4.820000f}}),
  new Patch("side_over",new float[][]{{4.005000f,25.900000f,-4.500000f},{4.005000f,25.900000f,3.950000f},{4.005000f,23.400000f,3.950000f},{4.005000f,23.400000f,-4.500000f}}),
  new Patch("side_over",new float[][]{{2.900000f,25.700000f,-4.755000f},{4.400000f,25.700000f,-4.755000f},{4.350000f,23.500000f,-4.895000f},{3.000000f,23.500000f,-4.895000f}}),
  new Patch("side_under",new float[][]{{-3.930000f,25.900000f,-4.500000f},{-3.930000f,25.900000f,3.950000f},{-3.930000f,23.400000f,3.950000f},{-3.930000f,23.400000f,-4.500000f}}),
  new Patch("side_under",new float[][]{{-2.900000f,25.700000f,-4.680000f},{-4.400000f,25.700000f,-4.680000f},{-4.350000f,23.500000f,-4.820000f},{-3.000000f,23.500000f,-4.820000f}}),
  new Patch("side_over",new float[][]{{-4.005000f,25.900000f,-4.500000f},{-4.005000f,25.900000f,3.950000f},{-4.005000f,23.400000f,3.950000f},{-4.005000f,23.400000f,-4.500000f}}),
  new Patch("side_over",new float[][]{{-2.900000f,25.700000f,-4.755000f},{-4.400000f,25.700000f,-4.755000f},{-4.350000f,23.500000f,-4.895000f},{-3.000000f,23.500000f,-4.895000f}}),
  new Patch("back_under",new float[][]{{-3.940000f,25.900000f,4.660000f},{3.940000f,25.900000f,4.660000f},{3.850000f,23.250000f,4.660000f},{-3.850000f,23.250000f,4.660000f}}),
  new Patch("back_over",new float[][]{{-3.940000f,25.900000f,4.735000f},{3.940000f,25.900000f,4.735000f},{3.850000f,23.250000f,4.735000f},{-3.850000f,23.250000f,4.735000f}}),
 };
 public static void render(PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  for(var patch:PATCHES){var type=RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/hair7/"+patch.texture+".png"));var out=buffers.getBuffer(type);var q=patch.q;
   var n=new Vector3f(-(q[1][0]-q[0][0]),q[1][1]-q[0][1],q[1][2]-q[0][2]).cross(new Vector3f(-(q[2][0]-q[0][0]),q[2][1]-q[0][1],q[2][2]-q[0][2]));if(n.lengthSquared()>0)n.normalize();
   for(int i=0;i<4;i++){var a=q[i];out.addVertex(pose.last(),-a[0]/16,a[1]/16,a[2]/16).setColor(255,255,255,255).setUv(i==1||i==2?1:0,i>=2?1:0).setOverlay(overlay).setLight(light).setNormal(pose.last(),n.x,n.y,n.z);}
  }
 }
 public static String fingerprint(){return "ff8097d8c18e7f1fe1ad3f9f70a063bc4512418ed6727deed5b22d002499efdf";}
}
