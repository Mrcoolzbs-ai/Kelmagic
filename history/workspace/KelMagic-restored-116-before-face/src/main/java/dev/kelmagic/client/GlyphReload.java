package dev.kelmagic.client;
import dev.kelmagic.KelMagic;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)public final class GlyphReload {
 @SubscribeEvent public static void reload(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent e){e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)manager->ClientSpellState.VISUALS.values().forEach(v->{v.lines=null;v.widths=null;}));}
 private GlyphReload(){}
}
