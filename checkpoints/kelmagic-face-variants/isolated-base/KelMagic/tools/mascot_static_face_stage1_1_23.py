"""Stage 1: static face only. Preserve the 1.23 rig, atlas and animation ownership."""
import copy
from mascot_surface_fixes_1_21 import surface_mesh

FACE_BONES = {
    "head", "nose", "mouth", "lower_lip",
    "eye_left", "eye_right", "pupil_left", "pupil_right",
    "lid_left", "lid_right", "brow_left", "brow_right",
}


def apply_static_face(bones):
    by = {b["name"]: b for b in bones}
    before = copy.deepcopy(by)

    def material(bone, index=0):
        return copy.deepcopy(by[bone]["cubes"][index]["uv"])

    # Existing solid-colour UV islands are reused. No atlas changes or new bones.
    skin_face = next(iter(material("head").values()))
    skin = {f: copy.deepcopy(skin_face)
            for f in ["north", "south", "east", "west", "up", "down"]}
    dark = material("pupil_left", 1)

    def cube(origin, size, uv):
        return {"origin": origin, "size": size, "uv": copy.deepcopy(uv)}

    # Two small steps shape each aperture, rather than a finely subdivided mesh.
    # The outer lower corner is raised; the inner upper corner is lowered.
    xs = [-4, -3.35, -3.15, -.75, -.55, .55, .75, 3.15, 3.35, 4,
          -1.1, 1.1]
    ys = [24, 25.25, 26.25, 27.1, 27.32, 29.1, 29.3, 32]
    xs = sorted(set(xs))
    parts = [([-4, 24, -3.12], [4, 32, 4], "wood", None)]

    def opening(x, y):
        if -1.1 < x < 1.1 and 25.25 < y < 26.25:
            return True
        ax = abs(x)
        if .55 < ax < 3.35 and 27.1 < y < 29.3:
            return not (ax < .75 and y > 29.1
                        or ax > 3.15 and y < 27.32)
        return False

    for x0, x1 in zip(xs, xs[1:]):
        for y0, y1 in zip(ys, ys[1:]):
            if not opening((x0 + x1) / 2, (y0 + y1) / 2):
                parts.append(([x0, y0, -4], [x1, y1, -3.12], "wood", None))

    # Union exposes only boundary faces; neighbouring skin panels share vertices.
    head = []
    for e in surface_mesh(parts, True):
        origin = e["from"].copy()
        top = e["to"].copy()
        origin[1] += 16
        top[1] += 16
        face = next(iter(e["faces"]))
        head.append(cube(origin, [top[i] - origin[i] for i in range(3)],
                         {face: skin[face]}))

    for side, sign in [("left", 1), ("right", -1)]:
        # Mirror interval endpoints, not width, so UV orientation remains valid.
        def box(x0, x1, y, h, z, depth, uv):
            lo, hi = sorted([sign * x0, sign * x1])
            return cube([lo, y, z], [hi - lo, h, depth], uv)

        # A compact two-step upper lash establishes the eye shape in neutral pose.
        # One north-facing surface: no superimposed skin-facing back wall.
        head += [
            box(.76, 3.30, 29.19, .18, -4.025, 0, {"north": dark["north"]}),
            box(.56, .76, 29.09, .22, -4.025, 0, {"north": dark["north"]}),
        ]

        white = material("eye_" + side)
        iris, pupil, glint = [material("pupil_" + side, i) for i in range(3)]
        lid_skin, lid_edge = [material("lid_" + side, i) for i in range(2)]
        brow = material("brow_" + side)
        by["eye_" + side]["cubes"] = [
            box(.59, 3.31, 27.14, 2.12, -3.73, .30, white)]
        by["pupil_" + side]["cubes"] = [
            box(1.51, 2.39, 27.32, 1.68, -3.83, .08, iris),
            box(1.84, 2.06, 27.48, 1.35, -3.88, .045, pupil),
            box(1.68, 1.91, 28.60, .26, -3.935, .04, glint),
        ]
        # Retain the lid's original pivot and scaling interval for full closure.
        by["lid_" + side]["cubes"] = [
            box(.56, 3.34, 27.10, 2.20, -3.975, .065, lid_skin),
            box(.56, 3.34, 27.10, .12, -3.990, .01, lid_edge),
        ]
        by["brow_" + side]["cubes"] = [
            box(.76, 3.21, 29.45, .18, -4.055, .08, brow)]

    by["head"]["cubes"] = head
    cavity, tongue, teeth = [material("mouth", i) for i in range(3)]
    lip_skin, lip_line = [material("lower_lip", i) for i in range(2)]
    by["mouth"]["cubes"] = [
        cube([-1.08, 25.25, -3.75], [2.16, 1.0, .30], cavity),
        cube([-.72, 25.29, -3.84], [1.44, .18, .06], tongue),
        cube([-.88, 26.08, -3.82], [1.76, .13, .04], teeth),
    ]
    by["lower_lip"]["cubes"] = [
        cube([-1.1, 25.25, -4], [2.2, 1.0, 0],
             {"north": lip_skin["north"]}),
        cube([-1.035, 26.17, -4.014], [2.07, .08, 0],
             {"north": lip_line["north"]}),
    ]
    by["nose"]["cubes"] = [
        cube([-.13, 26.68, -4.105], [.26, .51, .105], skin),
        cube([-.23, 26.53, -4.19], [.46, .15, .19], skin),
    ]

    assert set(by) == set(before), "Stage 1 may not add bones"
    for name in by:
        if name not in FACE_BONES:
            assert by[name] == before[name], name
        else:
            assert {k: v for k, v in by[name].items() if k != "cubes"} == {
                k: v for k, v in before[name].items() if k != "cubes"}, name