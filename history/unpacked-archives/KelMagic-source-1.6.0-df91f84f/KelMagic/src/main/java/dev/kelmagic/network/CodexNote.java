package dev.kelmagic.network;
import dev.kelmagic.*;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record CodexNote(boolean offhand,String word,String note) implements CustomPacketPayload {
 public static final Type<CodexNote> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"codex_note"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CodexNote> CODEC=StreamCodec.of((b,p)->{b.writeBoolean(p.offhand);b.writeUtf(p.word,8);b.writeUtf(p.note,240);},b->new CodexNote(b.readBoolean(),b.readUtf(8),b.readUtf(240)));
 @Override public Type<CodexNote> type(){return TYPE;}
 public static void save(ServerPlayer p,CodexNote packet){
  if(!p.isAlive())return;var s=p.getItemInHand(packet.offhand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
  if(!(s.getItem() instanceof dev.kelmagic.item.CodexItem) || !s.getOrDefault(MagicComponents.CODEX_WORDS.get(),List.<String>of()).contains(packet.word))return;
  var keys=new ArrayList<>(dev.kelmagic.core.KelLexicon.WORDS.keySet());int index=keys.indexOf(packet.word);if(index<0 || packet.note.length()>240)return;
  var notes=new ArrayList<>(s.getOrDefault(MagicComponents.CODEX_NOTES.get(),List.<String>of()));while(notes.size()<keys.size())notes.add("");
  notes.set(index,packet.note.replaceAll("[\\p{Cntrl}]"," "));s.set(MagicComponents.CODEX_NOTES.get(),List.copyOf(notes));
 }
}
