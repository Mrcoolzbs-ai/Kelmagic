package dev.kelmagic.client;
import net.minecraft.world.item.ItemStack;import net.minecraft.core.component.DataComponents;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.entity.EquipmentSlot;
public final class TrimAppearance {
 public static final java.util.List<String> PATTERNS=java.util.List.of("sentry","dune","coast","wild","ward","eye","vex","tide","snout","rib","spire","wayfinder","shaper","silence","raiser","host","flow","bolt");
 public static final java.util.List<String> MATERIALS=java.util.List.of("quartz","iron","netherite","redstone","copper","gold","emerald","diamond","lapis","amethyst");
 public static int index(ItemStack s){var t=s.get(DataComponents.TRIM);if(t==null)return 0;int p=PATTERNS.indexOf(t.pattern().value().assetId().getPath()),m=MATERIALS.indexOf(t.material().value().assetName());return p<0||m<0?0:1+p*10+m;}
 public static ResourceLocation texture(ItemStack s,boolean robe,EquipmentSlot slot){var t=s.get(DataComponents.TRIM);String pattern=t.pattern().value().assetId().getPath(),material=t.material().value().assetName();return ResourceLocation.fromNamespaceAndPath("kelmagic","textures/trims/"+(robe?"mage":"arcane")+"_"+slot.getName()+"/"+pattern+"_"+material+".png");}
 public static int color(ItemStack s){return 0xFFFFFFFF;}
 private TrimAppearance(){}
}
