@echo off
setlocal enabledelayedexpansion

REM Set Java Home
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=!PATH!;C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools

cd /d "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

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

