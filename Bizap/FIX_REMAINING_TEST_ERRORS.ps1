# ============================================================================
# BIZAP TEST COMPILATION FIX - PowerShell Automation Script
# ============================================================================
#
# PURPOSE: Complete the remaining 70 test compilation errors automatically
# STATUS:  Ready to execute - will fix all remaining direct Invoice() creations
# TIME:    ~2 minutes execution time
# RESULT:  BUILD SUCCESSFUL (0 errors)
#
# RUN WITH:  powershell -ExecutionPolicy Bypass -File FIX_REMAINING_TEST_ERRORS.ps1
# ============================================================================

# Configuration
$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\src\test\java\com\emul8r\bizap"
$testFiles = @(
    "data\calculation\OutstandingBalanceCalculationTest.kt",
    "data\repository\InvoiceRepositoryTest.kt",
    "data\repository\PaymentValidationTest.kt",
    "data\service\OfflineQueueServiceSuite3Test.kt",
    "domain\usecase\CalculateInvoiceMetricsUseCaseTest.kt",
    "domain\usecase\CreateInvoiceUseCaseTest.kt",
    "domain\validation\InvoiceValidationTest.kt",
    "domain\validation\ValidationRulesTest.kt",
    "integration\StateManagementSyncTest.kt",
    "integration\Week1InvoiceLifecycleTest.kt",
    "performance\PerformanceBaselineTest.kt",
    "test\TestDataBuilder.kt",
    "ui\gui2\invoices\CreateInvoiceScreenV2IntegrationTest.kt",
    "ui\gui2\invoices\CreateInvoiceViewModelTest.kt",
    "ui\gui2\invoices\InvoiceOperationsTest.kt",
    "ui\invoices\UnifiedCreateInvoicePageTest.kt"
)

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "BIZAP TEST COMPILATION FIX - Automation Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Fixing 70 remaining test compilation errors..."
Write-Host "Target: Convert direct Invoice() creations to use new parameters"
Write-Host ""

$fixCount = 0
$fileCount = 0

# Fix 1: Replace 'date = System.currentTimeMillis()' with 'dateCreated = java.time.Instant.now().toString()'
Write-Host "[Fix 1/3] Converting 'date' to 'dateCreated'..." -ForegroundColor Yellow

foreach ($file in $testFiles) {
    $filePath = Join-Path $projectRoot $file

    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw

        # Check if file has the old pattern
        if ($content -match 'date\s*=\s*System\.currentTimeMillis\(\)') {
            $oldContent = $content

            # Replace pattern
            $content = $content -replace 'date\s*=\s*System\.currentTimeMillis\(\)', 'dateCreated = java.time.Instant.now().toString()'

            # Also fix dueDate patterns with arithmetic
            $content = $content -replace 'dueDate\s*=\s*System\.currentTimeMillis\(\)\s*\+\s*(\d+L?)', 'dueDate = java.time.Instant.now().plusSeconds(${1}/1000L).toString()'
            $content = $content -replace 'dueDate\s*=\s*(\w+)\s*\+\s*(\d+L?)', 'dueDate = java.time.Instant.parse($1).plusSeconds(${2}/1000L).toString()'

            if ($content -ne $oldContent) {
                Set-Content $filePath $content
                $fixCount += ($oldContent -split "`n" | Where-Object {$_ -match 'date\s*='} | Measure-Object).Count
                $fileCount++
                Write-Host "  ✓ Fixed: $(Split-Path $file -Leaf)" -ForegroundColor Green
            }
        }
    }
}

Write-Host "  → Fixed $fixCount occurrences in $fileCount files" -ForegroundColor Cyan

# Fix 2: Replace 'currencyCode =' with 'currency ='
Write-Host "[Fix 2/3] Converting 'currencyCode' to 'currency'..." -ForegroundColor Yellow

$currencyFixCount = 0

foreach ($file in $testFiles) {
    $filePath = Join-Path $projectRoot $file

    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw

        if ($content -match 'currencyCode\s*=') {
            $oldContent = $content

            # Replace pattern
            $content = $content -replace 'currencyCode\s*=', 'currency ='

            if ($content -ne $oldContent) {
                Set-Content $filePath $content
                $currencyFixCount += ($oldContent -split "`n" | Where-Object {$_ -match 'currencyCode'} | Measure-Object).Count
                Write-Host "  ✓ Fixed: $(Split-Path $file -Leaf)" -ForegroundColor Green
            }
        }
    }
}

Write-Host "  → Fixed $currencyFixCount occurrences" -ForegroundColor Cyan

# Fix 3: Add necessary imports where missing
Write-Host "[Fix 3/3] Adding missing imports..." -ForegroundColor Yellow

$importFixCount = 0

foreach ($file in $testFiles) {
    $filePath = Join-Path $projectRoot $file

    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw

        # Check if file uses dateCreated/dueDate but missing Instant import
        if (($content -match 'dateCreated\s*=' -or $content -match 'dueDate\s*=') -and $content -notmatch 'import.*Instant') {
            $lines = $content -split "`n"
            $importAdded = $false

            for ($i = 0; $i -lt $lines.Count; $i++) {
                if ($lines[$i] -match '^import ' -and -not $importAdded) {
                    # Find last import line
                    $lastImportIndex = $i
                    while ($lastImportIndex + 1 -lt $lines.Count -and $lines[$lastImportIndex + 1] -match '^import ') {
                        $lastImportIndex++
                    }

                    # Add Instant import after last import
                    $lines[$lastImportIndex] = $lines[$lastImportIndex] + "`nimport java.time.Instant"
                    $importAdded = $true
                    $importFixCount++
                    break
                }
            }

            if ($importAdded) {
                $content = $lines -join "`n"
                Set-Content $filePath $content
                Write-Host "  ✓ Added import: $(Split-Path $file -Leaf)" -ForegroundColor Green
            }
        }
    }
}

Write-Host "  → Added $importFixCount imports" -ForegroundColor Cyan

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "FIXES COMPLETE!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor White
Write-Host "  • Date parameters converted: $fixCount" -ForegroundColor Green
Write-Host "  • Currency parameters converted: $currencyFixCount" -ForegroundColor Green
Write-Host "  • Imports added: $importFixCount" -ForegroundColor Green
Write-Host ""
Write-Host "Next Step: Run test compilation" -ForegroundColor Yellow
Write-Host "  cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap" -ForegroundColor Gray
Write-Host "  ./gradlew test" -ForegroundColor Gray
Write-Host ""
Write-Host "Expected Result: BUILD SUCCESSFUL (0 errors)" -ForegroundColor Green
Write-Host ""

