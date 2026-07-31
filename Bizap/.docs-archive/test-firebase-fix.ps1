# 🚀 FIREBASE CRASH FIX - QUICK TEST SCRIPT (PowerShell Version)
# This script rebuilds, installs, and monitors the app for crashes

Write-Host "🎯 FIREBASE CRASH FIX VERIFICATION" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
cd $projectPath

# Step 1: Clean Build
Write-Host "📦 Step 1: Clean Build..." -ForegroundColor Yellow
./gradlew clean build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful" -ForegroundColor Green
Write-Host ""

# Step 2: Install Debug APK
Write-Host "📱 Step 2: Installing debug APK..." -ForegroundColor Yellow
./gradlew installDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Installation failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ APK installed successfully" -ForegroundColor Green
Write-Host ""

# Step 3: Clear Logcat
Write-Host "🧹 Step 3: Clearing logcat..." -ForegroundColor Yellow
adb logcat -c

Write-Host ""
Write-Host "📊 Step 4: Monitoring logcat for Firebase initialization..." -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Watch for these messages:" -ForegroundColor Cyan
Write-Host "  ✅ '✅ Firebase Analytics initialized' = Firebase working" -ForegroundColor Green
Write-Host "  ⚠️  '⚠️ Failed to initialize FirebaseAnalytics' = Firebase not configured (OK for dev)" -ForegroundColor Yellow
Write-Host "  ❌ 'Exception|CRASH|Error' = Application crashed (need to investigate)" -ForegroundColor Red
Write-Host ""
Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Yellow
Write-Host ""

adb logcat | Select-String "Firebase|Bizap|EVENT|Exception|CRASH|Error"


