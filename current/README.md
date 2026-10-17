# Selected KelMagic source — 1.27 / Hair180

The user explicitly selected180Hz after the final visual demonstration and requested publication of this variant together with the current model and table interaction changes.

## Project

`KelMagic/` is the complete cumulative production-source copy:
- Hair v8.4 and Head65, RearHairCloth9×19,180substeps/second target, iteration limit64.
- Existing accepted temporary-vector reuse, topology-only pair list and early no-advance preparation gate. No rejected coefficient-cache or48-iteration experiment.
- Current character geometry, source artwork, textures, animations and existing Eye Controller v3.3.
- The1.27 table orientation/collision/page-ray and table/codex interaction fixes, as present in the saved1.27 source.
- All other source/resources from the preserved baseline, unchanged. The existing `mod_version=1.27.0-f3-table-fix-diagnostic` string is retained; no fabricated release version or newly tested binary is implied.

Relative to the immutable240Hz/64 baseline, the selected source changes exactly one expression in `RearHairCloth.java`: `Math.ceil(dt*240)` to `Math.ceil(dt*180)`. No solver, mesh, collision, gravity or compliance changes were made for publication. The240Hz baseline and all historical repository snapshots are preserved; no old branch or history is overwritten.

## Build

Java21, Minecraft1.21.1, NeoForge21.1.256, GeckoLib4.9.3. Before building, restore the official GeckoLib NeoForge1.21.1 dependency to `KelMagic/libs/geckolib-neoforge-1.21.1-4.9.3.jar`; repository rules exclude third-party dependency JARs. The Gradle wrapper bootstrap JAR is included.

```sh
cd current/KelMagic
bash gradlew -I tools/binary-build.init.gradle jar
```

Old project documentation and generator/QA helpers are preserved as source history, not instructions to regenerate the selected assets. Existing build exclusions for historical QA classes are retained. The new frequency-measurement rigs, FinalProbe, final-gameplay driver, videos, logs, saves, caches, credentials and build products are not part of this publication.

## Evidence limits retained

The prior240/180 experiment showed a median speed improvement and better four-second reversal recovery for180 in that controlled sequence, but one timing repeat was slower and peak structural strain increased. The final continuous ordinary-movement take showed transient bends/lifts. Its software-rendered client produced about5actualFPS, and some rendered frames lacked integration records; it was not an unconditional full visual pass. These limitations are not waived or hidden by the user's selection.

No new full audit, build, performance run or game test was performed for Git publication. The source snapshot and one-expression180Hz difference were checked against the saved SHA-256 manifest. See `PUBLICATION_SHA256.json` for the actual published source files and the explicit dependency exclusion. No token is stored in this repository.
