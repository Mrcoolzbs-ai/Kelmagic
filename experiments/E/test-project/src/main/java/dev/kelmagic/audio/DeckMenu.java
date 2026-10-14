package dev.kelmagic.audio;
import dev.kelmagic.*;
import dev.kelmagic.item.ScripterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class DeckMenu extends AbstractContainerMenu {
 public final Container machine;public final ContainerData data;private final ContainerLevelAccess access;
 public DeckMenu(int id,Inventory inv,BlockPos pos){this(id,inv,inv.player.level().getBlockEntity(pos) instanceof DeckEntity d?d:null,pos);}
 public DeckMenu(int id,Inventory inv,DeckEntity d){this(id,inv,d,d.getBlockPos());}
 private DeckMenu(int id,Inventory inv,DeckEntity d,BlockPos pos){super(MagicAudio.DECK_MENU.get(),id);machine=d==null?new SimpleContainer(3):d.inventory;data=d==null?new SimpleContainerData(2):d.data;access=ContainerLevelAccess.create(inv.player.level(),pos);
 addSlot(new Slot(machine,0,29,58){@Override public boolean mayPlace(ItemStack s){return CassetteItem.blank(s);}@Override public int getMaxStackSize(){return 1;}});
 addSlot(new Slot(machine,1,65,58){@Override public boolean mayPlace(ItemStack s){return s.getItem() instanceof ScripterItem && ScripterItem.valid(CassetteItem.words(s)) && !s.getOrDefault(MagicComponents.RECORDING.get(),false);}@Override public int getMaxStackSize(){return 1;}});
 addSlot(new Slot(machine,2,137,58){@Override public boolean mayPlace(ItemStack s){return false;}});
 for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,115+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,173));addDataSlots(data);
 }
 @Override public boolean stillValid(Player p){return stillValid(access,p,MagicAudio.DECK.get());}
 @Override public ItemStack quickMoveStack(Player p,int idx){if(idx<0||idx>=slots.size())return ItemStack.EMPTY;var slot=slots.get(idx);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(idx<3){if(!moveItemStackTo(s,3,39,true))return ItemStack.EMPTY;}else if(CassetteItem.blank(s)){if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}else if(s.getItem() instanceof ScripterItem && dev.kelmagic.item.ScripterItem.valid(CassetteItem.words(s)) && !s.getOrDefault(MagicComponents.RECORDING.get(),false)){if(!moveItemStackTo(s,1,2,false))return ItemStack.EMPTY;}else return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
