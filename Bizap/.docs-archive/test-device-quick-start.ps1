# Device Testing Quick Start
# Run this script to build and install APK on connected device
# Bizap v0.9.3 - April 28, 2026

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BIZAP DEVICE TESTING QUICK START" -ForegroundColor Cyan
Write-Host "  April 28, 2026" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check device connected
Write-Host "[1/5] Checking device connection..." -ForegroundColor Yellow
$devices = adb devices
Write-Host $devices
$deviceCount = ($devices | Measure-Object -Line).Lines - 2
if ($deviceCount -lt 1) {
    Write-Host "❌ ERROR: No device connected!" -ForegroundColor Red
    Write-Host "   Please connect a device via USB and enable USB debugging" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Device connected!" -ForegroundColor Green
Write-Host ""

# Step 2: Clean build
Write-Host "[2/5] Building APK (this takes 2-3 minutes)..." -ForegroundColor Yellow
./gradlew clean assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ APK built successfully!" -ForegroundColor Green
Write-Host ""

# Step 3: Check APK exists
Write-Host "[3/5] Verifying APK file..." -ForegroundColor Yellow
$apkPath = "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "✅ APK found: $apkPath ($([Math]::Round($apkSize, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "❌ APK not found at expected location!" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 4: Clear old app data
Write-Host "[4/5] Clearing previous app data..." -ForegroundColor Yellow
adb shell pm clear com.emul8r.bizap
Write-Host "✅ App data cleared" -ForegroundColor Green
Write-Host ""

# Step 5: Install APK
Write-Host "[5/5] Installing APK on device..." -ForegroundColor Yellow
adb install -r $apkPath
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Installation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ APK installed successfully!" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ DEVICE TESTING READY!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Tap app icon on device to launch Bizap" -ForegroundColor White
Write-Host "2. Select GUI3 (Matrix/Cyberpunk theme)" -ForegroundColor White
Write-Host "3. Follow testing checklist in:" -ForegroundColor White
Write-Host "   - DEVICE_TESTING_MANUAL_COMPREHENSIVE_GUIDE.md" -ForegroundColor White
Write-Host ""
Write-Host "To monitor logs in real-time, run in another terminal:" -ForegroundColor Cyan
Write-Host "  adb logcat -s ""BizapApp:V""" -ForegroundColor Yellow
Write-Host ""
Write-Host "To check device info:" -ForegroundColor Cyan
Write-Host "  adb shell getprop ro.build.model" -ForegroundColor Yellow
Write-Host "  adb shell getprop ro.build.version.release" -ForegroundColor Yellow
Write-Host ""

