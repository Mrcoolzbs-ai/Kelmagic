package dev.kelmagic.item;
import dev.kelmagic.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
public final class AncientManuscriptItem extends Item {
 public AncientManuscriptItem(Properties p){super(p);}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
  var s=p.getItemInHand(h);
  if(p instanceof ServerPlayer){
   var keys=new ArrayList<>(MagicArchive.PAGES.keySet().stream().filter(w->dev.kelmagic.core.WordRarity.of(w)==s.getOrDefault(net.minecraft.core.component.DataComponents.RARITY,Rarity.COMMON)).toList());if(keys.isEmpty())return InteractionResultHolder.fail(s);String word=keys.get(l.random.nextInt(keys.size()));
   var page=MagicArchive.PAGES.get(word).toStack();p.setItemInHand(h,page);
   p.displayClientMessage(Component.translatable("kelmagic.manuscript_revealed",word),false);return InteractionResultHolder.success(page);
  }
  return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
 }
}
