package dev.kelmagic.client.face;
import java.util.*;
/** One procedural timeline and one set of channels for all expressions. Seconds, model pixels.
 * No entity/world references, RNG, target search, or replacement blink schedule. */
public final class EyeExpressionController {
 public enum Preset { WINK_LEFT,WINK_RIGHT,DOUBLE_BLINK,SLOW_BLINK,EYE_ROLL,SKEPTICAL,SUSPICIOUS,DARTING,SACCADES,SIDE_GLANCE,ASKANCE,SURPRISE,SLEEPY,TIRED,DROWSY_LIDS,FOCUS,LOOK_UP,LOOK_DOWN,PUPIL_PULSE,TARGET_TRANSFER,READING,CONFUSION,DISBELIEF,IRRITATION,ALERT,AVERT,RETURN_CALM }
 public record Options(double duration,float intensity,float smoothness){public Options {if(!Double.isFinite(duration)||duration<.12||duration>30||!Float.isFinite(intensity+smoothness)||intensity<0||intensity>1||smoothness<0||smoothness>1)throw new IllegalArgumentException("duration .12..30s; intensity/smoothness 0..1");}public static Options defaults(){return new Options(2,1,.8f);}}
 public record Frame(float x,float y,float gazeWeight,float vergence,float closeLeft,float closeRight,float widen,float pupil){
  public static final Frame ZERO=new Frame(0,0,0,0,0,0,0,0);
  public Frame mix(Frame b,float t){return new Frame(l(x,b.x,t),l(y,b.y,t),l(gazeWeight,b.gazeWeight,t),l(vergence,b.vergence,t),l(closeLeft,b.closeLeft,t),l(closeRight,b.closeRight,t),l(widen,b.widen,t),l(pupil,b.pupil,t));}
  public Frame scale(float a){return ZERO.mix(this,a);}
 }
 private record Key(double t,Frame f){}
 private static Frame f(float x,float y,float gaze,float l,float r,float widen,float pupil){return new Frame(x,y,gaze,0,l,r,widen,pupil);}
 private static Key k(double t,Frame f){return new Key(t,f);}
 private static final Frame Z=Frame.ZERO;
 private static final Map<Preset,List<Key>> LIBRARY=library();
 private static Map<Preset,List<Key>> library(){var m=new EnumMap<Preset,List<Key>>(Preset.class);
  m.put(Preset.WINK_LEFT,List.of(k(0,Z),k(.25,f(0,0,0,1,0,0,0)),k(.55,f(0,0,0,1,0,0,0)),k(1,Z)));
  m.put(Preset.WINK_RIGHT,List.of(k(0,Z),k(.25,f(0,0,0,0,1,0,0)),k(.55,f(0,0,0,0,1,0,0)),k(1,Z)));
  m.put(Preset.DOUBLE_BLINK,List.of(k(0,Z),k(.17,f(0,0,0,1,1,0,0)),k(.35,Z),k(.53,f(0,0,0,1,1,0,0)),k(.78,Z),k(1,Z)));
  m.put(Preset.SLOW_BLINK,List.of(k(0,Z),k(.4,f(0,0,0,1,1,0,0)),k(.6,f(0,0,0,1,1,0,0)),k(1,Z)));
  m.put(Preset.EYE_ROLL,List.of(k(0,Z),k(.2,f(.3f,.95f,1,0,0,0,0)),k(.45,f(0,1.14f,1,0,0,0,0)),k(.7,f(-.3f,.95f,1,0,0,0,0)),k(1,Z)));
  hold(m,Preset.SKEPTICAL,f(.45f,.10f,.8f,.6f,.18f,0,0));hold(m,Preset.SUSPICIOUS,f(-.5f,.02f,1,.48f,.48f,0,-.12f));
  m.put(Preset.DARTING,List.of(k(0,Z),k(.14,f(-.65f,.12f,1,0,0,0,0)),k(.34,f(.66f,-.18f,1,0,0,0,0)),k(.52,f(-.4f,.35f,1,0,0,0,0)),k(.73,f(.45f,.06f,1,0,0,0,0)),k(1,Z)));
  m.put(Preset.SACCADES,List.of(k(0,Z),k(.19,Z),k(.23,f(.7f,0,1,0,0,0,0)),k(.43,f(.7f,0,1,0,0,0,0)),k(.47,f(-.7f,.22f,1,0,0,0,0)),k(.68,f(-.7f,.22f,1,0,0,0,0)),k(.72,f(.15f,-.35f,1,0,0,0,0)),k(1,Z)));
  hold(m,Preset.SIDE_GLANCE,f(.72f,0,1,0,0,0,0));hold(m,Preset.ASKANCE,f(-.58f,.4f,1,.2f,.35f,0,0));
  hold(m,Preset.SURPRISE,f(0,.15f,.4f,0,0,.12f,.28f));hold(m,Preset.SLEEPY,f(0,-.25f,.5f,.68f,.68f,0,0));hold(m,Preset.TIRED,f(0,-.16f,.4f,.45f,.55f,0,-.1f));
  m.put(Preset.DROWSY_LIDS,List.of(k(0,Z),k(.75,f(0,-.1f,.2f,.93f,.93f,0,0)),k(.9,f(0,-.1f,.2f,.93f,.93f,0,0)),k(1,Z)));
  hold(m,Preset.FOCUS,f(0,0,.2f,.22f,.22f,0,-.25f));hold(m,Preset.LOOK_UP,f(0,.92f,1,0,0,0,0));hold(m,Preset.LOOK_DOWN,f(0,-.92f,1,0,0,0,0));
  m.put(Preset.PUPIL_PULSE,List.of(k(0,Z),k(.25,f(0,0,0,0,0,0,.35f)),k(.65,f(0,0,0,0,0,0,-.35f)),k(1,Z)));
  // Real target selection is supplied by EyeExpressions.transferTo; the existing gaze smoother
  // remains the target writer. This shared clip only adds a short settling/blink accent.
  m.put(Preset.TARGET_TRANSFER,List.of(k(0,Z),k(.13,f(0,0,0,.2f,.2f,0,0)),k(.35,Z),k(1,Z)));
  m.put(Preset.READING,List.of(k(0,Z),k(.08,f(-.55f,-.25f,1,0,0,0,0)),k(.29,f(.55f,-.25f,1,0,0,0,0)),k(.34,f(-.55f,-.45f,1,0,0,0,0)),k(.56,f(.55f,-.45f,1,0,0,0,0)),k(.61,f(-.55f,-.65f,1,0,0,0,0)),k(.85,f(.55f,-.65f,1,0,0,0,0)),k(1,Z)));
  m.put(Preset.CONFUSION,List.of(k(0,Z),k(.25,f(-.25f,.12f,.7f,.1f,.35f,0,.1f)),k(.6,f(.35f,-.05f,.7f,.35f,.1f,0,.1f)),k(1,Z)));
  m.put(Preset.DISBELIEF,List.of(k(0,Z),k(.2,f(0,0,0,0,0,.12f,.2f)),k(.36,f(0,0,0,1,1,0,0)),k(.5,f(0,0,0,0,0,.12f,.2f)),k(.68,f(0,0,0,1,1,0,0)),k(.82,f(0,.2f,.5f,0,0,.08f,0)),k(1,Z)));
  hold(m,Preset.IRRITATION,f(.2f,.35f,.7f,.55f,.55f,0,-.12f));hold(m,Preset.ALERT,f(-.3f,.3f,.65f,0,0,.08f,.2f));hold(m,Preset.AVERT,f(-.72f,-.3f,1,.1f,.1f,0,0));
  m.put(Preset.RETURN_CALM,List.of(k(0,Z),k(1,Z)));if(m.size()!=Preset.values().length)throw new ExceptionInInitializerError("missing preset");return Collections.unmodifiableMap(m);
 }
 private static void hold(Map<Preset,List<Key>> m,Preset p,Frame f){m.put(p,List.of(k(0,Z),k(.22,f),k(.72,f),k(1,Z)));}
 private Preset preset;private Options options;private double start,cancelAt=Double.NaN,cancelDuration;private Frame carry=Z;private long serial;
 public long play(double now,Preset p,Options o){finite(now);Objects.requireNonNull(p);Objects.requireNonNull(o);Frame current=sample(now);preset=p;options=o;start=now;cancelAt=Double.NaN;carry=current;return ++serial;}
 public boolean cancel(double now,long token,double fade){finite(now);if(!Double.isFinite(fade)||fade<=0||fade>3)throw new IllegalArgumentException("fade 0..3 seconds");if(token!=serial||!active(now))return false;carry=sample(now);preset=null;cancelAt=now;cancelDuration=fade;return true;}
 public boolean active(double now){return preset!=null?now<start+options.duration:!Double.isNaN(cancelAt)&&now<cancelAt+cancelDuration;}
 public long token(){return serial;}
 public Frame sample(double now){finite(now);
  if(preset==null)return Double.isNaN(cancelAt)?Z:carry.scale(1-ease((now-cancelAt)/cancelDuration,1));
  double t=(now-start)/options.duration;if(t>=1)return Z;if(t<0)return carry;
  var keys=LIBRARY.get(preset);Frame raw=Z;for(int i=1;i<keys.size();i++)if(t<=keys.get(i).t){var a=keys.get(i-1);var b=keys.get(i);raw=a.f.mix(b.f,ease((t-a.t)/(b.t-a.t),options.smoothness));break;}
  float envelope=1-ease((t-.88)/.12,options.smoothness);raw=raw.scale(options.intensity*envelope);
  return carry.mix(raw,ease((now-start)/Math.min(.25,options.duration*.18),options.smoothness));
 }
 private static void finite(double n){if(!Double.isFinite(n))throw new IllegalArgumentException("finite time");}
 private static float ease(double t,float smooth){float a=(float)Math.max(0,Math.min(1,t)),s=a*a*a*(a*(a*6-15)+10);return l(a,s,smooth);}
 private static float l(float a,float b,float t){return a+(b-a)*t;}
}
