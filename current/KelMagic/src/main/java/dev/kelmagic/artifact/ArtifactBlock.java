package dev.kelmagic.artifact;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
public final class ArtifactBlock extends BaseEntityBlock {
    public enum Kind { BOWL,ACCESS,WARD,CRYSTAL,STRIKER,NIGHT,GROVE,MUTE,BRIDGE,ROOT_WALL }
    public static final BooleanProperty POWERED=BlockStateProperties.POWERED;
    public static final IntegerProperty FILL=IntegerProperty.create("fill",0,4);
    public final Kind kind;public final int capacity;
    public ArtifactBlock(Kind kind,int capacity,Properties props){super(props);this.kind=kind;this.capacity=capacity;registerDefaultState(stateDefinition.any().setValue(POWERED,false).setValue(FILL,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new ArtifactBlock(kind,capacity,p));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(POWERED,FILL);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArtifactBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide?null:createTickerHelper(type,ArtifactContent.ENTITY.get(),ArtifactBlockEntity::tick);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(kind){case BOWL->Shapes.or(box(1,0,1,15,3,15),box(1,3,1,3,10,15),box(13,3,1,15,10,15),box(3,3,1,13,10,3),box(3,3,13,13,10,15),box(6.5,2,6.5,9.5,16,9.5));case CRYSTAL->box(4,0,4,12,15,12);case STRIKER->Shapes.or(box(2,0,2,14,3,14),box(6,2,6,10,12,10),box(3,10,5,13,14,11));case WARD->box(0,0,0,16,16,16);case BRIDGE->box(0,12,0,16,16,16);case ROOT_WALL->box(1,0,1,15,22,15);default->box(3,0,3,13,16,13);};}
    @Override public void setPlacedBy(Level l,BlockPos pos,BlockState s,LivingEntity entity,ItemStack stack){super.setPlacedBy(l,pos,s,entity,stack);if(entity instanceof Player p&&l.getBlockEntity(pos) instanceof ArtifactBlockEntity be){be.owner=p.getUUID();be.changed();}}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(player instanceof net.minecraft.server.level.ServerPlayer sp&&l.getBlockEntity(p) instanceof ArtifactBlockEntity be)be.interact(sp);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected ItemInteractionResult useItemOn(ItemStack item,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(item.getItem() instanceof BlockItem)return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;if(player instanceof net.minecraft.server.level.ServerPlayer sp&&l.getBlockEntity(p) instanceof ArtifactBlockEntity be)be.interact(sp);return ItemInteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block from,BlockPos fromPos,boolean moving){if(l.isClientSide||kind!=Kind.STRIKER)return;boolean powered=l.hasNeighborSignal(p);if(powered!=s.getValue(POWERED)){l.setBlock(p,s.setValue(POWERED,powered),2);if(powered&&l.getBlockEntity(p.below()) instanceof ArtifactBlockEntity crystal)crystal.strike();}}
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,net.minecraft.util.RandomSource random){if((kind==Kind.BRIDGE||kind==Kind.ROOT_WALL)&&l.getBlockEntity(p) instanceof ArtifactBlockEntity be&&l.getGameTime()>=be.expires)l.removeBlock(p,false);}
    @Override public void animateTick(BlockState s,Level l,BlockPos pos,net.minecraft.util.RandomSource random){if(s.getValue(FILL)>0||kind==Kind.CRYSTAL||kind==Kind.BRIDGE){if(random.nextInt(5)==0)l.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,pos.getX()+.2+random.nextDouble()*.6,pos.getY()+.6+random.nextDouble()*.5,pos.getZ()+.2+random.nextDouble()*.6,0,.015,0);}}
}
