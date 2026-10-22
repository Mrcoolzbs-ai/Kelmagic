package dev.kelmagic.content;
import dev.kelmagic.MagicArchive;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class ArchivistInkRecipe extends CustomRecipe {
 public ArchivistInkRecipe(CraftingBookCategory c){super(c);}
 private DyeColor scan(CraftingInput in){boolean glow=false,bottle=false,feather=false;DyeColor color=null;for(int i=0;i<in.size();i++){var s=in.getItem(i);if(s.isEmpty())continue;if(s.is(Items.GLOW_INK_SAC)&&!glow)glow=true;else if(s.is(Items.GLASS_BOTTLE)&&!bottle)bottle=true;else if(s.is(Items.FEATHER)&&!feather)feather=true;else if(s.getItem() instanceof DyeItem d&&color==null)color=d.getDyeColor();else return null;}return glow&&bottle&&feather?(color==null?DyeColor.CYAN:color):null;}
 @Override public boolean matches(CraftingInput in,Level l){return scan(in)!=null;}
 @Override public ItemStack assemble(CraftingInput in,HolderLookup.Provider r){var color=scan(in);if(color==null)return ItemStack.EMPTY;var out=ArchiveStorage.INK.toStack();out.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor(),false));return out;}
 @Override public boolean canCraftInDimensions(int w,int h){return w*h>=3;}
 @Override public RecipeSerializer<?> getSerializer(){return MagicArchive.ARCHIVIST_INK.get();}
}
