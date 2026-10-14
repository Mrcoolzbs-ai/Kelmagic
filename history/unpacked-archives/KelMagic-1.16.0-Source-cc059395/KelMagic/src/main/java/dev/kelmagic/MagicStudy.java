package dev.kelmagic;
import dev.kelmagic.archive.*;import net.minecraft.core.registries.Registries;import net.minecraft.world.entity.npc.VillagerProfession;import net.minecraft.world.entity.ai.village.poi.PoiType;import net.minecraft.world.item.*;import net.minecraft.world.inventory.*;import net.minecraft.world.level.block.entity.*;import net.neoforged.neoforge.registries.*;import net.neoforged.bus.api.IEventBus;
public final class MagicStudy {
 public static final DeferredBlock<ArchiveTableBlock> TABLE=KelMagic.BLOCKS.register("archivist_table",()->new ArchiveTableBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(3).noOcclusion().sound(net.minecraft.world.level.block.SoundType.WOOD)));
 public static final DeferredItem<BlockItem> TABLE_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(TABLE);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArchiveTableEntity>> TABLE_ENTITY=MagicAudio.ENTITIES.register("archivist_table",()->BlockEntityType.Builder.of(ArchiveTableEntity::new,TABLE.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<ArchiveTableMenu>> TABLE_MENU=MagicAudio.MENUS.register("archivist_table",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inv,b)->new ArchiveTableMenu(id,inv,b.readBlockPos())));
 public static final DeferredRegister<PoiType> POIS=DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE,KelMagic.MODID);
 public static final DeferredRegister<VillagerProfession> JOBS=DeferredRegister.create(Registries.VILLAGER_PROFESSION,KelMagic.MODID);
 public static final DeferredHolder<PoiType,PoiType> POI=POIS.register("archivist",()->new PoiType(java.util.Set.copyOf(TABLE.get().getStateDefinition().getPossibleStates()),1,1));
 public static final DeferredHolder<VillagerProfession,VillagerProfession> ARCHIVIST=JOBS.register("archivist",()->new VillagerProfession("archivist",h->h.value()==POI.get(),h->h.value()==POI.get(),com.google.common.collect.ImmutableSet.of(),com.google.common.collect.ImmutableSet.of(),net.minecraft.sounds.SoundEvents.VILLAGER_WORK_LIBRARIAN));
 public static void register(IEventBus b){POIS.register(b);JOBS.register(b);}private MagicStudy(){}
}