#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Batch fix all remaining test compilation errors
    Processes all test files with systematic replacements
#>

$ErrorActionPreference = "Continue"
$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$testDir = "$projectRoot\app\src\test\java\com\emul8r\bizap"
$now = Get-Date -Format "yyyy-MM-dd_HHmmss"

Write-Host "🔧 BATCH FIX: All Remaining Test Compilation Errors"
Write-Host "=================================================="
Write-Host ""

# Get all test files
$allTestFiles = Get-ChildItem $testDir -Recurse -Filter "*Test.kt"
Write-Host "Found $($allTestFiles.Count) test files to process"
Write-Host ""

$fixed = 0
$errors = 0

foreach ($file in $allTestFiles) {
    try {
        $content = Get-Content $file.FullName -Raw
        $original = $content

        # Fix 1: Replace LineItem with InvoiceItem
        $content = $content -replace 'import com\.emul8r\.bizap\.domain\.model\.LineItem', 'import com.emul8r.bizap.domain.model.InvoiceItem'
        $content = $content -replace 'List<LineItem>', 'List<InvoiceItem>'
        $content = $content -replace '\bLineItem\(', 'InvoiceItem('

        # Fix 2: Replace date parameters (Long-based)
        # date = System.currentTimeMillis() → dateCreated = Instant.now().toString()
        $content = $content -replace 'date\s*=\s*System\.currentTimeMillis\(\)', 'dateCreated = Instant.now().toString()'

        # date = now (where now is Long) → dateCreated = now.toString() or similar
        $content = $content -replace 'date\s*=\s*now([,\)])', 'dateCreated = now$1'
        $content = $content -replace 'dueDate\s*=\s*now\s*\+\s*86_?400_?000L', 'dueDate = Instant.now().plusSeconds(86_400L).toString()'
        $content = $content -replace 'dueDate\s*=\s*tomorrow', 'dueDate = tomorrow'

        # Fix 3: Replace currencyCode with currency
        $content = $content -replace 'currencyCode\s*=', 'currency ='

        # Fix 4: Add Instant import if needed
        if (($content -match 'Instant\.now|dateCreated\s*=|dueDate\s*=') -and $content -notmatch 'import java\.time\.Instant') {
            if ($content -match 'import java\.time\.' -or $content -match 'import java\.' ) {
                $content = $content -replace '(import java\.time\..*?\n)', "`$1import java.time.Instant`n"
            } else {
                $content = $content -replace '(package .*?\n)', "`$1import java.time.Instant`n"
            }
        }

        # Fix 5: Add InvoiceItem import if missing
        if (($content -match 'InvoiceItem\(') -and $content -notmatch 'import.*InvoiceItem') {
            $content = $content -replace '(import com\.emul8r\.bizap\.domain\.model.*?\n)', "`$1import com.emul8r.bizap.domain.model.InvoiceItem`n"
        }

        if ($content -ne $original) {
            Set-Content $file.FullName $content -NoNewline
            $fixed++
            Write-Host "✓ $(Split-Path -Leaf $file.FullName)"
        }
    }
    catch {
        $errors++
        Write-Host "✗ $(Split-Path -Leaf $file.FullName): $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=================================================="
Write-Host "✅ Batch fix complete!"
Write-Host "   Files fixed: $fixed"
Write-Host "   Errors: $errors"
Write-Host ""
Write-Host "Next: Run ./gradlew compileDebugUnitTestKotlin"

