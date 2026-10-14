package dev.kelmagic.network;
import net.minecraft.network.*;import net.minecraft.network.codec.*;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.minecraft.resources.ResourceLocation;
public record ResearchAction(boolean offhand,String token,String node) implements CustomPacketPayload {
 public static final Type<ResearchAction> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kelmagic","research_action"));public static final StreamCodec<RegistryFriendlyByteBuf,ResearchAction> CODEC=StreamCodec.of((b,p)->{b.writeBoolean(p.offhand);b.writeUtf(p.token,36);b.writeUtf(p.node,64);},b->new ResearchAction(b.readBoolean(),b.readUtf(36),b.readUtf(64)));public Type<ResearchAction> type(){return TYPE;}
}
