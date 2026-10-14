package dev.kelmagic.server;
import dev.kelmagic.*;
import dev.kelmagic.audio.CassetteItem;
import dev.kelmagic.item.CodexItem;
import dev.kelmagic.core.KelLexicon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
/** Server-only impossible criteria are awarded from verified gameplay, never client claims. */
@EventBusSubscriber(modid=KelMagic.MODID)
public final class MagicProgress {
 public static void award(ServerPlayer p,String name){if(!name.equals("root"))award(p,"root");var a=p.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,name));if(a==null)return;var progress=p.getAdvancements().getOrStartProgress(a);if(!progress.isDone())for(String c:progress.getRemainingCriteria())p.getAdvancements().award(a,c);}
 public static void inventory(ServerPlayer p){
  for(var s:p.getInventory().items)check(p,s);check(p,p.getOffhandItem());
  if(p.getData(MagicAttachments.MANA).dragonUpgrade())award(p,"dragon_heart");
 }
 private static void check(ServerPlayer p,net.minecraft.world.item.ItemStack s){if(s.isEmpty())return;
  if(s.is(MagicContent.MANA_CORE))award(p,"root");
  if(s.is(MagicMaterials.GOLDEN_THREAD))award(p,"golden_thread");if(s.is(MagicMaterials.WOOL_ITEM))award(p,"golden_wool");
  if(s.is(MagicContent.MAGIC_CLOTH))award(p,"magic_cloth");
  if(s.is(MagicAudio.DECK_ITEM))award(p,"cassette_deck");if(s.is(MagicAudio.PLAYER))award(p,"cassette_player");
  if(CassetteItem.recorded(s)){award(p,"recorded_tape");if(s.has(net.minecraft.core.component.DataComponents.DYED_COLOR)||s.has(MagicComponents.TAPE_PATTERN.get()))award(p,"personal_tape");}
  if(s.is(MagicArchive.CODEX)&&s.getOrDefault(MagicComponents.CODEX_WORDS.get(),java.util.List.<String>of()).stream().filter(KelLexicon.WORDS::containsKey).distinct().count()==KelLexicon.WORDS.size())award(p,"complete_codex");
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p&&p.isAlive()&&p.tickCount%20==0)inventory(p);}
 @SubscribeEvent public static void craft(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent e){if(e.getEntity() instanceof ServerPlayer p)check(p,e.getCrafting());}
 private MagicProgress(){}
}
