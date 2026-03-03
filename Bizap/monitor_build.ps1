# Monitor build progress
Write-Host "Monitoring build progress..."
$maxWait = 300 # 5 minutes
$elapsed = 0
$checkInterval = 10

while ($elapsed -lt $maxWait) {
    $apkPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"

    if (Test-Path $apkPath) {
        $fileInfo = Get-Item $apkPath
        Write-Host "✓ BUILD SUCCESS!"
        Write-Host "APK: $apkPath"
        Write-Host "Size: $([Math]::Round($fileInfo.Length / 1MB, 2)) MB"
        Write-Host "Created: $($fileInfo.CreationTime)"
        exit 0
    }

    Write-Host "... Waiting for APK ($elapsed/$maxWait seconds)"
    Start-Sleep -Seconds $checkInterval
    $elapsed += $checkInterval
}

Write-Host "✗ Build did not complete within timeout"
exit 1

