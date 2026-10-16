package dev.kelmagic;
import dev.kelmagic.resonance.*;import net.minecraft.world.item.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.BlockBehaviour;import net.minecraft.world.inventory.*;import net.neoforged.neoforge.registries.*;
public final class MagicResonance {
 public static final DeferredBlock<Block> MANA=KelMagic.BLOCKS.register("mana_block",()->new Block(BlockBehaviour.Properties.of().strength(4,8).sound(SoundType.AMETHYST).lightLevel(s->8)));
 public static final DeferredItem<BlockItem> MANA_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(MANA);
 public static final DeferredBlock<ResonanceBlock> STATION=KelMagic.BLOCKS.register("resonance_station",()->new ResonanceBlock(BlockBehaviour.Properties.of().strength(4,10).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredItem<BlockItem> STATION_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(STATION);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ResonanceEntity>> ENTITY=MagicAudio.ENTITIES.register("resonance_station",()->BlockEntityType.Builder.of(ResonanceEntity::new,STATION.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<ResonanceMenu>> MENU=MagicAudio.MENUS.register("resonance_station",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inv,b)->new ResonanceMenu(id,inv,b.readBlockPos())));
 public static void init(){}private MagicResonance(){}
}
