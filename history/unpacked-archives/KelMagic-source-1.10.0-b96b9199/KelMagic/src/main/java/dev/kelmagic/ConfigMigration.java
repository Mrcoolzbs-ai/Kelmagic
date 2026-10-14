package dev.kelmagic;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
@EventBusSubscriber(modid=KelMagic.MODID,bus=EventBusSubscriber.Bus.MOD)
public final class ConfigMigration {
 @SubscribeEvent public static void load(net.neoforged.fml.event.config.ModConfigEvent.Loading e){if(e.getConfig().getSpec()!=Config.SPEC||Config.RANGE_MIGRATED.get())return;if(Config.RANGE.get()==24)Config.RANGE.set(48);Config.RANGE_MIGRATED.set(true);Config.SPEC.save();}
 private ConfigMigration(){}
}
