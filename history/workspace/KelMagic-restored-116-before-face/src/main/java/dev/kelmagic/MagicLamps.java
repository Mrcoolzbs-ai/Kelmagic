package dev.kelmagic;
import net.neoforged.neoforge.registries.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.*;import net.minecraft.core.*;import net.minecraft.core.particles.ParticleTypes;import net.minecraft.world.item.*;
public final class MagicLamps {
 private static BlockBehaviour.Properties torchProps(){return BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s->14).noLootTable().sound(SoundType.WOOD);}
 public static final DeferredBlock<TorchBlock> TORCH=KelMagic.BLOCKS.register("magic_torch",()->new TorchBlock(ParticleTypes.END_ROD,torchProps()));
 public static final DeferredBlock<WallTorchBlock> WALL_TORCH=KelMagic.BLOCKS.register("magic_wall_torch",()->new WallTorchBlock(ParticleTypes.END_ROD,torchProps()));
 public static final DeferredBlock<Block> LANTERN=KelMagic.BLOCKS.register("magic_lantern",()->new Block(BlockBehaviour.Properties.of().strength(.5f).lightLevel(s->15).noLootTable().sound(SoundType.GLASS)));
 public static final DeferredItem<Item> TORCH_ITEM=KelMagic.ITEMS.register("magic_torch",()->new StandingAndWallBlockItem(TORCH.get(),WALL_TORCH.get(),new Item.Properties(),Direction.DOWN));
 public static final DeferredItem<BlockItem> LANTERN_ITEM=KelMagic.ITEMS.register("magic_lantern",()->new BlockItem(LANTERN.get(),new Item.Properties()));
 public static BlockState torchState(Direction face){return face.getAxis().isHorizontal()?WALL_TORCH.get().defaultBlockState().setValue(WallTorchBlock.FACING,face):TORCH.get().defaultBlockState();}
 public static void init(){}private MagicLamps(){}
}
