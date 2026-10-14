package dev.kelmagic.mixin;
import dev.kelmagic.MagicContent;import net.minecraft.world.entity.*;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(LivingEntity.class)
public abstract class SpectralFlightMixin {
 @Redirect(method="updateFallFlying",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))private boolean kelmagic$wings(ItemStack s,LivingEntity e){return e.hasEffect(MagicContent.WINGS)||s.canElytraFly(e);}
 @Redirect(method="updateFallFlying",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;elytraFlightTick(Lnet/minecraft/world/entity/LivingEntity;I)Z"))private boolean kelmagic$noWear(ItemStack s,LivingEntity e,int ticks){return e.hasEffect(MagicContent.WINGS)||s.elytraFlightTick(e,ticks);}
}