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
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> ACTIVE_TAPE=TYPES.register("active_tape",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,7)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> TAPE_PATTERN=TYPES.register("tape_pattern",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> CODEX_PAGE=TYPES.register("codex_page",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,179)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    // Welcome (7) and infusion (8) are persisted pages too. The old 0..6 codec crashed while saving either page.
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> CODEX_SECTION=TYPES.register("codex_section",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,8)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> CODEX_QUERY=TYPES.register("codex_query",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> CODEX_TOKEN=TYPES.register("codex_token",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> RESEARCH_ID=TYPES.register("research_id",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> RESEARCH_OWNER=TYPES.register("research_owner",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Boolean>> CODEX_WELCOMED=TYPES.register("codex_welcomed",()->DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> CODEX_SORT=TYPES.register("codex_sort",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,1)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Boolean>> CODEX_KNOWN=TYPES.register("codex_known_only",()->DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Boolean>> MONOCLE=TYPES.register("monocle",()->DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> INK_PATTERN=TYPES.register("ink_pattern",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    private MagicComponents() {}
}
