@echo off
REM Rebuild with no-build-cache to bypass Kotlin daemon issue

setlocal enabledelayedexpansion

set LOG_FILE=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\build_output_v2.txt
set PROJECT_DIR=C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

echo ================================================ > "%LOG_FILE%"
echo Building Bizap v0.1.0-stabilized (Retry with --no-build-cache) >> "%LOG_FILE%"
echo ================================================ >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"
echo Build Start Time: %date% %time% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

cd /d "%PROJECT_DIR%"

REM Stop Gradle daemon
echo Stopping Gradle daemon... >> "%LOG_FILE%"
call gradlew --stop >> "%LOG_FILE%" 2>&1

REM Small delay
timeout /t 5 /nobreak

REM Delete build cache directories
echo Deleting .gradle cache >> "%LOG_FILE%"
rmdir /s /q "%PROJECT_DIR%\.gradle" 2>>nul
rmdir /s /q "%PROJECT_DIR%\app\build\kotlin" 2>>nul

echo. >> "%LOG_FILE%"
echo Step 1: Clean >> "%LOG_FILE%"
call gradlew clean >> "%LOG_FILE%" 2>&1

echo. >> "%LOG_FILE%"
echo Step 2: Build with --no-build-cache >> "%LOG_FILE%"
call gradlew :app:assembleDebug --no-build-cache --stacktrace >> "%LOG_FILE%" 2>&1
set BUILD_EXIT_CODE=%ERRORLEVEL%

echo. >> "%LOG_FILE%"
echo Build exit code: %BUILD_EXIT_CODE% >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

echo Step 3: Check APK >> "%LOG_FILE%"
if exist "%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk" (
    echo BUILD SUCCESS >> "%LOG_FILE%"
    for %%A in ("%PROJECT_DIR%\app\build\outputs\apk\debug\app-debug.apk") do (
        echo APK Size: %%~zA bytes >> "%LOG_FILE%"
        echo Modified: %%~TA >> "%LOG_FILE%"
    )
) else (
    echo BUILD FAILED - APK NOT FOUND >> "%LOG_FILE%"
)

echo. >> "%LOG_FILE%"
echo Build End Time: %date% %time% >> "%LOG_FILE%"

endlocal

