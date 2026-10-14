package dev.kelmagic.audio;
import com.mojang.serialization.MapCodec;
import dev.kelmagic.MagicAudio;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class DeckBlock extends BaseEntityBlock {
 public static final MapCodec<DeckBlock> CODEC=simpleCodec(DeckBlock::new);
 public DeckBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,net.minecraft.core.Direction.NORTH));}
 @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){b.add(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,c.getHorizontalDirection().getOpposite());}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(0,0,0,16,12,16);}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DeckEntity(p,s);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){if(l.getBlockEntity(pos) instanceof DeckEntity d){if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.openMenu(d,b->b.writeBlockPos(pos));return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 @Override protected void onRemove(BlockState old,Level l,BlockPos pos,BlockState next,boolean moving){if(!old.is(next.getBlock()) && l.getBlockEntity(pos) instanceof DeckEntity d)Containers.dropContents(l,pos,d.inventory);super.onRemove(old,l,pos,next,moving);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,MagicAudio.DECK_ENTITY.get(),DeckEntity::tick);}
}
