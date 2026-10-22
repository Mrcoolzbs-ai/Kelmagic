package dev.kelmagic.item;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
public final class LargeFieldHoeItem extends HoeItem {
 public LargeFieldHoeItem(Tier t,Properties p){super(t,p);}
 @Override public InteractionResult useOn(UseOnContext c){if(!(c.getPlayer() instanceof ServerPlayer p))return super.useOn(c);var l=p.serverLevel();var tool=c.getItemInHand();var center=c.getClickedPos();var state=l.getBlockState(center);if(!(state.getBlock() instanceof CropBlock)&&!(state.getBlock() instanceof NetherWartBlock))return super.useOn(c);boolean fertilizing=!mature(state);int changed=0;for(BlockPos q:BlockPos.betweenClosed(center.offset(-4,0,-4),center.offset(4,0,4))){var s=l.getBlockState(q);if(!(s.getBlock() instanceof CropBlock)&&!(s.getBlock() instanceof NetherWartBlock))continue;if(fertilizing){if(fertilize(l,q,s))changed++;}else if(harvest(p,l,q,s))changed++;}if(changed==0)return InteractionResult.PASS;tool.hurtAndBreak(fertilizing?changed:Math.max(1,changed/3),p,EquipmentSlot.MAINHAND);l.playSound(null,center,fertilizing?SoundEvents.BONE_MEAL_USE:SoundEvents.CROP_BREAK,SoundSource.BLOCKS,.8f,1);return InteractionResult.SUCCESS;}
 static boolean mature(BlockState s){if(s.getBlock() instanceof CropBlock c)return c.isMaxAge(s);return s.is(Blocks.NETHER_WART)&&s.getValue(NetherWartBlock.AGE)>=3;}
 static boolean fertilize(ServerLevel l,BlockPos p,BlockState s){if(s.getBlock() instanceof CropBlock c){if(c.isMaxAge(s))return false;c.growCrops(l,p,s);return true;}if(s.is(Blocks.NETHER_WART)){int a=s.getValue(NetherWartBlock.AGE);if(a>=3)return false;l.setBlockAndUpdate(p,s.setValue(NetherWartBlock.AGE,a+1));return true;}return false;}
 static Item seed(BlockState s){return s.is(Blocks.WHEAT)?Items.WHEAT_SEEDS:s.is(Blocks.CARROTS)?Items.CARROT:s.is(Blocks.POTATOES)?Items.POTATO:s.is(Blocks.BEETROOTS)?Items.BEETROOT_SEEDS:s.is(Blocks.TORCHFLOWER_CROP)?Items.TORCHFLOWER_SEEDS:s.is(Blocks.PITCHER_CROP)?Items.PITCHER_POD:s.is(Blocks.NETHER_WART)?Items.NETHER_WART:Items.AIR;}
 static boolean consumeSeed(ServerPlayer p,Item seed){if(seed==Items.AIR)return false;if(p.isCreative())return true;for(var s:p.getInventory().items)if(s.is(seed)){s.shrink(1);return true;}for(var s:p.getInventory().offhand)if(s.is(seed)){s.shrink(1);return true;}return false;}
 static boolean harvest(ServerPlayer p,ServerLevel l,BlockPos pos,BlockState s){if(!mature(s))return false;Item seed=seed(s);if(!p.isCreative()&&!p.getInventory().contains(new ItemStack(seed)))return false;if(!p.gameMode.destroyBlock(pos))return false;if(!consumeSeed(p,seed))return true;l.setBlockAndUpdate(pos,s.getBlock() instanceof CropBlock c?c.getStateForAge(0):Blocks.NETHER_WART.defaultBlockState());return true;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Зрелые культуры: сбор и пересадка 9×9 из запаса семян. Незрелые: удобрение 9×9."));}
}
