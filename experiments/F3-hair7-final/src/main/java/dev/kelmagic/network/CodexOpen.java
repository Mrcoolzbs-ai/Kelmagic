package dev.kelmagic.network;
import java.util.List;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record CodexOpen(List<String> words,List<String> notes,boolean offhand,int section,int page,String query,String token,int sort,boolean knownOnly) implements CustomPacketPayload {
 public CodexOpen(List<String> w,List<String> n,boolean h){this(w,n,h,0,0,"","",0,false);}
 public CodexOpen(List<String> w,List<String> n,boolean h,int s,int p,String q,String t){this(w,n,h,s,p,q,t,0,false);}
 public static final Type<CodexOpen> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"codex_open"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CodexOpen> CODEC=StreamCodec.of((b,p)->{
  b.writeCollection(p.words,(buf,s)->buf.writeUtf(s,8));b.writeCollection(p.notes,(buf,s)->buf.writeUtf(s,240));b.writeBoolean(p.offhand);b.writeVarInt(p.section);b.writeVarInt(p.page);b.writeUtf(p.query,40);b.writeUtf(p.token,36);b.writeVarInt(p.sort);b.writeBoolean(p.knownOnly);
 },b->new CodexOpen(read(b,8),read(b,240),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readUtf(40),b.readUtf(36),b.readVarInt(),b.readBoolean()));
 private static List<String> read(RegistryFriendlyByteBuf b,int length){int size=b.readVarInt();if(size<0 || size>dev.kelmagic.core.KelLexicon.CODEX_LIMIT)throw new IllegalArgumentException("Invalid codex size");var list=new java.util.ArrayList<String>();for(int i=0;i<size;i++)list.add(b.readUtf(length));return List.copyOf(list);}
 @Override public Type<CodexOpen> type(){return TYPE;}
}
