/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.cache.object.GeoCube
 *  software.bernie.geckolib.cache.object.GeoQuad
 *  software.bernie.geckolib.cache.object.GeoVertex
 *  software.bernie.geckolib.model.GeoModel
 */
package dev.kelmagic.client.hair;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.kelmagic.client.diag.FpsDiagnostics;
import dev.kelmagic.client.hair.HairRig;
import dev.kelmagic.client.hair.RearHairCloth;
import dev.kelmagic.entity.RedMage;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.GeoModel;

public final class RearHairFollow {
    private static final Map<GeoBone, List<GeoCube>> REST = Collections.synchronizedMap(new WeakHashMap());
    private static final Map<RedMage, MotionState> MOTION = Collections.synchronizedMap(new WeakHashMap());

    public static void captureBody(RedMage e, PoseStack pose, Matrix4f entityBase) {
        MOTION.computeIfAbsent(e, k -> new MotionState()).bodyFrame.set(entityBase).invert().mul(pose.last().pose());
    }

    public static boolean owns(String n) {
        return n.startsWith("hair6_hair_back_") || n.startsWith("hair6_hair_mid_") || n.startsWith("hair6_hair_tip_");
    }

    public static void restore(GeoBone b) {
        if (!RearHairFollow.owns(b.getName())) {
            return;
        }
        List<GeoCube> original = REST.get(b);
        if (original != null) {
            b.getCubes().clear();
            b.getCubes().addAll(original);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void apply(GeoModel<?> model, GeoBone b, RedMage entity, float partial) {
        long diagStart = FpsDiagnostics.begin(1);
        try {
            if (!RearHairFollow.owns(b.getName())) {
                return;
            }
            GeoBone h = model.getBone("head").orElse(null);
            if (h == null) {
                return;
            }
            REST.computeIfAbsent(b, k -> new ArrayList(k.getCubes()));
            MotionState state = MOTION.computeIfAbsent(entity, k -> new MotionState());
            boolean teleport = state.placed && Math.pow(entity.getX() - state.x, 2.0) + Math.pow(entity.getY() - state.y, 2.0) + Math.pow(entity.getZ() - state.z, 2.0) > 4.0;
            double now = ((double)entity.tickCount + (double)Math.clamp(partial, 0.0f, 1.0f)) / 20.0;
            boolean relocation = state.placed && entity.visualRelocating();
            boolean diagNeeds = state.cloth.needsSample(now, teleport || (relocation && !state.relocating));
            FpsDiagnostics.hairVisit(entity, now, diagNeeds);
            if (diagNeeds && (!FpsDiagnostics.hairDisabled() || state.diagSampleTime != now)) {
                Matrix4f delta = HairRig.belowBody(h).translate(0.0f, 0.0755f, 0.0f);
                Vector3f displacement = state.placed ? new Vector3f((float)(entity.getX() - state.x), (float)(entity.getY() - state.y), (float)(entity.getZ() - state.z)) : new Vector3f();
                ArrayList<RearHairCloth.Collider> colliders = new ArrayList<RearHairCloth.Collider>();
                colliders.add(new RearHairCloth.Collider(delta, new Vector3f(-3.3f, 23.9f, -3.3f), new Vector3f(3.3f, 31.8f, 3.3f), false));
                colliders.add(new RearHairCloth.Collider(new Matrix4f(), new Vector3f(-4.0f, 12.0f, -2.0f), new Vector3f(4.0f, 24.0f, 2.0f), false));
                for (String name : new String[]{"left_arm", "right_arm", "left_forearm", "right_forearm"}) {
                    model.getBone(name).ifPresent(a -> {
                        boolean left = name.startsWith("left"), fore = name.contains("forearm");
                    float lo = left ? (fore ? -6.955f : -7f) : (fore ? 4.045f : 4f);
                    float hi = left ? (fore ? -4.045f : -4f) : (fore ? 6.955f : 7f);
                    colliders.add(new RearHairCloth.Collider(HairRig.belowBody(a), new Vector3f(lo, fore ? 12.0f : 18.04f, fore ? -1.955f : -2.0f), new Vector3f(hi, fore ? 18.28f : 24.0f, fore ? 1.955f : 2.0f), true));
                    });
                }
                if (FpsDiagnostics.hairDisabled()) {
                    state.diagSampleTime = now;
                    FpsDiagnostics.hairModeSkip();
                } else {
                    int diagSteps = state.cloth.steps;
                    FpsDiagnostics.sampleCalled();
                    long diagSolver = FpsDiagnostics.begin(0);
                    try {
                        if (state.placed && (relocation || teleport)) {
                            state.cloth.relocate(now, delta, colliders, state.bodyFrame);
                        } else {
                            state.cloth.sample(now, delta, colliders, false, displacement, state.bodyFrame);
                        }
                    }
                    finally {
                        FpsDiagnostics.end(0, diagSolver);
                    }
                    FpsDiagnostics.sampleCompleted(state.cloth.steps - diagSteps);
                }
            }
            // Consume motion only when this invocation actually prepared the simulation.
            // A repeated draw at the same time must not swallow the next displacement.
            if (diagNeeds) {
            state.x = entity.getX();
            state.y = entity.getY();
            state.z = entity.getZ();
            state.placed = true;
            }
            state.relocating = relocation;
            Matrix4f toBody = HairRig.belowBody(b);
            Matrix4f fromBody = new Matrix4f((Matrix4fc)toBody).invert();
            ArrayList<GeoCube> fresh = new ArrayList<GeoCube>(b.getCubes().size());
            // Invocation-local scratch: never shared between entities or reentrant calls.
            Vector3f edgeScratch = new Vector3f();
            int diagQuads = 0;
            for (GeoCube cube : b.getCubes()) {
                int faceCount = 0;
                for (GeoQuad face : cube.quads()) if (face != null) ++faceCount;
                GeoQuad[] faces = new GeoQuad[faceCount * 12 * 6];
                int faceIndex = 0;
                for (GeoQuad face : cube.quads()) {
                    if (face == null) continue;
                    for (int v = 0; v < 12; ++v) {
                        for (int u = 0; u < 6; ++u) {
                            GeoVertex[] verts = new GeoVertex[4];
                            float u0 = (float)u / 6.0f, u1 = (float)(u + 1) / 6.0f;
                            float v0 = (float)v / 12.0f, v1 = (float)(v + 1) / 12.0f;
                            for (int k2 = 0; k2 < 4; ++k2) {
                                GeoVertex s = RearHairFollow.sample(face.vertices(), k2 == 1 || k2 == 2 ? u1 : u0, k2 >= 2 ? v1 : v0);
                                // sample owns this new vertex/position; transform it directly.
                                Vector3f pos = toBody.transformPosition(s.position());
                                state.cloth.deform(pos);
                                fromBody.transformPosition(pos);
                                verts[k2] = s;
                            }
                            Vector3f normal = new Vector3f((Vector3fc)verts[1].position()).sub((Vector3fc)verts[0].position()).cross((Vector3fc)edgeScratch.set((Vector3fc)verts[3].position()).sub((Vector3fc)verts[0].position()));
                            if (normal.lengthSquared() < 1.0E-12f) {
                                normal.set((Vector3fc)face.normal());
                            } else {
                                normal.normalize();
                            }
                            if (normal.dot((Vector3fc)face.normal()) < 0.0f) {
                                normal.negate();
                            }
                            faces[faceIndex++] = new GeoQuad(verts, normal, face.direction());
                        }
                    }
                }
                diagQuads += faces.length;
                fresh.add(new GeoCube(faces, cube.pivot(), cube.rotation(), cube.size(), cube.inflate(), cube.mirror()));
            }
            b.getCubes().clear();
            b.getCubes().addAll(fresh);
            FpsDiagnostics.hairGeometry(diagQuads);
        }
        finally {
            FpsDiagnostics.end(1, diagStart);
        }
    }

    private static GeoVertex sample(GeoVertex[] a, float u, float v) {
        // Keep the original four FMA/UV accumulation steps and their order.
        float w0 = (1.0f - u) * (1.0f - v);
        float w1 = u * (1.0f - v);
        float w2 = u * v;
        float w3 = (1.0f - u) * v;
        Vector3f p = new Vector3f();
        float tu = 0.0f, tv = 0.0f;
        p.fma(w0, (Vector3fc)a[0].position());
        tu += w0 * a[0].texU();
        tv += w0 * a[0].texV();
        p.fma(w1, (Vector3fc)a[1].position());
        tu += w1 * a[1].texU();
        tv += w1 * a[1].texV();
        p.fma(w2, (Vector3fc)a[2].position());
        tu += w2 * a[2].texU();
        tv += w2 * a[2].texV();
        p.fma(w3, (Vector3fc)a[3].position());
        tu += w3 * a[3].texU();
        tv += w3 * a[3].texV();
        return new GeoVertex(p, tu, tv);
    }

    public static String diagnostics(RedMage e) {
        MotionState m = MOTION.get((Object)e);
        return m == null ? "uninitialized" : "steps=" + m.cloth.steps + " bodyContacts=" + m.cloth.bodyContacts + " armContacts=" + m.cloth.armContacts + " selfContacts=" + m.cloth.selfContacts + " maxStrain=" + m.cloth.maxStrain + " widthFreedom=" + m.cloth.widthMobility();
    }

    private RearHairFollow() {
    }

    private static final class MotionState {
        final RearHairCloth cloth = new RearHairCloth();
        final Matrix4f bodyFrame = new Matrix4f();
        double x;
        double y;
        double z;
        boolean placed;
        boolean relocating;
        double diagSampleTime = Double.NaN;

        private MotionState() {
        }
    }
}

