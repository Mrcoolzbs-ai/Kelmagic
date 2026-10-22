/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

public final class BenchmarkPlan {
    private static final boolean ENABLED = Boolean.getBoolean("kelmagic.fpsdiag");
    private static final String[] LABELS = new String[]{"PAIR1_A", "PAIR1_B", "PAIR2_B", "PAIR2_A", "PAIR3_A", "PAIR3_B"};
    private static final boolean[] ARMS = new boolean[]{false, true, true, false, false, true};
    private static boolean active;
    private static boolean optimized;
    private static int index;

    static void start() {
        if (!ENABLED) {
            throw new IllegalStateException("diagnostics disabled");
        }
        active = true;
        index = 0;
        optimized = false;
    }

    static void selectArm(boolean b) {
        if (!active) {
            throw new IllegalStateException("no active A/B");
        }
        optimized = b;
    }

    public static boolean optimized() {
        return !ENABLED || !active || optimized;
    }

    static String label() {
        return LABELS[index];
    }

    static String[] labels() {
        return (String[])LABELS.clone();
    }

    static boolean next() {
        if (++index >= LABELS.length) {
            index = LABELS.length - 1;
            BenchmarkPlan.cancel();
            return false;
        }
        optimized = ARMS[index];
        return true;
    }

    static void cancel() {
        active = false;
        optimized = false;
    }

    private BenchmarkPlan() {
    }
}

