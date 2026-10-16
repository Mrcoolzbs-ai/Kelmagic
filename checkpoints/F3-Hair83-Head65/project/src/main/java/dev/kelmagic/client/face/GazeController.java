package dev.kelmagic.client.face;
import net.minecraft.world.phys.Vec3;
import java.util.SplittableRandom;
/** Shared fixation point; 20 Hz motion updates + render interpolation. Never randomizes eyes separately. */
public final class GazeController {
 private final SplittableRandom random;private Vec3 previous,current;private double nextMicro,microStart=-10;private float fromX,fromY,toX,toY;
 public GazeController(long seed){random=new SplittableRandom(seed^0x47415a455632L);nextMicro=1+random.nextDouble()*1.8;}
 public void tick(double now,Vec3 wanted,boolean reading){
  if(current==null)previous=current=wanted;
  previous=current;current=current.lerp(wanted,1-Math.exp(-.05/(reading?.42:.14)));
  if(now>=nextMicro){var m=micro(now);fromX=m[0];fromY=m[1];toX=(float)((random.nextDouble()-.5)*.034);toY=(float)((random.nextDouble()-.5)*.020);microStart=now;nextMicro=now+1.2+random.nextDouble()*1.8;}
 }
 public Vec3 sample(float partial){return current==null?null:previous.lerp(current,Math.max(0,Math.min(1,partial)));}
 public float[] micro(double now){double t=Math.max(0,Math.min(1,(now-microStart)/.075));t=t*t*(3-2*t);return new float[]{(float)(fromX+(toX-fromX)*t),(float)(fromY+(toY-fromY)*t)};}
 /** Safe interior of existing stepped sockets, in model pixels. Does not rely on the skin to hide overshoot. */
 public static float[] offset(double x,double y,double forward,float size,boolean reading){
  if(forward<=.015)return new float[]{0,0};
  float maxX=Math.max(0,.87f-.62f*size-.025f),maxY=Math.max(0,.85f-.71f*size-.025f);
  float dx=(float)(Math.atan2(x,forward)*.80),dy=(float)(Math.atan2(y,forward)*(reading?.10:.30));
  return new float[]{Math.clamp(dx,-maxX,maxX),Math.clamp(dy,-maxY,maxY)};
 }
}
