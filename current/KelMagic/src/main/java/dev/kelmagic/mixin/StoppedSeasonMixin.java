package dev.kelmagic.mixin;
import dev.kelmagic.artifact.ArtifactSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class StoppedSeasonMixin {
    @Inject(method="randomTick",at=@At("HEAD"),cancellable=true)
    private void kelmagic$holdSeason(ServerLevel level,BlockPos pos,RandomSource random,CallbackInfo ci){if(ArtifactSeasons.locked(level,pos))ci.cancel();}
}
