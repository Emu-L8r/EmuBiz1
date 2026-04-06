#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - APP RELAUNCH
# Relaunches app to trigger crash report upload to Firebase

param(
    [string]$Device = "emulator-5554"
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║  FIREBASE CRASHLYTICS - APP RELAUNCH                  ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Yellow

$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"

Write-Host "Device: $Device`n" -ForegroundColor Cyan

# Verify device
Write-Host "Verifying device connection..." -ForegroundColor Yellow
$test = adb -s $Device shell echo "OK" 2>&1
if ($test -ne "OK") {
    Write-Host "❌ Device '$Device' not found or offline" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Device connected`n" -ForegroundColor Green

# Relaunch
Write-Host "Relaunching $PACKAGE..." -ForegroundColor Yellow
adb -s $Device shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
Start-Sleep -Seconds 2

Write-Host "✅ App relaunched`n" -ForegroundColor Green

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║  Upload should occur within 15-30 seconds             ║" -ForegroundColor Yellow
Write-Host "║                                                        ║" -ForegroundColor Yellow
Write-Host "║  Expected Logcat:                                      ║" -ForegroundColor Yellow
Write-Host "║  D/FirebaseCrashlytics: Uploading crash report...     ║" -ForegroundColor Yellow
Write-Host "║  D/FirebaseCrashlytics: Completed report upload       ║" -ForegroundColor Yellow
Write-Host "║                                                        ║" -ForegroundColor Yellow
Write-Host "║  To see it, run: monitor-logcat.ps1 in another window ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Yellow

Write-Host "✅ Relaunch complete. Check Firebase Console in 5-10 minutes." -ForegroundColor Green
Write-Host "   URL: https://console.firebase.google.com/project/bizap-801c0/crashlytics`n"

