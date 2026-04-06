#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - DIAGNOSTICS
# Tests connectivity, package installation, and Firebase setup

param(
    [string]$Device = "emulator-5554"
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  FIREBASE CRASHLYTICS - DIAGNOSTICS                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"
$PROJECT_ID = "bizap-801c0"

Write-Host "Device: $Device" -ForegroundColor Cyan
Write-Host "Package: $PACKAGE" -ForegroundColor Cyan
Write-Host "Project: $PROJECT_ID`n" -ForegroundColor Cyan

# Test 1: Device connection
Write-Host "[TEST 1/6] Device Connection" -ForegroundColor Yellow
$test = adb -s $Device shell echo "OK" 2>&1
if ($test -eq "OK") {
    Write-Host "✅ Device is connected`n" -ForegroundColor Green
} else {
    Write-Host "❌ Device '$Device' not found or offline`n" -ForegroundColor Red
    exit 1
}

# Test 2: Package installation
Write-Host "[TEST 2/6] Package Installation" -ForegroundColor Yellow
$packages = adb -s $Device shell pm list packages 2>&1
$bizap = $packages | Select-String $PACKAGE
if ($bizap) {
    Write-Host "✅ Package installed: $bizap`n" -ForegroundColor Green
} else {
    Write-Host "❌ Package NOT installed" -ForegroundColor Red
    Write-Host "   Run: ./gradlew clean :app:installDebug`n" -ForegroundColor Yellow
}

# Test 3: Network connectivity
Write-Host "[TEST 3/6] Network Connectivity (ping 8.8.8.8)" -ForegroundColor Yellow
$ping = adb -s $Device shell ping -c 4 8.8.8.8 2>&1 | Out-String
if ($ping -match "icmp_seq") {
    Write-Host "✅ Device has internet access`n" -ForegroundColor Green
} else {
    Write-Host "❌ Device CANNOT reach 8.8.8.8" -ForegroundColor Red
    Write-Host "   Emulator network may not be configured`n" -ForegroundColor Yellow
}

# Test 4: Firebase initialization
Write-Host "[TEST 4/6] Firebase Initialization" -ForegroundColor Yellow
adb -s $Device logcat -c 2>&1 | Out-Null
adb -s $Device shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
Start-Sleep -Seconds 6

$logs = adb -s $Device logcat -d 2>&1
$firebase = $logs | Select-String "FirebaseCrashlytics|FirebaseApp" | Select-Object -First 10

if ($firebase) {
    Write-Host "✅ Firebase initializing:" -ForegroundColor Green
    $firebase | ForEach-Object { Write-Host "   $_" -ForegroundColor Cyan }
    Write-Host ""
} else {
    Write-Host "❌ Firebase not initializing" -ForegroundColor Red
    Write-Host "   Check google-services.json and build configuration`n" -ForegroundColor Yellow
}

# Test 5: Critical errors
Write-Host "[TEST 5/6] Critical Errors" -ForegroundColor Yellow
$errors = $logs | Select-String "Error|Exception|Failed|denied" | Select-Object -First 5
if ($errors) {
    Write-Host "⚠️  Found errors:" -ForegroundColor Yellow
    $errors | ForEach-Object { Write-Host "   $_" -ForegroundColor Red }
    Write-Host ""
} else {
    Write-Host "✅ No critical errors detected`n" -ForegroundColor Green
}

# Test 6: Google Services Configuration
Write-Host "[TEST 6/6] Google Services Configuration" -ForegroundColor Yellow
$gsPath = "$PSScriptRoot\app\google-services.json"
if (Test-Path $gsPath) {
    Write-Host "✅ google-services.json found at: $gsPath" -ForegroundColor Green
    $content = Get-Content $gsPath | ConvertFrom-Json
    Write-Host "   Project ID: $($content.project_info.project_id)" -ForegroundColor Cyan
    Write-Host "   Package: $($content.client[0].client_info.android_client_info.package_name)" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "❌ google-services.json NOT found" -ForegroundColor Red
    Write-Host "   Expected: app/google-services.json`n" -ForegroundColor Yellow
}

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Diagnostics Complete                                  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Next steps:" -ForegroundColor Green
Write-Host "1. Fix any ❌ failures shown above" -ForegroundColor Green
Write-Host "2. Run trigger-crash.ps1 to test" -ForegroundColor Green
Write-Host ""

