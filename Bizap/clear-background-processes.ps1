# Clear All Background Processes - Development Cleanup Script
# This script stops unnecessary services and processes to free up system resources
# Useful for development, testing, and freeing memory before builds

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Background Process Cleaner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "ℹ️  This will stop:" -ForegroundColor Yellow
Write-Host "  1. Gradle daemon processes" -ForegroundColor Yellow
Write-Host "  2. Android emulator processes" -ForegroundColor Yellow
Write-Host "  3. ADB (Android Debug Bridge) server" -ForegroundColor Yellow
Write-Host "  4. Java background processes (optional)" -ForegroundColor Yellow
Write-Host "  5. Unused IDE processes (optional)" -ForegroundColor Yellow
Write-Host ""

$cleanAll = Read-Host "Clean ALL processes? (yes/no) [default: yes]"
if ($cleanAll -eq "") { $cleanAll = "yes" }

Write-Host ""
Write-Host "Step 1: Stopping Gradle Daemons..." -ForegroundColor Cyan
try {
    & "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\gradlew.bat" --stop 2>&1 | Out-Null
    Write-Host "✓ Gradle daemons stopped" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Could not stop Gradle via script" -ForegroundColor Yellow
}

# Forcefully kill gradle processes
$gradleProcesses = Get-Process -Name "gradle*" -ErrorAction SilentlyContinue
if ($gradleProcesses) {
    $gradleProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "✓ Force-killed Gradle processes" -ForegroundColor Green
}

Write-Host ""
Write-Host "Step 2: Stopping ADB Server..." -ForegroundColor Cyan
try {
    $adbPath = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $adbPath) {
        & $adbPath kill-server 2>&1 | Out-Null
        Write-Host "✓ ADB server stopped" -ForegroundColor Green
    } else {
        Write-Host "⚠️  ADB not found at $adbPath" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Could not stop ADB server" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Step 3: Stopping Android Emulator Processes..." -ForegroundColor Cyan
$emulatorProcesses = Get-Process -Name "emulator*" -ErrorAction SilentlyContinue
if ($emulatorProcesses) {
    $emulatorProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "✓ Emulator processes stopped" -ForegroundColor Green
} else {
    Write-Host "ℹ️  No emulator processes running" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Step 4: Stopping QEMU (Emulator backend)..." -ForegroundColor Cyan
$qemuProcesses = Get-Process -Name "qemu*" -ErrorAction SilentlyContinue
if ($qemuProcesses) {
    $qemuProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "✓ QEMU processes stopped" -ForegroundColor Green
} else {
    Write-Host "ℹ️  No QEMU processes running" -ForegroundColor Gray
}

if ($cleanAll -eq "yes") {
    Write-Host ""
    Write-Host "Step 5: Stopping Java Processes..." -ForegroundColor Cyan

    # Get Java processes but exclude IntelliJ/Android Studio
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -notlike "*IntelliJ*" -and
        $_.CommandLine -notlike "*AndroidStudio*"
    }

    if ($javaProcesses) {
        $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
        Write-Host "✓ Java background processes stopped" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  No background Java processes found" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "Step 6: Clearing Temporary Build Files..." -ForegroundColor Cyan
    $buildDir = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build"
    if (Test-Path $buildDir) {
        try {
            Remove-Item -Path "$buildDir\*" -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "✓ Build directory cleared" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  Could not fully clear build directory (files may be in use)" -ForegroundColor Yellow
        }
    }

    Write-Host ""
    Write-Host "Step 7: Clearing Gradle Cache..." -ForegroundColor Cyan
    $gradleCache = "$env:USERPROFILE\.gradle\caches"
    if (Test-Path $gradleCache) {
        Write-Host "ℹ️  Note: This will clear build cache and require fresh download" -ForegroundColor Yellow
        $clearCache = Read-Host "Clear Gradle cache? (yes/no) [default: no]"
        if ($clearCache -eq "yes") {
            try {
                Remove-Item -Path "$gradleCache\*" -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "✓ Gradle cache cleared" -ForegroundColor Green
            } catch {
                Write-Host "⚠️  Could not fully clear Gradle cache" -ForegroundColor Yellow
            }
        }
    }
}

Write-Host ""
Write-Host "Step 8: Memory & System Info..." -ForegroundColor Cyan
$memInfo = Get-CimInstance Win32_OperatingSystem | Select-Object @{Name="TotalMemory(GB)";Expression={[math]::Round($_.TotalVisibleMemorySize/1MB,2)}}, @{Name="FreeMemory(GB)";Expression={[math]::Round($_.FreePhysicalMemory/1MB,2)}}
$memInfo | Format-Table -AutoSize
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Cleanup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Run your build fresh" -ForegroundColor Yellow
Write-Host "  2. Start fresh emulator if needed" -ForegroundColor Yellow
Write-Host "  3. Run: .\fix-database-crash.ps1" -ForegroundColor Yellow
Write-Host ""

