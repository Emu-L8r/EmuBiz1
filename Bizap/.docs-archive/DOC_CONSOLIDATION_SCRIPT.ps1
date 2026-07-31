# Phase 2 Documentation Consolidation Script
# Purpose: Consolidate 500+ scattered docs into clean structure
# Safety: Dry-run mode available

param(
    [switch]$DryRun = $true,
    [string]$RepoRoot = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
)

$ErrorActionPreference = "Continue"

# Consolidation manifest
$ConsolidationPlan = @{
    "PROJECT_STATUS" = @{
        sources = @("PROJECT_STATUS.md", "PROJECT_STATUS_DASHBOARD.md")
        canonical = "PROJECT_STATUS_APRIL_7_2026.md"
        target = "docs/PROJECT_STATUS.md"
        archiveDir = "docs/archive/STATUS_ARCHIVE"
    }
    "START_HERE" = @{
        sources = @("START_HERE.md", "START_HERE_ATTEMPT_11_COMPLETE.md", "START_HERE_ATTEMPT_12.md", "START_HERE_CRASHLYTICS_EXECUTION.md", "START_HERE_REAL_DIAGNOSIS.md")
        canonical = "START_HERE_RC1_EDITION.md"
        target = "docs/START_HERE.md"
        archiveDir = "docs/archive/obsolete"
    }
}

Write-Host ""
Write-Host "========== PHASE 2: DOC CONSOLIDATION ==========" -ForegroundColor Cyan
Write-Host "Repository: $RepoRoot" -ForegroundColor Cyan
Write-Host "Dry Run: $DryRun" -ForegroundColor Cyan
Write-Host ""

# Phase 1: Backup old variants
Write-Host "[PHASE 1] BACKUP OLD DOCUMENT VARIANTS" -ForegroundColor Yellow
Write-Host ""

$backupCount = 0
foreach ($category in $ConsolidationPlan.Keys) {
    $plan = $ConsolidationPlan[$category]
    $archiveDir = Join-Path -Path $RepoRoot -ChildPath $plan.archiveDir

    foreach ($source in $plan.sources) {
        $sourcePath = Join-Path -Path $RepoRoot -ChildPath "docs\$source"

        if (Test-Path -Path $sourcePath) {
            $archivePath = Join-Path -Path $archiveDir -ChildPath $source

            if (-not $DryRun) {
                if (-not (Test-Path -Path $archiveDir)) {
                    New-Item -ItemType Directory -Path $archiveDir -Force | Out-Null
                }
                Move-Item -Path $sourcePath -Destination $archivePath -Force
                Write-Host "  OK Archived: $source" -ForegroundColor Green
            } else {
                Write-Host "  [DRY] Archive: $source to archive/" -ForegroundColor Gray
            }
            $backupCount++
        }
    }
}

Write-Host "  Total: $backupCount files" -ForegroundColor Cyan
Write-Host ""

# Phase 2: Copy canonical versions to consolidated locations
Write-Host "[PHASE 2] CREATE CONSOLIDATED DOCUMENTS" -ForegroundColor Yellow
Write-Host ""

$consolidateCount = 0
foreach ($category in $ConsolidationPlan.Keys) {
    $plan = $ConsolidationPlan[$category]
    $canonicalPath = Join-Path -Path $RepoRoot -ChildPath "docs\$($plan.canonical)"
    $targetPath = Join-Path -Path $RepoRoot -ChildPath $plan.target

    if (Test-Path -Path $canonicalPath) {
        if (-not $DryRun) {
            Copy-Item -Path $canonicalPath -Destination $targetPath -Force
            Write-Host "  OK Consolidated: $($plan.target)" -ForegroundColor Green
        } else {
            Write-Host "  [DRY] Copy: $($plan.canonical)" -ForegroundColor Gray
        }
        $consolidateCount++
    }
}

Write-Host "  Total: $consolidateCount files" -ForegroundColor Cyan
Write-Host ""

# Phase 3: Summary
Write-Host "========== SUMMARY ==========" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backed up variants:     $backupCount" -ForegroundColor Cyan
Write-Host "Consolidated documents: $consolidateCount" -ForegroundColor Cyan
Write-Host "TOTAL OPERATIONS:       $($backupCount + $consolidateCount)" -ForegroundColor Green
Write-Host ""

if ($DryRun) {
    Write-Host "DRY RUN COMPLETE - No files modified" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To execute: .\DOC_CONSOLIDATION_SCRIPT.ps1 -DryRun:$false" -ForegroundColor Green
} else {
    Write-Host "CONSOLIDATION COMPLETE" -ForegroundColor Green
}

Write-Host ""
Write-Host "========== END ==========" -ForegroundColor Cyan
Write-Host ""




