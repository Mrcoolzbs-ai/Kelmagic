package dev.kelmagic.client;
import dev.kelmagic.*;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MageRendering {
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(RedMageModel.LAYER,RedMageModel::layer);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(MagicCreatures.RED_MAGE.get(),RedMageRenderer::new);e.registerBlockEntityRenderer(MagicResonance.ENTITY.get(),ResonanceRenderer::new);e.registerBlockEntityRenderer(MagicStudy.TABLE_ENTITY.get(),TableBookRenderer::new);}
 private MageRendering(){}
}
