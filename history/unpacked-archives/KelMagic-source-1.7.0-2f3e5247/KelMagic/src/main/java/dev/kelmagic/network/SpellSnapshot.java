package dev.kelmagic.network;

import dev.kelmagic.KelMagic;
import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpellSnapshot(UUID player, long generation, int revision, int mana, int maximum,
                            List<String> words, int kind) implements CustomPacketPayload {
    public static final int SYNC=0, ACCEPT=1, END=2;
    public SpellSnapshot { words = List.copyOf(words); }
    public static final Type<SpellSnapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"spell_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellSnapshot> CODEC = StreamCodec.of(
        (buf,p) -> {
            buf.writeUUID(p.player); buf.writeLong(p.generation); buf.writeVarInt(p.revision);
            buf.writeVarInt(p.mana); buf.writeVarInt(p.maximum); buf.writeVarInt(p.kind);
            buf.writeVarInt(p.words.size()); for(String w:p.words) buf.writeUtf(w,8);
        }, buf -> {
            UUID player=buf.readUUID(); long token=buf.readLong(); int revision=buf.readVarInt();
            int mana=buf.readVarInt(), maximum=buf.readVarInt(), kind=buf.readVarInt(), size=buf.readVarInt();
            if(size<0 || size>100 || revision<0 || revision>100 || mana<0 || maximum<5 || maximum>1024 || mana>maximum || kind<0 || kind>2)
                throw new IllegalArgumentException("Invalid spell snapshot");
            List<String> words=new ArrayList<>(); for(int i=0;i<size;i++) words.add(buf.readUtf(8));
            return new SpellSnapshot(player,token,revision,mana,maximum,words,kind);
        });
    @Override public Type<SpellSnapshot> type() { return TYPE; }
}
