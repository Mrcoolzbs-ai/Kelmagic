package dev.kelmagic.compat;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

/** No Curios types in this always-loadable entry point. */
public final class MonocleAccessories {
 private static final boolean LOADED=ModList.get().isLoaded("curios");
 public static boolean loaded(){return LOADED;}
 public static void init(IEventBus bus){if(LOADED)bus.addListener(CuriosMonocle::setup);}
 public static boolean equipped(Player player){return LOADED&&CuriosMonocle.equipped(player);}
 private MonocleAccessories(){}
}
