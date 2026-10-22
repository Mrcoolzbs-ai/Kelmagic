package dev.kelmagic.item;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
public final class GardenerSickleItem extends SwordItem {
 private static final Set<UUID> LEAVES=new HashSet<>();public GardenerSickleItem(Tier t,Properties p){super(t,p);}public static boolean leafMode(net.minecraft.world.entity.player.Player p){return LEAVES.contains(p.getUUID());}
 @Override public InteractionResult useOn(UseOnContext c){if(!(c.getPlayer() instanceof ServerPlayer p))return InteractionResult.PASS;if(p.isShiftKeyDown()){if(!LEAVES.add(p.getUUID()))LEAVES.remove(p.getUUID());p.displayClientMessage(Component.literal(leafMode(p)?"Серп: режим листвы":"Серп: режим растений"),true);return InteractionResult.SUCCESS;}var l=p.serverLevel();var center=c.getClickedPos();boolean leaf=leafMode(p);int broken=0;for(BlockPos q:BlockPos.betweenClosed(center.offset(leaf?-2:-1,-1,leaf?-2:-1),center.offset(leaf?2:1,1,leaf?2:1))){var s=l.getBlockState(q);boolean match=leaf?(s.is(BlockTags.LEAVES)||s.is(Blocks.VINE)):(s.is(BlockTags.CROPS)||s.is(BlockTags.FLOWERS)||s.is(BlockTags.REPLACEABLE_BY_TREES)||s.is(Blocks.SUGAR_CANE)||s.is(Blocks.NETHER_WART));if(match&&!s.isAir()&&s.getDestroySpeed(l,q)>=0&&p.gameMode.destroyBlock(q))broken++;}if(broken==0)return InteractionResult.PASS;c.getItemInHand().hurtAndBreak(Math.max(1,broken/3),p,EquipmentSlot.MAINHAND);return InteractionResult.SUCCESS;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("ПКМ — растения рядом. Shift+ПКМ — отдельный режим листвы."));}
}
