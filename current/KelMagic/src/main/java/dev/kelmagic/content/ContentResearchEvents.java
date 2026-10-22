package dev.kelmagic.content;
import dev.kelmagic.research.Research;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="kelmagic")
public final class ContentResearchEvents {
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p&&p.tickCount%10==0&&Research.hasPaper(p,"midnight_wood")&&p.getInventory().contains(MagicGrove.LOG.toStack()))Research.observe(p,"carry:kelmagic:midnight_log");}
}
