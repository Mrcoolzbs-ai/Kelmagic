package dev.kelmagic.content;
import dev.kelmagic.entity.ThresholdPortal;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
@EventBusSubscriber(modid="kelmagic",bus=EventBusSubscriber.Bus.MOD)
public final class ThresholdContent {
 public static final DeferredHolder<EntityType<?>,EntityType<ThresholdPortal>> TYPE=dev.kelmagic.MagicCreatures.TYPES.register("threshold_portal",()->EntityType.Builder.<ThresholdPortal>of(ThresholdPortal::new,MobCategory.MISC).sized(.9f,2f).clientTrackingRange(10).updateInterval(10).build("kelmagic:threshold_portal"));
 public static void init(){}
 @SubscribeEvent public static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e){e.put(TYPE.get(),LivingEntity.createLivingAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,1).build());}
}
