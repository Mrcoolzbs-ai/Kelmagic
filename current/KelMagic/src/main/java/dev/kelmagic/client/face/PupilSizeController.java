package dev.kelmagic.client.face;
/** Independent absolute-time dark pupil size. Future intents have no automatic sensors. */
public final class PupilSizeController {
 public static final float MIN=.65f,MAX=1.35f;
 public enum State { NORMAL, CONSTRICTED, DILATED }
 public enum Intent { LIGHT, THREAT, FEAR, INTEREST, SCRIPT }
 private double start,duration=.3;private float from=1,to=1;private State state=State.NORMAL;private Intent intent=Intent.SCRIPT;
 private static float value(State s){return s==State.CONSTRICTED?MIN:s==State.DILATED?MAX:1;}
 public float sample(double now){double t=Math.max(0,Math.min(1,(now-start)/duration));t=t*t*(3-2*t);return (float)(from+(to-from)*t);}
 public void target(double now,State s,double seconds,Intent reason){
  if(!Double.isFinite(now)||!Double.isFinite(seconds)||seconds<=0)throw new IllegalArgumentException("finite positive transition");
  java.util.Objects.requireNonNull(s);java.util.Objects.requireNonNull(reason);from=sample(now);to=value(s);start=now;duration=seconds;state=s;intent=reason;
 }
 public State state(){return state;}public Intent intent(){return intent;}
}
