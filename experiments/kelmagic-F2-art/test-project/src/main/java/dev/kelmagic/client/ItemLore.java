package dev.kelmagic.client;
import dev.kelmagic.KelMagic;
import dev.kelmagic.core.SpellProfiles;
import dev.kelmagic.item.WordManuscriptItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
/** Flavour is separate from controls and authoritative gameplay data. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class ItemLore {
 @SubscribeEvent public static void tooltip(ItemTooltipEvent event){
  var item=event.getItemStack().getItem();String text;
  if(item instanceof WordManuscriptItem page)text=SpellProfiles.describe(page.word);
  else {var id=BuiltInRegistries.ITEM.getKey(item);if(!id.toString().equals("kelmagic:ancient_manuscript"))return;text=I18n.get("lore.kelmagic.ancient_manuscript");}
  String line="";for(String word:text.split(" ")){if(line.length()+word.length()>48&&!line.isEmpty()){event.getToolTip().add(Component.literal(line).withStyle(ChatFormatting.GRAY));line="";}line+=line.isEmpty()?word:" "+word;}if(!line.isEmpty())event.getToolTip().add(Component.literal(line).withStyle(ChatFormatting.GRAY));
 }
 private ItemLore(){}
}
