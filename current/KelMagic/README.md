> **R14 Artifacts TEST:** актуальные инструкции для этой локальной сборки — `README-R14-RU.md`, результаты — `QA-R14.md`, каталог — `ARTIFACT-CATALOG-R14.md`. Новых рецептов нет.

# KelMagic R12 TEST — 1.33.0-r12-roots-TEST
Complete cumulative project, not a patch. Current Russian instructions: README-R12-RU.md. Formulas: FORMULAS-R12.txt. Results and limitations: reports/QA-R12.md. Design canon and invariants: KELMAGIC_HANDOFF.md. Historical R9–R11 docs do not override R12.

Minecraft 1.21.1 / NeoForge 21.1.256 / Java 21 / Gradle 9.2.1 / GeckoLib 4.9.3. Curios optional. Libraries in libs are development dependencies, not embedded in the mod JAR.

Standard build on a clean machine (Java 21; dependencies need internet):
```sh
bash gradlew build
```
The full check retains an inherited eyeStateTest failure (`continuous pulse`). Remaining checks and jar:
```sh
bash gradlew check -x eyeStateTest jar
bash gradlew -I tools/research-r12/gametest.init.gradle runGameTestServer
```
The server suite currently has five inherited failures: see QA-R12, do not hide them. The sandbox used `-I tools/recovery/binary-build.init.gradle` with already prepared Minecraft artifacts; it is not a mod runtime dependency. JAR excludes developer Qa* render harnesses.

Source of truth is src/. Historical generators contain old sandbox paths and must not be rerun over final source. No game world, caches or user data are shipped. Native QA is destructive and requires an expendable superflat world called KelMagicR12QA. `tools/research-r12/prepare-scene.py /path/to/flat/level.dat` resets that dedicated scene (grass at y=-61). Run native.init.gradle only on this test world, with a local display/Xvfb and OpenGL. QA generators and static baseline comparisons are evidence tools, not required for normal compilation; their archived inputs are not bundled again.

Back up worlds before installing and especially before grove awakening. Terrain cleanup intentionally includes player-placed tagged logs/leaves inside the eligible area. Third-party region protection mods, shaders and large multiplayer modpacks are unverified.
