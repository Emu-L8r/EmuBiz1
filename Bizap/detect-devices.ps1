#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - DEVICE DETECTION
# Shows all connected devices with their serials

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  FIREBASE CRASHLYTICS - DEVICE DETECTION              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Scanning for connected devices..." -ForegroundColor Yellow

$output = adb devices -l 2>&1
$lines = $output -split "`n"

$devices = @()
foreach ($line in $lines) {
    if ($line -match "emulator|device.*usb|[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}") {
        if ($line -notmatch "^List|^  ") {
            $serial = ($line -split '\s+')[0]
            $status = ($line -split '\s+')[1]
            if ($serial -and $status) {
                $devices += [PSCustomObject]@{Serial=$serial; Status=$status}
            }
        }
    }
}

if ($devices.Count -eq 0) {
    Write-Host "❌ No devices found" -ForegroundColor Red
    Write-Host "`nMake sure:" -ForegroundColor Yellow
    Write-Host "  • Emulator is running (or device is connected)"
    Write-Host "  • USB debugging is enabled"
    Write-Host "  • ADB can see the device: adb devices -l`n"
    exit 1
}

Write-Host "Found $($devices.Count) device(s):`n" -ForegroundColor Green

$devices | ForEach-Object -Begin {$i=1} -Process {
    Write-Host "[$i] Serial: $($_.Serial)" -ForegroundColor Cyan
    Write-Host "    Status: $($_.Status)" -ForegroundColor Green
    $i++
}

Write-Host "`n✅ Use one of these serials in your commands:" -ForegroundColor Green
Write-Host '   $DEVICE = "' -NoNewline
Write-Host "$($devices[0].Serial)" -ForegroundColor Yellow -NoNewline
Write-Host '"' -ForegroundColor Green

Write-Host "`nRaw output:" -ForegroundColor DarkGray
adb devices -l | Write-Host -ForegroundColor DarkGray

Write-Host ""

