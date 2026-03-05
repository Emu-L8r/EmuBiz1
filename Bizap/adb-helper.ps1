#!/usr/bin/env pwsh
# Bizap ADB Helper Script
# This script helps you install and run the APK without manually setting up ADB PATH

param(
    [Parameter(Position=0)]
    [ValidateSet("devices", "install", "launch", "help")]
    [string]$Command = "help"
)

# Common ADB locations
$adbSearchPaths = @(
    "C:\Android\Sdk\platform-tools\adb.exe",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    "C:\Program Files\Android\Sdk\platform-tools\adb.exe",
    "C:\Program Files (x86)\Android\Sdk\platform-tools\adb.exe",
    "$env:ANDROID_HOME\platform-tools\adb.exe",
    "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe"
)

# Find ADB
$adbPath = $null
foreach ($path in $adbSearchPaths) {
    if (Test-Path $path) {
        $adbPath = $path
        Write-Host "✅ Found ADB at: $adbPath" -ForegroundColor Green
        break
    }
}

if ($null -eq $adbPath) {
    Write-Host "❌ ADB not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Android SDK:" -ForegroundColor Yellow
    Write-Host "1. Open Android Studio"
    Write-Host "2. Go to Tools > SDK Manager"
    Write-Host "3. Install 'Android SDK Platform-Tools'"
    Write-Host "4. Restart PowerShell"
    exit 1
}

# Execute command
switch ($Command) {
    "devices" {
        Write-Host "🔍 Checking connected devices..." -ForegroundColor Cyan
        & $adbPath devices
    }

    "install" {
        Write-Host "📦 Installing APK..." -ForegroundColor Cyan
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (-not (Test-Path $apkPath)) {
            Write-Host "❌ APK not found at: $apkPath" -ForegroundColor Red
            exit 1
        }
        & $adbPath install -r $apkPath
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ APK installed successfully!" -ForegroundColor Green
        }
    }

    "launch" {
        Write-Host "🚀 Launching app..." -ForegroundColor Cyan
        & $adbPath shell am start -n "com.emul8r.bizap/.MainActivity"
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ App launched!" -ForegroundColor Green
        }
    }

    "help" {
        Write-Host "Bizap ADB Helper Script" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Usage: .\adb-helper.ps1 [command]" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Commands:"
        Write-Host "  devices  - List connected devices"
        Write-Host "  install  - Install the debug APK"
        Write-Host "  launch   - Launch the Bizap app"
        Write-Host "  help     - Show this help message"
        Write-Host ""
        Write-Host "Examples:"
        Write-Host "  .\adb-helper.ps1 devices"
        Write-Host "  .\adb-helper.ps1 install"
        Write-Host "  .\adb-helper.ps1 launch"
    }
}

