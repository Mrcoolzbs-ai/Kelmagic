package dev.kelmagic.network;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** Entity id only; all visuals installed by the physical client. */
public record HeartActivation(int entityId) implements CustomPacketPayload {
    public static final Type<HeartActivation> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"heart_activation"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HeartActivation> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.entityId),b->new HeartActivation(b.readVarInt()));
    @Override public Type<HeartActivation> type(){return TYPE;}
}
