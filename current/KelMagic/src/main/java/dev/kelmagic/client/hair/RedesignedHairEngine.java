/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Math
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package dev.kelmagic.client.hair;

import dev.kelmagic.client.diag.HairStageProfiler;
import dev.kelmagic.client.hair.RearHairCloth;
import java.util.Arrays;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class RedesignedHairEngine {
    public static final int COLS = 9;
    public static final int ROWS = 19;
    private static final float WIDTH = 8.4f;
    private static final float TOP = 26.2f;
    private static final float LENGTH = 14.85f;
    private static final float Z = 4.2f;
    private static final float CONTACT = 0.65f;
    private static final float SELF = 0.35f;
    private static final float GRAVITY = 650.0f;
    private static final float STRETCH = 1.035f;
    private static final int[] SELF_PAIRS = RedesignedHairEngine.selfPairs();
    private static final int[] SELF_GROUPS = RedesignedHairEngine.selfGroups();
    private final float[] selfBounds = new float[114];
    private final Matrix4f previousBodyFrame = new Matrix4f();
    private final Vector3f solverDelta = new Vector3f();
    private final Vector3f contactBefore = new Vector3f();
    private final Vector3f contactCorrection = new Vector3f();
    private final Vector3f[] pos = new Vector3f[171];
    private final Vector3f[] prev = new Vector3f[this.pos.length];
    private final Vector3f[] oldPins = new Vector3f[9];
    private static final int LINK_COUNT = 888;
    private final int[] linkA = new int[888];
    private final int[] linkB = new int[888];
    private final float[] linkLength = new float[888];
    private final float[] linkCompliance = new float[888];
    private final float[] linkLambda = new float[888];
    private final float[] envelopeInnerLowSquared = new float[888];
    private final float[] envelopeInnerHighSquared = new float[888];
    private int linkCount;
    private double time = Double.NaN;
    public int bodyContacts;
    public int armContacts;
    public int selfContacts;
    public float maxStrain;
    public int steps;
    private static final float NAPE_TOP = 24.35f;
    private static final float ROOT_JOIN = 20.2f;

    private static int[] selfGroups() {
        int[] nArray = new int[SELF_PAIRS.length * 2];
        int n = 0;
        int n2 = 0;
        while (n2 < SELF_PAIRS.length) {
            int n3 = n2;
            int n4 = SELF_PAIRS[n2];
            int n5 = SELF_PAIRS[n2 + 1] / 9;
            n2 += 2;
            while (n2 < SELF_PAIRS.length && SELF_PAIRS[n2] == n4 && SELF_PAIRS[n2 + 1] / 9 == n5) {
                n2 += 2;
            }
            nArray[n++] = n4;
            nArray[n++] = n5;
            nArray[n++] = n3;
            nArray[n++] = n2;
        }
        return Arrays.copyOf(nArray, n);
    }

    private static int[] selfPairs() {
        int[] nArray = new int[26082];
        int n = 0;
        for (int i = 9; i < 171; ++i) {
            for (int j = i + 1; j < 171; ++j) {
                if (Math.abs(i / 9 - j / 9) <= 2 && Math.abs(i % 9 - j % 9) <= 2) continue;
                nArray[n++] = i;
                nArray[n++] = j;
            }
        }
        return Arrays.copyOf(nArray, n);
    }

    public RedesignedHairEngine() {
        int n;
        int n2;
        for (n2 = 0; n2 < 19; ++n2) {
            for (n = 0; n < 9; ++n) {
                int n3 = RedesignedHairEngine.id(n, n2);
                this.pos[n3] = RedesignedHairEngine.rest(n, n2);
                this.prev[n3] = new Vector3f((Vector3fc)this.pos[n3]);
                if (n2 != 0) continue;
                this.oldPins[n] = new Vector3f((Vector3fc)this.pos[n3]);
            }
        }
        for (n2 = 0; n2 < 19; ++n2) {
            for (n = 0; n < 9; ++n) {
                if (n + 1 < 9) {
                    this.add(n, n2, n + 1, n2, 3.0E-7f);
                }
                if (n2 + 1 < 19) {
                    this.add(n, n2, n, n2 + 1, 1.5E-6f);
                }
                if (n + 1 < 9 && n2 + 1 < 19) {
                    this.add(n, n2, n + 1, n2 + 1, 1.2E-5f);
                    this.add(n + 1, n2, n, n2 + 1, 1.2E-5f);
                }
                if (n + 2 < 9) {
                    this.add(n, n2, n + 2, n2, 3.5E-4f);
                }
                if (n2 + 2 >= 19) continue;
                this.add(n, n2, n, n2 + 2, 3.5E-4f);
            }
        }
    }

    private static int id(int n, int n2) {
        return n2 * 9 + n;
    }

    private static float napeY(float f) {
        return f <= 20.2f ? f : 20.2f + (f - 20.2f) * 4.1499996f / 6.0f;
    }

    private static float materialY(float f) {
        return f <= 20.2f ? f : 20.2f + (f - 20.2f) * 6.0f / 4.1499996f;
    }

    private static Vector3f rest(int n, int n2) {
        return new Vector3f(-4.2f + 8.4f * (float)n / 8.0f, RedesignedHairEngine.napeY(26.2f - 14.85f * (float)n2 / 18.0f), 4.2f);
    }

    private void add(int n, int n2, int n3, int n4, float f) {
        int n5 = RedesignedHairEngine.id(n, n2);
        int n6 = RedesignedHairEngine.id(n3, n4);
        this.linkA[this.linkCount] = n5;
        this.linkB[this.linkCount] = n6;
        this.linkLength[this.linkCount] = this.pos[n5].distance((Vector3fc)this.pos[n6]);
        this.linkCompliance[this.linkCount] = f;
        float f2 = this.linkLength[this.linkCount] * 0.94f;
        float f3 = this.linkLength[this.linkCount] * 1.035f;
        this.envelopeInnerLowSquared[this.linkCount] = Math.nextUp(f2 * f2);
        this.envelopeInnerHighSquared[this.linkCount] = Math.nextDown(f3 * f3);
        ++this.linkCount;
    }

    /** Teleport is NOT world inertia. Keep each column's relative shape, re-seat roots,
     * clear Verlet velocity/lambda and replace the frame/time atomically before drawing.
     * Collision correction is local and applied equally to pos/prev (no artificial kick).
     */
    public void relocate(double now, Matrix4f head, List<RearHairCloth.Collider> colliders, Matrix4f body) {
        if (!Double.isFinite(time)) { sample(now, head, colliders, true, new Vector3f(), body); return; }
        Vector3f pin = new Vector3f(), shift = new Vector3f();
        for (int col=0; col<9; col++) {
            head.transformPosition(pin.set(rest(col,0)).div(16f)).mul(16f);
            shift.set(pin).sub(oldPins[col]);
            for (int row=0; row<19; row++) {
                int i=id(col,row);
                pos[i].add(shift);
                if(row>0) for(var collider:colliders) collider.project(pos[i], .65f);
                prev[i].set(pos[i]);
            }
            pos[id(col,0)].set(pin); prev[id(col,0)].set(pin); oldPins[col].set(pin);
        }
        Arrays.fill(linkLambda,0f);
        previousBodyFrame.set(body); time=now;
        bodyContacts=armContacts=selfContacts=0; maxStrain=0;
    }

    public boolean needsSample(double d, boolean bl) {
        double d2 = d - this.time;
        return bl || !Double.isFinite(this.time) || d2 < 0.0 || !(d2 < 1.0E-6);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sample(double d, Matrix4f matrix4f, List<RearHairCloth.Collider> list, boolean bl, Vector3f vector3f, Matrix4f matrix4f2) {
        HairStageProfiler.inputTime(d);
        long l = HairStageProfiler.beginSample();
        try {
            int n;
            if (!this.needsSample(d, bl)) {
                return;
            }
            Vector3f[] vector3fArray = new Vector3f[9];
            for (int i = 0; i < 9; ++i) {
                vector3fArray[i] = matrix4f.transformPosition(RedesignedHairEngine.rest(i, 0).div(16.0f)).mul(16.0f);
            }
            double d2 = d - this.time;
            if (bl || !Double.isFinite(this.time) || d2 < 0.0 || d2 > 0.25) {
                int n2;
                for (n2 = 0; n2 < 19; ++n2) {
                    for (int i = 0; i < 9; ++i) {
                        this.pos[RedesignedHairEngine.id(i, n2)].set((Vector3fc)RedesignedHairEngine.rest(i, n2)).add((Vector3fc)new Vector3f((Vector3fc)vector3fArray[i]).sub((Vector3fc)RedesignedHairEngine.rest(i, 0)));
                        this.prev[RedesignedHairEngine.id(i, n2)].set((Vector3fc)this.pos[RedesignedHairEngine.id(i, n2)]);
                    }
                }
                for (n2 = 0; n2 < 9; ++n2) {
                    this.oldPins[n2].set((Vector3fc)vector3fArray[n2]);
                }
                this.previousBodyFrame.set((Matrix4fc)matrix4f2);
                this.time = d;
                return;
            }
            Matrix4f matrix4f3 = new Matrix4f((Matrix4fc)matrix4f2).invert();
            Matrix4f matrix4f4 = new Matrix4f((Matrix4fc)matrix4f3).translate(-vector3f.x, -vector3f.y, -vector3f.z).mul((Matrix4fc)this.previousBodyFrame);
            for (int i = 0; i < this.pos.length; ++i) {
                matrix4f4.transformPosition(this.pos[i].div(16.0f)).mul(16.0f);
                matrix4f4.transformPosition(this.prev[i].div(16.0f)).mul(16.0f);
            }
            for (Vector3f vector3f2 : this.oldPins) {
                matrix4f4.transformPosition(vector3f2.div(16.0f)).mul(16.0f);
            }
            Vector3f vector3f3 = matrix4f3.transformDirection(new Vector3f(0.0f, -650.0f, 0.0f));
            int n3 = Math.max(1, (int)Math.ceil(d2 * 180.0));
            float f = (float)(d2 / (double)n3);
            float f2 = (float)Math.exp(-11.0f * f);
            this.selfContacts = 0;
            this.armContacts = 0;
            this.bodyContacts = 0;
            this.maxStrain = 0.0f;
            for (n = 0; n < n3; ++n) {
                int n4;
                float f3 = ((float)n + 1.0f) / (float)n3;
                for (n4 = 1; n4 < 19; ++n4) {
                    for (int i = 0; i < 9; ++i) {
                        int n5 = RedesignedHairEngine.id(i, n4);
                        Vector3f vector3f4 = this.pos[n5];
                        Vector3f vector3f5 = new Vector3f((Vector3fc)vector3f4).sub((Vector3fc)this.prev[n5]).mul(f2);
                        float f4 = 70.0f * f;
                        if (vector3f5.length() > f4) {
                            vector3f5.normalize(f4);
                        }
                        this.prev[n5].set((Vector3fc)vector3f4);
                        vector3f4.add((Vector3fc)vector3f5).fma(f * f, (Vector3fc)vector3f3);
                    }
                }
                Arrays.fill(this.linkLambda, 0.0f);
                for (n4 = 0; n4 < 64; ++n4) {
                    this.pin(vector3fArray, f3);
                    HairStageProfiler.iteration();
                    long l2 = HairStageProfiler.beginStage();
                    if ((n4 & 1) == 0) {
                        for (int var24_30 = 0; var24_30 < this.linkCount; ++var24_30) {
                            this.solve(var24_30, f);
                        }
                    } else {
                        for (int var24_30 = this.linkCount - 1; var24_30 >= 0; --var24_30) {
                            this.solve(var24_30, f);
                        }
                    }
                    HairStageProfiler.endStage(0, l2);
                    l2 = HairStageProfiler.beginStage();
                    this.contacts(list);
                    HairStageProfiler.endStage(1, l2);
                    if (n4 % 8 == 7) {
                        l2 = HairStageProfiler.beginStage();
                        this.selfSeparate();
                        HairStageProfiler.endStage(2, l2);
                    }
                    if (n4 >= 31 && n4 % 8 == 7 && this.strain() < 0.008f) break;
                }
                this.pin(vector3fArray, f3);
                long l3 = HairStageProfiler.beginStage();
                this.contacts(list);
                HairStageProfiler.endStage(1, l3);
                ++this.steps;
                HairStageProfiler.substep();
            }
            for (n = 0; n < this.linkCount; ++n) {
                if (!(this.linkCompliance[n] < 1.0E-5f)) continue;
                this.maxStrain = Math.max(this.maxStrain, Math.abs(this.pos[this.linkA[n]].distance((Vector3fc)this.pos[this.linkB[n]]) / this.linkLength[n] - 1.0f));
            }
            for (n = 0; n < 9; ++n) {
                this.oldPins[n].set((Vector3fc)vector3fArray[n]);
            }
            this.previousBodyFrame.set((Matrix4fc)matrix4f2);
            this.time = d;
        }
        finally {
            HairStageProfiler.endSample(l);
        }
    }

    private float strain() {
        float f = 0.0f;
        for (int i = 0; i < this.linkCount; ++i) {
            if (!(this.linkCompliance[i] < 1.0E-5f)) continue;
            f = Math.max(f, Math.abs(this.pos[this.linkA[i]].distance((Vector3fc)this.pos[this.linkB[i]]) / this.linkLength[i] - 1.0f));
        }
        return f;
    }

    private void pin(Vector3f[] vector3fArray, float f) {
        for (int i = 0; i < 9; ++i) {
            this.pos[i].set((Vector3fc)this.oldPins[i]).lerp((Vector3fc)vector3fArray[i], f);
            this.prev[i].set((Vector3fc)this.pos[i]);
        }
    }

    private void solve(int n, float f) {
        float f2;
        int n2 = this.linkA[n];
        int n3 = this.linkB[n];
        float f3 = n2 < 9 ? 0.0f : 1.0f;
        float f4 = f2 = n3 < 9 ? 0.0f : 1.0f;
        if (f3 + f2 == 0.0f) {
            return;
        }
        Vector3f vector3f = this.solverDelta.set((Vector3fc)this.pos[n3]).sub((Vector3fc)this.pos[n2]);
        float f5 = vector3f.length();
        if (f5 < 1.0E-8f) {
            return;
        }
        float f6 = this.linkLength[n];
        float f7 = this.linkCompliance[n];
        float f8 = this.linkLambda[n];
        float f9 = f7 / (f * f);
        float f10 = (-(f5 - f6) - f9 * f8) / (f3 + f2 + f9);
        this.linkLambda[n] = f8 + f10;
        vector3f.mul(f10 / f5);
        this.pos[n2].fma(-f3, (Vector3fc)vector3f);
        this.pos[n3].fma(f2, (Vector3fc)vector3f);
        if (f7 < 1.0E-5f) {
            this.envelope(n, n2, n3, f6, f3, f2);
        }
    }

    private void envelope(int n, int n2, int n3, float f, float f2, float f3) {
        Vector3f vector3f = this.solverDelta.set((Vector3fc)this.pos[n3]).sub((Vector3fc)this.pos[n2]);
        float f4 = vector3f.lengthSquared();
        if (f4 >= this.envelopeInnerLowSquared[n] && f4 <= this.envelopeInnerHighSquared[n]) {
            return;
        }
        float f5 = org.joml.Math.sqrt((float)f4);
        float f6 = Math.clamp(f5, f * 0.94f, f * 1.035f);
        if (f5 > 1.0E-8f && f6 != f5) {
            vector3f.mul((f5 - f6) / (f5 * (f2 + f3)));
            this.pos[n2].fma(f2, (Vector3fc)vector3f);
            this.pos[n3].fma(-f3, (Vector3fc)vector3f);
        }
    }

    private void contacts(List<RearHairCloth.Collider> list) {
        boolean bl = false;
        for (int i = 9; i < this.pos.length; ++i) {
            Vector3f vector3f = this.pos[i];
            float f = 0.65f * Math.min(1.0f, (float)(i / 9) / 3.0f);
            for (RearHairCloth.Collider collider : list) {
                if (!collider.mightContact(vector3f, f)) {
                    bl = true;
                    continue;
                }
                bl = false;
                Vector3f vector3f2 = this.contactBefore.set((Vector3fc)vector3f);
                if (!collider.projectNarrow(vector3f, f)) continue;
                if (collider.arm) {
                    ++this.armContacts;
                } else {
                    ++this.bodyContacts;
                }
                Vector3f vector3f3 = this.contactCorrection.set((Vector3fc)vector3f).sub((Vector3fc)vector3f2);
                this.prev[i].fma(0.85f, (Vector3fc)vector3f3);
            }
        }
        if (bl) {
            this.contactBefore.set((Vector3fc)this.pos[this.pos.length - 1]);
        }
    }

    private void includeSelfBound(int n, Vector3f vector3f) {
        int n2 = n * 6;
        this.selfBounds[n2] = Math.min(this.selfBounds[n2], vector3f.x);
        this.selfBounds[n2 + 1] = Math.max(this.selfBounds[n2 + 1], vector3f.x);
        this.selfBounds[n2 + 2] = Math.min(this.selfBounds[n2 + 2], vector3f.y);
        this.selfBounds[n2 + 3] = Math.max(this.selfBounds[n2 + 3], vector3f.y);
        this.selfBounds[n2 + 4] = Math.min(this.selfBounds[n2 + 4], vector3f.z);
        this.selfBounds[n2 + 5] = Math.max(this.selfBounds[n2 + 5], vector3f.z);
    }

    private void rebuildSelfBounds() {
        for (int i = 1; i < 19; ++i) {
            int n = i * 6;
            Vector3f vector3f = this.pos[i * 9];
            float f = vector3f.x;
            this.selfBounds[n + 1] = f;
            this.selfBounds[n] = f;
            float f2 = vector3f.y;
            this.selfBounds[n + 3] = f2;
            this.selfBounds[n + 2] = f2;
            float f3 = vector3f.z;
            this.selfBounds[n + 5] = f3;
            this.selfBounds[n + 4] = f3;
            for (int j = 1; j < 9; ++j) {
                this.includeSelfBound(i, this.pos[i * 9 + j]);
            }
        }
    }

    private boolean selfGroupNear(int n, int n2) {
        int n3 = n2 * 6;
        Vector3f vector3f = this.pos[n];
        return !(this.selfBounds[n3 + 2] - vector3f.y >= 0.35f || vector3f.y - this.selfBounds[n3 + 3] >= 0.35f || this.selfBounds[n3 + 4] - vector3f.z >= 0.35f || vector3f.z - this.selfBounds[n3 + 5] >= 0.35f || this.selfBounds[n3] - vector3f.x >= 0.35f || vector3f.x - this.selfBounds[n3 + 1] >= 0.35f);
    }

    private void solveSelfGroup(int n, int n2, int n3, int n4) {
        for (int i = n3; i < n4; i += 2) {
            int n5 = SELF_PAIRS[i + 1];
            Vector3f vector3f = this.pos[n];
            Vector3f vector3f2 = this.pos[n5];
            float f = vector3f2.x - vector3f.x;
            float f2 = vector3f2.y - vector3f.y;
            float f3 = vector3f2.z - vector3f.z;
            float f4 = org.joml.Math.fma((float)f, (float)f, (float)org.joml.Math.fma((float)f2, (float)f2, (float)(f3 * f3)));
            if (f4 >= 0.122499995f || f4 < 1.0E-10f) continue;
            float f5 = (float)Math.sqrt(f4);
            float f6 = (0.35f - f5) * 0.5f / f5;
            vector3f.sub(f *= f6, f2 *= f6, f3 *= f6);
            vector3f2.add(f, f2, f3);
            ++this.selfContacts;
            this.includeSelfBound(n / 9, this.pos[n]);
            this.includeSelfBound(n2, this.pos[n5]);
        }
    }

    private void selfSeparate() {
        this.rebuildSelfBounds();
        for (int i = 0; i < SELF_GROUPS.length; i += 4) {
            int n = SELF_GROUPS[i];
            int n2 = SELF_GROUPS[i + 1];
            if (!this.selfGroupNear(n, n2)) continue;
            this.solveSelfGroup(n, n2, SELF_GROUPS[i + 2], SELF_GROUPS[i + 3]);
        }
    }

    public void deform(Vector3f vector3f) {
        float f = vector3f.x * 16.0f;
        float f2 = RedesignedHairEngine.materialY(vector3f.y * 16.0f);
        float f3 = vector3f.z * 16.0f;
        float f4 = Math.clamp((f + 4.2f) / 8.4f * 8.0f, 0.0f, 8.0f);
        float f5 = Math.clamp((26.2f - f2) / 14.85f * 18.0f, 0.0f, 18.0f);
        int n = Math.min(7, (int)f4);
        int n2 = Math.min(17, (int)f5);
        float f6 = f4 - (float)n;
        float f7 = f5 - (float)n2;
        Vector3f vector3f2 = this.pos[RedesignedHairEngine.id(n, n2)];
        Vector3f vector3f3 = this.pos[RedesignedHairEngine.id(n + 1, n2)];
        Vector3f vector3f4 = this.pos[RedesignedHairEngine.id(n, n2 + 1)];
        Vector3f vector3f5 = this.pos[RedesignedHairEngine.id(n + 1, n2 + 1)];
        Vector3f vector3f6 = new Vector3f((Vector3fc)vector3f2).lerp((Vector3fc)vector3f3, f6).lerp((Vector3fc)new Vector3f((Vector3fc)vector3f4).lerp((Vector3fc)vector3f5, f6), f7);
        Vector3f vector3f7 = new Vector3f((Vector3fc)vector3f3).sub((Vector3fc)vector3f2).lerp((Vector3fc)new Vector3f((Vector3fc)vector3f5).sub((Vector3fc)vector3f4), f7);
        Vector3f vector3f8 = new Vector3f((Vector3fc)vector3f4).sub((Vector3fc)vector3f2).lerp((Vector3fc)new Vector3f((Vector3fc)vector3f5).sub((Vector3fc)vector3f3), f6);
        Vector3f vector3f9 = vector3f8.cross((Vector3fc)vector3f7).normalize();
        if (!Float.isFinite(vector3f9.lengthSquared())) {
            vector3f9.set(0.0f, 0.0f, 1.0f);
        }
        vector3f6.fma(f3 - 4.2f, (Vector3fc)vector3f9);
        vector3f.set((Vector3fc)vector3f6).div(16.0f);
    }

    public float widthMobility() {
        Vector3f vector3f = this.pos[RedesignedHairEngine.id(0, 9)];
        Vector3f vector3f2 = this.pos[RedesignedHairEngine.id(8, 9)];
        return vector3f.y - vector3f2.y;
    }
}

