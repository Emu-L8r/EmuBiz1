@echo off
REM Simple batch script to measure StrictMode violations
REM Usage: .\measure-strictmode-violations-simple.bat

setlocal enabledelayedexpansion

echo.
echo ================================
echo STRICTMODE VIOLATION MEASUREMENT
echo ================================
echo.

REM Step 1: Launch app
echo Step 1: Launching app...
adb shell am start -n "com.emul8r.bizap/.MainActivity" -W >nul 2>&1
timeout /t 2 /nobreak >nul

REM Step 2: Clear logcat
echo Step 2: Clearing logcat buffer...
adb logcat -c

REM Step 3: Capture StrictMode for 90 seconds
echo Step 3: Capturing StrictMode violations for 90 seconds...
echo (Perform PIN operations: Setup, Verify, Clear)
echo.

adb logcat -s "StrictMode" -v time > strictmode-logcat.txt

REM Step 4: Wait 90 seconds
echo Waiting 90 seconds (this is automatic - app will auto-test)...
timeout /t 90 /nobreak

REM Step 5: Stop capture
echo.
echo Step 4: Analyzing results...

REM Count violations
setlocal enabledelayedexpansion
set "count=0"
for /f "tokens=*" %%i in (strictmode-logcat.txt) do (
    if "%%i"=="*DiskWrite*" (set /a count+=1)
    if "%%i"=="*DiskRead*" (set /a count+=1)
)

echo.
echo ════════════════════════════════════════════
echo TEST RESULTS
echo ════════════════════════════════════════════
echo Total violations found: !count!
echo.

if !count! gtr 5 (
    echo VERDICT: PINStorage blocks main thread (violations ^> 5)
    echo ACTION: Implement DataStore migration in Phase 1B
) else (
    echo VERDICT: No clear violation pattern
    echo ACTION: Consider no optimization needed OR test again
)

echo.
echo Full logcat saved to: strictmode-logcat.txt
echo.

endlocal

