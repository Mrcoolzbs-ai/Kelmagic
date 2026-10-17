package dev.kelmagic.client.hair;
public final class HairBridgeTests {
 public static void main(String[] args){int checks=0;
  for(int x=-12;x<=12;x++)for(int y=1;y<=16;y++)for(int z=-12;z<=12;z++){
   double dx=x*.03,dy=y*.03,dz=z*.03;var a=HairBridgeMath.aim(dx,dy,dz);
   double rx=-Math.sin(a.roll())*Math.cos(a.pitch())*a.length(),ry=Math.cos(a.roll())*Math.cos(a.pitch())*a.length(),rz=Math.sin(a.pitch())*a.length();
   if(Math.abs(rx-dx)+Math.abs(ry-dy)+Math.abs(rz-dz)>1e-9)throw new AssertionError("anchor mismatch");checks++;
  }
  if(HairBridgeMath.aim(0,0,0).length()!=0)throw new AssertionError("zero length");checks++;
  boolean bad=false;try{HairBridgeMath.aim(Double.NaN,1,0);}catch(IllegalArgumentException e){bad=true;}if(!bad)throw new AssertionError("NaN accepted");checks++;
  System.out.println("HAIR_BRIDGE_TESTS_OK checks="+checks+" (transform only; not proof of collision freedom)");
 }
}
