#!/usr/bin/env pwsh
# Install and launch Bizap v0.1.0-stabilized

$projectDir = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$sdkPath = "C:\Users\Saucey\AppData\Local\Android\Sdk"
$adbPath = "$sdkPath\platform-tools\adb.exe"
$apkPath = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
$packageName = "com.emul8r.bizap"
$activity = "$packageName.MainActivity"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Installing and Launching Bizap v0.1.0-stabilized" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check APK exists
Write-Host "Step 1: Verifying APK..." -ForegroundColor Yellow
if (Test-Path $apkPath) {
    $size = [Math]::Round((Get-Item $apkPath).Length / 1MB, 2)
    Write-Host "✓ APK found: $apkPath ($size MB)" -ForegroundColor Green
} else {
    Write-Host "✗ APK not found at $apkPath" -ForegroundColor Red
    exit 1
}

# Step 2: Check devices
Write-Host ""
Write-Host "Step 2: Checking connected devices..." -ForegroundColor Yellow
& $adbPath devices
$devices = & $adbPath devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" }
if ($devices.Count -eq 0) {
    Write-Host "✗ No connected devices found. Start an emulator or connect a device." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Device(s) found" -ForegroundColor Green

# Step 3: Install APK
Write-Host ""
Write-Host "Step 3: Installing APK..." -ForegroundColor Yellow
& $adbPath install -r $apkPath
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ APK installed successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Installation failed" -ForegroundColor Red
    exit 1
}

# Step 4: Launch app
Write-Host ""
Write-Host "Step 4: Launching app..." -ForegroundColor Yellow
& $adbPath shell am start -n "$activity"
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ App launched!" -ForegroundColor Green
    Write-Host ""
    Write-Host "The app should appear on your device/emulator shortly." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Next: Run the manual review checklist:" -ForegroundColor Cyan
    Write-Host "  1. Check currency display (should show with $ symbol, not 100x conversion)" -ForegroundColor Gray
    Write-Host "  2. Verify business profile edits update immediately" -ForegroundColor Gray
    Write-Host "  3. Test payment progress bar" -ForegroundColor Gray
    Write-Host "  4. Check document vault functionality" -ForegroundColor Gray
    Write-Host "  5. Test all navigation (Dashboard, Customers, Invoices, Vault, Settings)" -ForegroundColor Gray
} else {
    Write-Host "✗ Failed to launch app" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host "Installation Complete!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green

