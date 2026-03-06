#!/usr/bin/env pwsh

Write-Host "🔧 Starting Clean Build Process..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# Change to Bizap directory
Set-Location "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

Write-Host "📍 Current Location: $(Get-Location)" -ForegroundColor Yellow

# Stop any running gradle daemons
Write-Host "`n🛑 Stopping Gradle daemons..." -ForegroundColor Yellow
./gradlew --stop

# Clear local gradle cache
Write-Host "`n🗑️  Clearing local .gradle directory..." -ForegroundColor Yellow
if (Test-Path ".gradle") {
    Remove-Item -Recurse -Force ".gradle" -ErrorAction SilentlyContinue
    Write-Host "✅ Local .gradle cleared" -ForegroundColor Green
}

# Clear global gradle cache
Write-Host "`n🗑️  Clearing global Gradle cache..." -ForegroundColor Yellow
$gradleCache = "$env:USERPROFILE\.gradle\caches"
if (Test-Path $gradleCache) {
    Remove-Item -Recurse -Force $gradleCache -ErrorAction SilentlyContinue
    Write-Host "✅ Global cache cleared" -ForegroundColor Green
}

# Run clean build
Write-Host "`n🏗️  Starting build (this may take 2-5 minutes)..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$startTime = Get-Date
./gradlew clean build --refresh-dependencies
$exitCode = $LASTEXITCODE
$endTime = Get-Date

Write-Host "`n================================================" -ForegroundColor Cyan
$duration = ($endTime - $startTime).TotalSeconds
Write-Host "⏱️  Build completed in $([Math]::Round($duration, 2)) seconds" -ForegroundColor Cyan

if ($exitCode -eq 0) {
    Write-Host "✅ BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "`n📦 APK Location:" -ForegroundColor Green
    Write-Host "app/build/outputs/apk/debug/app-debug.apk" -ForegroundColor Cyan
} else {
    Write-Host "❌ BUILD FAILED (Exit Code: $exitCode)" -ForegroundColor Red
    Write-Host "`n📋 For more details, check build logs:" -ForegroundColor Yellow
    Write-Host "run: ./gradlew clean build --stacktrace" -ForegroundColor Cyan
}

exit $exitCode

