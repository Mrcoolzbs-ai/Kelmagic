package dev.kelmagic.client;
import dev.kelmagic.content.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
@EventBusSubscriber(modid="kelmagic",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ResearchContentClient {
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{InkDrawings.OPEN=h->net.minecraft.client.Minecraft.getInstance().setScreen(new InkPatternScreen(h));net.minecraft.client.renderer.Sheets.addWoodType(MagicGrove.WOOD);for(var b:java.util.List.of(MagicGrove.SAPLING,MagicGrove.LEAVES,MagicGrove.GOLDEN_LEAVES,MagicGrove.NATURAL_LEAVES,MagicGrove.NATURAL_GOLDEN_LEAVES,MagicGrove.DOOR,MagicGrove.TRAPDOOR))net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b.get(),net.minecraft.client.renderer.RenderType.cutout());net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(MagicGrove.GLASS.get(),net.minecraft.client.renderer.RenderType.translucent());});}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(InkDrawings.TYPE.get(),InkMarkRenderer::new);e.registerEntityRenderer(ThresholdContent.TYPE.get(),ThresholdPortalRenderer::new);e.registerBlockEntityRenderer(MagicGrove.SIGN_ENTITY.get(),net.minecraft.client.renderer.blockentity.SignRenderer::new);e.registerBlockEntityRenderer(MagicGrove.HANGING_ENTITY.get(),net.minecraft.client.renderer.blockentity.HangingSignRenderer::new);e.registerBlockEntityRenderer(ArchiveStorage.ENTITY.get(),ArchiveStorageRenderer::new);}
 @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(ArchiveStorage.MENU.get(),ArchiveStorageScreen::new);}
 @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item e){e.register((s,i)->i==1?net.minecraft.world.item.component.DyedItemColor.getOrDefault(s,0x169C9C)|0xFF000000:0xFFFFFFFF,ArchiveStorage.INK.get());}
}
