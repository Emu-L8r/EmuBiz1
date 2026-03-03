@echo off
setlocal enabledelayedexpansion

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set. Set it to your Android Studio JBR path.
    exit /b 1
)

cd /d "%~dp0"

echo ===== Building APK =====
call gradlew.bat :app:installDebug --no-daemon
if %ERRORLEVEL% EQU 0 (
    echo ✅ Build and install successful!
    timeout /t 3 /nobreak
    echo Launching app...
    call adb shell am start -n com.emul8r.bizap/.MainActivity
    timeout /t 5 /nobreak
    echo Capturing logs...
    call adb logcat -d | findstr /C:"MIGRATION_16_17" /C:"IllegalStateException" /C:"customers" > schema_test_results.txt
    type schema_test_results.txt
) else (
    echo ❌ Build failed!
    exit /b 1
)

