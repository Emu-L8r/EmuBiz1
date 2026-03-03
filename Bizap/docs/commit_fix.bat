@echo off
REM Commit the Json provider fix

cd /d C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

echo Committing NetworkModule.kt fix...
git add app/src/main/java/com/emul8r/bizap/di/NetworkModule.kt

git commit -m "Fix: Add Hilt @Provides method for kotlinx.serialization.json.Json - OfflineSyncQueue was trying to inject Json but Hilt had no binding - Added @Provides @Singleton fun provideJson() in NetworkModule - Configured with ignoreUnknownKeys = true for server response flexibility - Fixes [Dagger/MissingBinding] error during build"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Commit successful
) else (
    echo ✗ Commit failed or nothing to commit
)

echo.
echo Checking git status...
git status

pause

