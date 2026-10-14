# Optional native lore and locate QA
Java21,display or Xvfb,4GBRAM:do not run client and GameTestServer concurrently. Developer Qa* classes excluded from release JAR.

Create disposable Creative flat save KelMagicQA, structures enabled. Set run/options.txt lang:ru_ru for Russian pages.

`bash ./gradlew -I tools/binary-build.init.gradle -I tools/native-lore-1.13.init.gradle runClient`

Five screenshots under /data/native-qa-1.13/screenshots. Real command dispatcher queues locate from90000,64,90000; the world region must be unknown for cold-generation timing. A second run there is not a fresh benchmark. Inspect coordinate success explicitly (Russian locale prints Ближайшее строение...), not merely queue empty/timeout. Property kelmagic.locateQA disabled in released builds. QaLore prints marker then gracefully stops; guard stage>=5 prevents one extra client tick after stop from indexing past final page.
