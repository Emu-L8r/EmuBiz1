#!/bin/bash
# Build and deploy script for Bizap
# Run from Android Studio terminal or IDE

echo "==============================================="
echo "Bizap Data Consistency Fixes - Build & Deploy"
echo "==============================================="
echo ""

# Step 1: Kill any hanging gradle processes
echo "[1/5] Killing any hanging gradle processes..."
pkill -f "gradle" 2>/dev/null || true
pkill -f "java" 2>/dev/null || true
sleep 2

# Step 2: Clean build cache
echo "[2/5] Cleaning build cache..."
rm -rf app/build
rm -rf .gradle
rm -rf build

# Step 3: Fresh build
echo "[3/5] Running fresh build..."
./gradlew clean build -x test --no-daemon --stacktrace

if [ $? -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL"
else
    echo "❌ BUILD FAILED"
    exit 1
fi

# Step 4: Install on emulator
echo "[4/5] Installing APK on emulator..."
if adb devices | grep -q "emulator"; then
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    echo "✅ APK INSTALLED"
else
    echo "⚠️  No emulator found. APK ready at: app/build/outputs/apk/debug/app-debug.apk"
fi

# Step 5: Launch app
echo "[5/5] Launching app..."
adb shell am start -n com.emul8r.bizap/com.emul8r.bizap.MainActivity

echo ""
echo "==============================================="
echo "✅ Build and deploy complete!"
echo "==============================================="

