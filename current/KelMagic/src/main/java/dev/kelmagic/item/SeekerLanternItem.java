package dev.kelmagic.item;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class SeekerLanternItem extends Item {
 public static final int COOLDOWN=300,REVEAL_TICKS=40;
 public SeekerLanternItem(Properties p){super(p);}
 @Override public InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player player,InteractionHand hand){var s=player.getItemInHand(hand);if(!(player instanceof ServerPlayer p))return InteractionResultHolder.sidedSuccess(s,l.isClientSide);if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);p.getCooldowns().addCooldown(this,COOLDOWN);p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.PLAYERS,.9f,1.55f);p.displayClientMessage(Component.literal("Фонарь на миг отмечает скрытые сундуки."),true);return InteractionResultHolder.sidedSuccess(s,false);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Показывает хранилища сквозь блоки в радиусе 50. Перезарядка: 15 секунд."));}
}
