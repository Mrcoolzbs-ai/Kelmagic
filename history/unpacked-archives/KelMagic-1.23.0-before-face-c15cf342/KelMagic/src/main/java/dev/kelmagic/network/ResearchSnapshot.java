package dev.kelmagic.network;
import dev.kelmagic.research.ResearchState;import net.minecraft.network.*;import net.minecraft.network.codec.*;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.minecraft.resources.ResourceLocation;import java.util.*;
public record ResearchSnapshot(ResearchState state) implements CustomPacketPayload {
 public static final Type<ResearchSnapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kelmagic","research_snapshot"));
 static void list(RegistryFriendlyByteBuf b,List<String> s){b.writeCollection(s,(buf,v)->buf.writeUtf(v,96));}static List<String> read(RegistryFriendlyByteBuf b){int n=b.readVarInt();if(n<0||n>512)throw new IllegalArgumentException("research length");var out=new ArrayList<String>();for(int i=0;i<n;i++)out.add(b.readUtf(96));return List.copyOf(out);}
 public static final StreamCodec<RegistryFriendlyByteBuf,ResearchSnapshot> CODEC=StreamCodec.of((b,p)->{list(b,p.state.learned());list(b,p.state.tabs());list(b,p.state.started());list(b,p.state.evidence());},b->new ResearchSnapshot(new ResearchState(read(b),read(b),read(b),read(b))));public Type<ResearchSnapshot> type(){return TYPE;}
}
