package dev.kelmagic.client.motion;

import org.joml.Matrix4f;import org.joml.Quaternionf;import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.*;
import java.util.*;

/** Two open-sided hinge bands between rigid limb ends. Every draw starts at rest.
 * No overlapping end caps, retained entity pose, new textures or per-frame vertex allocations.
 * Band endpoint follows the exact child matrix (including authored clip offsets/scale).
 */
public final class LimbJointRig {
 private static final Map<GeoQuad[],Buffer> CACHE=new WeakHashMap<>();
 private static final class Face {
  final GeoQuad quad;final float[] xyz=new float[12];final boolean upper;final float winding;
  Face(GeoQuad q,boolean up){quad=q;upper=up;for(int i=0;i<4;i++){var p=q.vertices()[i].position();xyz[i*3]=p.x;xyz[i*3+1]=p.y;xyz[i*3+2]=p.z;}var a=new Vector3f(q.vertices()[1].position()).sub(q.vertices()[0].position());var b=new Vector3f(q.vertices()[2].position()).sub(q.vertices()[0].position());winding=a.cross(b).dot(q.normal())<0?-1:1;}
 }
 private static final class Buffer {
  final List<Face> faces=new ArrayList<>();final Matrix4f full=new Matrix4f(),half=new Matrix4f();
  final Quaternionf rotation=new Quaternionf(),mid=new Quaternionf();
  final Vector3f a=new Vector3f(),b=new Vector3f(),c=new Vector3f();
  Buffer(GeoBone bone){var cubes=bone.getCubes();for(int ci=cubes.size()-2;ci<cubes.size();ci++)for(var q:cubes.get(ci).quads())if(q!=null){
    // Private mutable output; source values stay in xyz and are never accumulated.
    GeoVertex[] v=new GeoVertex[4];for(int i=0;i<4;i++)v[i]=new GeoVertex(new Vector3f(q.vertices()[i].position()),q.vertices()[i].texU(),q.vertices()[i].texV());
    var out=new GeoQuad(v,new Vector3f(q.normal()),q.direction());
    for(int k=0;k<cubes.get(ci).quads().length;k++)if(cubes.get(ci).quads()[k]==q){cubes.get(ci).quads()[k]=out;break;}
    faces.add(new Face(out,ci==cubes.size()-2));
   }}
 }
 public static void apply(GeoBone upper){
  String name=upper.getName();String child=switch(name){case "left_arm"->"left_forearm";case "right_arm"->"right_forearm";case "left_leg"->"left_shin";case "right_leg"->"right_shin";default->null;};
  if(child==null)return;GeoBone lower=null;var children=upper.getChildBones();for(int i=0;i<children.size();i++){var b=children.get(i);if(b.getName().equals(child)){lower=b;break;}}
  if(lower==null||upper.getCubes().size()<3)return;
  // GeoBone.equals compares hashes/names, so it is NOT a safe identity cache key.
  // Arrays use identity equality; weak keys release on model/resource reload.
  var key=upper.getCubes().get(upper.getCubes().size()-2).quads();
  Buffer s=CACHE.get(key);if(s==null){s=new Buffer(upper);CACHE.put(key,s);}
  float px=lower.getPivotX()/16,py=lower.getPivotY()/16,pz=lower.getPivotZ()/16;
  // Baked bone pivot X is ALREADY mirrored. RenderUtil: Z then Y then X.
  s.rotation.rotationZYX(lower.getRotZ(),lower.getRotY(),lower.getRotX());s.mid.identity().slerp(s.rotation,.5f);
  s.full.identity().translate(-lower.getPosX()/16,lower.getPosY()/16,lower.getPosZ()/16).translate(px,py,pz).rotate(s.rotation).scale(lower.getScaleX(),lower.getScaleY(),lower.getScaleZ()).translate(-px,-py,-pz);
  s.half.identity().translate(-lower.getPosX()/32,lower.getPosY()/32,lower.getPosZ()/32).translate(px,py,pz).rotate(s.mid).scale((1+lower.getScaleX())*.5f,(1+lower.getScaleY())*.5f,(1+lower.getScaleZ())*.5f).translate(-px,-py,-pz);
  // The two rest sections are slightly different widths: join them at their averaged contour.
  for(int fi=0;fi<s.faces.size();fi++){var f=s.faces.get(fi);for(int i=0;i<4;i++){
    var p=f.quad.vertices()[i].position().set(f.xyz[i*3],f.xyz[i*3+1],f.xyz[i*3+2]);
    if(Math.abs(p.y-py)<.00001f){
      // Original forearm/shin inset is .045/.05 model units; no displaced centerline.
      float inset=name.endsWith("_arm")?.045f:.05f;
      p.x+=(f.upper?-1:1)*Math.copySign(inset/32,p.x-px);
      p.z+=(f.upper?-1:1)*Math.copySign(inset/32,p.z-pz);
      s.half.transformPosition(p);
    }else if(!f.upper)s.full.transformPosition(p);
   }
   var v=f.quad.vertices();s.a.set(v[1].position()).sub(v[0].position());s.b.set(v[2].position()).sub(v[0].position());s.c.set(s.a).cross(s.b);
   // Baked winding is fixed; Gecko uses its face normal (which may oppose vertex cross).
   if(s.c.lengthSquared()>1e-15f){s.c.normalize();s.c.mul(f.winding);f.quad.normal().set(s.c);}
  }
 }
 private LimbJointRig(){}
}
