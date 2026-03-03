# Test script for Edit Invoice Save Bug Fix
$ErrorActionPreference = "Continue"
$adb = if ($env:ANDROID_HOME) { "$env:ANDROID_HOME\platform-tools\adb.exe" } else { "adb" }

Write-Host "===== EDIT INVOICE SAVE BUG - DEBUG TEST =====" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build
Write-Host "[1/5] Building APK..." -ForegroundColor Yellow
if (-not $env:JAVA_HOME) {
    Write-Host "ERROR: JAVA_HOME is not set. Set it to your Android Studio JBR path." -ForegroundColor Red
    exit 1
}
Set-Location $PSScriptRoot
./gradlew clean :app:assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build successful" -ForegroundColor Green
Write-Host ""

# Step 2: Install
Write-Host "[2/5] Installing APK..." -ForegroundColor Yellow
& $adb install -r "app\build\outputs\apk\debug\app-debug.apk"
Write-Host "✅ APK installed" -ForegroundColor Green
Write-Host ""

# Step 3: Clear logcat
Write-Host "[3/5] Clearing logcat..." -ForegroundColor Yellow
& $adb logcat -c
Write-Host "✅ Logcat cleared" -ForegroundColor Green
Write-Host ""

# Step 4: Launch app
Write-Host "[4/5] Launching app..." -ForegroundColor Yellow
& $adb shell am start -n com.emul8r.bizap/.MainActivity
Start-Sleep -Seconds 3
Write-Host "✅ App launched" -ForegroundColor Green
Write-Host ""

# Step 5: Monitor logcat for save events
Write-Host "[5/5] Monitoring logcat for 'EditInvoice' logs..." -ForegroundColor Yellow
Write-Host "Now go edit an invoice and click 'Save Invoice'" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Gray
Write-Host ""
& $adb logcat | Select-String "EditInvoice|InvoiceDao|BIZAP"

