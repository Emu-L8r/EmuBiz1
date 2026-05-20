# MASTER VALIDATION ORCHESTRATOR
# Runs all 6 validation stages sequentially
# Total time: 10-15 minutes (with manual testing)

Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║   BIZAP COMPREHENSIVE VALIDATION SUITE                ║"
Write-Output "║   Date: May 10, 2026                                  ║"
Write-Output "║   Version: v0.9.3-Gold-Stable-Testing                ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""
Write-Output "This script will run all 6 validation stages:"
Write-Output ""
Write-Output "1️⃣  QUICK BUILD (30s)    - Unit tests only, no device"
Write-Output "2️⃣  APK COMPILATION (1m)  - Build debug APK"
Write-Output "3️⃣  CORE FEATURES (5m)    - Install & launch app (MANUAL)"
Write-Output "4️⃣  PDF CUSTOMIZATION (5m)- Test settings & presets (MANUAL)"
Write-Output "5️⃣  ERROR MONITORING (∞)  - Live logcat tracking"
Write-Output "6️⃣  FULL REGRESSION (3m)  - Complete build + tests"
Write-Output ""
Write-Output "Total time: 15-20 minutes (includes manual testing)"
Write-Output ""

# Confirm start
$confirm = Read-Host "Start validation suite? (yes/no)"
if ($confirm -ne "yes") {
    Write-Output "Cancelled."
    exit 0
}

Write-Output ""
$startTime = Get-Date

# =============================================================================
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ STAGE 1: QUICK BUILD VALIDATION (30 seconds)         ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""

& ".\01_STAGE1_QuickTest.ps1"

Write-Output ""
$stageConfirm = Read-Host "Stage 1 complete. Continue to Stage 2? (yes/no)"
if ($stageConfirm -ne "yes") {
    Write-Output "Validation suite paused."
    exit 0
}

# =============================================================================
Write-Output ""
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ STAGE 2: FAST COMPILATION CHECK (60 seconds)         ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""

& ".\02_STAGE2_BuildAPK.ps1"

Write-Output ""
$stageConfirm = Read-Host "Stage 2 complete. Continue to Stage 3? (yes/no, skip to Stage 6?)"
if ($stageConfirm -eq "6") {
    Write-Output "Skipping to Stage 6..."
    goto Stage6
}
if ($stageConfirm -ne "yes") {
    Write-Output "Validation suite paused."
    exit 0
}

# =============================================================================
Write-Output ""
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ STAGE 3: CORE FEATURE VALIDATION (5 minutes)         ║"
Write-Output "║ MANUAL: Test on device                                ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""

& ".\03_STAGE3_CoreFeatures.ps1"

Write-Output ""
Write-Output "⏳ MANUAL PHASE: Test on device..."
Write-Output ""
$manualConfirm = Read-Host "Manual testing complete? (yes/no)"
if ($manualConfirm -ne "yes") {
    Write-Output "Validation suite paused."
    exit 0
}

# =============================================================================
Write-Output ""
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ STAGE 4: INVOICE CUSTOMIZATION (5 minutes)           ║"
Write-Output "║ MANUAL: Test PDF settings & presets                  ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""

& ".\04_STAGE4_PDFCustomization.ps1"

Write-Output ""
Write-Output "⏳ MANUAL PHASE: Test PDF customization..."
Write-Output ""
$manualConfirm = Read-Host "Manual testing complete? (yes/no)"
if ($manualConfirm -ne "yes") {
    Write-Output "Validation suite paused."
    exit 0
}

# =============================================================================
Write-Output ""
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ STAGE 5: REAL-TIME LOGCAT MONITORING                  ║"
Write-Output "║ CONTINUOUS: Monitor for errors/crashes                ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""
Write-Output "⏳ Opening logcat monitoring..."
Write-Output "Press Ctrl+C when done testing to move to Stage 6"
Write-Output ""

& ".\05_STAGE5_LogcatMonitor.ps1"

Write-Output ""
$stageConfirm = Read-Host "Logcat monitoring complete. Continue to Stage 6? (yes/no)"
if ($stageConfirm -ne "yes") {
    Write-Output "Validation suite paused."
    exit 0
}

# =============================================================================
Stage6:
Write-Output ""
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ STAGE 6: FULL BUILD + TESTS (2-3 minutes)            ║"
Write-Output "║ FINAL: Complete regression test                       ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""

& ".\06_STAGE6_FullBuild.ps1"

# =============================================================================
Write-Output ""
Write-Output "╔════════════════════════════════════════════════════════╗"
Write-Output "║ VALIDATION SUITE COMPLETE                             ║"
Write-Output "╚════════════════════════════════════════════════════════╝"
Write-Output ""

$endTime = Get-Date
$duration = $endTime - $startTime

Write-Output "Total validation time: $($duration.ToString('mm\:ss'))"
Write-Output ""
Write-Output "✅ All stages completed successfully!"
Write-Output ""
Write-Output "Next steps:"
Write-Output "  1. Review all log files in project root"
Write-Output "  2. Check for any warnings or errors"
Write-Output "  3. Verify all 102 tests passed"
Write-Output "  4. Confirm PDF customization working end-to-end"
Write-Output ""
Write-Output "For issues, check:"
Write-Output "  - stage1_tests_*.log (unit test failures)"
Write-Output "  - stage2_build_*.log (compilation issues)"
Write-Output "  - stage3_app_*.log (app installation/launch)"
Write-Output "  - logcat_*.log (runtime errors)"
Write-Output "  - stage6_full_build_*.log (regression test)"
Write-Output ""

