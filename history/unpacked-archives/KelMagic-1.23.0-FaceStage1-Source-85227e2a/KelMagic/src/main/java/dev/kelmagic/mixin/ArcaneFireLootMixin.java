package dev.kelmagic.mixin;
import dev.kelmagic.server.MagicAttachments;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Vanilla cooked-loot and on-fire predicates recognize the independent magic burn. */
@Mixin(EntityFlagsPredicate.class)
public abstract class ArcaneFireLootMixin {
 @Redirect(method="matches",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;isOnFire()Z"))
 private boolean kelmagic$burningForLoot(Entity e){
  return e.isOnFire() || e instanceof LivingEntity live && live.getData(MagicAttachments.ARCANE_BURN)>0;
 }
}
