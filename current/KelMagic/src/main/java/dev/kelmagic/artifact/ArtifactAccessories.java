package dev.kelmagic.artifact;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.IEventBus;
/** No linkage to Curios on a dedicated server without the optional dependency. */
public final class ArtifactAccessories {
    public static boolean loaded(){return ModList.get().isLoaded("curios");}
    public static void init(IEventBus bus){if(loaded())bus.addListener(ArtifactCurios::setup);}
    public static ItemStack stack(LivingEntity e,Item i){return loaded()?ArtifactCurios.stack(e,i):ItemStack.EMPTY;}
    public static boolean has(LivingEntity e,Item i){return !stack(e,i).isEmpty();}
    private ArtifactAccessories(){}
}
