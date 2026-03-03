# Simple build script for Bizap v0.1.0-stabilized
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

Write-Host "Step 1: Clean"
.\gradlew clean

Write-Host ""
Write-Host "Step 2: Build APK"
.\gradlew :app:assembleDebug --stacktrace

Write-Host ""
Write-Host "Step 3: Check APK"
if (Test-Path "app\build\outputs\apk\debug\app-debug.apk") {
    Write-Host "APK BUILT SUCCESSFULLY"
    Get-Item "app\build\outputs\apk\debug\app-debug.apk" | Select-Object FullName, Length
} else {
    Write-Host "APK NOT FOUND"
}

Write-Host ""
Write-Host "Step 4: Run Tests"
.\gradlew :app:testDebugUnitTest --stacktrace

Write-Host ""
Write-Host "BUILD COMPLETE"

