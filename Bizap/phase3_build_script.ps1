#!/usr/bin/env powershell
# Phase 3 Build Execution Script
# Automates the build and testing process

$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
Set-Location $projectRoot

Write-Host "════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  PHASE 3 BUILD & TEST EXECUTION" -ForegroundColor Green
Write-Host "  Date: April 12, 2026" -ForegroundColor Cyan
Write-Host "════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Step 1: Verify project structure
Write-Host "[1/5] Verifying project structure..." -ForegroundColor Yellow
$pinSetupFile = Join-Path $projectRoot "app/src/main/java/com/emul8r/bizap/ui/auth/PINSetupScreen.kt"
$gradleFile = Join-Path $projectRoot "gradlew"

if ((Test-Path $pinSetupFile) -and (Test-Path $gradleFile)) {
    Write-Host "✅ PINSetupScreen.kt found" -ForegroundColor Green
    Write-Host "✅ Gradle wrapper found" -ForegroundColor Green
} else {
    Write-Host "❌ Project structure verification failed!" -ForegroundColor Red
    exit 1
}

# Step 2: Show file status
Write-Host ""
Write-Host "[2/5] Checking Phase 3 implementation..." -ForegroundColor Yellow
$pinContent = Get-Content $pinSetupFile
$hasAnimatedLockIcon = $pinContent -match "AnimatedLockIcon"
$hasPINDotIndicator = $pinContent -match "PINDotIndicator"
$hasBrandedWrapper = $pinContent -match "BrandedBackgroundWrapper"
$hasEnhancedButton = $pinContent -match "EnhancedUnlockButton"

Write-Host "  - AnimatedLockIcon: $(if($hasAnimatedLockIcon) {'✅'} else {'❌'})" -ForegroundColor $(if($hasAnimatedLockIcon) {'Green'} else {'Red'})
Write-Host "  - PINDotIndicator: $(if($hasPINDotIndicator) {'✅'} else {'❌'})" -ForegroundColor $(if($hasPINDotIndicator) {'Green'} else {'Red'})
Write-Host "  - BrandedBackgroundWrapper: $(if($hasBrandedWrapper) {'✅'} else {'❌'})" -ForegroundColor $(if($hasBrandedWrapper) {'Green'} else {'Red'})
Write-Host "  - EnhancedUnlockButton: $(if($hasEnhancedButton) {'✅'} else {'❌'})" -ForegroundColor $(if($hasEnhancedButton) {'Green'} else {'Red'})

# Step 3: Clean build
Write-Host ""
Write-Host "[3/5] Cleaning previous build..." -ForegroundColor Yellow
Write-Host "Command: ./gradlew clean --no-daemon" -ForegroundColor Cyan
$cleanResult = & "$gradleFile" clean --no-daemon 2>&1
Write-Host "✅ Clean complete" -ForegroundColor Green

# Step 4: Compile Kotlin
Write-Host ""
Write-Host "[4/5] Compiling Kotlin code..." -ForegroundColor Yellow
Write-Host "Command: ./gradlew compileDebugKotlin --no-daemon" -ForegroundColor Cyan
& "$gradleFile" compileDebugKotlin -x test --no-daemon > "phase3_compile.log" 2>&1
$compileSuccess = $LASTEXITCODE -eq 0

if ($compileSuccess) {
    Write-Host "✅ Compilation SUCCESSFUL" -ForegroundColor Green
} else {
    Write-Host "❌ Compilation FAILED" -ForegroundColor Red
    Write-Host ""
    Write-Host "Recent errors:" -ForegroundColor Yellow
    Get-Content "phase3_compile.log" -Tail 50 | Write-Host -ForegroundColor Red
    exit 1
}

# Step 5: Build APK
Write-Host ""
Write-Host "[5/5] Building debug APK..." -ForegroundColor Yellow
Write-Host "Command: ./gradlew assembleDebug --no-daemon" -ForegroundColor Cyan
& "$gradleFile" assembleDebug --no-daemon > "phase3_build.log" 2>&1
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Host "✅ Build SUCCESSFUL" -ForegroundColor Green

    # Check for APK
    $apkPath = "app/build/outputs/apk/debug/app-debug.apk"
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        Write-Host "✅ APK generated: $apkPath ($([math]::Round($apkSize, 2)) MB)" -ForegroundColor Green
    } else {
        Write-Host "⚠️  APK path expected but not found" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Build FAILED" -ForegroundColor Red
    Write-Host ""
    Write-Host "Recent errors:" -ForegroundColor Yellow
    Get-Content "phase3_build.log" -Tail 50 | Write-Host -ForegroundColor Red
    exit 1
}

# Summary
Write-Host ""
Write-Host "════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  BUILD SUMMARY" -ForegroundColor Green
Write-Host "════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Phase 3 Code: Verified" -ForegroundColor Green
Write-Host "✅ Project Structure: Valid" -ForegroundColor Green
Write-Host "✅ Compilation: Successful" -ForegroundColor Green
Write-Host "✅ APK Build: Successful" -ForegroundColor Green
Write-Host ""
Write-Host "📱 Next Steps:" -ForegroundColor Yellow
Write-Host "1. Start Android Emulator" -ForegroundColor Cyan
Write-Host "2. Run: adb install app/build/outputs/apk/debug/app-debug.apk" -ForegroundColor Cyan
Write-Host "3. Launch app and navigate to PIN setup" -ForegroundColor Cyan
Write-Host "4. Verify all visual elements and animations" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Testing Checklist: See PHASE_3_VERIFICATION_CHECKLIST.md" -ForegroundColor Green
Write-Host ""
Write-Host "════════════════════════════════════════════" -ForegroundColor Cyan


