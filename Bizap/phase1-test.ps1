# PHASE 1 Automated Testing Script - Simple Version
# Avoids Unicode encoding issues

param(
    [string]$TabletId = ""
)

$AppPackage = "com.emul8r.bizap"
$AppActivity = "com.emul8r.bizap.MainActivity"
$ApkPath = "app/build/outputs/apk/debug/app-debug.apk"
$TestResultsFile = "PHASE1_TEST_RESULTS_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"
$LogFile = "phase1_logcat_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          PHASE 1 AUTOMATED TESTING - BIZAP APP" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check ADB
Write-Host "[1/6] Checking ADB availability..." -ForegroundColor Cyan
try {
    adb version > $null 2>&1
    Write-Host "[OK] ADB is installed" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] ADB not found. Install Android SDK tools." -ForegroundColor Red
    exit 1
}

# Step 2: Check device
Write-Host "[2/6] Checking for connected devices..." -ForegroundColor Cyan
$devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device`$" } | ForEach-Object { $_.Split()[0] }

if ($devices.Count -eq 0) {
    Write-Host "[ERROR] No devices connected. Connect your tablet via USB." -ForegroundColor Red
    exit 1
}

if ($devices.Count -eq 1) {
    $deviceId = $devices[0]
    Write-Host "[OK] Found device: $deviceId" -ForegroundColor Green
} else {
    $deviceId = $devices[0]
    Write-Host "[OK] Found $($devices.Count) devices. Using: $deviceId" -ForegroundColor Green
}

# Step 3: Check APK
Write-Host "[3/6] Checking APK..." -ForegroundColor Cyan
if (Test-Path $ApkPath) {
    $size = [Math]::Round((Get-Item $ApkPath).Length / 1MB, 2)
    Write-Host "[OK] APK found: $ApkPath ($size MB)" -ForegroundColor Green
} else {
    Write-Host "[ERROR] APK not found at $ApkPath" -ForegroundColor Red
    exit 1
}

# Step 4: Install app
Write-Host "[4/6] Installing app on tablet..." -ForegroundColor Cyan
Write-Host "       This may take 30-60 seconds..." -ForegroundColor Cyan
$output = adb -s $deviceId install -r $ApkPath 2>&1 | Out-String

if ($output -match "Success") {
    Write-Host "[OK] App installed successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Installation failed" -ForegroundColor Red
    Write-Host $output
    exit 1
}

# Step 5: Launch app
Write-Host "[5/6] Launching app..." -ForegroundColor Cyan
$output = adb -s $deviceId shell am start -n "$AppPackage/$AppActivity" 2>&1 | Out-String

if ($output -match "Error|Exception") {
    Write-Host "[ERROR] Failed to launch app" -ForegroundColor Red
    Write-Host $output
    exit 1
}

Write-Host "[OK] App launched successfully" -ForegroundColor Green
Write-Host "       Waiting 3 seconds for app to stabilize..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

# Step 6: Capture logs and health check
Write-Host "[6/6] Capturing logs and running health checks..." -ForegroundColor Cyan
Write-Host "       Capturing for 30 seconds..." -ForegroundColor Cyan

$endTime = (Get-Date).AddSeconds(30)
while ((Get-Date) -lt $endTime) {
    Start-Sleep -Seconds 1
}

Write-Host "[OK] Log capture complete" -ForegroundColor Green
adb -s $deviceId logcat -d | Out-File -FilePath $LogFile -Encoding UTF8

# Health check
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          APP HEALTH CHECK" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$logContent = adb -s $deviceId logcat -d | Out-String
$passed = 0
$total = 0

$checks = @{
    "App Running" = $logContent -match "ActivityManager.*Started"
    "Status Counts Unified" = $logContent -match "Status Counts"
    "No Fatal Crashes" = $logContent -notmatch "Fatal Exception"
}

foreach ($check in $checks.GetEnumerator()) {
    $total++
    if ($check.Value) {
        Write-Host "[PASS] $($check.Name)" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "[WARN] $($check.Name)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Health check: $passed/$total passed" -ForegroundColor Cyan
Write-Host ""

# Manual tests
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          MANUAL TESTING REQUIRED" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "TEST 1: Status Persistence (5 minutes)" -ForegroundColor Yellow
Write-Host "  What: Fix for SENT/DRAFT Bug"
Write-Host "  Steps:"
Write-Host "    1. Create new invoice -> leave as DRAFT"
Write-Host "    2. Open in GUI2 (modern interface)"
Write-Host "    3. Tap status chip -> change to SENT"
Write-Host "    4. Close invoice"
Write-Host "    5. Reopen same invoice"
Write-Host "    6. Check if status still shows SENT"
Write-Host "  Expected: Status should persist as SENT (not revert to DRAFT)"
Write-Host ""
Write-Host "Enter result (PASS/FAIL/SKIP): " -NoNewline
$test1 = Read-Host

Write-Host ""
Write-Host "TEST 2: Pie Chart Parity (10 minutes)" -ForegroundColor Yellow
Write-Host "  What: Both dashboards show same counts"
Write-Host "  Setup: Create 5 invoices (DRAFT, SENT, PAID, PARTIAL, OVERDUE)"
Write-Host "  Steps:"
Write-Host "    1. Open GUI2 Dashboard"
Write-Host "    2. Note pie chart counts for each status"
Write-Host "    3. Force-stop app"
Write-Host "    4. Reopen and check GUI1 Dashboard"
Write-Host "    5. Compare counts"
Write-Host "  Expected: Counts should match exactly between GUI1 and GUI2"
Write-Host ""
Write-Host "Enter result (PASS/FAIL/SKIP): " -NoNewline
$test2 = Read-Host

Write-Host ""
Write-Host "TEST 3: Live Synchronization (5 minutes)" -ForegroundColor Yellow
Write-Host "  What: Status changes visible in both GUIs"
Write-Host "  Steps:"
Write-Host "    1. Create invoice, set to SENT"
Write-Host "    2. Open in GUI2, change status to PAID"
Write-Host "    3. Check GUI2 dashboard: counts updated?"
Write-Host "    4. Switch to GUI1 dashboard"
Write-Host "    5. Verify both show updated counts"
Write-Host "  Expected: Both dashboards update in real-time"
Write-Host ""
Write-Host "Enter result (PASS/FAIL/SKIP): " -NoNewline
$test3 = Read-Host

# Generate report
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          PHASE 1 TEST RESULTS" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

$report = @"
PHASE 1 COMPREHENSIVE TEST REPORT
Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

APP HEALTH CHECK
===============
Checks Passed: $passed/$total

MANUAL TESTS
============
Test 1 - Status Persistence: $test1
Test 2 - Pie Chart Parity: $test2
Test 3 - Live Synchronization: $test3

ARTIFACTS
=========
Logcat File: $LogFile
Results File: $TestResultsFile

OVERALL RESULT
==============
"@

$passCount = @($test1 -eq 'PASS', $test2 -eq 'PASS', $test3 -eq 'PASS') | Where-Object { $_ } | Measure-Object | Select-Object -ExpandProperty Count

if ($passCount -eq 3) {
    $report += "SUCCESS - All tests passed!`nNEXT STEPS: Ready to proceed with Phase 2"
} elseif ($passCount -gt 0) {
    $report += "PARTIAL - Some tests passed ($passCount/3)`nNEXT STEPS: Review failed tests"
} else {
    $report += "FAILED - Tests did not pass`nNEXT STEPS: Review logs and troubleshoot"
}

Write-Host $report
Write-Host ""
$report | Out-File -FilePath $TestResultsFile -Encoding UTF8

Write-Host "Results saved to: $TestResultsFile" -ForegroundColor Green
Write-Host "Logs saved to: $LogFile" -ForegroundColor Green
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan

