# Android Studio Helper Scripts
# Run these from PowerShell in the project root directory

# Function 1: Invalidate Caches and Restart
function Invalidate-AndroidStudio {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Android Studio Cache Invalidation" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Follow these steps:" -ForegroundColor Yellow
    Write-Host "1. Open Android Studio" -ForegroundColor White
    Write-Host "2. Go to: File → Invalidate Caches / Clear Cached Data" -ForegroundColor White
    Write-Host "3. Select both options:" -ForegroundColor White
    Write-Host "   - Invalidate and Restart" -ForegroundColor White
    Write-Host "   - Also clear: Indexes, Local History, etc." -ForegroundColor White
    Write-Host "4. Click 'Invalidate and Restart'" -ForegroundColor White
    Write-Host ""
}

# Function 2: Gradle Daemon Cleanup
function Stop-GradleDaemon {
    Write-Host "Stopping Gradle Daemon..." -ForegroundColor Yellow
    ./gradlew.bat --stop
    Write-Host "✓ Gradle daemon stopped" -ForegroundColor Green
}

# Function 3: Full Sync
function Sync-AndBuild {
    param(
        [switch]$Clean = $false
    )

    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Syncing and Building Project" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    if ($Clean) {
        Write-Host "[1/4] Cleaning project..." -ForegroundColor Yellow
        ./gradlew.bat clean
    } else {
        Write-Host "[1/3] Stopping Gradle daemon..." -ForegroundColor Yellow
        ./gradlew.bat --stop
    }

    Write-Host "[2/3] Syncing Gradle files..." -ForegroundColor Yellow
    ./gradlew.bat build --dry-run

    Write-Host "[3/3] Building project..." -ForegroundColor Yellow
    ./gradlew.bat assembleDebug --no-daemon

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Build successful!" -ForegroundColor Green
    } else {
        Write-Host "✗ Build failed" -ForegroundColor Red
    }
}

# Function 4: Quick Rebuild
function Quick-Build {
    Write-Host "Quick building..." -ForegroundColor Yellow
    ./gradlew.bat assembleDebug --no-daemon
}

# Function 5: Run on Emulator/Device
function Run-App {
    Write-Host "Running app on connected device/emulator..." -ForegroundColor Yellow
    ./gradlew.bat installDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ App installed successfully!" -ForegroundColor Green
    } else {
        Write-Host "✗ Installation failed" -ForegroundColor Red
    }
}

# Export functions for easy use
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Studio Helper Functions Loaded" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Available commands:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Invalidate-AndroidStudio      - Shows steps to invalidate AS caches" -ForegroundColor White
Write-Host "2. Stop-GradleDaemon            - Stops the Gradle daemon" -ForegroundColor White
Write-Host "3. Sync-AndBuild                - Full sync and build" -ForegroundColor White
Write-Host "   Sync-AndBuild -Clean         - Full sync with clean first" -ForegroundColor White
Write-Host "4. Quick-Build                  - Quick rebuild (cached)" -ForegroundColor White
Write-Host "5. Run-App                      - Install and run on device" -ForegroundColor White
Write-Host ""
Write-Host "Usage example:" -ForegroundColor Cyan
Write-Host "  . .\android-studio-helpers.ps1" -ForegroundColor White
Write-Host "  Sync-AndBuild" -ForegroundColor White
Write-Host ""

