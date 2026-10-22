/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Locale;

final class MotionCsv {
    static void write(Path path, long[][] lArray, double[][] dArray, int[] nArray, int n) throws IOException {
        if (n < 0 || n > lArray.length || n > dArray.length || n > nArray.length) {
            throw new IllegalArgumentException("row count");
        }
        for (int i = 0; i < n; ++i) {
            if (lArray[i].length == 15 && dArray[i].length == 10) continue;
            throw new IllegalArgumentException("row width");
        }
        String[] stringArray = new String[]{"frame_time_ms", "links_ms", "contacts_ms", "self_ms", "other_inclusive_ms", "solver_internal_ms", "solver_sample_calls", "iterations", "link_visits", "body_hits", "arm_hits", "self_hits", "client_thread_allocated_bytes", "substeps", "estimated_timer_overhead_ns"};
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);){
            int n2;
            bufferedWriter.write("frame,elapsed_s");
            String[] stringArray2 = stringArray;
            int n3 = stringArray2.length;
            for (n2 = 0; n2 < n3; ++n2) {
                String string = stringArray2[n2];
                bufferedWriter.write("," + (String)string);
            }
            bufferedWriter.write(",flags,npc_x,npc_y,npc_z,body_yaw,head_yaw,pitch,npc_tick,world_tick,npc_scene,hair_input_sim_s,gpu_render_ms,draw_calls\n");
            long l = 0L;
            for (n2 = 0; n2 < n; ++n2) {
                bufferedWriter.write(n2 + "," + String.format(Locale.ROOT, "%.9f", (double)(l += lArray[n2][0]) / 1.0E9));
                for (int i = 0; i < 15; ++i) {
                    bufferedWriter.write("," + (i <= 5 ? String.format(Locale.ROOT, "%.6f", (double)lArray[n2][i] / 1000000.0) : (lArray[n2][i] < 0L ? "" : Long.toString(lArray[n2][i]))));
                }
                bufferedWriter.write("," + nArray[n2]);
                for (double object : dArray[n2]) {
                    bufferedWriter.write("," + (Double.isFinite((double)object) ? String.format(Locale.ROOT, "%.9f", (double)object) : ""));
                }
                bufferedWriter.write(",,\n");
            }
        }
    }

    private MotionCsv() {
    }
}

