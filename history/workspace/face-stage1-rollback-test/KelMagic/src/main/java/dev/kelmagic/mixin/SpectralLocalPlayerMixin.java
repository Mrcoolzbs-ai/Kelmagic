package dev.kelmagic.mixin;
import dev.kelmagic.MagicContent;import net.minecraft.client.player.LocalPlayer;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(LocalPlayer.class) public abstract class SpectralLocalPlayerMixin {
 @Redirect(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z")) private boolean kelmagic$clientWings(ItemStack s,LivingEntity e){return e.hasEffect(MagicContent.WINGS)||s.canElytraFly(e);}
}