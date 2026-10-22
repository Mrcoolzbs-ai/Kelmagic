package dev.kelmagic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.*;
public final class MagicSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,KelMagic.MODID);
    public static final DeferredHolder<SoundEvent,SoundEvent> WORD=SOUNDS.register("word",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"word")));
    public static final DeferredHolder<SoundEvent,SoundEvent> THRESHOLD_OPEN=register("threshold_open"),THRESHOLD_CLOSE=register("threshold_close"),THRESHOLD_TRAVEL=register("threshold_travel"),THRESHOLD_HUM=register("threshold_hum");
    private static DeferredHolder<SoundEvent,SoundEvent> register(String name){return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,name)));}
    private MagicSounds() {}
}
