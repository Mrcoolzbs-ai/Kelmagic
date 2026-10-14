package dev.kelmagic.network;
import net.minecraft.network.*;import net.minecraft.network.codec.*;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.minecraft.resources.ResourceLocation;
public record ResearchNotice(String id) implements CustomPacketPayload {
 public static final Type<ResearchNotice> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kelmagic","research_notice"));public static final StreamCodec<RegistryFriendlyByteBuf,ResearchNotice> CODEC=StreamCodec.of((b,p)->b.writeUtf(p.id,64),b->new ResearchNotice(b.readUtf(64)));public Type<ResearchNotice> type(){return TYPE;}
}
