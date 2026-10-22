package dev.kelmagic.resonance;
import dev.kelmagic.research.*;import java.util.*;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.world.level.Level;
/** Native recipes are the authoritative diagrams; their outputs are blocked on ordinary grids. */
public final class InfusionRecipes {
 public static final Set<String> ITEMS=Set.of("magic_sapling","resonance_glass","magic_ingot","magic_cloth","scripter","cassette_player","dragon_heart","arcane_helmet","arcane_chestplate","arcane_leggings","arcane_boots","mage_helmet","mage_chestplate","mage_leggings","mage_boots");
 public static final int[] GRID={8,1,2,7,0,3,6,5,4};
 public static boolean uses(ItemStack s){var id=BuiltInRegistries.ITEM.getKey(s.getItem());return id.getNamespace().equals("kelmagic")&&ITEMS.contains(id.getPath());}
 public static String researchFor(String key){if(key.startsWith("arcane_"))return "arcane_armor";if(key.startsWith("mage_"))return "mage_robes";return key;}
 public static CraftingInput input(ResonanceEntity v){return CraftingInput.of(3,3,Arrays.stream(GRID).mapToObj(v.inventory::getItem).toList());}
 public static ItemStack matching(Level l,ResonanceEntity v,String research){var input=input(v);return l.getRecipeManager().getRecipes().stream().filter(r->r.value() instanceof CraftingRecipe).map(r->(CraftingRecipe)r.value()).filter(r->{var out=r.getResultItem(l.registryAccess());return uses(out)&&researchFor(BuiltInRegistries.ITEM.getKey(out.getItem()).getPath()).equals(research)&&r.matches(input,l);}).map(r->r.assemble(input,l.registryAccess())).findFirst().orElse(ItemStack.EMPTY);}
 private InfusionRecipes(){}
}
