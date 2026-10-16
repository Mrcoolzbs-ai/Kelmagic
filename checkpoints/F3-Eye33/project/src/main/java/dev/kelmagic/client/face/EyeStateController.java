package dev.kelmagic.client.face;
import java.util.SplittableRandom;
/** Pure time-domain eyelid compositor. Units: seconds and normalized opening (0 closed, 1 open).
 * Randomness is consumed only at scheduled interval boundaries. No world/API dependency. */
public final class EyeStateController {
 public record BlinkProfile(double close, double hold, double open, double minInterval, double maxInterval) {
  public BlinkProfile { if(!Double.isFinite(close+hold+open+minInterval+maxInterval)||close<=0||hold<0||open<=0||minInterval<0||maxInterval<minInterval)throw new IllegalArgumentException("blink profile"); }
  public double duration(){return close+hold+open;}
 }
 public static final BlinkProfile NORMAL=new BlinkProfile(.070,.020,.120,2.5,5.5);
 public static final BlinkProfile FAST=new BlinkProfile(.065,.035,.10,0,0);
 /** Reserved context contract: producers explicitly provide accurate intent; no sensors or gaze motion here.
  * lightExposure, lookingAtPlayer, explosion and asymmetry are NOT connected to behavior in this stage. */
 public record ContextSignals(float lightExposure, boolean lookingAtPlayer, double nearbyExplosionTime,
                              float asymmetricSquintLeft, float asymmetricSquintRight, int expressionState) {}
 public record Frame(float leftOpening,float rightOpening,float blinkClosure,boolean burstActive,float cheekLeft,float cheekRight,float lowerLeft,float lowerRight) {
  public float leftMask(){return 1-.93f*leftOpening;}
  public float rightMask(){return 1-.93f*rightOpening;}
 }
 // Stable muscle channels use their own time envelope, never the blink pulse.
 private double muscleStart;private final float[] muscleFrom=new float[4],muscleTo=new float[4];
 public void setMuscles(double now,float cl,float cr,float ll,float lr){
  float[] target={unit(cl),unit(cr),unit(ll),unit(lr)};
  if(java.util.Arrays.equals(target,muscleTo))return;
  for(int i=0;i<4;i++){muscleFrom[i]=muscle(now,i);muscleTo[i]=target[i];}muscleStart=now;
 }
 private float muscle(double now,int i){double t=smooth((now-muscleStart)/.35);return (float)(muscleFrom[i]+(muscleTo[i]-muscleFrom[i])*t);}
 private final SplittableRandom random;
 private BlinkProfile profile;
 private double nextStart, pulseStart=Double.NaN;
 private BlinkProfile pulseProfile;
 private double burstStart=Double.NaN,burstEnd;private int burstCount;private double burstGap;private BlinkProfile burstProfile;
 private double transitionStart;private float fromL=1,fromR=1,toL=1,toR=1;
 private boolean initialized;private double latest;
 private ContextSignals signals=new ContextSignals(0,false,Double.NEGATIVE_INFINITY,0,0,0);
 public EyeStateController(long seed,double now,BlinkProfile profile){random=new SplittableRandom(seed);this.profile=profile;latest=now;nextStart=now+interval();transitionStart=now;}
 private double interval(){return profile.minInterval+(profile.maxInterval-profile.minInterval)*random.nextDouble();}
 private static float unit(float v){return Float.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
 private static double smooth(double x){x=Math.max(0,Math.min(1,x));return x*x*(3-2*x);}
 private float baseline(double now,boolean left){double a=smooth((now-transitionStart)/.22);return (float)((left?fromL:fromR)*(1-a)+(left?toL:toR)*a);}
 /** Called when stable inputs change; expressions retain their existing resting openness. */
 public void setBase(double now,float left,float right,float sustainedSquint){
  float l=unit(left)*(1-unit(sustainedSquint)),r=unit(right)*(1-unit(sustainedSquint));
  if(!initialized){fromL=toL=l;fromR=toR=r;transitionStart=now;initialized=true;return;}
  if(l==toL&&r==toR)return;
  fromL=baseline(now,true);fromR=baseline(now,false);toL=l;toR=r;transitionStart=now;
 }
 public void setSignals(ContextSignals value){signals=java.util.Objects.requireNonNull(value);}
 public ContextSignals signals(){return signals;}
 public void setBlinkProfile(double now,BlinkProfile value){java.util.Objects.requireNonNull(value);if(profile.equals(value))return;advance(now);profile=value;if(Double.isNaN(pulseStart)&&Double.isNaN(burstStart))nextStart=now+interval();}
 /** Generic transient profile. No explosion subscriber is installed. Requests during a burst are coalesced.
  * If a spontaneous blink is already closing/opening, finish it before starting the series (no jump). */
 public void requestBurst(double now,int count,BlinkProfile value,double gap){
  if(count<1||count>16||!Double.isFinite(gap)||gap<0)throw new IllegalArgumentException("burst");java.util.Objects.requireNonNull(value);advance(now);
  if(!Double.isNaN(burstStart))return;
  burstStart=Double.isNaN(pulseStart)?now:pulseStart+scheduledDuration(pulseProfile);burstCount=count;burstProfile=value;burstGap=gap;
  burstEnd=burstStart+count*value.duration()+(count-1)*gap;
 }
 public void advance(double now){
  if(!Double.isFinite(now))throw new IllegalArgumentException("time");
  // Render interpolation can request an earlier point in the same tick; never rewind the schedule.
  latest=Math.max(latest,now);
  if(!Double.isNaN(burstStart)){
   if(now>=burstEnd){burstStart=Double.NaN;pulseStart=Double.NaN;nextStart=burstEnd+interval();}
   else return;
  }
  while(true){
   if(!Double.isNaN(pulseStart)){
    double end=pulseStart+scheduledDuration(pulseProfile);if(now<end)return;
    pulseStart=Double.NaN;nextStart=end+interval();
   }
   if(now<nextStart)return;pulseStart=nextStart;pulseProfile=profile;
  }
 }
 // Preserve the original spontaneous blink start-to-start schedule and RNG consumption.
 // The shortened visual pulse leaves a fully-open tail inside its original 310 ms slot.
 private static double scheduledDuration(BlinkProfile p){return p==NORMAL?.310:p.duration();}
 // C2-continuous easing for the normal blink only; stable inputs and FAST bursts are unchanged.
 private static double blinkEase(double x,BlinkProfile p){
  if(p!=NORMAL)return smooth(x);
  x=Math.max(0,Math.min(1,x));return x*x*x*(x*(x*6-15)+10);
 }
 private static float closure(double elapsed,BlinkProfile p){
  if(elapsed<0||elapsed>=p.duration())return 0;
  if(elapsed<p.close)return (float)blinkEase(elapsed/p.close,p);
  if(elapsed<p.close+p.hold)return 1;
  return (float)(1-blinkEase((elapsed-p.close-p.hold)/p.open,p));
 }
 public Frame sample(double now){advance(now);float b=0;boolean burst=!Double.isNaN(burstStart);
  if(burst&&now>=burstStart){double stride=burstProfile.duration()+burstGap;int index=(int)((now-burstStart)/stride);if(index<burstCount)b=closure(now-burstStart-index*stride,burstProfile);}
  else if(!Double.isNaN(pulseStart))b=closure(now-pulseStart,pulseProfile);
  return new Frame(unit(baseline(now,true)*(1-b)),unit(baseline(now,false)*(1-b)),b,burst,muscle(now,0),muscle(now,1),muscle(now,2),muscle(now,3));
 }
}
