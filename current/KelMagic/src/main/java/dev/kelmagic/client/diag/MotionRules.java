/*
 * Decompiled with CFR 0.152.
 */
package dev.kelmagic.client.diag;

final class MotionRules {
    static int actor(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        if (!bl) {
            return 16;
        }
        return bl2 && bl3 && bl4 && bl5 ? 0 : 32;
    }

    private MotionRules() {
    }
}

