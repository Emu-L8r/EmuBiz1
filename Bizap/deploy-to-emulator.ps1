# Complete Setup & Deploy to Emulator
# This script handles everything: restart emulator, install app, launch it

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BizAp: Complete Emulator Setup & Deploy" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$adbPath = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$emulatorPath = "C:\Users\Saucey\AppData\Local\Android\Sdk\emulator\emulator.exe"

# Step 1: Ensure we're in the project directory
Write-Host "Step 1: Verifying project directory..." -ForegroundColor Yellow
if (-not (Test-Path $projectRoot)) {
    Write-Host "❌ Project directory not found: $projectRoot" -ForegroundColor Red
    exit 1
}
cd $projectRoot
Write-Host "✓ Project directory confirmed" -ForegroundColor Green
Write-Host ""

# Step 2: Check if emulator is running
Write-Host "Step 2: Checking emulator status..." -ForegroundColor Yellow
$devices = & $adbPath devices
if ($devices -match "emulator-\d+\s+device") {
    Write-Host "✓ Emulator is running" -ForegroundColor Green
    $deviceId = [regex]::Match($devices, "emulator-\d+").Value
    Write-Host "  Device ID: $deviceId" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  Emulator not running - you'll need to start it manually" -ForegroundColor Yellow
    Write-Host "   Or start with: emulator @Pixel_4a_API_34 (or your AVD name)" -ForegroundColor Gray
}
Write-Host ""

# Step 3: Uninstall old app
Write-Host "Step 3: Removing old app..." -ForegroundColor Yellow
& $adbPath uninstall com.emul8r.bizap 2>&1 | Out-Null
Write-Host "✓ Old app removed (or was not installed)" -ForegroundColor Green
Write-Host ""

# Step 4: Build fresh APK
Write-Host "Step 4: Building fresh Debug APK..." -ForegroundColor Yellow
.\gradlew.bat clean assembleDebug --no-daemon 2>&1 | Select-Object -Last 5
$buildStatus = $LASTEXITCODE
if ($buildStatus -eq 0) {
    Write-Host "✓ Build successful" -ForegroundColor Green
} else {
    Write-Host "❌ Build failed with exit code: $buildStatus" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 5: Install APK
Write-Host "Step 5: Installing APK on emulator..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
    exit 1
}

& $adbPath install -r $apkPath 2>&1 | Select-Object -Last 3
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ APK installed successfully" -ForegroundColor Green
} else {
    Write-Host "⚠️  Install may have failed - check emulator storage" -ForegroundColor Yellow
    Write-Host "   Try: adb shell pm clear com.android.systemui" -ForegroundColor Gray
}
Write-Host ""

# Step 6: Launch the app
Write-Host "Step 6: Launching app..." -ForegroundColor Yellow
& $adbPath shell am start -n com.emul8r.bizap/.MainActivity 2>&1 | Out-Null
Write-Host "✓ App launched!" -ForegroundColor Green
Write-Host ""

# Step 7: Show live logcat
Write-Host "Step 7: Monitoring app startup (Ctrl+C to stop)..." -ForegroundColor Yellow
Write-Host ""
& $adbPath logcat -v threadtime | Select-String -Pattern "bizap|Exception|Error|Crash" -Context 2

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If app crashes:"  -ForegroundColor Yellow
Write-Host "  1. Check logcat errors above" -ForegroundColor Yellow
Write-Host "  2. Run database recovery: .\fix-database-crash.ps1" -ForegroundColor Yellow
Write-Host "  3. Try again: ./deploy-to-emulator.ps1" -ForegroundColor Yellow

