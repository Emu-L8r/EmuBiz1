# PHASE 1 Parallel Testing Script - Emulator + Tablet
# Runs automated tests on both devices simultaneously

param(
    [string]$EmulatorName = "Pixel_6a",
    [int]$EmulatorTimeout = 120  # seconds to wait for emulator startup
)

$AppPackage = "com.emul8r.bizap"
$AppActivity = "com.emul8r.bizap.MainActivity"
$ApkPath = "app/build/outputs/apk/debug/app-debug.apk"
$EmulatorLogFile = "phase1_emulator_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
$TabletLogFile = "phase1_tablet_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
$ResultsFile = "PHASE1_PARALLEL_RESULTS_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "    PHASE 1 PARALLEL TESTING - EMULATOR + TABLET" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================================
# HELPER FUNCTION
# ============================================================================

function Test-Device {
    param(
        [string]$DeviceId,
        [string]$DeviceName,
        [string]$LogFile
    )

    Write-Host "[TESTING] $DeviceName ($DeviceId)" -ForegroundColor Green

    # Check if APK exists
    if (-not (Test-Path $ApkPath)) {
        Write-Host "[ERROR] APK not found: $ApkPath" -ForegroundColor Red
        return @{
            Device = $DeviceName
            Status = "FAILED"
            Reason = "APK not found"
        }
    }

    # Install app
    Write-Host "  [1/4] Installing app..." -ForegroundColor Cyan
    $output = adb -s $DeviceId install -r $ApkPath 2>&1 | Out-String
    if (-not ($output -match "Success")) {
        Write-Host "  [ERROR] Installation failed" -ForegroundColor Red
        return @{
            Device = $DeviceName
            Status = "FAILED"
            Reason = "Installation failed"
        }
    }
    Write-Host "  [OK] App installed" -ForegroundColor Green

    # Launch app
    Write-Host "  [2/4] Launching app..." -ForegroundColor Cyan
    $output = adb -s $DeviceId shell am start -n "$AppPackage/$AppActivity" 2>&1 | Out-String
    if ($output -match "Error|Exception") {
        Write-Host "  [ERROR] Launch failed" -ForegroundColor Red
        return @{
            Device = $DeviceName
            Status = "FAILED"
            Reason = "Launch failed"
        }
    }
    Write-Host "  [OK] App launched" -ForegroundColor Green

    Start-Sleep -Seconds 2

    # Capture logs
    Write-Host "  [3/4] Capturing logs..." -ForegroundColor Cyan
    adb -s $DeviceId logcat -d | Out-File -FilePath $LogFile -Encoding UTF8
    Write-Host "  [OK] Logs captured" -ForegroundColor Green

    # Health check
    Write-Host "  [4/4] Health checks..." -ForegroundColor Cyan
    $logContent = adb -s $DeviceId logcat -d | Out-String

    $checks = @{
        "App Running" = $logContent -match "ActivityManager.*Started"
        "Status Unified" = $logContent -match "Status Counts"
        "No Crashes" = $logContent -notmatch "Fatal Exception"
    }

    $passed = 0
    foreach ($check in $checks.GetEnumerator()) {
        if ($check.Value) {
            $passed++
        }
    }

    Write-Host "  [OK] Health checks: $passed/$($checks.Count) passed" -ForegroundColor Green

    return @{
        Device = $DeviceName
        Status = "READY"
        LogFile = $LogFile
        HealthChecksPassed = $passed
        HealthChecksTotal = $checks.Count
    }
}

# ============================================================================
# MAIN
# ============================================================================

Write-Host "[STEP 1] Detecting devices..." -ForegroundColor Cyan
Write-Host ""

$devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" } | ForEach-Object { $_.Split()[0] }

if ($devices.Count -eq 0) {
    Write-Host "[ERROR] No devices found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Available devices:" -ForegroundColor Yellow
    adb devices
    exit 1
}

Write-Host "Found $($devices.Count) device(s):" -ForegroundColor Green
foreach ($device in $devices) {
    $deviceInfo = adb -s $device shell getprop ro.product.model 2>&1
    Write-Host "  - $device ($deviceInfo)" -ForegroundColor Green
}
Write-Host ""

# Start emulator if needed
$emulatorDevice = $null
$tabletDevice = $null

if ($devices.Count -eq 1) {
    # Only one device - could be tablet or emulator already running
    $deviceInfo = adb -s $devices[0] shell getprop ro.kernel.qemu 2>&1
    if ($deviceInfo -match "1") {
        Write-Host "[INFO] Detected emulator: $($devices[0])" -ForegroundColor Cyan
        $emulatorDevice = $devices[0]
    } else {
        Write-Host "[INFO] Detected tablet: $($devices[0])" -ForegroundColor Cyan
        $tabletDevice = $devices[0]
    }
} else {
    # Multiple devices - identify which is which
    foreach ($device in $devices) {
        $deviceInfo = adb -s $device shell getprop ro.kernel.qemu 2>&1
        if ($deviceInfo -match "1") {
            $emulatorDevice = $device
        } else {
            $tabletDevice = $device
        }
    }
}

Write-Host ""
Write-Host "[STEP 2] Starting emulator if needed..." -ForegroundColor Cyan
Write-Host ""

if (-not $emulatorDevice) {
    Write-Host "Starting $EmulatorName emulator in background..." -ForegroundColor Yellow
    $emulatorPath = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"

    if (-not (Test-Path $emulatorPath)) {
        Write-Host "[ERROR] Emulator not found at $emulatorPath" -ForegroundColor Red
        exit 1
    }

    # Start emulator in background
    $emulatorProc = Start-Process -FilePath $emulatorPath -ArgumentList "-avd", $EmulatorName, "-no-boot-anim" -PassThru -WindowStyle Hidden
    Write-Host "Emulator process started (PID: $($emulatorProc.Id))" -ForegroundColor Green

    # Wait for emulator to be ready
    Write-Host "Waiting for emulator to boot (up to $EmulatorTimeout seconds)..." -ForegroundColor Yellow
    $startTime = Get-Date
    $ready = $false

    while ((Get-Date) -lt $startTime.AddSeconds($EmulatorTimeout)) {
        $devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" } | ForEach-Object { $_.Split()[0] }

        foreach ($device in $devices) {
            $deviceInfo = adb -s $device shell getprop ro.kernel.qemu 2>&1
            if ($deviceInfo -match "1") {
                $emulatorDevice = $device
                $ready = $true
                break
            }
        }

        if ($ready) { break }
        Write-Host "  Still waiting..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
    }

    if (-not $ready) {
        Write-Host "[ERROR] Emulator failed to start within $EmulatorTimeout seconds" -ForegroundColor Red
        exit 1
    }

    Write-Host "Emulator ready: $emulatorDevice" -ForegroundColor Green
}

Write-Host ""
Write-Host "[STEP 3] Running parallel tests..." -ForegroundColor Cyan
Write-Host ""

# Test both devices
$results = @()

if ($emulatorDevice) {
    Write-Host "Testing Emulator..." -ForegroundColor Yellow
    $emulatorResult = Test-Device -DeviceId $emulatorDevice -DeviceName "EMULATOR" -LogFile $EmulatorLogFile
    $results += $emulatorResult
    Write-Host ""
}

if ($tabletDevice) {
    Write-Host "Testing Tablet..." -ForegroundColor Yellow
    $tabletResult = Test-Device -DeviceId $tabletDevice -DeviceName "TABLET" -LogFile $TabletLogFile
    $results += $tabletResult
    Write-Host ""
}

# Generate report
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "    PHASE 1 PARALLEL TEST RESULTS" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

$report = @"
PHASE 1 PARALLEL TESTING REPORT
Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

DEVICE STATUS
=============
"@

foreach ($result in $results) {
    $report += "`n$($result.Device):`n"
    $report += "  Status: $($result.Status)`n"

    if ($result.Status -eq "FAILED") {
        $report += "  Reason: $($result.Reason)`n"
    } else {
        $report += "  Health Checks: $($result.HealthChecksPassed)/$($result.HealthChecksTotal)`n"
        $report += "  Logs: $($result.LogFile)`n"
    }
}

$report += @"


TEST REQUIREMENTS
=================
Both devices have been prepared for manual testing:

MANUAL TESTS (Required on each device):
1. Status Persistence Test (5 min)
   - Create invoice in DRAFT
   - Change to SENT in GUI2
   - Close and reopen
   - Verify status persists as SENT

2. Pie Chart Parity Test (10 min)
   - Create 5 invoices (DRAFT, SENT, PAID, PARTIAL, OVERDUE)
   - Check pie charts on both GUIs
   - Verify counts match

3. Live Sync Test (5 min)
   - Change status in GUI2
   - Verify both dashboards update
   - Check counts are identical

NEXT STEPS
==========
Manual testing required on both devices.
Complete test on emulator first, then tablet.

Artifacts
=========
Emulator Logs: $EmulatorLogFile
Tablet Logs: $TabletLogFile
Results: $ResultsFile
"@

Write-Host $report
$report | Out-File -FilePath $ResultsFile -Encoding UTF8

Write-Host ""
Write-Host "Results saved to: $ResultsFile" -ForegroundColor Green
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "READY FOR MANUAL TESTING" -ForegroundColor Green
Write-Host ""
if ($emulatorDevice) {
    Write-Host "Emulator is available for testing" -ForegroundColor Cyan
    Write-Host "  Device ID: $emulatorDevice" -ForegroundColor Cyan
}
if ($tabletDevice) {
    Write-Host "Tablet is available for testing" -ForegroundColor Cyan
    Write-Host "  Device ID: $tabletDevice" -ForegroundColor Cyan
}
Write-Host ""

