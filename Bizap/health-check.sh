#!/bin/bash

# ============================================================================
# BIZAP GOLDEN BUILD - SIMPLIFIED HEALTH CHECK SCRIPT
# ============================================================================
# Purpose: Verify core systems and generate health report
# Usage: bash health-check.sh
# ============================================================================

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║     🏥 BIZAP GOLDEN BUILD - HEALTH CHECK                   ║"
echo "║              Version 1.0 | $(date '+%Y-%m-%d %H:%M:%S')              ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

PASS=0
FAIL=0
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="HEALTH_CHECK_$TIMESTAMP.txt"

{
    echo "═══════════════════════════════════════════════════════════"
    echo "BIZAP HEALTH CHECK REPORT - $TIMESTAMP"
    echo "═══════════════════════════════════════════════════════════"
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 1: BUILD VERSION
    # ────────────────────────────────────────────────────────────────
    echo "📦 CHECK 1: BUILD VERSION"
    if grep -q "versionCode = 3" app/build.gradle.kts; then
        echo "✅ PASS: versionCode = 3"
        ((PASS++))
    else
        echo "❌ FAIL: versionCode not 3"
        ((FAIL++))
    fi

    if grep -q 'versionName = "1.0-stable-golden"' app/build.gradle.kts; then
        echo "✅ PASS: versionName = 1.0-stable-golden"
        ((PASS++))
    else
        echo "❌ FAIL: versionName not correct"
        ((FAIL++))
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 2: GIT REPOSITORY
    # ────────────────────────────────────────────────────────────────
    echo "📝 CHECK 2: GIT REPOSITORY"
    if [ -d ".git" ]; then
        echo "✅ PASS: Git repository initialized"
        ((PASS++))
    else
        echo "❌ FAIL: No git repository"
        ((FAIL++))
    fi

    if git tag -l | grep -q "v1.0-stable-golden"; then
        echo "✅ PASS: Golden build tag exists (v1.0-stable-golden)"
        ((PASS++))
    else
        echo "❌ FAIL: Golden build tag missing"
        ((FAIL++))
    fi

    if [ -z "$(git status --porcelain)" ]; then
        echo "✅ PASS: Working tree clean"
        ((PASS++))
    else
        echo "⚠️  WARN: Working tree has uncommitted changes"
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 3: DEVICE CONNECTION
    # ────────────────────────────────────────────────────────────────
    echo "📱 CHECK 3: DEVICE CONNECTION"
    if command -v adb &> /dev/null; then
        echo "✅ PASS: ADB available"
        ((PASS++))
    else
        echo "❌ FAIL: ADB not found"
        ((FAIL++))
    fi

    DEVICE_COUNT=$(adb devices 2>/dev/null | tail -n +2 | grep -c "device$" || echo "0")
    if [ "$DEVICE_COUNT" -gt 0 ]; then
        echo "✅ PASS: Device connected ($DEVICE_COUNT)"
        ((PASS++))
    else
        echo "⚠️  WARN: No device connected (not critical)"
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 4: APPLICATION
    # ────────────────────────────────────────────────────────────────
    echo "⚙️  CHECK 4: APPLICATION INSTALLATION"
    if [ "$DEVICE_COUNT" -gt 0 ]; then
        if adb shell pm list packages 2>/dev/null | grep -q "com.emul8r.bizap"; then
            echo "✅ PASS: App installed"
            ((PASS++))
        else
            echo "⚠️  WARN: App not installed (can install via ADB)"
        fi
    else
        echo "⚠️  WARN: Skipped (no device connected)"
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 5: PIN SECURITY
    # ────────────────────────────────────────────────────────────────
    echo "🔒 CHECK 5: PIN SECURITY (4-DIGIT ENFORCEMENT)"
    if grep -r "MIN_PIN_LENGTH = 4" app/src/main/java/com/emul8r/bizap/domain/service/ &>/dev/null; then
        echo "✅ PASS: PIN minimum length = 4 digits"
        ((PASS++))
    else
        echo "❌ FAIL: PIN minimum length check failed"
        ((FAIL++))
    fi

    if grep -r "BruteForceProtection" app/src/main/java/com/emul8r/bizap/ui/auth/ &>/dev/null; then
        echo "✅ PASS: Brute force protection integrated"
        ((PASS++))
    else
        echo "❌ FAIL: Brute force protection missing"
        ((FAIL++))
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 6: BUILD INTEGRITY
    # ────────────────────────────────────────────────────────────────
    echo "🏗️  CHECK 6: BUILD INTEGRITY"
    if [ -f "app/build.gradle.kts" ]; then
        echo "✅ PASS: build.gradle.kts exists"
        ((PASS++))
    else
        echo "❌ FAIL: build.gradle.kts missing"
        ((FAIL++))
    fi

    if [ -f "build.gradle.kts" ]; then
        echo "✅ PASS: Root build.gradle.kts exists"
        ((PASS++))
    else
        echo "❌ FAIL: Root build.gradle.kts missing"
        ((FAIL++))
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # CHECK 7: TEST SUITE
    # ────────────────────────────────────────────────────────────────
    echo "🧪 CHECK 7: UNIFIED TEST SUITE"
    if [ -f "app/src/androidTest/java/com/emul8r/bizap/androidtest/GoldenBuildVerificationTest.kt" ]; then
        echo "✅ PASS: Unified test suite present"
        ((PASS++))
    else
        echo "❌ FAIL: Unified test suite missing"
        ((FAIL++))
    fi
    echo ""

    # ────────────────────────────────────────────────────────────────
    # SUMMARY
    # ────────────────────────────────────────────────────────────────
    echo "═══════════════════════════════════════════════════════════"
    echo "SUMMARY"
    echo "═══════════════════════════════════════════════════════════"
    echo "✅ PASSED: $PASS"
    echo "❌ FAILED: $FAIL"
    echo ""

    if [ $FAIL -eq 0 ]; then
        echo "🟢 HEALTH STATUS: EXCELLENT"
        echo "✅ Ready for deployment"
    elif [ $FAIL -le 2 ]; then
        echo "🟡 HEALTH STATUS: GOOD (Minor issues)"
        echo "⚠️  Review failures above"
    else
        echo "🔴 HEALTH STATUS: NEEDS ATTENTION"
        echo "❌ Critical issues detected"
    fi
    echo ""
    echo "═══════════════════════════════════════════════════════════"
    echo "NEXT STEPS:"
    echo "═══════════════════════════════════════════════════════════"
    echo "1. Run unit tests: ./gradlew test"
    echo "2. Run integration tests: ./gradlew connectedAndroidTest"
    echo "3. Run on device: ./gradlew installDebug"
    echo "4. Manual verification checklist: See GOLDEN_BUILD_APR_2026.md"
    echo ""
    echo "Report generated: $TIMESTAMP"

} | tee "$REPORT_FILE"

echo ""
echo "📄 Report saved to: $REPORT_FILE"
echo ""

if [ $FAIL -eq 0 ]; then
    exit 0
else
    exit 1
fi

