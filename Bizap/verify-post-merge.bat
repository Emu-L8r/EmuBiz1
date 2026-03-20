@echo off
REM POST-MERGE VERIFICATION SCRIPT
REM Simple batch script to verify everything is working

setlocal enabledelayedexpansion

echo.
echo ========================================
echo POST-MERGE VERIFICATION
echo ========================================
echo.

REM Get current git status
echo [1] Checking Git Status...
for /f "tokens=*" %%a in ('git branch --show-current') do set BRANCH=%%a
for /f "tokens=*" %%a in ('git rev-parse --short HEAD') do set COMMIT=%%a

echo Current Branch: %BRANCH%
echo Current Commit: %COMMIT%
echo.

REM Build
echo [2] Running Gradle Clean Build...
call gradlew clean build -x test
if %errorlevel% neq 0 (
    echo BUILD FAILED
    exit /b 1
)
echo BUILD SUCCESSFUL
echo.

REM Tests
echo [3] Running Tests...
call gradlew test
if %errorlevel% neq 0 (
    echo TESTS FAILED
    exit /b 1
)
echo TESTS SUCCESSFUL
echo.

REM Debug APK
echo [4] Checking Debug APK...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo Debug APK: BUILT
) else (
    echo Debug APK: NOT FOUND
    exit /b 1
)

REM Release APK
echo [5] Checking Release APK...
if exist "app\build\outputs\apk\release\app-release.apk" (
    echo Release APK: BUILT
) else (
    echo Release APK: NOT FOUND
    exit /b 1
)

REM Modules
echo [6] Checking Modules...
if exist "app" (
    echo Module app: FOUND
) else (
    echo Module app: NOT FOUND
    exit /b 1
)
if exist "data" (
    echo Module data: FOUND
) else (
    echo Module data: NOT FOUND
    exit /b 1
)
if exist "domain" (
    echo Module domain: FOUND
) else (
    echo Module domain: NOT FOUND
    exit /b 1
)

echo.
echo ========================================
echo VERIFICATION COMPLETE - ALL SYSTEMS GO
echo ========================================
echo.
echo Status Summary:
echo   Build:       PASSING
echo   Tests:       PASSING
echo   Debug APK:   BUILT
echo   Release APK: BUILT
echo   Modules:     INTEGRATED
echo.
echo CLEARED FOR OPTION C IMPLEMENTATION
echo.
echo Next Steps:
echo   1. Review: PROJECT_IMPROVEMENT_INITIATIVE_SUMMARY.md
echo   2. Choose: Your pace
echo   3. Start: Issue #10 (Security Fix)
echo.

exit /b 0

