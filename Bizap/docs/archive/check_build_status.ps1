#!/usr/bin/env pwsh
# Quick build status checker

$bizapPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
Set-Location $bizapPath

Write-Host "=== BUILD STATUS CHECK ===" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date)" -ForegroundColor Yellow

# Check for build directory
if (Test-Path "app/build") {
    Write-Host "✅ Build directory exists" -ForegroundColor Green
    $itemCount = (Get-ChildItem "app/build" -Recurse | Measure-Object).Count
    Write-Host "   Items in build: $itemCount" -ForegroundColor Green
}else {
    Write-Host "⏳ Build directory not yet created" -ForegroundColor Yellow
}

# Check for APK
if (Test-Path "app/build/outputs/apk/debug/app-debug.apk") {
    $apkSize = (Get-Item "app/build/outputs/apk/debug/app-debug.apk").Length / 1MB
    Write-Host "✅ APK CREATED!" -ForegroundColor Green
    Write-Host "   Size: $([Math]::Round($apkSize, 2)) MB" -ForegroundColor Green
    Write-Host "   Path: app/build/outputs/apk/debug/app-debug.apk" -ForegroundColor Green
} else {
    Write-Host "⏳ APK not yet created (build in progress)" -ForegroundColor Yellow
}

# Check gradle processes
$gradleProcesses = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -like "*gradle*" -or $_.CommandLine -like "*gradle*" }
if ($gradleProcesses) {
    Write-Host "🔄 Gradle daemon still running" -ForegroundColor Cyan
    Write-Host "   Processes: $($gradleProcesses.Count)" -ForegroundColor Cyan
} else {
    Write-Host "ℹ️  No Gradle daemon process found" -ForegroundColor Gray
}

# Check last log lines
if (Test-Path "baseline_build.log") {
    Write-Host "`n📋 Last lines from baseline_build.log:" -ForegroundColor Yellow
    Get-Content "baseline_build.log" -Tail 5 -ErrorAction SilentlyContinue | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
}

Write-Host "`n============================================" -ForegroundColor Cyan

