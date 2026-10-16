package dev.kelmagic.client.face;
/** Dark-centre containment, independent of iris travel, socket shape and expression engine. */
public final class PupilVisibility {
 public record Seat(float centre,boolean visible){}
 public static Seat seat(float current,float radius,float low,float high){
  if(!Float.isFinite(current+radius+low+high)||radius<=0)return new Seat(current,false);
  float span=high-low;
  // A sliver of iris must not acquire a detached black line. Keep its original
  // dark pixels masked rather than squeezing the approved round pupil shape.
  if(span<radius*1.05f+.035f)return new Seat(current,false);
  if(span<2*radius+.035f)return new Seat((low+high)*.5f,true);
  return new Seat(Math.clamp(current,low+radius+.015f,high-radius-.015f),true);
 }
 private PupilVisibility(){}
}
