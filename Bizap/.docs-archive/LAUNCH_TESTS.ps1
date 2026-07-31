################################################################################
#
# BIZAP TEST SUITE LAUNCHER
# One-click automated testing
#
# Purpose: Simplified entry point for test automation
# Usage: ./LAUNCH_TESTS.ps1
#        ./LAUNCH_TESTS.ps1 -Quick    (Phase 1 only)
#        ./LAUNCH_TESTS.ps1 -Full     (All phases)
#
# Created: April 28, 2026
# Version: 1.0
#
################################################################################

param(
    [ValidateSet("Quick", "Full", "Monitor")][string]$Mode = "Full"
)

Clear-Host

Write-Host "`n" -ForegroundColor Cyan
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         BIZAP AUTOMATED TEST SUITE LAUNCHER               ║" -ForegroundColor Cyan
Write-Host "║                April 28, 2026                             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host "`n"

Write-Host "Welcome to Automated Emulator Testing!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 What's included:" -ForegroundColor Cyan
Write-Host "  • Phase 1: Smoke Tests (Unit tests, 15 min)" -ForegroundColor White
Write-Host "  • Phase 2: Functional Tests (Espresso, 45 min)" -ForegroundColor White
Write-Host "  • Phase 3: Error Scenarios (ADB tests, 20 min)" -ForegroundColor White
Write-Host "  • Phase 4: Performance Profiling (30 min)" -ForegroundColor White
Write-Host ""
Write-Host "⏱️  Total Duration: ~2 hours (fully automated)" -ForegroundColor Yellow
Write-Host ""

# Check prerequisites
Write-Host "🔍 Checking prerequisites..." -ForegroundColor Cyan
Write-Host ""

$checks = @{
    "PowerShell version" = $PSVersionTable.PSVersion.Major -ge 5
    "Gradle installed" = Test-Path "./gradlew"
    "Android SDK tools" = Test-Path $env:ANDROID_HOME
    "Emulator running" = $null -ne (& adb devices 2>&1 | Select-String "emulator" -ErrorAction SilentlyContinue)
}

foreach ($check in $checks.GetEnumerator()) {
    $status = if ($check.Value) { "✅" } else { "⚠️" }
    Write-Host "  $status $($check.Key)" -ForegroundColor (if ($check.Value) { "Green" } else { "Yellow" })
}

Write-Host ""

# Get emulator info
try {
    $emulators = & adb devices 2>&1 | Select-Object -Skip 1 | Where-Object { $_ -match "emulator" }
    if ($emulators) {
        $emulator = ($emulators | Select-Object -First 1).Split()[0]
        Write-Host "📱 Emulator detected: $emulator" -ForegroundColor Green
    } else {
        Write-Host "⚠️  No emulator running" -ForegroundColor Yellow
        Write-Host "    Start emulator from Android Studio before running tests" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Could not detect emulator (ADB may not be available)" -ForegroundColor Yellow
}

Write-Host ""

# Show mode selection
Write-Host "🎯 Test Mode:" -ForegroundColor Cyan
Write-Host "  1. Quick  - Phase 1 only (smoke tests, 15 min) - FASTEST" -ForegroundColor White
Write-Host "  2. Full   - All phases (complete test, 2 hours) - RECOMMENDED" -ForegroundColor Green
Write-Host "  3. Monitor - Real-time logcat monitoring (optional, run in parallel)" -ForegroundColor White
Write-Host ""

if ($Mode -eq "Full") {
    Write-Host "Selected: FULL TEST SUITE (All 4 Phases)" -ForegroundColor Green
} elseif ($Mode -eq "Quick") {
    Write-Host "Selected: QUICK TEST (Phase 1 Only)" -ForegroundColor Yellow
} else {
    Write-Host "Selected: MONITOR MODE (Logcat Analysis)" -ForegroundColor Cyan
}

Write-Host ""

# Ask for confirmation
Write-Host "Ready to start testing?" -ForegroundColor Cyan
Write-Host ""

$response = Read-Host "Type 'yes' to start, or 'q' to quit"

if ($response -ne "yes") {
    Write-Host "`n❌ Test launch cancelled." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "🚀 Launching test suite..." -ForegroundColor Green
Write-Host ""

# Execute based on mode
switch ($Mode) {
    "Quick" {
        Write-Host "Running QUICK mode (Phase 1: Smoke Tests)..." -ForegroundColor Cyan
        Write-Host ""
        & ./TEST_AUTOMATION_MASTER.ps1 -Phase 1 -GenerateReport -OpenReportAfter
    }

    "Full" {
        Write-Host "Running FULL mode (All Phases)..." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "💡 TIP: Open another PowerShell and run this for real-time monitoring:" -ForegroundColor Yellow
        Write-Host "    ./LOGCAT_REALTIME_ANALYZER.ps1 -Emulator emulator-5554" -ForegroundColor White
        Write-Host ""
        & ./TEST_AUTOMATION_MASTER.ps1 -Phase All -GenerateReport -OpenReportAfter
    }

    "Monitor" {
        Write-Host "Starting MONITOR mode (Real-time logcat)..." -ForegroundColor Cyan
        Write-Host ""
        & ./LOGCAT_REALTIME_ANALYZER.ps1 -Emulator emulator-5554 -MonitorDurationMinutes 120
    }
}

# Completion
Write-Host "`n"
Write-Host "════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "✅ TEST SUITE COMPLETE" -ForegroundColor Green
Write-Host "════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Review the test report that just opened in your browser" -ForegroundColor White
Write-Host "  2. Check the decision matrix in the report" -ForegroundColor White
Write-Host "  3. If all pass: Schedule real device testing for tomorrow" -ForegroundColor Green
Write-Host "  4. If issues found: Follow troubleshooting guide in report" -ForegroundColor Yellow
Write-Host ""
Write-Host "📂 Test artifacts saved to: test-automation-logs/" -ForegroundColor Cyan
Write-Host ""
Write-Host "Questions? See: AUTOMATED_TESTS_QUICK_START.md" -ForegroundColor Yellow
Write-Host ""

