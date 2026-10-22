package dev.kelmagic.content;

import dev.kelmagic.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.registries.*;

/** Infusion-grown, sterile dark-blue wood. No overworld generation and no replacement of vanilla trees. */
public final class MagicGrove {
 public static final Map<String,DeferredItem<? extends Item>> ITEMS=new LinkedHashMap<>();
 public static final WoodType WOOD=WoodType.register(new WoodType("kelmagic:midnight",BlockSetType.OAK));
 public static final ResourceKey<ConfiguredFeature<?,?>> TREE=ResourceKey.create(Registries.CONFIGURED_FEATURE,ResourceLocation.fromNamespaceAndPath("kelmagic","midnight_tree"));
 private static BlockBehaviour.Properties copy(Block b){return BlockBehaviour.Properties.ofFullCopy(b);}
 private static <T extends Block> DeferredBlock<T> block(String name,java.util.function.Supplier<T> make){var b=KelMagic.BLOCKS.register(name,make);ITEMS.put(name,KelMagic.ITEMS.registerSimpleBlockItem(b));return b;}
 public static final DeferredBlock<RotatedPillarBlock> LOG=block("midnight_log",()->new RotatedPillarBlock(copy(Blocks.OAK_LOG)));
 public static final DeferredBlock<RotatedPillarBlock> STRIPPED_LOG=block("stripped_midnight_log",()->new RotatedPillarBlock(copy(Blocks.STRIPPED_OAK_LOG)));
 public static final DeferredBlock<RotatedPillarBlock> WOOD_BLOCK=block("midnight_wood",()->new RotatedPillarBlock(copy(Blocks.OAK_WOOD)));
 public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WOOD=block("stripped_midnight_wood",()->new RotatedPillarBlock(copy(Blocks.STRIPPED_OAK_WOOD)));
 public static final DeferredBlock<Block> PLANKS=block("midnight_planks",()->new Block(copy(Blocks.OAK_PLANKS)));
 public static final DeferredBlock<StairBlock> STAIRS=block("midnight_stairs",()->new StairBlock(PLANKS.get().defaultBlockState(),copy(Blocks.OAK_STAIRS)));
 public static final DeferredBlock<SlabBlock> SLAB=block("midnight_slab",()->new SlabBlock(copy(Blocks.OAK_SLAB)));
 public static final DeferredBlock<FenceBlock> FENCE=block("midnight_fence",()->new FenceBlock(copy(Blocks.OAK_FENCE)));
 public static final DeferredBlock<FenceGateBlock> GATE=block("midnight_fence_gate",()->new FenceGateBlock(WOOD,copy(Blocks.OAK_FENCE_GATE)));
 public static final DeferredBlock<DoorBlock> DOOR=block("midnight_door",()->new DoorBlock(BlockSetType.OAK,copy(Blocks.OAK_DOOR)));
 public static final DeferredBlock<TrapDoorBlock> TRAPDOOR=block("midnight_trapdoor",()->new TrapDoorBlock(BlockSetType.OAK,copy(Blocks.OAK_TRAPDOOR)));
 public static final DeferredBlock<PressurePlateBlock> PLATE=block("midnight_pressure_plate",()->new PressurePlateBlock(BlockSetType.OAK,copy(Blocks.OAK_PRESSURE_PLATE)));
 public static final DeferredBlock<ButtonBlock> BUTTON=block("midnight_button",()->new ButtonBlock(BlockSetType.OAK,30,copy(Blocks.OAK_BUTTON)));
 public static final DeferredBlock<LeavesBlock> LEAVES=block("midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES)));
 public static final DeferredBlock<LeavesBlock> GOLDEN_LEAVES=block("golden_midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES).lightLevel(s->9)));
 public static final DeferredBlock<LeavesBlock> NATURAL_LEAVES=block("natural_midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES)));
 public static final DeferredBlock<LeavesBlock> NATURAL_GOLDEN_LEAVES=block("natural_golden_midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES).lightLevel(s->9)));
 public static final DeferredBlock<SaplingBlock> SAPLING=block("magic_sapling",()->new SaplingBlock(new TreeGrower("kelmagic:midnight",Optional.empty(),Optional.of(TREE),Optional.empty()),copy(Blocks.OAK_SAPLING)));
 public static final DeferredBlock<StandingSignBlock> SIGN=KelMagic.BLOCKS.register("midnight_sign",()->new StandingSignBlock(WOOD,copy(Blocks.OAK_SIGN)){
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GroveSign(p,s);}});
 public static final DeferredBlock<WallSignBlock> WALL_SIGN=KelMagic.BLOCKS.register("midnight_wall_sign",()->new WallSignBlock(WOOD,copy(Blocks.OAK_WALL_SIGN).dropsLike(SIGN.get())){
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GroveSign(p,s);}});
 public static final DeferredItem<SignItem> SIGN_ITEM=KelMagic.ITEMS.register("midnight_sign",()->new SignItem(new Item.Properties().stacksTo(16),SIGN.get(),WALL_SIGN.get()));
 public static final DeferredBlock<CeilingHangingSignBlock> HANGING=KelMagic.BLOCKS.register("midnight_hanging_sign",()->new CeilingHangingSignBlock(WOOD,copy(Blocks.OAK_HANGING_SIGN)){
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GroveHangingSign(p,s);}});
 public static final DeferredBlock<WallHangingSignBlock> WALL_HANGING=KelMagic.BLOCKS.register("midnight_wall_hanging_sign",()->new WallHangingSignBlock(WOOD,copy(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(HANGING.get())){
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GroveHangingSign(p,s);}});
 public static final DeferredItem<HangingSignItem> HANGING_ITEM=KelMagic.ITEMS.register("midnight_hanging_sign",()->new HangingSignItem(HANGING.get(),WALL_HANGING.get(),new Item.Properties().stacksTo(16)));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<GroveSign>> SIGN_ENTITY=MagicAudio.ENTITIES.register("midnight_sign",()->BlockEntityType.Builder.of(GroveSign::new,SIGN.get(),WALL_SIGN.get()).build(null));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<GroveHangingSign>> HANGING_ENTITY=MagicAudio.ENTITIES.register("midnight_hanging_sign",()->BlockEntityType.Builder.of(GroveHangingSign::new,HANGING.get(),WALL_HANGING.get()).build(null));
 public static final class GroveSign extends SignBlockEntity {public GroveSign(BlockPos p,BlockState s){super(SIGN_ENTITY.get(),p,s);}}
 public static final class GroveHangingSign extends SignBlockEntity {public GroveHangingSign(BlockPos p,BlockState s){super(HANGING_ENTITY.get(),p,s);}@Override public int getTextLineHeight(){return 9;}@Override public int getMaxTextLineWidth(){return 60;}}
 public static final DeferredItem<Item> FIBER=KelMagic.ITEMS.registerSimpleItem("living_fiber");
 public static final DeferredBlock<ConnectedGlassBlock> GLASS=block("resonance_glass",()->new ConnectedGlassBlock(copy(Blocks.GLASS).strength(.6f).noOcclusion()));
 static {ITEMS.put("midnight_sign",SIGN_ITEM);ITEMS.put("midnight_hanging_sign",HANGING_ITEM);ITEMS.put("living_fiber",FIBER);}
 public static void init(){}
 public static String research(String key){if(key.equals("otherworld_dream_bed")||key.equals("magic_grass_block"))return "midnight_wood";if(key.equals("manuscript_cabinet"))return "archivist_cabinet";if(key.equals("magic_sapling")||key.equals("resonance_glass"))return key;if(key.equals("living_fiber"))return key;return key.contains("midnight_")?"midnight_wood":key;}
 private MagicGrove(){}
}
