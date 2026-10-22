package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.compat.*;
import net.minecraft.world.item.*;import net.minecraft.server.level.ServerPlayer;
import top.theillusivec4.curios.api.*;
public final class QaR5CuriosSupport {
 static void ok(boolean b,String n){QaR5Curios.ok(b,n);}
 static top.theillusivec4.curios.api.type.capability.ICuriosItemHandler inventory(net.minecraft.world.entity.player.Player p){return CuriosApi.getCuriosInventory(p).orElseThrow();}
 static void equip(ServerPlayer p,int stage){var inv=inventory(p);var slots=inv.getStacksHandler(CuriosMonocle.SLOT).orElseThrow();inv.setSlotActive(CuriosMonocle.SLOT,0,true);slots.getStacks().setStackInSlot(0,ItemStack.EMPTY);
  if(stage>=2&&stage<=5||stage==10||stage==11||stage==12)slots.getStacks().setStackInSlot(0,MagicStudy.MONOCLE.toStack());
  if(stage==11)inv.setSlotActive(CuriosMonocle.SLOT,0,false);System.out.println("R5_SERVER_EQUIP stage="+stage+" "+debug(p)+" main="+p.getMainHandItem()+" off="+p.getOffhandItem()+" head="+p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD));
 }
 static void serverChecks(ServerPlayer p){
  var inv=inventory(p);var slot=inv.getStacksHandler(CuriosMonocle.SLOT).orElseThrow();ok(slot.getSlots()==1,"one dedicated slot");
  var context=new SlotContext(CuriosMonocle.SLOT,p,0,false,false);var mono=MagicStudy.MONOCLE.toStack();
  ok(CuriosApi.isStackValid(context,mono),"tag validator accepts monocle");ok(!CuriosApi.isStackValid(context,new ItemStack(Items.PAPER)),"tag validator rejects paper");
  var curio=CuriosApi.getCurio(mono).orElseThrow();ok(curio.canEquip(context),"monocle registered with API");ok(!curio.canEquip(new SlotContext(CuriosMonocle.SLOT,p,0,true,false)),"cosmetic rejected");ok(!curio.canEquip(new SlotContext("ring",p,0,false,false)),"unrelated slot rejected");ok(curio.canEquipFromUse(context),"Curios right-click opt-in");
  mono.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("R5 saved monocle"));slot.getStacks().setStackInSlot(0,mono);ok(MonocleAccessories.equipped(p),"functional slot detected");
  var saved=inv.writeTag();slot.getStacks().setStackInSlot(0,ItemStack.EMPTY);ok(!MonocleAccessories.equipped(p),"removed not detected");inv.readTag(saved);var restored=inventory(p).getStacksHandler(CuriosMonocle.SLOT).orElseThrow().getStacks().getStackInSlot(0);ok(ItemStack.isSameItemSameComponents(restored,mono),"Curios persistence roundtrip retains components");
  inventory(p).setSlotActive(CuriosMonocle.SLOT,0,false);ok(!MonocleAccessories.equipped(p),"disabled slot not functional");inventory(p).setSlotActive(CuriosMonocle.SLOT,0,true);
  slot=inventory(p).getStacksHandler(CuriosMonocle.SLOT).orElseThrow();slot.getStacks().setStackInSlot(0,ItemStack.EMPTY);p.getInventory().setItem(9,MagicStudy.MONOCLE.toStack());
  var menu=new top.theillusivec4.curios.common.inventory.container.CuriosContainer(77,p.getInventory());
  int index=-1;for(var s:menu.slots)if(s.container==p.getInventory()&&s.getContainerSlot()==9)index=s.index;ok(index>=0,"real Curios menu inventory slot");
  menu.quickMoveStack(p,index);ok(MonocleAccessories.equipped(p)&&p.getInventory().getItem(9).isEmpty(),"real Curios menu shift-equip");
  slot.getStacks().setStackInSlot(0,ItemStack.EMPTY);System.out.println("R5_CURIOS_SERVER_PASS slots/tag/registration/cosmetic/active/persistence/realMenu");
 }
 static String debug(net.minecraft.world.entity.player.Player p){var inv=inventory(p);return inv.getStacksHandler(CuriosMonocle.SLOT).map(h->"slot="+h.getStacks().getStackInSlot(0)+" active="+inv.isSlotActive(CuriosMonocle.SLOT,0)).orElse("noSlot");}
 static void open(){net.neoforged.neoforge.network.PacketDistributor.sendToServer(new top.theillusivec4.curios.common.network.client.CPacketOpenCurios(ItemStack.EMPTY));}
}
