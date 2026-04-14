# Run tests and save output
Write-Output "Starting tests..."
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
./gradlew test --no-daemon 2>&1 | Tee-Object -FilePath test_final.log | Select-String "tests completed" | Out-File -FilePath test_summary.txt
Write-Output "Tests complete. Check test_final.log and test_summary.txt"

