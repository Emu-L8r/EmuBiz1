@echo off
REM Build script with Hilt Json provider fix
REM This fixes the missing kotlinx.serialization.json.Json binding error

setlocal enabledelayedexpansion

set LOG_FILE=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\build_output_fixed.txt
set PROJECT_DIR=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

echo ================================================ > "%LOG_FILE%"
echo Building Bizap v0.1.0-stabilized (With Hilt Json Fix) >> "%LOG_FILE%"
echo ================================================ >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"
echo Build Start Time: %date% %time% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

cd /d "%PROJECT_DIR%"

REM Stop Gradle daemon
echo Stopping Gradle daemon... >> "%LOG_FILE%"
call gradlew --stop >> "%LOG_FILE%" 2>&1

REM Small delay
timeout /t 3 /nobreak

echo. >> "%LOG_FILE%"
echo Step 1: Clean >> "%LOG_FILE%"
call gradlew clean >> "%LOG_FILE%" 2>&1

echo. >> "%LOG_FILE%"
echo Step 2: Build Debug APK >> "%LOG_FILE%"
call gradlew :app:assembleDebug --stacktrace >> "%LOG_FILE%" 2>&1
set BUILD_EXIT_CODE=%ERRORLEVEL%

echo. >> "%LOG_FILE%"
echo Build exit code: %BUILD_EXIT_CODE% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

echo Step 3: Check APK >> "%LOG_FILE%"
if exist "%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk" (
    echo ============================================ >> "%LOG_FILE%"
    echo BUILD SUCCESS >> "%LOG_FILE%"
    echo ============================================ >> "%LOG_FILE%"
    for %%A in ("%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk") do (
        echo APK Size: %%~zA bytes >> "%LOG_FILE%"
        echo APK Path: %PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk >> "%LOG_FILE%"
        echo Modified: %%~TA >> "%LOG_FILE%"
    )
) else (
    echo ============================================ >> "%LOG_FILE%"
    echo BUILD FAILED - APK NOT FOUND >> "%LOG_FILE%"
    echo ============================================ >> "%LOG_FILE%"
)

echo. >> "%LOG_FILE%"
echo Build End Time: %date% %time% >> "%LOG_FILE%"

endlocal

