/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.server.ServerStoppingEvent
 *  net.neoforged.neoforge.event.tick.EntityTickEvent$Pre
 */
package dev.kelmagic.client.diag;

import dev.kelmagic.client.diag.FixtureHoldCounters;
import dev.kelmagic.client.diag.FpsDiagnostics;
import dev.kelmagic.entity.RedMage;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid="kelmagic", value={Dist.CLIENT})
public final class DiagnosticFixture {
    private static volatile Saved saved;

    private static void say(String text) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.displayClientMessage((Component)Component.literal((String)("[KelMagic FPS fixture] " + text)), true);
            }
        });
    }

    public static void hold() {
        if (!FpsDiagnostics.ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.level == null) {
            DiagnosticFixture.say("hold \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043e\u0434\u0438\u043d\u043e\u0447\u043d\u043e\u0439 \u0438\u0433\u0440\u0435 \u0441 integrated server; \u043d\u0443\u0436\u0435\u043d \u043e\u0442\u0434\u0435\u043b\u044c\u043d\u044b\u0439 \u0442\u0435\u0441\u0442\u043e\u0432\u044b\u0439 \u043c\u0438\u0440.");
            return;
        }
        RedMage only = null;
        int count = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            RedMage r;
            if (!(e instanceof RedMage) || (r = (RedMage)e).isRemoved()) continue;
            only = r;
            ++count;
        }
        if (count != 1) {
            DiagnosticFixture.say("\u041d\u0443\u0436\u043d\u0430 \u043e\u0434\u043d\u0430 \u0447\u0430\u0440\u043e\u0434\u0435\u0439\u043a\u0430; \u0443\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435 \u043d\u0435 \u043f\u0440\u0438\u043c\u0435\u043d\u0435\u043d\u043e.");
            return;
        }
        UUID uuid = only.getUUID();
        server.execute(() -> {
            if (saved != null) {
                DiagnosticFixture.say("\u0423\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435 \u0443\u0436\u0435 \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u043e; \u0441\u043d\u0430\u0447\u0430\u043b\u0430 release.");
                return;
            }
            for (ServerLevel level : server.getAllLevels()) {
                Entity patt0$temp = level.getEntity(uuid);
                if (!(patt0$temp instanceof RedMage)) continue;
                RedMage e = (RedMage)patt0$temp;
                try {
                    Saved snapshot;
                    saved = snapshot = new Saved((MinecraftServer)server, e, e.isNoAi(), e.scene(), new FixtureHoldCounters((Object)e));
                    e.getNavigation().stop();
                    e.setTarget(null);
                    e.setNoAi(true);
                    snapshot.counters.apply((Object)e);
                    if (e.throne().isEmpty()) {
                        e.scene(0);
                    }
                    DiagnosticFixture.say("NPC \u0443\u0434\u0435\u0440\u0436\u0430\u043d\u0430. \u0422\u0438\u043a\u0438 \u0438 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0430\u044e\u0442\u0441\u044f. \u041f\u043e\u0434\u043e\u0436\u0434\u0438 10 \u0441\u0435\u043a\u0443\u043d\u0434, \u0437\u0430\u0442\u0435\u043c session A/B. \u041f\u043e\u0441\u043b\u0435 \u0442\u0435\u0441\u0442\u0430 release.");
                }
                catch (Exception ex) {
                    DiagnosticFixture.restore();
                    DiagnosticFixture.say("\u041e\u0448\u0438\u0431\u043a\u0430 \u0444\u0438\u043a\u0441\u0442\u0443\u0440\u044b: " + String.valueOf(ex));
                }
                return;
            }
            DiagnosticFixture.say("NPC \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435.");
        });
    }

    @SubscribeEvent
    public static void maintain(EntityTickEvent.Pre event) {
        if (!FpsDiagnostics.ENABLED) {
            return;
        }
        Saved s = saved;
        if (s == null || event.getEntity() != s.entity || event.getEntity().level().isClientSide()) {
            return;
        }
        try {
            s.counters.apply((Object)s.entity);
        }
        catch (Exception ex) {
            DiagnosticFixture.restore();
            DiagnosticFixture.say("hold \u043e\u0442\u043a\u043b\u044e\u0447\u0451\u043d \u0438\u0437-\u0437\u0430 \u043e\u0448\u0438\u0431\u043a\u0438 \u0441\u0447\u0451\u0442\u0447\u0438\u043a\u043e\u0432: " + String.valueOf(ex));
        }
    }

    public static boolean readyForBenchmark() {
        RedMage e;
        boolean integrated;
        Minecraft mc = Minecraft.getInstance();
        Saved s = saved;
        boolean bl = integrated = mc.getSingleplayerServer() != null;
        if (mc.level == null || s == null) {
            return false;
        }
        Entity entity = mc.level.getEntity(s.entity.getId());
        boolean same = entity instanceof RedMage && !(e = (RedMage)entity).isRemoved() && e.isAlive() && e.getUUID().equals(s.entity.getUUID());
        return FixtureHoldCounters.benchmarkReady(integrated, s.server == mc.getSingleplayerServer(), same, same && ((RedMage)entity).isNoAi(), same && ((RedMage)entity).scene() == 0 && ((RedMage)entity).throne().isEmpty());
    }

    public static String status() {
        Minecraft mc = Minecraft.getInstance();
        Saved s = saved;
        return "integrated_server=" + (mc.getSingleplayerServer() != null) + "\nactive_server_hold=" + (s != null && s.server == mc.getSingleplayerServer()) + "\nbenchmark_fixture_ready=" + DiagnosticFixture.readyForBenchmark() + "\nhold_scope=only explicitly held NPC; countdown maintenance, ticks not cancelled; no per-tick scene/pose/position rewrite\n";
    }

    public static void removeForControl() {
        Minecraft mc = Minecraft.getInstance();
        IntegratedServer server = mc.getSingleplayerServer();
        if (server == null || mc.level == null) {
            DiagnosticFixture.say("remove \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043a\u043e\u043f\u0438\u0438 \u043e\u0434\u0438\u043d\u043e\u0447\u043d\u043e\u0433\u043e \u043c\u0438\u0440\u0430");
            return;
        }
        RedMage only = null;
        int n = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            RedMage r;
            if (!(e instanceof RedMage) || (r = (RedMage)e).isRemoved()) continue;
            only = r;
            ++n;
        }
        if (n != 1) {
            DiagnosticFixture.say("\u041d\u0443\u0436\u043d\u0430 \u0440\u043e\u0432\u043d\u043e \u043e\u0434\u043d\u0430 \u0447\u0430\u0440\u043e\u0434\u0435\u0439\u043a\u0430; \u043d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u0443\u0434\u0430\u043b\u0435\u043d\u043e");
            return;
        }
        UUID id = only.getUUID();
        server.execute(() -> {
            for (ServerLevel level : server.getAllLevels()) {
                Entity patt0$temp = level.getEntity(id);
                if (!(patt0$temp instanceof RedMage)) continue;
                RedMage e = (RedMage)patt0$temp;
                e.discard();
                DiagnosticFixture.say("NPC \u0443\u0434\u0430\u043b\u0435\u043d\u0430 \u0431\u0435\u0437 \u0434\u0440\u043e\u043f\u0430, \u0442\u0440\u043e\u043d \u043d\u0435 \u043c\u0435\u043d\u044f\u043b\u0441\u044f. \u041f\u043e\u0434\u043e\u0436\u0434\u0438 10 \u0441\u0435\u043a\u0443\u043d\u0434 \u0438 !kfps run B_ABSENT. \u0410\u0432\u0442\u043e\u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u044f NPC \u043d\u0435\u0442: \u044d\u0442\u043e \u043a\u043e\u043f\u0438\u044f \u043c\u0438\u0440\u0430.");
                return;
            }
        });
    }

    public static void release() {
        Saved s = saved;
        if (s == null) {
            DiagnosticFixture.say("\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0433\u043e \u0443\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u044f.");
            return;
        }
        s.server.execute(() -> {
            DiagnosticFixture.restore();
            DiagnosticFixture.say("\u0423\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435 \u0441\u043d\u044f\u0442\u043e; \u0438\u0441\u0445\u043e\u0434\u043d\u044b\u0435 NoAI \u0438 \u0441\u0447\u0451\u0442\u0447\u0438\u043a\u0438 \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u044b, \u0435\u0441\u043b\u0438 NPC \u0435\u0449\u0451 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442.");
        });
    }

    private static void restore() {
        Saved s = saved;
        if (s == null) {
            return;
        }
        try {
            RedMage e = s.entity;
            if (!e.isRemoved()) {
                e.setNoAi(s.noAi);
                s.counters.restore((Object)e);
                if (e.scene() != s.scene) {
                    e.scene(s.scene);
                }
            }
        }
        catch (Exception ex) {
            System.err.println("KelMagic FPS fixture restore failed: " + String.valueOf(ex));
        }
        finally {
            saved = null;
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        if (saved != null && DiagnosticFixture.saved.server == event.getServer()) {
            DiagnosticFixture.restore();
        }
    }

    private DiagnosticFixture() {
    }

    private record Saved(MinecraftServer server, RedMage entity, boolean noAi, int scene, FixtureHoldCounters counters) {
    }
}

