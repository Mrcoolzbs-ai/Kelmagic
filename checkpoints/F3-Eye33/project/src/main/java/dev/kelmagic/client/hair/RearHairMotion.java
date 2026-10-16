package dev.kelmagic.client.hair;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Per-entity, critically damped response to the evaluated head orientation.
 * Game time, not wall time: pause and repeated camera/layer draws cannot advance it.
 * This class never owns bones, textures, or rest geometry.
 */
public final class RearHairMotion {
 private final float omega;
 public RearHairMotion(){this(11f);}
 public RearHairMotion(float frequency){omega=Math.max(4,Math.min(20,frequency));}
 private static final float MAX_LAG=(float)Math.toRadians(18);
 private final Vector3f position=new Vector3f(),velocity=new Vector3f(),previousTarget=new Vector3f();
 private final Quaternionf residual=new Quaternionf();
 private double time=Double.NaN;
 public Quaternionf sample(double now,Quaternionf head,boolean discontinuity){
  var target=log(new Quaternionf(head).normalize());
  double dt=now-time;
  if(!Double.isFinite(now)||!Float.isFinite(target.lengthSquared())){time=Double.NaN;return residual.identity();}
  if(discontinuity||!Double.isFinite(time)||dt<-.000001||dt>.25||target.distance(previousTarget)>2.1f){
   position.set(target);previousTarget.set(target);velocity.zero();time=now;return residual.identity();
  }
  if(dt<=.000001)return new Quaternionf(residual);
  float h=(float)dt,decay=(float)Math.exp(-omega*h);
  // Exact critically damped solution for a linearly changing target over this frame.
  // No accumulated Euler instability at low FPS; static input converges without a sine.
  var targetSpeed=new Vector3f(target).sub(previousTarget).div(h);
  var u=new Vector3f(position).sub(previousTarget).fma(2/omega,targetSpeed);
  var du=new Vector3f(velocity).sub(targetSpeed);
  var c=new Vector3f(u).mul(omega).add(du);
  position.set(target).fma(-2/omega,targetSpeed).add(new Vector3f(u).fma(h,c).mul(decay));
  velocity.set(targetSpeed).add(du.fma(-omega*h,c).mul(decay));
  previousTarget.set(target);time=now;
  // Residual is layered on the approved body-weighted attachment, never on the roots.
  residual.set(exp(position)).mul(new Quaternionf(head).conjugate()).normalize();
  var error=log(new Quaternionf(residual));float angle=error.length();
  if(angle>MAX_LAG)residual.set(exp(error.mul(MAX_LAG/angle)));
  return new Quaternionf(residual);
 }
 private static Vector3f log(Quaternionf q){
  if(q.w<0)q.mul(-1);float s=(float)Math.sqrt(q.x*q.x+q.y*q.y+q.z*q.z);
  return s<1e-7f?new Vector3f():new Vector3f(q.x,q.y,q.z).mul(2*(float)Math.atan2(s,q.w)/s);
 }
 private static Quaternionf exp(Vector3f v){float a=v.length();return a<1e-7f?new Quaternionf():new Quaternionf().rotationAxis(a,v.x/a,v.y/a,v.z/a);}
}
