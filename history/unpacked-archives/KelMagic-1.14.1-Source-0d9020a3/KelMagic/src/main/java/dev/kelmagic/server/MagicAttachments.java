package dev.kelmagic.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.kelmagic.KelMagic;
import dev.kelmagic.core.SpellSession;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class MagicAttachments {
    public record Mana(double current, boolean dragonUpgrade) {
        public Mana {current=Double.isFinite(current)?Math.clamp(current,0,1024):0;}
        public static final Codec<Mana> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.doubleRange(0, 1024).fieldOf("current").forGetter(Mana::current),
            Codec.BOOL.optionalFieldOf("dragon_upgrade", false).forGetter(Mana::dragonUpgrade)
        ).apply(i, Mana::new));
        public Mana withCurrent(double amount) { return new Mana(Double.isFinite(amount)?Math.max(0, Math.min(1024,amount)):0, dragonUpgrade); }
    }
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KelMagic.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Mana>> MANA = TYPES.register("mana",
        () -> AttachmentType.builder(() -> new Mana(5,false)).serialize(Mana.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> ARCANE_BURN=TYPES.register("arcane_burn",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,3600)).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Float>> ARCANE_POWER=TYPES.register("arcane_power",()->AttachmentType.builder(()->1f).serialize(Codec.floatRange(.01f,16f)).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Float>> FLAME_HEAL=TYPES.register("flame_heal",()->AttachmentType.builder(()->2f).serialize(Codec.floatRange(.1f,24f)).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpellSession>> SESSION = TYPES.register("spell_session",
        () -> AttachmentType.builder(SpellSession::new).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> NEUTRAL_KIND=TYPES.register("neutral_kind",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,2)).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> NEUTRAL_ANGER=TYPES.register("neutral_anger",()->AttachmentType.builder(()->0L).serialize(Codec.LONG).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> NEUTRAL_INSTALLED=TYPES.register("neutral_installed",()->AttachmentType.builder(()->false).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> BARTER_READY=TYPES.register("golden_barter_ready",()->AttachmentType.builder(()->0L).serialize(Codec.LONG).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> GOLDEN_EXCHANGE=TYPES.register("golden_exchange",()->AttachmentType.builder(()->false).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> MANA_BOOST_END=TYPES.register("mana_boost_end",()->AttachmentType.builder(()->0L).serialize(Codec.LONG).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> CREATIVE_PREVIOUS=TYPES.register("creative_previous",()->AttachmentType.builder(()->-1).serialize(Codec.intRange(-1,3)).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> CREATIVE_END=TYPES.register("creative_end",()->AttachmentType.builder(()->0L).serialize(Codec.LONG).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Double>> BASE_MANA_MAX=TYPES.register("base_mana_max",()->AttachmentType.builder(()->-1d).serialize(Codec.doubleRange(-1,974)).copyOnDeath().build());
    private MagicAttachments() {}
}
