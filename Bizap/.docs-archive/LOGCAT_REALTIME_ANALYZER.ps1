################################################################################
#
# BIZAP LOGCAT REAL-TIME ANALYZER
# Automated monitoring and issue detection
#
# Purpose: Monitor logcat in real-time and alert on issues
# Usage: ./LOGCAT_REALTIME_ANALYZER.ps1 -Emulator emulator-5554
#        ./LOGCAT_REALTIME_ANALYZER.ps1 -Emulator emulator-5554 -MonitorDurationMinutes 120
#
# Created: April 28, 2026
# Version: 1.0
#
################################################################################

param(
    [string]$Emulator = "emulator-5554",
    [int]$MonitorDurationMinutes = 60,
    [switch]$AlertOnError = $true,
    [string]$OutputLog = "logcat_analysis_$(Get-Date -f 'yyyy-MM-dd_HHmmss').log"
)

$ErrorActionPreference = "Continue"

# Color scheme
$Colors = @{
    CriticalError = "Red"
    Warning = "Yellow"
    Performance = "Magenta"
    Network = "DarkYellow"
    Database = "DarkRed"
    Success = "Green"
    Info = "Cyan"
    Debug = "Gray"
}

# Pattern definitions for issue detection
$Patterns = @{
    CriticalError = "FATAL|CRASH|EXCEPTION|NullPointerException|OutOfMemoryError|StrictMode|ANR|Force closing"
    Warning = "W/|Warning|Deprecated|Leaked"
    Performance = "jank|Choreographer|dropped.*frames|frame too long|Skipped|Not rendering"
    Network = "NetworkError|Connection refused|Timeout|UnknownHostException|EOFException"
    Database = "database locked|Corrupted database|integrity check failed|SQLITE_CANTOPEN"
    Success = "✅|PASS|Success|successfully|complete"
}

function Get-IssueLevel {
    param([string]$LogLine)

    foreach ($level in "CriticalError", "Warning", "Performance", "Network", "Database") {
        if ($LogLine -match $Patterns[$level]) {
            return $level
        }
    }
    return "Info"
}

function Write-ColoredOutput {
    param(
        [string]$Message,
        [string]$Level = "Info"
    )

    $color = $Colors[$Level]
    $timestamp = Get-Date -f "HH:mm:ss.fff"

    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
    "$timestamp [$Level] $Message" | Out-File -Append $OutputLog
}

function Invoke-LogcatMonitoring {
    Write-Host "`n$('=' * 80)" -ForegroundColor Cyan
    Write-Host "BIZAP LOGCAT REAL-TIME ANALYZER" -ForegroundColor Cyan
    Write-Host "Starting monitoring session..." -ForegroundColor Cyan
    Write-Host "$('=' * 80)" -ForegroundColor Cyan
    Write-Host "Emulator:  $Emulator" -ForegroundColor Cyan
    Write-Host "Duration:  $MonitorDurationMinutes minutes" -ForegroundColor Cyan
    Write-Host "Log file:  $OutputLog" -ForegroundColor Cyan
    Write-Host "Started:   $(Get-Date -f 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "$('=' * 80)`n" -ForegroundColor Cyan

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $endTime = (Get-Date).AddMinutes($MonitorDurationMinutes)

    $issueStats = @{
        CriticalError = 0
        Warning = 0
        Performance = 0
        Network = 0
        Database = 0
        Success = 0
        Info = 0
    }

    try {
        # Check if emulator is connected
        Write-ColoredOutput "Verifying emulator connection..." -Level Info
        $devices = & adb devices 2>&1
        if ($devices -notmatch $Emulator) {
            Write-ColoredOutput "❌ ERROR: Emulator '$Emulator' not found!" -Level CriticalError
            Write-ColoredOutput "Available devices:" -Level Info
            $devices | Select-Object -Skip 1 | Write-ColoredOutput -Level Info
            return
        }
        Write-ColoredOutput "✅ Emulator connected: $Emulator" -Level Success

        # Clear existing logcat
        Write-ColoredOutput "Clearing logcat buffer..." -Level Info
        & adb -s $Emulator logcat --clear 2>&1 | Out-Null
        Start-Sleep -Milliseconds 500

        # Start monitoring
        Write-ColoredOutput "Starting real-time monitoring (press Ctrl+C to stop)..." -Level Info
        Write-ColoredOutput "Waiting for logcat data..." -Level Debug
        Write-Host ""

        # Create script block for logcat processing
        $logcatCmd = "adb -s $Emulator logcat -s `"com.emul8r.bizap`""

        $logcatOutput = Invoke-Expression $logcatCmd 2>&1

        foreach ($line in @($logcatOutput)) {
            # Stop if duration exceeded
            if ($stopwatch.Elapsed.TotalMinutes -gt $MonitorDurationMinutes) {
                Write-ColoredOutput "⏱️ Monitoring duration ($MonitorDurationMinutes min) exceeded. Stopping..." -Level Info
                break
            }

            # Skip empty lines and timestamps
            if ([string]::IsNullOrWhiteSpace($line)) {
                continue
            }

            # Detect issue level
            $issueLevel = Get-IssueLevel $line

            # Update stats
            $issueStats[$issueLevel]++

            # Output with color (limit length for readability)
            $displayLine = if ($line.Length -gt 140) {
                $line.Substring(0, 137) + "..."
            } else {
                $line
            }

            Write-ColoredOutput $displayLine -Level $issueLevel

            # Alert on critical issues
            if ($issueLevel -eq "CriticalError" -and $AlertOnError) {
                Write-Host "`n⚠️⚠️⚠️ CRITICAL ISSUE DETECTED! ⚠️⚠️⚠️" -ForegroundColor Red
                Write-Host "Line: $line`n" -ForegroundColor Red
            }
        }
    } catch {
        Write-ColoredOutput "Error during monitoring: $_" -Level CriticalError
    } finally {
        $stopwatch.Stop()
    }

    # Print summary
    Write-Host "`n$('=' * 80)" -ForegroundColor Cyan
    Write-Host "MONITORING SUMMARY" -ForegroundColor Cyan
    Write-Host "$('=' * 80)" -ForegroundColor Cyan

    Write-Host ""
    foreach ($level in @("CriticalError", "Warning", "Performance", "Network", "Database", "Success", "Info")) {
        $count = $issueStats[$level]
        $color = $Colors[$level]
        $bar = "=" * [math]::Min($count, 50)
        Write-Host "  $($level.PadRight(15)) : $count`..........$bar" -ForegroundColor $color
    }

    Write-Host ""
    Write-Host "Monitoring duration: $($stopwatch.Elapsed.TotalSeconds) seconds ($($stopwatch.Elapsed.TotalMinutes) min)" -ForegroundColor Cyan
    Write-Host "Log file saved to:   $OutputLog" -ForegroundColor Cyan
    Write-Host "Session ended:       $(Get-Date -f 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "$('=' * 80)`n" -ForegroundColor Cyan

    # Analysis
    if ($issueStats["CriticalError"] -gt 0) {
        Write-Host "⚠️  CRITICAL ISSUES DETECTED - Review log file for details" -ForegroundColor Red
    } elseif ($issueStats["Warning"] -gt 5) {
        Write-Host "⚠️  Multiple warnings detected - May warrant investigation" -ForegroundColor Yellow
    } else {
        Write-Host "✅ No critical issues detected - Monitoring session clean" -ForegroundColor Green
    }

    Write-Host ""
}

# Execute monitoring
Invoke-LogcatMonitoring

