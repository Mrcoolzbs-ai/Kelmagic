package dev.kelmagic.client.motion;

/** Reusable scalar state. Units: seconds, blocks, degrees; pose offsets are model units/radians.
 * No world access, no random-per-frame noise, no allocations after construction.
 */
public final class MotionState {
 public static final int ROOT=0,BODY=1,HEAD=2,LEFT_LEG=3,RIGHT_LEG=4,LEFT_SHIN=5,RIGHT_SHIN=6,LEFT_ARM=7,RIGHT_ARM=8,LEFT_FOREARM=9,RIGHT_FOREARM=10;
 public final float[][] pose=new float[11][6]; // rx,ry,rz,px,py,pz; never retained by mesh buffers
 public double time=Double.NaN,phase,speed,airTime;
 public float bodyYaw,headYaw,headPitch,weight,move,run,air,landing;
 private double x,y,z,nextVariation,variationStart=-100,ackStart=-100,lastTarget=-1;
 private float forward=1,side,lean,lastVY;private boolean grounded=true,wasTeleport;
 private long random;
 public MotionState(long seed){random=seed;}
 public static float wrap(float d){return d-(float)Math.floor((d+180)/360)*360;}
 private static float approachAngle(float a,float b,float dt,float rate){return a+Math.clamp(wrap(b-a),-rate*dt,rate*dt);}
 private static float smooth(float a,float b,float dt,float tau){return a+(b-a)*(float)-Math.expm1(-dt/tau);}
 private double interval(){random=random*6364136223846793005L+1442695040888963407L;return 14+((random>>>11)*0x1.0p-53)*15;}
 public void sample(double now,double nx,double ny,double nz,float vanillaBody,float desiredHead,float desiredPitch,
                    boolean onGround,boolean allowed,boolean teleport,int targetId){
  if(!Double.isFinite(now+nx+ny+nz)||!Float.isFinite(vanillaBody+desiredHead+desiredPitch))return;
  double delta=now-time;
  if(Double.isFinite(time)&&delta>=0&&delta<1e-7)return;
  boolean relocation=teleport&&!wasTeleport;wasTeleport=teleport;
  if(!Double.isFinite(time)||delta<0||delta>.25||relocation){
   x=nx;y=ny;z=nz;time=now;bodyYaw=vanillaBody;headYaw=Math.clamp(wrap(desiredHead-bodyYaw),-65,65);headPitch=Math.clamp(desiredPitch,-65,45);
   speed=0;move=run=air=landing=lean=0;airTime=0;grounded=onGround;lastVY=0;phase=0;weight=allowed?1:0;nextVariation=now+interval();variationStart=ackStart=-100;lastTarget=targetId;evaluate(now);return;
  }
  if(delta<1e-7)return; // repeated layers/cameras at the same game time are idempotent
  float dt=(float)delta;double dx=teleport?0:nx-x,dz=teleport?0:nz-z,dist=Math.hypot(dx,dz);float vy=(float)((ny-y)/delta);
  double measured=dist/delta;
  float oldSpeed=(float)speed;speed=Math.min(12,measured);
  float desiredMove=allowed?(float)Math.clamp(speed/.9,0,1):0;
  move=smooth(move,desiredMove,dt,speed<.025?.055f:.11f);
  run=smooth(run,speed>2.68?1:0,dt,.16f);weight=smooth(weight,allowed?1:0,dt,.12f);
  float desiredBody=vanillaBody;
  if(dist>1e-5 && allowed){
   float heading=(float)Math.toDegrees(Math.atan2(-dx,dz)),gap=wrap(heading-vanillaBody);
   // Forward follows the path. Backward/strafe preserve requested facing instead of spinning180.
   if(Math.abs(gap)<55)desiredBody=heading;
  } else if(allowed){float gap=wrap(desiredHead-vanillaBody);if(Math.abs(gap)>45)desiredBody=vanillaBody+gap-Math.copySign(35,gap);}
  bodyYaw=approachAngle(bodyYaw,desiredBody,dt,160);
  headYaw=smooth(headYaw,Math.clamp(wrap(desiredHead-bodyYaw),-65,65),dt,.12f);
  headPitch=smooth(headPitch,Math.clamp(desiredPitch,-65,45),dt,.14f);
  if(dist>1e-6 && onGround && allowed){
   double angle=Math.toRadians(bodyYaw);float f=(float)((-Math.sin(angle)*dx+Math.cos(angle)*dz)/dist),s=(float)((Math.cos(angle)*dx+Math.sin(angle)*dz)/dist);
   forward=smooth(forward,f,dt,.09f);side=smooth(side,s,dt,.09f);
   phase+=dist/(1.12+.65*run)*Math.PI*2; // distance, NOT a wall-clock loop after stopping
  }
  lean=smooth(lean,Math.clamp(((float)speed-oldSpeed)/Math.max(.01f,dt)*.006f,-.09f,.09f),dt,.12f);
  if(!onGround)airTime+=delta;
  if(onGround&&!grounded&&airTime>.1&&lastVY<-.35)landing=Math.clamp(-lastVY*.055f,0,.3f);
  if(onGround)airTime=0;
  air=smooth(air,!onGround&&airTime>.04?1:0,dt,.07f);landing*=Math.exp(-dt/.11f);
  if(allowed&&move<.05&&now>=nextVariation){variationStart=now;nextVariation=now+interval();}
  if(targetId!=lastTarget){if(allowed&&now-ackStart>3)ackStart=now;lastTarget=targetId;}
  lastVY=vy;grounded=onGround;x=nx;y=ny;z=nz;time=now;evaluate(now);
 }
 private void evaluate(double now){
  for(var row:pose)java.util.Arrays.fill(row,0);
  float swing=(float)Math.sin(phase),lift=(float)Math.cos(phase),gait=move*(1-air),amp=.42f+.38f*run;
  float idle=(1-move)*(1-air),balance=(float)Math.sin(now*.67)*.014f*idle;
  float variation=envelope(now-variationStart,2.8),ack=envelope(now-ackStart,.9);
  pose[BODY][0]=-.08f*run*gait-lean+landing*.35f;
  pose[BODY][1]=(float)Math.sin(phase)*.035f*gait;
  pose[BODY][2]=balance+.028f*swing*gait;
  pose[ROOT][3]=balance*1.2f;
  pose[ROOT][4]=(.16f+.22f*run)*(1-Math.abs(lift))*gait-landing;
  pose[HEAD][0]=-(float)Math.toRadians(headPitch)+.025f*ack;
  pose[HEAD][1]=(float)Math.toRadians(headYaw);
  for(int i=0;i<2;i++){
   float sign=i==0?1:-1,step=swing*sign;
   pose[LEFT_LEG+i][0]=amp*step*forward*gait+air*(i==0?-.28f:.18f);
   pose[LEFT_LEG+i][2]=amp*.48f*step*side*gait;
   // Gecko runtime X is the opposite of the authored Bedrock X: knees fold back.
   pose[LEFT_SHIN+i][0]=-(Math.max(0,-lift*sign)*(.46f+.55f*run)*gait+air*(i==0?.55f:.32f)+landing*.7f);
   pose[LEFT_ARM+i][0]=-(.95f+.12f*run)*amp*step*forward*gait-air*.35f;
   pose[LEFT_ARM+i][2]=sign*(.055f+.03f*idle)+.11f*step*side*gait+sign*.25f*air;
   pose[LEFT_FOREARM+i][0]=(.08f+(.12f+.48f*run+.12f*Math.max(0,-step))*gait+.1f*air);
  }
  // Restrained wrist/forearm acknowledgement and rare idle weight variation, not another full clip.
  pose[RIGHT_ARM][0]-=.13f*ack+.07f*variation*idle;
  pose[RIGHT_FOREARM][0]+=.24f*ack+.18f*variation*idle;
  pose[RIGHT_ARM][2]-=.06f*ack;
  pose[BODY][2]+=.025f*variation*idle;
 }
 private static float envelope(double age,double duration){return age<=0||age>=duration?0:(float)(Math.sin(Math.PI*age/duration)*Math.sin(Math.PI*age/duration));}
}
