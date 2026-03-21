#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# PHASE 2.5 CRASH FIX + MANUAL TESTING EXECUTION SCRIPT
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Purpose: Fix the Hilt injection crash and prepare for Phase 2.5 Task 7 testing
# Timeline: 5-10 minutes to fix, then ready for manual testing
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -e  # Exit on error

PROJECT_ROOT="C:/Users/Saucey/Documents/GitHub/EmuBiz/Bizap"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
PACKAGE_NAME="com.emul8r.bizap"

cd "$PROJECT_ROOT"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "PHASE 2.5: CRASH FIX + TESTING EXECUTION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 1: VERIFY BUILD
# ────────────────────────────────────────────────────────────────────────────────

echo "📦 STEP 1: Building APK (crash fix included)..."
echo ""

if ./gradlew clean assembleDebug --no-daemon; then
    echo ""
    echo "✅ Build SUCCESSFUL"
else
    echo ""
    echo "❌ Build FAILED - Check error messages above"
    exit 1
fi

echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 2: VERIFY APK
# ────────────────────────────────────────────────────────────────────────────────

echo "🔍 STEP 2: Verifying APK..."
echo ""

if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK NOT FOUND at: $APK_PATH"
    exit 1
fi

APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
echo "✅ APK found: $APK_SIZE"
echo "   Location: $APK_PATH"
echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 3: CHECK EMULATOR/DEVICE
# ────────────────────────────────────────────────────────────────────────────────

echo "📱 STEP 3: Checking for emulator/device..."
echo ""

DEVICE=$(adb devices -l | grep -E "emulator|device" | head -1 | awk '{print $1}')

if [ -z "$DEVICE" ]; then
    echo "❌ No emulator or device found"
    echo "   Start an emulator or connect a device first"
    exit 1
fi

echo "✅ Device found: $DEVICE"
echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 4: CLEAR APP DATA
# ────────────────────────────────────────────────────────────────────────────────

echo "🧹 STEP 4: Clearing app data (fresh start)..."
echo ""

adb shell pm clear $PACKAGE_NAME 2>/dev/null || echo "   (App not installed yet - that's OK)"
echo "✅ App data cleared"
echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 5: INSTALL APK
# ────────────────────────────────────────────────────────────────────────────────

echo "📲 STEP 5: Installing APK..."
echo ""

if adb install -r "$APK_PATH" > /dev/null 2>&1; then
    echo "✅ APK installed successfully"
else
    echo "❌ APK installation failed"
    exit 1
fi

echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 6: LAUNCH APP
# ────────────────────────────────────────────────────────────────────────────────

echo "🚀 STEP 6: Launching app..."
echo ""

adb shell am start -n "$PACKAGE_NAME/.MainActivity"
echo "✅ App launched"
echo "   Waiting for startup (10 seconds)..."
sleep 10
echo ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 7: CHECK FOR CRASHES
# ────────────────────────────────────────────────────────────────────────────────

echo "🔍 STEP 7: Checking for crashes..."
echo ""

LOGCAT_ERRORS=$(adb logcat -d -s AndroidRuntime:E 2>/dev/null | head -20)

if [ -z "$LOGCAT_ERRORS" ]; then
    echo "✅ NO CRASHES DETECTED - App is running!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🎉 CRASH FIX SUCCESSFUL!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "✅ Phase 2.5 Task 7 Manual Testing is now READY"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Open PHASE_2_5_TASK_7_MANUAL_TESTING_GUIDE.md"
    echo "   2. Run each test case (13 test suites)"
    echo "   3. Document results in test matrix"
    echo "   4. Test on 3+ devices if possible"
    echo ""
    echo "🎯 Test Suites:"
    echo "   ✓ Classic Theme Features (4 tests)"
    echo "   ✓ Modern Theme Features (4 tests)"
    echo "   ✓ Theme Switching (3 tests)"
    echo "   ✓ Persistence Testing (3 tests)"
    echo "   ✓ Edge Cases & Validation (3 tests)"
    echo ""
    echo "⏱️  Estimated time: 2-3 hours for 3+ devices"
    echo ""
else
    echo "❌ CRASHES DETECTED:"
    echo ""
    echo "$LOGCAT_ERRORS"
    echo ""
    echo "Please fix these errors and try again"
    exit 1
fi

