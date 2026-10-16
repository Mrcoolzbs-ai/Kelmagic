package dev.kelmagic.client.hair;
import org.joml.Quaternionf;import org.joml.Vector3f;
/** Shared arc-length frame field for every rear material and segment.
 * A head-attached root, gravity-aligned lower lengths, and three correlated delays.
 * Rest surfaces/UVs remain unchanged. No bone scaling and no autonomous oscillation. */
public final class RearHairDrape {
 private static final float TOP=26.2f,LENGTH=15.5f,Z=4.15f;
 private static final int STEPS=64;
 private final Vector3f[] centres=new Vector3f[STEPS+1];
 private final Quaternionf[] frames=new Quaternionf[STEPS+1];
 public RearHairDrape(Vector3f pivot,Vector3f shift,Quaternionf head,Quaternionf upper,Quaternionf middle,Quaternionf tip){
  Vector3f root=new Vector3f(0,TOP/16,Z/16).sub(pivot);head.transform(root);root.add(pivot).add(shift).mul(16);centres[0]=root;
  for(int i=0;i<=STEPS;i++){
   float distance=LENGTH*i/STEPS,tail=distance/LENGTH;
   // Continuous torsion relaxation, not a rigid rotation of the entire sheet.
   float follow=(float)Math.exp(-distance/3.7f);
   Quaternionf lag=tail<.5f?new Quaternionf(upper).slerp(middle,tail*2):new Quaternionf(middle).slerp(tip,tail*2-1);
   frames[i]=new Quaternionf().slerp(head,follow).mul(new Quaternionf().slerp(lag,.72f*tail));
   if(i==0)continue;
   Vector3f direction=frames[i-1].transform(new Vector3f(0,-1,0));
   Vector3f prev=centres[i-1];
   // Smooth restoring curvature toward the back, with fixed arc step length.
   float pull=.16f*(1-(float)Math.exp(-distance/2.5f));
   direction.add(-prev.x*pull,0,(Z-prev.z)*pull);direction.y=Math.min(-.55f,direction.y);direction.normalize();
   centres[i]=new Vector3f(prev).fma(LENGTH/STEPS,direction);
  }
 }
 public void deform(Vector3f point){
  float restY=point.y*16,at=Math.clamp((TOP-restY)/LENGTH*STEPS,0,STEPS);int i=Math.min(STEPS-1,(int)at);float f=at-i;
  var centre=new Vector3f(centres[i]).lerp(centres[i+1],f);
  var frame=new Quaternionf(frames[i]).slerp(frames[i+1],f);
  var cross=new Vector3f(point.x*16,0,point.z*16-Z);frame.transform(cross);
  point.set(centre.add(cross)).div(16);
 }
}
