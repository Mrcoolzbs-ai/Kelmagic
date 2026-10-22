/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientChatEvent
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderFrameEvent$Post
 *  net.neoforged.neoforge.client.event.RenderFrameEvent$Pre
 *  net.neoforged.neoforge.client.event.ScreenEvent$Opening
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent
 *  org.lwjgl.opengl.GL11
 */
package dev.kelmagic.client.diag;

import dev.kelmagic.client.diag.BenchmarkPlan;
import dev.kelmagic.client.diag.CaptureGuard;
import dev.kelmagic.client.diag.CaptureValidity;
import dev.kelmagic.client.diag.DiagnosticFixture;
import dev.kelmagic.client.diag.FixtureHoldCounters;
import dev.kelmagic.client.diag.FrameAllocation;
import dev.kelmagic.client.diag.FrameStats;
import dev.kelmagic.client.diag.HairStageProfiler;
import dev.kelmagic.entity.RedMage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid="kelmagic", value={Dist.CLIENT})
public final class FpsDiagnostics {
    public static final boolean ENABLED = Boolean.getBoolean("kelmagic.fpsdiag");
    public static final int HAIR_SOLVER = 0;
    public static final int HAIR_TOTAL = 1;
    public static final int EYES = 2;
    public static final int SEARCH = 3;
    public static final int EYE_MESH = 4;
    public static final int MAGE_RENDER = 5;
    private static final int METRICS = 12;
    private static final int CAPACITY = 65536;
    private static final int MAX_ATTEMPTS = 6;
    private static final long WARMUP = 5000000000L;
    private static final long WINDOW = 15000000000L;
    private static final String[] NAMES = new String[]{"hair_solver_cpu_wall_ms", "hair_bridge_inclusive_cpu_wall_ms", "eye_controller_cpu_wall_ms", "target_search_cpu_wall_ms", "eye_optical_mesh_cpu_wall_ms", "mage_render_inclusive_cpu_wall_ms", "hair_constraints_cpu_wall_ms", "hair_body_arm_contacts_cpu_wall_ms", "hair_self_collisions_cpu_wall_ms", "hair_other_inclusive_cpu_wall_ms", "hair_internal_total_cpu_wall_ms", "hair_stage_timer_estimated_overhead_cpu_wall_ms"};
    private static final String[] EXTRA = new String[]{"hair_bridge_bone_visits", "hair_needs_sample_bone_visits", "hair_time_gated_bone_visits", "hair_mode_disabled_updates", "hair_sample_completed_calls", "hair_substeps", "hair_zero_step_calls", "hair_geometry_prepared_batches", "hair_geometry_prepared_quads", "hair_rendered_bone_batches", "hair_rendered_quads", "hair_stage_timer_pairs", "hair_stage_sample_calls", "hair_iterations", "hair_link_visits", "hair_body_contacts", "hair_arm_contacts", "hair_self_contacts", "client_thread_allocated_bytes"};
    private static final long[] totals = new long[12];
    private static final long[] extras = new long[EXTRA.length];
    private static final int[] depth = new int[12];
    private static final Map<String, Integer> validCounts = new LinkedHashMap<String, Integer>();
    private static final Map<String, Integer> attemptCounts = new LinkedHashMap<String, Integer>();
    private static Mode mode = Mode.FULL;
    private static String phase = "FULL";
    private static boolean recording;
    private static boolean running;
    private static boolean benchmarkExperiment;
    private static int benchmarkPrewarm;
    private static final long BENCHMARK_WARMUP = 20000000000L;
    private static long allocationStart;
    private static long warmUntil;
    private static long lastPre;
    private static long windowStart;
    private static long renderCalls;
    private static long solverCalls;
    private static double inputSim;
    private static int count;
    private static int attemptInCommand;
    private static int commandIssues;
    private static long[] frames;
    private static long[] draws;
    private static long[] solves;
    private static long[] gameTimes;
    private static long[] npcTicks;
    private static long[][] samples;
    private static long[][] extraRows;
    private static double[][] cameras;
    private static double[][] npcRows;
    private static int[] flags;
    private static Path directory;

    public static boolean hairDisabled() {
        return ENABLED && mode == Mode.NO_HAIR_PHYSICS;
    }

    public static boolean eyesDisabled() {
        return ENABLED && mode == Mode.NO_EYES;
    }

    public static boolean hidden() {
        return ENABLED && mode == Mode.OUT_OF_VIEW;
    }

    public static long begin(int n) {
        if (!recording) {
            return 0L;
        }
        int n2 = n;
        int n3 = depth[n2];
        depth[n2] = n3 + 1;
        return n3 == 0 ? System.nanoTime() : -1L;
    }

    public static void end(int n, long l) {
        if (l == 0L) {
            return;
        }
        int n2 = n;
        depth[n2] = depth[n2] - 1;
        if (depth[n2] == 0) {
            int n3 = n;
            totals[n3] = totals[n3] + (System.nanoTime() - l);
        }
    }

    public static void rendered(RedMage redMage) {
        if (recording) {
            ++renderCalls;
        }
    }

    public static void sampleCalled() {
        if (recording) {
            ++solverCalls;
        }
    }

    public static void sampleCompleted(int n) {
        if (recording) {
            extras[4] = extras[4] + 1L;
            extras[5] = extras[5] + (long)n;
            if (n == 0) {
                extras[6] = extras[6] + 1L;
            }
        }
    }

    public static void hairVisit(RedMage redMage, double d, boolean bl) {
        if (recording) {
            extras[0] = extras[0] + 1L;
            int n = bl ? 1 : 2;
            extras[n] = extras[n] + 1L;
            inputSim = d;
        }
    }

    public static void hairEmitted(int n) {
        if (recording) {
            extras[9] = extras[9] + 1L;
            extras[10] = extras[10] + (long)n;
        }
    }

    public static boolean measuring() {
        return recording;
    }

    public static void hairModeSkip() {
        if (recording) {
            extras[3] = extras[3] + 1L;
        }
    }

    public static void hairGeometry(int n) {
        if (recording) {
            extras[7] = extras[7] + 1L;
            extras[8] = extras[8] + (long)n;
        }
    }

    private static boolean absent() {
        return mode == Mode.ABSENT;
    }

    private static boolean visible() {
        return !FpsDiagnostics.hidden() && !FpsDiagnostics.absent();
    }

    private static void say(String string) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage((Component)Component.literal((String)("[KelMagic FPS] " + string)), true);
        }
    }

    @SubscribeEvent
    public static void chat(ClientChatEvent clientChatEvent) {
        String string;
        String string2 = clientChatEvent.getMessage().trim();
        if (!string2.equals("!kfps") && !string2.startsWith("!kfps ")) {
            return;
        }
        clientChatEvent.setCanceled(true);
        if (!ENABLED) {
            FpsDiagnostics.say("\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043e: \u043d\u0443\u0436\u0435\u043d JVM-\u0444\u043b\u0430\u0433 -Dkelmagic.fpsdiag=true");
            return;
        }
        String[] stringArray = string2.split("\\s+");
        String string3 = string = stringArray.length > 1 ? stringArray[1].toLowerCase(Locale.ROOT) : "help";
        if (string.equals("status")) {
            FpsDiagnostics.say("\u0421\u0435\u0440\u0438\u044f " + CaptureGuard.series + "; " + phase + "; " + (running ? (recording ? "\u0437\u0430\u043f\u0438\u0441\u044c" : "\u043f\u0440\u043e\u0433\u0440\u0435\u0432") : "\u043e\u0436\u0438\u0434\u0430\u043d\u0438\u0435") + "; valid=" + String.valueOf(validCounts));
            return;
        }
        if (string.equals("stop")) {
            if (recording) {
                FpsDiagnostics.finish(16384, true);
            } else {
                running = false;
                mode = Mode.FULL;
                BenchmarkPlan.cancel();
                HairStageProfiler.active(false);
                FpsDiagnostics.writeStatusQuietly();
            }
            FpsDiagnostics.say("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e. FULL; \u043d\u0435\u043f\u043e\u043b\u043d\u043e\u0435 \u043e\u043a\u043d\u043e \u043d\u0435 \u043f\u0440\u0438\u043d\u044f\u0442\u043e.");
            return;
        }
        if (running) {
            FpsDiagnostics.say("\u0418\u0434\u0451\u0442 \u0441\u0435\u0440\u0438\u044f. \u041c\u0435\u043d\u044e/\u0447\u0430\u0442 \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u043e\u043a\u043d\u0430 \u0434\u0435\u043b\u0430\u044e\u0442 \u0435\u0433\u043e \u043d\u0435\u0432\u0430\u043b\u0438\u0434\u043d\u044b\u043c. !kfps stop");
            return;
        }
        if (string.equals("remove")) {
            if (!CaptureGuard.series.equals("B") || validCounts.getOrDefault("B_PRESENT", 0) < 3) {
                FpsDiagnostics.say("remove \u0440\u0430\u0437\u0440\u0435\u0448\u0451\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e\u0441\u043b\u0435 \u0442\u0440\u0451\u0445 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445 B_PRESENT, \u0432 \u043a\u043e\u043f\u0438\u0438 \u043c\u0438\u0440\u0430");
                return;
            }
            DiagnosticFixture.removeForControl();
            return;
        }
        if (string.equals("hold") || string.equals("release")) {
            if (string.equals("hold")) {
                DiagnosticFixture.hold();
            } else {
                DiagnosticFixture.release();
            }
            return;
        }
        if (string.equals("session") && stringArray.length == 3) {
            String string4 = stringArray[2].toUpperCase(Locale.ROOT);
            if (!string4.equals("A") && !string4.equals("B")) {
                FpsDiagnostics.say("\u0421\u0435\u0440\u0438\u044f \u0442\u043e\u043b\u044c\u043a\u043e A \u0438\u043b\u0438 B");
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || minecraft.player == null) {
                return;
            }
            try {
                BenchmarkPlan.cancel();
                benchmarkExperiment = false;
                HairStageProfiler.active(false);
                HairStageProfiler.calibrate();
                FrameAllocation.read();
                CaptureGuard.session(string4);
                validCounts.clear();
                attemptCounts.clear();
                mode = Mode.FULL;
                directory = minecraft.gameDirectory.toPath().resolve("kelmagic-hair-accum-r2").resolve(Instant.now().toString().replace(':', '-') + "-" + string4);
                Files.createDirectories(directory.resolve("rejected"), new FileAttribute[0]);
                Files.writeString(directory.resolve("environment.txt"), (CharSequence)FpsDiagnostics.environment(), StandardCharsets.UTF_8, new OpenOption[0]);
                Files.writeString(directory.resolve("timer-calibration.txt"), (CharSequence)HairStageProfiler.calibration(), StandardCharsets.UTF_8, new OpenOption[0]);
                FpsDiagnostics.writeStatus();
                FpsDiagnostics.say("\u041d\u043e\u0432\u0430\u044f \u0441\u0435\u0440\u0438\u044f " + string4 + ". \u041a\u0430\u043c\u0435\u0440\u0430/NPC/\u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0444\u0438\u043a\u0441\u0438\u0440\u0443\u044e\u0442\u0441\u044f \u043f\u0435\u0440\u0432\u044b\u043c \u043e\u043a\u043d\u043e\u043c; \u043d\u0435 \u043c\u0435\u043d\u044f\u0442\u044c \u043c\u0435\u0436\u0434\u0443 \u0440\u0435\u0436\u0438\u043c\u0430\u043c\u0438.");
            }
            catch (Exception exception) {
                FpsDiagnostics.say("\u041e\u0448\u0438\u0431\u043a\u0430 \u0441\u0435\u0441\u0441\u0438\u0438: " + String.valueOf(exception));
            }
            return;
        }
        if (string.equals("ab") && stringArray.length == 2) {
            if (directory == null || !CaptureGuard.series.equals("A")) {
                FpsDiagnostics.say("\u041d\u0443\u0436\u0435\u043d -Dkelmagic.fpsdiag=true, !kfps session A \u0438 \u043e\u0431\u044b\u0447\u043d\u0430\u044f \u0441\u0446\u0435\u043d\u0430 \u0431\u0435\u0437 \u0442\u0440\u043e\u043d\u0430");
                return;
            }
            if (!DiagnosticFixture.readyForBenchmark()) {
                FpsDiagnostics.say("A/B \u043d\u0435 \u0437\u0430\u043f\u0443\u0449\u0435\u043d: \u043d\u0443\u0436\u0435\u043d \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u044b\u0439 hold \u0438 \u0441\u0446\u0435\u043d\u0430 0, \u043e\u0434\u043d\u043e\u0433\u043e NoAI \u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e. !kfps release, !kfps hold, \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c 10\u0441.");
                return;
            }
            if (!validCounts.isEmpty() || benchmarkExperiment) {
                FpsDiagnostics.say("\u0414\u043b\u044f \u043d\u043e\u0432\u043e\u0433\u043e A/B \u043d\u0443\u0436\u043d\u0430 \u043d\u043e\u0432\u0430\u044f !kfps session A");
                return;
            }
            try {
                Files.writeString(directory.resolve("fixture-status.txt"), (CharSequence)DiagnosticFixture.status(), StandardCharsets.UTF_8, new OpenOption[0]);
            }
            catch (IOException iOException) {
                FpsDiagnostics.say("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0441\u0442\u0430\u0442\u0443\u0441 \u0444\u0438\u043a\u0441\u0442\u0443\u0440\u044b: " + String.valueOf(iOException));
                return;
            }
            BenchmarkPlan.start();
            benchmarkExperiment = true;
            benchmarkPrewarm = 0;
            phase = "PREWARM_A";
            mode = Mode.FULL;
            commandIssues = 0;
            attemptInCommand = 0;
            FpsDiagnostics.allocateBuffers();
            count = 0;
            FpsDiagnostics.clearCounters();
            running = true;
            recording = false;
            HairStageProfiler.active(true);
            FpsDiagnostics.say("A/B: \u043f\u0440\u043e\u0433\u0440\u0435\u0432 A20+B20+A20 \u0441\u0435\u043a\u0443\u043d\u0434; \u0437\u0430\u0442\u0435\u043c 6 \u043e\u043a\u043e\u043d \u043f\u043e 15\u0441, \u043f\u043e 20\u0441 \u043f\u0440\u043e\u0433\u0440\u0435\u0432\u0430 \u043f\u0435\u0440\u0435\u0434 \u043e\u0441\u0442\u0430\u043b\u044c\u043d\u044b\u043c\u0438. \u041d\u0435 \u043c\u0435\u043d\u044f\u0442\u044c \u0441\u0446\u0435\u043d\u0443/\u043a\u0430\u043c\u0435\u0440\u0443/\u043c\u0435\u043d\u044e.");
            Minecraft.getInstance().setScreen(null);
            warmUntil = System.nanoTime() + 20000000000L;
            FpsDiagnostics.writeStatusQuietly();
            return;
        }
        if (string.equals("run") && stringArray.length == 3) {
            Mode mode;
            if (benchmarkExperiment) {
                FpsDiagnostics.say("\u0412 A/B-\u0441\u0435\u0441\u0441\u0438\u0438 \u043e\u0431\u044b\u0447\u043d\u044b\u0439 run \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d; \u043d\u043e\u0432\u0430\u044f !kfps session A \u0434\u043b\u044f \u0434\u0440\u0443\u0433\u043e\u0433\u043e \u0442\u0435\u0441\u0442\u0430");
                return;
            }
            if (directory == null) {
                FpsDiagnostics.say("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 !kfps session A \u0438\u043b\u0438 !kfps session B");
                return;
            }
            String string5 = stringArray[2].toUpperCase(Locale.ROOT);
            try {
                if (CaptureGuard.series.equals("A")) {
                    mode = Mode.valueOf(string5);
                    if (mode == Mode.ABSENT || mode == Mode.NO_EYES) {
                        throw new IllegalArgumentException();
                    }
                    if (mode != Mode.FULL && validCounts.getOrDefault("FULL", 0) < 3) {
                        FpsDiagnostics.say("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0442\u0440\u0438 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445 FULL");
                        return;
                    }
                } else {
                    if (string5.equals("B_PRESENT")) {
                        mode = Mode.FULL;
                    } else if (string5.equals("B_ABSENT")) {
                        mode = Mode.ABSENT;
                    } else {
                        throw new IllegalArgumentException();
                    }
                    if (mode == Mode.ABSENT && validCounts.getOrDefault("B_PRESENT", 0) < 3) {
                        FpsDiagnostics.say("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0442\u0440\u0438 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445 B_PRESENT");
                        return;
                    }
                }
            }
            catch (IllegalArgumentException illegalArgumentException) {
                FpsDiagnostics.say("A: FULL, NO_HAIR_PHYSICS, OUT_OF_VIEW. Eye Controller unchanged. B: B_PRESENT, B_ABSENT");
                return;
            }
            if (validCounts.getOrDefault(string5, 0) >= 3) {
                FpsDiagnostics.say("\u0423\u0436\u0435 \u0435\u0441\u0442\u044c \u0442\u0440\u0438 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445 " + string5 + " \u0432 \u044d\u0442\u043e\u0439 \u0441\u0435\u0441\u0441\u0438\u0438");
                return;
            }
            FpsDiagnostics.mode = mode;
            phase = string5;
            attemptInCommand = 0;
            commandIssues = 0;
            FpsDiagnostics.allocateBuffers();
            running = true;
            recording = false;
            count = 0;
            FpsDiagnostics.clearCounters();
            FpsDiagnostics.say(string5 + (HairStageProfiler.ENABLED ? " [4 \u044d\u0442\u0430\u043f\u0430: ON]" : " [4 \u044d\u0442\u0430\u043f\u0430: OFF]") + ": \u0434\u043e 3 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445 \u043e\u043a\u043e\u043d; \u043c\u0430\u043a\u0441\u0438\u043c\u0443\u043c 6 \u043f\u043e\u043f\u044b\u0442\u043e\u043a. 5 \u0441 \u043f\u0440\u043e\u0433\u0440\u0435\u0432 + 15 \u0441 \u0437\u0430\u043f\u0438\u0441\u044c. \u041d\u0435 \u0442\u0440\u043e\u0433\u0430\u0439 \u043a\u0430\u043c\u0435\u0440\u0443/\u043c\u0435\u043d\u044e.");
            Minecraft.getInstance().setScreen(null);
            warmUntil = System.nanoTime() + 5000000000L;
            return;
        }
        FpsDiagnostics.say("!kfps ab; !kfps session A|B; !kfps run <\u0440\u0435\u0436\u0438\u043c>; !kfps hold|release|remove; !kfps status|stop");
    }

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent entityJoinLevelEvent) {
        if (ENABLED && entityJoinLevelEvent.getLevel().isClientSide() && entityJoinLevelEvent.getEntity() instanceof RedMage) {
            CaptureGuard.mageIds.add(entityJoinLevelEvent.getEntity().getId());
            if (recording) {
                CaptureGuard.mark(16);
            }
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent entityLeaveLevelEvent) {
        if (ENABLED && entityLeaveLevelEvent.getLevel().isClientSide() && entityLeaveLevelEvent.getEntity() instanceof RedMage) {
            CaptureGuard.mageIds.remove(entityLeaveLevelEvent.getEntity().getId());
            if (recording) {
                CaptureGuard.mark(16);
            }
        }
    }

    @SubscribeEvent
    public static void screen(ScreenEvent.Opening opening) {
        if (recording && opening.getNewScreen() != null) {
            CaptureGuard.mark(1);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post post) {
        if (recording) {
            CaptureGuard.poll(FpsDiagnostics.absent());
        }
    }

    @SubscribeEvent
    public static void post(RenderFrameEvent.Post post) {
        if (recording) {
            CaptureGuard.poll(FpsDiagnostics.absent());
        }
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Pre pre) {
        int n;
        if (!running) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            if (recording) {
                FpsDiagnostics.finish(2048, true);
            } else {
                running = false;
                mode = Mode.FULL;
                BenchmarkPlan.cancel();
                HairStageProfiler.active(false);
                FpsDiagnostics.writeStatusQuietly();
            }
            return;
        }
        long l = System.nanoTime();
        if (!recording) {
            if (benchmarkExperiment) {
                HairStageProfiler.reset();
                if (l >= warmUntil && benchmarkPrewarm < 2) {
                    BenchmarkPlan.selectArm(++benchmarkPrewarm == 1);
                    phase = benchmarkPrewarm == 1 ? "PREWARM_B" : BenchmarkPlan.label();
                    warmUntil = l + 20000000000L;
                    FpsDiagnostics.writeStatusQuietly();
                    return;
                }
            }
            if (l < warmUntil) {
                return;
            }
            try {
                if (benchmarkExperiment && !DiagnosticFixture.readyForBenchmark()) {
                    throw new IllegalStateException("\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0433\u043e hold/\u043e\u0431\u044b\u0447\u043d\u043e\u0439 \u0441\u0446\u0435\u043d\u044b 0; A/B \u043d\u0435 \u043d\u0430\u0447\u0430\u0442, \u043f\u0440\u043e\u0432\u0435\u0440\u044c !kfps hold");
                }
                if (CaptureGuard.reference == null) {
                    if (FpsDiagnostics.absent()) {
                        throw new IllegalStateException("\u041d\u0435\u043b\u044c\u0437\u044f \u043d\u0430\u0447\u0438\u043d\u0430\u0442\u044c \u0441 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0439 NPC");
                    }
                    CaptureGuard.establish();
                    Files.writeString(directory.resolve("reference.txt"), (CharSequence)CaptureGuard.reference.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
                }
                CaptureGuard.index();
                CaptureGuard.beginWindow();
                count = 0;
                lastPre = windowStart = System.nanoTime();
                FpsDiagnostics.clearCounters();
                recording = true;
                HairStageProfiler.active(true);
                allocationStart = FrameAllocation.read();
            }
            catch (Exception exception) {
                running = false;
                mode = Mode.FULL;
                BenchmarkPlan.cancel();
                HairStageProfiler.active(false);
                FpsDiagnostics.writeStatusQuietly();
                FpsDiagnostics.say("\u041d\u0435 \u043d\u0430\u0447\u0430\u0442\u043e: " + exception.getMessage());
            }
            return;
        }
        if (count == 65536) {
            FpsDiagnostics.finish(8192, true);
            return;
        }
        int n2 = count++;
        FpsDiagnostics.frames[n2] = l - lastPre;
        if (HairStageProfiler.ENABLED) {
            for (int i = 0; i < 5; ++i) {
                FpsDiagnostics.totals[6 + i] = HairStageProfiler.nanos(i);
            }
            FpsDiagnostics.totals[11] = HairStageProfiler.overheadEstimateNs();
            FpsDiagnostics.extras[11] = HairStageProfiler.pairs();
            FpsDiagnostics.extras[12] = HairStageProfiler.samples();
        }
        FpsDiagnostics.extras[13] = HairStageProfiler.iterations();
        FpsDiagnostics.extras[14] = HairStageProfiler.linkVisits();
        FpsDiagnostics.extras[15] = HairStageProfiler.bodyContacts();
        FpsDiagnostics.extras[16] = HairStageProfiler.armContacts();
        FpsDiagnostics.extras[17] = HairStageProfiler.selfContacts();
        long l2 = FrameAllocation.read();
        FpsDiagnostics.extras[18] = allocationStart < 0L || l2 < 0L ? -1L : l2 - allocationStart;
        for (n = 0; n < 12; ++n) {
            FpsDiagnostics.samples[n][n2] = totals[n];
        }
        for (n = 0; n < EXTRA.length; ++n) {
            FpsDiagnostics.extraRows[n][n2] = extras[n];
        }
        FpsDiagnostics.draws[n2] = renderCalls;
        FpsDiagnostics.solves[n2] = solverCalls;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 vec3 = camera.getPosition();
        FpsDiagnostics.cameras[0][n2] = vec3.x;
        FpsDiagnostics.cameras[1][n2] = vec3.y;
        FpsDiagnostics.cameras[2][n2] = vec3.z;
        FpsDiagnostics.cameras[3][n2] = camera.getYRot();
        FpsDiagnostics.cameras[4][n2] = camera.getXRot();
        int n3 = CaptureGuard.poll(FpsDiagnostics.absent());
        if (benchmarkExperiment && !DiagnosticFixture.readyForBenchmark()) {
            n3 |= 0x20;
        }
        n3 |= CaptureValidity.visibility(FpsDiagnostics.visible(), renderCalls, extras[9]);
        if (FpsDiagnostics.visible()) {
            n3 |= CaptureGuard.hairShape(renderCalls, extras[9], extras[10], mode == Mode.FULL && n3 == 0);
        }
        if (!(!FpsDiagnostics.hairDisabled() && FpsDiagnostics.visible() || solverCalls == 0L && totals[0] == 0L)) {
            n3 |= 0x400;
        }
        if (HairStageProfiler.ENABLED && !HairStageProfiler.balanced(solverCalls)) {
            n3 |= 0x8000;
        }
        FpsDiagnostics.flags[n2] = n3;
        CaptureGuard.mark(n3);
        FpsDiagnostics.gameTimes[n2] = minecraft.level.getGameTime();
        FpsDiagnostics.npcTicks[n2] = -1L;
        for (int i = 0; i < 4; ++i) {
            FpsDiagnostics.npcRows[i][n2] = Double.NaN;
        }
        FpsDiagnostics.npcRows[4][n2] = inputSim;
        CaptureGuard.Reference reference = CaptureGuard.reference;
        Entity entity = minecraft.level.getEntity(reference.npcId());
        if (entity instanceof RedMage) {
            RedMage redMage = (RedMage)entity;
            FpsDiagnostics.npcTicks[n2] = redMage.tickCount;
            FpsDiagnostics.npcRows[0][n2] = redMage.getX();
            FpsDiagnostics.npcRows[1][n2] = redMage.getY();
            FpsDiagnostics.npcRows[2][n2] = redMage.getZ();
            FpsDiagnostics.npcRows[3][n2] = redMage.scene();
        }
        lastPre = l;
        FpsDiagnostics.clearCounters();
        allocationStart = FrameAllocation.read();
        if (l - windowStart >= 15000000000L) {
            FpsDiagnostics.finish(0, false);
        }
    }

    private static void allocateBuffers() {
        frames = new long[65536];
        samples = new long[12][65536];
        extraRows = new long[EXTRA.length][65536];
        draws = new long[65536];
        solves = new long[65536];
        gameTimes = new long[65536];
        npcTicks = new long[65536];
        cameras = new double[5][65536];
        npcRows = new double[5][65536];
        flags = new int[65536];
    }

    private static boolean benchmarkComplete() {
        for (String string : BenchmarkPlan.labels()) {
            if (validCounts.getOrDefault(string, 0) >= 1) continue;
            return false;
        }
        return true;
    }

    private static void clearCounters() {
        HairStageProfiler.reset();
        Arrays.fill(totals, 0L);
        Arrays.fill(depth, 0);
        Arrays.fill(extras, 0L);
        solverCalls = 0L;
        renderCalls = 0L;
        inputSim = Double.NaN;
    }

    private static void finish(int n, boolean bl) {
        int n2;
        HairStageProfiler.active(false);
        recording = false;
        int n3 = CaptureGuard.observedIssues | n;
        long l = 0L;
        long l2 = 0L;
        long l3 = 0L;
        long l4 = 0L;
        long l5 = 0L;
        long l6 = 0L;
        for (n2 = 0; n2 < count; ++n2) {
            l6 += frames[n2];
            l += solves[n2];
            l2 += extraRows[4][n2];
            l3 += extraRows[5][n2];
            l4 += samples[0][n2];
            l5 += extraRows[9][n2];
            n3 |= flags[n2];
        }
        n3 |= CaptureValidity.counters(FpsDiagnostics.visible(), FpsDiagnostics.hairDisabled(), l, l2, l3, l4, l5);
        if (l6 < 15000000000L) {
            n3 |= 0x4000;
        }
        if (benchmarkExperiment && FixtureHoldCounters.abortSceneChange(n3)) {
            bl = true;
        }
        n2 = n3 == 0 ? 1 : 0;
        ++attemptInCommand;
        int n4 = attemptCounts.merge(phase, 1, Integer::sum);
        int n5 = validCounts.getOrDefault(phase, 0);
        if (n2 != 0) {
            ++n5;
        }
        commandIssues |= n3;
        try {
            Path path = n2 != 0 ? directory.resolve(phase + "-valid-" + n5 + ".csv") : directory.resolve("rejected/" + phase + "-attempt-" + n4 + ".csv");
            FpsDiagnostics.writeFrames(path);
            if (n2 != 0) {
                FpsDiagnostics.writeStageSummary(n5, n4, l6);
                FrameStats frameStats = FrameStats.of(frames, count);
                Path path2 = directory.resolve("summary-valid.csv");
                boolean bl2 = Files.exists(path2, new LinkOption[0]);
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);){
                    if (!bl2) {
                        bufferedWriter.write("series,phase,mode,valid_repeat,attempt,frames,duration_s,mean_frame_ms,median_frame_ms,p95_frame_ms,fps_total,hair_sample_calls,hair_sample_completed,hair_substeps");
                        for (String string : NAMES) {
                            bufferedWriter.write(",mean_" + string);
                        }
                        bufferedWriter.write("\n");
                    }
                    bufferedWriter.write(CaptureGuard.series + "," + phase + "," + String.valueOf((Object)mode) + "," + n5 + "," + n4 + "," + count + "," + FpsDiagnostics.sec(l6) + "," + frameStats.meanMs() + "," + frameStats.medianMs() + "," + frameStats.p95Ms() + "," + frameStats.fps() + "," + l + "," + l2 + "," + l3);
                    for (int i = 0; i < 12; ++i) {
                        if (!FpsDiagnostics.metricAvailable(i)) {
                            bufferedWriter.write(",");
                            continue;
                        }
                        double d = 0.0;
                        for (int j = 0; j < count; ++j) {
                            d += (double)samples[i][j];
                        }
                        bufferedWriter.write("," + d / (double)count / 1000000.0);
                    }
                    bufferedWriter.write("\n");
                }
            }
            Path path3 = directory.resolve("rejected.csv");
            boolean bl3 = Files.exists(path3, new LinkOption[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path3, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);){
                if (!bl3) {
                    bufferedWriter.write("series,phase,attempt,frames,duration_s,reason_bits,reasons,hair_sample_calls,hair_sample_completed,hair_substeps\n");
                }
                bufferedWriter.write(CaptureGuard.series + "," + phase + "," + n4 + "," + count + "," + FpsDiagnostics.sec(l6) + "," + n3 + "," + CaptureValidity.explain(n3) + "," + l + "," + l2 + "," + l3 + "\n");
            }
            if (n2 != 0) {
                validCounts.put(phase, n5);
            }
            FpsDiagnostics.writeStatus();
            if (benchmarkExperiment) {
                if (!bl && n2 != 0 && BenchmarkPlan.next()) {
                    phase = BenchmarkPlan.label();
                    attemptInCommand = 0;
                    count = 0;
                    FpsDiagnostics.clearCounters();
                    warmUntil = System.nanoTime() + 20000000000L;
                    HairStageProfiler.active(true);
                    FpsDiagnostics.writeStatus();
                    FpsDiagnostics.say("\u041f\u0440\u0438\u043d\u044f\u0442\u043e. \u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 " + phase + ": \u043f\u0440\u043e\u0433\u0440\u0435\u0432 20\u0441, \u0437\u0430\u0442\u0435\u043c \u043e\u043a\u043d\u043e 15\u0441.");
                    return;
                }
                if (!bl && n2 == 0 && attemptInCommand < 6) {
                    count = 0;
                    FpsDiagnostics.clearCounters();
                    warmUntil = System.nanoTime() + 20000000000L;
                    HairStageProfiler.active(true);
                    FpsDiagnostics.say("\u041e\u043a\u043d\u043e " + phase + " \u043e\u0442\u043a\u043b\u043e\u043d\u0435\u043d\u043e: " + CaptureValidity.explain(n3) + ". \u041f\u043e\u0432\u0442\u043e\u0440 \u043f\u043e\u0441\u043b\u0435 20\u0441 \u043f\u0440\u043e\u0433\u0440\u0435\u0432\u0430.");
                    return;
                }
                running = false;
                mode = Mode.FULL;
                BenchmarkPlan.cancel();
                FpsDiagnostics.clearCounters();
                FpsDiagnostics.writeStatus();
                FpsDiagnostics.say(FpsDiagnostics.benchmarkComplete() ? "A/B: \u0432\u0441\u0435 6 \u043e\u043a\u043e\u043d \u0432\u0430\u043b\u0438\u0434\u043d\u044b. \u041f\u0430\u0442\u0447 \u041d\u0415 \u043f\u0440\u0438\u043d\u044f\u0442 \u0434\u043e \u0430\u043d\u0430\u043b\u0438\u0437\u0430 CSV. \u0412\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d \u043a\u0430\u043d\u0434\u0438\u0434\u0430\u0442 B. \u041f\u0440\u0438\u0448\u043b\u0438 \u0432\u0441\u044e \u043f\u0430\u043f\u043a\u0443." : "A/B \u043d\u0435\u043f\u043e\u043b\u043d\u044b\u0439; \u043a\u0430\u043d\u0434\u0438\u0434\u0430\u0442 B \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d. \u0421\u043c. status.txt/rejected.csv");
                return;
            }
            if (!bl && n5 < 3 && attemptInCommand < 6) {
                count = 0;
                FpsDiagnostics.clearCounters();
                warmUntil = System.nanoTime() + 5000000000L;
                FpsDiagnostics.say(n2 != 0 ? "\u041f\u0440\u0438\u043d\u044f\u0442\u043e " + n5 + "/3. \u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u043f\u0440\u043e\u0433\u0440\u0435\u0432." : "\u041e\u043a\u043d\u043e \u043e\u0442\u043a\u043b\u043e\u043d\u0435\u043d\u043e: " + CaptureValidity.explain(n3) + ". \u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u043f\u0440\u043e\u0433\u0440\u0435\u0432.");
                return;
            }
            running = false;
            mode = Mode.FULL;
            FpsDiagnostics.clearCounters();
            FpsDiagnostics.writeStatus();
            FpsDiagnostics.say(n5 >= 3 ? phase + ": \u0442\u0440\u0438 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445 \u043e\u043a\u043d\u0430. FULL \u0432\u043e\u0441\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d." : phase + ": \u0442\u043e\u043b\u044c\u043a\u043e " + n5 + "/3 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0445; " + CaptureValidity.explain(commandIssues) + ". \u0421\u043c. status.txt/rejected.csv");
        }
        catch (Exception exception) {
            running = false;
            mode = Mode.FULL;
            BenchmarkPlan.cancel();
            FpsDiagnostics.clearCounters();
            FpsDiagnostics.say("\u041e\u0448\u0438\u0431\u043a\u0430 \u044d\u043a\u0441\u043f\u043e\u0440\u0442\u0430: " + String.valueOf(exception));
        }
    }

    private static void writeStatus() throws IOException {
        String[] stringArray;
        if (directory == null) {
            return;
        }
        if (benchmarkExperiment) {
            stringArray = BenchmarkPlan.labels();
        } else if (CaptureGuard.series.equals("A")) {
            String[] stringArray2 = new String[3];
            stringArray2[0] = "FULL";
            stringArray2[1] = "NO_HAIR_PHYSICS";
            stringArray = stringArray2;
            stringArray2[2] = "OUT_OF_VIEW";
        } else {
            String[] stringArray3 = new String[2];
            stringArray3[0] = "B_PRESENT";
            stringArray = stringArray3;
            stringArray3[1] = "B_ABSENT";
        }
        String[] stringArray4 = stringArray;
        StringBuilder stringBuilder = new StringBuilder("series=" + CaptureGuard.series + "\nhair_stages_enabled=" + HairStageProfiler.ENABLED + "\nredesign_experiment=" + benchmarkExperiment + "\nsolver_variant=" + (BenchmarkPlan.optimized() ? "B_REDESIGN" : "A_ORIGINAL") + "\nrequired_valid_per_phase=" + (benchmarkExperiment ? 1 : 3) + "\n");
        for (String string : stringArray4) {
            int n = validCounts.getOrDefault(string, 0);
            int n2 = attemptCounts.getOrDefault(string, 0);
            stringBuilder.append(string).append(": valid=").append(n).append(benchmarkExperiment ? "/1 attempts=" : "/3 attempts=").append(n2).append(" status=").append(n >= (benchmarkExperiment ? 1 : 3) ? "COMPLETE" : (n2 == 0 ? "NOT_RUN" : "INCOMPLETE_SEE_REJECTED")).append('\n');
        }
        stringBuilder.append("last_command_reasons=").append(CaptureValidity.explain(commandIssues)).append("\nNo GPU timings or draw calls recorded. Throne series A/B must never be pooled; solver arms are compared only within matching PAIR.\n");
        Files.writeString(directory.resolve("status.txt"), (CharSequence)stringBuilder, StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private static void writeStatusQuietly() {
        try {
            FpsDiagnostics.writeStatus();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static void writeFrames(Path path) throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);){
            int n;
            bufferedWriter.write("mode,attempt,frame,elapsed_s,frame_time_ms,instant_fps");
            for (String string : NAMES) {
                bufferedWriter.write("," + string);
            }
            bufferedWriter.write(",mage_render_calls,hair_solver_calls,gpu_render_ms,draw_calls,camera_x,camera_y,camera_z,camera_yaw,camera_pitch,flags");
            String[] stringArray = EXTRA;
            int n2 = stringArray.length;
            for (n = 0; n < n2; ++n) {
                String string;
                string = stringArray[n];
                bufferedWriter.write("," + string);
            }
            bufferedWriter.write(",world_game_time,npc_tick,npc_x,npc_y,npc_z,npc_scene,hair_input_sim_s,series,phase\n");
            long l = 0L;
            for (n = 0; n < count; ++n) {
                int n3;
                bufferedWriter.write(String.valueOf((Object)mode) + "," + String.valueOf(attemptCounts.getOrDefault(phase, 0)) + "," + n + "," + FpsDiagnostics.sec(l += frames[n]) + "," + FpsDiagnostics.ms(frames[n]) + "," + String.format(Locale.ROOT, "%.6f", 1.0E9 / (double)frames[n]));
                for (n3 = 0; n3 < 12; ++n3) {
                    bufferedWriter.write("," + (FpsDiagnostics.metricAvailable(n3) ? FpsDiagnostics.ms(samples[n3][n]) : ""));
                }
                bufferedWriter.write("," + draws[n] + "," + solves[n] + ",,");
                for (n3 = 0; n3 < 5; ++n3) {
                    bufferedWriter.write("," + FpsDiagnostics.num(cameras[n3][n]));
                }
                bufferedWriter.write("," + flags[n]);
                for (n3 = 0; n3 < EXTRA.length; ++n3) {
                    bufferedWriter.write("," + (extraRows[n3][n] < 0L ? "" : Long.toString(extraRows[n3][n])));
                }
                bufferedWriter.write("," + gameTimes[n] + "," + (npcTicks[n] < 0L ? "" : Long.toString(npcTicks[n])));
                for (n3 = 0; n3 < 5; ++n3) {
                    bufferedWriter.write("," + FpsDiagnostics.num(npcRows[n3][n]));
                }
                bufferedWriter.write("," + CaptureGuard.series + "," + phase + "\n");
            }
        }
    }

    private static void writeStageSummary(int n, int n2, long l) throws IOException {
        if (!HairStageProfiler.ENABLED) {
            return;
        }
        Path path = directory.resolve("stage-summary-valid.csv");
        boolean bl = Files.exists(path, new LinkOption[0]);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);){
            if (!bl) {
                bufferedWriter.write("series,phase,valid_repeat,attempt,frames,duration_s,metric,mean_ms_per_frame,median_ms_per_frame,p95_ms_per_frame\n");
            }
            for (int i = -1; i < 12; ++i) {
                if (i >= 0 && !FpsDiagnostics.metricAvailable(i)) continue;
                FrameStats frameStats = FrameStats.of(i < 0 ? frames : samples[i], count);
                String string = i < 0 ? "frame_time_ms" : NAMES[i];
                bufferedWriter.write(CaptureGuard.series + "," + phase + "," + n + "," + n2 + "," + count + "," + FpsDiagnostics.sec(l) + "," + string + "," + frameStats.meanMs() + "," + frameStats.medianMs() + "," + frameStats.p95Ms() + "\n");
            }
        }
    }

    private static boolean metricAvailable(int n) {
        return n != 2 && n != 3 && n != 4 && (n < 6 || HairStageProfiler.ENABLED);
    }

    private static String environment() {
        Minecraft minecraft = Minecraft.getInstance();
        Options options = minecraft.options;
        return "build=1.27.0-f3-hair-accum-r2b-jfrfix\nbase_git=5ddcc4858ae36c9d9ae87dff137285351bfd054e\nseries=" + CaptureGuard.series + "\njava=" + System.getProperty("java.version") + "\nos=" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\ncpu_available_processors=" + Runtime.getRuntime().availableProcessors() + "\nheap_max_bytes=" + Runtime.getRuntime().maxMemory() + "\ngl_renderer=" + GL11.glGetString((int)7937) + "\ngl_vendor=" + GL11.glGetString((int)7936) + "\ngl_version=" + GL11.glGetString((int)7938) + "\nresolution=" + minecraft.getWindow().getWidth() + "x" + minecraft.getWindow().getHeight() + "\nsettings_graphics_hash=" + CaptureGuard.graphics() + "\nfov=" + String.valueOf(options.fov().get()) + "\ngraphics=" + String.valueOf(options.graphicsMode().get()) + "\nrender_distance=" + String.valueOf(options.renderDistance().get()) + "\nsimulation_distance=" + String.valueOf(options.simulationDistance().get()) + "\nvsync=" + String.valueOf(options.enableVsync().get()) + "\nfps_cap=" + String.valueOf(options.framerateLimit().get()) + "\nbob=" + String.valueOf(options.bobView().get()) + "\nhair_stages_enabled=" + HairStageProfiler.ENABLED + "\n" + DiagnosticFixture.status() + "CPU timers are elapsed on client thread, not GPU or ThreadMXBean CPU. Allocation is whole client-thread allocation between frame probes, not solver-only. Eye/search metrics are unavailable: these systems are byte-identical to production.\nA=previous measured r1/T solver; B=r2/ZA: allocation-free self-pair delta; conservative no-op envelope rejection. Alpha division remains per projection in both. target180/max64/early exit unchanged. Resources unchanged.\nRedesign run: PAIR1_A,PAIR1_B,PAIR2_B,PAIR2_A,PAIR3_A,PAIR3_B. Same reference for all six.\nA/B compares incremental patches against r1, NOT original production. Constraints timer includes the full loop in both engines. Other timers unchanged.\nDefault/finish/stop returns to redesigned B. Arm changes reset the selected engine before warmup; never inside a measurement window. No acceptance before real-client paired results.\nA OUT_OF_VIEW = render exclusion; NPC still exists and ticks.\nB_ABSENT = actual zero RedMage in client level; no automatic removal. Throne must stay intact.\nCamera, graphics, NPC identity/position/body yaw/scene and throne are checked against ONE session reference, not reset every window.\nGuard polls frame Pre/Post and client ticks; menu opening is latched by event.\nOnly summary-valid.csv contains reportable windows. Rejected data are separate.\nGPU time/draw calls are unavailable, blank, not zero.\n";
    }

    private static String ms(long l) {
        return String.format(Locale.ROOT, "%.6f", (double)l / 1000000.0);
    }

    private static String sec(long l) {
        return String.format(Locale.ROOT, "%.9f", (double)l / 1.0E9);
    }

    private static String num(double d) {
        return Double.isFinite(d) ? String.format(Locale.ROOT, "%.6f", d) : "";
    }

    private FpsDiagnostics() {
    }

    static {
        inputSim = Double.NaN;
    }

    public static enum Mode {
        FULL,
        NO_HAIR_PHYSICS,
        NO_EYES,
        OUT_OF_VIEW,
        ABSENT;

    }
}

