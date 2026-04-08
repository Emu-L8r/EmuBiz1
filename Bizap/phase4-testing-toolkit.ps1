#!/usr/bin/env powershell
# PHASE 4 TESTING TOOLKIT - PowerShell Helper Scripts
# Usage: . .\phase4-testing-toolkit.ps1

Write-Host "🧪 BIZAP PHASE 4 TESTING TOOLKIT LOADED" -ForegroundColor Green
Write-Host ""

# ============================================================================
# SECTION 1: Device Management Functions
# ============================================================================

function Get-ConnectedDevices {
    <#
    .SYNOPSIS
    List all connected Android devices via ADB
    .EXAMPLE
    Get-ConnectedDevices
    #>
    Write-Host "📱 Checking connected devices..." -ForegroundColor Cyan
    $devices = adb devices | Select-Object -Skip 1 | Where-Object {$_ -match "device$" -or $_ -match "emulator"}

    if ($devices.Count -eq 0) {
        Write-Host "❌ No devices connected" -ForegroundColor Red
        return $null
    }

    Write-Host "✅ Found $($devices.Count) device(s):" -ForegroundColor Green
    $devices | ForEach-Object { Write-Host "   $_" }
    return $devices
}

function Get-DeviceInfo {
    <#
    .SYNOPSIS
    Get detailed device information
    .EXAMPLE
    Get-DeviceInfo
    #>
    Write-Host "📋 Device Information:" -ForegroundColor Cyan

    $brand = adb shell getprop ro.product.brand
    $model = adb shell getprop ro.product.model
    $android = adb shell getprop ro.build.version.release
    $sdk = adb shell getprop ro.build.version.sdk
    $memory = adb shell cat /proc/meminfo | Select-String "MemTotal" | ForEach-Object {$_.Line}

    Write-Host "  Brand: $brand"
    Write-Host "  Model: $model"
    Write-Host "  Android: $android (API $sdk)"
    Write-Host "  Memory: $memory"
}

# ============================================================================
# SECTION 2: APK Installation Functions
# ============================================================================

function Install-DebugAPK {
    <#
    .SYNOPSIS
    Install debug APK on connected device
    .EXAMPLE
    Install-DebugAPK
    #>
    Write-Host "📦 Installing debug APK..." -ForegroundColor Cyan

    $apkPath = "app/build/outputs/apk/debug/app-debug.apk"

    if (-not (Test-Path $apkPath)) {
        Write-Host "❌ APK not found. Building..." -ForegroundColor Yellow
        ./gradlew clean assembleDebug --no-daemon -q
    }

    adb install -r $apkPath

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ APK installed successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ APK installation failed" -ForegroundColor Red
    }
}

function Uninstall-App {
    <#
    .SYNOPSIS
    Uninstall Bizap from device
    .EXAMPLE
    Uninstall-App
    #>
    Write-Host "🗑️  Uninstalling Bizap..." -ForegroundColor Cyan
    adb uninstall com.emul8r.bizap
    Write-Host "✅ App uninstalled" -ForegroundColor Green
}

# ============================================================================
# SECTION 3: Testing & Monitoring Functions
# ============================================================================

function Start-App {
    <#
    .SYNOPSIS
    Launch Bizap on connected device
    .EXAMPLE
    Start-App
    #>
    Write-Host "🚀 Launching Bizap..." -ForegroundColor Cyan
    adb shell am start -n com.emul8r.bizap/.MainActivity
    Write-Host "✅ App launched" -ForegroundColor Green
}

function Stop-App {
    <#
    .SYNOPSIS
    Force stop Bizap
    .EXAMPLE
    Stop-App
    #>
    Write-Host "⏹️  Stopping Bizap..." -ForegroundColor Cyan
    adb shell am force-stop com.emul8r.bizap
    Write-Host "✅ App stopped" -ForegroundColor Green
}

function Watch-Logs {
    <#
    .SYNOPSIS
    Watch Bizap logcat in real-time
    .EXAMPLE
    Watch-Logs
    #>
    Write-Host "📝 Watching logs (press Ctrl+C to stop)..." -ForegroundColor Cyan
    adb logcat --clear
    adb logcat | Select-String "bizap|Error|Exception|Crash"
}

function Get-CrashLog {
    <#
    .SYNOPSIS
    Get last crash log from device
    .EXAMPLE
    Get-CrashLog
    #>
    Write-Host "🐛 Retrieving crash log..." -ForegroundColor Cyan
    adb logcat -d | Select-String "FATAL|ANR|Exception|Crash" | Select-Object -Last 50
}

# ============================================================================
# SECTION 4: Offline Testing Functions
# ============================================================================

function Disable-Network {
    <#
    .SYNOPSIS
    Disable WiFi and cellular on device (airplane mode)
    .EXAMPLE
    Disable-Network
    #>
    Write-Host "📡 Disabling network..." -ForegroundColor Cyan
    adb shell settings put global airplane_mode_on 1
    adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true
    Write-Host "✅ Network disabled (Airplane Mode ON)" -ForegroundColor Green
}

function Enable-Network {
    <#
    .SYNOPSIS
    Enable WiFi and cellular on device
    .EXAMPLE
    Enable-Network
    #>
    Write-Host "📡 Enabling network..." -ForegroundColor Cyan
    adb shell settings put global airplane_mode_on 0
    adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
    Write-Host "✅ Network enabled (Airplane Mode OFF)" -ForegroundColor Green
}

function Get-NetworkStatus {
    <#
    .SYNOPSIS
    Check current network status
    .EXAMPLE
    Get-NetworkStatus
    #>
    Write-Host "📡 Network Status:" -ForegroundColor Cyan
    $airplaneMode = adb shell settings get global airplane_mode_on

    if ($airplaneMode -eq "1") {
        Write-Host "  ✈️  Airplane Mode: ON (Offline)" -ForegroundColor Red
    } else {
        Write-Host "  ✈️  Airplane Mode: OFF (Online)" -ForegroundColor Green
    }
}

# ============================================================================
# SECTION 5: Performance Profiling Functions
# ============================================================================

function Measure-AppStartup {
    <#
    .SYNOPSIS
    Measure app startup time
    .EXAMPLE
    Measure-AppStartup
    #>
    Write-Host "⏱️  Measuring app startup time..." -ForegroundColor Cyan
    Stop-App
    Start-Sleep -Seconds 2
    adb shell am start -W com.emul8r.bizap/.MainActivity
    Write-Host "⏱️  (Check output for ThisTime/TotalTime)" -ForegroundColor Yellow
}

function Monitor-Memory {
    <#
    .SYNOPSIS
    Monitor memory usage in real-time
    .EXAMPLE
    Monitor-Memory
    #>
    Write-Host "💾 Memory Usage (press Ctrl+C to stop)..." -ForegroundColor Cyan

    $counter = 0
    while ($true) {
        $mem = adb shell dumpsys meminfo com.emul8r.bizap | Select-String "TOTAL" | Select-Object -First 1
        $timestamp = Get-Date -Format "HH:mm:ss"
        Write-Host "$timestamp - $mem"
        Start-Sleep -Seconds 2
        $counter++
        if ($counter -gt 30) { break }
    }
}

function Get-BatteryStatus {
    <#
    .SYNOPSIS
    Get battery level and temperature
    .EXAMPLE
    Get-BatteryStatus
    #>
    Write-Host "🔋 Battery Status:" -ForegroundColor Cyan
    $dumpsys = adb shell dumpsys batterymanager

    $level = $dumpsys | Select-String "level:" | ForEach-Object {$_.Line.Split(":")[1].Trim()}
    $temp = $dumpsys | Select-String "temperature:" | ForEach-Object {$_.Line.Split(":")[1].Trim()}
    $health = $dumpsys | Select-String "health:" | ForEach-Object {$_.Line.Split(":")[1].Trim()}

    Write-Host "  Level: $level%"
    Write-Host "  Temperature: $temp°C"
    Write-Host "  Health: $health"
}

# ============================================================================
# SECTION 6: Database & Data Functions
# ============================================================================

function Clear-AppData {
    <#
    .SYNOPSIS
    Clear all app data and cache
    .EXAMPLE
    Clear-AppData
    #>
    Write-Host "🗑️  Clearing app data..." -ForegroundColor Cyan
    Stop-App
    adb shell pm clear com.emul8r.bizap
    Write-Host "✅ App data cleared" -ForegroundColor Green
}

function Backup-Database {
    <#
    .SYNOPSIS
    Backup app database to computer
    .EXAMPLE
    Backup-Database
    #>
    Write-Host "💾 Backing up database..." -ForegroundColor Cyan

    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupDir = "device_backups"

    if (-not (Test-Path $backupDir)) { mkdir $backupDir }

    adb pull "/data/data/com.emul8r.bizap/databases/bizap.db" "$backupDir/bizap_$timestamp.db"
    Write-Host "✅ Database backed up to $backupDir/bizap_$timestamp.db" -ForegroundColor Green
}

# ============================================================================
# SECTION 7: Testing Scenarios
# ============================================================================

function Test-OfflineInvoiceCreation {
    <#
    .SYNOPSIS
    Automated test: Create invoice while offline
    .EXAMPLE
    Test-OfflineInvoiceCreation
    #>
    Write-Host "🧪 TEST: Offline Invoice Creation" -ForegroundColor Cyan
    Write-Host "1. Disabling network..." -ForegroundColor Yellow
    Disable-Network
    Start-Sleep -Seconds 2

    Write-Host "2. Starting app..." -ForegroundColor Yellow
    Start-App
    Start-Sleep -Seconds 3

    Write-Host "3. Monitoring logs for 60 seconds..." -ForegroundColor Yellow
    Write-Host "   (Manually: Create invoice, watch for success)" -ForegroundColor Gray
    adb logcat --clear
    adb logcat | Select-String "Invoice" | Select-Object -First 20

    Write-Host "4. Enabling network..." -ForegroundColor Yellow
    Enable-Network

    Write-Host "✅ Test complete. Check logs above." -ForegroundColor Green
}

function Test-SyncOnReconnect {
    <#
    .SYNOPSIS
    Automated test: Sync when reconnecting to internet
    .EXAMPLE
    Test-SyncOnReconnect
    #>
    Write-Host "🧪 TEST: Sync on Reconnect" -ForegroundColor Cyan

    Write-Host "1. Checking network status..." -ForegroundColor Yellow
    Get-NetworkStatus

    Write-Host "2. Watching logs for sync activity..." -ForegroundColor Yellow
    Write-Host "   (Watching for 'SyncManager' or 'OfflineQueue')" -ForegroundColor Gray
    adb logcat --clear

    $startTime = Get-Date
    adb logcat | Select-String "Sync|Queue|Upload" | ForEach-Object {
        Write-Host $_.Line
    } | Select-Object -First 50

    Write-Host "✅ Sync test complete" -ForegroundColor Green
}

# ============================================================================
# SECTION 8: Build Functions
# ============================================================================

function Build-DebugAPK {
    <#
    .SYNOPSIS
    Build debug APK
    .EXAMPLE
    Build-DebugAPK
    #>
    Write-Host "🔨 Building debug APK..." -ForegroundColor Cyan
    ./gradlew clean assembleDebug --no-daemon

    if ($LASTEXITCODE -eq 0) {
        $size = (Get-Item "app/build/outputs/apk/debug/app-debug.apk").Length / 1MB
        Write-Host "✅ Build successful - APK size: $([math]::Round($size, 2)) MB" -ForegroundColor Green
    }
}

function Run-AllTests {
    <#
    .SYNOPSIS
    Run all unit tests locally
    .EXAMPLE
    Run-AllTests
    #>
    Write-Host "🧪 Running all unit tests..." -ForegroundColor Cyan
    ./gradlew testDebugUnitTest --no-daemon

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ All tests passed" -ForegroundColor Green
    } else {
        Write-Host "❌ Some tests failed" -ForegroundColor Red
    }
}

# ============================================================================
# SECTION 9: Menu & Help
# ============================================================================

function Show-Menu {
    Write-Host ""
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║       BIZAP PHASE 4 TESTING TOOLKIT - COMMAND MENU             ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📱 DEVICE MANAGEMENT:" -ForegroundColor Green
    Write-Host "  Get-ConnectedDevices      - List connected devices"
    Write-Host "  Get-DeviceInfo           - Show device specifications"
    Write-Host ""
    Write-Host "📦 APK MANAGEMENT:" -ForegroundColor Green
    Write-Host "  Build-DebugAPK           - Build debug APK"
    Write-Host "  Install-DebugAPK         - Install APK on device"
    Write-Host "  Uninstall-App            - Uninstall app from device"
    Write-Host ""
    Write-Host "🚀 APP CONTROL:" -ForegroundColor Green
    Write-Host "  Start-App                - Launch Bizap"
    Write-Host "  Stop-App                 - Force stop app"
    Write-Host "  Watch-Logs               - View real-time logs"
    Write-Host "  Get-CrashLog             - Get crash details"
    Write-Host ""
    Write-Host "📡 OFFLINE TESTING:" -ForegroundColor Green
    Write-Host "  Disable-Network          - Turn off WiFi (Airplane mode)"
    Write-Host "  Enable-Network           - Turn on WiFi"
    Write-Host "  Get-NetworkStatus        - Check current network state"
    Write-Host "  Test-OfflineInvoiceCreation - Run offline test"
    Write-Host "  Test-SyncOnReconnect     - Run sync test"
    Write-Host ""
    Write-Host "💾 PERFORMANCE MONITORING:" -ForegroundColor Green
    Write-Host "  Measure-AppStartup       - Test app launch time"
    Write-Host "  Monitor-Memory           - Watch memory usage"
    Write-Host "  Get-BatteryStatus        - Check battery"
    Write-Host ""
    Write-Host "🔧 DATA MANAGEMENT:" -ForegroundColor Green
    Write-Host "  Clear-AppData            - Wipe all user data"
    Write-Host "  Backup-Database          - Save database to computer"
    Write-Host ""
    Write-Host "🧪 TESTING:" -ForegroundColor Green
    Write-Host "  Run-AllTests             - Execute local unit tests"
    Write-Host ""
}

# Display menu on load
Show-Menu

Write-Host ""
Write-Host "✅ Toolkit ready! Type commands above or 'Show-Menu' for help." -ForegroundColor Green
Write-Host ""

