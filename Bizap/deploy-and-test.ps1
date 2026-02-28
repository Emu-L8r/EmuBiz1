# ═══════════════════════════════════════════════════════════════
# PHASE 3B STAGE 1C - DEPLOYMENT & ISOLATION TEST SCRIPT
# ═══════════════════════════════════════════════════════════════

Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  PHASE 3B STAGE 1C: MULTI-BUSINESS ISOLATION TEST" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Set ADB path
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apkPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"

# Check ADB exists
if (-not (Test-Path $adb)) {
    Write-Host "❌ ERROR: ADB not found at $adb" -ForegroundColor Red
    Write-Host "Please update the path in this script" -ForegroundColor Yellow
    exit 1
}

# Check APK exists
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ ERROR: APK not found at $apkPath" -ForegroundColor Red
    Write-Host "Please run build first: ./gradlew :app:assembleDebug" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ ADB found: $adb" -ForegroundColor Green
Write-Host "✅ APK found: $apkPath" -ForegroundColor Green
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# STEP 1: CHECK DEVICE CONNECTION
# ─────────────────────────────────────────────────────────────────
Write-Host "STEP 1: Checking device connection..." -ForegroundColor Yellow
& $adb devices

$devices = & $adb devices | Select-String "device$"
if ($devices.Count -eq 0) {
    Write-Host "❌ No devices connected!" -ForegroundColor Red
    Write-Host "Please start your emulator or connect a device" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Device connected" -ForegroundColor Green
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# STEP 2: UNINSTALL OLD VERSION
# ─────────────────────────────────────────────────────────────────
Write-Host "STEP 2: Uninstalling old version..." -ForegroundColor Yellow
& $adb uninstall com.emul8r.bizap 2>&1 | Out-Null
Write-Host "✅ Old version removed (if it existed)" -ForegroundColor Green
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# STEP 3: INSTALL NEW APK
# ─────────────────────────────────────────────────────────────────
Write-Host "STEP 3: Installing new APK..." -ForegroundColor Yellow
$installResult = & $adb install $apkPath 2>&1 | Out-String

if ($installResult -match "Success") {
    Write-Host "✅ APK installed successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Installation failed!" -ForegroundColor Red
    Write-Host $installResult -ForegroundColor Red
    exit 1
}
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# STEP 4: CLEAR APP DATA
# ─────────────────────────────────────────────────────────────────
Write-Host "STEP 4: Clearing app data for fresh start..." -ForegroundColor Yellow
& $adb shell pm clear com.emul8r.bizap 2>&1 | Out-Null
Write-Host "✅ App data cleared" -ForegroundColor Green
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# STEP 5: LAUNCH APP
# ─────────────────────────────────────────────────────────────────
Write-Host "STEP 5: Launching app..." -ForegroundColor Yellow
& $adb shell am start -n com.emul8r.bizap/.MainActivity 2>&1 | Out-Null
Start-Sleep -Seconds 2
Write-Host "✅ App launched" -ForegroundColor Green
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# STEP 6: MONITOR FOR CRASHES
# ─────────────────────────────────────────────────────────────────
Write-Host "STEP 6: Checking for crashes (5 seconds)..." -ForegroundColor Yellow
$logcat = & $adb logcat -d | Select-String "FATAL|AndroidRuntime|Exception" | Select-Object -First 5

if ($logcat) {
    Write-Host "⚠️ WARNING: Potential errors detected:" -ForegroundColor Yellow
    $logcat | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    Write-Host ""
    Write-Host "Check logcat for details: adb logcat | Select-String `"bizap`"" -ForegroundColor Yellow
} else {
    Write-Host "✅ No crashes detected" -ForegroundColor Green
}
Write-Host ""

# ─────────────────────────────────────────────────────────────────
# DEPLOYMENT COMPLETE
# ─────────────────────────────────────────────────────────────────
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  DEPLOYMENT COMPLETE - READY FOR ISOLATION TEST" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ App is running on your device/emulator" -ForegroundColor Green
Write-Host ""
Write-Host "NOW EXECUTE THE ISOLATION TEST:" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "STEP A: Verify Dashboard shows 'Default Business'" -ForegroundColor White
Write-Host "STEP B: Create invoice ($1234.56) - Check number is INV-2026-000001" -ForegroundColor White
Write-Host "STEP C: Switch to 'Emu Global B'" -ForegroundColor White
Write-Host "STEP D: ⭐⭐⭐ CRITICAL - Verify Vault is EMPTY" -ForegroundColor Yellow
Write-Host "STEP E: ⭐⭐⭐ CRITICAL - Create invoice ($5678.90) - Should be INV-2026-000001" -ForegroundColor Yellow
Write-Host "STEP F: Verify only Business B invoice visible" -ForegroundColor White
Write-Host "STEP G: ⭐⭐⭐ CRITICAL - Switch back - Data should REAPPEAR" -ForegroundColor Yellow
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Report results in this format:" -ForegroundColor Cyan
Write-Host "  Step A: ✅ PASS / ❌ FAIL" -ForegroundColor White
Write-Host "  Step B: ✅ PASS / ❌ FAIL" -ForegroundColor White
Write-Host "  Step C: ✅ PASS / ❌ FAIL" -ForegroundColor White
Write-Host "  Step D: ✅ PASS / ❌ FAIL [CRITICAL]" -ForegroundColor White
Write-Host "  Step E: ✅ PASS / ❌ FAIL [CRITICAL]" -ForegroundColor White
Write-Host "  Step F: ✅ PASS / ❌ FAIL" -ForegroundColor White
Write-Host "  Step G: ✅ PASS / ❌ FAIL [CRITICAL]" -ForegroundColor White
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan

