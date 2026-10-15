package dev.kelmagic.audio;
import dev.kelmagic.*;
import dev.kelmagic.item.ScripterItem;
import dev.kelmagic.server.*;
import dev.kelmagic.core.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
public final class TapePlayerItem extends Item {
 public TapePlayerItem(Properties p){super(p);}
 public static int cost(List<String> w){return Math.max(1,(w.stream().mapToInt(String::length).sum()*3+9)/10);}
 public static List<ItemStack> tapes(ItemStack s){var out=new ArrayList<ItemStack>();var c=s.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY);for(int i=0;i<8;i++)out.add(i<c.getSlots()?c.getStackInSlot(i).copy():ItemStack.EMPTY);return out;}
 public static int active(ItemStack s){return Math.floorMod(s.getOrDefault(MagicComponents.ACTIVE_TAPE.get(),0),8);}
 public static void select(ServerPlayer p,int step){if(!p.isAlive()||p.isSpectator()||Math.abs(step)!=1)return;for(var h:InteractionHand.values()){var s=p.getItemInHand(h);if(s.getItem() instanceof TapePlayerItem){int i=Math.floorMod(active(s)+step,8);s.set(MagicComponents.ACTIVE_TAPE.get(),i);p.displayClientMessage(Component.translatable("kelmagic.tape.selected",i+1),true);p.inventoryMenu.broadcastChanges();return;}}}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player player,InteractionHand hand){var s=player.getItemInHand(hand);if(player instanceof ServerPlayer p){if(p.isSpectator())return InteractionResultHolder.fail(s);if(p.isShiftKeyDown()){p.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,owner)->new TapeMenu(id,inv,hand),Component.translatable("menu.kelmagic.cassette_player")),b->b.writeBoolean(hand==InteractionHand.OFF_HAND));}else{var tape=tapes(s).get(active(s));if(!CassetteItem.recorded(tape)||p.getData(MagicAttachments.SESSION).active()){p.displayClientMessage(Component.translatable("kelmagic.tape.unavailable"),true);return InteractionResultHolder.fail(s);}var words=CassetteItem.words(tape);var mana=p.getData(MagicAttachments.MANA);var spend=ManaMath.spend(Math.min(MagicServer.maximum(p),mana.current()),cost(words));p.setData(MagicAttachments.MANA,mana.withCurrent(spend.remaining()));Playback.run(p,"tape",()->{MagicServer.hurtMana(p,spend.healthDamage());if(p.isAlive()){var session=p.getData(MagicAttachments.SESSION);words.forEach(session::append);MagicServer.actionFromScripter(p,new dev.kelmagic.network.SpellAction(session.generation(),session.revision(),false));MagicProgress.award(p,"play_tape");}});MagicServer.sync(p);p.serverLevel().playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.NOTE_BLOCK_HAT.value(),net.minecraft.sounds.SoundSource.PLAYERS,.35f,.8f);}}return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext ctx,List<Component> tips,TooltipFlag flag){tips.add(Component.translatable("kelmagic.tape.controls"));int i=active(s);var t=tapes(s).get(i);tips.add(Component.translatable("kelmagic.tape.selected",i+1));if(CassetteItem.recorded(t)){tips.add(Component.literal(String.join(" ",CassetteItem.words(t))));tips.add(Component.translatable("kelmagic.tape.cost",cost(CassetteItem.words(t))));}}
}
