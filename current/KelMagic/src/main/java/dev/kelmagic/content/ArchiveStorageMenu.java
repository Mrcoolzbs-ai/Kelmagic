package dev.kelmagic.content;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
/** Fixed 54-slot viewport over any loaded face-connected cluster. Server resolves every slot. */
public final class ArchiveStorageMenu extends AbstractContainerMenu {
 public final BlockPos origin;
 private final Player owner;
 private final List<BlockPos> members;
 private final boolean client;
 private final Container view;
 private final ContainerData data;
 private int page;
 public ArchiveStorageMenu(int id,Inventory inv,BlockPos at){super(ArchiveStorage.MENU.get(),id);origin=at.immutable();owner=inv.player;client=owner.level().isClientSide;members=client?List.of():ArchiveStorage.cluster(owner.level(),origin);var local=new SimpleContainer(54);
  data=client?new SimpleContainerData(4):new ContainerData(){public int getCount(){return 4;}public int get(int i){return switch(i){case 0->page;case 1->members.size()&65535;case 2->members.size()>>>16;default->owner.level().getBlockState(origin).is(ArchiveStorage.MANUSCRIPTS.get())?1:0;};}public void set(int i,int v){if(i==0)page=v;}};
  view=client?local:new Container(){public int getContainerSize(){return 54;}public boolean isEmpty(){for(int i=0;i<54;i++)if(!getItem(i).isEmpty())return false;return true;}private ArchiveStorageEntity entity(int i){int n=page*3+i/18;return n>=0&&n<members.size()&&owner.level().hasChunkAt(members.get(n))&&owner.level().getBlockEntity(members.get(n)) instanceof ArchiveStorageEntity d?d:null;}public ItemStack getItem(int i){var d=entity(i);return d==null?ItemStack.EMPTY:d.inventory.getItem(i%18);}public ItemStack removeItem(int i,int n){var d=entity(i);return d==null?ItemStack.EMPTY:d.inventory.removeItem(i%18,n);}public ItemStack removeItemNoUpdate(int i){var d=entity(i);return d==null?ItemStack.EMPTY:d.inventory.removeItemNoUpdate(i%18);}public void setItem(int i,ItemStack s){var d=entity(i);if(d!=null)d.inventory.setItem(i%18,s);}public void setChanged(){for(int n=page*3;n<Math.min(members.size(),page*3+3);n++)if(owner.level().getBlockEntity(members.get(n)) instanceof ArchiveStorageEntity d)d.inventory.setChanged();}public boolean stillValid(Player p){return ArchiveStorageMenu.this.stillValid(p);}public void clearContent(){for(int i=0;i<54;i++)setItem(i,ItemStack.EMPTY);}};
  for(int y=0;y<6;y++)for(int x=0;x<9;x++){final int slot=y*9+x;addSlot(new Slot(view,slot,8+x*18,32+y*18){@Override public boolean isActive(){return slotAvailable(slot);}@Override public boolean mayPlace(ItemStack s){return slotAvailable(slot)&&(data.get(3)==0||ArchiveStorage.manuscript(s));}});}
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,9+y*9+x,8+x*18,158+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,216));addDataSlots(data);
 }
 public int page(){return data.get(0);}
 public int modules(){return (data.get(1)&65535)|((data.get(2)&65535)<<16);}
 public int pages(){return Math.max(1,(modules()+2)/3);}
 public boolean slotAvailable(int slot){return page()*54+slot<modules()*18;}
 public boolean topologyValid(){return client||members.equals(ArchiveStorage.cluster(owner.level(),origin));}
 @Override public boolean stillValid(Player p){return client||p==owner&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(origin))<=64&&!members.isEmpty()&&topologyValid();}
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(!client&&!stillValid(p)){p.closeContainer();return;}super.clicked(slot,button,type,p);}
 @Override public boolean clickMenuButton(Player p,int id){if(client||p!=owner||id<0||id>1||!stillValid(p))return false;page=Math.clamp(page+(id==0?-1:1),0,pages()-1);broadcastChanges();return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||!stillValid(p))return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var out=s.copy();if(index<54){if(!moveItemStackTo(s,54,90,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,54,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return out;}
}
