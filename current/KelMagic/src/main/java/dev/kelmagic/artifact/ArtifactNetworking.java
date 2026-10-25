package dev.kelmagic.artifact;
import dev.kelmagic.KelMagic;
import dev.kelmagic.server.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
@EventBusSubscriber(modid=KelMagic.MODID,bus=EventBusSubscriber.Bus.MOD)
public final class ArtifactNetworking {
    public record Action(int action) implements CustomPacketPayload {
        public static final Type<Action> TYPE=new Type<>(ArtifactContent.id("artifact_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((b,v)->b.writeVarInt(v.action),b->new Action(b.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(Action.TYPE,Action.CODEC,(packet,ctx)->ctx.enqueueWork(()->{if(ctx.player() instanceof ServerPlayer p)activate(p,packet.action);}));}
    public static void activate(ServerPlayer p,int action){if(action<0||action>4||!p.isAlive()||p.isSpectator())return;long now=p.serverLevel().getGameTime();var state=ArtifactRuntime.state(p);if(now<state.lastAction+5)return;state.lastAction=now;
        if(action==0||action==1){var stack=ArtifactAccessories.stack(p,ArtifactContent.RESERVE.get());if(stack.isEmpty())return;double stored=ArtifactData.charge(stack,10);if(action==1){double n=ArtifactRules.deposit(stored,Math.min(1,p.getData(MagicAttachments.MANA).current()),10);if(!ArtifactRuntime.spend(p,n))return;stored+=n;}else{double n=Math.min(1,Math.min(stored,MagicServer.maximum(p)-p.getData(MagicAttachments.MANA).current()));if(n<=0)return;stored-=n;ArtifactRuntime.addMana(p,n);}ArtifactData.charge(stack,stored,10);ArtifactVisuals.sound(p.serverLevel(),p.position(),"mana_transfer",action==0?1.15f:.85f);MagicServer.sync(p);}
        else if(action==2){var stack=ArtifactAccessories.stack(p,ArtifactContent.DEW.get());if(stack.isEmpty()||ArtifactData.charge(stack,1)<1)return;for(var effect:java.util.List.copyOf(p.getActiveEffects()))if(!effect.getEffect().value().isBeneficial())p.removeEffect(effect.getEffect());ArcaneFire.extinguish(p);p.clearFire();p.getPersistentData().remove("KelMuteUntil");p.getPersistentData().remove("KelRootUntil");p.getPersistentData().remove("KelHeavyUntil");ArtifactData.charge(stack,0,1);ArtifactVisuals.ring(p.serverLevel(),p.position().add(0,.3,0),1,0x88ebc3,30);ArtifactVisuals.sound(p.serverLevel(),p.position(),"artifact_chime",1.2f);}
        else if(action==3)ArtifactWorld.sample(p);
        else if(ArtifactRuntime.pieces(p,"steady_anchor")==4&&p.onGround()&&!p.isPassenger()){boolean anchored=p.getPersistentData().getBoolean("KelAnchored");p.getPersistentData().putBoolean("KelAnchored",!anchored);if(!anchored){p.getPersistentData().putDouble("KelAnchorX",p.getX());p.getPersistentData().putDouble("KelAnchorY",p.getY());p.getPersistentData().putDouble("KelAnchorZ",p.getZ());}ArtifactVisuals.sound(p.serverLevel(),p.position(),"root_bind",.7f);}
    }
    private ArtifactNetworking(){}
}
