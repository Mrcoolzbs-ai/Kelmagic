package dev.kelmagic.content;
import dev.kelmagic.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class ArchiveStorage {
 public static final int SLOTS=18;
 public static final List<DeferredItem<? extends Item>> ITEMS=new ArrayList<>();
 private static DeferredBlock<ArchiveStorageBlock> block(String name,boolean manuscript,boolean stand){var b=KelMagic.BLOCKS.register(name,()->new ArchiveStorageBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion(),manuscript,stand));ITEMS.add(KelMagic.ITEMS.registerSimpleBlockItem(b));return b;}
 public static final DeferredBlock<ArchiveStorageBlock> CABINET=block("archivist_cabinet",false,false);
 public static final DeferredBlock<ArchiveStorageBlock> MANUSCRIPTS=block("manuscript_cabinet",true,false);
 public static final DeferredBlock<ArchiveStorageBlock> STAND=block("sample_stand",false,true);
 public static final DeferredItem<ArchivistInkItem> INK=KelMagic.ITEMS.register("archivist_ink",ArchivistInkItem::new);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArchiveStorageEntity>> ENTITY=MagicAudio.ENTITIES.register("archive_storage",()->BlockEntityType.Builder.of(ArchiveStorageEntity::new,CABINET.get(),MANUSCRIPTS.get(),STAND.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<ArchiveStorageMenu>> MENU=MagicAudio.MENUS.register("archive_storage",()->IMenuTypeExtension.create((id,inv,buf)->new ArchiveStorageMenu(id,inv,buf.readBlockPos())));
 public static void init(){ITEMS.add(INK);}
 /** Face-connected component in loaded chunks only: never force chunk loads. */
 public static List<BlockPos> cluster(Level l,BlockPos root){if(!l.hasChunkAt(root)||!(l.getBlockState(root).getBlock() instanceof ArchiveStorageBlock b)||b.stand)return List.of();var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();seen.add(root.immutable());queue.add(root.immutable());while(!queue.isEmpty()){var at=queue.remove();for(var d:Direction.values()){var next=at.relative(d);if(!seen.contains(next)&&l.hasChunkAt(next)&&l.getBlockState(next).is(b)){seen.add(next);queue.add(next);}}}var sorted=new ArrayList<>(seen);sorted.sort(Comparator.<BlockPos>comparingInt(v->v.getX()).thenComparingInt(v->v.getY()).thenComparingInt(v->v.getZ()));return List.copyOf(sorted);}
 public static boolean manuscript(ItemStack s){return s.getItem() instanceof dev.kelmagic.item.AncientManuscriptItem||s.getItem() instanceof dev.kelmagic.item.WordManuscriptItem||s.getItem() instanceof dev.kelmagic.research.ResearchPaperItem||s.is(Items.WRITTEN_BOOK)||s.is(Items.WRITABLE_BOOK)||s.is(Items.PAPER);}
 private ArchiveStorage(){}
}
