package dev.kelmagic.core;
import java.util.*;
public final class PhraseGeometryTests {
 static int checks;static void ok(boolean b,String n){checks++;if(!b)throw new AssertionError(n);}
 public static void main(String[] args){
  for(int kind=0;kind<3;kind++)for(double scale:new double[]{.1,1,2,11.4})for(int seed:new int[]{0,42,Integer.MIN_VALUE,Integer.MAX_VALUE})for(int age=-1;age<=45;age++){
   final int[] count={0};final int k=kind,a=age;
   PhraseGeometry.draw(kind,age,scale,seed,(x,y,z,style,size,alpha)->{count[0]++;ok(Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z),"finite");ok(Math.abs(x)<=8&&Math.abs(z)<=8&&y>=0&&y<=7,"bounds");ok(style>=0&&style<=5&&size>0&&size<=.12&&alpha>=0&&alpha<=1,"style/opacity");});
   ok(count[0]<=256,"bounded point generation "+k+"/"+a);if(age<0||age>=PhraseGeometry.lifetime(kind))ok(count[0]==0,"expiry");
  }
  for(var words:List.of(List.of("vit","pir"),List.of("vin","dun"),List.of("kor","ten"))){var p=SpellParser.plan(words);ok(p.effect()==SpellParser.Effect.SYNERGY,"existing grammar "+words);var reversed=new ArrayList<>(words);Collections.reverse(reversed);ok(SpellParser.plan(reversed).equals(p),"order independent");}
  final int[] count={0};PhraseGeometry.draw(77,0,1,0,(x,y,z,s,size,a)->count[0]++);ok(count[0]==0,"unknown ignored");
  System.out.println("PHRASE_GEOMETRY_PASS checks="+checks);
 }
}
