import dev.kelmagic.client.face.*;
public class ComposeCheck {
 static void ok(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] a){var z=EyeExpressionController.Frame.ZERO;var up=new EyeExpressionController.Frame(0,0,0,0,0,0,.12f,.35f);
  var closed=new EyeStateController.Frame(0,0,1,false,0,0,0,0);ok(EyeExpressions.compose(closed,up).leftOpening()==0,"expression opens a closed natural blink");
  var light=new EyeStateController.Frame(.4f,.4f,0,false,0,0,0,0);ok(EyeExpressions.compose(light,up).leftOpening()<.45f,"widen erased background squint");
  var changed=new EyeStateController.Frame(.37f,.62f,0,false,.1f,.2f,.3f,.4f);ok(EyeExpressions.compose(changed,z).equals(changed),"completion/cancel fails to restore CURRENT input");
  var wink=new EyeExpressionController.Frame(0,0,0,0,1,0,0,0);var f=EyeExpressions.compose(changed,wink);ok(f.leftOpening()==0&&f.rightOpening()==.62f,"wink independence");ok(f.cheekLeft()==.1f&&f.lowerRight()==.4f,"other channels changed");
  System.out.println("PASS production EyeExpressions.compose: closed blink dominance, background squint retained, live-state restoration, unilateral wink, untouched muscle channels.");
 }
}
