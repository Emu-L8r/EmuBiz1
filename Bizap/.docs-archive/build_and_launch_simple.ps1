# BIZAP AUTOMATED BUILD AND LAUNCH SCRIPT
# Automates complete build and deployment for GUI3

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "         BIZAP AUTOMATED BUILD AND LAUNCH" -ForegroundColor Cyan
Write-Host "         GUI3 Matrix Theme Edition" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$appPackageName = "com.emul8r.bizap"
$mainActivity = ".MainActivity"

# Step 1
Write-Host "[1/6] Starting build process..." -ForegroundColor Yellow
cd $projectPath
Write-Host "OK - Directory set" -ForegroundColor Green
Write-Host ""

# Step 2
Write-Host "[2/6] Cleaning up processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "gradle" }
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "OK - Terminated Gradle daemons" -ForegroundColor Green
}
Start-Sleep -Seconds 2
Write-Host ""

# Step 3
Write-Host "[3/6] Clearing build cache..." -ForegroundColor Yellow
if (Test-Path "app\build") {
    Remove-Item "app\build" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "OK - Cleared app\build" -ForegroundColor Green
}
Write-Host ""

# Step 4
Write-Host "[4/6] Building APK..." -ForegroundColor Yellow
$buildOutput = & .\gradlew.bat clean assembleDebug --no-daemon --no-build-cache 2>&1
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Host "OK - BUILD SUCCESSFUL" -ForegroundColor Green
} else {
    Write-Host "ERROR - BUILD FAILED" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 5
Write-Host "[5/6] Verifying APK..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "OK - APK created" -ForegroundColor Green
} else {
    Write-Host "ERROR - APK not found" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 6
Write-Host "[6/6] Launching app..." -ForegroundColor Yellow
$devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -and $_ -notmatch "^List" }
$activeDevices = $devices | Where-Object { $_ -match "device" }

if ($activeDevices) {
    Write-Host "OK - Device found" -ForegroundColor Green
    adb install -r $apkPath
    Start-Sleep -Seconds 2
    adb shell am start -n "$appPackageName/$mainActivity"

    Write-Host ""
    Write-Host "=================================================================" -ForegroundColor Green
    Write-Host "SUCCESS - APP LAUNCHED" -ForegroundColor Green
    Write-Host "=================================================================" -ForegroundColor Green
} else {
    Write-Host "WARNING - No device connected" -ForegroundColor Yellow
    Write-Host "APK ready at: $apkPath" -ForegroundColor Green
}

