# ============================================================================
# AUTOMATED DEVICE TESTING SCRIPT
# ============================================================================
# Tests Bizap on all connected devices (physical + emulator)
# Reports: Launch success, crashes, UI layout issues
# ============================================================================

param(
    [switch]$Quick = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Continue"
$APP_PACKAGE = "com.emul8r.bizap"
$APP_ACTIVITY = "com.emul8r.bizap.MainActivity"
$BUILD_TYPE = "release"  # Use release APK
$APK_PATH = "app/build/outputs/apk/$BUILD_TYPE/app-$BUILD_TYPE*.apk"
$RESULTS_FILE = "device-testing-results-$(Get-Date -Format 'yyyy-MM-dd-HHmmss').txt"

# Colors for output
$GREEN = "`e[32m"
$RED = "`e[31m"
$YELLOW = "`e[33m"
$BLUE = "`e[36m"
$RESET = "`e[0m"

Write-Host "$BLUE════════════════════════════════════════════════════════════════$RESET"
Write-Host "$BLUE BIZAP AUTOMATED DEVICE TESTING $RESET"
Write-Host "$BLUE════════════════════════════════════════════════════════════════$RESET"
Write-Host ""

# ============================================================================
# STEP 1: BUILD APK
# ============================================================================
Write-Host "$YELLOW[1/4] Building APK...$RESET"
Write-Host "Command: ./gradlew assembleRelease"

$buildOutput = & ./gradlew assembleRelease 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "$GREEN✅ Build successful$RESET"
} else {
    Write-Host "$RED❌ Build failed$RESET"
    Write-Host "Output: $buildOutput"
    exit 1
}

# Find APK
$apkFile = Get-Item $APK_PATH -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $apkFile) {
    Write-Host "$RED❌ APK not found at $APK_PATH$RESET"
    exit 1
}
Write-Host "APK: $($apkFile.Name) ($([math]::Round($apkFile.Length/1MB, 2))MB)"
Write-Host ""

# ============================================================================
# STEP 2: GET CONNECTED DEVICES
# ============================================================================
Write-Host "$YELLOW[2/4] Scanning for devices...$RESET"

$devices = & adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\tdevice$' -or $_ -match '\temulator' } | ForEach-Object { ($_ -split '\t')[0] }

if ($devices.Count -eq 0) {
    Write-Host "$RED❌ No devices found$RESET"
    exit 1
}

Write-Host "$GREEN✅ Found $(($devices | Measure-Object).Count) device(s)$RESET"
$devices | ForEach-Object { Write-Host "   • $_" }
Write-Host ""

# ============================================================================
# STEP 3: TEST ON EACH DEVICE
# ============================================================================
Write-Host "$YELLOW[3/4] Testing on devices...$RESET"
Write-Host ""

$results = @()
$passed = 0
$failed = 0

foreach ($device in $devices) {
    Write-Host "$BLUE━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━$RESET"
    Write-Host "Device: $device"

    $deviceResult = @{
        device = $device
        tests = @()
    }

    # Get device info
    $androidVersion = & adb -s $device shell getprop ro.build.version.release 2>$null
    $manufacturer = & adb -s $device shell getprop ro.manufacturer 2>$null
    $model = & adb -s $device shell getprop ro.model 2>$null
    $isEmulator = if ($device -like "emulator*") { "Emulator" } else { "Physical" }

    Write-Host "Type: $isEmulator | Android: $androidVersion | $manufacturer $model"
    Write-Host ""

    # Test 1: Install APK
    Write-Host "  [Test 1] Installing APK..."
    $installOutput = & adb -s $device install -r $apkFile.FullName 2>&1
    if ($installOutput -match "Success") {
        Write-Host "  $GREEN✅ Install successful$RESET"
        $deviceResult.tests += @{ name = "Install"; status = "PASS" }
        $passed++
    } else {
        Write-Host "  $RED❌ Install failed$RESET"
        Write-Host "  Error: $installOutput"
        $deviceResult.tests += @{ name = "Install"; status = "FAIL"; error = $installOutput }
        $failed++
        $results += $deviceResult
        continue
    }

    # Test 2: Clear app data
    Write-Host "  [Test 2] Clearing app data..."
    & adb -s $device shell pm clear $APP_PACKAGE 2>$null | Out-Null
    Write-Host "  $GREEN✅ App data cleared$RESET"

    # Test 3: Launch app
    Write-Host "  [Test 3] Launching app..."
    & adb -s $device shell am start -n $APP_PACKAGE/$APP_ACTIVITY 2>$null | Out-Null

    # Wait for app to start
    Start-Sleep -Seconds 3

    # Check for crashes in logcat
    $logcat = & adb -s $device logcat -d -e "FATAL|CRASH|Exception" 2>$null | Select-Object -First 5

    if ($logcat) {
        Write-Host "  $RED❌ App crashed$RESET"
        Write-Host "  Error: $logcat"
        $deviceResult.tests += @{ name = "Launch"; status = "FAIL"; error = $logcat }
        $failed++
    } else {
        Write-Host "  $GREEN✅ App launched successfully (no crashes)$RESET"
        $deviceResult.tests += @{ name = "Launch"; status = "PASS" }
        $passed++
    }

    # Test 4: Check if app is still running
    Write-Host "  [Test 4] Verifying app is running..."
    $isRunning = & adb -s $device shell pidof $APP_PACKAGE 2>$null
    if ($isRunning) {
        Write-Host "  $GREEN✅ App is running (PID: $isRunning)$RESET"
        $deviceResult.tests += @{ name = "Running"; status = "PASS" }
        $passed++
    } else {
        Write-Host "  $RED❌ App crashed or stopped$RESET"
        $deviceResult.tests += @{ name = "Running"; status = "FAIL" }
        $failed++
    }

    # Test 5: Check app orientation (tablet detection)
    Write-Host "  [Test 5] Checking display orientation..."
    $rotation = & adb -s $device shell dumpsys display | Select-String "rotation" 2>$null | Select-Object -First 1
    $screenSize = & adb -s $device shell dumpsys display | Select-String "mScreenState" 2>$null | Select-Object -First 1
    Write-Host "  Info: $rotation"
    $deviceResult.tests += @{ name = "Display"; status = "INFO"; value = "$rotation" }

    # Cleanup: Stop app
    Write-Host "  [Cleanup] Stopping app..."
    & adb -s $device shell am force-stop $APP_PACKAGE 2>$null | Out-Null
    Write-Host ""

    $results += $deviceResult
}

Write-Host ""

# ============================================================================
# STEP 4: GENERATE REPORT
# ============================================================================
Write-Host "$YELLOW[4/4] Generating report...$RESET"
Write-Host ""

Write-Host "$BLUE SUMMARY $RESET"
Write-Host "Tests Passed: $GREEN$passed$RESET"
Write-Host "Tests Failed: $RED$failed$RESET"
Write-Host "Pass Rate: $(if ($passed + $failed -gt 0) { [math]::Round(($passed / ($passed + $failed)) * 100, 0) }else { "N/A" })%"
Write-Host ""

# Write detailed results
$reportContent = @"
========================================================================
BIZAP DEVICE TESTING REPORT
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
========================================================================

SUMMARY
=======
Tests Passed: $passed
Tests Failed: $failed
Total Devices: $(($devices | Measure-Object).Count)
Pass Rate: $(if ($passed + $failed -gt 0) { [math]::Round(($passed / ($passed + $failed)) * 100, 0) }else { "N/A" })%

BUILD INFO
==========
APK: $($apkFile.Name)
Size: $([math]::Round($apkFile.Length/1MB, 2))MB
Path: $($apkFile.FullName)

DEVICE RESULTS
==============
"@

foreach ($result in $results) {
    $reportContent += "`n`nDEVICE: $($result.device)`n"
    $reportContent += "==================================================`n"

    foreach ($test in $result.tests) {
        $status = if ($test.status -eq "PASS") { "[PASS]" } elseif ($test.status -eq "FAIL") { "[FAIL]" } else { "[$($test.status)]" }
        $reportContent += "$status  | $($test.name)`n"

        if ($test.error) {
            $reportContent += "        Error: $($test.error)`n"
        }
        if ($test.value) {
            $reportContent += "        Value: $($test.value)`n"
        }
    }
}

$reportContent += "`n`nRECOMMENDATIONS`n══════════════════`n"

if ($failed -eq 0) {
    $reportContent += "✅ All devices passed! Ready for next phase.`n"
} else {
    $reportContent += "❌ Some devices failed. Investigate before release:`n"
    $results | Where-Object { $_.tests | Where-Object { $_.status -eq "FAIL" } } | ForEach-Object {
        $reportContent += "   - $($_.device): $($_.tests | Where-Object { $_.status -eq "FAIL" } | ForEach-Object { $_.name } | Join-String -Separator ", ")`n"
    }
}

$reportContent += "`n`nNEXT STEPS`n══════════`n"
$reportContent += "1. Manual testing: Try creating invoice and recording payment`n"
$reportContent += "2. Check PDF generation on each device`n"
$reportContent += "3. Verify database persistence (restart app, check data)`n"
$reportContent += "4. Monitor battery/memory usage over 5 minutes`n"

# Save report
$reportContent | Out-File -FilePath $RESULTS_FILE -Encoding UTF8
Write-Host "$GREEN✅ Report saved to: $RESULTS_FILE$RESET"
Write-Host ""

# Display key insights
if ($passed -eq ($passed + $failed)) {
    Write-Host "$GREEN════════════════════════════════════════════════════════════════$RESET"
    Write-Host "$GREEN 🎉 ALL TESTS PASSED - READY FOR MANUAL TESTING $RESET"
    Write-Host "$GREEN════════════════════════════════════════════════════════════════$RESET"
} else {
    Write-Host "$RED════════════════════════════════════════════════════════════════$RESET"
    Write-Host "$RED ⚠️  SOME TESTS FAILED - REVIEW REPORT $RESET"
    Write-Host "$RED════════════════════════════════════════════════════════════════$RESET"
}

Write-Host ""
Write-Host "Full report: $RESULTS_FILE"
Write-Host ""



