/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Camera
 *  net.minecraft.client.CloudStatus
 *  net.minecraft.client.GraphicsStatus
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package dev.kelmagic.client.diag;

import com.mojang.blaze3d.platform.Window;
import dev.kelmagic.client.diag.CaptureValidity;
import dev.kelmagic.entity.RedMage;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class CaptureGuard {
    static String series = "";
    static final Set<Integer> mageIds = new HashSet<Integer>();
    static Reference reference;
    static long lastGameTime;
    static long lastClockNs;
    static long lastNpcClockNs;
    static int lastNpcTick;
    static int observedIssues;
    static long refHairBatches;
    static long refHairQuads;
    static long refRenderCalls;

    static void session(String name) {
        series = name;
        reference = null;
        observedIssues = 0;
        refRenderCalls = 0L;
        refHairQuads = 0L;
        refHairBatches = 0L;
        mageIds.clear();
    }

    static void index() {
        Minecraft m = Minecraft.getInstance();
        mageIds.clear();
        if (m.level != null) {
            for (Entity e : m.level.entitiesForRendering()) {
                if (!(e instanceof RedMage) || e.isRemoved()) continue;
                mageIds.add(e.getId());
            }
        }
    }

    static void establish() {
        Minecraft m = Minecraft.getInstance();
        CaptureGuard.index();
        if (mageIds.size() != 1) {
            throw new IllegalStateException("\u041d\u0443\u0436\u043d\u0430 \u0440\u043e\u0432\u043d\u043e \u043e\u0434\u043d\u0430 \u0447\u0430\u0440\u043e\u0434\u0435\u0439\u043a\u0430");
        }
        RedMage e = (RedMage)m.level.getEntity(mageIds.iterator().next().intValue());
        if (e == null || e.kitten()) {
            throw new IllegalStateException("\u041d\u0443\u0436\u043d\u0430 \u0432\u0438\u0434\u0438\u043c\u0430\u044f \u0433\u0443\u043c\u0430\u043d\u043e\u0438\u0434\u043d\u0430\u044f \u0447\u0430\u0440\u043e\u0434\u0435\u0439\u043a\u0430, \u043d\u0435 kitten");
        }
        boolean seated = e.throne().isPresent();
        if (series.equals("A") && seated) {
            throw new IllegalStateException("\u0421\u0435\u0440\u0438\u044f A \u0434\u043e\u043b\u0436\u043d\u0430 \u0431\u044b\u0442\u044c \u0431\u0435\u0437 \u0442\u0440\u043e\u043d\u0430");
        }
        if (series.equals("B") && !seated) {
            throw new IllegalStateException("B_PRESENT \u0442\u0440\u0435\u0431\u0443\u0435\u0442 \u0447\u0430\u0440\u043e\u0434\u0435\u0439\u043a\u0443 \u043d\u0430 \u0442\u0440\u043e\u043d\u0435");
        }
        Camera c = m.gameRenderer.getMainCamera();
        Vec3 p = c.getPosition();
        BlockPos throne = e.throne().orElse(null);
        reference = new Reference((Level)m.level, p.x, p.y, p.z, c.getYRot(), c.getXRot(), CaptureGuard.graphics(), e.getId(), e.getUUID(), e.getX(), e.getY(), e.getZ(), e.yBodyRot, e.getYRot(), e.scene(), e.isNoAi(), e.isNoGravity(), throne, throne == null ? null : m.level.getBlockState(throne), throne == null ? null : m.level.getBlockState(throne.above()));
    }

    static void beginWindow() {
        observedIssues = 0;
        lastGameTime = Minecraft.getInstance().level.getGameTime();
        lastClockNs = lastNpcClockNs = System.nanoTime();
        Entity e = Minecraft.getInstance().level.getEntity(CaptureGuard.reference.npcId);
        lastNpcTick = e == null ? -1 : e.tickCount;
    }

    static int poll(boolean absent) {
        Minecraft m = Minecraft.getInstance();
        Reference r = reference;
        int f = CaptureValidity.ui(m.screen != null || m.getOverlay() != null, m.isWindowActive(), m.isPaused());
        if (r == null || m.level != r.level || m.player == null) {
            observedIssues |= f | 0x800;
            return f | 0x800;
        }
        Camera c = m.gameRenderer.getMainCamera();
        Vec3 p = c.getPosition();
        if (Math.abs(p.x - r.x) > 0.001 || Math.abs(p.y - r.y) > 0.001 || Math.abs(p.z - r.z) > 0.001 || CaptureGuard.angle(c.getYRot(), r.yaw) > 0.01 || CaptureGuard.angle(c.getXRot(), r.pitch) > 0.01) {
            f |= 8;
        }
        if (CaptureGuard.graphics() != r.graphics) {
            f |= 0x40;
        }
        if (mageIds.size() != (absent ? 0 : 1)) {
            f |= 0x10;
        }
        if (!absent) {
            RedMage e;
            Entity entity = m.level.getEntity(r.npcId);
            if (!(entity instanceof RedMage) || (e = (RedMage)entity).isRemoved() || !e.isAlive() || !e.getUUID().equals(r.npcUuid)) {
                f |= CaptureValidity.actor(false, false, false);
            } else {
                long nowNpc = System.nanoTime();
                if (e.tickCount != lastNpcTick) {
                    lastNpcTick = e.tickCount;
                    lastNpcClockNs = nowNpc;
                } else if (nowNpc - lastNpcClockNs > 500000000L) {
                    f |= 0x100;
                }
                if (Math.abs(e.getX() - r.nx) > 0.001 || Math.abs(e.getY() - r.ny) > 0.001 || Math.abs(e.getZ() - r.nz) > 0.001 || CaptureGuard.angle(e.yBodyRot, r.bodyYaw) > 0.01 || CaptureGuard.angle(e.getYRot(), r.entityYaw) > 0.01 || e.scene() != r.scene || e.isNoAi() != r.noAi || e.isNoGravity() != r.noGravity || !Objects.equals(e.throne().orElse(null), r.throne)) {
                    f |= CaptureValidity.actor(true, true, false);
                }
            }
        }
        if (r.throne != null && (m.level.getBlockState(r.throne) != r.lower || m.level.getBlockState(r.throne.above()) != r.upper)) {
            f |= 0x1000;
        }
        long now = System.nanoTime();
        long t = m.level.getGameTime();
        if (CaptureValidity.clockStalled(t, lastGameTime, now, lastClockNs)) {
            f |= 0x100;
        }
        if (t != lastGameTime) {
            lastGameTime = t;
            lastClockNs = now;
        }
        CaptureGuard.mark(f);
        return f;
    }

    static int hairShape(long render, long batches, long quads, boolean eligibleReference) {
        if (render <= 0L || batches <= 0L || quads <= 0L) {
            return 512;
        }
        if (refRenderCalls == 0L && eligibleReference) {
            refRenderCalls = render;
            refHairBatches = batches;
            refHairQuads = quads;
        }
        return refRenderCalls > 0L && !CaptureValidity.sameHair(render, batches, quads, refRenderCalls, refHairBatches, refHairQuads) ? 512 : 0;
    }

    static void mark(int bits) {
        observedIssues |= bits;
    }

    static double angle(double a, double b) {
        double d = (a - b) % 360.0;
        return Math.abs(d > 180.0 ? d - 360.0 : (d < -180.0 ? d + 360.0 : d));
    }

    static long graphics() {
        Minecraft m = Minecraft.getInstance();
        Options o = m.options;
        Window w = m.getWindow();
        long h = 17L;
        h = 31L * h + (long)w.getWidth();
        h = 31L * h + (long)w.getHeight();
        h = 31L * h + (long)((Integer)o.fov().get()).intValue();
        h = 31L * h + (long)((GraphicsStatus)o.graphicsMode().get()).ordinal();
        h = 31L * h + (long)((Integer)o.renderDistance().get()).intValue();
        h = 31L * h + (long)((Integer)o.simulationDistance().get()).intValue();
        h = 31L * h + (long)((Integer)o.framerateLimit().get()).intValue();
        h = 31L * h + (long)((Boolean)o.enableVsync().get() != false ? 1 : 0);
        h = 31L * h + (long)o.getCameraType().ordinal();
        h = 31L * h + (long)((Boolean)o.bobView().get() != false ? 1 : 0);
        h = 31L * h + (long)((CloudStatus)o.cloudStatus().get()).ordinal();
        h = 31L * h + (long)((ParticleStatus)o.particles().get()).ordinal();
        h = 31L * h + (long)((Integer)o.mipmapLevels().get()).intValue();
        h = 31L * h + Double.doubleToLongBits((Double)o.entityDistanceScaling().get());
        h = 31L * h + Double.doubleToLongBits((Double)o.gamma().get());
        h = 31L * h + (long)((Boolean)o.entityShadows().get() != false ? 1 : 0);
        h = 31L * h + (long)((Boolean)o.ambientOcclusion().get() != false ? 1 : 0);
        h = 31L * h + (long)(o.hideGui ? 1 : 0);
        h = 31L * h + (long)(m.getDebugOverlay().showDebugScreen() ? 1 : 0);
        return h;
    }

    private CaptureGuard() {
    }

    record Reference(Level level, double x, double y, double z, float yaw, float pitch, long graphics, int npcId, UUID npcUuid, double nx, double ny, double nz, float bodyYaw, float entityYaw, int scene, boolean noAi, boolean noGravity, BlockPos throne, Object lower, Object upper) {
    }
}

