/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Math
 *  org.joml.Matrix4d
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package dev.kelmagic.client.hair;

import dev.kelmagic.client.diag.BenchmarkPlan;
import dev.kelmagic.client.diag.HairStageProfiler;
import dev.kelmagic.client.hair.RedesignedHairEngine;
import dev.kelmagic.client.hair.ReferenceHairEngine;
import java.util.List;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class RearHairCloth {
    public static final int COLS = 9;
    public static final int ROWS = 19;
    private static final float CONTACT = 0.65f;
    private final RedesignedHairEngine redesigned = new RedesignedHairEngine();
    private ReferenceHairEngine reference;
    private boolean useReference;
    private boolean forceReset;
    public int bodyContacts;
    public int armContacts;
    public int selfContacts;
    public int steps;
    public float maxStrain;

    private boolean select() {
        boolean bl;
        boolean bl2 = bl = !BenchmarkPlan.optimized();
        if (bl != this.useReference) {
            this.useReference = bl;
            this.forceReset = true;
        }
        if (bl && this.reference == null) {
            this.reference = new ReferenceHairEngine();
        }
        return bl;
    }

    public boolean needsSample(double d, boolean bl) {
        boolean bl2 = this.select();
        return this.forceReset || (bl2 ? this.reference.needsSample(d, bl) : this.redesigned.needsSample(d, bl));
    }

    public void sample(double d, Matrix4f matrix4f, List<Collider> list, boolean bl, Vector3f vector3f, Matrix4f matrix4f2) {
        int n;
        boolean bl2 = this.select();
        boolean bl3 = bl || this.forceReset;
        this.forceReset = false;
        int n2 = n = bl2 ? this.reference.steps : this.redesigned.steps;
        if (bl2) {
            this.reference.sample(d, matrix4f, list, bl3, vector3f, matrix4f2);
            this.bodyContacts = this.reference.bodyContacts;
            this.armContacts = this.reference.armContacts;
            this.selfContacts = this.reference.selfContacts;
            this.maxStrain = this.reference.maxStrain;
            this.steps += this.reference.steps - n;
        } else {
            this.redesigned.sample(d, matrix4f, list, bl3, vector3f, matrix4f2);
            this.bodyContacts = this.redesigned.bodyContacts;
            this.armContacts = this.redesigned.armContacts;
            this.selfContacts = this.redesigned.selfContacts;
            this.maxStrain = this.redesigned.maxStrain;
            this.steps += this.redesigned.steps - n;
        }
        if ((bl2 ? this.reference.steps : this.redesigned.steps) > n) {
            HairStageProfiler.contacts(this.bodyContacts, this.armContacts, this.selfContacts);
        }
    }

    /** Confirmed visual relocation: shape stays body-local, no world-space impulse. */
    public void relocate(double now, Matrix4f head, List<Collider> colliders, Matrix4f body) {
        boolean ref = select();
        forceReset = false;
        if (ref) reference.relocate(now, head, colliders, body);
        else redesigned.relocate(now, head, colliders, body);
        bodyContacts = armContacts = selfContacts = 0;
        maxStrain = 0;
    }

    public void deform(Vector3f vector3f) {
        if (this.useReference) {
            this.reference.deform(vector3f);
        } else {
            this.redesigned.deform(vector3f);
        }
    }

    public float widthMobility() {
        return this.useReference ? this.reference.widthMobility() : this.redesigned.widthMobility();
    }

    public static final class Collider {
        final Matrix4f toBody;
        final Matrix4f local;
        private final Vector3f low;
        private final Vector3f high;
        final boolean arm;
        private float minX;
        private float minY;
        private float minZ;
        private float maxX;
        private float maxY;
        private float maxZ;
        private final Vector3f pointScratch = new Vector3f();
        private final Vector3f nearScratch = new Vector3f();
        private final Vector3f deltaScratch = new Vector3f();

        public Collider(Matrix4f matrix4f, Vector3f vector3f, Vector3f vector3f2, boolean bl) {
            this.toBody = new Matrix4f((Matrix4fc)matrix4f);
            this.local = new Matrix4f((Matrix4fc)matrix4f).invert();
            this.arm = bl;
            this.low = new Vector3f((Vector3fc)vector3f);
            this.high = new Vector3f((Vector3fc)vector3f2);
            this.buildBounds();
        }

        private void buildBounds() {
            double d;
            this.minZ = Float.NEGATIVE_INFINITY;
            this.minY = Float.NEGATIVE_INFINITY;
            this.minX = Float.NEGATIVE_INFINITY;
            this.maxZ = Float.POSITIVE_INFINITY;
            this.maxY = Float.POSITIVE_INFINITY;
            this.maxX = Float.POSITIVE_INFINITY;
            if (this.local.m03() != 0.0f || this.local.m13() != 0.0f || this.local.m23() != 0.0f || this.local.m33() != 1.0f) {
                return;
            }
            Matrix4d matrix4d = new Matrix4d((Matrix4fc)this.local).invert();
            double d2 = Math.max(Math.abs(this.local.m00()) + Math.abs(this.local.m10()) + Math.abs(this.local.m20()), Math.max(Math.abs(this.local.m01()) + Math.abs(this.local.m11()) + Math.abs(this.local.m21()), Math.abs(this.local.m02()) + Math.abs(this.local.m12()) + Math.abs(this.local.m22())));
            double d3 = Math.max(Math.abs(matrix4d.m00()) + Math.abs(matrix4d.m10()) + Math.abs(matrix4d.m20()), Math.max(Math.abs(matrix4d.m01()) + Math.abs(matrix4d.m11()) + Math.abs(matrix4d.m21()), Math.abs(matrix4d.m02()) + Math.abs(matrix4d.m12()) + Math.abs(matrix4d.m22())));
            double d4 = 8.0 * (double)Math.ulp(1.0f);
            double d5 = d4 * d3 * d2;
            if (!Double.isFinite(d5) || d5 >= 0.01) {
                return;
            }
            double d6 = (double)this.low.x - (double)0.65f;
            double d7 = (double)this.low.y - (double)0.65f;
            double d8 = (double)this.low.z - (double)0.65f;
            double d9 = (double)this.high.x + (double)0.65f;
            double d10 = (double)this.high.y + (double)0.65f;
            double d11 = (double)this.high.z + (double)0.65f;
            double d12 = Collider.bound(matrix4d.m00(), matrix4d.m10(), matrix4d.m20(), matrix4d.m30() * 16.0, d6, d7, d8, d9, d10, d11, false);
            double d13 = Collider.bound(matrix4d.m00(), matrix4d.m10(), matrix4d.m20(), matrix4d.m30() * 16.0, d6, d7, d8, d9, d10, d11, true);
            double d14 = Collider.bound(matrix4d.m01(), matrix4d.m11(), matrix4d.m21(), matrix4d.m31() * 16.0, d6, d7, d8, d9, d10, d11, false);
            double d15 = Collider.bound(matrix4d.m01(), matrix4d.m11(), matrix4d.m21(), matrix4d.m31() * 16.0, d6, d7, d8, d9, d10, d11, true);
            double d16 = Collider.bound(matrix4d.m02(), matrix4d.m12(), matrix4d.m22(), matrix4d.m32() * 16.0, d6, d7, d8, d9, d10, d11, false);
            double d17 = Collider.bound(matrix4d.m02(), matrix4d.m12(), matrix4d.m22(), matrix4d.m32() * 16.0, d6, d7, d8, d9, d10, d11, true);
            double d18 = Math.max(Math.max(Math.abs(d12), Math.abs(d13)), Math.max(Math.max(Math.abs(d14), Math.abs(d15)), Math.max(Math.abs(d16), Math.abs(d17))));
            double d19 = (d18 + d3 * d4 * (d = 16.0 * (double)Math.max(Math.abs(this.local.m30()), Math.max(Math.abs(this.local.m31()), Math.abs(this.local.m32())))) + 0.001) / (1.0 - d5);
            double d20 = d3 * d4 * (d2 * d19 + d) + 0.001;
            if (!Double.isFinite(d20)) {
                return;
            }
            this.minX = Math.nextDown((float)(d12 - d20));
            this.maxX = Math.nextUp((float)(d13 + d20));
            this.minY = Math.nextDown((float)(d14 - d20));
            this.maxY = Math.nextUp((float)(d15 + d20));
            this.minZ = Math.nextDown((float)(d16 - d20));
            this.maxZ = Math.nextUp((float)(d17 + d20));
        }

        private static double bound(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, boolean bl) {
            return d4 + d * (d >= 0.0 == bl ? d8 : d5) + d2 * (d2 >= 0.0 == bl ? d9 : d6) + d3 * (d3 >= 0.0 == bl ? d10 : d7);
        }

        public boolean project(Vector3f vector3f, float f) {
            return this.mightContact(vector3f, f) && this.projectNarrow(vector3f, f);
        }

        boolean mightContact(Vector3f vector3f, float f) {
            return !(f <= 0.65f && (vector3f.x < this.minX || vector3f.x > this.maxX || vector3f.y < this.minY || vector3f.y > this.maxY || vector3f.z < this.minZ || vector3f.z > this.maxZ) && Float.isFinite(vector3f.x) && Float.isFinite(vector3f.y) && Float.isFinite(vector3f.z));
        }

        boolean projectNarrow(Vector3f vector3f, float f) {
            float f2;
            float f3;
            float f4;
            float f5 = vector3f.x * 0.0625f;
            float f6 = vector3f.y * 0.0625f;
            float f7 = vector3f.z * 0.0625f;
            float f8 = org.joml.Math.fma((float)this.local.m00(), (float)f5, (float)org.joml.Math.fma((float)this.local.m10(), (float)f6, (float)org.joml.Math.fma((float)this.local.m20(), (float)f7, (float)this.local.m30()))) * 16.0f;
            float f9 = f8 - (f4 = Math.clamp(f8, this.low.x, this.high.x));
            if (f9 >= f || f9 <= -f) {
                return false;
            }
            float f10 = org.joml.Math.fma((float)this.local.m01(), (float)f5, (float)org.joml.Math.fma((float)this.local.m11(), (float)f6, (float)org.joml.Math.fma((float)this.local.m21(), (float)f7, (float)this.local.m31()))) * 16.0f;
            float f11 = f10 - (f3 = Math.clamp(f10, this.low.y, this.high.y));
            if (f11 >= f || f11 <= -f) {
                return false;
            }
            float f12 = org.joml.Math.fma((float)this.local.m02(), (float)f5, (float)org.joml.Math.fma((float)this.local.m12(), (float)f6, (float)org.joml.Math.fma((float)this.local.m22(), (float)f7, (float)this.local.m32()))) * 16.0f;
            float f13 = f12 - (f2 = Math.clamp(f12, this.low.z, this.high.z));
            if (f13 >= f || f13 <= -f) {
                return false;
            }
            Vector3f vector3f2 = this.pointScratch.set(f8, f10, f12);
            Vector3f vector3f3 = this.nearScratch.set(f4, f3, f2);
            Vector3f vector3f4 = this.deltaScratch.set(f9, f11, f13);
            float f14 = vector3f4.length();
            if (f14 >= f) {
                return false;
            }
            if (f14 > 1.0E-6f) {
                vector3f2.set((Vector3fc)vector3f3).fma(f / f14, (Vector3fc)vector3f4);
            } else {
                float f15 = Float.POSITIVE_INFINITY;
                int n = 0;
                boolean bl = false;
                for (int i = 0; i < 3; ++i) {
                    float f16 = vector3f2.get(i) - this.low.get(i);
                    if (f16 < f15) {
                        f15 = f16;
                        n = i;
                        bl = false;
                    }
                    if (!((f16 = this.high.get(i) - vector3f2.get(i)) < f15)) continue;
                    f15 = f16;
                    n = i;
                    bl = true;
                }
                vector3f2.setComponent(n, bl ? this.high.get(n) + f : this.low.get(n) - f);
            }
            vector3f.set((Vector3fc)this.toBody.transformPosition(vector3f2.div(16.0f))).mul(16.0f);
            return true;
        }

        public boolean projectOriginal(Vector3f vector3f, float f) {
            Vector3f vector3f2 = this.local.transformPosition(this.pointScratch.set((Vector3fc)vector3f).div(16.0f)).mul(16.0f);
            Vector3f vector3f3 = this.nearScratch.set(Math.clamp(vector3f2.x, this.low.x, this.high.x), Math.clamp(vector3f2.y, this.low.y, this.high.y), Math.clamp(vector3f2.z, this.low.z, this.high.z));
            Vector3f vector3f4 = this.deltaScratch.set((Vector3fc)vector3f2).sub((Vector3fc)vector3f3);
            float f2 = vector3f4.length();
            if (f2 >= f) {
                return false;
            }
            if (f2 > 1.0E-6f) {
                vector3f2.set((Vector3fc)vector3f3).fma(f / f2, (Vector3fc)vector3f4);
            } else {
                float f3 = Float.POSITIVE_INFINITY;
                int n = 0;
                boolean bl = false;
                for (int i = 0; i < 3; ++i) {
                    float f4 = vector3f2.get(i) - this.low.get(i);
                    if (f4 < f3) {
                        f3 = f4;
                        n = i;
                        bl = false;
                    }
                    if (!((f4 = this.high.get(i) - vector3f2.get(i)) < f3)) continue;
                    f3 = f4;
                    n = i;
                    bl = true;
                }
                vector3f2.setComponent(n, bl ? this.high.get(n) + f : this.low.get(n) - f);
            }
            vector3f.set((Vector3fc)this.toBody.transformPosition(vector3f2.div(16.0f))).mul(16.0f);
            return true;
        }
    }
}

