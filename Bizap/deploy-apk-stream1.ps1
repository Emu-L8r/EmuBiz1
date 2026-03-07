# PHASE 2 DAY 5 STREAM 1 - AUTOMATED TESTING DEPLOYMENT (Windows PowerShell)
# Run this script to automatically deploy APK and prepare testing environment

Write-Host "🚀 PHASE 2 DAY 5 STREAM 1 - AUTOMATED DEPLOYMENT" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""

# Step 1: Verify APK exists
Write-Host "✅ STEP 1: Verifying APK exists..." -ForegroundColor Yellow
$APK_PATH = "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $APK_PATH) {
    $APK_SIZE = (Get-Item $APK_PATH).Length / 1MB
    Write-Host "   ✅ APK found: $APK_PATH ($([Math]::Round($APK_SIZE, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "   ❌ ERROR: APK not found at $APK_PATH" -ForegroundColor Red
    Write-Host "   Please run: ./gradlew assembleDebug" -ForegroundColor Red
    exit 1
}

# Step 2: Check adb connectivity
Write-Host ""
Write-Host "✅ STEP 2: Checking ADB connectivity..." -ForegroundColor Yellow
$devices = adb devices | Select-String "device$" | Measure-Object | Select-Object -ExpandProperty Count
if ($devices -gt 0) {
    Write-Host "   ✅ Found $devices connected device(s)" -ForegroundColor Green
    adb devices
} else {
    Write-Host "   ❌ ERROR: No devices connected" -ForegroundColor Red
    Write-Host "   Please start the emulator or connect a device" -ForegroundColor Red
    exit 1
}

# Step 3: Uninstall previous version
Write-Host ""
Write-Host "✅ STEP 3: Uninstalling previous app version..." -ForegroundColor Yellow
adb uninstall com.emul8r.bizap 2>$null | Out-Null
Write-Host "   ✅ Previous version uninstalled (or didn't exist)" -ForegroundColor Green

# Step 4: Install APK
Write-Host ""
Write-Host "✅ STEP 4: Installing fresh APK..." -ForegroundColor Yellow
$installOutput = adb install -r $APK_PATH
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ APK installed successfully" -ForegroundColor Green
} else {
    Write-Host "   ❌ ERROR: APK installation failed" -ForegroundColor Red
    Write-Host $installOutput -ForegroundColor Red
    exit 1
}

# Step 5: Launch app
Write-Host ""
Write-Host "✅ STEP 5: Launching app..." -ForegroundColor Yellow
adb shell am start -n com.emul8r.bizap/.MainActivity
Write-Host "   ✅ App launched" -ForegroundColor Green
Write-Host ""

# Step 6: Instructions for manual setup
Write-Host "==================================================" -ForegroundColor Green
Write-Host "✅ DEPLOYMENT COMPLETE!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""
Write-Host "📋 NEXT STEPS (Manual):" -ForegroundColor Cyan
Write-Host "  1. Open Android Studio → View → Tool Windows → Database Inspector" -ForegroundColor White
Write-Host "  2. Select 'bizap.db' connection" -ForegroundColor White
Write-Host "  3. Navigate to 'offline_operations' table" -ForegroundColor White
Write-Host "  4. Open PowerShell terminal and run:" -ForegroundColor White
Write-Host "     adb logcat | Select-String '📶|💰|🗑️|👤|📋|offline'" -ForegroundColor Cyan
Write-Host "  5. In Emulator: Extended Controls (⋮) → Network → Airplane Mode ON" -ForegroundColor White
Write-Host "  6. Verify offline indicator appears" -ForegroundColor White
Write-Host "  7. Follow PHASE_2_DAY_5_STREAM_1_TESTING_EXECUTION.md" -ForegroundColor White
Write-Host ""
Write-Host "🟢 Ready to start Test Suite 1!" -ForegroundColor Green
Write-Host ""

