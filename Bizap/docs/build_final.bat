@echo off
REM Build script for Bizap v0.1.0-stabilized
REM This writes output to a log file we can read

setlocal enabledelayedexpansion

set LOG_FILE=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\build_output_final.txt
set PROJECT_DIR=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

echo ================================================ > "%LOG_FILE%"
echo Building Bizap v0.1.0-stabilized Debug APK >> "%LOG_FILE%"
echo ================================================ >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"
echo Build Start Time: %date% %time% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

cd /d "%PROJECT_DIR%"

echo Step 1: Clean >> "%LOG_FILE%"
call gradlew clean >> "%LOG_FILE%" 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Clean succeeded >> "%LOG_FILE%"
) else (
    echo Clean failed with code %ERRORLEVEL% >> "%LOG_FILE%"
)
echo. >> "%LOG_FILE%"

echo Step 2: Build Debug APK >> "%LOG_FILE%"
call gradlew :app:assembleDebug --stacktrace >> "%LOG_FILE%" 2>&1
set BUILD_EXIT_CODE=%ERRORLEVEL%
echo Build exit code: %BUILD_EXIT_CODE% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

echo Step 3: Check APK >> "%LOG_FILE%"
if exist "%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk" (
    echo APK EXISTS >> "%LOG_FILE%"
    for %%A in ("%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk") do (
        echo Size: %%~zA bytes >> "%LOG_FILE%"
        echo Modified: %%~TA >> "%LOG_FILE%"
    )
) else (
    echo APK NOT FOUND >> "%LOG_FILE%"
)
echo. >> "%LOG_FILE%"

echo Build End Time: %date% %time% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

if %BUILD_EXIT_CODE% EQU 0 (
    echo ============================================ >> "%LOG_FILE%"
    echo BUILD SUCCESSFUL >> "%LOG_FILE%"
    echo ============================================ >> "%LOG_FILE%"
) else (
    echo ============================================ >> "%LOG_FILE%"
    echo BUILD FAILED >> "%LOG_FILE%"
    echo ============================================ >> "%LOG_FILE%"
)

endlocal

