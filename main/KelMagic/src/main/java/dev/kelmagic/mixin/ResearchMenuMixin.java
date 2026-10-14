package dev.kelmagic.mixin;
import net.minecraft.world.inventory.*;import net.minecraft.world.entity.player.Player;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AbstractContainerMenu.class)public abstract class ResearchMenuMixin {
 @Inject(method="clicked",at=@At("HEAD"),cancellable=true)private void take(int slot,int button,ClickType type,Player p,CallbackInfo ci){var menu=(AbstractContainerMenu)(Object)this;if(slot<0||slot>=menu.slots.size())return;boolean output=menu.getSlot(slot) instanceof ResultSlot||menu instanceof StonecutterMenu&&slot==1;if(output&&!dev.kelmagic.research.Research.allowed(p,menu.getSlot(slot).getItem())){menu.getSlot(slot).set(net.minecraft.world.item.ItemStack.EMPTY);menu.broadcastFullState();ci.cancel();}}
}
