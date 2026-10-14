import java.nio.file.*;import java.util.*;
import org.joml.Vector3f;
import dev.kelmagic.client.face.*;
import dev.kelmagic.entity.RedMage;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.*;
public final class CheckNativeRig {
 static int checks;static void ok(boolean v,String s){checks++;if(!v)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception {
  var raw=KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(Path.of(args[0])),Model.class);var baked=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(raw));
  for(String name:new String[]{"cheek_left","cheek_right","lower_lid_left","lower_lid_right"})baked.getBone(name).orElseThrow().saveInitialSnapshot();
  var model=new GeoModel<RedMage>(){public ResourceLocation getModelResource(RedMage e){return null;}public ResourceLocation getTextureResource(RedMage e){return null;}public ResourceLocation getAnimationResource(RedMage e){return null;}@Override public Optional<GeoBone> getBone(String n){return baked.getBone(n);}};
  var bones=List.of(baked.getBone("cheek_left").orElseThrow(),baked.getBone("cheek_right").orElseThrow());
  var rest=new IdentityHashMap<GeoCube,GeoQuad[]>();for(var b:bones)for(var c:b.getCubes())rest.put(c,c.quads().clone());
  for(float amount:new float[]{0,.8f,1,0,.4f,0}){
   CheekRig.apply(model,new EyeStateController.Frame(1,1,0,false,amount,0,amount,0));
   for(int side=0;side<2;side++)for(var c:bones.get(side).getCubes())for(int q=0;q<c.quads().length;q++){
    var current=c.quads()[q];if(current==null)continue;var source=rest.get(c)[q];ok(current.normal().isFinite(),"normal finite");
    for(int i=0;i<4;i++){var s=source.vertices()[i];var v=current.vertices()[i];var p=new Vector3f(s.position());float a=side==0?amount:0;
     p.y+=CheekDeformation.liftY(p.x*16,p.y*16,a)/16;p.z+=CheekDeformation.liftZ(s.position().x*16,s.position().y*16,a)/16;
     ok(p.distance(v.position())<.000001,"rest based actual Gecko quad morph");ok(s.texU()==v.texU()&&s.texV()==v.texV(),"runtime UV immutable");
    }
   }
  }
  System.out.println("NATIVE_GECKO_RIG_TEST_OK checks="+checks+" independent sides, UV invariant, repeated nonzero/zero rest restoration");
 }
}
