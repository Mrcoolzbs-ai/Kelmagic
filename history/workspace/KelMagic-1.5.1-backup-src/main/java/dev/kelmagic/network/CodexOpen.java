package dev.kelmagic.network;
import java.util.List;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record CodexOpen(List<String> words,List<String> notes,boolean offhand) implements CustomPacketPayload {
 public static final Type<CodexOpen> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"codex_open"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CodexOpen> CODEC=StreamCodec.of((b,p)->{
  b.writeCollection(p.words,(buf,s)->buf.writeUtf(s,8));b.writeCollection(p.notes,(buf,s)->buf.writeUtf(s,240));b.writeBoolean(p.offhand);
 },b->new CodexOpen(read(b,8),read(b,240),b.readBoolean()));
 private static List<String> read(RegistryFriendlyByteBuf b,int length){int size=b.readVarInt();if(size<0 || size>6)throw new IllegalArgumentException("Invalid codex size");var list=new java.util.ArrayList<String>();for(int i=0;i<size;i++)list.add(b.readUtf(length));return List.copyOf(list);}
 @Override public Type<CodexOpen> type(){return TYPE;}
}
