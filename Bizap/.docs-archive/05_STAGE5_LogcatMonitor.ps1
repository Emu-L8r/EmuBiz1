# Stage 5: Real-time Logcat Monitoring
# Execution Time: Continuous during Stages 3-4
# Device Required: YES
# Purpose: Monitor for crashes, errors, and debug info

Write-Output "=========================================="
Write-Output "STAGE 5: REAL-TIME LOGCAT MONITORING"
Write-Output "=========================================="
Write-Output ""
Write-Output "This stage monitors application logs for:"
Write-Output "  - Crashes and exceptions"
Write-Output "  - Runtime errors"
Write-Output "  - Debug information"
Write-Output ""
Write-Output "Prerequisites:"
Write-Output "  ✓ Device connected"
Write-Output "  ✓ App running (from Stage 3)"
Write-Output ""

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "logcat_${timestamp}.log"

Write-Output "Starting logcat capture..."
Write-Output "Log file: $logFile"
Write-Output ""
Write-Output "Key indicators to watch:"
Write-Output "  ✅ INFO (I:) - Normal operation"
Write-Output "  ✅ DEBUG (D:) - Debug information"
Write-Output "  ❌ ERROR (E:) - Error occurred"
Write-Output "  ❌ FATAL - Crash or fatal error"
Write-Output ""
Write-Output "=========================================="
Write-Output "LIVE LOGCAT FEED (Press Ctrl+C to stop)"
Write-Output "=========================================="
Write-Output ""

# Start capturing logcat
# Note: This will run continuously until user presses Ctrl+C
# The -d flag is removed to get live stream

try {
    adb logcat -s "BizapApp:V" 2>&1 | Tee-Object -FilePath $logFile
} catch {
    Write-Output "Logcat monitoring stopped."
}

Write-Output ""
Write-Output "=========================================="
Write-Output "LOGCAT CAPTURE COMPLETE"
Write-Output "=========================================="
Write-Output ""

# Analyze the captured log
Write-Output "Analyzing captured logs..."
Write-Output ""

$content = Get-Content $logFile -ErrorAction SilentlyContinue

if ($content) {
    # Count errors
    $errorCount = ($content | Select-String -Pattern "^E/" | Measure-Object).Count
    $infoCount = ($content | Select-String -Pattern "^I/" | Measure-Object).Count
    $debugCount = ($content | Select-String -Pattern "^D/" | Measure-Object).Count
    $fatalCount = ($content | Select-String -Pattern "FATAL|Exception|Crash" | Measure-Object).Count

    Write-Output "Statistics:"
    Write-Output "  INFO logs: $infoCount"
    Write-Output "  DEBUG logs: $debugCount"
    Write-Output "  ERROR logs: $errorCount"
    Write-Output "  FATAL/Exception: $fatalCount"
    Write-Output ""

    if ($fatalCount -gt 0) {
        Write-Output "❌ CRITICAL ISSUES FOUND:"
        $content | Select-String -Pattern "FATAL|Exception|Crash"
    } elseif ($errorCount -gt 0) {
        Write-Output "⚠️  ERRORS FOUND (may be expected):"
        $content | Select-String -Pattern "^E/" | Select-Object -First 5
    } else {
        Write-Output "✅ No critical errors found"
    }
} else {
    Write-Output "⚠️  No log output captured"
}

Write-Output ""
Write-Output "📝 Full log saved to: $logFile"
Write-Output ""
Write-Output "To view errors:"
Write-Output "  Select-String -Path '$logFile' -Pattern '^E/' | Select-Object -First 20"
Write-Output ""
Write-Output "Next: Run Stage 6 for full regression test"

