@echo off
setlocal enabledelayedexpansion

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set. Set it to your Android Studio JBR path.
    exit /b 1
)
cd /d "%~dp0"

echo ===== Testing Phase 1 Implementation =====
echo.
echo [1] Compiling...
call gradlew.bat :app:compileDebugKotlin --no-daemon > compile_test.log 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilation successful
) else (
    echo ❌ Compilation failed - see compile_test.log
    type compile_test.log | findstr /C:"error" /C:"ERROR"
    exit /b 1
)

echo.
echo [2] Running unit tests...
call gradlew.bat :app:testDebugUnitTest --no-daemon > test_results.log 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Tests passed!
    echo.
    echo Summary:
    findstr /C:"passed" test_results.log
) else (
    echo ❌ Tests failed - see test_results.log
    type test_results.log | findstr /C:"FAILED" /C:"ERROR" /C:"failures"
    exit /b 1
)

echo.
echo ===== Phase 1 Complete =====

