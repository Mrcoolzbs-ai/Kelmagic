package dev.kelmagic;
import java.util.List;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.*;
public final class MagicComponents {
    public static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,KelMagic.MODID);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<String>>> SPELL=TYPES.register("recorded_spell",()->DataComponentType.<List<String>>builder()
        .persistent(Codec.STRING.listOf(0,100))
        .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(100))).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Boolean>> RECORDING=TYPES.register("scripter_recording",()->DataComponentType.<Boolean>builder()
        .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<String>>> CANDIDATE=TYPES.register("scripter_candidate",()->DataComponentType.<List<String>>builder()
        .persistent(Codec.STRING.listOf(0,100)).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(100))).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<String>>> CODEX_WORDS=TYPES.register("codex_words",()->DataComponentType.<List<String>>builder()
        .persistent(Codec.STRING.listOf(0,dev.kelmagic.core.KelLexicon.CODEX_LIMIT)).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(dev.kelmagic.core.KelLexicon.CODEX_LIMIT))).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<String>>> CODEX_NOTES=TYPES.register("codex_notes",()->DataComponentType.<List<String>>builder()
        .persistent(Codec.STRING.listOf(0,dev.kelmagic.core.KelLexicon.CODEX_LIMIT)).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(dev.kelmagic.core.KelLexicon.CODEX_LIMIT))).build());
    private MagicComponents() {}
}
