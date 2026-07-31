#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Validates fixes for frame skips, StrictMode violations, and database contention
    on Bizap Android app (April 25, 2026 patch)

.DESCRIPTION
    - Checks APK build status
    - Deploys to connected device
    - Collects Logcat traces for validation
    - Reports pass/fail for each fix

.EXAMPLE
    .\validate_fixes_april25.ps1

.NOTES
    Requires: adb, gradle, Android device/emulator connected
#>

param(
    [switch]$SkipDeploy = $false,
    [int]$LogcatDurationSeconds = 30
)

$ErrorActionPreference = "Stop"
$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

Write-Host @"
╔════════════════════════════════════════════════════════════════════════════════╗
║                                                                                │
║   🔧 BIZAP PERFORMANCE FIX VALIDATION (April 25, 2026)                        │
║   ─────────────────────────────────────────────────────────────────────────────│
║   Testing: Frame Skips, StrictMode Violations, DB Contention                  │
║                                                                                │
╚════════════════════════════════════════════════════════════════════════════════╝
"@

# ════════════════════════════════════════════════════════════════════════════════
# STEP 1: Verify Build
# ════════════════════════════════════════════════════════════════════════════════

Write-Host "`n📦 [STEP 1/5] Verifying build status..." -ForegroundColor Cyan

Push-Location $projectRoot

$apkPath = "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "✅ APK found: $apkPath ($([math]::Round($apkSize, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "❌ APK not found at $apkPath" -ForegroundColor Red
    Write-Host "   Building now..." -ForegroundColor Yellow
    & ./gradlew assembleDebug -x test 2>&1 | Select-Object -Last 5
    if (-not (Test-Path $apkPath)) {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Build successful" -ForegroundColor Green
}

# ════════════════════════════════════════════════════════════════════════════════
# STEP 2: Check Device Connection
# ════════════════════════════════════════════════════════════════════════════════

Write-Host "`n📱 [STEP 2/5] Checking device connection..." -ForegroundColor Cyan

$devices = adb devices
if ($devices -match "device$") {
    Write-Host "✅ Device connected and ready" -ForegroundColor Green
    $devices | Select-Object -Skip 1 | Where-Object { $_ -match "^\S+\s+" } | ForEach-Object {
        Write-Host "   Device: $_" -ForegroundColor Gray
    }
} else {
    Write-Host "❌ No devices found. Please connect Android device via USB." -ForegroundColor Red
    Write-Host "   Or ensure emulator is running." -ForegroundColor Yellow
    adb devices
    exit 1
}

# ════════════════════════════════════════════════════════════════════════════════
# STEP 3: Deploy APK (Optional)
# ════════════════════════════════════════════════════════════════════════════════

Write-Host "`n🚀 [STEP 3/5] Deploying APK..." -ForegroundColor Cyan

if (-not $SkipDeploy) {
    Write-Host "   Uninstalling old version..." -ForegroundColor Gray
    adb uninstall com.emul8r.bizap 2>&1 | Out-Null

    Write-Host "   Installing new APK..." -ForegroundColor Gray
    $installResult = adb install $apkPath 2>&1
    if ($installResult -match "Success") {
        Write-Host "✅ APK installed successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ Installation failed:" -ForegroundColor Red
        $installResult | Write-Host
        exit 1
    }
} else {
    Write-Host "⏭️  Skipping deployment (--SkipDeploy flag set)" -ForegroundColor Yellow
}

# ════════════════════════════════════════════════════════════════════════════════
# STEP 4: Launch App & Collect Logcat
# ════════════════════════════════════════════════════════════════════════════════

Write-Host "`n📊 [STEP 4/5] Launching app and collecting Logcat traces..." -ForegroundColor Cyan

Write-Host "   Launching app..." -ForegroundColor Gray
adb shell am start -n com.emul8r.bizap/.MainActivity 2>&1 | Out-Null
Start-Sleep -Seconds 2

Write-Host "   Recording Logcat for $LogcatDurationSeconds seconds..." -ForegroundColor Gray
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $projectRoot "logcat_validation_$timestamp.txt"

# Collect Logcat with relevant filters
adb logcat -v threadtime -s `
    "BizapApp:V" `
    "MatrixBackground:V" `
    "StrictMode:V" `
    "Choreographer:W" `
    "SQLCipher:V" `
    "SQLiteDatabase:V" `
    | Out-File $logFile &

$logcatPid = $?
Start-Sleep -Seconds $LogcatDurationSeconds

# Stop logcat gracefully
adb logcat -c 2>&1 | Out-Null

Write-Host "✅ Logcat saved: $logFile" -ForegroundColor Green

# ════════════════════════════════════════════════════════════════════════════════
# STEP 5: Analyze Results
# ════════════════════════════════════════════════════════════════════════════════

Write-Host "`n🔍 [STEP 5/5] Analyzing Logcat for fix validation..." -ForegroundColor Cyan

$logContent = Get-Content $logFile -ErrorAction SilentlyContinue

$results = @{
    MatrixDeferral = $false
    NoFrameSkips = $false
    NoStrictModeViolations = $false
    NoDBLocks = $false
    CleanLogcat = $false
}

# Check 1: MatrixBackground deferral
if ($logContent -match "MatrixBackground initialization deferred") {
    Write-Host "✅ Fix #1 (Matrix Deferral): MatrixBackground deferred as expected" -ForegroundColor Green
    $results.MatrixDeferral = $true
} else {
    Write-Host "⚠️  Fix #1 (Matrix Deferral): DeferralLog not found (may appear after delay)" -ForegroundColor Yellow
}

# Check 2: No frame skips
$frameSkips = $logContent -split "`n" | Select-String -Pattern "Skipped (\d+) frames"
if ($frameSkips) {
    $frameSkips | ForEach-Object {
        if ($_ -match "Skipped (\d+) frames") {
            $count = [int]$matches[1]
            if ($count -le 2) {
                Write-Host "✅ Fix #2 (Frame Skips): Only $count skips (acceptable threshold < 3)" -ForegroundColor Green
                $results.NoFrameSkips = $true
            } else {
                Write-Host "❌ Fix #2 (Frame Skips): $count skips detected (THRESHOLD EXCEEDED)" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host "✅ Fix #2 (Frame Skips): No frame skip warnings found!" -ForegroundColor Green
    $results.NoFrameSkips = $true
}

# Check 3: No StrictMode violations
$strictModeViolations = $logContent | Select-String -Pattern "(DiskRead|DiskWrite)Violation" -All | Measure-Object
if ($strictModeViolations.Count -eq 0) {
    Write-Host "✅ Fix #3 (StrictMode): No violations detected" -ForegroundColor Green
    $results.NoStrictModeViolations = $true
} else {
    Write-Host "❌ Fix #3 (StrictMode): $($strictModeViolations.Count) violations found" -ForegroundColor Red
    $strictModeViolations | ForEach-Object { Write-Host "   $_" -ForegroundColor Red }
}

# Check 4: No database locks
$dbLocks = $logContent | Select-String -Pattern "Long monitor contention" -All | Measure-Object
if ($dbLocks.Count -eq 0) {
    Write-Host "✅ Fix #4 (DB Contention): No long locks detected" -ForegroundColor Green
    $results.NoDBLocks = $true
} else {
    Write-Host "⚠️  Fix #4 (DB Contention): $($dbLocks.Count) lock entries found (review severity)" -ForegroundColor Yellow
}

# Check 5: Logcat cleanliness
$errorCount = $logContent -split "`n" | Select-String -Pattern "ERROR|FATAL" | Measure-Object
if ($errorCount.Count -eq 0) {
    Write-Host "✅ Fix #5 (Logcat Clean): No errors/fatals found" -ForegroundColor Green
    $results.CleanLogcat = $true
} else {
    Write-Host "⚠️  Fix #5 (Logcat Clean): $($errorCount.Count) error entries (review)" -ForegroundColor Yellow
}

# ════════════════════════════════════════════════════════════════════════════════
# SUMMARY
# ════════════════════════════════════════════════════════════════════════════════

$passCount = ($results.Values | Where-Object { $_ -eq $true }).Count
$totalTests = $results.Count

Write-Host @"

╔════════════════════════════════════════════════════════════════════════════════╗
║                         📋 VALIDATION SUMMARY                                 │
╠════════════════════════════════════════════════════════════════════════════════╣
║                                                                                │
║  Tests Passed: $passCount / $totalTests                                                     │
"@

if ($passCount -eq $totalTests) {
    Write-Host "║  Status: ✅ ALL FIXES VALIDATED                                         │"
    Write-Host "║  Recommendation: Ready for production deployment                         │"
} elseif ($passCount -ge 4) {
    Write-Host "║  Status: ⚠️  MOSTLY GOOD (Minor issues to investigate)                   │"
    Write-Host "║  Recommendation: Review Logcat, may need iterative fixes                 │"
} else {
    Write-Host "║  Status: ❌ MULTIPLE ISSUES DETECTED                                     │"
    Write-Host "║  Recommendation: Do NOT deploy; escalate to dev team                     │"
}

Write-Host @"
║                                                                                │
║  📁 Full Logcat: $logFile │
║                                                                                │
╚════════════════════════════════════════════════════════════════════════════════╝
"@

Pop-Location

# Exit code reflects pass/fail
exit $(if ($passCount -eq $totalTests) { 0 } else { 1 })

