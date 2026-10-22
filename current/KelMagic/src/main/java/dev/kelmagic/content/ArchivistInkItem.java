package dev.kelmagic.content;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import java.util.List;
/** A refillable-looking but finite 16-use bottle; colours and illuminates unwaxed signs. */
public final class ArchivistInkItem extends Item {
 public ArchivistInkItem(){super(new Properties().durability(16));}
 public static DyeColor color(ItemStack s){int c=s.getOrDefault(DataComponents.DYED_COLOR,new net.minecraft.world.item.component.DyedItemColor(DyeColor.CYAN.getTextureDiffuseColor(),false)).rgb();for(var d:DyeColor.values())if(d.getTextureDiffuseColor()==c)return d;return DyeColor.CYAN;}
 @Override public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level l,net.minecraft.world.entity.player.Player p,InteractionHand h){if(l.isClientSide)InkDrawings.OPEN.accept(h);return InteractionResultHolder.sidedSuccess(p.getItemInHand(h),l.isClientSide);}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()!=null&&c.getPlayer().isShiftKeyDown())return InkDrawings.draw(c);var p=c.getPlayer();if(p==null||!p.mayBuild()||!(c.getLevel().getBlockEntity(c.getClickedPos()) instanceof SignBlockEntity sign)||sign.isWaxed()||sign.getPlayerWhoMayEdit()!=null&&!p.getUUID().equals(sign.getPlayerWhoMayEdit()))return InteractionResult.PASS;
  boolean front=sign.isFacingFrontText(p);var text=sign.getText(front);var dye=color(c.getItemInHand());if(text.hasGlowingText()&&text.getColor()==dye)return InteractionResult.PASS;
  if(!c.getLevel().isClientSide){sign.updateText(t->t.setColor(dye).setHasGlowingText(true),front);c.getLevel().playSound(null,c.getClickedPos(),net.minecraft.sounds.SoundEvents.GLOW_INK_SAC_USE,net.minecraft.sounds.SoundSource.BLOCKS,1,1);c.getItemInHand().hurtAndBreak(1,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(c.getHand()));}return InteractionResult.sidedSuccess(c.getLevel().isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.translatable("kelmagic.ink_hint"));t.add(Component.literal("Рисунок: "+InkPatterns.label(InkPatterns.get(InkDrawings.selected(s)))));t.add(Component.translatable("kelmagic.ink_color",Component.translatable("color.minecraft."+color(s).getName())));}
}
