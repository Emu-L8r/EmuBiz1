#!/bin/bash
# PHASE 2 DAY 5 STREAM 1 - AUTOMATED TESTING DEPLOYMENT SCRIPT
# This script automates APK deployment and environment setup for Stream 1 testing

echo "🚀 PHASE 2 DAY 5 STREAM 1 - AUTOMATED DEPLOYMENT"
echo "=================================================="
echo ""

# Step 1: Verify APK exists
echo "✅ STEP 1: Verifying APK exists..."
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
    echo "   ✅ APK found: $APK_PATH ($APK_SIZE)"
else
    echo "   ❌ ERROR: APK not found at $APK_PATH"
    echo "   Please run: ./gradlew assembleDebug"
    exit 1
fi

# Step 2: Check adb connectivity
echo ""
echo "✅ STEP 2: Checking ADB connectivity..."
DEVICES=$(adb devices | tail -n +2 | grep -c "device$")
if [ "$DEVICES" -gt 0 ]; then
    echo "   ✅ Found $DEVICES connected device(s)"
    adb devices
else
    echo "   ❌ ERROR: No devices connected"
    echo "   Please start the emulator or connect a device"
    exit 1
fi

# Step 3: Uninstall previous version
echo ""
echo "✅ STEP 3: Uninstalling previous app version..."
adb uninstall com.emul8r.bizap 2>/dev/null
echo "   ✅ Previous version uninstalled (or didn't exist)"

# Step 4: Install APK
echo ""
echo "✅ STEP 4: Installing fresh APK..."
adb install -r "$APK_PATH"
if [ $? -eq 0 ]; then
    echo "   ✅ APK installed successfully"
else
    echo "   ❌ ERROR: APK installation failed"
    exit 1
fi

# Step 5: Launch app
echo ""
echo "✅ STEP 5: Launching app..."
adb shell am start -n com.emul8r.bizap/.MainActivity
echo "   ✅ App launched"
echo ""

# Step 6: Instructions for manual setup
echo "=================================================="
echo "✅ DEPLOYMENT COMPLETE!"
echo "=================================================="
echo ""
echo "📋 NEXT STEPS (Manual):"
echo "  1. Open Android Studio → View → Tool Windows → Database Inspector"
echo "  2. Select 'bizap.db' connection"
echo "  3. Navigate to 'offline_operations' table"
echo "  4. Open Terminal and run:"
echo "     adb logcat | grep -E '📶|💰|🗑️|👤|📋|offline'"
echo "  5. In Emulator: Extended Controls (⋮) → Network → Airplane Mode ON"
echo "  6. Verify offline indicator appears"
echo "  7. Follow PHASE_2_DAY_5_STREAM_1_TESTING_EXECUTION.md"
echo ""
echo "🟢 Ready to start Test Suite 1!"
echo ""

