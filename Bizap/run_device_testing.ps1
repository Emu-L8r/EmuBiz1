# Automated Testing Script for Performance Validation

# Wait for app to fully launch
Start-Sleep -Seconds 3

Write-Host "=== BIZAP PERFORMANCE TESTING SESSION ===" -ForegroundColor Green
Write-Host "Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host ""

# Step 1: Launch GUI3
Write-Host "Step 1: Launching GUI3 (Matrix Cyberpunk)..." -ForegroundColor Yellow
adb shell am start -n com.emul8r.bizap/.MatrixGUIMainActivity
Start-Sleep -Seconds 5

# Step 2: Simulate user interactions
Write-Host "Step 2: Performing user interactions..." -ForegroundColor Yellow
Write-Host "  - Scrolling dashboard..."
adb shell input swipe 500 1000 500 300
Start-Sleep -Seconds 2

Write-Host "  - Scrolling back up..."
adb shell input swipe 500 300 500 1000
Start-Sleep -Seconds 2

Write-Host "  - Tapping navigation..."
adb shell input tap 500 1500
Start-Sleep -Seconds 3

Write-Host "  - Tapping another screen..."
adb shell input tap 300 1500
Start-Sleep -Seconds 3

# Step 3: Return to dashboard
Write-Host "Step 3: Returning to dashboard..." -ForegroundColor Yellow
adb shell input tap 100 50
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "=== USER INTERACTION COMPLETE ===" -ForegroundColor Green
Write-Host "Collecting Logcat data for 10 more seconds..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "✅ Testing session complete!" -ForegroundColor Green
Write-Host "Logcat data saved to: logcat_session_april26.txt"

