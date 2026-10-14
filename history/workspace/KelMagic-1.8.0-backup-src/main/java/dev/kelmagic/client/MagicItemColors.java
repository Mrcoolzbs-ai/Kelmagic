package dev.kelmagic.client;
import dev.kelmagic.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MagicItemColors {
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register((stack,layer)->layer==0?net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack,0x443347)|0xFF000000:0xFFFFFFFF,MagicAudio.CASSETTE.get());
        for(var item:MagicContent.ROBE_ITEMS) e.register((stack,layer)->layer==0?net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack,0xFF635875) | 0xFF000000:0xFFFFFFFF,item.get());
    }
    private MagicItemColors() {}
}
