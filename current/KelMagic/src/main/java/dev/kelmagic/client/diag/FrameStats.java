/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

import java.util.Arrays;

public record FrameStats(double meanMs, double medianMs, double p95Ms, double fps) {
    public static FrameStats of(long[] frames, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("empty capture");
        }
        long[] sorted = Arrays.copyOf(frames, length);
        Arrays.sort(sorted);
        double sum = 0.0;
        for (long t : sorted) {
            sum += (double)t;
        }
        double median = length % 2 == 1 ? (double)sorted[length / 2] : ((double)sorted[length / 2 - 1] + (double)sorted[length / 2]) / 2.0;
        return new FrameStats(sum / (double)length / 1000000.0, median / 1000000.0, (double)sorted[(int)Math.ceil(0.95 * (double)length) - 1] / 1000000.0, (double)length * 1.0E9 / sum);
    }
}

