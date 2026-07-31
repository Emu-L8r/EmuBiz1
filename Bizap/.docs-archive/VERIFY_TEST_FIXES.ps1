#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Verification script to confirm all test fixes are in place

.DESCRIPTION
    Scans all test files for remaining issues and reports status
#>

$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$testDir = "$projectRoot\app\src\test\java\com\emul8r\bizap"

Write-Host "🔍 VERIFICATION: Test Compilation Fixes"
Write-Host "=========================================="
Write-Host ""

$issues = @{
    "LineItem imports" = 0
    "currencyCode references" = 0
    "date = System.currentTimeMillis()" = 0
    "date = \d+L" = 0
    "Missing Instant imports" = 0
}

$checksPerformed = 0
$filesScanned = 0

# Check 1: LineItem imports
Write-Host "Checking for LineItem imports..."
$lineItemImports = Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match 'import.*LineItem') { $_ }
}
$issues["LineItem imports"] = $lineItemImports.Count
$checksPerformed++
$filesScanned += (Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | Measure-Object).Count

# Check 2: currencyCode references
Write-Host "Checking for currencyCode references..."
$currencyCodeRefs = Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match 'currencyCode\s*=') { $_ }
}
$issues["currencyCode references"] = $currencyCodeRefs.Count
$checksPerformed++

# Check 3: Old date patterns
Write-Host "Checking for date = System.currentTimeMillis() patterns..."
$systemMillisRefs = Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match 'date\s*=\s*System\.currentTimeMillis\(\)') { $_ }
}
$issues["date = System.currentTimeMillis()"] = $systemMillisRefs.Count
$checksPerformed++

# Check 4: Numeric date assignments (might be entity models - check context)
Write-Host "Checking for numeric date assignments in domain models..."
$numericDateRefs = Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | Where-Object { $_.FullName -notmatch "Dao|Entity" } | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match 'Invoice\(.*date\s*=\s*\d+L' -and $content -notmatch 'InvoiceEntity') { $_ }
}
$issues["date = \d+L"] = $numericDateRefs.Count
$checksPerformed++

# Check 5: Missing Instant imports where needed
Write-Host "Checking for missing Instant imports..."
$missingInstantImports = Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if (($content -match 'Instant\.now|dateCreated.*Instant') -and $content -notmatch 'import java\.time\.Instant') { $_ }
}
$issues["Missing Instant imports"] = $missingInstantImports.Count
$checksPerformed++

Write-Host ""
Write-Host "=========================================="
Write-Host "✅ VERIFICATION RESULTS"
Write-Host "=========================================="
Write-Host ""

$allClear = $true
foreach ($issue in $issues.GetEnumerator() | Sort-Object Value -Descending) {
    if ($issue.Value -eq 0) {
        Write-Host "✅ $($issue.Name): PASS (0 issues)"
    } else {
        Write-Host "⚠️  $($issue.Name): $($issue.Value) issues found"
        $allClear = $false
    }
}

Write-Host ""
Write-Host "=========================================="
Write-Host "Summary:"
Write-Host "  Files Scanned: $filesScanned"
Write-Host "  Checks Performed: $checksPerformed"
Write-Host "  Total Issues Found: $($issues.Values | Measure-Object -Sum | Select-Object -ExpandProperty Sum)"
Write-Host "=========================================="
Write-Host ""

if ($allClear) {
    Write-Host "🎉 ALL CHECKS PASSED!"
    Write-Host "✅ Ready to compile: ./gradlew compileDebugUnitTestKotlin"
    exit 0
} else {
    Write-Host "⚠️  Some issues remain. Review the issues above."
    exit 1
}

