package dev.kelmagic.client.face;
public final class EyeStateTests {
 static int checks;static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static boolean eq(float a,float b){return Math.abs(a-b)<.00001;}
 static EyeStateController make(){var c=new EyeStateController(123,0,EyeStateController.NORMAL);c.setBase(0,1,1,0);return c;}
 public static void main(String[] args){
  for(int fps:new int[]{15,30,60,144}){var a=make();var b=make();for(int tenth=0;tenth<1200;tenth++){
   double end=tenth/10.0;double start=(tenth-1)/10.0;for(double t=Math.max(0,start);t<end;t+=1.0/fps)a.sample(t);
   var x=a.sample(end);var y=b.sample(end);ok(eq(x.leftOpening(),y.leftOpening()),"FPS schedule "+fps);
   ok(x.leftOpening()>=0&&x.leftOpening()<=1,"bounds");}}
  var c=make();c.setBase(1,1,1,.45f);ok(eq(c.sample(1.3).leftOpening(),.55f),"sustained squint");
  c.requestBurst(1.3,3,EyeStateController.FAST,.075);ok(eq(c.sample(1.38).leftOpening(),0),"burst full closure");
  c.setBase(1.4,1,1,.25f);ok(eq(c.sample(2.2).leftOpening(),.75f),"return CURRENT baseline");ok(!c.sample(2.2).burstActive(),"burst ended");
  var d=make();d.setBase(1,.8f,.6f,.2f);ok(eq(d.sample(1.3).leftOpening(),.64f)&&eq(d.sample(1.3).rightOpening(),.48f),"independent channels");
  var e=make();e.setBlinkProfile(0,new EyeStateController.BlinkProfile(.1,.05,.16,.1,.1));
  var before=e.sample(.16);e.requestBurst(.16,3,EyeStateController.FAST,.075);ok(eq(before.leftOpening(),e.sample(.16).leftOpening()),"active blink continuity");
  boolean full=false;for(int i=0;i<500;i++){var f=e.sample(.16+i*.01);if(f.leftOpening()==0)full=true;ok(eq(f.leftOpening(),f.rightOpening()),"paired closure");}ok(full,"full closed hold");
  var f=make();float prev=1;for(int i=0;i<20000;i++){var x=f.sample(i*.001);ok(Math.abs(prev-x.leftOpening())<.025,"continuous pulse");prev=x.leftOpening();}
  var g=make();g.setBase(0,1,1,.4f);boolean squintClosed=false;for(int i=0;i<20000;i++){var x=g.sample(i*.001);if(x.leftOpening()==0)squintClosed=true;ok(i<220||x.leftOpening()<=.60001,"squint retained");}ok(squintClosed,"ordinary blink over squint");
  try{new EyeStateController.BlinkProfile(0,0,.1,1,2);throw new AssertionError();}catch(IllegalArgumentException expected){checks++;}
  System.out.println("EYE_STATE_TESTS_OK checks="+checks);
 }
}
