package dev.kelmagic.client.face;
/** Pure presentation amplitudes; phase is the existing RedMage.breathing(time), not a second clock. */
public final class BreathingPresentation {
 public static final float NOSE_Y=.012f;
 public static float noseY(float phase,boolean enabled){return enabled?phase*NOSE_Y:0;}
 private BreathingPresentation(){}
}
