package dev.kelmagic.client.face;
/** Independent time-domain value. No emotion/gameplay triggers. Seconds, smoothstep, [0,1]. */
public final class CheekBlushController {
 private float start,target;private double since,duration;
 private static float clamp(float x){return Float.isFinite(x)?Math.clamp(x,0,1):0;}
 public float sample(double now){if(!Double.isFinite(now))return start;if(duration<=0)return target;double t=Math.clamp((now-since)/duration,0,1);return (float)(start+(target-start)*t*t*(3-2*t));}
 public void transition(float value,double seconds,double now){start=sample(now);target=clamp(value);since=Double.isFinite(now)?now:0;duration=Double.isFinite(seconds)?Math.max(0,seconds):0;if(duration==0)start=target;}
 public void set(float value){start=target=clamp(value);duration=0;}
 public float target(){return target;}
}
