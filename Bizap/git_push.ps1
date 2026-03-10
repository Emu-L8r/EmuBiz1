#!/bin/pwsh
# Git Push Script for Bizap Project

Set-Location "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

Write-Host "=== BIZAP GIT PUSH OPERATIONS ===" -ForegroundColor Green
Write-Host ""

# Show current status
Write-Host "1. Current Git Status:" -ForegroundColor Cyan
git status

Write-Host ""
Write-Host "2. Adding all changes..." -ForegroundColor Cyan
git add .

Write-Host ""
Write-Host "3. Committing changes..." -ForegroundColor Cyan
$commitMessage = "chore: Branding fixes and system health updates - March 10, 2026

- Fixed zoomed app icon and splash screen display issues
- Updated ic_launcher_foreground.xml with 60dp centered container
- Updated splash_screen.xml with 180dp sizing for crisp rendering
- Verified Firebase Analytics and Crashlytics integration
- Integrated PR #60: Auto-record payment on invoice PAID status
- Integrated PR #61: Dashboard PDF logo enhancement
- Verified GUI2 dashboard rendering and data consistency
- Confirmed system stability with no runtime crashes

Related PRs: #60, #61
Status: Branding repaired and system stable"

git commit -m "$commitMessage"

Write-Host ""
Write-Host "4. Pushing to main branch..." -ForegroundColor Cyan
git push origin main

Write-Host ""
Write-Host "5. Final Status:" -ForegroundColor Cyan
git log --oneline -3

Write-Host ""
Write-Host "=== PUSH COMPLETE ===" -ForegroundColor Green

