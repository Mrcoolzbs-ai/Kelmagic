/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

import java.lang.reflect.Field;

final class FixtureHoldCounters {
    private final Field still;
    private final Field pose;
    private final Field life;
    private final int oldStill;
    private final int oldPose;
    private final int oldLife;

    FixtureHoldCounters(Object e) throws ReflectiveOperationException {
        this.still = FixtureHoldCounters.field(e, "stillTicks");
        this.pose = FixtureHoldCounters.field(e, "poseTicks");
        this.life = FixtureHoldCounters.field(e, "lifeTicks");
        this.oldStill = this.still.getInt(e);
        this.oldPose = this.pose.getInt(e);
        this.oldLife = this.life.getInt(e);
    }

    private static Field field(Object e, String n) throws ReflectiveOperationException {
        Field f = e.getClass().getDeclaredField(n);
        f.setAccessible(true);
        return f;
    }

    void apply(Object e) throws IllegalAccessException {
        this.still.setInt(e, -1000000);
        this.pose.setInt(e, 1000000);
        this.life.setInt(e, 1000000);
    }

    void restore(Object e) throws IllegalAccessException {
        this.still.setInt(e, this.oldStill);
        this.pose.setInt(e, this.oldPose);
        this.life.setInt(e, this.oldLife);
    }

    static boolean benchmarkReady(boolean integrated, boolean activeServerHold, boolean sameNpc, boolean noAi, boolean ordinaryIdle) {
        return integrated && activeServerHold && sameNpc && noAi && ordinaryIdle;
    }

    static boolean abortSceneChange(int issues) {
        return (issues & 0x28) != 0;
    }
}

