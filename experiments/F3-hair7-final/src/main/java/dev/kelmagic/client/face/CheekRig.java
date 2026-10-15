package dev.kelmagic.client.face;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.cache.object.*;
import org.joml.Vector3f;
import java.util.*;
/** Sole E surface writer. Source quads are immutable snapshots; no cumulative deformation.
 * Geometry is model-shared: restore from rest for EVERY entity draw, including zero pose.
 * Four groups only; no morph library or dependency. Native GeoQuad API verified in GeckoLib 4.9.3.
 */
public final class CheekRig {
 private record Rest(GeoCube cube,int index,GeoQuad quad){}
 private static final Map<GeoBone,List<Rest>> CACHE=new WeakHashMap<>();
 public static void apply(GeoModel<?> model,EyeStateController.Frame frame){
  for(String side:new String[]{"left","right"}){
   boolean left=side.equals("left");float amount=left?frame.cheekLeft():frame.cheekRight();
   model.getBone("cheek_"+side).ifPresent(b->{
    var rest=CACHE.computeIfAbsent(b,bone->{List<Rest> list=new ArrayList<>();for(var c:bone.getCubes())for(int q=0;q<c.quads().length;q++)if(c.quads()[q]!=null)list.add(new Rest(c,q,c.quads()[q]));return list;});
    b.setPosX(b.getInitialSnapshot().getOffsetX());b.setPosY(b.getInitialSnapshot().getOffsetY());b.setPosZ(b.getInitialSnapshot().getOffsetZ());b.setScaleX(1);b.setScaleY(1);b.setScaleZ(1);
    for(var r:rest){var source=r.quad();if(amount==0){r.cube().quads()[r.index()]=source;continue;}
     var v=new GeoVertex[4];for(int i=0;i<4;i++){var a=source.vertices()[i];var p=new Vector3f(a.position());float x=p.x*16,y=p.y*16;p.y+=CheekDeformation.liftY(x,y,amount)/16;p.z+=CheekDeformation.liftZ(x,y,amount)/16;v[i]=new GeoVertex(p,a.texU(),a.texV());}
     var edge1=new Vector3f(v[1].position()).sub(v[0].position());var edge2=new Vector3f(v[2].position()).sub(v[0].position());var normal=edge1.cross(edge2).normalize();if(normal.dot(source.normal())<0)normal.negate();
     r.cube().quads()[r.index()]=new GeoQuad(v,normal,source.direction());
    }
   });
   model.getBone("lower_lid_"+side).ifPresent(b->{b.setPosY(b.getInitialSnapshot().getOffsetY()+.22f*(left?frame.lowerLeft():frame.lowerRight()));b.setScaleY(1);});
  }
 }
 private CheekRig(){}
}
