package dev.kelmagic.network;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record BurnSnapshot(int entityId,int ticks) implements CustomPacketPayload {
 public static final Type<BurnSnapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"arcane_burn"));
 public static final StreamCodec<RegistryFriendlyByteBuf,BurnSnapshot> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.entityId);b.writeVarInt(p.ticks);},b->new BurnSnapshot(b.readVarInt(),Math.max(0,Math.min(3600,b.readVarInt()))));
 @Override public Type<BurnSnapshot> type(){return TYPE;}
}
