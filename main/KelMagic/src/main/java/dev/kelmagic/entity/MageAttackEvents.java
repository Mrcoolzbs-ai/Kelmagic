package dev.kelmagic.entity;
import dev.kelmagic.KelMagic;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
/** Capture the attack charge before vanilla resets it, so only Sharpness's real delta survives. */
@EventBusSubscriber(modid=KelMagic.MODID)
public final class MageAttackEvents {
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(!e.isCanceled()&&!e.getEntity().level().isClientSide&&e.getTarget() instanceof RedMage m)m.prepareSharpnessAttack(e.getEntity());}
}
