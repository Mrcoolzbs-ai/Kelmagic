package dev.kelmagic.artifact;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public final class ArtifactCurios {
    static void setup(FMLCommonSetupEvent event){event.enqueueWork(()->ArtifactContent.SLOTS.forEach((id,slot)->CuriosApi.registerCurio(ArtifactContent.ITEMS.get(id).get(),new ICurioItem(){
        @Override public boolean canEquip(SlotContext c,ItemStack stack){return !c.cosmetic()&&slot.equals(c.identifier())&&CuriosApi.getCuriosInventory(c.entity()).map(inv->inv.findFirstCurio(stack.getItem()).isEmpty()).orElse(false);}
        @Override public boolean canEquipFromUse(SlotContext c,ItemStack s){return canEquip(c,s);}
    })));}
    static ItemStack stack(LivingEntity e,Item i){return CuriosApi.getCuriosInventory(e).map(inv->{for(var entry:inv.getCurios().entrySet()){var handler=entry.getValue();for(int n=0;n<handler.getStacks().getSlots();n++){var s=handler.getStacks().getStackInSlot(n);if(inv.isSlotActive(entry.getKey(),n)&&s.is(i))return s;}}return ItemStack.EMPTY;}).orElse(ItemStack.EMPTY);}
    private ArtifactCurios(){}
}
