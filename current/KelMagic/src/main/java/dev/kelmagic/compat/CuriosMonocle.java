package dev.kelmagic.compat;

import dev.kelmagic.MagicStudy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Loaded only when Curios is present. Uses public API; no renderer or copied implementation. */
public final class CuriosMonocle {
 public static final String SLOT="kelmagic_monocle";
 public static void setup(FMLCommonSetupEvent event){event.enqueueWork(()->CuriosApi.registerCurio(MagicStudy.MONOCLE.get(),new ICurioItem(){
  @Override public boolean canEquip(SlotContext context,ItemStack stack){return !context.cosmetic()&&SLOT.equals(context.identifier())&&stack.is(MagicStudy.MONOCLE.get());}
  @Override public boolean canEquipFromUse(SlotContext context,ItemStack stack){return canEquip(context,stack);}
 }));}
 public static boolean equipped(Player player){
  return CuriosApi.getCuriosInventory(player).map(inventory->inventory.getStacksHandler(SLOT).map(handler->{
   var stacks=handler.getStacks();
   for(int i=0;i<stacks.getSlots();i++)if(inventory.isSlotActive(SLOT,i)&&stacks.getStackInSlot(i).is(MagicStudy.MONOCLE.get()))return true;
   return false;
  }).orElse(false)).orElse(false);
 }
 private CuriosMonocle(){}
}
