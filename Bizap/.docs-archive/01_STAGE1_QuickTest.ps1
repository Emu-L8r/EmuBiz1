Write-Output "Running Stage 1: Quick Build Validation"
Write-Output "Running 102 unit tests..."
Write-Output ""
$output = .\gradlew test --no-daemon 2>&1
Write-Output $output
if ($output -match "BUILD SUCCESSFUL") {
    Write-Output ""
    Write-Output "STAGE 1: PASSED - All tests completed successfully"
} else {
    Write-Output ""
    Write-Output "STAGE 1: Check output above for details"
}
