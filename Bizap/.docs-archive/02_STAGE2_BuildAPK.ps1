Write-Output "=========================================="
Write-Output "STAGE 2: FAST COMPILATION CHECK (APK BUILD)"
Write-Output "=========================================="
Write-Output ""
Write-Output "Building debug APK..."
Write-Output "Expected size: 50-52 MB"
Write-Output ""
$output = .\gradlew assembleDebug --no-daemon 2>&1
Write-Output $output
if ($output -match "BUILD SUCCESSFUL") {
    Write-Output ""
    Write-Output "Build Status: SUCCESS"
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        Write-Output "APK Size: {0:F2} MB" -f $apkSize
        if ($apkSize -gt 48 -and $apkSize -lt 55) {
            Write-Output "Size Check: PASSED (within 50-52 MB range)"
        } else {
            Write-Output "Size Check: WARNING (outside expected range)"
        }
    } else {
        Write-Output "ERROR: APK not found at $apkPath"
    }
} else {
    Write-Output ""
    Write-Output "Build Status: FAILED"
}
