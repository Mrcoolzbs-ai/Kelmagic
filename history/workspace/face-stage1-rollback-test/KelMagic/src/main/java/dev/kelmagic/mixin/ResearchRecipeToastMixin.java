package dev.kelmagic.mixin;
import net.minecraft.client.gui.components.toasts.*;import net.minecraft.world.item.crafting.RecipeHolder;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Keep vanilla recipe book updates, but no duplicate toast for our discoveries. */
@Mixin(RecipeToast.class)public abstract class ResearchRecipeToastMixin {
 @Inject(method="addOrUpdate",at=@At("HEAD"),cancellable=true)private static void kelmagic$quiet(ToastComponent c,RecipeHolder<?> recipe,CallbackInfo ci){if(recipe.id().getNamespace().equals("kelmagic"))ci.cancel();}
}
