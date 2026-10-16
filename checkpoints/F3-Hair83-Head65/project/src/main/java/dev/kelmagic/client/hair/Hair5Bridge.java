package dev.kelmagic.client.hair;
import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;
/** Only new B connector surfaces are written. A/C bones, animations and resources are untouched.
 * Per-draw rest-based deformation follows actual head and torso transforms, including QA angles.
 * Broad partial neck/shoulder patches: no front ring, no bone scaling, no endpoint chord through neck.
 */
public final class Hair5Bridge {
 private record Rest(GeoCube cube,int index,GeoQuad quad){}
 private static final Map<GeoBone,List<Rest>> REST=new WeakHashMap<>();
 private static Matrix4f matrix(GeoBone b){var chain=new ArrayDeque<GeoBone>();for(;b!=null&&!b.getName().equals("body");b=b.getParent())chain.addFirst(b);var p=new PoseStack();for(var n:chain)RenderUtil.prepMatrixForBone(p,n);return new Matrix4f(p.last().pose());}
 public static void apply(GeoModel<?> model,GeoBone bone){
  String name=bone.getName();String region=name.split("_")[1];String tier=name.split("_")[2];
  double a0=region.equals("left")?40:region.equals("right")?209:137,a1=region.equals("left")?151:region.equals("right")?320:223;
  float off=tier.equals("over")?.22f:tier.equals("under")?.09f:0;
  Matrix4f head=matrix(model.getBone("head").orElseThrow());
  var rest=REST.computeIfAbsent(bone,b->{var l=new ArrayList<Rest>();for(var c:b.getCubes())for(int i=0;i<c.quads().length;i++)if(c.quads()[i]!=null)l.add(new Rest(c,i,c.quads()[i]));return l;});
  for(var r:rest){GeoQuad q=r.quad();GeoVertex[] vs=new GeoVertex[4];for(int i=0;i<4;i++){
   var v=q.vertices()[i];float u=-v.position().x*16/8,t=(v.position().y*16-24)/3;t=Math.clamp(t,0,1);
   double a=Math.toRadians(a0+(a1-a0)*u),s=Math.sin(a),c=Math.cos(a);
   // Top is buried within existing head hair. Bottom is feathered over the existing C roots.
   float topY=25.65f,lowY=region.equals("nape")?22.55f:(float)(23.05+1.25*Math.exp(-Math.pow(c/.35,2)));
   double rad=4.43/Math.max(Math.abs(s),Math.abs(c));
   Vector3f top=new Vector3f((float)(-s*rad/16),topY/16,(float)(-c*rad/16));head.transformPosition(top);top.mul(16);
   double destAngle=Math.atan2(-top.x,-top.z),delta=Math.atan2(Math.sin(destAngle-a),Math.cos(destAngle-a));
   float w=t*t*(3-2*t);double angle=a+delta*w;
   double lowerRadius=(region.equals("nape")?4.88:4.65)/Math.max(Math.abs(s),Math.abs(c));
   double radius=lowerRadius*(1-w)+Math.hypot(top.x,top.z)*w+off-1.40*Math.sin(Math.PI*t);
   // Angular interpolation avoids a straight stretched strip crossing the neck at 90 degrees.
   float y=lowY*(1-t)+top.y*t;
   radius-=v.position().z*16;
   // A few broad overlapping lobes, not an annular lower edge. Head's front gap stays open.
   y+=(float)(.13*Math.cos(u*Math.PI*4)*(1-t));
   Vector3f p=new Vector3f((float)(-Math.sin(angle)*radius/16),y/16,(float)(-Math.cos(angle)*radius/16));
   vs[i]=new GeoVertex(p,v.texU(),v.texV());
  }
  var normal=new Vector3f(vs[1].position()).sub(vs[0].position()).cross(new Vector3f(vs[2].position()).sub(vs[0].position())).normalize();
  r.cube().quads()[r.index()]=new GeoQuad(vs,normal,q.direction());
  }
 }
 private Hair5Bridge(){}
}
