/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

public final class CaptureValidity {
    public static final int MENU = 1;
    public static final int UNFOCUSED = 2;
    public static final int PAUSED = 4;
    public static final int CAMERA = 8;
    public static final int NPC_COUNT = 16;
    public static final int NPC_STATE = 32;
    public static final int SETTINGS = 64;
    public static final int VISIBILITY = 128;
    public static final int CLOCK_STALLED = 256;
    public static final int HAIR_NOT_DRAWN = 512;
    public static final int SOLVER_COUNTERS = 1024;
    public static final int WORLD = 2048;
    public static final int THRONE = 4096;
    public static final int OVERFLOW = 8192;
    public static final int ABORTED = 16384;
    public static final int STAGE_COUNTERS = 32768;

    public static boolean clockStalled(long value, long previous, long nowNs, long lastChangeNs) {
        return value == previous && nowNs - lastChangeNs > 500000000L;
    }

    public static int ui(boolean menu, boolean focused, boolean paused) {
        return (menu ? 1 : 0) | (!focused ? 2 : 0) | (paused ? 4 : 0);
    }

    public static int actor(boolean present, boolean identity, boolean stable) {
        return !present || !identity ? 16 : (!stable ? 32 : 0);
    }

    public static int visibility(boolean visible, long renderCalls, long batches) {
        return (visible ? renderCalls == 0L : renderCalls != 0L) ? 128 : (visible && batches == 0L ? 512 : 0);
    }

    public static boolean sameHair(long render, long batches, long quads, long refRender, long refBatches, long refQuads) {
        return batches * refRender == refBatches * render && quads * refRender == refQuads * render;
    }

    public static int counters(boolean visible, boolean disabled, long calls, long completed, long steps, long ns, long hairBatches) {
        int flags = 0;
        if (visible && hairBatches == 0L) {
            flags |= 0x200;
        }
        if (calls != completed) {
            flags |= 0x400;
        }
        if (disabled || !visible) {
            if (calls != 0L || ns != 0L || steps != 0L) {
                flags |= 0x400;
            }
        } else if (calls == 0L || steps == 0L || ns <= 0L) {
            flags |= 0x400;
        }
        return flags;
    }

    public static String explain(int bits) {
        String[] names = new String[]{"MENU", "UNFOCUSED", "PAUSED", "CAMERA_MOVED", "NPC_COUNT_OR_MISSING", "NPC_MOVED_ROTATED_SCENE_CHANGED", "GRAPHICS_CHANGED", "UNEXPECTED_RENDER_VISIBILITY", "SIM_CLOCK_STALLED", "HAIR_GEOMETRY_MISSING_OR_CHANGED", "SOLVER_CALL_TIME_PROGRESS_MISMATCH", "WORLD_CHANGED", "THRONE_CHANGED", "BUFFER_OVERFLOW", "ABORTED", "HAIR_STAGE_ACCOUNTING_MISMATCH"};
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < names.length; ++i) {
            if ((bits & 1 << i) == 0) continue;
            if (!s.isEmpty()) {
                s.append('|');
            }
            s.append(names[i]);
        }
        return s.isEmpty() ? "OK" : s.toString();
    }

    private CaptureValidity() {
    }
}

