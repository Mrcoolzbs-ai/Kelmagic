/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

import java.util.Arrays;

public final class HairStageProfiler {
    public static final boolean ENABLED = Boolean.getBoolean("kelmagic.fpsdiag") && Boolean.getBoolean("kelmagic.fpsdiag.hairStages");
    public static final int CONSTRAINTS = 0;
    public static final int CONTACTS = 1;
    public static final int SELF = 2;
    public static final int OTHER = 3;
    public static final int TOTAL = 4;
    private static final boolean WORK = Boolean.getBoolean("kelmagic.fpsdiag");
    private static long iterations;
    private static long visits;
    private static long body;
    private static long arm;
    private static long self;
    private static long substeps;
    private static double inputTime;
    private static final long[] ns;
    private static boolean active;
    private static long base0;
    private static long base1;
    private static long base2;
    private static long pairs;
    private static long samples;
    private static double estimatedNsPerPair;
    private static String calibration;
    private static long calibrationSink;

    public static void substep() {
        if (WORK) {
            ++substeps;
        }
    }

    public static long substeps() {
        return substeps;
    }

    public static void inputTime(double d) {
        if (WORK) {
            inputTime = d;
        }
    }

    public static double inputTime() {
        return inputTime;
    }

    public static void iteration() {
        if (WORK) {
            ++iterations;
            visits += 888L;
        }
    }

    public static void contacts(int n, int n2, int n3) {
        if (WORK) {
            body += (long)n;
            arm += (long)n2;
            self += (long)n3;
        }
    }

    public static long iterations() {
        return iterations;
    }

    public static long linkVisits() {
        return visits;
    }

    public static long bodyContacts() {
        return body;
    }

    public static long armContacts() {
        return arm;
    }

    public static long selfContacts() {
        return self;
    }

    public static void active(boolean bl) {
        active = ENABLED && bl;
    }

    public static boolean active() {
        return ENABLED && active;
    }

    public static void reset() {
        Arrays.fill(ns, 0L);
        samples = 0L;
        pairs = 0L;
        substeps = 0L;
        self = 0L;
        arm = 0L;
        body = 0L;
        visits = 0L;
        iterations = 0L;
        inputTime = Double.NaN;
    }

    public static long nanos(int n) {
        return ns[n];
    }

    public static long pairs() {
        return pairs;
    }

    public static long samples() {
        return samples;
    }

    public static long overheadEstimateNs() {
        return Math.round((double)pairs * estimatedNsPerPair);
    }

    public static long beginSample() {
        if (!ENABLED || !active) {
            return 0L;
        }
        base0 = ns[0];
        base1 = ns[1];
        base2 = ns[2];
        return System.nanoTime();
    }

    public static long beginStage() {
        return ENABLED && active ? System.nanoTime() : 0L;
    }

    public static void endStage(int n, long l) {
        if (l == 0L) {
            return;
        }
        long l2 = System.nanoTime() - l;
        int n2 = n;
        ns[n2] = ns[n2] + l2;
        ++pairs;
    }

    public static void endSample(long l) {
        if (l == 0L) {
            return;
        }
        long l2 = System.nanoTime() - l;
        ns[3] = ns[3] + (l2 - (ns[0] - base0) - (ns[1] - base1) - (ns[2] - base2));
        ns[4] = ns[4] + l2;
        ++pairs;
        ++samples;
    }

    public static boolean balanced(long l) {
        return samples == l && ns[0] >= 0L && ns[1] >= 0L && ns[2] >= 0L && ns[3] >= 0L && ns[4] == ns[0] + ns[1] + ns[2] + ns[3];
    }

    public static void calibrate() {
        int n;
        if (!ENABLED) {
            calibration = "disabled";
            return;
        }
        if (active) {
            throw new IllegalStateException("calibration during capture");
        }
        long[] lArray = new long[128];
        long[] lArray2 = new long[128];
        long[] lArray3 = new long[128];
        active = true;
        for (n = 0; n < 32768; ++n) {
            long l = HairStageProfiler.beginStage();
            HairStageProfiler.endStage(0, l);
        }
        HairStageProfiler.reset();
        for (n = 0; n < 128; ++n) {
            if ((n & 1) == 0) {
                lArray2[n] = HairStageProfiler.controlLoop(512);
                lArray[n] = HairStageProfiler.timedLoop(512);
            } else {
                lArray[n] = HairStageProfiler.timedLoop(512);
                lArray2[n] = HairStageProfiler.controlLoop(512);
            }
            lArray3[n] = Math.max(0L, lArray[n] - lArray2[n]);
        }
        active = false;
        HairStageProfiler.reset();
        long l = 0L;
        long l2 = 0L;
        long l3 = 0L;
        for (int i = 0; i < 128; ++i) {
            l += lArray3[i];
            l2 += lArray[i];
            l3 += lArray2[i];
        }
        estimatedNsPerPair = (double)l / 65536.0;
        long[] lArray4 = (long[])lArray3.clone();
        Arrays.sort(lArray4);
        calibration = "method=empty_instrumented_block_minus_primitive_counter_control; alternating_batch_order\nbatches=128\npairs_per_batch=512\nraw_mean_ns_per_pair=" + (double)l2 / 65536.0 + "\ncontrol_mean_ns_per_pair=" + (double)l3 / 65536.0 + "\nestimated_added_mean_ns_per_pair=" + estimatedNsPerPair + "\nestimated_added_median_ns_per_pair=" + ((double)lArray4[63] + (double)lArray4[64]) / 1024.0 + "\nestimated_added_p95_batch_ns_per_pair=" + (double)lArray4[(int)Math.ceil(121.6) - 1] / 512.0 + "\nwarning=Harness estimate only; not exact solver slowdown or upper bound; uncorrected gross timings retained. OTHER includes unassigned timer/bookkeeping costs.\n";
    }

    private static long timedLoop(int n) {
        long l = System.nanoTime();
        for (int i = 0; i < n; ++i) {
            long l2 = HairStageProfiler.beginStage();
            HairStageProfiler.endStage(0, l2);
        }
        return System.nanoTime() - l;
    }

    private static long controlLoop(int n) {
        long l = System.nanoTime();
        for (int i = 0; i < n; ++i) {
            ns[0] = ns[0] + (long)(i & 1);
            ++pairs;
        }
        calibrationSink = ns[0];
        return System.nanoTime() - l;
    }

    public static String calibration() {
        return "hair_stages_enabled=" + ENABLED + "\n" + calibration;
    }

    private HairStageProfiler() {
    }

    static {
        inputTime = Double.NaN;
        ns = new long[5];
        calibration = "not_run";
    }
}

