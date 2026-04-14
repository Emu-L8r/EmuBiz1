@echo off
REM Quick build validation script for Bizap
REM Usage: Run this script to verify the build works

echo.
echo ====================================================
echo BIZAP BUILD VALIDATION SCRIPT
echo ====================================================
echo.

cd /d "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

echo [1/3] Checking syntax fixes...
findstr "Expecting '" "app\src\main\java\com\emul8r\bizap\ui\gui3\screens\PaymentAnalyticsScreenV3.kt" >nul 2>&1
if errorlevel 1 (
    echo ✓ PaymentAnalyticsScreenV3.kt syntax OK
) else (
    echo ✗ PaymentAnalyticsScreenV3.kt still has issues
    exit /b 1
)

findstr "Expecting '" "app\src\main\java\com\emul8r\bizap\ui\gui3\screens\RevenueAnalyticsScreenV3.kt" >nul 2>&1
if errorlevel 1 (
    echo ✓ RevenueAnalyticsScreenV3.kt syntax OK
) else (
    echo ✗ RevenueAnalyticsScreenV3.kt still has issues
    exit /b 1
)

echo.
echo [2/3] Clearing build cache...
if exist "app\build" (
    echo Removing app\build...
    rmdir /s /q "app\build" >nul 2>&1
)
if exist ".gradle" (
    echo Removing .gradle...
    rmdir /s /q ".gradle" >nul 2>&1
)
echo ✓ Cache cleared

echo.
echo [3/3] Building APK...
echo Starting build (this may take 2-3 minutes)...
echo.

call gradlew.bat clean assembleDebug --no-daemon --no-build-cache

if errorlevel 1 (
    echo.
    echo ✗ BUILD FAILED
    exit /b 1
) else (
    echo.
    echo ✓ BUILD SUCCESSFUL!
    echo.
    echo Checking APK...
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo ✓ APK file created: app\build\outputs\apk\debug\app-debug.apk
        echo.
        echo Ready to launch! Press Green Play button in Android Studio.
    ) else (
        echo ✗ APK file not found
        exit /b 1
    )
)

echo.
echo ====================================================
echo Validation complete!
echo ====================================================

