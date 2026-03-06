#!/usr/bin/env pwsh

Write-Host "Build Complete Check" -ForegroundColor Cyan

$bizapPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
Set-Location $bizapPath

Write-Host "Stopping gradle..." -ForegroundColor Yellow
& ./gradlew --stop 2>&1 | Out-Null
Start-Sleep -Seconds 2

Write-Host "Starting clean build with AGP 8.5.0 + Hilt 2.46..." -ForegroundColor Cyan
& ./gradlew clean assembleDebug --no-build-cache

Write-Host "Build completed" -ForegroundColor Green

