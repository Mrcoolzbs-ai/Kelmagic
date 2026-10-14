package dev.kelmagic;
import dev.kelmagic.entity.RedMage;import net.minecraft.core.registries.Registries;import net.minecraft.world.entity.*;import net.minecraft.world.item.Item;import net.neoforged.neoforge.registries.*;import net.neoforged.bus.api.*;import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=KelMagic.MODID,bus=EventBusSubscriber.Bus.MOD)
public final class MagicCreatures {
 public static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,KelMagic.MODID);
 public static final DeferredHolder<EntityType<?>,EntityType<RedMage>> RED_MAGE=TYPES.register("red_mage",()->EntityType.Builder.of(RedMage::new,MobCategory.CREATURE).sized(.75f,2.5f).eyeHeight(1.75f).clientTrackingRange(8).build("kelmagic:red_mage"));
 public static final DeferredHolder<EntityType<?>,EntityType<dev.kelmagic.entity.ThroneSeat>> THRONE_SEAT=TYPES.register("throne_seat",()->EntityType.Builder.<dev.kelmagic.entity.ThroneSeat>of(dev.kelmagic.entity.ThroneSeat::new,MobCategory.MISC).sized(.01f,.01f).clientTrackingRange(8).updateInterval(20).build("kelmagic:throne_seat"));
 public static final DeferredItem<Item> MAGE_EGG=KelMagic.ITEMS.register("red_mage_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(RED_MAGE,0x184144,0xA3342E,new Item.Properties()));
 @SubscribeEvent public static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e){e.put(RED_MAGE.get(),RedMage.attributes().build());}
 private MagicCreatures(){}
}
