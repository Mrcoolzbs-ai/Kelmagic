# Verification — 1.4.0

Minecraft 1.21.1 / NeoForge 21.1.256 / Corretto JDK 21.

`bash ./gradlew build runGameTestServer --max-workers=1 --console=plain`: BUILD SUCCESSFUL; all 37 required GameTests passed; CORE TESTS PASSED: 367 checks.

Regression coverage includes mana, equipment, dye persistence, recipe outputs, brewing, scripter capture/replay, PvP/protection gates and overload. New coverage includes magic fire damage despite Fire Resistance, creative immunity, burn persistence, final fire block expiry, actual ancient-manuscript reveal, actual manuscript-to-codex crafting preserving components and rejecting duplicate words, both ink alternatives and server validation of notes.

Native client checks in our own sandbox test world: purple/crimson Heart motes visually confirmed in third-person with F1 (the retained totem-style item animation otherwise obscures the center); layered garments/plates and crouch, mana tube and Shift-only numbers, purple animated block, codex learned/unknown pages, editable note persisted after closing/reopening, real item recipe diagram. Fixed repeated Screen background blur by rendering the background once before the book; widgets rendered without a second Screen.render background pass.

Thaumcraft was inspected only as supplied archive source/texture reference, never run in game. No reference classes or textures included in the mod.

Limits: no manuscript loot distribution yet; six registered words only. Wet extinguishing remains immediate. Entity burn uses purple dust effects, not a replacement first-person flame overlay. Dedicated tests verify duration/damage; visual art quality remains subjective.
