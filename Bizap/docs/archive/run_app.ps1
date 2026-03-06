#!/usr/bin/env pwsh
# Bizap App Runner Script - Week 4

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BIZAP APP RUNNER - WEEK 4" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set working directory
$bizapDir = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
Set-Location $bizapDir
Write-Host "Working directory: $(Get-Location)" -ForegroundColor Green

Write-Host ""
Write-Host "Step 1: Building the app..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# Build the app
$buildStart = Get-Date
./gradlew clean :app:assembleDebug

$buildEnd = Get-Date
$buildTime = ($buildEnd - $buildStart).TotalSeconds
Write-Host ""
Write-Host "Build completed in $buildTime seconds" -ForegroundColor Green

# Check if APK was created
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "✅ APK created: $apkSize MB" -ForegroundColor Green
} else {
    Write-Host "❌ APK not found!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 2: Checking for device..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# Check for connected devices
$devices = adb devices | Select-Object -Skip 1 | Where-Object {$_ -match "device$"}

if ($devices.Count -eq 0) {
    Write-Host "❌ No devices/emulators found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "To fix:" -ForegroundColor Yellow
    Write-Host "1. Start an Android emulator from Android Studio"
    Write-Host "2. Or connect a physical device with USB debugging"
    Write-Host ""
    Write-Host "Once ready, run: adb install -r `"$apkPath`"" -ForegroundColor Cyan
    exit 1
}

Write-Host "✅ Found device(s):" -ForegroundColor Green
$devices | ForEach-Object {
    $parts = $_ -split '\s+'
    Write-Host "   - $($parts[0])" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 3: Installing app..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

adb install -r $apkPath

Write-Host ""
Write-Host "Step 4: Launching app..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

adb shell am start -n com.emul8r.bizap/.MainActivity

Write-Host ""
Write-Host "✅ App launched!" -ForegroundColor Green
Write-Host ""
Write-Host "Checking for crashes (waiting 5 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Crash check:" -ForegroundColor Yellow
adb logcat -d -s AndroidRuntime:E | Select-Object -First 20

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "APP RUNNER COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "What to test:" -ForegroundColor Yellow
Write-Host "  1. App launches without crash" -ForegroundColor White
Write-Host "  2. Navigate to Invoices → Create Invoice" -ForegroundColor White
Write-Host "  3. Add line items with quantities/prices" -ForegroundColor White
Write-Host "  4. Verify currency displays correctly" -ForegroundColor White
Write-Host "  5. Save invoice" -ForegroundColor White
Write-Host "  6. Navigate Settings → Business Profile" -ForegroundColor White
Write-Host ""

