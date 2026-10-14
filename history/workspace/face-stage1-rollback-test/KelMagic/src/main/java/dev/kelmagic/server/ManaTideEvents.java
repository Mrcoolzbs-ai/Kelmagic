package dev.kelmagic.server;
import dev.kelmagic.*;import net.minecraft.server.level.ServerPlayer;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
@EventBusSubscriber(modid=KelMagic.MODID) public final class ManaTideEvents {
 @SubscribeEvent public static void removed(MobEffectEvent.Remove e){if(e.getEntity() instanceof ServerPlayer p&&e.getEffect()==MagicContent.MANA_TIDE){p.setData(MagicAttachments.MANA_BOOST_END,0L);MagicServer.sync(p);}}
 @SubscribeEvent public static void expired(MobEffectEvent.Expired e){if(e.getEntity() instanceof ServerPlayer p&&e.getEffectInstance()!=null&&e.getEffectInstance().is(MagicContent.MANA_TIDE)){p.setData(MagicAttachments.MANA_BOOST_END,0L);MagicServer.sync(p);}}
}
