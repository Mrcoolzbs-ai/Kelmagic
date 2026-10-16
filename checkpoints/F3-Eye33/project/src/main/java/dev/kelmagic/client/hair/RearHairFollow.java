package dev.kelmagic.client.hair;

import java.util.*;
import dev.kelmagic.entity.RedMage;
import org.joml.Matrix4f;import org.joml.Vector3f;import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.model.GeoModel;

/** Rear Hair7 sheets only. Frozen surfaces/UVs are re-sampled, never integrated over frames.
 * The scalp end follows the evaluated head; the lower drape retains body-relative weight.
 * Subdivision is render tessellation of the SAME surfaces, not additional hair strands.
 */
public final class RearHairFollow {
 private static final Map<GeoBone,List<GeoCube>> REST=Collections.synchronizedMap(new WeakHashMap<>());
 private static final Map<RedMage,MotionState> MOTION=Collections.synchronizedMap(new WeakHashMap<>());
 private static final class MotionState {final RearHairMotion spring=new RearHairMotion(13),middle=new RearHairMotion(9),tip=new RearHairMotion(7);double x,y,z;boolean placed;}
 public static boolean owns(String n){return n.startsWith("hair6_hair_back_")||n.startsWith("hair6_hair_mid_")||n.startsWith("hair6_hair_tip_");}
 /** Restore the original cube layout before Hair6Meshes rebuilds its frozen quad positions. */
 public static void restore(GeoBone b){if(!owns(b.getName()))return;var original=REST.get(b);if(original!=null){b.getCubes().clear();b.getCubes().addAll(original);}}
 public static void apply(GeoModel<?> model,GeoBone b,RedMage entity,float partial){
  if(!owns(b.getName()))return;var h=model.getBone("head").orElse(null);if(h==null)return;
  REST.computeIfAbsent(b,k->new ArrayList<>(k.getCubes()));
  // HeadSeat is evaluated before the rear siblings. Undo only its NEUTRAL translation:
  // neutral yaw/pitch leaves every original rear point in exactly its original position.
  Matrix4f delta=HairRig.belowBody(h).translate(0,HeadSeat.DROP_PIXELS/16,0);
  Quaternionf rotation=delta.getUnnormalizedRotation(new Quaternionf()).normalize();
  var state=MOTION.computeIfAbsent(entity,k->new MotionState());
  boolean teleport=state.placed && (Math.pow(entity.getX()-state.x,2)+Math.pow(entity.getY()-state.y,2)+Math.pow(entity.getZ()-state.z,2)>4);
  double now=(entity.tickCount+(double)Math.clamp(partial,0,1))/20.0;
  Quaternionf lag=state.spring.sample(now,rotation,teleport),mid=state.middle.sample(now,rotation,teleport),tip=state.tip.sample(now,rotation,teleport);
  state.x=entity.getX();state.y=entity.getY();state.z=entity.getZ();state.placed=true;
  Vector3f pivot=new Vector3f(-h.getPivotX()/16,(h.getPivotY()-HeadSeat.DROP_PIXELS)/16,h.getPivotZ()/16);
  Vector3f shift=delta.transformPosition(new Vector3f(pivot)).sub(pivot);
  Matrix4f toBody=HairRig.belowBody(b),fromBody=new Matrix4f(toBody).invert();
  var shoulders=new ArrayList<float[]>();
  for(String arm:new String[]{"left_arm","right_arm"})model.getBone(arm).ifPresent(a->{
   var mat=HairRig.belowBody(a);float[] box={Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
   float lo=arm.equals("left_arm")?-7:4,hi=lo+3;
   for(float x:new float[]{lo,hi})for(float y:new float[]{18.04f,24})for(float z:new float[]{-2,2}){var p=mat.transformPosition(new Vector3f(x/16,y/16,z/16)).mul(16);box[0]=Math.min(box[0],p.x);box[1]=Math.max(box[1],p.x);box[2]=Math.min(box[2],p.y);box[3]=Math.max(box[3],p.y);box[4]=Math.max(box[4],p.z);}
   shoulders.add(box);
  });
  var drape=new RearHairDrape(pivot,shift,rotation,lag,mid,tip);
  var fresh=new ArrayList<GeoCube>();
  for(var cube:b.getCubes()){
   var faces=new ArrayList<GeoQuad>();
   for(var face:cube.quads())if(face!=null){
    // GeckoLib dummy cube has one frozen surface per direction. Hair6Meshes supplies it.
    // Identical duplicated directions are retained, as in the original Hair7 material.
    for(int v=0;v<12;v++)for(int u=0;u<6;u++){
     GeoVertex[] verts=new GeoVertex[4];float[][] uv={{u/6f,v/12f},{(u+1)/6f,v/12f},{(u+1)/6f,(v+1)/12f},{u/6f,(v+1)/12f}};
     for(int k=0;k<4;k++){
      var s=sample(face.vertices(),uv[k][0],uv[k][1]);var pos=toBody.transformPosition(new Vector3f(s.position()));
      drape.deform(pos);guard(pos,shoulders);fromBody.transformPosition(pos);verts[k]=new GeoVertex(pos,s.texU(),s.texV());
     }
     Vector3f normal=new Vector3f(verts[1].position()).sub(verts[0].position()).cross(new Vector3f(verts[3].position()).sub(verts[0].position()));
     if(normal.lengthSquared()<1e-12f)normal.set(face.normal());else normal.normalize();
     if(normal.dot(face.normal())<0)normal.negate();faces.add(new GeoQuad(verts,normal,face.direction()));
    }
   }
   fresh.add(new GeoCube(faces.toArray(GeoQuad[]::new),cube.pivot(),cube.rotation(),cube.size(),cube.inflate(),cube.mirror()));
  }
  b.getCubes().clear();b.getCubes().addAll(fresh);
 }
 private static GeoVertex sample(GeoVertex[] a,float u,float v){float[] w={(1-u)*(1-v),u*(1-v),u*v,(1-u)*v};var p=new Vector3f();float tu=0,tv=0;for(int i=0;i<4;i++){p.fma(w[i],a[i].position());tu+=w[i]*a[i].texU();tv+=w[i]*a[i].texV();}return new GeoVertex(p,tu,tv);}
 private static void guard(Vector3f p,List<float[]> shoulders){
  // Keep the long sheets behind the torso/shoulder envelope, without changing UVs.
  // At the scalp the guard fades out so the attachment follows the head exactly.
  float shoulder=1-smooth(24.1f,25.6f,p.y*16);float side=1-smooth(4.25f,5.8f,Math.abs(p.x)*16);
  float floor=2.35f/16; if(p.z<floor)p.z+=(floor-p.z)*shoulder*side;
  // Read the existing animated arm transforms; do not drive arms or front/chest locks.
  for(var a:shoulders){float x=p.x*16,y2=p.y*16;float w=smooth(a[0]-3f,a[0]-.35f,x)*(1-smooth(a[1]+.35f,a[1]+3f,x))*smooth(a[2]-3f,a[2]-.35f,y2)*(1-smooth(a[3]+.35f,a[3]+3f,y2));float back=(a[4]+.45f)/16;if(p.z<back)p.z+=(back-p.z)*w;}
 }
 private static float smooth(float a,float b,float x){float t=clamp((x-a)/(b-a),0,1);return t*t*(3-2*t);}
 private static float clamp(float x,float a,float b){return Math.max(a,Math.min(b,x));}
 private RearHairFollow(){}
}
