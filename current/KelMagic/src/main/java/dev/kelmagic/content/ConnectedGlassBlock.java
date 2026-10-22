package dev.kelmagic.content;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
/** Six neighbour flags remove seams on all three axes. Internal faces are culled. */
public final class ConnectedGlassBlock extends TransparentBlock {
 public static final MapCodec<ConnectedGlassBlock> CODEC=simpleCodec(ConnectedGlassBlock::new);
 public static final BooleanProperty VIOLET=BooleanProperty.create("violet");
 public static final BooleanProperty[] JOIN={BooleanProperty.create("join_down"),BooleanProperty.create("join_up"),BooleanProperty.create("join_north"),BooleanProperty.create("join_south"),BooleanProperty.create("join_west"),BooleanProperty.create("join_east")};
 public ConnectedGlassBlock(Properties p){super(p);var s=stateDefinition.any().setValue(VIOLET,false);for(var flag:JOIN)s=s.setValue(flag,false);registerDefaultState(s);}
 @Override protected MapCodec<? extends TransparentBlock> codec(){return CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(JOIN).add(VIOLET);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){var s=defaultBlockState();for(var d:Direction.values())s=s.setValue(JOIN[d.ordinal()],c.getLevel().getBlockState(c.getClickedPos().relative(d)).is(this));return s;}
 @Override protected BlockState updateShape(BlockState s,Direction d,BlockState adjacent,LevelAccessor l,BlockPos p,BlockPos other){return s.setValue(JOIN[d.ordinal()],adjacent.is(this));}
 @Override protected boolean skipRendering(BlockState s,BlockState adjacent,Direction d){return adjacent.is(this)||super.skipRendering(s,adjacent,d);}
}
