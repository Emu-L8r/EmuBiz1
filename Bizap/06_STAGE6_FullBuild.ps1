# Stage 6: Full Build + Tests (Comprehensive Regression Test)
# Execution Time: 2-3 minutes
# Device Required: NO
# Purpose: Complete build with all unit tests to catch regressions

Write-Output "=========================================="
Write-Output "STAGE 6: FULL BUILD + TESTS"
Write-Output "=========================================="
Write-Output "Comprehensive Regression Test"
Write-Output ""
Write-Output "This stage:"
Write-Output "  - Cleans all build artifacts"
Write-Output "  - Recompiles entire project"
Write-Output "  - Runs all 102 unit tests"
Write-Output "  - Verifies no regressions"
Write-Output ""

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "stage6_full_build_${timestamp}.log"
$summaryFile = "stage6_summary_${timestamp}.txt"

cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

Write-Output "⏱️  Starting full build at: $(Get-Date)"
Write-Output ""

# Run clean build
$output = ./gradlew clean build 2>&1

# Save full output
$output | Out-File -FilePath $logFile -Encoding UTF8

# Display output
Write-Output $output

Write-Output ""
Write-Output "=========================================="
Write-Output "BUILD ANALYSIS"
Write-Output "=========================================="
Write-Output ""

# Parse results
$summary = @()

if ($output -match "BUILD SUCCESSFUL") {
    Write-Output "✅ BUILD SUCCESSFUL"
    $summary += "BUILD: SUCCESSFUL"
} else {
    Write-Output "❌ BUILD FAILED"
    $summary += "BUILD: FAILED"
}

# Extract test results
if ($output -match "(\d+) tests? completed") {
    $testsCompleted = [int]$matches[1]
    Write-Output "✅ Tests completed: $testsCompleted"
    $summary += "TESTS_COMPLETED: $testsCompleted"
}

if ($output -match "(\d+) passed") {
    $testsPassed = [int]$matches[1]
    Write-Output "✅ Tests passed: $testsPassed"
    $summary += "TESTS_PASSED: $testsPassed"
}

if ($output -match "(\d+) failed") {
    Write-Output "❌ Tests failed: $($matches[1])"
    $summary += "TESTS_FAILED: $($matches[1])"
}

# Check build time
if ($output -match "BUILD SUCCESSFUL in (.+)") {
    Write-Output "✅ Build time: $($matches[1])"
    $summary += "BUILD_TIME: $($matches[1])"
}

# Check APK size
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Output "✅ APK size: {0:F2} MB" -f $apkSize
    $summary += "APK_SIZE: {0:F2}MB" -f $apkSize

    if ($apkSize -gt 55) {
        Write-Output "⚠️  WARNING: APK larger than baseline (> 55 MB)"
        $summary += "APK_WARNING: Size exceeds baseline"
    }
} else {
    Write-Output "❌ APK not found"
    $summary += "APK_NOT_FOUND"
}

# Check for warnings
$warningCount = ($output | Select-String -Pattern "warning" | Measure-Object).Count
if ($warningCount -gt 0) {
    Write-Output "⚠️  Build warnings: $warningCount (may be expected)"
    $summary += "WARNINGS: $warningCount"
}

# Check for errors
$errorCount = ($output | Select-String -Pattern "error:" | Measure-Object).Count
if ($errorCount -gt 0) {
    Write-Output "❌ Build errors: $errorCount"
    $summary += "ERRORS: $errorCount"
} else {
    Write-Output "✅ No build errors"
    $summary += "ERRORS: 0"
}

Write-Output ""
Write-Output "=========================================="
Write-Output "REGRESSION DETECTION"
Write-Output "=========================================="
Write-Output ""

# Compare to baseline
$baselineTests = 102
$baselineSize = 52.3

if ($testsCompleted -eq $baselineTests) {
    Write-Output "✅ Test count matches baseline: $baselineTests"
} else {
    Write-Output "⚠️  Test count differs from baseline (expected: $baselineTests, got: $testsCompleted)"
}

if ($testsPassed -eq $baselineTests) {
    Write-Output "✅ All tests passing (baseline: $baselineTests)"
} else {
    Write-Output "❌ Tests not passing (baseline: $baselineTests, passed: $testsPassed)"
}

Write-Output ""
Write-Output "=========================================="
Write-Output "SUMMARY"
Write-Output "=========================================="
Write-Output ""

# Display summary
foreach ($item in $summary) {
    Write-Output $item
}

# Save summary
$summary | Out-File -FilePath $summaryFile -Encoding UTF8

Write-Output ""
Write-Output "📝 Full log: $logFile"
Write-Output "📝 Summary: $summaryFile"
Write-Output ""

# Final verdict
Write-Output "=========================================="
if ($output -match "BUILD SUCCESSFUL" -and $testsPassed -eq $baselineTests) {
    Write-Output "🎉 ALL VALIDATIONS PASSED!"
    Write-Output "✅ The app is production-ready"
} elseif ($output -match "BUILD SUCCESSFUL") {
    Write-Output "⚠️  BUILD SUCCESSFUL but tests may have issues"
    Write-Output "Review log file for details"
} else {
    Write-Output "❌ BUILD FAILED"
    Write-Output "Review log file: $logFile"
}
Write-Output "=========================================="

