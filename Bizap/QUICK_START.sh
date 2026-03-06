#!/bin/bash
# 🎯 QUICK START SCRIPT FOR BIZAP TESTING
# Copy and paste these commands one at a time

echo "🚀 BIZAP QUICK START - March 6, 2026"
echo "======================================"
echo ""

# STEP 1: Navigate to project
echo "📂 Step 1: Navigate to Bizap directory"
echo "Command:"
echo "cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
echo ""

# STEP 2: Get latest code
echo "🔄 Step 2: Pull latest code from GitHub"
echo "Command:"
echo "git pull origin main"
echo ""
echo "Expected output:"
echo "  - Shows 'remote: Counting objects...' (new changes)"
echo "  - Shows 'Already up to date' (all changes local)"
echo ""

# STEP 3: Build the app
echo "🔨 Step 3: Build debug APK"
echo "Command:"
echo "./gradlew clean assembleDebug"
echo ""
echo "Expected output:"
echo "  - 'BUILD SUCCESSFUL in ~60s'"
echo "  - APK created at: app/build/outputs/apk/debug/app-debug.apk"
echo ""

# STEP 4: Run tests
echo "✅ Step 4: Run unit tests"
echo "Command:"
echo "./gradlew testDebugUnitTest"
echo ""
echo "Expected output:"
echo "  - 'BUILD SUCCESSFUL in ~15s'"
echo "  - No test failures"
echo ""

# STEP 5: Install on device
echo "📱 Step 5: Install APK on device/emulator"
echo "Command:"
echo "adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "Expected output:"
echo "  - 'Success' message"
echo "  - App installs and is ready to open"
echo ""

# STEP 6: Open the app
echo "🎮 Step 6: Open the app on device"
echo "Command (optional):"
echo "adb shell am start -n com.emul8r.bizap/.MainActivity"
echo ""

# STEP 7: Test manually
echo "🧪 Step 7: Run manual tests"
echo "See YOUR_ACTION_ITEMS.md for:"
echo "  1. Test Customer Creation"
echo "  2. Test Invoice Creation"
echo "  3. Test Database Migration"
echo "  4. Test Form Validation"
echo ""

# STEP 8: Report results
echo "📝 Step 8: Report your findings"
echo "Come back and tell me:"
echo "  ✅ Build successful? (YES/NO)"
echo "  ✅ Tests pass? (YES/NO)"
echo "  ✅ App installs? (YES/NO)"
echo "  ✅ Manual tests? (PASS/FAIL)"
echo "  ❌ Any errors or crashes?"
echo ""

echo "======================================"
echo "✅ All steps ready! Start with Step 1"
echo "======================================"

