#!/bin/bash
# check-blockers.sh — Monitor for dependency blocker resolution
# Usage: bash check-blockers.sh
# Checks if the 4 critical blockers for AGP 9.0 migration have been resolved.

set -euo pipefail

echo "=== CoilTester Dependency Blocker Monitor ==="
echo "Date: $(date)"
echo ""

# 1. Check com.android.kotlin.multiplatform.library plugin
echo "--- 1. com.android.kotlin.multiplatform.library plugin ---"
STATUS=$(curl -sI --max-time 10 "https://dl.google.com/dl/android/maven2/com/android/kotlin/multiplatform/library/com.android.kotlin.multiplatform.library.gradle.plugin/9.5.0/com.android.kotlin.multiplatform.library.gradle.plugin-9.5.0.jar" 2>/dev/null | head -1)
echo "AGP 9.5.0 plugin JAR: $STATUS"
if echo "$STATUS" | grep -q "200"; then
    echo "  ✅ AVAILABLE — AGP 9.0 migration can proceed"
else
    echo "  ❌ NOT AVAILABLE — AGP 9.0 migration blocked"
fi

# 2. Check KSP 2.4.x
echo ""
echo "--- 2. KSP 2.4.x ---"
STATUS=$(curl -s --max-time 10 "https://dl.google.com/dl/android/maven2/com/google/devtools/ksp/symbol-processing-gradle-plugin/maven-metadata.xml" 2>/dev/null | grep -o "2\.4\.[0-9]*" | sort -u | tail -1)
if [ -n "$STATUS" ]; then
    echo "  ✅ KSP 2.4.x available: $STATUS"
else
    echo "  ❌ KSP 2.4.x NOT available"
fi

# 3. Check kotlinx-serialization 1.9+
echo ""
echo "--- 3. kotlinx-serialization 1.9.0+ ---"
STATUS=$(curl -s --max-time 10 "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-serialization-json/maven-metadata.xml" 2>/dev/null | grep -o "1\.9\.[0-9]*" | sort -u | tail -1)
if [ -n "$STATUS" ]; then
    echo "  ✅ kotlinx-serialization 1.9+ available: $STATUS"
else
    echo "  ❌ kotlinx-serialization 1.9+ NOT available"
fi

# 4. Check Koin 4.2.0+
echo ""
echo "--- 4. Koin 4.2.0+ ---"
STATUS=$(curl -s --max-time 10 "https://repo1.maven.org/maven2/io/insert-koin/koin-core/maven-metadata.xml" 2>/dev/null | grep -o "4\.2\.[0-9]*" | sort -u | tail -1)
if [ -n "$STATUS" ]; then
    echo "  ✅ Koin 4.2+ available: $STATUS"
else
    echo "  ❌ Koin 4.2+ NOT available"
fi

# 5. Check AGP 9.5.0
echo ""
echo "--- 5. AGP 9.5.0 ---"
STATUS=$(curl -sI --max-time 10 "https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/9.5.0/gradle-9.5.0.jar" 2>/dev/null | head -1)
echo "AGP 9.5.0 JAR: $STATUS"

echo ""
echo "=== Summary ==="
echo "Run this script periodically to check if blockers have been resolved."
echo "When all 4 blockers show ✅, the AGP 9.0 migration can proceed."