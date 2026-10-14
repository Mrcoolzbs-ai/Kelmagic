package dev.kelmagic.item;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class WordManuscriptItem extends Item {
 public final String word;
 public WordManuscriptItem(Properties p,String w){super(p);word=w;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal(dev.kelmagic.core.KelLexicon.WORDS.get(word).translation()));t.add(Component.literal(dev.kelmagic.core.GlyphWriting.write(word)).withStyle(net.minecraft.network.chat.Style.EMPTY.withFont(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","kel"))));t.add(Component.literal(dev.kelmagic.core.SpellProfiles.describe(word)));t.add(Component.literal("Редкость: "+dev.kelmagic.core.WordRarity.label(word)));t.add(Component.translatable("kelmagic.page_hint"));}
}
