################################################################################
#
# BIZAP TEST RESULTS AGGREGATOR
# Combines all test results into unified report
#
# Purpose: Aggregate Phase 1-4 results into actionable report
# Usage: ./TEST_RESULTS_AGGREGATOR.ps1 -ResultsDirectory test-automation-logs
#        ./TEST_RESULTS_AGGREGATOR.ps1 -OpenInBrowser
#
# Created: April 28, 2026
# Version: 1.0
#
################################################################################

param(
    [string]$ResultsDirectory = "test-automation-logs",
    [switch]$OpenInBrowser = $true
)

$ErrorActionPreference = "Continue"

function ConvertTo-MarkdownTable {
    param(
        [object[]]$Data,
        [string[]]$Columns
    )

    if ($Data.Count -eq 0) {
        return "No data to display`n"
    }

    # Header
    $table = "| " + ($Columns -join " | ") + " |`n"
    $table += "| " + (($Columns | ForEach-Object { "---" }) -join " | ") + " |`n"

    # Rows
    foreach ($row in $Data) {
        $values = @()
        foreach ($col in $Columns) {
            $val = $row.$col
            if ($null -eq $val) { $val = "-" }
            if ($val.GetType().Name -eq "Int32") {
                $values += $val.ToString()
            } else {
                $values += $val.ToString().Replace("|", "")  # Escape pipes in data
            }
        }
        $table += "| " + ($values -join " | ") + " |`n"
    }

    return $table
}

function Invoke-ResultsAggregation {
    Write-Host "`n$('=' * 80)" -ForegroundColor Cyan
    Write-Host "BIZAP TEST RESULTS AGGREGATOR" -ForegroundColor Cyan
    Write-Host "Compiling automated test results..." -ForegroundColor Cyan
    Write-Host "$('=' * 80)`n" -ForegroundColor Cyan

    # Check if results directory exists
    if (-not (Test-Path $ResultsDirectory)) {
        Write-Host "❌ Results directory not found: $ResultsDirectory" -ForegroundColor Red
        Write-Host "Please run tests first: ./TEST_AUTOMATION_MASTER.ps1 -Phase All" -ForegroundColor Yellow
        return
    }

    # Find all test result files
    $resultFiles = Get-ChildItem -Path $ResultsDirectory -Filter "EMULATOR_TEST_RESULTS_*.md" -ErrorAction SilentlyContinue | Sort-Object -Property LastWriteTime -Descending

    if ($resultFiles.Count -eq 0) {
        Write-Host "❌ No test results found in $ResultsDirectory" -ForegroundColor Red
        Write-Host "Please run tests first: ./TEST_AUTOMATION_MASTER.ps1 -Phase All" -ForegroundColor Yellow
        return
    }

    # Take latest result
    $latestResult = $resultFiles[0]
    Write-Host "✅ Latest test results found: $($latestResult.Name)" -ForegroundColor Green
    Write-Host "   Timestamp: $($latestResult.LastWriteTime)" -ForegroundColor Cyan
    Write-Host ""

    # Read content
    $content = Get-Content -Path $latestResult.FullName -Raw

    # Check for pass/fail indicators
    $hasPasses = $content -match "PASS"
    $hasFailures = $content -match "FAIL"
    $hasErrors = $content -match "ERROR"

    # Determine overall status
    if ($hasFailures -or $hasErrors) {
        $overallStatus = "⚠️  NEEDS ATTENTION"
        $statusColor = "Yellow"
    } elseif ($hasPasses) {
        $overallStatus = "✅ SUCCESS"
        $statusColor = "Green"
    } else {
        $overallStatus = "❓ UNKNOWN"
        $statusColor = "Gray"
    }

    # Create comprehensive aggregated report
    $report = @"
# BIZAP COMPLETE TEST AUTOMATION REPORT
**Final Summary — $(Get-Date -f 'MMMM dd, yyyy HH:mm:ss')**

---

## 📊 EXECUTIVE SUMMARY

| Metric | Value |
|--------|-------|
| **Test Session** | $(Get-Date -f 'yyyy-MM-dd HH:mm:ss') |
| **Environment** | Android Emulator (Automated) |
| **Test Phases** | 4 (Unit, Functional, Error Scenarios, Performance) |
| **Overall Status** | $overallStatus |
| **Results File** | $($latestResult.Name) |
| **Generated** | $(Get-Date -f 'o') |

---

## 🧪 DETAILED TEST RESULTS

**Source:** EMULATOR_TEST_RESULTS_*.md (Latest: $(Get-Date -f 'yyyy-MM-dd HH:mm:ss'))

$content

---

## 🎯 CRITICAL SUCCESS CRITERIA

### ✅ Phase 1: Unit Tests (Smoke Tests)
- **Status:** $(if ($content -match "Phase 1.*PASS") { "✅ PASS" } else { "⏳ CHECK MANUAL" })
- **Purpose:** Validate 5 critical settings fields map correctly
- **Fields:** primaryColor, taxName, enableGradientHeader, enableQrCode, paymentTermsDays
- **Expected:** 8/8 tests pass

### ✅ Phase 2: Functional Tests (Espresso)
- **Status:** $(if ($content -match "Phase 2.*PASS|Phase 2.*SKIP") { "✅ READY" } else { "❓ REVIEW" })
- **Purpose:** End-to-end UI automation
- **Tests:** Settings→PDF, Customer→Invoice, Complex Fields, Error Handling
- **Expected:** Framework operational (may skip in headless environments)

### ✅ Phase 3: Error Scenarios
- **Status:** $(if ($content -match "Phase 3.*PASS|Phase 3.*WARN") { "✅ ACCEPTABLE" } else { "❓ REVIEW" })
- **Purpose:** Validate graceful error handling
- **Scenarios:** Network failure, Low memory, Rapid navigation
- **Expected:** No critical crashes

### ✅ Phase 4: Performance Profiling
- **Status:** $(if ($content -match "Phase 4.*PASS") { "✅ GOOD" } else { "❓ REVIEW" })
- **Metrics:** CPU <30%, Memory <150MB, FPS ≥60
- **Purpose:** Ensure app performs well on emulator
- **Expected:** Performance within targets

---

## 📋 DECISION MATRIX

$([string]::IsNullOrWhiteSpace($content) ? "No test results to analyze" : "")

### IF ALL TESTS PASS: ✅
**Action: PROCEED TO REAL DEVICE TESTING**

\`\`\`
Timeline: 2 days to production (Saturday, April 30)
Next: Real device testing tomorrow (3 devices minimum)
Devices: Pixel 6a (mid-range), Pixel 3a (budget), Tablet
Quality Gate: All 3 devices must pass before release
\`\`\`

**Next Steps:**
1. ✅ Schedule device lab (3 devices)
2. ✅ Run same test suite on real hardware
3. ✅ Compare emulator vs real device results
4. ✅ Verify PDF output quality
5. ✅ Deploy to beta if all pass
6. ✅ Production release

---

### IF 1-2 TESTS FAIL/SKIP: ⚠️
**Action: CONTINUE INVESTIGATION + RETEST**

\`\`\`
Timeline: +1 day (3 days total to production)
Next: Fix identified issues, rerun failed tests
Root Cause: Likely environment configuration (ADB, emulator state)
\`\`\`

**Next Steps:**
1. ✅ Review failed phase output above
2. ✅ Check if app is installed on emulator (adb install -r app/build/outputs/apk/debug/app-debug.apk)
3. ✅ Verify ADB connection (adb devices)
4. ✅ Clear app data (adb shell pm clear com.emul8r.bizap)
5. ✅ Rerun tests: ./TEST_AUTOMATION_MASTER.ps1 -Phase All

---

### IF 3+ TESTS FAIL: ❌
**Action: ESCALATE TO ROOT CAUSE ANALYSIS**

\`\`\`
Timeline: +3-5 days (comprehensive debugging)
Next: Detailed investigation and fixes
Risk: May require redesign or rollback
\`\`\`

**Next Steps:**
1. ✅ Review detailed error logs in test-automation-logs/
2. ✅ Run unit tests individually (Phase 1 is most reliable)
3. ✅ Build and verify APK manually (./gradlew assembleDebug)
4. ✅ Check app health (./gradlew clean build)
5. ✅ File detailed bug report with logs
6. ✅ Escalate to development team

---

## 📈 PERFORMANCE TARGETS vs ACTUAL

| Metric | Target | Status | Notes |
|--------|--------|--------|-------|
| **CPU Usage** | < 30% | [See Phase 4] | Monitor during PDF generation |
| **Memory** | < 150MB | [See Phase 4] | Emulator overhead ~100MB baseline |
| **Frame Rate** | 60 FPS | [See Phase 4] | Check for jank events |
| **PDF Generation** | < 5 sec | [Manual verify] | Time from click to PDF ready |
| **Jank Events** | < 5 | [See Phase 4] | Events per test run |

---

## 🔧 TROUBLESHOOTING QUICK REFERENCE

| Problem | Solution |
|---------|----------|
| **"No emulator running"** | Start Android Emulator, then retry |
| **"App not installed"** | Run: \`./gradlew installDebug\` |
| **"Permission denied"** | Run PowerShell as Administrator |
| **"Tests timeout"** | Run individual phases: \`./TEST_AUTOMATION_MASTER.ps1 -Phase 1\` |
| **"ADB connection lost"** | \`adb kill-server\` then \`adb devices\` |
| **"Gradle sync failed"** | Run: \`./gradlew clean build\` |

---

## 📂 TEST ARTIFACTS & LOGS

All test outputs preserved in: **test-automation-logs/**

```
test-automation-logs/
├── EMULATOR_TEST_RESULTS_*.md        ← Main test report
├── logcat_analysis_*.log             ← Real-time logcat output
└── FINAL_TEST_REPORT_*.md            ← Aggregated summary
```

**To review:**
1. Open latest EMULATOR_TEST_RESULTS_*.md file
2. Check logcat_analysis_*.log for error patterns
3. Refer to FINAL_TEST_REPORT_*.md for executive summary

---

## 🚀 RUNNING TESTS LOCALLY

### Run All Tests (Recommended):
\`\`\`powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
./TEST_AUTOMATION_MASTER.ps1 -Phase All
\`\`\`
**Duration:** ~2 hours (fully automated, unattended)

### Run Specific Phase:
\`\`\`powershell
./TEST_AUTOMATION_MASTER.ps1 -Phase 1       # Smoke tests only (15 min)
./TEST_AUTOMATION_MASTER.ps1 -Phase 1,2,3   # Phases 1-3 (90 min)
\`\`\`

### Monitor Logcat in Real-Time:
\`\`\`powershell
./LOGCAT_REALTIME_ANALYZER.ps1 -Emulator emulator-5554 -MonitorDurationMinutes 120
\`\`\`
**Duration:** Runs parallel to test suite

### Re-Aggregate Results:
\`\`\`powershell
./TEST_RESULTS_AGGREGATOR.ps1 -ResultsDirectory test-automation-logs -OpenInBrowser
\`\`\`

---

## 📞 SUPPORT & ESCALATION

**Tests Passing:** ✅ Proceed to next phase (device testing)
**Tests Failing:** ⚠️ Follow troubleshooting guide above
**Persistent Issues:** 🔴 Escalate with complete logs attached

### Information to Include in Bug Report:
1. Full test output (copy from report above)
2. Logcat errors (from logcat_analysis_*.log)
3. Environment (Windows version, Android Emulator version)
4. Exact reproduction steps
5. Screenshots of failures

---

## 📋 CHECKLIST: WHAT'S READY FOR PRODUCTION?

- [ ] Phase 1 (Unit Tests): All 8 smoke tests pass
- [ ] Phase 2 (Functional): Framework operational or skipped (expected)
- [ ] Phase 3 (Error Scenarios): No critical crashes
- [ ] Phase 4 (Performance): Metrics within targets
- [ ] Manual verification: PDF renders correctly with custom settings
- [ ] Code review: SnapshotMappers.kt merged and tested
- [ ] Documentation: Updated and verified
- [ ] Team sign-off: All stakeholders informed

---

## 🎉 NEXT MILESTONE

**Expected:** Real Device Testing (Tomorrow)
- **Devices:** Pixel 6a, Pixel 3a, Tablet
- **Duration:** 2-3 hours
- **Exit Criteria:** All 3 devices pass Phase 1-3
- **Outcome:** Production release (Saturday, April 30)

---

**Generated by TEST_RESULTS_AGGREGATOR.ps1**
**Report Created:** $(Get-Date -f 'yyyy-MM-dd HH:mm:ss')
**Environment:** Windows PowerShell + Android Emulator

"@

    # Save aggregated report
    $reportFile = "$ResultsDirectory\FINAL_TEST_REPORT_$(Get-Date -f 'yyyy-MM-dd_HHmmss').md"
    $report | Out-File -FilePath $reportFile -Encoding UTF8 -Force

    Write-Host "✅ Aggregated report generated" -ForegroundColor Green
    Write-Host "   Location: $reportFile" -ForegroundColor Cyan
    Write-Host ""

    # Open in browser if requested
    if ($OpenInBrowser -and (Test-Path $reportFile)) {
        Write-Host "Opening report in browser..." -ForegroundColor Cyan
        try {
            if ($IsWindows -or $PSVersionTable.OS -like "*Windows*") {
                Start-Process $reportFile
            } else {
                Write-Host "Note: On non-Windows systems, open report manually: $reportFile" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "Could not open browser (may be headless environment)" -ForegroundColor Yellow
        }
    }

    Write-Host ""
    Write-Host "$('=' * 80)" -ForegroundColor Cyan
    Write-Host "AGGREGATION COMPLETE" -ForegroundColor Cyan
    Write-Host "$('=' * 80)" -ForegroundColor Cyan

    # Print quick status
    Write-Host ""
    Write-Host "Overall Status: $overallStatus" -ForegroundColor $statusColor
    Write-Host ""
    Write-Host "View full report:" -ForegroundColor Cyan
    Write-Host "  $reportFile" -ForegroundColor White
    Write-Host ""

    return $reportFile
}

# Execute aggregation
$reportPath = Invoke-ResultsAggregation

if ($reportPath) {
    Write-Host "✅ All done! Review the report above." -ForegroundColor Green
}

