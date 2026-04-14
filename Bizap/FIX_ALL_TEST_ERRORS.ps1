#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Complete Test Compilation Fix - Phase B Implementation
    Fixes all remaining test compilation errors systematically

.DESCRIPTION
    This script automatically fixes:
    1. Parameter names: date → dateCreated, dueDate
    2. Type names: LineItem → InvoiceItem
    3. Missing imports for InvoiceItem and java.time.Instant
    4. currencyCode → currency
    5. dueDate = System.currentTimeMillis() arithmetic fixes

.NOTES
    Session: April 12, 2026
    Total estimated fixes: 100+ errors
#>

$ErrorActionPreference = "Stop"
$projectRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$testDir = "$projectRoot\app\src\test\java\com\emul8r\bizap"
$now = Get-Date -Format "yyyy-MM-dd_HHmmss"
$logFile = "$projectRoot\FIX_ALL_TEST_ERRORS_$now.log"
$fixedFiles = @()
$errorLog = @()

function Log {
    param([string]$message)
    Write-Host $message
    Add-Content $logFile $message
}

Log "╔════════════════════════════════════════════════════════════════╗"
Log "║        COMPREHENSIVE TEST COMPILATION FIX - PHASE B            ║"
Log "║                    Starting at $(Get-Date)                   ║"
Log "╚════════════════════════════════════════════════════════════════╝"
Log ""

# Find all test files that likely have issues
$testFiles = @(
    "$testDir\data\repository\InvoiceRepositoryTest.kt",
    "$testDir\data\service\OfflineQueueServiceSuite3Test.kt",
    "$testDir\domain\usecase\CalculateInvoiceMetricsUseCaseTest.kt",
    "$testDir\domain\usecase\CreateInvoiceUseCaseTest.kt",
    "$testDir\domain\validation\InvoiceValidationTest.kt",
    "$testDir\integration\Week1InvoiceLifecycleTest.kt",
    "$testDir\test\TestDataBuilder.kt",
    "$testDir\ui\gui2\invoices\CreateInvoiceViewModelTest.kt",
    "$testDir\ui\gui2\invoices\EditInvoiceViewModelTest.kt",
    "$testDir\ui\invoices\LineItemDataFlowTest.kt",
    "$testDir\ui\invoices\UnifiedCreateInvoicePageTest.kt"
)

# Fix 1: Replace old Invoice constructor calls with new parameters
Log ""
Log "[Fix 1/5] Fixing Invoice() constructor calls..."
$replacements = @{
    'date = 0L' = 'dateCreated = java.time.Instant.now().toString(), dueDate = java.time.Instant.now().toString()'
    'date = System.currentTimeMillis()' = 'dateCreated = java.time.Instant.now().toString()'
}

foreach ($file in $testFiles) {
    if (-not (Test-Path $file)) { continue }

    try {
        $content = Get-Content $file -Raw
        $originalContent = $content

        # Replace all old parameter patterns
        foreach ($oldPattern in $replacements.Keys) {
            $newValue = $replacements[$oldPattern]
            if ($content -match [regex]::Escape($oldPattern)) {
                $content = $content -replace [regex]::Escape($oldPattern), $newValue
                Log "  ✓ Replaced '$oldPattern' in $(Split-Path -Leaf $file)"
            }
        }

        # Replace dueDate arithmetic with new format
        $dueDatePattern = 'dueDate = System\.currentTimeMillis\(\)\s*\+\s*(\d+_?\d+L?)'
        if ($content -match $dueDatePattern) {
            $content = $content -replace $dueDatePattern, 'dueDate = java.time.Instant.now().plusSeconds($1/1000L).toString()'
            Log "  ✓ Fixed dueDate arithmetic in $(Split-Path -Leaf $file)"
        }

        if ($content -ne $originalContent) {
            Set-Content $file $content
            $fixedFiles += (Split-Path -Leaf $file)
        }
    }
    catch {
        $errorLog += "Error processing $file: $_"
        Log "  ✗ Error in $(Split-Path -Leaf $file): $_"
    }
}

# Fix 2: Replace currencyCode with currency
Log ""
Log "[Fix 2/5] Fixing currencyCode → currency..."
foreach ($file in $testFiles) {
    if (-not (Test-Path $file)) { continue }

    try {
        $content = Get-Content $file -Raw
        $originalContent = $content

        if ($content -match 'currencyCode') {
            $content = $content -replace 'currencyCode', 'currency'
            Set-Content $file $content
            Log "  ✓ Fixed currencyCode in $(Split-Path -Leaf $file)"
            $fixedFiles += (Split-Path -Leaf $file)
        }
    }
    catch {
        $errorLog += "Error processing $file: $_"
    }
}

# Fix 3: Replace LineItem with InvoiceItem
Log ""
Log "[Fix 3/5] Fixing LineItem → InvoiceItem..."
foreach ($file in $testFiles) {
    if (-not (Test-Path $file)) { continue }

    try {
        $content = Get-Content $file -Raw
        $originalContent = $content

        if ($content -match 'LineItem') {
            # Replace LineItem type references
            $content = $content -replace 'List<LineItem>', 'List<InvoiceItem>'
            $content = $content -replace 'LineItem\(', 'InvoiceItem('
            $content = $content -replace 'createValidLineItem', 'createValidInvoiceItem'
            $content = $content -replace 'buildLineItem', 'buildInvoiceItem'

            Set-Content $file $content
            Log "  ✓ Fixed LineItem references in $(Split-Path -Leaf $file)"
            $fixedFiles += (Split-Path -Leaf $file)
        }
    }
    catch {
        $errorLog += "Error processing $file: $_"
    }
}

# Fix 4: Add missing imports
Log ""
Log "[Fix 4/5] Adding missing imports..."
foreach ($file in $testFiles) {
    if (-not (Test-Path $file)) { continue }

    try {
        $content = Get-Content $file -Raw
        $originalContent = $content

        $needsInvoiceItemImport = $content -match 'InvoiceItem' -and $content -notmatch 'import.*InvoiceItem'
        $needsInstantImport = $content -match 'Instant\.now' -and $content -notmatch 'import.*Instant'

        if ($needsInvoiceItemImport) {
            $importLine = "`nimport com.emul8r.bizap.domain.model.InvoiceItem`n"
            $content = $content -replace '(package .*?\n)', "`$1$importLine"
            Log "  ✓ Added InvoiceItem import to $(Split-Path -Leaf $file)"
        }

        if ($needsInstantImport) {
            $importLine = "import java.time.Instant`n"
            if ($content -notmatch 'import java\.time') {
                $content = $content -replace '(package .*?\n)', "`$1$importLine"
                Log "  ✓ Added Instant import to $(Split-Path -Leaf $file)"
            }
        }

        if ($content -ne $originalContent) {
            Set-Content $file $content
            $fixedFiles += (Split-Path -Leaf $file)
        }
    }
    catch {
        $errorLog += "Error processing $file: $_"
    }
}

# Fix 5: Global replacements for remaining patterns
Log ""
Log "[Fix 5/5] Global pattern replacements..."
$globalPatterns = @{
    'balanceRemaining' = 'balanceRemaining'  # Already correct, but ensure imports
    'isFullyPaid' = 'isFullyPaid'            # Already correct, but ensure imports
}

Get-ChildItem $testDir -Recurse -Filter "*Test.kt" | ForEach-Object {
    try {
        $file = $_.FullName
        $content = Get-Content $file -Raw
        $originalContent = $content

        # Ensure extension property imports if used
        if ($content -match '\.balanceRemaining|\.isFullyPaid') {
            if ($content -notmatch 'import.*balanceRemaining|import.*isFullyPaid') {
                # Add to imports from InvoiceExtensions or domain.model
                if ($content -match 'import com.emul8r.bizap.domain.model') {
                    $content = $content -replace '(import com\.emul8r\.bizap\.domain\.model)', "`$1`nimport com.emul8r.bizap.domain.model.balanceRemaining`nimport com.emul8r.bizap.domain.model.isFullyPaid"
                }
            }
        }

        if ($content -ne $originalContent) {
            Set-Content $file $content
            $fixedFiles += (Split-Path -Leaf $file)
        }
    }
    catch {
        # Silently skip files that can't be processed
    }
}

# Summary
Log ""
Log "╔════════════════════════════════════════════════════════════════╗"
Log "║                     FIXES COMPLETED                            ║"
Log "╚════════════════════════════════════════════════════════════════╝"
Log ""
Log "Files modified: $($fixedFiles.Count)"
$fixedFiles | Sort-Object -Unique | ForEach-Object { Log "  ✓ $_" }

if ($errorLog.Count -gt 0) {
    Log ""
    Log "Errors encountered:"
    $errorLog | ForEach-Object { Log "  ✗ $_" }
}

Log ""
Log "Next step: Run './gradlew test' to verify fixes"
Log ""
Log "Log file saved to: $logFile"

