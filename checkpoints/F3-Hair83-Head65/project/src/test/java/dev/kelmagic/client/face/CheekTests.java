package dev.kelmagic.client.face;
public final class CheekTests {
 static int checks;static void ok(boolean v,String s){checks++;if(!v)throw new AssertionError(s);}
 static boolean eq(float a,float b){return Math.abs(a-b)<.00001;}
 static EyeStateController make(){var c=new EyeStateController(123,0,EyeStateController.NORMAL);c.setBase(0,1,1,0);return c;}
 public static void main(String[] args){
  for(int fps:new int[]{15,30,60,144}){var a=make();var b=make();a.setMuscles(0,.8f,.2f,.7f,.1f);b.setMuscles(0,.8f,.2f,.7f,.1f);
   for(int i=0;i<120;i++){double end=i*.1;for(double t=Math.max(0,end-.1);t<end;t+=1.0/fps)a.sample(t);var x=a.sample(end);var y=b.sample(end);ok(eq(x.cheekLeft(),y.cheekLeft()),"FPS muscle");ok(eq(x.leftOpening(),y.leftOpening()),"FPS blink");}}
  var c=make();c.setMuscles(0,.8f,.2f,.7f,.1f);c.setBase(0,1,1,.45f);c.requestBurst(1,3,EyeStateController.FAST,.075);boolean closed=false;
  for(int i=0;i<300;i++){var f=c.sample(1+i*.01);ok(eq(f.cheekLeft(),.8f)&&eq(f.cheekRight(),.2f),"no pulse cheek jitter");ok(eq(f.lowerLeft(),.7f),"lower stable");if(f.leftOpening()==0)closed=true;}ok(closed,"full closure");ok(eq(c.sample(4).leftOpening(),.55f),"return squint");
  var d=make();for(int i=0;i<1000;i++)ok(d.sample(i*.01).cheekLeft()==0,"normal blink no cheeks");
  for(float y:new float[]{25.5f,27.2f})for(float x:new float[]{-.9f,.9f,-3.85f,3.85f,2,-2})ok(CheekDeformation.weight(x,y)<.0001,"fixed boundary");
  // Actual corrected blush lies wholly within the rigid translation plateau.
  for(float x:new float[]{-2.7f,-1.85f,2.0f,2.9f})for(float y:new float[]{26.21f,26.69f})ok(eq(CheekDeformation.weight(x,y),1),"rigid blush");
  for(int i=0;i<1000;i++){float y=25.5f+i*1.7f/999;float yy=y+CheekDeformation.liftY(2,y,1);if(i>0){float prev=25.5f+(i-1)*1.7f/999;ok(yy>prev+CheekDeformation.liftY(2,prev,1),"no fold");}}
  System.out.println("CHEEK_TESTS_OK checks="+checks);
 }
}
