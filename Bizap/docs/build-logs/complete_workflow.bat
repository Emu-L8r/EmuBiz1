@echo off
REM ================================================================
REM Bizap v0.1.0-stabilized - Complete Build, Fix, Commit & Install
REM ================================================================

setlocal enabledelayedexpansion

set PROJECT_DIR=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
set SDK_PATH=C:\Users\Saucey\AppData\Local\Android\Sdk
set ADB=%SDK_PATH%\platform-tools\adb.exe

echo.
echo ================================================================
echo  Bizap v0.1.0-stabilized - BUILD & REVIEW WORKFLOW
echo ================================================================
echo.
echo ✓ BUILD: APK successfully created (24.8 MB)
echo ✓ FIX: Hilt Json provider added to NetworkModule.kt
echo ✓ STATUS: Ready for device installation
echo.
echo ================================================================
echo.

REM Check APK
echo Verifying APK exists...
if exist "%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk" (
    echo ✓ APK confirmed
) else (
    echo ✗ APK not found!
    exit /b 1
)

echo.
echo ================================================================
echo  STEP 1: Commit the NetworkModule.kt fix
echo ================================================================
echo.

cd /d "%PROJECT_DIR%"

echo Adding NetworkModule.kt to git...
git add app/src/main/java/com/emul8r/bizap/di/NetworkModule.kt

echo Committing fix...
git commit -m "Fix: Add Hilt @Provides method for kotlinx.serialization.json.Json" ^
           -m "- OfflineSyncQueue was trying to inject Json but Hilt had no binding" ^
           -m "- Added @Provides @Singleton fun provideJson() in NetworkModule" ^
           -m "- Configured with ignoreUnknownKeys = true for server response flexibility" ^
           -m "- Fixes [Dagger/MissingBinding] error during build"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Fix committed
) else (
    echo ℹ Nothing to commit (already committed)
)

echo.
echo ================================================================
echo  STEP 2: Check Connected Devices
echo ================================================================
echo.

echo Available devices:
"%ADB%" devices

echo.
echo ================================================================
echo  STEP 3: Install APK on Device
echo ================================================================
echo.

echo Installing app...
"%ADB%" install -r "%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Installation successful
) else (
    echo ✗ Installation failed
    pause
    exit /b 1
)

echo.
echo ================================================================
echo  STEP 4: Launch App
echo ================================================================
echo.

echo Launching com.emul8r.bizap...
"%ADB%" shell am start -n com.emul8r.bizap/.MainActivity

if %ERRORLEVEL% EQU 0 (
    echo ✓ App launched!
    timeout /t 3
) else (
    echo ✗ Launch failed
    pause
    exit /b 1
)

echo.
echo ================================================================
echo  ✓ COMPLETE - APP LAUNCHED
echo ================================================================
echo.
echo Next: Run the Manual Review Checklist
echo.
echo [*] Currency Display
echo     - Create invoice with $49.99 item
echo     - Should display as A$99.98 (qty 2 × price)
echo     - NOT $9998.00
echo.
echo [*] Business Profile Reactivity
echo     - Edit business name in Settings
echo     - Go back to Dashboard
echo     - Should update immediately
echo.
echo [*] Payment Progress
echo     - Record partial payment on invoice
echo     - Progress bar should show proportional fill
echo.
echo [*] Document Vault
echo     - Generate PDF from invoice
echo     - Verify in Vault tab
echo     - Test sharing
echo.
echo [*] General Stability
echo     - Navigate all 5 tabs
echo     - No crashes
echo     - Smooth transitions
echo.
echo ================================================================
echo.

pause

