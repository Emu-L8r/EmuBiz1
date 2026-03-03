# Monitor Edit Invoice Save Events
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"

Write-Host "===== MONITORING EDIT INVOICE SAVE EVENTS =====" -ForegroundColor Cyan
Write-Host ""
Write-Host "Instructions:" -ForegroundColor Yellow
Write-Host "1. Open an existing invoice in the app"
Write-Host "2. Make changes (add/remove line items, edit fields)"
Write-Host "3. Click 'Save Invoice' button"
Write-Host "4. Watch the output below for debug logs"
Write-Host ""
Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Gray
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

& $adb logcat | Select-String "EditInvoice|InvoiceDao|saveInvoice|BIZAP" | ForEach-Object {
    $line = $_.Line
    if ($line -match "Save button clicked") {
        Write-Host $line -ForegroundColor Green
    } elseif ($line -match "Save successful") {
        Write-Host $line -ForegroundColor Green
    } elseif ($line -match "Failed|Error|Exception") {
        Write-Host $line -ForegroundColor Red
    } else {
        Write-Host $line -ForegroundColor White
    }
}

