package dev.kelmagic.content;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
public final class ArchiveStorageBlock extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final BooleanProperty LEFT=BooleanProperty.create("join_left"),RIGHT=BooleanProperty.create("join_right"),UP=BooleanProperty.create("join_up"),DOWN=BooleanProperty.create("join_down");
 public final boolean manuscripts,stand;
 public ArchiveStorageBlock(Properties p,boolean manuscripts,boolean stand){super(p);this.manuscripts=manuscripts;this.stand=stand;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LEFT,false).setValue(RIGHT,false).setValue(UP,false).setValue(DOWN,false));}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new ArchiveStorageBlock(p,manuscripts,stand));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LEFT,RIGHT,UP,DOWN);}
 private BlockState joins(BlockState s,LevelAccessor l,BlockPos p){Direction f=s.getValue(FACING);Direction[] ds={f.getClockWise(),f.getCounterClockWise(),Direction.UP,Direction.DOWN};BooleanProperty[] ps={LEFT,RIGHT,UP,DOWN};for(int i=0;i<4;i++){var a=l.getBlockState(p.relative(ds[i]));s=s.setValue(ps[i],!stand&&a.is(this)&&a.getValue(FACING)==f);}return s;}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return joins(defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()),c.getLevel(),c.getClickedPos());}
 @Override protected BlockState updateShape(BlockState s,Direction d,BlockState a,LevelAccessor l,BlockPos p,BlockPos other){return joins(s,l,p);}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return stand?Shapes.or(box(1,0,1,15,2,15),box(5,2,5,11,12,11),box(1,12,1,15,14,15)):Shapes.block();}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArchiveStorageEntity(p,s);}
 @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos at,Player p,InteractionHand hand,BlockHitResult hit){if(!stand)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;if(!(l.getBlockEntity(at) instanceof ArchiveStorageEntity d)||!p.mayBuild())return ItemInteractionResult.FAIL;if(!l.isClientSide){var old=d.inventory.removeItem(0,1);if(old.isEmpty()&&!stack.isEmpty()){d.inventory.setItem(0,stack.copyWithCount(1));if(!p.getAbilities().instabuild)stack.shrink(1);}else if(!old.isEmpty()&&!p.getInventory().add(old))p.drop(old,false);}return ItemInteractionResult.sidedSuccess(l.isClientSide);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult hit){if(stand){if(!p.mayBuild())return InteractionResult.PASS;if(!l.isClientSide&&l.getBlockEntity(at) instanceof ArchiveStorageEntity d){var old=d.inventory.removeItem(0,1);if(!old.isEmpty()&&!p.getInventory().add(old))p.drop(old,false);}return InteractionResult.sidedSuccess(l.isClientSide);}if(l.getBlockEntity(at) instanceof ArchiveStorageEntity d){if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.openMenu(d,at);return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&l.getBlockEntity(p) instanceof ArchiveStorageEntity d){Containers.dropContents(l,p,d.inventory);l.updateNeighbourForOutputSignal(p,this);}super.onRemove(s,l,p,next,moving);}
 @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
 @Override protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p){return l.getBlockEntity(p) instanceof ArchiveStorageEntity d?net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(d.inventory):0;}
}
