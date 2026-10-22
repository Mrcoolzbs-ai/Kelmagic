/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

public final class FrameAllocation {
    private static final ThreadMXBean BEAN = FrameAllocation.bean();

    private static ThreadMXBean bean() {
        try {
            ThreadMXBean t;
            java.lang.management.ThreadMXBean b = ManagementFactory.getThreadMXBean();
            if (b instanceof ThreadMXBean && (t = (ThreadMXBean)b).isThreadAllocatedMemorySupported()) {
                t.setThreadAllocatedMemoryEnabled(true);
                return t;
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return null;
    }

    public static long read() {
        return BEAN == null ? -1L : BEAN.getThreadAllocatedBytes(Thread.currentThread().threadId());
    }

    private FrameAllocation() {
    }
}

