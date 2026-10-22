package dev.kelmagic.content;
import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.*;

/** No vanilla dimension explosion path; keeps vanilla two-part bed placement and sleeping pose. */
public final class DreamBedBlock extends BedBlock {
 public static final MapCodec<DreamBedBlock> CODEC=simpleCodec(DreamBedBlock::new);
 public DreamBedBlock(Properties p){super(DyeColor.YELLOW,p);}
 @Override public MapCodec<BedBlock> codec(){return (MapCodec)CODEC;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return null;}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public static BlockPos head(BlockState s,BlockPos p){return s.getValue(PART)==BedPart.HEAD?p:p.relative(s.getValue(FACING));}
 public static boolean intact(LevelReader l,BlockPos head){var s=l.getBlockState(head);if(!(s.getBlock() instanceof DreamBedBlock)||s.getValue(PART)!=BedPart.HEAD)return false;var foot=l.getBlockState(head.relative(s.getValue(FACING).getOpposite()));return foot.getBlock()==s.getBlock()&&foot.getValue(PART)==BedPart.FOOT&&foot.getValue(FACING)==s.getValue(FACING);}
 @Override public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState s,EntityType<?> type,LevelReader l,BlockPos pos,float angle){var head=head(s,pos);if(!intact(l,head))return Optional.empty();return findStandUpPosition(type,l,head,s.getValue(FACING),angle).map(v->ServerPlayer.RespawnPosAngle.of(v,head));}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){
  if(l.isClientSide)return InteractionResult.SUCCESS;var head=head(s,pos);if(!intact(l,head))return InteractionResult.CONSUME;s=l.getBlockState(head);
  if(s.getValue(OCCUPIED)){player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"),true);return InteractionResult.CONSUME;}
  var p=(ServerPlayer)player;var result=p.startSleepInBed(head);result.ifRight(u->p.setRespawnPosition(l.dimension(),head,p.getYRot(),false,true));result.ifLeft(problem->{if(problem.getMessage()!=null)p.displayClientMessage(problem.getMessage(),true);});return InteractionResult.CONSUME;
 }
}
