package dev.kelmagic.artifact;
import dev.kelmagic.KelMagic;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class ArtifactPlantEvents {
    @SubscribeEvent public static void growth(CropGrowEvent.Pre e){if(e.getLevel() instanceof ServerLevel l&&ArtifactSeasons.locked(l,e.getPos()))e.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);}
    @SubscribeEvent public static void bone(BonemealEvent e){if(e.getLevel() instanceof ServerLevel l&&ArtifactSeasons.locked(l,e.getPos()))e.setCanceled(true);}
}
