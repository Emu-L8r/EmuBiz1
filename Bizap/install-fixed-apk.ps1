#!/usr/bin/env pwsh
# Install Script for Fixed APK
# Finds ADB automatically or guides you to use Android Studio

Write-Host "🔄 INSTALLING FIXED APK" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan

# Search for ADB in common locations
$adbPaths = @(
    "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    "C:\Android\Sdk\platform-tools\adb.exe",
    "C:\Program Files\Android\Sdk\platform-tools\adb.exe",
    "C:\Program Files (x86)\Android\Sdk\platform-tools\adb.exe"
)

$adbPath = $null
foreach ($path in $adbPaths) {
    if (Test-Path $path) {
        $adbPath = $path
        Write-Host "✅ Found ADB at: $adbPath" -ForegroundColor Green
        break
    }
}

if ($null -eq $adbPath) {
    Write-Host ""
    Write-Host "❌ ADB NOT FOUND" -ForegroundColor Red
    Write-Host ""
    Write-Host "SOLUTION: Use Android Studio to install (Easiest!)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Steps:" -ForegroundColor Cyan
    Write-Host "1. Open Android Studio"
    Write-Host "2. File → Open"
    Write-Host "3. Select: C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
    Write-Host "4. Connect your device/emulator"
    Write-Host "5. Click the Run button (or press Shift+F10)"
    Write-Host "6. Select your device"
    Write-Host "7. Android Studio will build, install, and launch!"
    Write-Host ""
    Write-Host "OR install ADB:" -ForegroundColor Cyan
    Write-Host "1. Open Android Studio"
    Write-Host "2. Tools → SDK Manager"
    Write-Host "3. Check 'Android SDK Platform-Tools'"
    Write-Host "4. Click Apply"
    Write-Host "5. Restart PowerShell"
    Write-Host "6. Run this script again"
    exit 1
}

# Uninstall old version
Write-Host ""
Write-Host "1️⃣  Uninstalling old version..." -ForegroundColor Yellow
& $adbPath uninstall com.emul8r.bizap
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  App not installed (first time?). Continuing..." -ForegroundColor Yellow
}

# Wait a moment
Start-Sleep -Seconds 1

# Install new version
Write-Host "2️⃣  Installing new fixed APK..." -ForegroundColor Yellow
& $adbPath install -r "app\build\outputs\apk\debug\app-debug.apk"
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Installation successful!" -ForegroundColor Green
} else {
    Write-Host "❌ Installation failed!" -ForegroundColor Red
    exit 1
}

# Wait a moment
Start-Sleep -Seconds 2

# Launch app
Write-Host "3️⃣  Launching app..." -ForegroundColor Yellow
& $adbPath shell am start -n "com.emul8r.bizap/.MainActivity"

Write-Host ""
Write-Host "✅ App should launch now!" -ForegroundColor Green
Write-Host "If it crashes, check: CRASH_DIAGNOSTIC.md" -ForegroundColor Cyan


