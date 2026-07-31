# ===============================================================================
# MEASURE-STRICTMODE-VIOLATIONS.ps1
# ===============================================================================
# Purpose: Monitor StrictMode violations on running emulator/device
# Usage: .\measure-strictmode-violations.ps1
#
# This script:
# 1. Launches the app on connected emulator/device
# 2. Captures StrictMode violations for 90 seconds while performing PIN operations
# 3. Analyzes and reports disk read/write patterns
# 4. Exports results to STRICTMODE_REPORT_April18.txt
#
# Prerequisites:
# - Android emulator or device connected via adb
# - App already deployed (from previous: adb install -r app-debug.apk)
# - Timber logging configured
# ===============================================================================

$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$reportFile = "$projectRoot\STRICTMODE_REPORT_April18.txt"
$logFile = "$projectRoot\strictmode-logcat.txt"

Write-Host "================================" -ForegroundColor Green
Write-Host "STRICTMODE VIOLATION MEASUREMENT" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check device connectivity
Write-Host "✓ Checking device connectivity..." -ForegroundColor Cyan
$devices = adb devices | Select-String "device$" | Measure-Object | Select-Object -ExpandProperty Count
if ($devices -eq 0) {
    Write-Host "✗ No devices found. Start emulator first." -ForegroundColor Red
    exit 1
}
Write-Host "  Found $devices device(s) connected" -ForegroundColor Green

# Step 2: Launch app
Write-Host ""
Write-Host "✓ Launching app..." -ForegroundColor Cyan
adb shell am start -n "com.emul8r.bizap/.MainActivity" -W | Out-Null
Start-Sleep -Seconds 2

# Step 3: Clear existing logs
Write-Host ""
Write-Host "✓ Clearing logcat buffer..." -ForegroundColor Cyan
adb logcat -c

# Step 4: Start capturing StrictMode violations
Write-Host ""
Write-Host "✓ Capturing StrictMode violations for 90 seconds..." -ForegroundColor Cyan
Write-Host "  (Ensure app navigates to PIN setup during this time)" -ForegroundColor Yellow
Write-Host ""

$captureJob = @"
`$output = @()
adb logcat -s "StrictMode" -v time 2>&1 | ForEach-Object {
    `$output += `$_
    Write-Host `$_ -ForegroundColor Yellow
}
`$output | Out-File -FilePath '$logFile' -Encoding UTF8
"@

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
$violations = @()
$startTime = Get-Date

# Capture logcat in real-time for 90 seconds
$logProcess = Start-Process -FilePath "powershell.exe" -ArgumentList "-Command", "adb logcat -s `"StrictMode`" -v time" `
    -NoNewWindow -PassThru -RedirectStandardOutput $logFile

Start-Sleep -Seconds 90
Stop-Process -InputObject $logProcess -ErrorAction SilentlyContinue

$stopwatch.Stop()

# Step 5: Analyze violations
Write-Host ""
Write-Host "✓ Analyzing violations..." -ForegroundColor Cyan

$violations = Select-String "DiskWrite|DiskRead" $logFile -ErrorAction SilentlyContinue | Measure-Object | Select-Object -ExpandProperty Count
$diskWrites = Select-String "DiskWrite" $logFile -ErrorAction SilentlyContinue | Measure-Object | Select-Object -ExpandProperty Count
$diskReads = Select-String "DiskRead" $logFile -ErrorAction SilentlyContinue | Measure-Object | Select-Object -ExpandProperty Count

# Step 6: Generate report
Write-Host ""
Write-Host "✓ Generating report..." -ForegroundColor Cyan

$report = @"
================================================================================
STRICTMODE VIOLATION ANALYSIS REPORT
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Test Duration: $($stopwatch.TotalSeconds) seconds
================================================================================

SUMMARY
-------
Total Violations:     $violations
  - Disk Writes:      $diskWrites
  - Disk Reads:       $diskReads

INTERPRETATION
--------------
If violations > 5:
  → PINStorage is blocking the main thread (CONFIRMED)
  → Recommend: Migrate to DataStore (async, non-blocking)

If violations = 0:
  → No StrictMode violations detected (UNLIKELY - PIN uses SharedPreferences.apply())
  → Recommendation: Either StrictMode not enabled or penalty not triggered

Expected Culprits (if violations detected):
  1. PINStorage.setupPIN() → prefs.edit().apply()
  2. PINStorage.verifyPIN() → prefs.getString() + prefs.contains()
  3. PINStorage.clearPIN() → prefs.edit().apply()
  4. BizapApplication.performAsyncInitialization() → prefs.getBoolean() / .apply()

NEXT STEPS
----------
1. Review full logcat output in: $logFile
2. If violations confirmed:
   a) Run Phase 1B migration plan
   b) Create PINDataStore async replacement
   c) Re-run this test to verify violation count drops to ~0

TECHNICAL DETAILS
-----------------
StrictMode Configuration (BizapApplication.kt):
  - detectDiskReads()   → Flags SharedPreferences.getString(), .contains()
  - detectDiskWrites()  → Flags SharedPreferences.edit().apply()
  - detectNetwork()     → Flags network operations
  - penaltyLog()        → Logs to logcat (doesn't crash)

Each violation appears in logcat as:
  [StrictMode] detected <operation>

Full violations log:
$(if (Test-Path $logFile) { Get-Content $logFile | Select-Object -First 20 } else { "No violations captured" })
...

================================================================================
"@

$report | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host "✓ Report saved to: $reportFile" -ForegroundColor Green

Write-Host ""
Write-Host "KEY RESULTS:" -ForegroundColor Cyan
Write-Host "  Total violations: $violations" -ForegroundColor Yellow
Write-Host "  Disk writes:      $diskWrites" -ForegroundColor Yellow
Write-Host "  Disk reads:       $diskReads" -ForegroundColor Yellow

Write-Host ""
Write-Host "NEXT:" -ForegroundColor Green
Write-Host "  1. Review: Get-Content '$reportFile'" -ForegroundColor Cyan
Write-Host "  2. View full logcat: Get-Content '$logFile'" -ForegroundColor Cyan
Write-Host "  3. If violations confirmed, proceed with Phase 1B (DataStore migration)" -ForegroundColor Cyan -NoNewline

Write-Host ""


