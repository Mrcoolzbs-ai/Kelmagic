package dev.kelmagic.content;
import dev.kelmagic.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.*;
public final class MagicDreams {
 public static final DeferredRegister<Feature<?>> FEATURES=DeferredRegister.create(Registries.FEATURE,KelMagic.MODID);
 public static final DeferredHolder<Feature<?>,MidnightTreeFeature> TREE=FEATURES.register("midnight_tree",()->new MidnightTreeFeature());
 public static final DeferredHolder<Feature<?>,MidnightTreeFeature> NATURAL_TREE=FEATURES.register("natural_midnight_tree",()->new MidnightTreeFeature(true));
 public static final DeferredBlock<DreamBedBlock> BED=KelMagic.BLOCKS.register("otherworld_dream_bed",()->new DreamBedBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_BED).lightLevel(s->3)));
 public static final DeferredItem<net.minecraft.world.item.BlockItem> BED_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(BED,new net.minecraft.world.item.Item.Properties().stacksTo(1));
 public static final DeferredBlock<MagicGrassBlock> GRASS=KelMagic.BLOCKS.register("magic_grass_block",()->new MagicGrassBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE)));
 public static final DeferredItem<net.minecraft.world.item.BlockItem> GRASS_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(GRASS);
 static {MagicGrove.ITEMS.put("otherworld_dream_bed",BED_ITEM);MagicGrove.ITEMS.put("magic_grass_block",GRASS_ITEM);}
 public static void init(net.neoforged.bus.api.IEventBus bus){FEATURES.register(bus);}
 private MagicDreams(){}
}
