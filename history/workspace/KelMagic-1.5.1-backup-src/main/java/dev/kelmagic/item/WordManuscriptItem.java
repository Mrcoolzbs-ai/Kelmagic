package dev.kelmagic.item;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class WordManuscriptItem extends Item {
 public final String word;
 public WordManuscriptItem(Properties p,String w){super(p);word=w;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.translatable("kelmagic.page_hint"));}
}
