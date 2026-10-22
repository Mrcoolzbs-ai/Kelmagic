package dev.kelmagic.item;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
public final class ReturnStoneItem extends Item {
 public ReturnStoneItem(Properties p){super(p);}
 @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(!(player instanceof ServerPlayer p))return InteractionResultHolder.sidedSuccess(stack,l.isClientSide);var target=p.findRespawnPositionAndUseSpawnBlock(false,DimensionTransition.DO_NOTHING);if(target==null||target.missingRespawnBlock()){p.displayClientMessage(Component.literal("Точка возрождения не найдена."),true);return InteractionResultHolder.fail(stack);}p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.AMETHYST_CLUSTER_BREAK,SoundSource.PLAYERS,.8f,.8f);if(target.newLevel()==p.serverLevel())p.teleportTo(target.pos().x,target.pos().y,target.pos().z);else p.changeDimension(target);p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.PLAYERS,.7f,1.2f);stack.shrink(1);return InteractionResultHolder.sidedSuccess(stack,false);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Одноразовый: возвращает к точке возрождения и рассыпается."));}
}
