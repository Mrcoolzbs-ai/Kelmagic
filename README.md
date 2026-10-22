# KelMagic — complete backup source import

This repository contains the extracted source tree of `KelMagic_FULL_BACKUP.zip`, not only the main working project. Start with `HANDOFF.md` and `RECOVERY.md`. These are historical backup documents; the statements that Git did not exist refer to the backup creation time, before this import.

## Current selected version — 1.34.1 R13.1 — НЕСТАБИЛЬНАЯ

The latest cumulative project is [current/KelMagic](current/KelMagic). **Unstable test version / pre-release**, not a stable release. See [publication notes](current/README.md), [R13.1 changes](current/KelMagic/README-R13.1-RU.md) and [known QA limitations](current/KelMagic/QA-R13.1.md). Back up worlds before updating.

This snapshot includes the creative-codex serialization crash fix, 17 codex documentation entries, restyled item/block textures and the limited-air pendant correction. Historical snapshots remain unchanged; the prior selected 1.27 source is also preserved in Git history at `5ddcc48`.

## Layout
- `main/KelMagic/`: main project, preserved separately.
- `approved/D-final-lashes/`: approved frozen design, do not overwrite.
- `experiments/E/`: full tested experimental project and evidence.
- `experiments/kelmagic-F2-art/`: E baseline plus F2 artwork; unapproved, not integrated.
- `checkpoints/`, `history/`, `references/`: previous snapshots, source tools and references.
- `history/unpacked-archives/`: source-only contents of historical ZIP backups; mapping in `backup-metadata/GITHUB_IMPORT.json`.

The Git import excludes JAR/ZIP packages, build/runtime directories, caches, logs and private local files. Small `gradle-wrapper.jar` bootstrap files are retained. Artwork, .bbmodel, geometry/UV, PNG source textures, animation JSON, Java, tests, QA tools, generators, docs and available reference images are retained. The repository is a source import, not a byte-for-byte replacement of the complete archival ZIP. `PAYLOAD_SHA256.json` and archived inventory describe the full original ZIP, including files deliberately ignored by Git; `GITHUB_IMPORT.json` describes this import.

## Build prerequisites
Use Java 21 and the existing Gradle wrapper in the chosen project. Minecraft 1.21.1, NeoForge 21.1.256, GeckoLib 4.9.3. Existing builds use `libs/geckolib-neoforge-1.21.1-4.9.3.jar`; restore the official NeoForge 1.21.1 GeckoLib 4.9.3 JAR from the original backup or official GeckoLib distribution before building. The R13.1 publication updates the selected gameplay source; third-party dependency JARs remain excluded.

F2 files in this backup were reconstructed from recorded drawing commands after the earlier working directory became unavailable. They have not been artistically approved. This import does not change that provenance, implement speech articulation or integrate F2.

No build/game tests were rerun for Git publication. Token/credentials are never stored in the repository. Nested .github/workflows files remain at their archived paths; this import does not promote them into active root workflows.

## Previous temporarily final model (preserved)
The previous checkpoint is [F3-hair7 1.24.4](experiments/F3-hair7-final/README.md) for the cumulative updated project, tested mod JAR and native-game proofs. Original main/D/E snapshots above are preserved.
