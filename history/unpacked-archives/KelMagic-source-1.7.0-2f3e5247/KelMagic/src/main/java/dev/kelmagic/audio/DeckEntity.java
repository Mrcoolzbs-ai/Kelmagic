package dev.kelmagic.audio;
import dev.kelmagic.*;
import dev.kelmagic.item.ScripterItem;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
public final class DeckEntity extends BlockEntity implements MenuProvider {
 public final SimpleContainer inventory=new SimpleContainer(3){@Override public void setChanged(){super.setChanged();DeckEntity.this.setChanged();}};
 public int progress,total;private List<String> recording=List.of();
 public final ContainerData data=new ContainerData(){public int get(int i){return i==0?progress:total;}public void set(int i,int v){if(i==0)progress=v;else total=v;}public int getCount(){return 2;}};
 public DeckEntity(BlockPos p,BlockState s){super(MagicAudio.DECK_ENTITY.get(),p,s);}
 public static int ticks(List<String> w){return w.size()*100;}
 public static void tick(Level l,BlockPos pos,BlockState s,DeckEntity d){d.advance();}
 public void advance(){
 var tape=inventory.getItem(0);var writer=inventory.getItem(1);var words=writer.getOrDefault(MagicComponents.SPELL.get(),List.<String>of());
 boolean valid=CassetteItem.blank(tape) && writer.getItem() instanceof ScripterItem && !writer.getOrDefault(MagicComponents.RECORDING.get(),false) && ScripterItem.valid(words) && inventory.getItem(2).isEmpty();
 if(!valid){if(progress!=0||total!=0){progress=total=0;recording=List.of();setChanged();}return;}
 if(!words.equals(recording)){recording=List.copyOf(words);progress=0;total=ticks(words);}
 if(++progress>=total){var result=tape.copyWithCount(1);result.set(MagicComponents.SPELL.get(),recording);inventory.setItem(2,result);inventory.removeItem(0,1);progress=total=0;recording=List.of();}setChanged();
 }
 @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider regs){super.saveAdditional(tag,regs);var list=new ListTag();for(int i=0;i<3;i++){var s=inventory.getItem(i);if(!s.isEmpty()){var t=new CompoundTag();t.putInt("Slot",i);t.put("Stack",s.save(regs));list.add(t);}}tag.put("Items",list);tag.putInt("Progress",progress);tag.putInt("Total",total);var words=new ListTag();for(String w:recording)words.add(StringTag.valueOf(w));tag.put("Recording",words);}
 @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider regs){super.loadAdditional(tag,regs);inventory.clearContent();var list=tag.getList("Items",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++){var t=list.getCompound(i);int slot=t.getInt("Slot");if(slot>=0&&slot<3)inventory.setItem(slot,ItemStack.parseOptional(regs,t.getCompound("Stack")));}progress=Math.max(0,Math.min(10000,tag.getInt("Progress")));total=Math.max(0,Math.min(10000,tag.getInt("Total")));var words=tag.getList("Recording",Tag.TAG_STRING);var out=new ArrayList<String>();for(int i=0;i<Math.min(100,words.size());i++)out.add(words.getString(i));recording=List.copyOf(out);}
 @Override public Component getDisplayName(){return Component.translatable("block.kelmagic.cassette_deck");}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new DeckMenu(id,inv,this);}
}
