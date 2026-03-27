# PDF Export Fix Validation Script (PowerShell)
# Captures logcat and filters for FileUriProvider and PDF generation events

Write-Host "🔍 PDF Export Fix Validation Script" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prerequisites:" -ForegroundColor Yellow
Write-Host "  ✓ App must be running on emulator/device" -ForegroundColor Gray
Write-Host "  ✓ ADB must be available in PATH" -ForegroundColor Gray
Write-Host ""

function Check-Pattern {
    param(
        [string]$Pattern,
        [string]$Description
    )

    Write-Host "📝 Checking for: $Description" -ForegroundColor Cyan

    $logcat = adb logcat -d
    $matches = $logcat | Select-String $Pattern | Select-Object -Last 3

    if ($matches) {
        Write-Host "✓ Found: $Pattern" -ForegroundColor Green
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
        $matches | ForEach-Object { Write-Host $_.Line -ForegroundColor Green }
        Write-Host ""
    } else {
        Write-Host "✗ Not found: $Pattern" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Clear logcat first
Write-Host "📋 Clearing logcat..." -ForegroundColor Cyan
adb logcat -c
Start-Sleep -Seconds 1

Write-Host ""
Write-Host "🎬 Now perform these steps in your app:" -ForegroundColor Yellow
Write-Host "   1. Open an invoice" -ForegroundColor Gray
Write-Host "   2. Tap 'Export as PDF'" -ForegroundColor Gray
Write-Host "   3. Tap 'Share Invoice'" -ForegroundColor Gray
Write-Host "   4. Select an app or cancel" -ForegroundColor Gray
Write-Host ""
Read-Host "Press ENTER when done with PDF export test"

Write-Host ""
Write-Host "🔎 Analyzing logcat for PDF export events..." -ForegroundColor Cyan
Write-Host ""

# Check for PDF generation success
Check-Pattern "PDF generated successfully" "PDF Generation Success"

# Check for FileUriProvider validation
Check-Pattern "FileUriProvider.*Successfully converted" "FileUriProvider Success"

# Check for any errors
Check-Pattern "PDF sharing failed" "PDF Sharing Error"

# Check for validation failures
Check-Pattern "File does not exist" "File Validation Error"

Write-Host ""
Write-Host "📊 Summary of Possible States:" -ForegroundColor Magenta
Write-Host ""
Write-Host "✅ SUCCESS (All good):" -ForegroundColor Green
Write-Host "   ✓ PDF generated successfully message found" -ForegroundColor Green
Write-Host "   ✓ FileUriProvider converted URI successfully" -ForegroundColor Green
Write-Host "   ✓ NO error messages" -ForegroundColor Green
Write-Host ""
Write-Host "⚠️  PARTIAL (File generated but sharing failed):" -ForegroundColor Yellow
Write-Host "   ✓ PDF generated successfully message found" -ForegroundColor Yellow
Write-Host "   ✗ FileUriProvider error (check file_paths.xml)" -ForegroundColor Yellow
Write-Host ""
Write-Host "❌ FAILURE (PDF generation failed):" -ForegroundColor Red
Write-Host "   ✗ PDF generated message NOT found" -ForegroundColor Red
Write-Host "   ✗ Error message found" -ForegroundColor Red
Write-Host ""

# Show log file analysis
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Magenta
Write-Host ""
Write-Host "✅ LOCAL LOGGING ENABLED - No Firebase Required!" -ForegroundColor Green
Write-Host ""
Write-Host "All logs are saved to:" -ForegroundColor Cyan
Write-Host "  /data/data/com.emul8r.bizap/files/bizap_logs.txt" -ForegroundColor Gray
Write-Host ""
Write-Host "📝 Pull and analyze local log files:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   1. Pull logs from device:" -ForegroundColor Gray
Write-Host "      adb pull /data/data/com.emul8r.bizap/files/bizap_logs.txt" -ForegroundColor Cyan
Write-Host ""
Write-Host "   2. View logs:" -ForegroundColor Gray
Write-Host "      cat bizap_logs.txt" -ForegroundColor Cyan
Write-Host ""
Write-Host "   3. Search for PDF operations:" -ForegroundColor Gray
Write-Host "      Select-String 'PDF_EXPORT' bizap_logs.txt" -ForegroundColor Cyan
Write-Host ""
Write-Host "   4. Find errors:" -ForegroundColor Gray
Write-Host "      Select-String '❌|FAILURE' bizap_logs.txt" -ForegroundColor Cyan
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Magenta



