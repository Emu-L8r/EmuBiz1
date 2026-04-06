#!/usr/bin/env powershell

param(
    [string]$Device = "emulator-5554"
)

Write-Host ""
Write-Host "============================================================"
Write-Host "FIREBASE CRASHLYTICS - AUTOMATED TEST SEQUENCE"
Write-Host "Windows PowerShell Edition"
Write-Host "============================================================"
Write-Host ""

$DEVICE = $Device
$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"
$PROJECT_ID = "bizap-801c0"

Write-Host "Configuration:"
Write-Host "  Device:    $DEVICE"
Write-Host "  Package:   $PACKAGE"
Write-Host "  Activity:  $ACTIVITY"
Write-Host "  Firebase:  $PROJECT_ID"
Write-Host ""

function Test-DeviceConnected {
    param([string]$Serial)
    try {
        $result = adb -s $Serial shell echo "OK" 2>&1
        return $result -eq "OK"
    } catch {
        return $false
    }
}

function Get-PackageStatus {
    param([string]$Serial, [string]$Package)
    try {
        $packages = adb -s $Serial shell pm list packages 2>&1
        return $packages | Select-String $Package
    } catch {
        return $null
    }
}

function Clear-AppData {
    param([string]$Serial, [string]$Package)
    Write-Host "Clearing app data..." -ForegroundColor Yellow
    adb -s $Serial shell pm clear $Package 2>&1 | Out-Null
    Start-Sleep -Seconds 2
    Write-Host "App data cleared" -ForegroundColor Green
}

function Launch-App {
    param([string]$Serial, [string]$Package, [string]$Activity)
    Write-Host "Launching app..." -ForegroundColor Yellow
    adb -s $Serial shell am start -n "$Package/$Activity" 2>&1 | Out-Null
    Start-Sleep -Seconds 5
    Write-Host "App launched" -ForegroundColor Green
}

function Relaunch-App {
    param([string]$Serial, [string]$Package, [string]$Activity)
    Write-Host "Relaunching app (to trigger upload)..." -ForegroundColor Yellow
    adb -s $Serial shell am start -n "$Package/$Activity" 2>&1 | Out-Null
    Start-Sleep -Seconds 3
    Write-Host "App relaunched" -ForegroundColor Green
}

function Monitor-Logcat {
    param(
        [string]$Serial,
        [int]$DurationSeconds = 45,
        [string]$SearchString = "Completed report upload"
    )

    Write-Host ""
    Write-Host "Monitoring Logcat ($DurationSeconds seconds)..."
    Write-Host "Looking for: '$SearchString'"
    Write-Host ""

    adb -s $Serial logcat -c 2>&1 | Out-Null
    Start-Sleep -Seconds 1

    $endTime = (Get-Date).AddSeconds($DurationSeconds)
    $foundTarget = $false
    $lineCount = 0

    adb -s $Serial logcat 2>&1 | ForEach-Object {
        $lineCount++

        if ((Get-Date) -gt $endTime) {
            Write-Host ""
            Write-Host "Timeout reached"
            exit
        }

        if ($_ -match $SearchString) {
            Write-Host "SUCCESS: $_" -ForegroundColor Green -BackgroundColor Black
            $foundTarget = $true
        } elseif ($_ -match "Uploading crash report|Initializing Crashlytics|Enabled") {
            Write-Host $_ -ForegroundColor Green
        } elseif ($_ -match "FirebaseCrashlytics") {
            Write-Host $_ -ForegroundColor Cyan
        } elseif ($_ -match "Error|Exception|Failed") {
            Write-Host "ERROR: $_" -ForegroundColor Red
        }
    }

    return $foundTarget
}

Write-Host "[STEP 1/6] Verifying device connection..." -ForegroundColor Yellow
if (-not (Test-DeviceConnected -Serial $DEVICE)) {
    Write-Host "ERROR: Device '$DEVICE' not found or offline" -ForegroundColor Red
    Write-Host ""
    Write-Host "Available devices:" -ForegroundColor Yellow
    adb devices -l
    exit 1
}
Write-Host "Device connected"
Write-Host ""

Write-Host "[STEP 2/6] Checking app installation..." -ForegroundColor Yellow
$pkg = Get-PackageStatus -Serial $DEVICE -Package $PACKAGE
if (-not $pkg) {
    Write-Host "ERROR: App not installed" -ForegroundColor Red
    Write-Host "Run: ./gradlew clean :app:installDebug" -ForegroundColor Yellow
    exit 1
}
Write-Host "App installed"
Write-Host ""

Write-Host "[STEP 3/6] Clearing app data (fresh Crashlytics state)..." -ForegroundColor Yellow
Clear-AppData -Serial $DEVICE -Package $PACKAGE
Write-Host ""

Write-Host "[STEP 4/6] Initial launch..." -ForegroundColor Yellow
Launch-App -Serial $DEVICE -Package $PACKAGE -Activity $ACTIVITY
Write-Host ""

Write-Host "[STEP 5/6] WAITING FOR USER ACTION..." -ForegroundColor Yellow
Write-Host "============================================================"
Write-Host "1. Look at your device/emulator"
Write-Host "2. Find the red button in the bottom-right corner"
Write-Host "3. TAP IT to crash the app"
Write-Host "4. App will force-close"
Write-Host "5. Press ENTER here when app has crashed"
Write-Host "============================================================"
Write-Host ""

Read-Host "Press ENTER when app has crashed"

Write-Host ""
Write-Host "[STEP 6/6] Relaunching app and monitoring for upload..." -ForegroundColor Yellow
Relaunch-App -Serial $DEVICE -Package $PACKAGE -Activity $ACTIVITY

$uploadFound = Monitor-Logcat -Serial $DEVICE -DurationSeconds 45 -SearchString "Completed report upload"

Write-Host ""
Write-Host "============================================================"
if ($uploadFound) {
    Write-Host "SUCCESS - CRASH UPLOAD CONFIRMED"
    Write-Host "Crash will appear in Firebase in 5-10 minutes"
} else {
    Write-Host "WARNING: Upload not detected"
    Write-Host "Check network connectivity or run diagnostics"
}
Write-Host "============================================================"
Write-Host ""

Write-Host "NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Wait 5-10 minutes"
Write-Host "2. Open Firebase Console:"
Write-Host "   https://console.firebase.google.com/project/$PROJECT_ID/crashlytics"
Write-Host "3. Refresh the page"
Write-Host "4. Crash should appear with:"
Write-Host "   - Exception: RuntimeException"
Write-Host "   - Message: INTENTIONAL TEST CRASH"
Write-Host "   - Custom Keys: test_crash_triggered, crash_reason"
Write-Host ""

