package dev.kelmagic.client;
import net.minecraft.world.item.ItemStack;import net.minecraft.core.component.DataComponents;
public final class TrimAppearance {
 public static int color(ItemStack s){var t=s.get(DataComponents.TRIM);if(t==null)return 0xFFFFFFFF;String a=t.material().value().assetName();return 0xFF000000|switch(a){case "quartz"->0xE4DED4;case "iron"->0xD5D5D5;case "netherite"->0x625B60;case "redstone"->0xB33030;case "copper"->0xB66B46;case "gold"->0xDEB648;case "emerald"->0x35B86C;case "diamond"->0x57D1CB;case "lapis"->0x354CAF;case "amethyst"->0xA378D3;default->0xE4DED4;};}
 private TrimAppearance(){}
}