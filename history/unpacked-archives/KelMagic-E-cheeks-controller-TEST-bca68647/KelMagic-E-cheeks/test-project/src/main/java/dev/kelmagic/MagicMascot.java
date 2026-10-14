package dev.kelmagic;
import dev.kelmagic.block.MageThroneBlock;import net.minecraft.world.item.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.BlockBehaviour;import net.neoforged.neoforge.registries.*;
public final class MagicMascot {
 public static final DeferredBlock<MageThroneBlock> THRONE=KelMagic.BLOCKS.register("mage_throne",()->new MageThroneBlock(BlockBehaviour.Properties.of().strength(3,8).sound(SoundType.WOOD).noOcclusion()));
 public static final DeferredItem<BlockItem> THRONE_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(THRONE);
 public static final net.neoforged.neoforge.registries.DeferredBlock<dev.kelmagic.block.MageBarricadeBlock> BARRICADE=KelMagic.BLOCKS.register("mage_barricade",()->new dev.kelmagic.block.MageBarricadeBlock(BlockBehaviour.Properties.of().strength(2).noLootTable().sound(SoundType.STONE)));
 public static void init(){}private MagicMascot(){}
}
