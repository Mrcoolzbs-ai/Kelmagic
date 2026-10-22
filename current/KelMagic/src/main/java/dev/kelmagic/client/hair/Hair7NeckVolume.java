package dev.kelmagic.client.hair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/** Shallow faceted infill BELOW the frozen Hair7 neck sheets. No bones, skinning or animation.
 * The five current under-sheet footprints are read only; x/z never expand beyond them.
 * Uses the existing opaque sample base; original sample2 under/over PNGs are not changed.
 */
public final class Hair7NeckVolume {
 private static final ResourceLocation BASE=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/hair6/sample_base.png");
 public static void render(PoseStack pose,MultiBufferSource buffers,int light,int overlay,float[][][] footprints,software.bernie.geckolib.model.GeoModel<?> model){
  var out=buffers.getBuffer(RenderType.entityCutoutNoCull(BASE));
  for(var q:footprints){
   float[][] top=new float[4][3],bevel=new float[4][3],bottom=new float[4][3];
   for(int i=0;i<4;i++){
    // Width pairs: 0--1 at one end, 3--2 at the other. Keep both ends in place.
    int mate=(i==0?1:i==1?0:i==2?3:2);
    float depth=depth(q[i][2]);
    top[i]=point(q[i],q[mate],.045f,.025f);
    bevel[i]=point(q[i],q[mate],.012f,Math.min(.18f,depth*.35f));
    bottom[i]=point(q[i],q[mate],.14f,depth);
   }
   quad(pose,out,top,light,overlay);
   // Faceted shoulders + sloping side walls. Closed ends and bottom, no hollow ribbon.
   for(int i=0;i<4;i++){int j=(i+1)%4;quad(pose,out,new float[][]{top[j],top[i],bevel[i],bevel[j]},light,overlay);quad(pose,out,new float[][]{bevel[j],bevel[i],bottom[i],bottom[j]},light,overlay);}
   quad(pose,out,new float[][]{bottom[3],bottom[2],bottom[1],bottom[0]},light,overlay);
  }
  chestRootBlend(pose,out,light,overlay,model);
 }
 private static void chestRootBlend(PoseStack pose,VertexConsumer out,int light,int overlay,software.bernie.geckolib.model.GeoModel<?> model){
  // Infill only the upper joining slope, behind the existing chest-root surfaces.
  // The chest bones and their original vertices are neither written nor reparented.
  for(float sign:new float[]{-1,1}){
   var chest=model.getBone(sign>0?"chest_left":"chest_right").orElse(null);if(chest==null)continue;
   var toBody=HairRig.belowBody(chest);float[][][] rings=new float[5][4][3];
   // Neck end overlaps the lowered R3 front-sheet lip, avoiding a tall exposed fin.
   rings[0]=new float[][]{{sign*3.27f,24.29f,-3.86f},{sign*4.15f,24.29f,-3.86f},{sign*4.06f,24.17f,-3.60f},{sign*3.36f,24.17f,-3.60f}};
   // Follow the actual new upper chest lock, not the obsolete straight root/apex chord.
   // Root and fixed neck end remain unchanged; no extra volume or animation writer.
   var faces=Hair6Meshes.DATA.get(sign>0?"hair6_front_left_base":"hair6_front_right_base").faces();
   for(int j=0;j<4;j++){float t=j/3f;
    var edge=faces[Math.min(j,2)*4];var a=edge[j==3?3:0];var b=edge[j==3?2:1];
    float y=a[1],z=a[2],lo=Math.min(Math.abs(a[0]),Math.abs(b[0]))+.025f,hi=Math.max(Math.abs(a[0]),Math.abs(b[0]))-.025f;
    float rear=.245f+.055f*(1-t*t*(3-2*t));float front=.205f;
    rings[j+1]=new float[][]{{sign*lo,y,z+front},{sign*hi,y,z+front},{sign*(hi-.09f),y,z+rear},{sign*(lo+.09f),y,z+rear}};
   }
   // Read the actual pre-existing chest placement (including its 22-degree rest rotation).
   // Only new infill vertices are converted to the common body space; old sheets stay static.
   for(int j=1;j<5;j++)for(int i=0;i<4;i++){var v=rings[j][i];var pos=toBody.transformPosition(new Vector3f(-v[0]/16,v[1]/16,v[2]/16)).mul(16);rings[j][i]=new float[]{-pos.x,pos.y,pos.z};}
   for(int j=0;j<4;j++){var center=new Vector3f();for(var v:rings[j])center.add(-v[0],v[1],v[2]);for(var v:rings[j+1])center.add(-v[0],v[1],v[2]);center.div(8);for(int i=0;i<4;i++){int k=(i+1)%4;outwardQuad(pose,out,new float[][]{rings[j][i],rings[j][k],rings[j+1][k],rings[j+1][i]},center,light,overlay);}}
   quad(pose,out,rings[0],light,overlay);quad(pose,out,new float[][]{rings[4][3],rings[4][2],rings[4][1],rings[4][0]},light,overlay);
  }
 }
 private static void outwardQuad(PoseStack pose,VertexConsumer out,float[][] q,Vector3f center,int light,int overlay){
  var a=new Vector3f(-q[1][0]+q[0][0],q[1][1]-q[0][1],q[1][2]-q[0][2]);var b=new Vector3f(-q[2][0]+q[0][0],q[2][1]-q[0][1],q[2][2]-q[0][2]);var mid=new Vector3f();for(var v:q)mid.add(-v[0],v[1],v[2]);mid.div(4).sub(center);if(a.cross(b).dot(mid)<0)q=new float[][]{q[3],q[2],q[1],q[0]};quad(pose,out,q,light,overlay);
 }
 private static float depth(float z){
  // Near the fixed chest-root overlap the depth tapers to .18 px, meeting its y=24.78 cap.
  float t=Math.clamp((z+3.95f)/2.35f,0,1);t=t*t*(3-2*t);return .18f+.52f*t;
 }
 private static float[] point(float[] a,float[] b,float inset,float drop){return new float[]{a[0]+(b[0]-a[0])*inset,a[1]-drop,a[2]+(b[2]-a[2])*inset};}
 private static void quad(PoseStack p,VertexConsumer out,float[][] q,int light,int overlay){
  var a=new Vector3f(-q[1][0]+q[0][0],q[1][1]-q[0][1],q[1][2]-q[0][2]);var b=new Vector3f(-q[2][0]+q[0][0],q[2][1]-q[0][1],q[2][2]-q[0][2]);var n=a.cross(b);if(n.lengthSquared()<1e-12f)return;n.normalize();
  for(int i=0;i<4;i++)out.addVertex(p.last(),-q[i][0]/16,q[i][1]/16,q[i][2]/16).setColor(255,255,255,255).setUv(i==1||i==2?1:0,i>=2?1:0).setOverlay(overlay).setLight(light).setNormal(p.last(),n.x,n.y,n.z);
 }
 private Hair7NeckVolume(){}
}
