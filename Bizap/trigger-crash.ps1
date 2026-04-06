#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - CRASH TRIGGER
# Launches app and waits for you to tap the red button

param(
    [string]$Device = "emulator-5554"
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║  FIREBASE CRASHLYTICS - TRIGGER TEST CRASH            ║" -ForegroundColor Magenta
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Magenta

$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"

Write-Host "Device: $Device`n" -ForegroundColor Cyan

# Verify device
Write-Host "Verifying device connection..." -ForegroundColor Yellow
$test = adb -s $Device shell echo "OK" 2>&1
if ($test -ne "OK") {
    Write-Host "❌ Device '$Device' not found or offline" -ForegroundColor Red
    Write-Host "`nRun 'detect-devices.ps1' to find your device serial`n"
    exit 1
}
Write-Host "✅ Device connected`n" -ForegroundColor Green

# Launch app
Write-Host "Launching $PACKAGE..." -ForegroundColor Yellow
adb -s $Device shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
Write-Host "✅ App launching (waiting 5 seconds for it to load...)`n" -ForegroundColor Green

Start-Sleep -Seconds 5

# Instructions
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  🔴 APP IS READY - TAP THE RED BUTTON!               ║" -ForegroundColor Green
Write-Host "╠════════════════════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "║  Instructions:                                         ║" -ForegroundColor Green
Write-Host "║  1. Look at your device/emulator screen               ║" -ForegroundColor Green
Write-Host "║  2. Find the RED CIRCLE button (bottom-right corner)  ║" -ForegroundColor Green
Write-Host "║  3. TAP IT                                            ║" -ForegroundColor Green
Write-Host "║  4. App will crash with RuntimeException              ║" -ForegroundColor Green
Write-Host "║  5. Press ENTER when you see the app force-close      ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Green

Read-Host "Press ENTER after app has crashed"

Write-Host "`n✅ Crash triggered successfully!" -ForegroundColor Green
Write-Host "   Now run 'relaunch-app.ps1' to upload the crash report`n"

