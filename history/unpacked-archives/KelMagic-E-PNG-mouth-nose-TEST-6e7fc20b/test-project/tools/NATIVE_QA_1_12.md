# Optional native QA 1.12
Create disposable Creative flat world `KelMagicQA` with structures enabled. Run from project root, Java21, actual OpenGL display or Xvfb. Do not run GameTestServer concurrently on4GB RAM.

`bash ./gradlew -I tools/binary-build.init.gradle -I tools/native-structures-1.12.init.gradle runClient`

This profile queues a real locate request in an unknown area, records queue latency and server heartbeats, renders15 exterior and15 cutaway native block models, then exits after search completion. Inspect logs for a positive coordinate result, not merely a timeout. Optional graphics Qa* classes are excluded from the release JAR.

`bash ./gradlew -I tools/binary-build.init.gradle -I tools/native-rear-1.12.init.gradle runClient`

Seven native villager outfits from the back/three-quarter back, then exits. Both profiles save images under /data/native-qa-1.12/screenshots. Paths are developer conveniences, change if running outside /data. Static block-model scene does not render block entities such as chests/table BER or actors. Real placement is covered by GameTests.
