package dev.kelmagic.network;

import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** No words or costs from the client: only an action against an acknowledged server revision. */
public record SpellAction(long generation, int revision, boolean cancel) implements CustomPacketPayload {
    public static final Type<SpellAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"spell_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellAction> CODEC = StreamCodec.of(
        (buf,p) -> { buf.writeLong(p.generation); buf.writeVarInt(p.revision); buf.writeBoolean(p.cancel); },
        buf -> new SpellAction(buf.readLong(),buf.readVarInt(),buf.readBoolean()));
    @Override public Type<SpellAction> type() { return TYPE; }
}
