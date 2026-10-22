package dev.kelmagic.archive;
import dev.kelmagic.*;import dev.kelmagic.item.*;import net.minecraft.core.*;import net.minecraft.nbt.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.*;import net.minecraft.network.chat.Component;
public final class ArchiveTableEntity extends BlockEntity implements MenuProvider {
 private ItemStack mountedCodex=ItemStack.EMPTY;
 public ItemStack mountedCodex(){return mountedCodex;}
 public boolean hasCodex(){return !mountedCodex.isEmpty();}
 public void mount(ItemStack stack){mountedCodex=stack.copyWithCount(1);if(!mountedCodex.has(MagicComponents.CODEX_TOKEN.get()))mountedCodex.set(MagicComponents.CODEX_TOKEN.get(),java.util.UUID.randomUUID().toString());changedCodex();refresh();}
 public ItemStack unmount(){var result=mountedCodex;mountedCodex=ItemStack.EMPTY;changedCodex();return result;}
 private void changedCodex(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 public CompoundTag getUpdateTag(HolderLookup.Provider p){var t=new CompoundTag();t.putBoolean("HasMountedCodex",hasCodex());if(hasCodex())t.put("MountedCodex",mountedCodex.save(p));return t;}
 // A book-only packet must not clear a concurrently open crafting inventory.
 public void handleUpdateTag(CompoundTag t,HolderLookup.Provider p){mountedCodex=ItemStack.parseOptional(p,t.getCompound("MountedCodex"));if(!(mountedCodex.getItem() instanceof CodexItem))mountedCodex=ItemStack.EMPTY;}
 public void onDataPacket(net.minecraft.network.Connection c,net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet,HolderLookup.Provider p){handleUpdateTag(packet.getTag(),p);}
 private boolean updating;public final SimpleContainer inventory=new SimpleContainer(5){public void setChanged(){super.setChanged();ArchiveTableEntity.this.setChanged();refresh();}};public boolean copying;
 public ArchiveTableEntity(BlockPos p,BlockState s){super(MagicStudy.TABLE_ENTITY.get(),p,s);}
 public void refresh(){if(updating)return;updating=true;try{if(level==null)return;
 if(!level.isClientSide&&hasCodex()){
  var words=new java.util.ArrayList<>(mountedCodex.getOrDefault(MagicComponents.CODEX_WORDS.get(),java.util.List.<String>of()));boolean imported=false;
  for(int i=0;i<4;i++){var page=inventory.getItem(i);if(page.getItem() instanceof WordManuscriptItem w&&!words.contains(w.word)){words.add(w.word);inventory.removeItem(i,1);imported=true;}}
  if(imported){mountedCodex.set(MagicComponents.CODEX_WORDS.get(),java.util.List.copyOf(words));changedCodex();}
 }
 var grid=CraftingInput.of(2,2,java.util.stream.IntStream.range(0,4).mapToObj(inventory::getItem).toList());var add=new ArchiveRecipe(CraftingBookCategory.MISC);var copy=new CodexCopyRecipe(CraftingBookCategory.MISC);copying=copy.matches(grid,level);ItemStack result=add.matches(grid,level)?add.assemble(grid,level.registryAccess()):copying?copy.assemble(grid,level.registryAccess()):ItemStack.EMPTY;inventory.setItem(4,result);}finally{updating=false;}}
 public void finish(Player player){updating=true;try{for(int i=0;i<4;i++)if(!copying||!(inventory.getItem(i).getItem() instanceof CodexItem))inventory.removeItem(i,1);}finally{updating=false;}if(player instanceof net.minecraft.server.level.ServerPlayer sp){dev.kelmagic.server.MagicProgress.award(sp,copying?"copy_codex":"archive_word");dev.kelmagic.research.Research.tab(sp,"words");}refresh();}
 protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);if(hasCodex())t.put("MountedCodex",mountedCodex.save(p));var list=new ListTag();for(int i=0;i<4;i++)if(!inventory.getItem(i).isEmpty()){var v=new CompoundTag();v.putInt("Slot",i);v.put("Stack",inventory.getItem(i).save(p));list.add(v);}t.put("Items",list);}
 protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){super.loadAdditional(t,p);mountedCodex=ItemStack.parseOptional(p,t.getCompound("MountedCodex"));if(!(mountedCodex.getItem() instanceof CodexItem))mountedCodex=ItemStack.EMPTY;updating=true;inventory.clearContent();var list=t.getList("Items",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++){var v=list.getCompound(i);int slot=v.getInt("Slot");if(slot>=0&&slot<4)inventory.setItem(slot,ItemStack.parseOptional(p,v.getCompound("Stack")));}updating=false;}
 public Component getDisplayName(){return Component.translatable("block.kelmagic.archivist_table");}
 public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){refresh();return new ArchiveTableMenu(id,inv,this);}
}