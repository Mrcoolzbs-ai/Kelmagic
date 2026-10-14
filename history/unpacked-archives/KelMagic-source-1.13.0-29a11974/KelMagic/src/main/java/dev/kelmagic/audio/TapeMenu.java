package dev.kelmagic.audio;
import dev.kelmagic.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
public final class TapeMenu extends AbstractContainerMenu {
 public final SimpleContainer tapes;
 public final ItemStack holder;
 private final Player owner;private final InteractionHand hand;private final int locked;
 public TapeMenu(int id,Inventory inv,InteractionHand hand){super(MagicAudio.PLAYER_MENU.get(),id);this.owner=inv.player;this.hand=hand;this.holder=owner.getItemInHand(hand);locked=hand==InteractionHand.MAIN_HAND?inv.selected:-1;
 tapes=new SimpleContainer(8){@Override public void setChanged(){super.setChanged();if(!owner.level().isClientSide && holder.getItem() instanceof TapePlayerItem)holder.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(getItems()));}};
 var contents=TapePlayerItem.tapes(holder);for(int i=0;i<8;i++)tapes.setItem(i,contents.get(i));
 for(int i=0;i<8;i++){final int index=i;addSlot(new Slot(tapes,i,24+(i%4)*38,54+(i/4)*29){@Override public boolean mayPlace(ItemStack s){return CassetteItem.recorded(s);}@Override public int getMaxStackSize(){return 1;}});}
 for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(inventorySlot(inv,x+y*9+9,8+x*18,126+y*18));
 for(int x=0;x<9;x++)addSlot(inventorySlot(inv,x,8+x*18,184));
 }
 private Slot inventorySlot(Inventory inv,int index,int x,int y){final int inventoryIndex=index;return new Slot(inv,index,x,y){@Override public boolean mayPickup(Player p){return inventoryIndex!=locked;}@Override public boolean mayPlace(ItemStack s){return inventoryIndex!=locked;}};}
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(type==ClickType.SWAP&&(button==locked||button==40&&hand==InteractionHand.OFF_HAND))return;super.clicked(slot,button,type,p);}
 @Override public boolean stillValid(Player p){return p==owner && p.isAlive() && p.getItemInHand(hand)==holder && holder.getItem() instanceof TapePlayerItem;}
 @Override public ItemStack quickMoveStack(Player p,int idx){if(idx<0||idx>=slots.size())return ItemStack.EMPTY;var slot=slots.get(idx);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(idx<8){if(!moveItemStackTo(s,8,44,true))return ItemStack.EMPTY;}else if(!CassetteItem.recorded(s)||!moveItemStackTo(s,0,8,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
