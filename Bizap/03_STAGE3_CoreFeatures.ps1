# Stage 3: Core Feature Validation (App Launch & Navigation)
# Execution Time: 5 minutes
# Device Required: YES (connected and USB debugging enabled)
# Purpose: Install APK and test core app features

Write-Output "=========================================="
Write-Output "STAGE 3: CORE FEATURE VALIDATION"
Write-Output "=========================================="
Write-Output ""
Write-Output "Prerequisites:"
Write-Output "  ✓ Device connected via USB"
Write-Output "  ✓ USB debugging enabled"
Write-Output "  ✓ APK built (run Stage 2 first)"
Write-Output ""

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "stage3_app_${timestamp}.log"

cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

# Step 1: Check for connected devices
Write-Output "Step 1: Checking for connected devices..."
Write-Output ""

$devices = adb devices 2>&1
Write-Output $devices | Out-File -FilePath $logFile -Encoding UTF8 -Append

if ($devices -match "device$") {
    Write-Output "✅ Device found"
} else {
    Write-Output "❌ No device found. Please connect device and enable USB debugging."
    exit 1
}

Write-Output ""

# Step 2: Check APK exists
Write-Output "Step 2: Checking APK..."
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Output "✅ APK found at: $apkPath"
} else {
    Write-Output "❌ APK not found. Run Stage 2 first."
    exit 1
}

Write-Output ""

# Step 3: Install APK
Write-Output "Step 3: Installing APK on device..."
Write-Output ""

$installOutput = adb install -r $apkPath 2>&1
Write-Output $installOutput | Out-File -FilePath $logFile -Encoding UTF8 -Append
Write-Output $installOutput

if ($installOutput -match "Success") {
    Write-Output "✅ APK installed successfully"
} else {
    Write-Output "❌ Installation failed"
    exit 1
}

Write-Output ""

# Step 4: Launch app
Write-Output "Step 4: Launching app..."
Write-Output ""

$launchOutput = adb shell am start -n com.emul8r.bizap/.MainActivity 2>&1
Write-Output $launchOutput | Out-File -FilePath $logFile -Encoding UTF8 -Append
Write-Output $launchOutput

if ($launchOutput -match "Error" -or $launchOutput -match "failed") {
    Write-Output "❌ App launch failed"
} else {
    Write-Output "✅ App launch command sent"
}

Write-Output ""
Write-Output "=========================================="
Write-Output "MANUAL TESTING CHECKLIST"
Write-Output "=========================================="
Write-Output ""
Write-Output "On your device, verify:"
Write-Output ""
Write-Output "□ SPLASH SCREEN"
Write-Output "  - Logo appears and animates"
Write-Output "  - Transitions smoothly to next screen"
Write-Output ""
Write-Output "□ PIN ENTRY SCREEN"
Write-Output "  - PIN input visible"
Write-Output "  - Accept valid PIN (test: 1234)"
Write-Output "  - Reject invalid PIN"
Write-Output ""
Write-Output "□ LANDING SCREEN"
Write-Output "  - Shows 3 GUI options (Classic/Modern/Matrix)"
Write-Output "  - Each button clickable"
Write-Output ""
Write-Output "□ GUI1 (CLASSIC)"
Write-Output "  - Launches without crash"
Write-Output "  - Traditional UI visible"
Write-Output "  - Can navigate to invoices/customers"
Write-Output ""
Write-Output "□ GUI2 (MODERN)"
Write-Output "  - Launches without crash"
Write-Output "  - Material Design 3 UI visible"
Write-Output "  - Dashboard loads and displays data"
Write-Output ""
Write-Output "□ GUI3 (MATRIX)"
Write-Output "  - Launches without crash"
Write-Output "  - Green-on-black cyberpunk theme"
Write-Output "  - Animations visible (falling characters, glitch effects)"
Write-Output "  - Dashboard renders correctly"
Write-Output ""
Write-Output "□ NAVIGATION"
Write-Output "  - Can switch between screens"
Write-Output "  - No crashes during navigation"
Write-Output "  - Back button works"
Write-Output ""
Write-Output "=========================================="
Write-Output ""
Write-Output "📝 App log saved to: $logFile"
Write-Output ""
Write-Output "Monitor logs with: adb logcat -s 'BizapApp:V'"
Write-Output "Next: Run Stage 4 to test PDF customization"

