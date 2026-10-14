package dev.kelmagic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.*;
public final class MagicSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,KelMagic.MODID);
    public static final DeferredHolder<SoundEvent,SoundEvent> WORD=SOUNDS.register("word",()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"word")));
    private MagicSounds() {}
}
