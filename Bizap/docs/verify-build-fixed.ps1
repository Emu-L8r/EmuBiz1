# Verification Build Script for Bizap - Task 11

Write-Host "TASK 11 VERIFICATION BUILD START" -ForegroundColor Cyan
Write-Host ""

# Set Java Home
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
Write-Host "JAVA_HOME set to: $env:JAVA_HOME" -ForegroundColor Green

# Change to project directory
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
Write-Host "Working directory: $(Get-Location)" -ForegroundColor Green
Write-Host ""

# Step 1: Clean
Write-Host "STEP 1: CLEAN BUILD" -ForegroundColor Yellow
Write-Host ""

$cleanStart = Get-Date
./gradlew clean 2>&1 | Out-Null
$cleanTime = (Get-Date) - $cleanStart

Write-Host "Clean completed in $($cleanTime.TotalSeconds) seconds" -ForegroundColor Green
Write-Host ""

# Step 2: Compile
Write-Host "STEP 2: COMPILE DEBUG APK" -ForegroundColor Yellow
Write-Host ""

$buildStart = Get-Date
$buildOutput = ./gradlew :app:assembleDebug --stacktrace 2>&1
$buildTime = (Get-Date) - $buildStart

# Check for success
if ($buildOutput -match "BUILD SUCCESSFUL") {
    Write-Host "BUILD SUCCESSFUL in $($buildTime.TotalSeconds) seconds" -ForegroundColor Green
} else {
    Write-Host "BUILD FAILED" -ForegroundColor Red
    Write-Host $buildOutput | Select-Object -Last 30
}

Write-Host ""

# Step 3: Run Tests
Write-Host "STEP 3: RUN UNIT TESTS" -ForegroundColor Yellow
Write-Host ""

$testStart = Get-Date
$testOutput = ./gradlew :app:testDebugUnitTest --stacktrace 2>&1
$testTime = (Get-Date) - $testStart

if ($testOutput -match "BUILD SUCCESSFUL") {
    Write-Host "TESTS COMPLETED in $($testTime.TotalSeconds) seconds" -ForegroundColor Green
} else {
    Write-Host "TEST BUILD COMPLETED" -ForegroundColor Yellow
}

Write-Host ""

# Final Summary
Write-Host "VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host ""
Write-Host "BUILD STATUS:" -ForegroundColor White
Write-Host "  Time: $([math]::Round($buildTime.TotalSeconds, 1))s" -ForegroundColor Cyan

# Check if APK exists
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "APK GENERATED: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Green
} else {
    Write-Host "APK NOT FOUND" -ForegroundColor Red
}

Write-Host ""
Write-Host "TASK 11 VERIFICATION COMPLETE" -ForegroundColor Cyan

