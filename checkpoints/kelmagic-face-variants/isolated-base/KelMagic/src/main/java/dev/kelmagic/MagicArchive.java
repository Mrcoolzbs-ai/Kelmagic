package dev.kelmagic;
import java.util.*;
import dev.kelmagic.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.*;
public final class MagicArchive {
 public static final DeferredRegister<RecipeSerializer<?>> RECIPES=DeferredRegister.create(Registries.RECIPE_SERIALIZER,KelMagic.MODID);
 public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<ArchiveRecipe>> ADD=RECIPES.register("add_manuscript",()->new SimpleCraftingRecipeSerializer<>(ArchiveRecipe::new));
 public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<CodexCopyRecipe>> COPY=RECIPES.register("copy_codex",()->new SimpleCraftingRecipeSerializer<>(CodexCopyRecipe::new));
 public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<dev.kelmagic.audio.DecorateTapeRecipe>> DECORATE=RECIPES.register("decorate_tape",()->new SimpleCraftingRecipeSerializer<>(dev.kelmagic.audio.DecorateTapeRecipe::new));
 public static final DeferredItem<CodexItem> CREATIVE_CODEX=KelMagic.ITEMS.register("creative_archivists_codex",()->new CodexItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).component(MagicComponents.CODEX_WORDS.get(),List.copyOf(dev.kelmagic.core.KelLexicon.WORDS.keySet()))));
 public static final DeferredItem<CodexItem> CODEX=KelMagic.ITEMS.register("archivists_codex",()->new CodexItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(MagicComponents.CODEX_WORDS.get(),List.of())));
 public static final DeferredItem<AncientManuscriptItem> ANCIENT=KelMagic.ITEMS.register("ancient_manuscript",()->new AncientManuscriptItem(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
 public static final Map<String,DeferredItem<WordManuscriptItem>> PAGES=new LinkedHashMap<>();
 static {for(String w:dev.kelmagic.core.KelLexicon.WORDS.keySet()) PAGES.put(w,KelMagic.ITEMS.register("manuscript_"+w,()->new WordManuscriptItem(new Item.Properties().stacksTo(1).rarity(dev.kelmagic.core.WordRarity.of(w)),w)));}
 private MagicArchive(){}
}
