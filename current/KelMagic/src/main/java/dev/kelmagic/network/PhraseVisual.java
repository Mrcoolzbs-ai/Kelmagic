package dev.kelmagic.network;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
/** A bounded server-to-client visual cue; never authorizes gameplay. */
public record PhraseVisual(int kind,double x,double y,double z,float scale,int seed,int entityId) implements CustomPacketPayload {
 public static final Type<PhraseVisual> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"phrase_visual"));
 public static final StreamCodec<RegistryFriendlyByteBuf,PhraseVisual> CODEC=StreamCodec.of((b,p)->{b.writeByte(p.kind);b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);b.writeFloat(p.scale);b.writeInt(p.seed);b.writeVarInt(p.entityId);},b->new PhraseVisual(b.readUnsignedByte(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat(),b.readInt(),b.readVarInt()));
 public boolean valid(){return kind>=0&&kind<3&&Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z)&&Math.abs(x)<=30000000&&Math.abs(z)<=30000000&&Math.abs(y)<=2048&&Float.isFinite(scale)&&scale>=.1f&&scale<=2;}
 @Override public Type<PhraseVisual> type(){return TYPE;}
}
