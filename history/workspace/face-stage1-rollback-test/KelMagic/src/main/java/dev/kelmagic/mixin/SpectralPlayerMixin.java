package dev.kelmagic.mixin;
import dev.kelmagic.MagicContent;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(Player.class)
public abstract class SpectralPlayerMixin {
 @Redirect(method="tryToStartFallFlying",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z"))private boolean kelmagic$start(ItemStack s,LivingEntity e){return e.hasEffect(MagicContent.WINGS)||s.canElytraFly(e);}
}