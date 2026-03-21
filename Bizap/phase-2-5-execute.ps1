# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# PHASE 2.5 CRASH FIX + MANUAL TESTING EXECUTION SCRIPT (PowerShell)
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# Purpose: Fix the Hilt injection crash and prepare for Phase 2.5 Task 7 testing
# Timeline: 5-10 minutes to fix, then ready for manual testing
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$PROJECT_ROOT = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$APK_PATH = "$PROJECT_ROOT\app\build\outputs\apk\debug\app-debug.apk"
$PACKAGE_NAME = "com.emul8r.bizap"

Set-Location $PROJECT_ROOT

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "PHASE 2.5: CRASH FIX + TESTING EXECUTION" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 1: VERIFY BUILD
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "📦 STEP 1: Building APK (crash fix included)..." -ForegroundColor Yellow
Write-Host ""

$buildOutput = & ./gradlew clean assembleDebug --no-daemon 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Build SUCCESSFUL" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Build FAILED - Check error messages above" -ForegroundColor Red
    $buildOutput | Select-Object -Last 50
    exit 1
}

Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 2: VERIFY APK
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "🔍 STEP 2: Verifying APK..." -ForegroundColor Yellow
Write-Host ""

if (-not (Test-Path $APK_PATH)) {
    Write-Host "❌ APK NOT FOUND at: $APK_PATH" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $APK_PATH).Length / 1MB
Write-Host "✅ APK found: $([Math]::Round($apkSize, 1)) MB" -ForegroundColor Green
Write-Host "   Location: $APK_PATH" -ForegroundColor Green
Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 3: CHECK EMULATOR/DEVICE
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "📱 STEP 3: Checking for emulator/device..." -ForegroundColor Yellow
Write-Host ""

$devices = & adb devices -l | Select-String -Pattern "emulator|device" | Select-Object -First 1

if ($null -eq $devices) {
    Write-Host "❌ No emulator or device found" -ForegroundColor Red
    Write-Host "   Start an emulator or connect a device first" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Device found" -ForegroundColor Green
Write-Host "   $devices" -ForegroundColor Green
Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 4: CLEAR APP DATA
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "🧹 STEP 4: Clearing app data (fresh start)..." -ForegroundColor Yellow
Write-Host ""

& adb shell pm clear $PACKAGE_NAME 2>&1 | Out-Null
Write-Host "✅ App data cleared" -ForegroundColor Green
Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 5: INSTALL APK
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "📲 STEP 5: Installing APK..." -ForegroundColor Yellow
Write-Host ""

$installOutput = & adb install -r $APK_PATH 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ APK installed successfully" -ForegroundColor Green
} else {
    Write-Host "❌ APK installation failed" -ForegroundColor Red
    Write-Host $installOutput
    exit 1
}

Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 6: LAUNCH APP
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "🚀 STEP 6: Launching app..." -ForegroundColor Yellow
Write-Host ""

& adb shell am start -n "$PACKAGE_NAME/.MainActivity" 2>&1 | Out-Null
Write-Host "✅ App launched" -ForegroundColor Green
Write-Host "   Waiting for startup (10 seconds)..." -ForegroundColor Green

Start-Sleep -Seconds 10
Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# STEP 7: CHECK FOR CRASHES
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "🔍 STEP 7: Checking for crashes..." -ForegroundColor Yellow
Write-Host ""

$logcatErrors = & adb logcat -d -s AndroidRuntime:E 2>&1 | Select-Object -First 20

if ($null -eq $logcatErrors -or $logcatErrors.Count -eq 0) {
    Write-Host "✅ NO CRASHES DETECTED - App is running!" -ForegroundColor Green
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
    Write-Host "🎉 CRASH FIX SUCCESSFUL!" -ForegroundColor Green
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ Phase 2.5 Task 7 Manual Testing is now READY" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Next Steps:" -ForegroundColor Cyan
    Write-Host "   1. Open PHASE_2_5_TASK_7_MANUAL_TESTING_GUIDE.md" -ForegroundColor Cyan
    Write-Host "   2. Run each test case (13 test suites)" -ForegroundColor Cyan
    Write-Host "   3. Document results in test matrix" -ForegroundColor Cyan
    Write-Host "   4. Test on 3+ devices if possible" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🎯 Test Suites:" -ForegroundColor Cyan
    Write-Host "   ✓ Classic Theme Features (4 tests)" -ForegroundColor Cyan
    Write-Host "   ✓ Modern Theme Features (4 tests)" -ForegroundColor Cyan
    Write-Host "   ✓ Theme Switching (3 tests)" -ForegroundColor Cyan
    Write-Host "   ✓ Persistence Testing (3 tests)" -ForegroundColor Cyan
    Write-Host "   ✓ Edge Cases & Validation (3 tests)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "⏱️  Estimated time: 2-3 hours for 3+ devices" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "❌ CRASHES DETECTED:" -ForegroundColor Red
    Write-Host ""
    $logcatErrors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    Write-Host ""
    Write-Host "Please fix these errors and try again" -ForegroundColor Red
    exit 1
}

