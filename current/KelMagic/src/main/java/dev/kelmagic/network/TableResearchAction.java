package dev.kelmagic.network;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record TableResearchAction(BlockPos pos,String token,String node) implements CustomPacketPayload {
 public static final Type<TableResearchAction> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kelmagic","table_research_action"));
 public static final StreamCodec<RegistryFriendlyByteBuf,TableResearchAction> CODEC=StreamCodec.of((b,p)->{b.writeBlockPos(p.pos);b.writeUtf(p.token,36);b.writeUtf(p.node,64);},b->new TableResearchAction(b.readBlockPos(),b.readUtf(36),b.readUtf(64)));
 public Type<TableResearchAction> type(){return TYPE;}
}
