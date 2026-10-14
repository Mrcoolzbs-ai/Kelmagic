package dev.kelmagic.item;
import dev.kelmagic.server.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class DragonHeartItem extends Item {
    public DragonHeartItem(Properties props){super(props);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p) {
            var mana=p.getData(MagicAttachments.MANA);
            if(mana.dragonUpgrade()) {p.displayClientMessage(Component.translatable("kelmagic.heart_already"),true);return InteractionResultHolder.fail(stack);}
            p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(mana.current(),true));
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new dev.kelmagic.network.HeartActivation(p.getId()));
            stack.consume(1,p);MagicServer.sync(p);p.displayClientMessage(Component.translatable("kelmagic.heart_used"),false);
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}
