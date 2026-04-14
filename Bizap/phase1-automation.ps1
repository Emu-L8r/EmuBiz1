#!/usr/bin/env powershell
<#
.SYNOPSIS
    PHASE 1 Automated Testing Script for BizAP App

.DESCRIPTION
    Automates:
    - Device connection verification
    - APK installation
    - App launching
    - Log capture and analysis
    - Health checks

    Requires manual testing for:
    - Creating invoices
    - Changing status via UI
    - Visual verification

.USAGE
    .\phase1-automation.ps1

.NOTES
    Run from the project root directory
#>

# ============================================================================
# CONFIGURATION
# ============================================================================

$AppPackage = "com.emul8r.bizap"
$AppActivity = "com.emul8r.bizap.MainActivity"
$ApkPath = "app/build/outputs/apk/debug/app-debug.apk"
$TestResultsFile = "PHASE1_TEST_RESULTS_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"
$LogFile = "phase1_logcat_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

function Write-Header {
    param([string]$Text)
    Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║ $($Text.PadRight(56)) ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Success {
    param([string]$Text)
    Write-Host "✅ $Text" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Text)
    Write-Host "⚠️  $Text" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Text)
    Write-Host "❌ $Text" -ForegroundColor Red
}

function Write-Info {
    param([string]$Text)
    Write-Host "ℹ️  $Text" -ForegroundColor Cyan
}

# Test ADB
function Test-Adb {
    try {
        adb version > $null 2>&1
        Write-Success "ADB is installed and accessible"
        return $true
    } catch {
        Write-Error-Custom "ADB not found. Install Android SDK tools."
        return $false
    }
}

# Get connected devices
function Get-ConnectedDevices {
    $devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" } | ForEach-Object { $_.Split()[0] }
    return $devices
}

# Check device connection
function Test-DeviceConnection {
    Write-Info "Checking for connected devices..."
    $devices = Get-ConnectedDevices

    if ($devices.Count -eq 0) {
        Write-Error-Custom "No devices connected. Connect your tablet via USB."
        return $null
    }

    if ($devices.Count -eq 1) {
        Write-Success "Found device: $($devices[0])"
    } else {
        Write-Success "Found $($devices.Count) devices"
    }
    return $devices[0]
}

# Check APK exists
function Test-ApkExists {
    if (Test-Path $ApkPath) {
        $size = [Math]::Round((Get-Item $ApkPath).Length / 1MB, 2)
        Write-Success "APK found: $ApkPath ($size MB)"
        return $true
    } else {
        Write-Error-Custom "APK not found at $ApkPath"
        return $false
    }
}

# Install app
function Install-App {
    param([string]$DeviceId)
    Write-Info "Installing APK on $DeviceId..."
    Write-Info "This may take 30-60 seconds..."

    $output = adb -s $DeviceId install -r $ApkPath 2>&1 | Out-String

    if ($output -match "Success") {
        Write-Success "App installed successfully"
        return $true
    } else {
        Write-Error-Custom "Installation failed"
        Write-Host $output
        return $false
    }
}

# Launch app
function Launch-App {
    param([string]$DeviceId)
    Write-Info "Launching app on $DeviceId..."

    $output = adb -s $DeviceId shell am start -n "$AppPackage/$AppActivity" 2>&1 | Out-String

    if ($output -match "Error|Exception") {
        Write-Error-Custom "Failed to launch app"
        Write-Host $output
        return $false
    }

    Write-Success "App launched successfully"
    Write-Info "Waiting 3 seconds for app to stabilize..."
    Start-Sleep -Seconds 3
    return $true
}

# Capture logs
function Capture-Logs {
    param([string]$DeviceId, [int]$Seconds = 60)

    Write-Info "Capturing logs for $Seconds seconds..."
    Write-Info "Saving to: $LogFile"

    $endTime = (Get-Date).AddSeconds($Seconds)
    $elapsed = 0

    while ((Get-Date) -lt $endTime) {
        $elapsed = [Math]::Round(((Get-Date) - ($endTime.AddSeconds(-$Seconds))).TotalSeconds)
        $remaining = $Seconds - $elapsed
        Write-Host "`rProgress: $elapsed/$Seconds seconds" -NoNewline
        Start-Sleep -Seconds 1
    }

    Write-Host "`r" + (" " * 40)
    Write-Success "Log capture complete"

    adb -s $DeviceId logcat -d | Out-File -FilePath $LogFile -Encoding UTF8
}

# Health check
function Check-AppHealth {
    param([string]$DeviceId)

    Write-Header "APP HEALTH CHECK"

    $logContent = adb -s $DeviceId logcat -d | Out-String

    $checks = @{
        "App Running" = $logContent -match "ActivityManager.*Started"
        "Status Counts Unified" = $logContent -match "Status Counts \(unified\)"
        "Dashboard Loaded" = $logContent -match "Dashboard"
        "No Fatal Crashes" = $logContent -notmatch "Fatal Exception"
        "No Update Errors" = $logContent -notmatch "ERROR.*failed"
    }

    $passed = 0
    foreach ($check in $checks.GetEnumerator()) {
        if ($check.Value) {
            Write-Host "[PASS] $($check.Name)" -ForegroundColor Green
            $passed++
        } else {
            Write-Host "[WARN] $($check.Name)" -ForegroundColor Yellow
        }
    }

    Write-Host ""
    Write-Info "Health check: $passed/$($checks.Count) checks passed"

    return @{ Passed = $passed; Total = $checks.Count }
}

# Manual test: Status persistence
function Test-StatusPersistence {
    Write-Header "TEST 1: STATUS PERSISTENCE (Manual)"
    Write-Host "What: Fix for SENT/DRAFT Bug" -ForegroundColor Yellow
    Write-Host "Problem Before: Status would revert to DRAFT when reopening" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Steps to perform on tablet:" -ForegroundColor Green
    Write-Host "1. Create new invoice → leave as DRAFT"
    Write-Host "2. Open in GUI2 (modern interface)"
    Write-Host "3. Tap status chip → change to SENT"
    Write-Host "4. Verify badge changes color immediately"
    Write-Host "5. Close invoice detail"
    Write-Host "6. REOPEN same invoice"
    Write-Host "7. Check if status still shows SENT"
    Write-Host ""
    Write-Host "Expected: Status should PERSIST as SENT (not revert to DRAFT)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Enter result (PASS/FAIL/SKIP): " -NoNewline
    $result = Read-Host
    return $result.ToUpper()
}

# Manual test: Pie chart parity
function Test-PieChartParity {
    Write-Header "TEST 2: PIE CHART PARITY (Manual)"
    Write-Host "What: Both dashboards show same status counts" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Setup - Create 5 test invoices:" -ForegroundColor Cyan
    Write-Host "1. Leave as DRAFT"
    Write-Host "2. Change to SENT"
    Write-Host "3. Change to PAID"
    Write-Host "4. Change to PARTIALLY_PAID"
    Write-Host "5. Set to OVERDUE (use past due date)"
    Write-Host ""
    Write-Host "Steps to perform on tablet:" -ForegroundColor Green
    Write-Host "1. Open GUI2 Dashboard (modern interface)"
    Write-Host "2. Look at pie chart legend"
    Write-Host "3. Write down the counts:"
    Write-Host "   - DRAFT: ___"
    Write-Host "   - SENT: ___"
    Write-Host "   - PAID: ___"
    Write-Host "   - OVERDUE: ___"
    Write-Host "   - PARTIALLY_PAID: ___"
    Write-Host "4. Force-stop app completely"
    Write-Host "5. Reopen app"
    Write-Host "6. Open GUI1 Dashboard (classic interface)"
    Write-Host "7. Verify pie chart counts match EXACTLY"
    Write-Host ""
    Write-Host "Expected: GUI1 and GUI2 show identical status counts" -ForegroundColor Green
    Write-Host ""
    Write-Host "Enter result (PASS/FAIL/SKIP): " -NoNewline
    $result = Read-Host
    return $result.ToUpper()
}

# Manual test: Live sync
function Test-LiveSync {
    Write-Header "TEST 3: LIVE SYNCHRONIZATION (Manual)"
    Write-Host "What: Status changes visible in both GUIs" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Steps to perform on tablet:" -ForegroundColor Green
    Write-Host "1. Create invoice, set to SENT"
    Write-Host "2. Open in GUI2, change status to PAID"
    Write-Host "3. Go back to GUI2 Dashboard"
    Write-Host "4. Check pie chart:"
    Write-Host "   - SENT count should decrease by 1"
    Write-Host "   - PAID count should increase by 1"
    Write-Host "5. Switch to GUI1 Dashboard (tap GUI switch button)"
    Write-Host "6. Verify GUI1 pie chart also shows updated counts"
    Write-Host ""
    Write-Host "Expected: Both dashboards update in real-time with same counts" -ForegroundColor Green
    Write-Host ""
    Write-Host "Enter result (PASS/FAIL/SKIP): " -NoNewline
    $result = Read-Host
    return $result.ToUpper()
}

# Generate report
function Generate-Report {
    param([hashtable]$Results)

    $test1Status = switch ($Results['StatusPersistence']) {
        'PASS' { '[PASS]' }
        'FAIL' { '[FAIL]' }
        'SKIP' { '[SKIP]' }
        default { '[UNKNOWN]' }
    }

    $test2Status = switch ($Results['PieChartParity']) {
        'PASS' { '[PASS]' }
        'FAIL' { '[FAIL]' }
        'SKIP' { '[SKIP]' }
        default { '[UNKNOWN]' }
    }

    $test3Status = switch ($Results['LiveSync']) {
        'PASS' { '[PASS]' }
        'FAIL' { '[FAIL]' }
        'SKIP' { '[SKIP]' }
        default { '[UNKNOWN]' }
    }

    # Determine overall status
    $passCount = @($Results['StatusPersistence'] -eq 'PASS', $Results['PieChartParity'] -eq 'PASS', $Results['LiveSync'] -eq 'PASS') | Where-Object { $_ } | Measure-Object | Select-Object -ExpandProperty Count
    $skipCount = @($Results['StatusPersistence'] -eq 'SKIP', $Results['PieChartParity'] -eq 'SKIP', $Results['LiveSync'] -eq 'SKIP') | Where-Object { $_ } | Measure-Object | Select-Object -ExpandProperty Count

    if ($passCount -eq 3) {
        $overallStatus = "[SUCCESS] All tests passed!"
        $nextStep = "Ready to proceed with Phase 2 (UI Standardization)"
    } elseif ($passCount -gt 0) {
        $overallStatus = "[PARTIAL] Some tests passed ($passCount/3)"
        $nextStep = "Review failed tests and investigate issues"
    } elseif ($skipCount -eq 3) {
        $overallStatus = "[INCOMPLETE] All tests were skipped"
        $nextStep = "Run tests again to get complete results"
    } else {
        $overallStatus = "[FAILED] Tests failed"
        $nextStep = "Review log file and troubleshoot issues"
    }

    Write-Header "PHASE 1 TEST RESULTS SUMMARY"

    $report = @"
╔════════════════════════════════════════════════════════════╗
║              PHASE 1 COMPREHENSIVE TEST REPORT             ║
╚════════════════════════════════════════════════════════════╝

Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

APP HEALTH CHECK
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Checks Passed: $($Results['HealthCheck'].Passed)/$($Results['HealthCheck'].Total)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

MANUAL TESTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Test 1 - Status Persistence (SENT/DRAFT Bug Fix): $test1Status
Test 2 - Pie Chart Parity (GUI1 vs GUI2): $test2Status
Test 3 - Live Synchronization (Real-time Updates): $test3Status

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

OVERALL RESULT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$overallStatus

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

ARTIFACTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Logcat File: $LogFile
Results File: $TestResultsFile

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

NEXT STEPS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

$nextStep

See PHASE2_DETAILED_PLAN.md for Phase 2 planning.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
"@

    Write-Host $report
    $report | Out-File -FilePath $TestResultsFile -Encoding UTF8
    Write-Host ""
    Write-Success "Results saved to: $TestResultsFile"
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

function Main {
    Write-Header "PHASE 1 AUTOMATED TESTING - BIZAP"
    Write-Host ""

    # Step 1: Check ADB
    Write-Info "Step 1/6: Checking ADB availability..."
    if (-not (Test-Adb)) {
        Write-Error-Custom "Cannot proceed without ADB"
        exit 1
    }
    Write-Host ""

    # Step 2: Check device
    Write-Info "Step 2/6: Checking device connection..."
    $deviceId = Test-DeviceConnection
    if (-not $deviceId) {
        Write-Error-Custom "Cannot proceed without device"
        exit 1
    }
    Write-Host ""

    # Step 3: Check APK
    Write-Info "Step 3/6: Checking APK..."
    if (-not (Test-ApkExists)) {
        Write-Error-Custom "Cannot proceed without APK"
        exit 1
    }
    Write-Host ""

    # Step 4: Install
    Write-Info "Step 4/6: Installing app..."
    if (-not (Install-App -DeviceId $deviceId)) {
        Write-Error-Custom "Installation failed"
        exit 1
    }
    Write-Host ""

    # Step 5: Launch
    Write-Info "Step 5/6: Launching app..."
    if (-not (Launch-App -DeviceId $deviceId)) {
        Write-Error-Custom "Launch failed"
        exit 1
    }
    Write-Host ""

    # Step 6: Tests
    Write-Info "Step 6/6: Running tests..."
    Capture-Logs -DeviceId $deviceId -Seconds 30
    Write-Host ""

    $healthCheck = Check-AppHealth -DeviceId $deviceId
    Write-Host ""

    # Manual tests
    $statusPersistence = Test-StatusPersistence
    Write-Host ""

    $pieChartParity = Test-PieChartParity
    Write-Host ""

    $liveSync = Test-LiveSync
    Write-Host ""

    # Results
    $results = @{
        'HealthCheck' = $healthCheck
        'StatusPersistence' = $statusPersistence
        'PieChartParity' = $pieChartParity
        'LiveSync' = $liveSync
    }

    Generate-Report -Results $results
}

# Run
Main



