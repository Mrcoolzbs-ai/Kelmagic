package dev.kelmagic.network;

import dev.kelmagic.server.MagicServer;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client hook installed only by the physical client entry point; no client class linkage here. */
public final class SpellNetwork {
    public static Consumer<SpellSnapshot> CLIENT_SINK = snapshot -> {};
    public static Consumer<CodexOpen> CODEX_SINK = packet -> {};
    public static Consumer<BurnSnapshot> BURN_SINK = packet -> {};
    public static Consumer<HeartActivation> HEART_SINK = packet -> {};
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("4");
        registrar.playToServer(TapeSelect.TYPE,TapeSelect.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p && p.isShiftKeyDown())dev.kelmagic.audio.TapePlayerItem.select(p,packet.step());}));
        registrar.playToClient(BurnSnapshot.TYPE,BurnSnapshot.CODEC,(packet,context)->context.enqueueWork(()->BURN_SINK.accept(packet)));
        registrar.playToServer(CodexNote.TYPE,CodexNote.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p)CodexNote.save(p,packet);}));
        registrar.playToClient(CodexOpen.TYPE,CodexOpen.CODEC,(packet,context)->context.enqueueWork(()->CODEX_SINK.accept(packet)));
        registrar.playToClient(HeartActivation.TYPE,HeartActivation.CODEC,(packet,context)->context.enqueueWork(()->HEART_SINK.accept(packet)));
        registrar.playToServer(SpellAction.TYPE,SpellAction.CODEC,(packet,context) -> context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer p) MagicServer.action(p,packet);
        }));
        registrar.playToClient(SpellSnapshot.TYPE,SpellSnapshot.CODEC,(packet,context) -> context.enqueueWork(() -> CLIENT_SINK.accept(packet)));
    }
    private SpellNetwork() {}
}
