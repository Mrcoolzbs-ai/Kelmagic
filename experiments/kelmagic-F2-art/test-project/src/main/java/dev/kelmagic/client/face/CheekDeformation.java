package dev.kelmagic.client.face;
/** Model units. Fixed outer boundary; rigid colour-bearing plateau; continuous side/height ramps. */
public final class CheekDeformation {
 private static float smooth(float t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
 public static float weight(float x,float y){x=Math.abs(x);return smooth(Math.min((x-.9f)/.6f,(3.85f-x)/.85f))*smooth(Math.min((y-25.5f)/.7f,(27.2f-y)/.35f));}
 public static float liftY(float x,float y,float amount){return .16f*Math.clamp(amount,0,1)*weight(x,y);}
 public static float liftZ(float x,float y,float amount){return -.04f*Math.clamp(amount,0,1)*weight(x,y);}
 private CheekDeformation(){}
}
