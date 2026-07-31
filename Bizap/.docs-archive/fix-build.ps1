# Build Fix Script for Bizap Project
# This script will resolve the current build issues

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Bizap Build Fix Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stash local changes
Write-Host "[Step 1/5] Stashing local changes..." -ForegroundColor Yellow
git stash
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to stash changes" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Changes stashed successfully" -ForegroundColor Green

# Step 2: Fetch latest from remote
Write-Host "[Step 2/5] Fetching latest from remote..." -ForegroundColor Yellow
git fetch origin
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to fetch from remote" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Fetch completed" -ForegroundColor Green

# Step 3: Pull latest main branch
Write-Host "[Step 3/5] Pulling latest main branch..." -ForegroundColor Yellow
git pull origin main
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to pull from main" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Main branch updated" -ForegroundColor Green

# Step 4: Clean Gradle cache and build
Write-Host "[Step 4/5] Cleaning Gradle and building project..." -ForegroundColor Yellow
./gradlew.bat clean assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed" -ForegroundColor Red
    Write-Host "Attempting to show error details..." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}
Write-Host "✓ Build successful!" -ForegroundColor Green

# Step 5: Verify build
Write-Host "[Step 5/5] Verifying build output..." -ForegroundColor Yellow
if (Test-Path "app/build/outputs/apk/debug/app-debug.apk") {
    Write-Host "✓ APK generated successfully at: app/build/outputs/apk/debug/app-debug.apk" -ForegroundColor Green
} else {
    Write-Host "WARNING: APK not found at expected location" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Build Fix Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Check Android Studio for any remaining issues" -ForegroundColor White
Write-Host "2. If needed, run 'git stash pop' to restore your changes" -ForegroundColor White
Write-Host "3. Test the app on an emulator or device" -ForegroundColor White

