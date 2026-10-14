package dev.kelmagic.client;
import dev.kelmagic.*;import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD) public final class LampRendering {
 @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(MagicPortals.GATE_ENTITY.get(),net.minecraft.client.renderer.blockentity.TheEndPortalRenderer::new);}
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(MagicLamps.TORCH.get(),net.minecraft.client.renderer.RenderType.cutout());net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(MagicLamps.WALL_TORCH.get(),net.minecraft.client.renderer.RenderType.cutout());});}
}
