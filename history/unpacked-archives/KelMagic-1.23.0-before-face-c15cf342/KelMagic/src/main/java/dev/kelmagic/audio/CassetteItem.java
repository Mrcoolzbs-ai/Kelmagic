package dev.kelmagic.audio;
import dev.kelmagic.MagicComponents;
import dev.kelmagic.item.ScripterItem;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class CassetteItem extends Item {
 public CassetteItem(Properties p){super(p);}
 public static List<String> words(ItemStack s){return s.getOrDefault(MagicComponents.SPELL.get(),List.of());}
 public static boolean recorded(ItemStack s){return s.getItem() instanceof CassetteItem && ScripterItem.valid(words(s));}
 public static boolean blank(ItemStack s){return s.getItem() instanceof CassetteItem && words(s).isEmpty();}
 @Override public Component getName(ItemStack s){return Component.translatable(recorded(s)?"item.kelmagic.recorded_cassette":"item.kelmagic.cassette");}
 public static int patternIndex(ItemStack s){String id=s.getOrDefault(MagicComponents.TAPE_PATTERN.get(),"");if(id.isEmpty())return 0;return switch(id){case "minecraft:flower_banner_pattern"->1;case "minecraft:creeper_banner_pattern"->2;case "minecraft:skull_banner_pattern"->3;case "minecraft:mojang_banner_pattern"->4;case "minecraft:globe_banner_pattern"->5;case "minecraft:piglin_banner_pattern"->6;case "minecraft:flow_banner_pattern"->7;case "minecraft:guster_banner_pattern"->8;default->9;};}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){String icon=s.getOrDefault(MagicComponents.TAPE_PATTERN.get(),"");if(!icon.isEmpty()){var parsed=net.minecraft.resources.ResourceLocation.tryParse(icon);if(parsed!=null)t.add(Component.translatable("kelmagic.tape.pattern",net.minecraft.core.registries.BuiltInRegistries.ITEM.get(parsed).getDescription()));}
 var w=words(s);t.add(Component.translatable(w.isEmpty()?"kelmagic.tape.blank":"kelmagic.tape.recorded"));if(!w.isEmpty()){t.add(Component.literal(String.join(" ",w)));t.add(Component.translatable("kelmagic.tape.cost",TapePlayerItem.cost(w)));}}
}
