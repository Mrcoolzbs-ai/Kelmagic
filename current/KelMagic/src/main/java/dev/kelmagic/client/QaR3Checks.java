package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.archive.*;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;import net.minecraft.core.component.DataComponents;import net.minecraft.network.chat.Component;import net.minecraft.world.inventory.*;
/** TEST harness only, excluded from distributable jar. */
public final class QaR3Checks {
 static void ok(boolean b,String why){if(!b)throw new AssertionError("R3 "+why);}
 public static void server(net.minecraft.server.level.ServerPlayer p){var l=p.serverLevel();var recipe=new MonocleSmithingRecipe();int count=0;
  for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){var base=new ItemStack(item);if(!MonocleSmithingRecipe.helmet(base))continue;
   base.set(DataComponents.CUSTOM_NAME,Component.literal("QA preserved"));if(base.isDamageableItem())base.setDamageValue(3);
   var original=base.copy();var input=new SmithingRecipeInput(ItemStack.EMPTY,base,MagicStudy.MONOCLE.toStack());ok(recipe.matches(input,l),"helmet accepted "+item);var out=recipe.assemble(input,l.registryAccess());ok(MonocleSmithingRecipe.installed(out),"marker");var unmarked=out.copy();unmarked.remove(MagicComponents.MONOCLE.get());ok(ItemStack.isSameItemSameComponents(original,unmarked),"all components preserved");ok(ItemStack.isSameItemSameComponents(original,base),"input unmodified");ok(!recipe.isBaseIngredient(out),"duplicate rejected");
   var tag=out.save(l.registryAccess());var loaded=ItemStack.parseOptional(l.registryAccess(),(net.minecraft.nbt.CompoundTag)tag);ok(ItemStack.isSameItemSameComponents(out,loaded),"save roundtrip");count++;
  }
  ok(count>=7,"vanilla helmets");ok(!recipe.isBaseIngredient(new ItemStack(Items.IRON_CHESTPLATE)),"chestplate rejected");ok(!recipe.matches(new SmithingRecipeInput(new ItemStack(Items.PAPER),new ItemStack(Items.IRON_HELMET),MagicStudy.MONOCLE.toStack()),l),"nonempty template rejected");
  var menu=new SmithingMenu(77,p.getInventory());menu.getSlot(1).set(new ItemStack(Items.DIAMOND_HELMET));menu.getSlot(2).set(MagicStudy.MONOCLE.toStack());menu.createResult();ok(menu.getSlot(2).mayPlace(MagicStudy.MONOCLE.toStack()),"addition slot accepts");ok(MonocleSmithingRecipe.installed(menu.getSlot(3).getItem()),"real menu output, no template");var got=menu.getSlot(3).getItem().copy();menu.getSlot(3).onTake(p,got);ok(menu.getSlot(1).getItem().isEmpty()&&menu.getSlot(2).getItem().isEmpty(),"smithing consumed base and one monocle");
  var grid=new java.util.ArrayList<ItemStack>(java.util.Collections.nCopies(9,ItemStack.EMPTY));for(int i:new int[]{1,3,5,7})grid.set(i,new ItemStack(Items.GOLD_NUGGET));grid.set(4,new ItemStack(Items.GLASS_PANE));var craft=CraftingInput.of(3,3,grid);var r=l.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,craft,l).orElseThrow();ok(r.value().assemble(craft,l.registryAccess()).is(MagicStudy.MONOCLE.get()),"actual crafting recipe 4 nuggets 1 pane");
  var node=dev.kelmagic.research.ResearchCatalog.BY_ID.get("monocle");ok(node!=null&&node.free(),"codex node free");ok(dev.kelmagic.research.Research.allowed(p,MagicStudy.MONOCLE.toStack()),"research gate permits craft");System.out.println("R3_SERVER_PASS helmets="+count+" component/save/duplicate/invalid/realSmithing/consumption/crafting/catalog/research");
 }
 private QaR3Checks(){}
}
