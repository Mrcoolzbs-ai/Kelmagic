package dev.kelmagic;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;
public final class MagicMaterials {
 public static final DeferredItem<Item> GOLDEN_THREAD=KelMagic.ITEMS.registerSimpleItem("golden_thread");
 public static final DeferredBlock<Block> GOLDEN_WOOL=KelMagic.BLOCKS.register("golden_wool",()->new Block(BlockBehaviour.Properties.of().strength(4f,12f).sound(SoundType.WOOL)));
 public static final DeferredItem<BlockItem> WOOL_ITEM=KelMagic.ITEMS.register("golden_wool",()->new WoolItem(GOLDEN_WOOL.get(),new Item.Properties().fireResistant()));
 public static final class WoolItem extends BlockItem {public WoolItem(Block b,Item.Properties p){super(b,p);}public boolean isPiglinCurrency(ItemStack s){return true;}}
 public static void init(){}
 private MagicMaterials(){}
}
