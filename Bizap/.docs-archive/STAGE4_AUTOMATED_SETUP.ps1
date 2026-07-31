# STAGE 4 AUTOMATED TEST SETUP
# Purpose: Automate pre-test preparation steps
# Run this before beginning manual tests

param(
    [switch]$SkipBuild = $false,
    [switch]$SkipInstall = $false
)

Write-Output "=========================================="
Write-Output "STAGE 4 TEST SETUP AUTOMATION"
Write-Output "=========================================="
Write-Output ""
Write-Output "Date: $(Get-Date)"
Write-Output ""

# Store test results
$testResults = @{
    Timestamp = Get-Date
    BuildSuccess = $false
    InstallSuccess = $false
    DeviceFound = $false
    PrepComplete = $false
}

# Step 1: Navigate to project
Write-Output "[1/5] Navigating to project directory..."
try {
    $projectPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
    Set-Location $projectPath
    Write-Output "✅ Location: $(Get-Location)"
} catch {
    Write-Output "❌ Failed to navigate to project"
    exit 1
}

# Step 2: Build APK
if (-not $SkipBuild) {
    Write-Output ""
    Write-Output "[2/5] Building APK (./gradlew assembleDebug)..."
    Write-Output "This may take 30-60 seconds..."

    $buildOutput = & ./gradlew assembleDebug 2>&1 | Out-String

    if ($buildOutput -match "BUILD SUCCESSFUL") {
        Write-Output "✅ Build successful"
        $testResults.BuildSuccess = $true

        # Verify APK exists
        $apkPath = "app/build/outputs/apk/debug/app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Output "✅ APK size: $([Math]::Round($apkSize, 2)) MB"
        }
    } else {
        Write-Output "❌ Build failed"
        Write-Output $buildOutput | Select-String "error" -First 5
        exit 1
    }
} else {
    Write-Output "[2/5] Skipping build (--SkipBuild)"
    $testResults.BuildSuccess = $true
}

# Step 3: Check device connection
Write-Output ""
Write-Output "[3/5] Checking device connection (adb devices)..."

$devices = & adb devices 2>&1
Write-Output $devices

if ($devices -match "device$") {
    Write-Output "✅ Device connected"
    $testResults.DeviceFound = $true
} else {
    Write-Output "⚠️ No device found - check USB connection and USB debugging enabled"
    Write-Output "Continuing with warning..."
}

# Step 4: Install APK
if (-not $SkipInstall -and $testResults.DeviceFound) {
    Write-Output ""
    Write-Output "[4/5] Installing APK on device..."
    Write-Output "Command: adb install -r app/build/outputs/apk/debug/app-debug.apk"

    $installOutput = & adb install -r app/build/outputs/apk/debug/app-debug.apk 2>&1 | Out-String

    if ($installOutput -match "Success|successful") {
        Write-Output "✅ Installation successful"
        $testResults.InstallSuccess = $true
    } else {
        Write-Output "❌ Installation failed"
        Write-Output $installOutput | Select-String "error|Error|failure" -First 5
    }
} else {
    Write-Output "[4/5] Skipping install (--SkipInstall or no device)"
}

# Step 5: Clear app data
if ($testResults.DeviceFound) {
    Write-Output ""
    Write-Output "[5/5] Clearing app data for fresh start..."

    $clearOutput = & adb shell pm clear com.emul8r.bizap 2>&1 | Out-String

    if ($clearOutput -match "Success|successful" -or -not $clearOutput) {
        Write-Output "✅ App data cleared"
        $testResults.PrepComplete = $true
    } else {
        Write-Output "⚠️ Could not clear app data (app may not be installed)"
    }
}

# Summary
Write-Output ""
Write-Output "=========================================="
Write-Output "SETUP COMPLETE - TEST RESULTS:"
Write-Output "=========================================="
Write-Output ""
Write-Output "Build:         $(if ($testResults.BuildSuccess) { '✅ SUCCESS' } else { '❌ FAILED' })"
Write-Output "Device:        $(if ($testResults.DeviceFound) { '✅ CONNECTED' } else { '⚠️ NOT FOUND' })"
Write-Output "Installation:  $(if ($testResults.InstallSuccess) { '✅ SUCCESS' } else { '⚠️ SKIPPED' })"
Write-Output "Prep Complete: $(if ($testResults.PrepComplete) { '✅ YES' } else { '⚠️ PARTIAL' })"
Write-Output ""

if ($testResults.BuildSuccess -and $testResults.DeviceFound) {
    Write-Output "✅ Ready for manual testing!"
    Write-Output ""
    Write-Output "Next steps:"
    Write-Output "1. Launch app on device"
    Write-Output "2. Follow STAGE4_MANUAL_TESTING_GUIDE.md"
    Write-Output "3. Record results"
    Write-Output ""
    Write-Output "Pro tip: Start logcat monitoring in another terminal:"
    Write-Output "  adb logcat -s 'BizapApp:V' > stage4_logcat.txt"
} else {
    Write-Output "⚠️ Some setup steps were skipped or failed"
    Write-Output "Please verify device connection and build status before testing"
}

Write-Output ""
Write-Output "Setup completed at $(Get-Date)"

