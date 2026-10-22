package dev.kelmagic.archive;

import dev.kelmagic.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Component-preserving upgrade. No template is consumed or required. */
public final class MonocleSmithingRecipe implements SmithingRecipe {
 public static boolean helmet(ItemStack stack){var e=Equipable.get(stack);return !stack.isEmpty()&&e!=null&&e.getEquipmentSlot()==EquipmentSlot.HEAD;}
 public static boolean installed(ItemStack stack){return Boolean.TRUE.equals(stack.get(MagicComponents.MONOCLE.get()));}
 public boolean isTemplateIngredient(ItemStack stack){return stack.isEmpty();}
 public boolean isBaseIngredient(ItemStack stack){return helmet(stack)&&!installed(stack);}
 public boolean isAdditionIngredient(ItemStack stack){return stack.is(MagicStudy.MONOCLE.get());}
 public boolean matches(SmithingRecipeInput input,Level level){return input.template().isEmpty()&&isBaseIngredient(input.base())&&isAdditionIngredient(input.addition());}
 public ItemStack assemble(SmithingRecipeInput input,HolderLookup.Provider lookup){if(!matches(input,null))return ItemStack.EMPTY;var result=input.base().copyWithCount(1);result.set(MagicComponents.MONOCLE.get(),true);return result;}
 public ItemStack getResultItem(HolderLookup.Provider lookup){var result=new ItemStack(Items.IRON_HELMET);result.set(MagicComponents.MONOCLE.get(),true);return result;}
 public RecipeSerializer<?> getSerializer(){return MagicArchive.MONOCLE.get();}
 public static final class Serializer implements RecipeSerializer<MonocleSmithingRecipe>{
  private static final MapCodec<MonocleSmithingRecipe> CODEC=MapCodec.unit(MonocleSmithingRecipe::new);
  private static final StreamCodec<RegistryFriendlyByteBuf,MonocleSmithingRecipe> STREAM=StreamCodec.of((buffer,value)->{},buffer->new MonocleSmithingRecipe());
  public MapCodec<MonocleSmithingRecipe> codec(){return CODEC;}
  public StreamCodec<RegistryFriendlyByteBuf,MonocleSmithingRecipe> streamCodec(){return STREAM;}
 }
}
