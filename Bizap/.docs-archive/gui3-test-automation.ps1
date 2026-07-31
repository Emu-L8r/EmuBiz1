#!/usr/bin/env powershell
# GUI3 Debug Testing Automation Script
# Purpose: Build APK, install, and launch for testing
# Date: April 13, 2026
# Fixed: UTF-8 encoding issues resolved

param(
    [string]$DeviceId = "",
    [switch]$SkipBuild = $false,
    [switch]$SkipInstall = $false,
    [switch]$ViewLogs = $false
)

# Colors for output
$colors = @{
    Success = 'Green'
    Error = 'Red'
    Warning = 'Yellow'
    Info = 'Cyan'
    Step = 'Magenta'
}

function Write-Status {
    param([string]$Message, [string]$Type = 'Info')
    $color = $colors[$Type]
    Write-Host $Message -ForegroundColor $color
}

function Write-Header {
    param([string]$Title)
    Write-Host ""
    Write-Host "=================================================================" -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Cyan
    Write-Host "=================================================================" -ForegroundColor Cyan
    Write-Host ""
}

# Start
Write-Header "BIZAP GUI3 DEBUG TESTING AUTOMATION"

# Project configuration
$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$apkPath = "$projectRoot\app\build\outputs\apk\debug\app-debug.apk"
$packageName = "com.emul8r.bizap"
$mainActivity = "$packageName.MainActivity"

Write-Status "Project Root: $projectRoot" 'Info'
Write-Status "APK Target: $apkPath" 'Info'
Write-Status ""

# === STEP 1: BUILD APK ===
if (-not $SkipBuild) {
    Write-Header "STEP 1: BUILD DEBUG APK"

    Write-Status "[1/4] Navigating to project..." 'Step'
    Set-Location $projectRoot

    Write-Status "[2/4] Running: ./gradlew clean assembleDebug" 'Step'
    $buildStartTime = Get-Date

    & ./gradlew clean assembleDebug 2>&1 | Tee-Object -Variable buildOutput | Out-Host

    $buildEndTime = Get-Date
    $buildDuration = [math]::Round(($buildEndTime - $buildStartTime).TotalSeconds, 1)

    if ($LASTEXITCODE -eq 0) {
        Write-Status "[OK] BUILD SUCCESSFUL - ${buildDuration}s" 'Success'
    } else {
        Write-Status "[FAIL] BUILD FAILED" 'Error'
        Write-Status "Run with details: ./gradlew clean assembleDebug --stacktrace" 'Error'
        exit 1
    }

    # Verify APK exists
    Write-Status "[3/4] Verifying APK artifact..." 'Step'
    if (Test-Path $apkPath) {
        $apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 1)
        Write-Status "[OK] APK found: ${apkSize}MB" 'Success'
    } else {
        Write-Status "[FAIL] APK not found at $apkPath" 'Error'
        exit 1
    }

    Write-Status "[4/4] Waiting 5 seconds before install..." 'Step'
    Start-Sleep -Seconds 5
    Write-Status ""
}

# === STEP 2: CHECK DEVICE CONNECTION ===
Write-Header "STEP 2: CHECK DEVICE/EMULATOR CONNECTION"

Write-Status "[1/3] Listing connected devices..." 'Step'
$devices = & adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\S' }

if ($devices.Count -eq 0) {
    Write-Status "[FAIL] NO DEVICES FOUND" 'Error'
    Write-Status "   Please:"
    Write-Status "   1. Launch Android Emulator, OR"
    Write-Status "   2. Connect physical device with USB debugging enabled"
    exit 1
}

Write-Status "[OK] Devices found:" 'Success'
$devices | ForEach-Object { Write-Status "   $_" 'Info' }
Write-Host ""

# Select device
if ($DeviceId -eq "") {
    $deviceList = $devices -split '\s+' | Where-Object { $_ -ne '' }
    if ($deviceList.Count -eq 1) {
        $DeviceId = $deviceList[0]
        Write-Status "[OK] Using device: $DeviceId" 'Success'
    } else {
        Write-Status "[WARN] Multiple devices found. Using first: $($deviceList[0])" 'Warning'
        $DeviceId = $deviceList[0]
    }
}

Write-Status "[2/3] Checking device storage..." 'Step'
$storageCheck = & adb -s $DeviceId shell df /data | Select-Object -Last 1
Write-Status "   $storageCheck" 'Info'

Write-Status "[3/3] Device ready for installation [OK]" 'Success'
Write-Status ""

# === STEP 3: INSTALL APK ===
if (-not $SkipInstall) {
    Write-Header "STEP 3: INSTALL APP"

    Write-Status "[1/4] Checking for existing installation..." 'Step'
    $installed = & adb -s $DeviceId shell pm list packages | Select-String $packageName

    if ($installed) {
        Write-Status "[WARN] App already installed, uninstalling first..." 'Warning'
        & adb -s $DeviceId uninstall $packageName 2>&1 | Out-Null
        Start-Sleep -Seconds 2
        Write-Status "[OK] Old version removed" 'Success'
    }

    Write-Status "[2/4] Installing APK: $apkPath" 'Step'
    $installStartTime = Get-Date

    & adb -s $DeviceId install -r $apkPath 2>&1 | Tee-Object -Variable installOutput | Out-Host

    $installEndTime = Get-Date
    $installDuration = [math]::Round(($installEndTime - $installStartTime).TotalSeconds, 1)

    if ($installOutput -match "Success") {
        Write-Status "[OK] INSTALL SUCCESSFUL - ${installDuration}s" 'Success'
    } else {
        Write-Status "[FAIL] INSTALL FAILED" 'Error'
        Write-Status "Output: $installOutput" 'Error'
        exit 1
    }

    Write-Status "[3/4] Verifying installation..." 'Step'
    $verify = & adb -s $DeviceId shell pm list packages | Select-String $packageName
    if ($verify) {
        Write-Status "[OK] App verified on device" 'Success'
    } else {
        Write-Status "[FAIL] Installation verification failed" 'Error'
        exit 1
    }

    Write-Status "[4/4] Waiting 2 seconds before launch..." 'Step'
    Start-Sleep -Seconds 2
    Write-Status ""
}

# === STEP 4: LAUNCH APP ===
Write-Header "STEP 4: LAUNCH APP"

Write-Status "[1/3] Starting app: $mainActivity" 'Step'
& adb -s $DeviceId shell am start -n $mainActivity 2>&1 | Out-Null

Write-Status "[OK] Launch command sent" 'Success'
Write-Status "[WAIT] App should launch in 2-3 seconds..." 'Info'

Start-Sleep -Seconds 3

Write-Status "[2/3] Verifying app is running..." 'Step'
$running = & adb -s $DeviceId shell pidof $packageName
if ($running) {
    Write-Status "[OK] App is running (PID: $running)" 'Success'
} else {
    Write-Status "[WARN] App might not be running yet, check device" 'Warning'
}

Write-Status "[3/3] All systems ready!" 'Step'
Write-Status ""

# === TESTING INSTRUCTIONS ===
Write-Header "TESTING QUICK START"

Write-Status "On your device/emulator:" 'Info'
Write-Status "1. You should see the Landing Screen with 3 GUI options" 'Info'
Write-Status "2. Click 'Matrix Experience' to test GUI3" 'Info'
Write-Status "3. Try switching between GUI1, GUI2, and GUI3" 'Info'
Write-Status "4. Test dashboard, navigation, data consistency" 'Info'
Write-Status ""

Write-Status "View app logs in real-time:" 'Info'
Write-Status "   adb logcat -s BizapApp:V" 'Info'
Write-Status ""

Write-Status "View crash logs:" 'Info'
Write-Status "   adb logcat -s E: | findstr BizapApp" 'Info'
Write-Status ""

# === OPTIONAL: VIEW LOGS ===
if ($ViewLogs) {
    Write-Header "VIEWING LIVE LOGS"
    Write-Status "Press Ctrl+C to stop" 'Warning'
    Start-Sleep -Seconds 2
    & adb -s $DeviceId logcat -s "BizapApp:V"
}

Write-Header "READY FOR TESTING"
Write-Status "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" 'Info'

