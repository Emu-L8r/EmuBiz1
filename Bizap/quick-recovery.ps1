# Quick Recovery Script - Use if you just want to get back to a working state quickly
# This version is more aggressive and resets everything

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Bizap Quick Recovery Script" -ForegroundColor Cyan
Write-Host "WARNING: This will reset all local changes!" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$confirm = Read-Host "Are you sure you want to continue? (yes/no)"
if ($confirm -ne "yes") {
    Write-Host "Operation cancelled." -ForegroundColor Yellow
    exit 0
}

Write-Host ""

# Step 1: Hard reset to origin/main
Write-Host "[Step 1/4] Hard resetting to origin/main..." -ForegroundColor Yellow
git fetch origin
git reset --hard origin/main
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to reset to origin/main" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Reset to origin/main successful" -ForegroundColor Green

# Step 2: Clean all untracked files
Write-Host "[Step 2/4] Cleaning untracked files..." -ForegroundColor Yellow
git clean -fd
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to clean untracked files" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Cleaned untracked files" -ForegroundColor Green

# Step 3: Clean Gradle
Write-Host "[Step 3/4] Cleaning Gradle cache..." -ForegroundColor Yellow
./gradlew.bat clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "WARNING: Gradle clean had issues but continuing..." -ForegroundColor Yellow
}
Write-Host "✓ Gradle cleaned" -ForegroundColor Green

# Step 4: Build
Write-Host "[Step 4/4] Building project..." -ForegroundColor Yellow
./gradlew.bat assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build successful!" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Quick Recovery Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Your project is now at: origin/main" -ForegroundColor Cyan
Write-Host "Ready to use or continue development!" -ForegroundColor Cyan

