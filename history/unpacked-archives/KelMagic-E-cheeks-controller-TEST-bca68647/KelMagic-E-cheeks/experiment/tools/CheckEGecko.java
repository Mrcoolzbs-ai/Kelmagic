import java.nio.file.*;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.loading.object.BakedModelFactory;
public final class CheckEGecko {
 public static void main(String[] args)throws Exception{
  for(String file:args){var model=KeyFramesAdapter.GEO_GSON.fromJson(Files.readString(Path.of(file)),Model.class);
   var tree=GeometryTree.fromModel(model);var baked=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(tree);
   for(String n:new String[]{"head","nose","mouth","pupil_left","pupil_right","cheek_left","cheek_right","lower_lid_left","lower_lid_right"})if(baked.getBone(n).isEmpty())throw new AssertionError("Missing "+n);
   System.out.println("GECKOLIB_4_9_3_BAKE_OK "+Path.of(file).getParent().getFileName()+" bones="+model.minecraftGeometry()[0].bones().length);
  }
 }
}
