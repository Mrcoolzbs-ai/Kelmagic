package dev.kelmagic.network;
import net.minecraft.network.*;import net.minecraft.network.codec.StreamCodec;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.minecraft.resources.ResourceLocation;
public record InkSelect(boolean offhand,String pattern) implements CustomPacketPayload {
 public static final Type<InkSelect> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kelmagic","ink_select"));
 public static final StreamCodec<RegistryFriendlyByteBuf,InkSelect> CODEC=StreamCodec.of((b,p)->{b.writeBoolean(p.offhand);b.writeUtf(p.pattern,32);},b->new InkSelect(b.readBoolean(),b.readUtf(32)));
 @Override public Type<InkSelect> type(){return TYPE;}
}
