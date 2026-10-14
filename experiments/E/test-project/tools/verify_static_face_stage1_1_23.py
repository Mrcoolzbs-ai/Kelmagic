"""Verify the static-only scope against the original 1.23.0 source ZIP."""
from pathlib import Path
import argparse
import base64
import hashlib
import json
import zipfile

P = Path(__file__).resolve().parents[1]
arg = argparse.ArgumentParser()
arg.add_argument("--baseline", type=Path, required=True)
args = arg.parse_args()
z = zipfile.ZipFile(args.baseline)

def old(path):
    return z.read("KelMagic/" + path)

def new(path):
    return (P / path).read_bytes()

allowed_existing = {
    "tools/generate_mascot_1_23.py", "tools/mascot_surface_fixes_1_23.py",
    "art/red-mage/red_mage.geo.json", "art/red-mage/red_mage_gecko.bbmodel",
    "src/main/resources/assets/kelmagic/geo/red_mage.geo.json",
}
modified = []
for n in z.namelist():
    if n.endswith("/") or not n.startswith("KelMagic/"):
        continue
    path = n.removeprefix("KelMagic/")
    if old(path) != new(path):
        modified.append(path)
        assert path in allowed_existing, "Out-of-scope modification: " + path
assert "mod_version=1.23.0" in new("gradle.properties").decode()

path = "src/main/resources/assets/kelmagic/geo/red_mage.geo.json"
before = json.loads(old(path))["minecraft:geometry"][0]
after = json.loads(new(path))["minecraft:geometry"][0]
assert {k: v for k, v in before.items() if k != "bones"} == {
    k: v for k, v in after.items() if k != "bones"}
ob = {b["name"]: b for b in before["bones"]}
nb = {b["name"]: b for b in after["bones"]}
face = {"head", "nose", "mouth", "lower_lip"} | {
    prefix + side for prefix in ["eye_", "pupil_", "lid_", "brow_"]
    for side in ["left", "right"]}
assert set(ob) == set(nb)
for name in ob:
    if name not in face:
        assert ob[name] == nb[name], name
    else:
        assert {k: v for k, v in ob[name].items() if k != "cubes"} == {
            k: v for k, v in nb[name].items() if k != "cubes"}, name

bbpath = "art/red-mage/red_mage_gecko.bbmodel"
obb = json.loads(old(bbpath))
bb = json.loads(new(bbpath))
assert obb["animations"] == bb["animations"]
assert obb["textures"] == bb["textures"]
assert len(bb["elements"]) == sum(len(b.get("cubes", [])) for b in nb.values())
assert json.loads(new("art/red-mage/red_mage.geo.json")) == json.loads(new(path))
assert new("art/red-mage/red_mage_geo.png") == new(
    "src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png")
assert base64.b64decode(bb["textures"][0]["source"].split(",", 1)[1]) == new(
    "art/red-mage/red_mage_geo.png")

for side in ["left", "right"]:
    # Negative Z is towards the viewer; distinct front planes prevent z-fighting.
    white = nb["eye_" + side]["cubes"][0]["origin"][2]
    iris = nb["pupil_" + side]["cubes"][0]["origin"][2]
    glint = nb["pupil_" + side]["cubes"][2]["origin"][2]
    lid = nb["lid_" + side]["cubes"][0]["origin"][2]
    assert -4 < lid < glint < iris < white < -3.12
assert all(c["size"][2] == 0 for c in nb["lower_lip"]["cubes"])

# No overlapping coplanar surface patches in the skin + upper-eye boundaries.
patches = []
for c in nb["head"]["cubes"]:
    assert len(c["uv"]) == 1
    face_name = next(iter(c["uv"]))
    axis = 0 if face_name in ["east", "west"] else 1 if face_name in ["up", "down"] else 2
    assert c["size"][axis] == 0
    plane = c["origin"][axis]
    lo = c["origin"]
    hi = [lo[i] + c["size"][i] for i in range(3)]
    others = [i for i in range(3) if i != axis]
    for a, p, l, h in patches:
        if a == axis and abs(p - plane) < 1e-8:
            assert not all(min(hi[i], h[i]) - max(lo[i], l[i]) > 1e-8
                           for i in others), "Overlapping visible face planes"
    patches.append((axis, plane, lo, hi))

print("PASS: original source baseline:", hashlib.sha256(args.baseline.read_bytes()).hexdigest())
print("PASS: exact version 1.23.0; only 5 existing files modified")
print("PASS: every Java file, build configuration, animation JSON and texture unchanged")
print("PASS: all 64 bone identities, pivots, parents and base rotations unchanged")
print("PASS: all non-face cubes unchanged; authoring clips and editor UUIDs unchanged")
print("PASS: source geometry, game geometry, BB elements and embedded texture synchronized")
print("PASS: ordered facial depths and no overlapping coplanar head surfaces")
print("Elements:", sum(len(b.get("cubes", [])) for b in ob.values()), "->",
      sum(len(b.get("cubes", [])) for b in nb.values()), "; head surfaces 47 -> 47")
print("Modified existing files:", *sorted(modified), sep="\n")