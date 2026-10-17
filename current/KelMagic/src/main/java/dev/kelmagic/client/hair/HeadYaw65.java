package dev.kelmagic.client.hair;
/** Stateless final bone-space ceiling relative to the frozen neutral yaw.
 * Excess requested yaw is discarded on every draw; no accumulator or eye input is touched. */
public final class HeadYaw65 {
 public static final float MAX_RADIANS=(float)Math.toRadians(65);
 public static float limit(float requested,float neutral){if(!Float.isFinite(neutral))neutral=0;return Float.isFinite(requested)?neutral+Math.clamp(requested-neutral,-MAX_RADIANS,MAX_RADIANS):neutral;}
 private HeadYaw65(){}
}
