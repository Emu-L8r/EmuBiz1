# Database Schema Mismatch Fix for Development
# This script clears the app data and reinstalls the app to reset the database

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BIZAP Database Schema Recovery" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "ℹ️  This will:" -ForegroundColor Yellow
Write-Host "  1. Uninstall the app (removes old database with schema mismatch)" -ForegroundColor Yellow
Write-Host "  2. Clean build the app" -ForegroundColor Yellow
Write-Host "  3. Install fresh APK" -ForegroundColor Yellow
Write-Host "  4. App will create new database with current schema" -ForegroundColor Yellow
Write-Host ""

$response = Read-Host "Continue? (yes/no)"
if ($response -ne "yes") {
    Write-Host "Cancelled." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "Step 1: Clearing app data from emulator/device..." -ForegroundColor Cyan

# Clear the app completely
adb shell pm clear com.emul8r.bizap
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Warning: Could not clear app (may not be installed yet)" -ForegroundColor Yellow
} else {
    Write-Host "✓ App data cleared" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 2: Building debug APK..." -ForegroundColor Cyan
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

.\gradlew.bat clean assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Build failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build complete" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Installing APK..." -ForegroundColor Cyan
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Installation failed" -ForegroundColor Red
    exit 1
}
Write-Host "✓ APK installed" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Launching app..." -ForegroundColor Cyan
adb shell am start -n com.emul8r.bizap/.MainActivity
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  Warning: Could not launch (emulator may not be running)" -ForegroundColor Yellow
} else {
    Write-Host "✓ App launching" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 5: Monitoring logcat for startup..." -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Gray
Write-Host ""

# Show logcat with filtering for important messages
adb logcat | Select-String -Pattern "(Room|database|IllegalStateException|FATAL|ERROR)" -CaseSensitive:$false

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Database recovery complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

