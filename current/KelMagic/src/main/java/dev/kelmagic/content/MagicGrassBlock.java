package dev.kelmagic.content;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.*;
/** Ordinary terrain block: animation is batched client-side, never one block entity per turf block. */
public final class MagicGrassBlock extends GrassBlock {
 public static final MapCodec<GrassBlock> CODEC=simpleCodec(MagicGrassBlock::new);
 public MagicGrassBlock(Properties p){super(p);}
 @Override public MapCodec<GrassBlock> codec(){return CODEC;}
 @Override public BlockState getToolModifiedState(BlockState state,UseOnContext c,ItemAbility action,boolean simulate){if(!c.getItemInHand().canPerformAction(action))return null;if(action==ItemAbilities.HOE_TILL&&c.getClickedFace()!=net.minecraft.core.Direction.DOWN&&c.getLevel().isEmptyBlock(c.getClickedPos().above()))return Blocks.FARMLAND.defaultBlockState();if(action==ItemAbilities.SHOVEL_FLATTEN&&c.getClickedFace()!=net.minecraft.core.Direction.DOWN&&c.getLevel().isEmptyBlock(c.getClickedPos().above()))return Blocks.DIRT_PATH.defaultBlockState();return super.getToolModifiedState(state,c,action,simulate);}
}
