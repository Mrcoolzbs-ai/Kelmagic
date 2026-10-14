package dev.kelmagic.item;
import java.util.*;
import dev.kelmagic.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
/** Copies the codex's complete components; accepts exactly one new word sheet. */
public final class ArchiveRecipe extends CustomRecipe {
 public ArchiveRecipe(CraftingBookCategory c){super(c);}
 private record Input(ItemStack book,WordManuscriptItem page){}
 private Input scan(CraftingInput in){
  ItemStack book=ItemStack.EMPTY;WordManuscriptItem page=null;
  for(int i=0;i<in.size();i++){
   var s=in.getItem(i);if(s.isEmpty())continue;
   if(s.getItem() instanceof CodexItem && book.isEmpty())book=s;
   else if(s.getItem() instanceof WordManuscriptItem w && page==null)page=w;
   else return null;
  }
  if(book.isEmpty() || page==null || book.getOrDefault(MagicComponents.CODEX_WORDS.get(),List.<String>of()).contains(page.word))return null;
  return new Input(book,page);
 }
 @Override public boolean matches(CraftingInput in,Level l){return scan(in)!=null;}
 @Override public ItemStack assemble(CraftingInput in,HolderLookup.Provider regs){
  var x=scan(in);if(x==null)return ItemStack.EMPTY;
  var s=x.book.copyWithCount(1);var words=new ArrayList<>(s.getOrDefault(MagicComponents.CODEX_WORDS.get(),List.<String>of()));
  words.add(x.page.word);s.set(MagicComponents.CODEX_WORDS.get(),List.copyOf(words));return s;
 }
 @Override public boolean canCraftInDimensions(int w,int h){return w*h>=2;}
 @Override public RecipeSerializer<?> getSerializer(){return MagicArchive.ADD.get();}
}
