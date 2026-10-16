package dev.kelmagic.item;
import dev.kelmagic.*;
import dev.kelmagic.core.KelLexicon;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
public final class CodexItem extends Item {
 public CodexItem(Properties p){super(p);}
 public static List<String> knownWords(ItemStack s){return s.getOrDefault(MagicComponents.CODEX_WORDS.get(),List.<String>of()).stream().filter(KelLexicon.WORDS::containsKey).distinct().limit(KelLexicon.CODEX_LIMIT).toList();}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){
  var s=p.getItemInHand(h);
  if(p instanceof ServerPlayer&&s.has(MagicComponents.CODEX_TOKEN.get())&&!s.has(MagicComponents.CODEX_WELCOMED.get()))s.set(MagicComponents.CODEX_WELCOMED.get(),true);
  if(p instanceof ServerPlayer&&!s.has(MagicComponents.CODEX_TOKEN.get()))s.set(MagicComponents.CODEX_TOKEN.get(),UUID.randomUUID().toString());
  if(p instanceof ServerPlayer sp){if(!knownWords(s).isEmpty()){dev.kelmagic.research.Research.tab(sp,"words");dev.kelmagic.research.Research.tab(sp,"writing");}dev.kelmagic.research.Research.sync(sp);}
  if(p instanceof ServerPlayer sp){boolean first=!s.getOrDefault(MagicComponents.CODEX_WELCOMED.get(),false);int chapter=s.getOrDefault(MagicComponents.CODEX_SECTION.get(),knownWords(s).isEmpty()?1:0);if(chapter<0||chapter>8||chapter!=1&&!dev.kelmagic.research.Research.state(sp).tabs().contains(new String[]{"words","origins","materials","sound","equipment","writing","great","home","infusion"}[chapter]))chapter=knownWords(s).isEmpty()?1:0;
   net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,new dev.kelmagic.network.CodexOpen(knownWords(s),s.getOrDefault(MagicComponents.CODEX_NOTES.get(),List.<String>of()).stream().limit(KelLexicon.CODEX_LIMIT).map(n->n.length()>240?n.substring(0,240):n).toList(),h==InteractionHand.OFF_HAND,first?7:chapter,s.getOrDefault(MagicComponents.CODEX_PAGE.get(),0),s.getOrDefault(MagicComponents.CODEX_QUERY.get(),""),s.getOrDefault(MagicComponents.CODEX_TOKEN.get(),""),s.getOrDefault(MagicComponents.CODEX_SORT.get(),0),s.getOrDefault(MagicComponents.CODEX_KNOWN.get(),false)));s.set(MagicComponents.CODEX_WELCOMED.get(),true);
  }
  return InteractionResultHolder.sidedSuccess(s,l.isClientSide);
 }
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
  t.add(Component.translatable("kelmagic.codex_copy"));t.add(Component.translatable("kelmagic.codex_hint"));t.add(Component.translatable("kelmagic.codex_count",knownWords(s).size(),KelLexicon.WORDS.size()));
 }
}
