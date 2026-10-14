from pathlib import Path
exec(Path('/data/patch19.py').read_text().split("p=r/'gradle.properties'")[0].replace('from pathlib import Path','from pathlib import Path'))
put('archive/ArchiveTableEntity.java','''package dev.kelmagic.archive;
import dev.kelmagic.*;import dev.kelmagic.item.*;import net.minecraft.core.*;import net.minecraft.nbt.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.network.chat.Component;
public final class ArchiveTableEntity extends BlockEntity implements MenuProvider {
 private boolean updating;public final SimpleContainer inventory=new SimpleContainer(5){public void setChanged(){super.setChanged();ArchiveTableEntity.this.setChanged();refresh();}};public boolean copying;
 public ArchiveTableEntity(BlockPos p,BlockState s){super(MagicStudy.TABLE_ENTITY.get(),p,s);}
 public void refresh(){if(updating)return;updating=true;try{var grid=CraftingInput.of(2,2,java.util.stream.IntStream.range(0,4).mapToObj(inventory::getItem).toList());var add=new ArchiveRecipe(CraftingBookCategory.MISC);var copy=new CodexCopyRecipe(CraftingBookCategory.MISC);copying=copy.matches(grid,level);ItemStack result=add.matches(grid,level)?add.assemble(grid,level.registryAccess()):copying?copy.assemble(grid,level.registryAccess()):ItemStack.EMPTY;inventory.setItem(4,result);}finally{updating=false;}}
 public void finish(Player player){if(inventory.getItem(4).isEmpty())return;updating=true;try{for(int i=0;i<4;i++)if(!copying||!(inventory.getItem(i).getItem() instanceof CodexItem))inventory.removeItem(i,1);}finally{updating=false;}if(player instanceof net.minecraft.server.level.ServerPlayer sp)dev.kelmagic.server.MagicProgress.award(sp,copying?"copy_codex":"archive_word");refresh();}
 protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);var list=new ListTag();for(int i=0;i<4;i++)if(!inventory.getItem(i).isEmpty()){var v=new CompoundTag();v.putInt("Slot",i);v.put("Stack",inventory.getItem(i).save(p));list.add(v);}t.put("Items",list);}
 protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);updating=true;inventory.clearContent();var list=t.getList("Items",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++){var v=list.getCompound(i);int slot=v.getInt("Slot");if(slot>=0&&slot<4)inventory.setItem(slot,ItemStack.parseOptional(p,v.getCompound("Stack")));}updating=false;}
 public Component getDisplayName(){return Component.translatable("block.kelmagic.archivist_table");}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){refresh();return new ArchiveTableMenu(id,inv,this);}
}''')
# Finish must be invoked after output removed, don't depend on output remaining.
sub('archive/ArchiveTableEntity.java','if(inventory.getItem(4).isEmpty())return;updating=true','updating=true')
put('archive/ArchiveTableBlock.java','''package dev.kelmagic.archive;
import dev.kelmagic.*;import com.mojang.serialization.MapCodec;import net.minecraft.core.BlockPos;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.world.phys.*;
public final class ArchiveTableBlock extends BaseEntityBlock {
 public static final MapCodec<ArchiveTableBlock> CODEC=simpleCodec(ArchiveTableBlock::new);public ArchiveTableBlock(Properties p){super(p);}
 protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArchiveTableEntity(p,s);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){if(l.getBlockEntity(pos) instanceof ArchiveTableEntity d){if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.openMenu(d,b->b.writeBlockPos(pos));return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 protected void onRemove(BlockState old,Level l,BlockPos pos,BlockState next,boolean moving){if(!old.is(next.getBlock())&&l.getBlockEntity(pos) instanceof ArchiveTableEntity d){var inputs=new SimpleContainer(4);for(int i=0;i<4;i++)inputs.setItem(i,d.inventory.getItem(i));Containers.dropContents(l,pos,inputs);}super.onRemove(old,l,pos,next,moving);}
}''')
put('archive/ArchiveTableMenu.java','''package dev.kelmagic.archive;
import dev.kelmagic.*;import dev.kelmagic.item.*;import net.minecraft.core.BlockPos;import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.*;
public final class ArchiveTableMenu extends AbstractContainerMenu {
 public final Container machine;private final ArchiveTableEntity table;private final ContainerLevelAccess access;
 public ArchiveTableMenu(int id,Inventory inv,BlockPos pos){this(id,inv,inv.player.level().getBlockEntity(pos) instanceof ArchiveTableEntity d?d:null,pos);}
 public ArchiveTableMenu(int id,Inventory inv,ArchiveTableEntity d){this(id,inv,d,d.getBlockPos());}
 private ArchiveTableMenu(int id,Inventory inv,ArchiveTableEntity d,BlockPos pos){super(MagicStudy.TABLE_MENU.get(),id);table=d;machine=d==null?new SimpleContainer(5):d.inventory;access=ContainerLevelAccess.create(inv.player.level(),pos);
 for(int i=0;i<4;i++){final int n=i;addSlot(new Slot(machine,i,22+i*31,58){public boolean mayPlace(ItemStack s){return n==0?s.getItem() instanceof CodexItem:s.getItem() instanceof WordManuscriptItem||s.is(Items.BOOK)||s.is(Items.FEATHER)||s.is(Items.INK_SAC)||s.is(Items.GLOW_INK_SAC);}});}
 addSlot(new Slot(machine,4,170,58){public boolean mayPlace(ItemStack s){return false;}public void onTake(Player p,ItemStack s){if(table!=null)table.finish(p);super.onTake(p,s);}});
 for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,24+x*18,112+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,24+x*18,170));}
 public boolean stillValid(Player p){return stillValid(access,p,MagicStudy.TABLE.get());}
 public ItemStack quickMoveStack(Player p,int idx){if(idx<0||idx>=slots.size())return ItemStack.EMPTY;var slot=slots.get(idx);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(idx<5){if(!moveItemStackTo(s,5,41,true))return ItemStack.EMPTY;}else if(s.getItem() instanceof CodexItem){if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,1,4,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}''')
put('MagicStudy.java','''package dev.kelmagic;
import dev.kelmagic.archive.*;import net.minecraft.core.registries.Registries;import net.minecraft.world.entity.npc.VillagerProfession;import net.minecraft.world.entity.ai.village.poi.PoiType;import net.minecraft.world.item.*;import net.minecraft.world.inventory.*;import net.minecraft.world.level.block.entity.*;import net.neoforged.neoforge.registries.*;import net.neoforged.bus.api.IEventBus;
public final class MagicStudy {
 public static final DeferredBlock<ArchiveTableBlock> TABLE=KelMagic.BLOCKS.register("archivist_table",()->new ArchiveTableBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(3).sound(net.minecraft.world.level.block.SoundType.WOOD)));
 public static final DeferredItem<BlockItem> TABLE_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(TABLE);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArchiveTableEntity>> TABLE_ENTITY=MagicAudio.ENTITIES.register("archivist_table",()->BlockEntityType.Builder.of(ArchiveTableEntity::new,TABLE.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<ArchiveTableMenu>> TABLE_MENU=MagicAudio.MENUS.register("archivist_table",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inv,b)->new ArchiveTableMenu(id,inv,b.readBlockPos())));
 public static final DeferredRegister<PoiType> POIS=DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE,KelMagic.MODID);
 public static final DeferredRegister<VillagerProfession> JOBS=DeferredRegister.create(Registries.VILLAGER_PROFESSION,KelMagic.MODID);
 public static final DeferredHolder<PoiType,PoiType> POI=POIS.register("archivist",()->new PoiType(java.util.Set.copyOf(TABLE.get().getStateDefinition().getPossibleStates()),1,1));
 public static final DeferredHolder<VillagerProfession,VillagerProfession> ARCHIVIST=JOBS.register("archivist",()->new VillagerProfession("archivist",h->h.value()==POI.get(),h->h.value()==POI.get(),com.google.common.collect.ImmutableSet.of(),com.google.common.collect.ImmutableSet.of(),net.minecraft.sounds.SoundEvents.VILLAGER_WORK_LIBRARIAN));
 public static void register(IEventBus b){POIS.register(b);JOBS.register(b);}private MagicStudy(){}
}''')
put('server/ArchivistTrades.java','''package dev.kelmagic.server;
import dev.kelmagic.*;import net.minecraft.world.item.Items;import net.minecraft.world.entity.npc.VillagerTrades;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class ArchivistTrades {
 @SubscribeEvent public static void trades(net.neoforged.neoforge.event.village.VillagerTradesEvent e){if(e.getType()!=MagicStudy.ARCHIVIST.get())return;var t=e.getTrades();t.get(1).add(new VillagerTrades.ItemsForEmeralds(Items.BOOK,1,3,16,1));t.get(1).add(new VillagerTrades.ItemsForEmeralds(MagicArchive.CODEX.get(),6,1,8,2));t.get(2).add(new VillagerTrades.ItemsForEmeralds(MagicAudio.CASSETTE.get(),4,1,12,5));t.get(2).add(new VillagerTrades.ItemsForEmeralds(MagicContent.MANA_CORE.get(),8,1,8,5));t.get(3).add(new VillagerTrades.ItemsForEmeralds(MagicContent.SCRIPTER.get(),18,1,4,10));t.get(4).add(new VillagerTrades.ItemsForEmeralds(MagicContent.MANA_CORE.get(),6,2,8,15));t.get(5).add(new VillagerTrades.ItemsForEmeralds(MagicArchive.ANCIENT.get(),24,1,2,30));}
}''')
sub('KelMagic.java','MagicMaterials.init();','MagicStudy.register(modEventBus);MagicLamps.init();MagicMaterials.init();')
sub('MagicContent.java','out.accept(MagicMaterials.GOLDEN_THREAD);','out.accept(MagicStudy.TABLE_ITEM);out.accept(MagicMaterials.GOLDEN_THREAD);')
put('client/ArchiveTableScreen.java','''package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.archive.ArchiveTableMenu;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.world.entity.player.Inventory;import net.minecraft.network.chat.Component;import net.minecraft.resources.ResourceLocation;
public final class ArchiveTableScreen extends AbstractContainerScreen<ArchiveTableMenu>{
 public ArchiveTableScreen(ArchiveTableMenu m,Inventory i,Component t){super(m,i,t);imageWidth=210;imageHeight=194;titleLabelX=16;titleLabelY=10;inventoryLabelX=24;inventoryLabelY=101;}
 protected void renderBg(GuiGraphics g,float p,int x,int y){g.blit(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/gui/archive_table.png"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);g.drawString(font,Component.translatable("kelmagic.table.hint"),leftPos+16,topPos+30,0xA9C6C7,false);}
 protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xEAF4D4,false);g.drawString(font,playerInventoryTitle,24,101,0x9CA995,false);}
 public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
}''')
sub('client/AudioScreens.java','e.register(MagicAudio.PLAYER_MENU.get(),','e.register(MagicStudy.TABLE_MENU.get(),ArchiveTableScreen::new);e.register(MagicAudio.PLAYER_MENU.get(),')
