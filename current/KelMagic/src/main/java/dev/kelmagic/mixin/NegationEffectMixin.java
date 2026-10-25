package dev.kelmagic.mixin;
import dev.kelmagic.artifact.ArtifactRuntime;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class)
public abstract class NegationEffectMixin {
    @Inject(method="addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",at=@At("HEAD"),cancellable=true)
    private void kelmagic$deny(MobEffectInstance effect,Entity source,CallbackInfoReturnable<Boolean> ci){if(ArtifactRuntime.immune((LivingEntity)(Object)this))ci.setReturnValue(false);}
    @Inject(method="forceAddEffect",at=@At("HEAD"),cancellable=true)
    private void kelmagic$denyForced(MobEffectInstance effect,Entity source,CallbackInfo ci){if(ArtifactRuntime.immune((LivingEntity)(Object)this))ci.cancel();}
}
