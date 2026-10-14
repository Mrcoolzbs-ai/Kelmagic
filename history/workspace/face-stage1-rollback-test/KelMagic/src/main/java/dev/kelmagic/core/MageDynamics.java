package dev.kelmagic.core;
/** Independent multi-axis damped spring, after studying Wildfire's motion/shape approach.
 * No upstream source copied. Inputs and geometry are specific to our adult NPC. */
public final class MageDynamics {
 private float y,x,r,vy,vx,vr,oy,ox,or;
 public void reset(){y=x=r=vy=vx=vr=oy=ox=or=0;}
 public void step(float acceleration,float turn){if(!Float.isFinite(acceleration)||!Float.isFinite(turn)){reset();return;}oy=y;ox=x;or=r;float ty=Math.clamp(acceleration*3,-.70f,.70f),tx=Math.clamp(-turn*5,-.25f,.25f),tr=Math.clamp(turn*2,-.20f,.20f);vy=(vy+(ty-y)*.15f)*.81f;vx=(vx+(tx-x)*.18f)*.78f;vr=(vr+(tr-r)*.16f)*.8f;y=Math.clamp(y+vy,-.85f,.85f);x=Math.clamp(x+vx,-.3f,.3f);r=Math.clamp(r+vr,-.24f,.24f);}
 public float lift(float p){return oy+(y-oy)*Math.clamp(p,0,1);}public float pitch(float p){return or+(r-or)*Math.clamp(p,0,1);}public float sway(float p){return ox+(x-ox)*Math.clamp(p,0,1);}
}
