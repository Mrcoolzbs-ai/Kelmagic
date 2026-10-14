package dev.kelmagic.network;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record TapeSelect(int step) implements CustomPacketPayload {
 public static final Type<TapeSelect> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"tape_select"));
 public static final StreamCodec<RegistryFriendlyByteBuf,TapeSelect> CODEC=StreamCodec.of((b,p)->b.writeByte(p.step),b->new TapeSelect(b.readByte()));
 @Override public Type<TapeSelect> type(){return TYPE;}
}
