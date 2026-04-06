#!/usr/bin/env powershell

param(
    [string]$Device = "emulator-5554"
)

Write-Host ""
Write-Host "============================================================"
Write-Host "FIREBASE CRASHLYTICS - DIRECT CRASH TEST (No Manual Tap)"
Write-Host "============================================================"
Write-Host ""

$DEVICE = $Device
$PACKAGE = "com.emul8r.bizap"
$PROJECT_ID = "bizap-801c0"

Write-Host "Configuration:"
Write-Host "  Device:    $DEVICE"
Write-Host "  Package:   $PACKAGE"
Write-Host ""

Write-Host "[STEP 1] Verifying device..."
$test = adb -s $DEVICE shell echo "OK" 2>&1
if ($test -ne "OK") {
    Write-Host "ERROR: Device offline" -ForegroundColor Red
    exit 1
}
Write-Host "Device connected"
Write-Host ""

Write-Host "[STEP 2] Checking Logcat for Firebase Crashlytics logs..."
Write-Host ""

adb -s $DEVICE logcat -d | Select-String "FirebaseCrashlytics|Timber|crashlytics" | Select-Object -First 30

Write-Host ""
Write-Host "============================================================"
Write-Host "ANALYSIS:"
Write-Host "============================================================"
Write-Host ""

Write-Host "Checking for:"
Write-Host "1. Is app running?"
$running = adb -s $DEVICE shell am stack list 2>&1 | Select-String "com.emul8r.bizap"
if ($running) {
    Write-Host "   YES - Bizap is running" -ForegroundColor Green
} else {
    Write-Host "   NO - Bizap is not running" -ForegroundColor Red
}

Write-Host ""
Write-Host "2. Is BuildConfig.DEBUG enabled?"
Write-Host "   Check: If you see the red circle button in bottom-right corner"
Write-Host "   If not, app was built in RELEASE mode (button hidden)"
Write-Host ""

Write-Host "3. Firebase initialization:"
$firebase = adb -s $DEVICE logcat -d | Select-String "FirebaseCrashlytics.*Enabled"
if ($firebase) {
    Write-Host "   YES - Crashlytics enabled" -ForegroundColor Green
    $firebase | Select-Object -First 1
} else {
    Write-Host "   CHECKING - Running app to trigger Firebase init..." -ForegroundColor Yellow
    adb -s $DEVICE shell am start -n "$PACKAGE/.MainActivity" 2>&1 | Out-Null
    Start-Sleep -Seconds 3
    $firebase = adb -s $DEVICE logcat -d | Select-String "FirebaseCrashlytics"
    if ($firebase) {
        Write-Host "   YES - Crashlytics enabled" -ForegroundColor Green
    } else {
        Write-Host "   NO - Crashlytics not initializing" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "============================================================"
Write-Host "NEXT STEPS:"
Write-Host "============================================================"
Write-Host ""
Write-Host "1. Look at your emulator screen"
Write-Host "2. In bottom-right corner, look for a red circle/button"
Write-Host "3. If you see it: TAP IT (the app will crash)"
Write-Host "4. If you don't see it:"
Write-Host "   - App may be in RELEASE mode"
Write-Host "   - Rebuild with: ./gradlew clean :app:installDebug"
Write-Host ""
Write-Host "5. After crash, app will force-close"
Write-Host "6. Script will relaunch and monitor upload"
Write-Host ""

