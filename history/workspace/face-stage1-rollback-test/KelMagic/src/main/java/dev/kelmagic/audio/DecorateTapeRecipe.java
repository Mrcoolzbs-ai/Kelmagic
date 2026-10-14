package dev.kelmagic.audio;
import dev.kelmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class DecorateTapeRecipe extends CustomRecipe {
 public DecorateTapeRecipe(CraftingBookCategory c){super(c);}
 private record Inputs(ItemStack tape,DyeItem dye,BannerPatternItem pattern){}
 private Inputs scan(CraftingInput in){ItemStack tape=ItemStack.EMPTY;DyeItem dye=null;BannerPatternItem pattern=null;boolean slime=false;
 for(int i=0;i<in.size();i++){var s=in.getItem(i);if(s.isEmpty())continue;
 if(CassetteItem.recorded(s)&&tape.isEmpty())tape=s;
 else if(s.is(Items.SLIME_BALL)&&!slime)slime=true;
 else if(s.getItem() instanceof DyeItem d&&dye==null)dye=d;
 else if(s.getItem() instanceof BannerPatternItem b&&pattern==null)pattern=b;
 else return null;
 }return !tape.isEmpty()&&slime&&(dye!=null||pattern!=null)?new Inputs(tape,dye,pattern):null;}
 @Override public boolean matches(CraftingInput in,Level l){return scan(in)!=null;}
 @Override public ItemStack assemble(CraftingInput in,HolderLookup.Provider regs){var x=scan(in);if(x==null)return ItemStack.EMPTY;var s=x.tape.copyWithCount(1);
 if(x.dye!=null)s.set(DataComponents.DYED_COLOR,new DyedItemColor(x.dye.getDyeColor().getTextureDiffuseColor(),false));
 if(x.pattern!=null){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(x.pattern).toString();s.set(MagicComponents.TAPE_PATTERN.get(),id);}return s;}
 @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in){var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);if(scan(in)!=null)for(int i=0;i<in.size();i++)if(in.getItem(i).getItem() instanceof BannerPatternItem)out.set(i,in.getItem(i).copyWithCount(1));return out;}
 @Override public boolean canCraftInDimensions(int w,int h){return w*h>=3;}
 @Override public RecipeSerializer<?> getSerializer(){return MagicArchive.DECORATE.get();}
}
