package dev.kelmagic.content;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
/** Inventory belongs to this block, never to the derived cluster or the menu. */
public final class ArchiveStorageEntity extends BlockEntity implements MenuProvider {
 private boolean loading;
 public final SimpleContainer inventory;
 public ArchiveStorageEntity(BlockPos p,BlockState s){super(ArchiveStorage.ENTITY.get(),p,s);int count=s.getBlock() instanceof ArchiveStorageBlock b&&b.stand?1:ArchiveStorage.SLOTS;inventory=new SimpleContainer(count){@Override public void setChanged(){super.setChanged();if(!loading){ArchiveStorageEntity.this.setChanged();if(level!=null&&!level.isClientSide){level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}}}};}
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);write(t,r);}
 private void write(CompoundTag t,HolderLookup.Provider r){var items=NonNullList.withSize(inventory.getContainerSize(),ItemStack.EMPTY);for(int i=0;i<items.size();i++)items.set(i,inventory.getItem(i));ContainerHelper.saveAllItems(t,items,r);}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);loading=true;try{var items=NonNullList.withSize(inventory.getContainerSize(),ItemStack.EMPTY);ContainerHelper.loadAllItems(t,items,r);for(int i=0;i<items.size();i++)inventory.setItem(i,items.get(i));}finally{loading=false;}}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();write(t,r);return t;}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 @Override public Component getDisplayName(){return getBlockState().getBlock().getName();}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new ArchiveStorageMenu(id,inv,worldPosition);}
}
