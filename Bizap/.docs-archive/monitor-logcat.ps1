#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - LOGCAT MONITORING
# Displays Crashlytics logs in real-time with color coding

param(
    [string]$Device = "emulator-5554",
    [int]$DurationSeconds = 120
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  FIREBASE CRASHLYTICS - LOGCAT MONITORING             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Device:      $Device" -ForegroundColor Cyan
Write-Host "Duration:    $DurationSeconds seconds" -ForegroundColor Cyan
Write-Host "Watch for:   'Completed report upload' (in GREEN)" -ForegroundColor Green
Write-Host "`nPress Ctrl+C to stop monitoring`n" -ForegroundColor Yellow

# Clear logcat buffer
adb -s $Device logcat -c 2>&1 | Out-Null
Start-Sleep -Seconds 1

$startTime = Get-Date
$endTime = $startTime.AddSeconds($DurationSeconds)
$foundTarget = $false
$lineCount = 0

Write-Host "Starting Logcat stream..." -ForegroundColor Yellow
Write-Host "═" * 58 -ForegroundColor DarkGray

# Capture logcat
adb -s $Device logcat 2>&1 | ForEach-Object {
    $lineCount++
    $elapsed = ((Get-Date) - $startTime).TotalSeconds

    # Check timeout
    if ($elapsed -gt $DurationSeconds) {
        Write-Host "`n═" * 58
        Write-Host "⏱️  Duration limit reached ($DurationSeconds seconds)" -ForegroundColor Yellow
        exit
    }

    # Color-code output
    if ($_ -match "Completed report upload") {
        Write-Host "🟢 ✅ UPLOAD CONFIRMED: $_" -ForegroundColor Green -BackgroundColor Black
        $foundTarget = $true
    } elseif ($_ -match "Uploading crash report") {
        Write-Host "⚙️  UPLOADING: $_" -ForegroundColor Yellow
    } elseif ($_ -match "Initializing Crashlytics|Enabled") {
        Write-Host "📊 $_" -ForegroundColor Green
    } elseif ($_ -match "FirebaseCrashlytics") {
        Write-Host "📱 $_" -ForegroundColor Cyan
    } elseif ($_ -match "Error|Exception|Failed") {
        Write-Host "🔴 ERROR: $_" -ForegroundColor Red
    } elseif ($lineCount % 100 -eq 0) {
        Write-Host "📝 $_" -ForegroundColor DarkGray
    }
}

Write-Host "`n═" * 58 -ForegroundColor DarkGray
Write-Host ""

if ($foundTarget) {
    Write-Host "✅ SUCCESS! Crash upload was confirmed." -ForegroundColor Green
    Write-Host "   Crash will appear in Firebase Console in 5-10 minutes." -ForegroundColor Green
} else {
    Write-Host "⚠️  Upload confirmation not detected." -ForegroundColor Yellow
    Write-Host "   Check diagnostics or verify app is crashing." -ForegroundColor Yellow
}

Write-Host ""

