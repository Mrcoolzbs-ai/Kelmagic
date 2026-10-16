package dev.kelmagic.resonance;
import dev.kelmagic.*;import com.mojang.serialization.MapCodec;import net.minecraft.core.BlockPos;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.phys.*;
public final class ResonanceBlock extends BaseEntityBlock {
 public static final MapCodec<ResonanceBlock> CODEC=simpleCodec(ResonanceBlock::new);public ResonanceBlock(Properties p){super(p);}
 protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.box(0,0,0,16,14,16);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ResonanceEntity(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,MagicResonance.ENTITY.get(),ResonanceEntity::tick);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){if(l.getBlockEntity(pos) instanceof ResonanceEntity d){if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.openMenu(d,b->b.writeBlockPos(pos));return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 protected void onRemove(BlockState old,Level l,BlockPos pos,BlockState next,boolean moving){if(!old.is(next.getBlock())&&l.getBlockEntity(pos) instanceof ResonanceEntity d){Containers.dropContents(l,pos,d.inventory);l.updateNeighbourForOutputSignal(pos,this);}super.onRemove(old,l,pos,next,moving);}
}
