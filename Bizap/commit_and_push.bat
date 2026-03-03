@echo off
REM Commit NetworkModule.kt fix and verify

cd /d C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

echo ================================================
echo Committing NetworkModule.kt fix
echo ================================================
echo.

echo Step 1: Check git status...
git status
echo.

echo Step 2: Add file...
git add app/src/main/java/com/emul8r/bizap/di/NetworkModule.kt
echo ✓ File added

echo.
echo Step 3: Commit...
git commit -m "Fix: Add Hilt @Provides method for kotlinx.serialization.json.Json

- OfflineSyncQueue was trying to inject Json but Hilt had no binding
- Added @Provides @Singleton fun provideJson() in NetworkModule
- Configured with ignoreUnknownKeys = true for server response flexibility
- Fixes [Dagger/MissingBinding] error during build"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Commit successful
) else (
    echo ℹ Nothing new to commit or already committed
)

echo.
echo Step 4: Check git log...
git log --oneline -3
echo.

echo Step 5: Push to main...
git push origin main
if %ERRORLEVEL% EQU 0 (
    echo ✓ Push successful
) else (
    echo ⚠ Push had an issue - check output above
)

echo.
echo ================================================
echo Git Operations Complete
echo ================================================
echo.

echo Final Status:
git status

pause

