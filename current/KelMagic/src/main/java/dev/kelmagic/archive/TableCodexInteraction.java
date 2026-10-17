package dev.kelmagic.archive;
import dev.kelmagic.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
/** Sneak normally bypasses a block with an item: explicitly allow our placement gesture. */
@EventBusSubscriber(modid=KelMagic.MODID)
public final class TableCodexInteraction {
 @SubscribeEvent public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){
  if(e.getEntity().isShiftKeyDown()&&e.getLevel().getBlockEntity(e.getPos()) instanceof ArchiveTableEntity&&
   (e.getItemStack().isEmpty()||e.getItemStack().getItem() instanceof dev.kelmagic.item.CodexItem))
   e.setUseBlock(net.neoforged.neoforge.common.util.TriState.TRUE);
 }
 private TableCodexInteraction(){}
}
