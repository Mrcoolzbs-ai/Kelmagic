package dev.kelmagic.archive;
import dev.kelmagic.*;import com.mojang.serialization.MapCodec;import net.minecraft.core.BlockPos;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.phys.*;
public final class ArchiveTableBlock extends BaseEntityBlock {
 public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 public static final MapCodec<ArchiveTableBlock> CODEC=simpleCodec(ArchiveTableBlock::new);public ArchiveTableBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 private static final java.util.Map<net.minecraft.core.Direction,net.minecraft.world.phys.shapes.VoxelShape> SHAPES=new java.util.EnumMap<>(net.minecraft.core.Direction.class);
 static {double[][] boxes={{0,10,0,16,12,16},{1,0,1,3,10,3},{13,0,1,15,10,3},{1,0,13,3,10,15},{13,0,13,15,10,15},{13,12,3,15,16,5}};
  for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL){var result=net.minecraft.world.phys.shapes.Shapes.empty();for(var box:boxes){double[] min={1,1,1},max={0,0,0};for(int i=0;i<8;i++){var v=TableOrientation.rotate(d,new Vec3(box[(i&1)==0?0:3]/16-.5,box[(i&2)==0?1:4]/16-.5,box[(i&4)==0?2:5]/16-.5)).add(.5,.5,.5);min[0]=Math.min(min[0],v.x);min[1]=Math.min(min[1],v.y);min[2]=Math.min(min[2],v.z);max[0]=Math.max(max[0],v.x);max[1]=Math.max(max[1],v.y);max[2]=Math.max(max[2],v.z);}result=net.minecraft.world.phys.shapes.Shapes.or(result,net.minecraft.world.phys.shapes.Shapes.box(min[0],min[1],min[2],max[0],max[1],max[2]));}SHAPES.put(d,result);}
 }
 protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return SHAPES.get(s.getValue(FACING));}

 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArchiveTableEntity(p,s);}
 protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){
  if(l.getBlockEntity(pos) instanceof ArchiveTableEntity d){
   if(p.isShiftKeyDown()&&stack.getItem() instanceof dev.kelmagic.item.CodexItem){if(!d.hasCodex()){if(!l.isClientSide){d.mount(stack);stack.shrink(1);}return net.minecraft.world.ItemInteractionResult.sidedSuccess(l.isClientSide);}return net.minecraft.world.ItemInteractionResult.FAIL;}
   if(p.isShiftKeyDown()&&stack.isEmpty()&&d.hasCodex()){if(!l.isClientSide){var book=d.unmount();if(!p.getInventory().add(book))p.drop(book,false);}return net.minecraft.world.ItemInteractionResult.sidedSuccess(l.isClientSide);}
   // The physical page consumes its own clicks client-side; the table remains usable.

  }return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
 }
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){
  if(!(l.getBlockEntity(pos) instanceof ArchiveTableEntity d))return InteractionResult.PASS;
  if(p.isShiftKeyDown()&&p.getMainHandItem().isEmpty()&&d.hasCodex()){
   if(!l.isClientSide){var book=d.unmount();if(!p.getInventory().add(book))p.drop(book,false);}
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
  if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.openMenu(d,b->b.writeBlockPos(pos));
  return InteractionResult.sidedSuccess(l.isClientSide);
 }
 protected void onRemove(BlockState old,Level l,BlockPos pos,BlockState next,boolean moving){if(!l.isClientSide&&!old.is(next.getBlock())&&l.getBlockEntity(pos) instanceof ArchiveTableEntity d){var inputs=new SimpleContainer(4);for(int i=0;i<4;i++)inputs.setItem(i,d.inventory.getItem(i));Containers.dropContents(l,pos,inputs);if(d.hasCodex())Containers.dropItemStack(l,pos.getX()+.5,pos.getY()+.8,pos.getZ()+.5,d.unmount());}super.onRemove(old,l,pos,next,moving);}
}