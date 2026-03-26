#!/bin/bash

# PDF EXPORT CRASH FIX - VERIFICATION SCRIPT
# Run this in Git Bash to verify the PDF export fix works

echo "🚀 PDF EXPORT CRASH FIX VERIFICATION SCRIPT"
echo "=========================================="
echo ""
echo "This script will:"
echo "1. Clean and rebuild the app"
echo "2. Install on device/emulator"
echo "3. Start monitoring Logcat for PDF operations"
echo ""
read -p "Press Enter to continue..." -t 10

BIZAP_DIR="C:/Users/Saucey/Documents/GitHub/EmuBiz/Bizap"
cd "$BIZAP_DIR" || exit 1

echo ""
echo "📦 Step 1: Building app..."
./gradlew clean build
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi
echo "✅ Build successful"

echo ""
echo "📱 Step 2: Installing debug APK..."
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Install failed!"
    exit 1
fi
echo "✅ App installed"

echo ""
echo "🚀 Step 3: Launching app..."
adb shell am start -n com.emul8r.bizap/.MainActivity
sleep 3

echo ""
echo "📊 Step 4: Monitoring Logcat for PDF operations..."
echo "=================================================="
echo ""
echo "Logcat will show PDF operations. Look for:"
echo "  ✅ PDF preview ready = SUCCESS"
echo "  ❌ Failed to generate = ERROR (needs fixing)"
echo ""
echo "Press Ctrl+C to stop monitoring"
echo ""

# Filter for PDF-related logs
adb logcat | grep -E "PDF|Export|bizap|Timber"

echo ""
echo "✅ Logcat monitoring ended"

