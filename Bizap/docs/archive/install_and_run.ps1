$adbPath = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apkPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"

Write-Host "Checking for connected devices..."
$devicesOutput = & $adbPath devices -l
Write-Host $devicesOutput

if ($devicesOutput -match "emulator") {
    Write-Host "`nDevice found. Installing APK..."
    Write-Host "APK path: $apkPath"

    $installOutput = & $adbPath install -r $apkPath 2>&1
    Write-Host $installOutput

    if ($installOutput -match "Success") {
        Write-Host "`nAPK installed successfully!"

        Write-Host "`nWaiting for app to be ready..."
        Start-Sleep -Seconds 3

        Write-Host "Launching app..."
        $launchOutput = & $adbPath shell am start -n com.emul8r.bizap/.MainActivity 2>&1
        Write-Host $launchOutput

        Write-Host "`nApp launched! Waiting for startup..."
        Start-Sleep -Seconds 5

        Write-Host "`nChecking for crashes..."
        $logOutput = & $adbPath logcat -d -s AndroidRuntime:E 2>&1
        if ($logOutput) {
            Write-Host "Logcat output:"
            Write-Host $logOutput
        } else {
            Write-Host "No crashes detected."
        }
    } else {
        Write-Host "`nInstallation failed. Output: $installOutput"
    }
} else {
    Write-Host "`nNo emulator found. Please start an Android emulator first."
}

