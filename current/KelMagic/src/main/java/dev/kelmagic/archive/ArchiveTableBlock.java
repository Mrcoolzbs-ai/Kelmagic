package dev.kelmagic.archive;
import dev.kelmagic.*;import com.mojang.serialization.MapCodec;import net.minecraft.core.BlockPos;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.phys.*;
public final class ArchiveTableBlock extends BaseEntityBlock {
 public static final net.minecraft.world.level.block.state.properties.BooleanProperty VIOLET=net.minecraft.world.level.block.state.properties.BooleanProperty.create("violet");
 public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 public static final MapCodec<ArchiveTableBlock> CODEC=simpleCodec(ArchiveTableBlock::new);public ArchiveTableBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(VIOLET,false).setValue(FACING,net.minecraft.core.Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,VIOLET);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
 protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 private static final java.util.Map<net.minecraft.core.Direction,net.minecraft.world.phys.shapes.VoxelShape> SHAPES=new java.util.EnumMap<>(net.minecraft.core.Direction.class);
 static {var boxes=new java.util.ArrayList<double[]>();
  boxes.add(new double[]{0.250000000,0.000000000,0.250000000,15.750000000,0.800000000,15.750000000});
  boxes.add(new double[]{1.500000000,0.800000000,2.000000000,14.500000000,1.500000000,14.000000000});
  boxes.add(new double[]{0.850000000,1.500000000,2.000000000,15.150000000,14.269367614,2.600000000});
  boxes.add(new double[]{0.850000000,1.500000000,2.600000000,15.150000000,14.020839477,3.200000000});
  boxes.add(new double[]{0.850000000,1.500000000,3.200000000,15.150000000,13.772311339,3.800000000});
  boxes.add(new double[]{0.850000000,1.500000000,3.800000000,15.150000000,13.523783202,4.400000000});
  boxes.add(new double[]{0.850000000,1.500000000,4.400000000,15.150000000,13.275255064,5.000000000});
  boxes.add(new double[]{0.850000000,1.500000000,5.000000000,15.150000000,13.026726927,5.600000000});
  boxes.add(new double[]{0.850000000,1.500000000,5.600000000,15.150000000,12.778198789,6.200000000});
  boxes.add(new double[]{0.850000000,1.500000000,6.200000000,15.150000000,12.529670652,6.800000000});
  boxes.add(new double[]{0.850000000,1.500000000,6.800000000,15.150000000,12.281142515,7.400000000});
  boxes.add(new double[]{0.850000000,1.500000000,7.400000000,15.150000000,12.032614377,8.000000000});
  boxes.add(new double[]{0.850000000,1.500000000,8.000000000,15.150000000,11.784086240,8.600000000});
  boxes.add(new double[]{0.850000000,1.500000000,8.600000000,15.150000000,11.535558102,9.200000000});
  boxes.add(new double[]{0.850000000,1.500000000,9.200000000,15.150000000,11.287029965,9.800000000});
  boxes.add(new double[]{0.850000000,1.500000000,9.800000000,15.150000000,11.038501827,10.400000000});
  boxes.add(new double[]{0.850000000,1.500000000,10.400000000,15.150000000,10.789973690,11.000000000});
  boxes.add(new double[]{0.850000000,1.500000000,11.000000000,15.150000000,10.541445553,11.600000000});
  boxes.add(new double[]{0.850000000,1.500000000,11.600000000,15.150000000,10.292917415,12.200000000});
  boxes.add(new double[]{0.850000000,1.500000000,12.200000000,15.150000000,10.044389278,12.800000000});
  boxes.add(new double[]{0.850000000,1.500000000,12.800000000,15.150000000,9.795861140,13.400000000});
  boxes.add(new double[]{0.850000000,1.500000000,13.400000000,15.150000000,9.547333003,14.000000000});
  boxes.add(new double[]{0.850000000,1.500000000,0.900000000,15.150000000,16.000000000,2.000000000});
  boxes.add(new double[]{0.500000000,9.082112347,1.226696527,15.500000000,15.178784027,14.467156728});
  boxes.add(new double[]{0.500000000,9.821215973,13.866635031,15.500000000,10.578093947,14.677632615});
  boxes.add(new double[]{0.500000000,16.000000000,0.500000000,15.500000000,16.700000000,2.250000000});
  boxes.add(new double[]{2.300000000,16.700000000,0.650000000,4.200000000,17.000000000,2.100000000});
  boxes.add(new double[]{2.500000000,17.000000000,0.800000000,4.000000000,18.250000000,2.000000000});
  boxes.add(new double[]{2.800000000,18.250000000,1.050000000,3.700000000,18.400000000,1.800000000});
  boxes.add(new double[]{5.700000000,16.700000000,0.800000000,6.500000000,17.150000000,1.600000000});
  boxes.add(new double[]{4.724965967,17.069385325,1.120000000,6.173910363,20.271805085,1.280000000});
  boxes.add(new double[]{5.081968746,17.709535188,1.120000000,5.681674548,18.233473536,1.280000000});
  boxes.add(new double[]{4.928895373,18.079087001,1.120000000,5.528601175,18.603025349,1.280000000});
  boxes.add(new double[]{4.775822000,18.448638814,1.120000000,5.375527802,18.972577162,1.280000000});
  boxes.add(new double[]{4.761330557,18.875593142,1.120000000,5.222454429,19.342128975,1.280000000});
  boxes.add(new double[]{4.783794296,19.317854808,1.120000000,5.069381056,19.711680788,1.280000000});
  boxes.add(new double[]{12.800000000,16.700000000,0.650000000,14.800000000,17.050000000,2.150000000});
  boxes.add(new double[]{13.650000000,17.050000000,1.250000000,13.950000000,18.250000000,1.550000000});
  boxes.add(new double[]{13.150000000,18.250000000,0.850000000,14.450000000,20.100000000,1.950000000});
  boxes.add(new double[]{13.000000000,20.100000000,0.700000000,14.600000000,20.400000000,2.100000000});
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