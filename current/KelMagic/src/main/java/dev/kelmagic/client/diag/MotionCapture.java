/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientChatEvent
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderFrameEvent$Post
 *  net.neoforged.neoforge.client.event.RenderFrameEvent$Pre
 *  net.neoforged.neoforge.client.event.ScreenEvent$Opening
 *  org.lwjgl.opengl.GL11
 */
package dev.kelmagic.client.diag;

import dev.kelmagic.client.diag.CaptureGuard;
import dev.kelmagic.client.diag.CaptureValidity;
import dev.kelmagic.client.diag.FpsDiagnostics;
import dev.kelmagic.client.diag.FrameAllocation;
import dev.kelmagic.client.diag.FrameStats;
import dev.kelmagic.client.diag.HairStageProfiler;
import dev.kelmagic.client.diag.MotionCsv;
import dev.kelmagic.client.diag.MotionRules;
import dev.kelmagic.entity.RedMage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid="kelmagic", value={Dist.CLIENT})
public final class MotionCapture {
    private static final boolean ENABLED = Boolean.getBoolean("kelmagic.fpsdiag");
    private static final int CAPACITY = 8192;
    private static final long WARM = 5000000000L;
    private static final long WINDOW = 15000000000L;
    private static boolean running;
    private static boolean recording;
    private static long warmUntil;
    private static long start;
    private static long last;
    private static long allocationStart;
    private static long lastWorldTick;
    private static long lastNpcNs;
    private static long lastWorldNs;
    private static int lastNpcTick;
    private static int count;
    private static int frameIssues;
    private static int allIssues;
    private static long[][] rows;
    private static double[][] pose;
    private static int[] flags;
    private static CaptureGuard.Reference reference;
    private static Path directory;
    private static Recording flight;
    private static String flightStatus;

    private static void say(String string) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage((Component)Component.literal((String)("[KelMagic motion] " + string)), false);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void chat(ClientChatEvent clientChatEvent) {
        String string = clientChatEvent.getMessage().trim();
        if (running && (string.equals("!kfps") || string.startsWith("!kfps "))) {
            clientChatEvent.setCanceled(true);
            MotionCapture.say("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 !khair stop; \u043d\u0435 \u0441\u043c\u0435\u0448\u0438\u0432\u0430\u0442\u044c motion \u0438 \u0441\u0442\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 FPSDiag.");
            return;
        }
        if (!string.equals("!khair") && !string.startsWith("!khair ")) {
            return;
        }
        clientChatEvent.setCanceled(true);
        if (string.equals("!khair stop")) {
            if (running) {
                MotionCapture.finish(16384);
            }
            return;
        }
        if (!string.equals("!khair motion")) {
            MotionCapture.say("!khair motion \u2014 5\u0441 \u043f\u0440\u043e\u0433\u0440\u0435\u0432 +15\u0441 \u043d\u0430\u0431\u043b\u044e\u0434\u0435\u043d\u0438\u0435; !khair stop \u2014 \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c.");
            return;
        }
        if (running) {
            MotionCapture.say("\u0417\u0430\u043f\u0438\u0441\u044c \u0443\u0436\u0435 \u0437\u0430\u043f\u0443\u0449\u0435\u043d\u0430.");
            return;
        }
        if (!ENABLED || !HairStageProfiler.ENABLED) {
            MotionCapture.say("\u041d\u0443\u0436\u043d\u044b -Dkelmagic.fpsdiag=true -Dkelmagic.fpsdiag.hairStages=true");
            return;
        }
        FpsDiagnostics.chat(new ClientChatEvent("!kfps stop"));
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            MotionCapture.say("\u0421\u043d\u0430\u0447\u0430\u043b\u0430 \u0432\u043e\u0439\u0442\u0438 \u0432 \u0442\u0435\u0441\u0442\u043e\u0432\u044b\u0439 \u043c\u0438\u0440.");
            return;
        }
        List<RedMage> list = MotionCapture.findMages();
        if (list.size() != 1 || list.getFirst().isNoAi() || list.getFirst().throne().isPresent()) {
            MotionCapture.say("\u041d\u0443\u0436\u043d\u0430 \u043e\u0434\u043d\u0430 \u0436\u0438\u0432\u0430\u044f \u0447\u0430\u0440\u043e\u0434\u0435\u0439\u043a\u0430 \u0431\u0435\u0437 \u0442\u0440\u043e\u043d\u0430 \u0438 \u0431\u0435\u0437 hold/NoAI. \u0421\u043d\u0430\u0447\u0430\u043b\u0430 !kfps release.");
            return;
        }
        try {
            directory = minecraft.gameDirectory.toPath().resolve("kelmagic-hair-motion-r2").resolve(Instant.now().toString().replace(':', '-'));
            Files.createDirectories(directory, new FileAttribute[0]);
            Files.writeString(directory.resolve("environment.txt"), (CharSequence)("build=1.27.0-f3-hair-accum-r2b-jfrfix\nprotocol=OBSERVATIONAL_NPC_MOTION_NOT_PAIRED_AB\nvariant=B_r2_ZA\njava=" + System.getProperty("java.version") + "\nos=" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + "\nprocessors=" + Runtime.getRuntime().availableProcessors() + "\nheap_max_bytes=" + Runtime.getRuntime().maxMemory() + "\ngl_renderer=" + GL11.glGetString((int)7937) + "\ngl_vendor=" + GL11.glGetString((int)7936) + "\ngl_version=" + GL11.glGetString((int)7938) + "\nresolution=" + minecraft.getWindow().getWidth() + "x" + minecraft.getWindow().getHeight() + "\ngraphics_hash=" + CaptureGuard.graphics() + "\nhair_180Hz_max64_grid9x19=true\n"), StandardCharsets.UTF_8, new OpenOption[0]);
            rows = new long[8192][15];
            pose = new double[8192][10];
            flags = new int[8192];
            allIssues = 0;
            frameIssues = 0;
            count = 0;
            reference = null;
            HairStageProfiler.active(false);
            HairStageProfiler.calibrate();
            FrameAllocation.read();
            MotionCapture.startFlight();
            running = true;
            recording = false;
            warmUntil = System.nanoTime() + 5000000000L;
            MotionCapture.say("\u041d\u0430\u0431\u043b\u044e\u0434\u0435\u043d\u0438\u0435 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f NPC: 5\u0441 \u043f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u043a\u0430, \u0437\u0430\u0442\u0435\u043c15\u0441. \u041a\u0430\u043c\u0435\u0440\u0443/\u0433\u0440\u0430\u0444\u0438\u043a\u0443 \u043d\u0435 \u043c\u0435\u043d\u044f\u0442\u044c; NPC \u0434\u043e\u043b\u0436\u043d\u0430 \u043e\u0441\u0442\u0430\u0432\u0430\u0442\u044c\u0441\u044f \u0432 \u043a\u0430\u0434\u0440\u0435. \u042d\u0442\u043e \u041d\u0415 \u043f\u0430\u0440\u043d\u044b\u0439 benchmark.");
            minecraft.setScreen(null);
        }
        catch (Exception exception) {
            MotionCapture.closeFlight();
            MotionCapture.say("\u041d\u0435 \u043d\u0430\u0447\u0430\u0442\u043e: " + String.valueOf(exception));
        }
    }

    private static List<RedMage> findMages() {
        ArrayList<RedMage> arrayList = new ArrayList<RedMage>();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            for (Entity entity : minecraft.level.entitiesForRendering()) {
                RedMage redMage;
                if (!(entity instanceof RedMage) || (redMage = (RedMage)entity).isRemoved() || !redMage.isAlive()) continue;
                arrayList.add(redMage);
            }
        }
        return arrayList;
    }

    private static CaptureGuard.Reference snapshot(RedMage redMage) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 vec3 = camera.getPosition();
        return new CaptureGuard.Reference((Level)minecraft.level, vec3.x, vec3.y, vec3.z, camera.getYRot(), camera.getXRot(), CaptureGuard.graphics(), redMage.getId(), redMage.getUUID(), redMage.getX(), redMage.getY(), redMage.getZ(), redMage.yBodyRot, redMage.getYRot(), redMage.scene(), redMage.isNoAi(), redMage.isNoGravity(), null, null, null);
    }

    private static int poll() {
        Minecraft minecraft = Minecraft.getInstance();
        int n = CaptureValidity.ui(minecraft.screen != null || minecraft.getOverlay() != null, minecraft.isWindowActive(), minecraft.isPaused());
        if (reference == null || minecraft.level != reference.level() || minecraft.player == null) {
            return n | 0x800;
        }
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 vec3 = camera.getPosition();
        if (Math.abs(vec3.x - reference.x()) > 0.001 || Math.abs(vec3.y - reference.y()) > 0.001 || Math.abs(vec3.z - reference.z()) > 0.001 || CaptureGuard.angle(camera.getYRot(), reference.yaw()) > 0.01 || CaptureGuard.angle(camera.getXRot(), reference.pitch()) > 0.01) {
            n |= 8;
        }
        if (CaptureGuard.graphics() != reference.graphics()) {
            n |= 0x40;
        }
        RedMage redMage = null;
        int n2 = 0;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            RedMage redMage2;
            if (!(entity instanceof RedMage) || (redMage2 = (RedMage)entity).isRemoved() || !redMage2.isAlive()) continue;
            ++n2;
            redMage = redMage2;
        }
        if (n2 != 1 || redMage == null) {
            return n | 0x10;
        }
        n |= MotionRules.actor(redMage.getId() == reference.npcId() && redMage.getUUID().equals(reference.npcUuid()), redMage.scene() == reference.scene(), redMage.isNoAi() == reference.noAi(), redMage.isNoGravity() == reference.noGravity(), redMage.throne().isEmpty());
        long l = System.nanoTime();
        long l2 = minecraft.level.getGameTime();
        if (CaptureValidity.clockStalled(l2, lastWorldTick, l, lastWorldNs)) {
            n |= 0x100;
        }
        if (l2 != lastWorldTick) {
            lastWorldTick = l2;
            lastWorldNs = l;
        }
        if (redMage.tickCount != lastNpcTick) {
            lastNpcTick = redMage.tickCount;
            lastNpcNs = l;
        } else if (l - lastNpcNs > 500000000L) {
            n |= 0x100;
        }
        return n;
    }

    private static void mark(int n) {
        frameIssues |= n;
        allIssues |= n;
    }

    @SubscribeEvent
    public static void screen(ScreenEvent.Opening opening) {
        if (recording && opening.getNewScreen() != null) {
            MotionCapture.mark(1);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post post) {
        if (recording) {
            MotionCapture.mark(MotionCapture.poll());
        }
    }

    @SubscribeEvent
    public static void post(RenderFrameEvent.Post post) {
        if (recording) {
            MotionCapture.mark(MotionCapture.poll());
        }
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Pre pre) {
        if (!running) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        long l = System.nanoTime();
        if (minecraft.level == null || minecraft.player == null) {
            MotionCapture.finish(2048);
            return;
        }
        if (!recording) {
            if (l < warmUntil) {
                return;
            }
            List<RedMage> list = MotionCapture.findMages();
            if (list.size() != 1) {
                MotionCapture.finish(16);
                return;
            }
            reference = MotionCapture.snapshot(list.getFirst());
            lastNpcNs = lastWorldNs = l;
            last = lastWorldNs;
            start = lastWorldNs;
            lastWorldTick = minecraft.level.getGameTime();
            lastNpcTick = list.getFirst().tickCount;
            recording = true;
            allIssues = 0;
            frameIssues = 0;
            HairStageProfiler.reset();
            HairStageProfiler.active(true);
            allocationStart = FrameAllocation.read();
            return;
        }
        if (FpsDiagnostics.measuring()) {
            MotionCapture.finish(16384);
            return;
        }
        if (count == 8192) {
            MotionCapture.finish(8192);
            return;
        }
        long[] lArray = rows[count];
        lArray[0] = l - last;
        for (int i = 0; i < 5; ++i) {
            lArray[1 + i] = HairStageProfiler.nanos(i);
        }
        lArray[6] = HairStageProfiler.samples();
        lArray[7] = HairStageProfiler.iterations();
        lArray[8] = HairStageProfiler.linkVisits();
        lArray[9] = HairStageProfiler.bodyContacts();
        lArray[10] = HairStageProfiler.armContacts();
        lArray[11] = HairStageProfiler.selfContacts();
        long l2 = FrameAllocation.read();
        lArray[12] = l2 < 0L || allocationStart < 0L ? -1L : l2 - allocationStart;
        lArray[13] = HairStageProfiler.substeps();
        lArray[14] = HairStageProfiler.overheadEstimateNs();
        MotionCapture.mark(MotionCapture.poll());
        if (lArray[6] == 0L) {
            MotionCapture.mark(128);
        }
        if (!HairStageProfiler.balanced(lArray[6])) {
            MotionCapture.mark(32768);
        }
        MotionCapture.flags[MotionCapture.count] = frameIssues;
        double[] dArray = pose[count];
        Arrays.fill(dArray, Double.NaN);
        Entity entity = minecraft.level.getEntity(reference.npcId());
        if (entity instanceof RedMage) {
            RedMage redMage = (RedMage)entity;
            dArray[0] = redMage.getX();
            dArray[1] = redMage.getY();
            dArray[2] = redMage.getZ();
            dArray[3] = redMage.yBodyRot;
            dArray[4] = redMage.yHeadRot;
            dArray[5] = redMage.getXRot();
            dArray[6] = redMage.tickCount;
            dArray[7] = minecraft.level.getGameTime();
            dArray[8] = redMage.scene();
            dArray[9] = HairStageProfiler.inputTime();
        }
        ++count;
        last = l;
        frameIssues = 0;
        HairStageProfiler.reset();
        allocationStart = FrameAllocation.read();
        if (l - start >= 15000000000L) {
            MotionCapture.finish(0);
        }
    }

    private static void finish(int n) {
        HairStageProfiler.active(false);
        recording = false;
        running = false;
        allIssues |= n;
        MotionCapture.closeFlight();
        if (directory == null) {
            return;
        }
        try {
            MotionCsv.write(directory.resolve("motion-frames.csv"), rows, pose, flags, count);
            long[] lArray = new long[count];
            for (int i = 0; i < count; ++i) {
                lArray[i] = rows[i][0];
            }
            String string = count == 0 ? "no_frames\n" : FrameStats.of(lArray, count).toString() + "\n";
            String string2 = "build=1.27.0-f3-hair-accum-r2b-jfrfix\nprotocol=OBSERVATIONAL_NPC_MOTION_NOT_PAIRED_AB\nframes=" + count + "\nissues=" + CaptureValidity.explain(allIssues) + "\nquality_checks_clean=" + (allIssues == 0) + "\n" + string + "jfr=" + flightStatus + "\nreference=" + String.valueOf(reference) + "\n";
            double d = 0.0;
            double d2 = 0.0;
            int n2 = 0;
            for (int i = 1; i < count; ++i) {
                double d3;
                double d4 = pose[i][0] - pose[i - 1][0];
                double d5 = pose[i][1] - pose[i - 1][1];
                double d6 = pose[i][2] - pose[i - 1][2];
                double d7 = Math.sqrt(d4 * d4 + d5 * d5 + d6 * d6);
                if (Double.isFinite(d7)) {
                    d += d7;
                    if (d7 > 1.0E-5) {
                        ++n2;
                    }
                }
                if (!Double.isFinite(d3 = pose[i][3] - pose[i - 1][3])) continue;
                d2 += Math.abs(Math.IEEEremainder(d3, 360.0));
            }
            string2 = string2 + "motion_path_blocks=" + d + "\ntranslation_frames=" + n2 + "\nbody_yaw_travel_degrees=" + d2 + "\n";
            string2 = string2 + "NPC position/body/head rotations intentionally allowed and recorded; camera, UI/focus, world, graphics, identity, scene/AI/gravity/throne remain guarded. All rows retained. No GPU timing. CPU-wall stage times include observer costs. Allocation is whole client-thread interval, not solver-only. JFR includes five-second warmup and is not zero-overhead. No repeatable-path or visual-equivalence claim.\n";
            Files.writeString(directory.resolve("motion-summary.txt"), (CharSequence)string2, StandardCharsets.UTF_8, new OpenOption[0]);
            Files.writeString(directory.resolve("timer-calibration.txt"), (CharSequence)HairStageProfiler.calibration(), StandardCharsets.UTF_8, new OpenOption[0]);
            MotionCapture.say("Motion \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d: " + count + " \u043a\u0430\u0434\u0440\u043e\u0432; " + CaptureValidity.explain(allIssues) + ". \u041f\u0440\u0438\u0448\u043b\u0438 \u0432\u0441\u044e kelmagic-hair-motion-r2/" + String.valueOf(directory.getFileName()));
        }
        catch (Exception exception) {
            MotionCapture.say("\u041e\u0448\u0438\u0431\u043a\u0430 \u044d\u043a\u0441\u043f\u043e\u0440\u0442\u0430 motion: " + String.valueOf(exception));
        }
    }

    private static void startFlight() {
        flightStatus = "disabled";
        if (!Boolean.getBoolean("kelmagic.fpsdiag.motionJfr")) {
            return;
        }
        try {
            flight = new Recording(Configuration.getConfiguration("profile"));
            for (String string : new String[]{"jdk.InitialEnvironmentVariable", "jdk.InitialSystemProperty", "jdk.JVMInformation", "jdk.SystemProcess", "jdk.ProcessStart", "jdk.FileRead", "jdk.FileWrite", "jdk.SocketRead", "jdk.SocketWrite"}) {
                flight.disable(string);
            }
            flight.setToDisk(false);
            flight.setMaxSize(0x4000000L);
            flight.setMaxAge(Duration.ofSeconds(40L));
            flight.start();
            flightStatus = "recording_with_warmup";
        }
        catch (Exception exception) {
            flightStatus = "unavailable:" + exception.getClass().getSimpleName();
            if (flight != null) {
                flight.close();
            }
            flight = null;
        }
    }

    private static void closeFlight() {
        if (flight == null) {
            return;
        }
        try {
            flight.dump(directory.resolve("motion-profile.jfr"));
            flight.stop();
            flightStatus = "saved_with_warmup";
        }
        catch (Exception exception) {
            flightStatus = "export_failed:" + exception.getClass().getSimpleName();
        }
        finally {
            flight.close();
            flight = null;
        }
    }

    private MotionCapture() {
    }

    static {
        flightStatus = "disabled";
    }
}

