package dev.kelmagic.mixin;
import dev.kelmagic.*;import dev.kelmagic.server.*;import net.minecraft.world.entity.monster.piglin.*;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(PiglinAi.class)
public abstract class PiglinWoolMixin {
 @Inject(method="stopHoldingOffHandItem",at=@At("HEAD"))private static void kelmagic$currency(Piglin p,boolean barter,CallbackInfo ci){p.setData(MagicAttachments.GOLDEN_EXCHANGE,p.getOffhandItem().is(MagicMaterials.WOOL_ITEM));}
 @Inject(method="getBarterResponseItems",at=@At("HEAD"),cancellable=true)private static void kelmagic$rewards(Piglin p,CallbackInfoReturnable<java.util.List<ItemStack>> ci){if(p.getData(MagicAttachments.GOLDEN_EXCHANGE))ci.setReturnValue(WoolBarter.rewards(p));}
 @Inject(method="stopHoldingOffHandItem",at=@At("RETURN"))private static void kelmagic$reset(Piglin p,boolean barter,CallbackInfo ci){p.setData(MagicAttachments.GOLDEN_EXCHANGE,false);}
}