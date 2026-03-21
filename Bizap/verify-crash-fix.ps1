#!/usr/bin/env powershell
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# SIMPLE VERIFICATION: Has crash fix worked?
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Write-Host "════════════════════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "PHASE 2.5 VERIFICATION: Crash Fix Status" -ForegroundColor Cyan
Write-Host "════════════════════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$PROJECT_ROOT = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$APK_PATH = "$PROJECT_ROOT\app\build\outputs\apk\debug\app-debug.apk"

# ────────────────────────────────────────────────────────────────────────────────
# CHECK 1: Build Status
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "1️⃣  BUILD STATUS" -ForegroundColor Yellow
Write-Host "─" * 80

if (Test-Path "$PROJECT_ROOT\.gradle\tasks.cache") {
    Write-Host "   ✅ Gradle cache exists" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Gradle cache not found" -ForegroundColor Yellow
}

if (Test-Path "$PROJECT_ROOT\app\build") {
    Write-Host "   ✅ Build directory exists" -ForegroundColor Green
} else {
    Write-Host "   ❌ Build directory not found" -ForegroundColor Red
}

Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# CHECK 2: APK Status
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "2️⃣  APK STATUS" -ForegroundColor Yellow
Write-Host "─" * 80

if (Test-Path $APK_PATH) {
    $size = (Get-Item $APK_PATH).Length / 1MB
    Write-Host "   ✅ APK exists: $([Math]::Round($size, 1)) MB" -ForegroundColor Green
    Write-Host "   📍 Location: $APK_PATH" -ForegroundColor Cyan
} else {
    Write-Host "   ❌ APK NOT FOUND" -ForegroundColor Red
    Write-Host "   📍 Expected at: $APK_PATH" -ForegroundColor Red
}

Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# CHECK 3: Code Status
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "3️⃣  CODE STATUS (Crash Fix)" -ForegroundColor Yellow
Write-Host "─" * 80

$settingsVMFile = "$PROJECT_ROOT\app\src\main\java\com\emul8r\bizap\ui\settings\SettingsViewModel.kt"
$goodSettingsVMFile = "$PROJECT_ROOT\app\src\main\java\com\emul8r\bizap\presentation\viewmodel\SettingsViewModel.kt"

if (-not (Test-Path $settingsVMFile)) {
    Write-Host "   ✅ Conflicting ui/settings/SettingsViewModel.kt DELETED" -ForegroundColor Green
} else {
    Write-Host "   ❌ Conflicting file still exists!" -ForegroundColor Red
}

if (Test-Path $goodSettingsVMFile) {
    Write-Host "   ✅ Correct presentation/viewmodel/SettingsViewModel.kt EXISTS" -ForegroundColor Green
} else {
    Write-Host "   ❌ Correct SettingsViewModel.kt NOT FOUND" -ForegroundColor Red
}

Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# CHECK 4: Git Status
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "4️⃣  GIT STATUS" -ForegroundColor Yellow
Write-Host "─" * 80

Push-Location $PROJECT_ROOT
$commits = & git log --oneline -5 2>&1

Write-Host "   Recent commits:" -ForegroundColor Cyan
$commits | ForEach-Object {
    if ($_ -match "crash|fix|SettingsViewModel") {
        Write-Host "   ✅ $_" -ForegroundColor Green
    } else {
        Write-Host "   📝 $_" -ForegroundColor Gray
    }
}

Pop-Location

Write-Host ""

# ────────────────────────────────────────────────────────────────────────────────
# SUMMARY
# ────────────────────────────────────────────────────────────────────────────────

Write-Host "════════════════════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "════════════════════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$allGood = ((Test-Path $APK_PATH) -and (-not (Test-Path $settingsVMFile)) -and (Test-Path $goodSettingsVMFile))

if ($allGood) {
    Write-Host "✅ ALL CHECKS PASSED - Crash fix is in place!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next step: Run the app on emulator/device" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Manual steps:" -ForegroundColor Yellow
    Write-Host "  1. adb shell pm clear com.emul8r.bizap" -ForegroundColor Gray
    Write-Host "  2. adb install -r app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Gray
    Write-Host "  3. adb shell am start -n com.emul8r.bizap/.MainActivity" -ForegroundColor Gray
    Write-Host "  4. Wait 10 seconds" -ForegroundColor Gray
    Write-Host "  5. adb logcat -d -s AndroidRuntime:E" -ForegroundColor Gray
    Write-Host "  6. If empty = SUCCESS ✅" -ForegroundColor Gray
} else {
    Write-Host "❌ SOME CHECKS FAILED - See details above" -ForegroundColor Red
    Write-Host ""
    Write-Host "Issues found:" -ForegroundColor Yellow
    if (-not (Test-Path $APK_PATH)) {
        Write-Host "  - APK not created (rebuild needed)" -ForegroundColor Red
    }
    if (Test-Path $settingsVMFile) {
        Write-Host "  - Conflicting file still exists (delete needed)" -ForegroundColor Red
    }
    if (-not (Test-Path $goodSettingsVMFile)) {
        Write-Host "  - Correct SettingsViewModel missing (rebuild needed)" -ForegroundColor Red
    }
}

Write-Host ""

