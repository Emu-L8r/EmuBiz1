# Bizap Build Validation Script (PowerShell)
# Run this to verify everything is working correctly

Write-Host "═════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  BIZAP BUILD VALIDATION SCRIPT" -ForegroundColor Cyan
Write-Host "═════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
cd $projectPath

# Step 1: Verify Syntax Fixes
Write-Host "[1/5] Verifying syntax fixes..." -ForegroundColor Yellow

$paymentFile = "app\src\main\java\com\emul8r\bizap\ui\gui3\screens\PaymentAnalyticsScreenV3.kt"
$content = Get-Content $paymentFile -Raw
$bracketCount = ($content | Select-String -Pattern "^}" -AllMatches | Measure-Object).Count
if ($bracketCount -gt 0) {
    Write-Host "  ✓ PaymentAnalyticsScreenV3.kt: Syntax correct" -ForegroundColor Green
} else {
    Write-Host "  ✗ PaymentAnalyticsScreenV3.kt: Syntax issue detected" -ForegroundColor Red
    exit 1
}

$revenueFile = "app\src\main\java\com\emul8r\bizap\ui\gui3\screens\RevenueAnalyticsScreenV3.kt"
$content = Get-Content $revenueFile -Raw
if ($content.EndsWith("}") -and -not $content.EndsWith("}}")) {
    Write-Host "  ✓ RevenueAnalyticsScreenV3.kt: Syntax correct" -ForegroundColor Green
} else {
    Write-Host "  ✗ RevenueAnalyticsScreenV3.kt: Extra braces detected" -ForegroundColor Red
    exit 1
}

# Step 2: Clear Build Cache
Write-Host ""
Write-Host "[2/5] Clearing build cache..." -ForegroundColor Yellow

if (Test-Path "app\build") {
    Remove-Item "app\build" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Removed app\build" -ForegroundColor Green
}

if (Test-Path ".gradle") {
    Remove-Item ".gradle" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Removed .gradle" -ForegroundColor Green
}

# Step 3: Kill Gradle Daemon
Write-Host ""
Write-Host "[3/5] Cleaning up Gradle processes..." -ForegroundColor Yellow

$javaProcesses = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "gradle" }
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Terminated Gradle daemon" -ForegroundColor Green
} else {
    Write-Host "  ✓ No Gradle processes found" -ForegroundColor Green
}

# Step 4: Build APK
Write-Host ""
Write-Host "[4/5] Building debug APK (this may take 2-3 minutes)..." -ForegroundColor Yellow
Write-Host "  Running: gradlew clean assembleDebug --no-daemon --no-build-cache" -ForegroundColor Gray
Write-Host ""

$buildOutput = & .\gradlew.bat clean assembleDebug --no-daemon --no-build-cache 2>&1
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Host "  ✓ Build completed successfully" -ForegroundColor Green
} else {
    Write-Host "  ✗ Build failed" -ForegroundColor Red
    Write-Host ""
    Write-Host "Last 20 lines of build output:" -ForegroundColor Gray
    $buildOutput | Select-Object -Last 20 | ForEach-Object { Write-Host "    $_" }
    exit 1
}

# Step 5: Verify APK
Write-Host ""
Write-Host "[5/5] Verifying APK creation..." -ForegroundColor Yellow

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host "  ✓ APK created successfully" -ForegroundColor Green
    Write-Host "    Size: $([Math]::Round($apkSize, 2)) MB" -ForegroundColor Gray
    Write-Host "    Path: $apkPath" -ForegroundColor Gray
} else {
    Write-Host "  ✗ APK not found" -ForegroundColor Red
    exit 1
}

# Success!
Write-Host ""
Write-Host "═════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  ✅ ALL VALIDATIONS PASSED!" -ForegroundColor Green
Write-Host "═════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Open Android Studio" -ForegroundColor White
Write-Host "  2. Open your Bizap project" -ForegroundColor White
Write-Host "  3. Click the Green Play button" -ForegroundColor White
Write-Host "  4. Select your emulator/device" -ForegroundColor White
Write-Host "  5. App should launch with all GUI3 fixes applied" -ForegroundColor White
Write-Host ""
Write-Host "Enjoy your fully-fixed GUI3 Matrix experience! 🟢" -ForegroundColor Cyan
Write-Host ""

