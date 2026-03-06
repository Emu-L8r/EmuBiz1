#!/bin/bash

echo "🔥 NUCLEAR GRADLE CLEAN - HILT FIX"
echo "==================================="
echo ""

# Navigate to project
cd "$(dirname "$0")"

echo "1️⃣  Stopping gradle daemon..."
./gradlew --stop 2>/dev/null

echo "2️⃣  Deleting project gradle cache..."
rm -rf .gradle
rm -rf app/.gradle

echo "3️⃣  Deleting build directories..."
rm -rf app/build
rm -rf build

echo "4️⃣  Uninstalling old APK from device..."
ADB="${ANDROID_HOME}/platform-tools/adb"
if [ ! -f "$ADB" ]; then
    ADB="$HOME/AppData/Local/Android/sdk/platform-tools/adb.exe"
fi
"$ADB" uninstall com.emul8r.bizap 2>/dev/null || echo "   (App not installed or device not connected)"

echo "5️⃣  Clearing logcat..."
"$ADB" logcat -c 2>/dev/null || true

echo ""
echo "6️⃣  REBUILDING FROM SCRATCH..."
echo "    This will take 4-5 minutes..."
echo ""

# Rebuild with verbose output
./gradlew clean assembleDebug --no-build-cache

echo ""
echo "Checking if APK was created..."
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ APK CREATED: $(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')"
    echo ""
    echo "7️⃣  Installing fresh APK..."
    "$ADB" install -r app/build/outputs/apk/debug/app-debug.apk

    echo ""
    echo "8️⃣  Launching app..."
    "$ADB" shell am start -n com.emul8r.bizap/.MainActivity

    echo ""
    echo "9️⃣  Monitoring logcat for 30 seconds..."
    sleep 2
    timeout 30 "$ADB" logcat 2>/dev/null | grep -E "FATAL|ClassNotFoundException|BizapApplication|MainActivity" || true

    echo ""
    echo "Checking if app is running..."
    if "$ADB" shell dumpsys activity 2>/dev/null | grep -q "com.emul8r.bizap"; then
        echo "🎉 ✅ SUCCESS! App is running without crash!"
    else
        echo "❌ App crashed or not running - see logcat above"
    fi
else
    echo "❌ APK NOT CREATED - Build failed"
    echo "Check the output above for errors"
fi



