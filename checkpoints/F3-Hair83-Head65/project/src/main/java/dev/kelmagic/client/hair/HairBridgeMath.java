package dev.kelmagic.client.hair;
/** Pure two-anchor strip transform. Units are model-space blocks (1 = 16 geometry units). */
public final class HairBridgeMath {
 public record Aim(double length,double pitch,double roll){}
 public static Aim aim(double dx,double dy,double dz){
  if(!Double.isFinite(dx)||!Double.isFinite(dy)||!Double.isFinite(dz))throw new IllegalArgumentException("finite hair anchors required");
  double n=Math.sqrt(dx*dx+dy*dy+dz*dz);if(n<1e-9)return new Aim(0,0,0);
  return new Aim(n,Math.asin(Math.clamp(dz/n,-1,1)),Math.atan2(-dx,dy));
 }
 private HairBridgeMath(){}
}
