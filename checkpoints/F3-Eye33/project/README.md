# F3-hair7 — temporarily final model (1.24.4)

Current user-approved checkpoint. Complete cumulative project source, not a patch requiring older experimental copies.

Includes the hair4/sample2 combined volume + alpha-plane construction, hair6 integration, body-owned chest locks, static neck surfaces, shortened chin (20%), seated head (-1.208 model pixels), fitted side UVs, and final direct breast-corner-to-chin connectors. Existing face/mouth/eye animation logic and palette are preserved.

- `src/`: current Java and all resources.
- `release/`: tested final mod JAR.
- `proof/`: six native game states and before/after images; 90 degrees is QA stress only.
- `reports/`: focused manifests and checks.
- `tools/`: historical modifiers and QA helpers. Do not rerun old stage generators on the final model. Some helpers retain original sandbox paths.

Build with Java 21: restore official GeckoLib 4.9.3 NeoForge 1.21.1 JAR to `libs/geckolib-neoforge-1.21.1-4.9.3.jar`, then run `bash gradlew -I tools/binary-build.init.gradle jar`. Minecraft 1.21.1 / NeoForge 21.1.256. Third-party dependency JARs are not republished.

The final JAR excludes QA classes. Short targeted game checks were completed before publication; they do not prove absence of clipping in all poses. No new game tests are claimed for Git publication.

Published on repository branch main; original `main/KelMagic`, approved D and original E are preserved. No caches, worlds, credentials, or reference LovsPlayer JAR included.
