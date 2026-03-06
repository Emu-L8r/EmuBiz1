#!/usr/bin/env pwsh

Write-Host "🔧 FINAL BUILD FIX - AGP 8.5.0 + Hilt 2.46" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$bizapPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
Set-Location $bizapPath

# Step 1: Kill all Gradle daemons
Write-Host "`n1️⃣  Killing all Gradle daemons..." -ForegroundColor Yellow
& ./gradlew --stop 2>&1 | Out-Null

# Step 2: Wait for cleanup
Start-Sleep -Seconds 3

# Step 3: Clear local gradle cache
Write-Host "2️⃣  Clearing local Gradle cache..." -ForegroundColor Yellow
Remove-Item -Recurse -Force ".gradle" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "app\.gradle" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "build" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "app\build" -ErrorAction SilentlyContinue

# Step 4: Run clean build
Write-Host "`n3️⃣  Running clean assembleDebug with AGP 8.5.0 + Hilt 2.46..." -ForegroundColor Cyan
$startTime = Get-Date
& ./gradlew clean assembleDebug --no-build-cache 2>&1 | Tee-Object -FilePath "final_build_attempt.log"
$buildExit = $LASTEXITCODE
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "⏱️  Build completed in $duration seconds" -ForegroundColor Yellow

if ($buildExit -eq 0) {
    Write-Host "✅ BUILD SUCCESSFUL!" -ForegroundColor Green
    if (Test-Path "app/build/outputs/apk/debug/app-debug.apk") {
        $apkSize = (Get-Item "app/build/outputs/apk/debug/app-debug.apk").Length / 1MB
        Write-Host "📦 APK created: $([Math]::Round($apkSize, 2)) MB" -ForegroundColor Green
    }
} else {
    Write-Host "❌ BUILD FAILED (Exit Code: $buildExit)" -ForegroundColor Red
    Write-Host "`n📋 Last 50 lines of log:" -ForegroundColor Yellow
    Get-Content "final_build_attempt.log" -Tail 50 -ErrorAction SilentlyContinue
}

exit $buildExit



