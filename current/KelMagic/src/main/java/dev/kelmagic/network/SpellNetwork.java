package dev.kelmagic.network;

import dev.kelmagic.server.MagicServer;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client hook installed only by the physical client entry point; no client class linkage here. */
public final class SpellNetwork {
    public static Consumer<SpellSnapshot> CLIENT_SINK = snapshot -> {};
    public static Consumer<ResearchNotice> RESEARCH_NOTICE=packet->{};
    public static Consumer<ResearchSnapshot> RESEARCH_SINK=packet->{};
    public static Consumer<CodexOpen> CODEX_SINK = packet -> {};
    public static Consumer<BurnSnapshot> BURN_SINK = packet -> {};
    public static Consumer<HeartActivation> HEART_SINK = packet -> {};
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("16-table-horizontal");
        registrar.playToClient(ResearchNotice.TYPE,ResearchNotice.CODEC,(packet,context)->context.enqueueWork(()->RESEARCH_NOTICE.accept(packet)));
        registrar.playToClient(ResearchSnapshot.TYPE,ResearchSnapshot.CODEC,(packet,context)->context.enqueueWork(()->RESEARCH_SINK.accept(packet)));
        registrar.playToServer(TableResearchAction.TYPE,TableResearchAction.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p)dev.kelmagic.research.Research.beginAtTable(p,packet.pos(),packet.token(),packet.node());}));
        registrar.playToServer(ResearchAction.TYPE,ResearchAction.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p)dev.kelmagic.research.Research.begin(p,packet.node(),packet.offhand(),packet.token());}));
        registrar.playToServer(CodexPosition.TYPE,CodexPosition.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p)CodexPosition.save(p,packet);}));
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
