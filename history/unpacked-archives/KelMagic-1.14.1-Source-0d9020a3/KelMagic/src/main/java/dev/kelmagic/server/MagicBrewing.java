package dev.kelmagic.server;
import dev.kelmagic.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class MagicBrewing {
    @SubscribeEvent public static void recipes(RegisterBrewingRecipesEvent e) {
        e.getBuilder().addMix(Potions.AWKWARD,MagicContent.MANA_CORE.get(),MagicContent.MANA);
        e.getBuilder().addMix(MagicContent.MANA,Items.REDSTONE,MagicContent.LONG_MANA);
    }
    private MagicBrewing() {}
}
