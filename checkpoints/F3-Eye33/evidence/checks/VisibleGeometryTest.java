import dev.kelmagic.client.face.*;import software.bernie.geckolib.cache.object.*;import org.joml.Vector3f;import net.minecraft.core.Direction;import net.minecraft.world.phys.Vec3;import java.util.*;
public class VisibleGeometryTest {
 record V(float x,float y,float u,float v){}
 static List<V> clip(List<V> in,int axis,float bound,boolean gt){var o=new ArrayList<V>();if(in.isEmpty())return o;V p=in.getLast();float pa=(axis==0?p.u:p.v)-bound;boolean pi=gt?pa>=0:pa<=0;for(V v:in){float a=(axis==0?v.u:v.v)-bound;boolean vi=gt?a>=0:a<=0;if(vi!=pi){float t=pa/(pa-a);o.add(new V(p.x+(v.x-p.x)*t,p.y+(v.y-p.y)*t,p.u+(v.u-p.u)*t,p.v+(v.v-p.v)*t));}if(vi)o.add(v);p=v;pa=a;pi=vi;}return o;}
 static double area(GeoQuad q){List<V> p=new ArrayList<>();for(var a:q.vertices())p.add(new V(a.position().x,a.position().y,a.texU(),a.texV()));p=clip(p,0,.4f,true);p=clip(p,0,.6f,false);p=clip(p,1,.2f,true);p=clip(p,1,.8f,false);double sum=0;for(int i=0;i<p.size();i++){var a=p.get(i);var b=p.get((i+1)%p.size());sum+=(double)a.x*b.y-(double)a.y*b.x;}return Math.abs(sum)/2;}
 public static void main(String[] args){int states=0,visible=0,closed=0;double least=1;
 for(boolean left:new boolean[]{false,true})for(float size:new float[]{.65f,1,1.35f})for(float dx:new float[]{-.8f,0,.8f})for(float dy:new float[]{-3,-.92f,0,1.14f,3})for(float mask:new float[]{0,.07f,.3676f,.7f,.9f,.95f,1})for(float lower:new float[]{0,1}){
 var b=new GeoBone(null,left?"pupil_left":"pupil_right",false,0.,false,false);float cx=left?-1.7f:1.7f,xl=(cx-.62f)/16,xh=(cx+.62f)/16,yl=27.49f/16,yh=28.91f/16,z=-3.91f/16;
 var vs=new GeoVertex[]{new GeoVertex(new Vector3f(xl,yh,z),0,0),new GeoVertex(new Vector3f(xh,yh,z),1,0),new GeoVertex(new Vector3f(xh,yl,z),1,1),new GeoVertex(new Vector3f(xl,yl,z),0,1)};
 b.getCubes().add(new GeoCube(new GeoQuad[]{new GeoQuad(vs,new Vector3f(0,0,-1),Direction.NORTH)},Vec3.ZERO,Vec3.ZERO,new Vec3(1.24,1.42,0),0,false));PupilIrisRig.apply(b,size,dx,dy,mask,lower);double black=0;
 for(var cube:b.getCubes())for(var q:cube.quads()){black+=area(q);for(var v:q.vertices()){var p=v.position();if(Float.floatToIntBits(p.z)!=Float.floatToIntBits(z))throw new AssertionError("Z");if(p.y>(29.3f-1.95f*mask)/16+1e-6||p.y<(27.35f+.22f*lower)/16-1e-6)throw new AssertionError("lid escape");}}
 boolean aperture=EyeTravel.bounds(size,dx,mask,lower,left).aperture();if(aperture){if(black<1e-9)throw new AssertionError("dark pupil disappeared size="+size+" dx="+dx+" dy="+dy+" mask="+mask+" lower="+lower);visible++;least=Math.min(least,black);}else closed++;states++;}
 System.out.println("PASS production PupilIrisRig: "+states+" geometry combinations; dark area positive in all "+visible+" nonclosed apertures, "+closed+" closed-aperture cases allow occlusion. Constant bitwise Z and shared live lid bounds. Minimum positive dark area="+least);
 }
}
