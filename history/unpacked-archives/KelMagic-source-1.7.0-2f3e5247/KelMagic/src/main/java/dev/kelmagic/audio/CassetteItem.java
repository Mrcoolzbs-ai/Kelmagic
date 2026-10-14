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
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){var w=words(s);t.add(Component.translatable(w.isEmpty()?"kelmagic.tape.blank":"kelmagic.tape.recorded"));if(!w.isEmpty()){t.add(Component.literal(String.join(" ",w)));t.add(Component.translatable("kelmagic.tape.cost",TapePlayerItem.cost(w)));}}
}
