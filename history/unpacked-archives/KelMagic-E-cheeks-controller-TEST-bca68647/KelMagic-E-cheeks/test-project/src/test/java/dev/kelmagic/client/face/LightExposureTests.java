package dev.kelmagic.client.face;
public final class LightExposureTests {
 static int n;static void ok(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
 static boolean near(double a,double b){return Math.abs(a-b)<1e-9;}
 public static void main(String[] args){
  var noon=LightExposureMath.sunDirection(0);ok(near(noon.x(),0)&&near(noon.y(),1),"noon sky basis");
  var east=LightExposureMath.sunDirection(.75);ok(near(east.x(),1),"east sunrise basis");
  for(int i=0;i<10000;i++){double a=i/10000.0;var d=LightExposureMath.sunDirection(a);ok(near(d.x()*d.x()+d.y()*d.y(),1),"sun unit vector");
   double dot=-1+2*a;double s=LightExposureMath.sun(dot,a,0,0),l=LightExposureMath.lava(dot,6*a);
   ok(s>=0&&s<=1&&l>=0&&l<=1,"bounded exposure");ok(LightExposureMath.combine(s,l)<=1,"bounded composite");}
  ok(LightExposureMath.angular(Math.cos(Math.toRadians(33)))==0,"outside gaze cone");ok(LightExposureMath.angular(-1)==0,"behind head");
  ok(LightExposureMath.lava(1,2)>LightExposureMath.lava(1,4),"distance attenuation");ok(LightExposureMath.lava(1,6)==0,"finite lava range");
  ok(LightExposureMath.sun(1,-.2,0,0)==0,"sun below horizon");ok(LightExposureMath.sun(1,1,1,1)<LightExposureMath.sun(1,1,0,0),"weather attenuation");
  double steady=0;for(int i=0;i<80;i++)steady=LightExposureMath.approach(steady,1,.25);ok(steady>.999,"attack settles");
  for(int i=0;i<80;i++)steady=LightExposureMath.approach(steady,0,.25);ok(steady<.00001,"release settles");
  double p=0,q=0;for(int i=0;i<100;i++)p=LightExposureMath.approach(p,1,.01);for(int i=0;i<4;i++)q=LightExposureMath.approach(q,1,.25);ok(near(p,q),"time-domain smoothing");
  var c=new EyeStateController(123,0,new EyeStateController.BlinkProfile(.1,.05,.16,.5,.5));c.setBase(0,1,1,0);
  c.setBase(.1,(1-.35f)/.93f,(1-.35f)/.93f,.45f);boolean full=false;
  for(int i=0;i<600;i++){var f=c.sample(.1+i*.01);if(f.leftOpening()==0)full=true;ok(f.leftOpening()>=0&&f.leftOpening()<=1,"expression light composition bounds");}
  ok(full,"blink closes over angry+light");
  var b=new EyeStateController(3,0,EyeStateController.NORMAL);b.setBase(0,1,1,.4f);b.requestBurst(1,3,EyeStateController.FAST,.075);b.setBase(1.2,1,1,.1f);ok(Math.abs(b.sample(2).leftOpening()-.9)<.00001,"reaction returns to current light");
  System.out.println("LIGHT_EXPOSURE_TESTS_OK checks="+n);
 }
}
