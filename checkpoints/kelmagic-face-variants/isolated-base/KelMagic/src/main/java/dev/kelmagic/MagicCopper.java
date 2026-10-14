package dev.kelmagic;
import dev.kelmagic.core.GrandSpells;
import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.*;
/** Native WeatheringCopper blocks; transitions supplied by NeoForge oxidizables/waxables data maps. */
public final class MagicCopper {
 public static final List<String> WORDS=dev.kelmagic.core.KelLexicon.WORDS.keySet().stream().sorted().toList();
 public static final String[] STAGES={"","exposed_","weathered_","oxidized_"};
 public static final Map<String,DeferredBlock<? extends Block>> BLOCKS=new LinkedHashMap<>();
 public static final Map<String,DeferredItem<BlockItem>> ITEMS=new LinkedHashMap<>();
 static {var nativeBlocks=new Block[]{Blocks.COPPER_BLOCK,Blocks.EXPOSED_COPPER,Blocks.WEATHERED_COPPER,Blocks.OXIDIZED_COPPER};var nativeWaxed=new Block[]{Blocks.WAXED_COPPER_BLOCK,Blocks.WAXED_EXPOSED_COPPER,Blocks.WAXED_WEATHERED_COPPER,Blocks.WAXED_OXIDIZED_COPPER};for(String word:WORDS)for(int stage=0;stage<4;stage++)for(boolean waxed:new boolean[]{false,true}){final int age=stage;String id=id(word,stage,waxed);DeferredBlock<? extends Block> block=waxed?KelMagic.BLOCKS.register(id,()->new Block(BlockBehaviour.Properties.ofFullCopy(nativeWaxed[age]))):KelMagic.BLOCKS.register(id,()->new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.values()[age],BlockBehaviour.Properties.ofFullCopy(nativeBlocks[age])));BLOCKS.put(id,block);ITEMS.put(id,KelMagic.ITEMS.registerSimpleBlockItem(block));}}
 public static String id(String word,int stage,boolean waxed){return (waxed?"waxed_":"")+STAGES[stage]+"inscribed_copper_"+word;}
 public static Block block(String word,int stage,boolean waxed){return BLOCKS.get(id(word,stage,waxed)).get();}
 public static void init(){}
 private MagicCopper(){}
}
