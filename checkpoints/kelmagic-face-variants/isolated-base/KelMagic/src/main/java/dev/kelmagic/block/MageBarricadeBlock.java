package dev.kelmagic.block;
import net.minecraft.world.level.block.Block;import net.minecraft.world.level.block.state.*;import net.minecraft.core.BlockPos;import net.minecraft.server.level.ServerLevel;import net.minecraft.util.RandomSource;
/** Unlootable conjured cover; scheduled expiration persists through chunk save/reload. */
public final class MageBarricadeBlock extends Block {
 public MageBarricadeBlock(BlockBehaviour.Properties p){super(p);}
 @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(l.getBlockState(p).is(this))l.removeBlock(p,false);}
}
