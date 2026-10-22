package dev.kelmagic.content;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid="kelmagic")
public final class GroveEvents {
 @SubscribeEvent public static void strip(net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent e){if(e.getItemAbility()!=net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP)return;var s=e.getState();if(s.is(MagicGrove.LOG.get()))e.setFinalState(MagicGrove.STRIPPED_LOG.get().withPropertiesOf(s));else if(s.is(MagicGrove.WOOD_BLOCK.get()))e.setFinalState(MagicGrove.STRIPPED_WOOD.get().withPropertiesOf(s));}
}
