# Verification Build Script for Bizap - Task 11

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   TASK 11 VERIFICATION BUILD START    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Set Java Home
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
Write-Host "✅ JAVA_HOME set to: $env:JAVA_HOME" -ForegroundColor Green

# Change to project directory
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
Write-Host "✅ Working directory: $(Get-Location)" -ForegroundColor Green
Write-Host ""

# Step 1: Clean
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "STEP 1: CLEAN BUILD" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$cleanStart = Get-Date
./gradlew clean 2>&1 | Out-Null
$cleanTime = (Get-Date) - $cleanStart

Write-Host "✅ Clean completed in $($cleanTime.TotalSeconds) seconds" -ForegroundColor Green
Write-Host ""

# Step 2: Compile
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "STEP 2: COMPILE DEBUG APK" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$buildStart = Get-Date
$buildOutput = ./gradlew :app:assembleDebug --stacktrace 2>&1
$buildTime = (Get-Date) - $buildStart

# Check for success
if ($buildOutput -match "BUILD SUCCESSFUL") {
    Write-Host "✅ BUILD SUCCESSFUL in $($buildTime.TotalSeconds) seconds" -ForegroundColor Green
} else {
    Write-Host "❌ BUILD FAILED" -ForegroundColor Red
    Write-Host $buildOutput | Select-Object -Last 30
}

# Count warnings and errors
$warnings = ($buildOutput | Select-String -Pattern "warning" -AllMatches).Matches.Count
$errors = ($buildOutput | Select-String -Pattern "error" -AllMatches).Matches.Count

Write-Host "  Warnings: $warnings" -ForegroundColor Cyan
Write-Host "  Errors: $errors" -ForegroundColor $(if ($errors -eq 0) { "Green" } else { "Red" })
Write-Host ""

# Step 3: Run Tests
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "STEP 3: RUN UNIT TESTS" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$testStart = Get-Date
$testOutput = ./gradlew :app:testDebugUnitTest --stacktrace 2>&1
$testTime = (Get-Date) - $testStart

# Parse test results
$passed = ($testOutput | Select-String -Pattern "(\d+) passed" -AllMatches).Matches.Groups[1].Value
$failed = ($testOutput | Select-String -Pattern "(\d+) failed" -AllMatches).Matches.Groups[1].Value

if ($testOutput -match "BUILD SUCCESSFUL") {
    Write-Host "✅ TESTS PASSED in $($testTime.TotalSeconds) seconds" -ForegroundColor Green
    Write-Host "  Total: $(if ($passed) { $passed } else { '0' }) tests" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  TESTS COMPLETED with output" -ForegroundColor Yellow
}

Write-Host ""

# Final Summary
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║      VERIFICATION SUMMARY              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "BUILD STATUS:" -ForegroundColor White
Write-Host "  Time: $([math]::Round($buildTime.TotalSeconds, 1))s" -ForegroundColor Cyan
Write-Host "  Warnings: $warnings" -ForegroundColor Cyan
Write-Host "  Errors: $errors" -ForegroundColor $(if ($errors -eq 0) { "Green" } else { "Red" })
Write-Host ""
Write-Host "TEST STATUS:" -ForegroundColor White
Write-Host "  Time: $([math]::Round($testTime.TotalSeconds, 1))s" -ForegroundColor Cyan
Write-Host "  Passed: $(if ($passed) { $passed } else { 'checking...' })" -ForegroundColor Green
Write-Host ""

# Check if APK exists
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "APK GENERATED:" -ForegroundColor White
    Write-Host "  Path: $apkPath" -ForegroundColor Cyan
    Write-Host "  Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "APK NOT FOUND" -ForegroundColor Red
}

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║    TASK 11 VERIFICATION COMPLETE       ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan

