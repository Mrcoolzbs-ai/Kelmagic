#!/usr/bin/env bash
# Run with Java 21 (JAVA_HOME set). No Minecraft client is launched.
set -euo pipefail
PROJECT=$(cd "$(dirname "$0")/../.." && pwd)
ROOT=$(cd "$PROJECT/.." && pwd)
cd "$PROJECT"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
ORIGINAL=${1:-"$ROOT/original/kelmagic-1.27.0-f3-hair-accum-r2b-jfrfix.jar"}
bash gradlew -I tools/recovery/binary-build.init.gradle -I tools/recovery/classpath.init.gradle jar writeRecoveryClasspath --no-configuration-cache --console=plain
CP=$(cat build/recovery-runtime-classpath.txt)
REBUILT="$PROJECT/build/libs/kelmagic-1.27.0-f3-hair-accum-r2b-jfrfix.jar"
mkdir -p build/recovery-tests
"$JAVAC" -cp "$CP" -d build/recovery-tests tools/recovery/RecoveryEquivalence.java tools/recovery/RecoveryPresentation.java
# The pure solver loader must not inherit production dev.kelmagic classes.
JOML=$(printf '%s' "$CP" | tr ':' '\n' | grep '/joml/1.10.5/' | head -1)
for FMA in false true; do
 "$JAVA" -Xmx512m -Djoml.useMathFma="$FMA" -cp "build/recovery-tests:$JOML" RecoveryEquivalence "$ORIGINAL" "$REBUILT"
done
"$JAVA" -Xmx512m -cp "build/recovery-tests:$CP" RecoveryPresentation "$ORIGINAL" "$REBUILT"
