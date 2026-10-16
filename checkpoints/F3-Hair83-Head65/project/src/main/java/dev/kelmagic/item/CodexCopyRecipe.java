package dev.kelmagic.item;
import dev.kelmagic.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
/** Shapeless copy: consume book/feather/ink; return the original in its own slot. */
public final class CodexCopyRecipe extends CustomRecipe {
 public CodexCopyRecipe(CraftingBookCategory c){super(c);}
 private int source(CraftingInput in){
  int codex=-1,books=0,feathers=0,inks=0;
  for(int i=0;i<in.size();i++){
   var s=in.getItem(i);if(s.isEmpty())continue;
   if(s.getItem() instanceof CodexItem && codex<0)codex=i;
   else if(s.is(Items.BOOK)&&books==0)books++;
   else if(s.is(Items.FEATHER)&&feathers==0)feathers++;
   else if((s.is(Items.INK_SAC)||s.is(Items.GLOW_INK_SAC))&&inks==0)inks++;
   else return -1;
  }
  return codex>=0 && books==1 && feathers==1 && inks==1 && !CodexItem.knownWords(in.getItem(codex)).isEmpty()?codex:-1;
 }
 @Override public boolean matches(CraftingInput in,Level l){return source(in)>=0;}
 @Override public ItemStack assemble(CraftingInput in,HolderLookup.Provider regs){int i=source(in);if(i<0)return ItemStack.EMPTY;var result=in.getItem(i).copyWithCount(1);if(result.has(MagicComponents.CODEX_TOKEN.get())||result.has(MagicComponents.CODEX_WELCOMED.get()))result.set(MagicComponents.CODEX_WELCOMED.get(),false);if(result.has(MagicComponents.CODEX_TOKEN.get()))result.set(MagicComponents.CODEX_TOKEN.get(),UUID.randomUUID().toString());return result;}
 @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in){
  var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);int i=source(in);if(i>=0)out.set(i,in.getItem(i).copyWithCount(1));return out;
 }
 @Override public boolean canCraftInDimensions(int w,int h){return w*h>=4;}
 @Override public RecipeSerializer<?> getSerializer(){return MagicArchive.COPY.get();}
}
