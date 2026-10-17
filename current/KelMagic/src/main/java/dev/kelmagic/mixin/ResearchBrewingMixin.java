package dev.kelmagic.mixin;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;import net.minecraft.world.level.Level;import net.minecraft.core.BlockPos;import net.minecraft.world.level.block.state.BlockState;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(BrewingStandBlockEntity.class)public abstract class ResearchBrewingMixin {
 @Inject(method="serverTick",at=@At("HEAD"),cancellable=true)private static void gate(Level l,BlockPos at,BlockState state,BrewingStandBlockEntity be,CallbackInfo ci){if(l instanceof net.minecraft.server.level.ServerLevel s&&dev.kelmagic.research.ResearchBrewing.gated(be)&&!dev.kelmagic.research.ResearchBrewing.allowed(s,be))ci.cancel();}
}
