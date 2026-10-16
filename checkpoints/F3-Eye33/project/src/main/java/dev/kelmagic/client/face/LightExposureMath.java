package dev.kelmagic.client.face;
/** Bounded artistic exposure approximation, not a shader/luminance simulation. Pure functions. */
public final class LightExposureMath {
 public static final double LAVA_RANGE=6, MAX_LIGHT_SQUINT=.45;
 private static final double OUTER=Math.cos(Math.toRadians(32)),INNER=Math.cos(Math.toRadians(10));
 public record Direction(double x,double y,double z){}
 public static double unit(double x){return Double.isFinite(x)?Math.max(0,Math.min(1,x)):0;}
 public static double smooth(double x){x=unit(x);return x*x*(3-2*x);}
 /** Vanilla sky rotates +Y by Rx(time*360), then Ry(-90). Not civil-clock sunrise guessing. */
 public static Direction sunDirection(double timeOfDay){double a=timeOfDay*Math.PI*2;return new Direction(-Math.sin(a),Math.cos(a),0);}
 public static double angular(double cosine){return smooth((cosine-OUTER)/(INNER-OUTER));}
 public static double sun(double cosine,double elevation,double rain,double thunder){
  return angular(cosine)*smooth((elevation-.04)/.22)*(1-.8*unit(rain))*(1-.5*unit(thunder));
 }
 public static double lava(double cosine,double distance){
  if(!Double.isFinite(distance)||distance<0)return 0;
  return angular(cosine)*smooth((LAVA_RANGE-distance)/2)/(1+distance*distance/9);
 }
 public static double combine(double sun,double lava){return 1-(1-unit(sun))*(1-unit(lava));}
 public static double approach(double previous,double target,double dt){
  previous=unit(previous);target=unit(target);dt=Math.max(0,dt);
  double tau=target>previous?.35:.65;return previous+(target-previous)*(1-Math.exp(-dt/tau));
 }
 private LightExposureMath(){}
}
