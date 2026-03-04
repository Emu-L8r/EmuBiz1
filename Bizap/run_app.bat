@echo off
REM Bizap App Runner - Week 4
REM This script builds, installs, and launches the app

setlocal enabledelayedexpansion

echo.
echo ========================================
echo BIZAP APP RUNNER - WEEK 4
echo ========================================
echo.

set BIZAP_DIR=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
set APK_PATH=%BIZAP_DIR%\app\build\outputs\apk\debug\app-debug.apk

cd /d "%BIZAP_DIR%"

echo Step 1: Building the app...
echo ========================================
call gradlew clean :app:assembleDebug --no-daemon

if not exist "%APK_PATH%" (
    echo.
    echo ERROR: APK not found after build!
    echo Expected at: %APK_PATH%
    pause
    exit /b 1
)

for %%A in ("%APK_PATH%") do set "APK_SIZE=%%~zA"
set /a APK_SIZE_MB=%APK_SIZE% / 1048576

echo.
echo ^✓ APK created: %APK_SIZE_MB% MB
echo.
echo Step 2: Checking for device...
echo ========================================

adb devices

echo.
echo Step 3: Installing app...
echo ========================================

call adb install -r "%APK_PATH%"

echo.
echo Step 4: Launching app...
echo ========================================

call adb shell am start -n com.emul8r.bizap/.MainActivity

echo.
echo ^✓ App launched!
echo.

timeout /t 5

echo Checking for crashes...
adb logcat -d -s AndroidRuntime:E

echo.
echo ========================================
echo APP RUNNER COMPLETE
echo ========================================
echo.
echo What to test:
echo   1. App launches without crash
echo   2. Navigate to Invoices - Create Invoice
echo   3. Add line items with quantities/prices
echo   4. Verify currency displays correctly
echo   5. Save invoice
echo   6. Go to Settings - Business Profile
echo.

pause

