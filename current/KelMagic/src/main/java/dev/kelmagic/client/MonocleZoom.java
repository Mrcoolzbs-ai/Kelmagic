package dev.kelmagic.client;

import dev.kelmagic.MagicStudy;
import dev.kelmagic.archive.MonocleSmithingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Presentation only: no options mutation, camera translation or interaction-range extension. */
@EventBusSubscriber(modid="kelmagic",value=Dist.CLIENT)
public final class MonocleZoom {
 private static float previous,amount;
 private static net.minecraft.client.multiplayer.ClientLevel world;
 public static boolean equipped(net.minecraft.world.entity.player.Player p){return p.getMainHandItem().is(MagicStudy.MONOCLE.get())||p.getOffhandItem().is(MagicStudy.MONOCLE.get())||MonocleSmithingRecipe.installed(p.getItemBySlot(EquipmentSlot.HEAD))||dev.kelmagic.compat.MonocleAccessories.equipped(p);}
 public static boolean eligible(){var m=Minecraft.getInstance();return m.player!=null&&m.level!=null&&m.player.isAlive()&&m.screen==null&&m.options.getCameraType().isFirstPerson()&&m.getCameraEntity()==m.player&&equipped(m.player);}
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){var m=Minecraft.getInstance();if(world!=m.level||!eligible()){world=m.level;previous=amount=0;return;}previous=amount;boolean aim=TableBookClient.hit()!=null;amount+=( (aim?1:0)-amount)*.45f;if(Math.abs(amount-(aim?1:0))<.001f)amount=aim?1:0;}
 @SubscribeEvent public static void fov(ViewportEvent.ComputeFov event){if(!event.usedConfiguredFov()||!eligible())return;float t=(float)Math.clamp(event.getPartialTick(),0,1);double blend=previous+(amount-previous)*t;event.setFOV(event.getFOV()+(Math.min(30,event.getFOV())-event.getFOV())*blend);}
 @SubscribeEvent public static void tooltip(ItemTooltipEvent event){if(MonocleSmithingRecipe.installed(event.getItemStack())||event.getItemStack().is(MagicStudy.MONOCLE.get()))event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.kelmagic.monocle").withStyle(net.minecraft.ChatFormatting.AQUA));}
 @SubscribeEvent public static void accessoryTooltip(ItemTooltipEvent event){if(dev.kelmagic.compat.MonocleAccessories.loaded()&&event.getItemStack().is(MagicStudy.MONOCLE.get()))event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.kelmagic.monocle.curios").withStyle(net.minecraft.ChatFormatting.GRAY));}
 private MonocleZoom(){}
}
