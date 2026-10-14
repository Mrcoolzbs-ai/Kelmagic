package dev.kelmagic.item;
import dev.kelmagic.server.MagicAttachments;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
/** Conditional regeneration does not extinguish either fire status or affect lava damage. */
public final class FlameLifeEffect extends MobEffect {
 public FlameLifeEffect(){super(MobEffectCategory.BENEFICIAL,0xC976EA);}
 @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return duration%20==0;}
 @Override public boolean applyEffectTick(LivingEntity e,int amplifier){
  if(!e.level().isClientSide && e.isAlive() && (e.isOnFire() || e.getData(MagicAttachments.ARCANE_BURN)>0))
   e.heal(e.getData(MagicAttachments.FLAME_HEAL));
  return true;
 }
}
