package dev.kelmagic.client.face;
public final class MouthPresentationTests {
 static int checks;static void ok(boolean v,String s){checks++;if(!v)throw new AssertionError(s);}
 public static void main(String[] args){
  for(int i=0;i<10000;i++){float time=i*.01f;float phase=(float)Math.sin(time*.07f);float off=BreathingPresentation.noseY(phase,true);ok(Math.abs(off)<=.012001f,"amplitude");ok(BreathingPresentation.noseY(phase,false)==0,"disable returns rest");ok(.05f+off>.0379f,"nose overlap remains");}
  for(int fps:new int[]{15,30,60,144})for(int i=0;i<500;i++){float t=i/20f;float phase=(float)Math.sin(t*.07f);ok(BreathingPresentation.noseY(phase,true)==phase*.012f,"single absolute phase "+fps);}
  ok(BreathingPresentation.noseY(1,true)>0&&BreathingPresentation.noseY(-1,true)<0,"sync phase direction");
  System.out.println("MOUTH_PRESENTATION_TESTS_OK checks="+checks);
 }
}
