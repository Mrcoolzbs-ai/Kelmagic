# Verification 1.5.0

Corretto JDK 21 / Minecraft 1.21.1 / NeoForge 21.1.256.

Final command: `bash ./gradlew build runGameTestServer --max-workers=1 --console=plain`.
Result: BUILD SUCCESSFUL; all 43 required GameTests passed; CORE TESTS PASSED: 367 checks.

New regressions: idle mana recovery at 10sec, upgraded recovery at 5sec, max cap, cancellation preserving regen timer; ordinary item destruction and netherite immunity; netherrack eternal fire; dry campfire lighting; wood consumption; wall fire shape/survival; doFireTick=false disables block burn/spread/aging. Existing Fire Resistance bypass, natural fire expiry and saved burn tests pass. All earlier mana/chat/scripter/codex/dye/recipes/brewing/PvP/overload checks remain.

Native client: successfully loaded both client-only Mixins, joined existing sandbox test world; examined new robe UV and plate face without mouth guard; burned test player with animated purple entity flames and first-person screen flames; creative transition cleared effect; visually compared ordinary and purple fire side by side. No mod render/injection exception. Environmental narrator/audio-device errors are not mod failures; fire sound behavior is inherited from FireBlock.animateTick (audible output was not separately measured).

Last source-only change after native test deferred campfire ignition to authorized fire tick and added its server regression; final 43-test build includes it. Art models checked in client from the same generated resources.

A native screenshot is not exhaustive compatibility proof. Two-client remote server/shader-pack combinations, small-scale codebook responsiveness and broad balance are still pending. Full language/worldgen specifications remain partly unimplemented.
