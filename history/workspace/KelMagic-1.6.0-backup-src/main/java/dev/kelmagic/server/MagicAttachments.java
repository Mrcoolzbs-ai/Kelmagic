package dev.kelmagic.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.kelmagic.KelMagic;
import dev.kelmagic.core.SpellSession;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class MagicAttachments {
    public record Mana(int current, boolean dragonUpgrade) {
        public static final Codec<Mana> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 1024).fieldOf("current").forGetter(Mana::current),
            Codec.BOOL.optionalFieldOf("dragon_upgrade", false).forGetter(Mana::dragonUpgrade)
        ).apply(i, Mana::new));
        public Mana withCurrent(int amount) { return new Mana(Math.max(0, Math.min(1024,amount)), dragonUpgrade); }
    }
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KelMagic.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Mana>> MANA = TYPES.register("mana",
        () -> AttachmentType.builder(() -> new Mana(5,false)).serialize(Mana.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> ARCANE_BURN=TYPES.register("arcane_burn",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,3600)).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Float>> ARCANE_POWER=TYPES.register("arcane_power",()->AttachmentType.builder(()->1f).serialize(Codec.floatRange(.01f,16f)).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Float>> FLAME_HEAL=TYPES.register("flame_heal",()->AttachmentType.builder(()->2f).serialize(Codec.floatRange(.1f,24f)).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpellSession>> SESSION = TYPES.register("spell_session",
        () -> AttachmentType.builder(SpellSession::new).build());
    private MagicAttachments() {}
}
