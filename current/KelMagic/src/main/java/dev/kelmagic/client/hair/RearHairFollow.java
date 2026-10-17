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
 private static final class MotionState {final RearHairCloth cloth=new RearHairCloth();final Matrix4f bodyFrame=new Matrix4f();double x,y,z;boolean placed;}
 /** Capture the actual animated BODY-to-world orientation, cancelling only camera/presentation.
  * Called before head traversal; no face, head transform or frozen geometry is written. */
 public static void captureBody(RedMage e,com.mojang.blaze3d.vertex.PoseStack pose,Matrix4f entityBase){
  MOTION.computeIfAbsent(e,k->new MotionState()).bodyFrame.set(entityBase).invert().mul(pose.last().pose());
 }
 public static boolean owns(String n){return n.startsWith("hair6_hair_back_")||n.startsWith("hair6_hair_mid_")||n.startsWith("hair6_hair_tip_");}
 /** Restore the original cube layout before Hair6Meshes rebuilds its frozen quad positions. */
 public static void restore(GeoBone b){if(!owns(b.getName()))return;var original=REST.get(b);if(original!=null){b.getCubes().clear();b.getCubes().addAll(original);}}
 public static void apply(GeoModel<?> model,GeoBone b,RedMage entity,float partial){
  if(!owns(b.getName()))return;var h=model.getBone("head").orElse(null);if(h==null)return;
  REST.computeIfAbsent(b,k->new ArrayList<>(k.getCubes()));
  var state=MOTION.computeIfAbsent(entity,k->new MotionState());
  boolean teleport=state.placed && (Math.pow(entity.getX()-state.x,2)+Math.pow(entity.getY()-state.y,2)+Math.pow(entity.getZ()-state.z,2)>4);
  double now=(entity.tickCount+(double)Math.clamp(partial,0,1))/20.0;
  if(state.cloth.needsSample(now,teleport)){
   // Fresh inputs for every reset/advancing sample; never reuse collision geometry.
   // HeadSeat is evaluated before the rear siblings. Undo only its NEUTRAL translation:
   // neutral yaw/pitch leaves every original rear point in exactly its original position.
   Matrix4f delta=HairRig.belowBody(h).translate(0,HeadSeat.DROP_PIXELS/16,0);

   var displacement=state.placed?new Vector3f((float)(entity.getX()-state.x),(float)(entity.getY()-state.y),(float)(entity.getZ()-state.z)):new Vector3f();
   var colliders=new ArrayList<RearHairCloth.Collider>();
   // Read-only inner head proxy prevents the cloth from passing through the scalp
   // and settling on the opposite side after a rapid reversal. Optical/head meshes are untouched.
   colliders.add(new RearHairCloth.Collider(delta,new Vector3f(-3.3f,23.9f,-3.3f),new Vector3f(3.3f,31.8f,3.3f),false));
   colliders.add(new RearHairCloth.Collider(new Matrix4f(),new Vector3f(-4,12,-2),new Vector3f(4,24,2),false));
   for(String name:new String[]{"left_arm","right_arm","left_forearm","right_forearm"})model.getBone(name).ifPresent(a->{
    boolean left=name.startsWith("left"),fore=name.contains("forearm");float lo=left?(fore?-6.955f:-7):(fore?4.045f:4),hi=left?(fore?-4.045f:-4):(fore?6.955f:7);
    colliders.add(new RearHairCloth.Collider(HairRig.belowBody(a),new Vector3f(lo,fore?12:18.04f,fore?-1.955f:-2),new Vector3f(hi,fore?18.28f:24,fore?1.955f:2),true));
   });
   state.cloth.sample(now,delta,colliders,teleport,displacement,state.bodyFrame);
  }
  state.x=entity.getX();state.y=entity.getY();state.z=entity.getZ();state.placed=true;
  Matrix4f toBody=HairRig.belowBody(b),fromBody=new Matrix4f(toBody).invert();
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
      state.cloth.deform(pos);fromBody.transformPosition(pos);verts[k]=new GeoVertex(pos,s.texU(),s.texV());
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
 public static String diagnostics(RedMage e){var m=MOTION.get(e);return m==null?"uninitialized":"steps="+m.cloth.steps+" bodyContacts="+m.cloth.bodyContacts+" armContacts="+m.cloth.armContacts+" selfContacts="+m.cloth.selfContacts+" maxStrain="+m.cloth.maxStrain+" widthFreedom="+m.cloth.widthMobility();}
 private RearHairFollow(){}
}
