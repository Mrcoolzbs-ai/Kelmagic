package dev.kelmagic.archive;
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
}