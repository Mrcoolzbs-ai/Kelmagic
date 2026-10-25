package dev.kelmagic.mixin;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MobEffectInstance.class)
public interface ArtifactEffectAccess { @Accessor("duration") void kelmagic$duration(int ticks); }
