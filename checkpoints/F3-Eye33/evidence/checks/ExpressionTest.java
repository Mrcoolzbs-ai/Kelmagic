import dev.kelmagic.client.face.EyeExpressionController;import dev.kelmagic.client.face.EyeExpressionController.*;
public class ExpressionTest {
 static void ok(boolean b,String m){if(!b)throw new AssertionError(m);}
 static boolean zero(Frame f){return f.equals(Frame.ZERO);}
 public static void main(String[] a){
  for(Preset p:Preset.values()){var c=new EyeExpressionController();c.play(0,p,new Options(2,1,.8f));ok(zero(c.sample(0)),p+" start");float energy=0;for(int i=0;i<=200;i++){var f=c.sample(i/100.0);ok(Float.isFinite(f.x()+f.y()+f.gazeWeight()+f.closeLeft()+f.closeRight()+f.widen()+f.pupil()),p+" finite");ok(f.gazeWeight()>=0&&f.gazeWeight()<=1&&f.closeLeft()>=0&&f.closeLeft()<=1&&f.closeRight()>=0&&f.closeRight()<=1,p+" bounds");energy+=Math.abs(f.x())+Math.abs(f.y())+f.closeLeft()+f.closeRight()+Math.abs(f.pupil())+f.widen();}ok(zero(c.sample(2.1)),p+" end");if(p!=Preset.RETURN_CALM)ok(energy>0,p+" placeholder");}
  var c=new EyeExpressionController();var token=c.play(0,Preset.EYE_ROLL,new Options(3,1,1));Frame before=c.sample(.6);ok(c.cancel(.6,token,.3),"cancel");ok(c.sample(.6).equals(before),"cancel continuity");ok(zero(c.sample(1)),"cancel returns neutral overlay");
  long n=c.play(1,Preset.SKEPTICAL,Options.defaults());ok(!c.cancel(1,token,.2),"stale cancel cannot kill replacement");Frame snap=c.sample(1.5);c.play(1.5,Preset.LOOK_DOWN,Options.defaults());ok(c.sample(1.5).equals(snap),"replace continuity");
  var half=new EyeExpressionController();half.play(0,Preset.EYE_ROLL,new Options(2,0,.5f));ok(zero(half.sample(.5)),"zero intensity");
  System.out.println("PASS "+Preset.values().length+" non-placeholder procedural presets (RETURN_CALM intentionally zero), endpoints, channel bounds, cancel/replacement continuity, stale handles and zero intensity. This is NOT a Minecraft check of all presets.");
 }
}
