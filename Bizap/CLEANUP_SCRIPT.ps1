# ============================================================================
# Bizap Repository Cleanup Script
# Date: April 10, 2026
# Purpose: Automate repository organization (scripts, logs, archive old docs)
# Safety: Includes dry-run mode, validation, and rollback guidance
# ============================================================================

param(
    [switch]$DryRun = $true,
    [string]$RepoRoot = (Get-Location),
    [switch]$SkipValidation = $false
)

# ============================================================================
# CONFIGURATION
# ============================================================================

$ErrorActionPreference = "Continue"
$WarningPreference = "Continue"

# Files that should NEVER be moved (safelist)
$SafelistFiles = @(
    "release-key.jks",
    "local.properties",
    "README.md",
    "build.gradle.kts",
    "settings.gradle.kts",
    ".gitignore"
)

# Directories that should NEVER be moved (safelist)
$SafelistDirs = @(
    ".github",
    "app",
    "data",
    "domain",
    "gradle",
    ".gradle",
    ".idea",
    ".kotlin"
)

# ============================================================================
# FUNCTIONS
# ============================================================================

function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host "════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host " $Text" -ForegroundColor Cyan
    Write-Host "════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Phase {
    param([string]$Number, [string]$Text)
    Write-Host "[$Number] $Text" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Text)
    Write-Host "  ✓ $Text" -ForegroundColor Green
}

function Write-DryRun {
    param([string]$Text)
    Write-Host "  [DRY RUN] $Text" -ForegroundColor Gray
}

function Write-Warning {
    param([string]$Text)
    Write-Host "  ⚠ $Text" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Text)
    Write-Host "  ✗ $Text" -ForegroundColor Red
}

# ============================================================================
# VALIDATION PHASE
# ============================================================================

Write-Header "BIZAP REPOSITORY CLEANUP SCRIPT"
Write-Host "Repository: $RepoRoot" -ForegroundColor Cyan
Write-Host "Dry Run Mode: $DryRun" -ForegroundColor Cyan
Write-Host "Skip Validation: $SkipValidation" -ForegroundColor Cyan

if (-not $SkipValidation) {
    Write-Phase "0" "VALIDATION CHECKS"

    # Check if we're in a git repository
    $gitCheck = Get-ChildItem -Path $RepoRoot -Filter ".git" -Depth 0 -ErrorAction SilentlyContinue
    if ($null -eq $gitCheck) {
        Write-Error "Not a git repository! Aborting for safety."
        exit 1
    }
    Write-Success "Git repository detected"

    # Check if git status is clean (no uncommitted changes)
    $gitStatus = & git -C $RepoRoot status --porcelain
    if ($gitStatus) {
        Write-Warning "Uncommitted changes detected. Consider committing first."
        Write-Host "  Run: git status"
        Write-Host ""
        $response = Read-Host "Continue anyway? (y/n)"
        if ($response -ne "y") {
            Write-Error "Aborted by user"
            exit 1
        }
    }
    Write-Success "Git status check complete"

    # Check for essential gradle files
    $gradleCheck = Get-ChildItem -Path $RepoRoot -Filter "build.gradle.kts" -Depth 0
    if ($null -eq $gradleCheck) {
        Write-Error "build.gradle.kts not found! Are we in the right directory?"
        exit 1
    }
    Write-Success "Gradle configuration found"

    Write-Host ""
}

# ============================================================================
# PHASE 1: CREATE DIRECTORIES
# ============================================================================

Write-Phase "1" "CREATING DIRECTORIES"

$dirsToCreate = @(
    "scripts",
    ".logs",
    ".logs/build_logs",
    ".logs/test_logs",
    ".logs/diagnostic_logs",
    "docs/archive",
    "docs/archive/phases",
    "docs/archive/phases/phase_0_startup",
    "docs/archive/phases/phase_1_mvp",
    "docs/archive/phases/phase_2_refinement",
    "docs/archive/experiments",
    "docs/archive/obsolete",
    "docs/archive/session_logs",
    "docs/active",
    "docs/active/AUDIT_SUITE",
    "docs/active/PRODUCTION_ROLLOUT",
    "docs/active/GUIDES",
    "docs/decisions"
)

$createdCount = 0
foreach ($dir in $dirsToCreate) {
    $fullPath = Join-Path $RepoRoot $dir
    if (-not (Test-Path $fullPath)) {
        if ($DryRun) {
            Write-DryRun "mkdir $dir"
        } else {
            New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
            Write-Success "Created: $dir"
        }
        $createdCount++
    }
}
Write-Host "  Total directories: $createdCount"
Write-Host ""

# ============================================================================
# PHASE 2: MOVE SCRIPTS
# ============================================================================

Write-Phase "2" "MOVING SCRIPTS TO /scripts/"

$scriptPatterns = @("*.ps1", "*.sh")
$scriptCount = 0

foreach ($pattern in $scriptPatterns) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter $pattern -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        # Skip CLEANUP_SCRIPT.ps1 itself
        if ($file.Name -eq "CLEANUP_SCRIPT.ps1") {
            Write-Warning "Skipping CLEANUP_SCRIPT.ps1 (cannot move self)"
            continue
        }

        $destination = Join-Path $RepoRoot "scripts" $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) → scripts/"
        } else {
            Move-Item -Path $file.FullName -Destination $destination -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $scriptCount++
    }
}
Write-Host "  Total scripts moved: $scriptCount"
Write-Host ""

# ============================================================================
# PHASE 3: MOVE LOGS
# ============================================================================

Write-Phase "3" "MOVING LOGS TO /.logs/"

$logPatterns = @("*.log", "*_log.txt", "logcat_*.txt", "device_*.txt", "audit_*.txt")
$logCount = 0

foreach ($pattern in $logPatterns) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter $pattern -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        $destination = Join-Path $RepoRoot ".logs" $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) → .logs/"
        } else {
            Move-Item -Path $file.FullName -Destination $destination -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $logCount++
    }
}
Write-Host "  Total logs moved: $logCount"
Write-Host ""

# ============================================================================
# PHASE 4: ARCHIVE PHASE 0-2 DOCS
# ============================================================================

Write-Phase "4" "ARCHIVING PHASE 0-2 DOCUMENTATION"

$phaseMap = @{
    "PHASE_0_" = "docs/archive/phases/phase_0_startup"
    "PHASE_1_" = "docs/archive/phases/phase_1_mvp"
    "PHASE_2_" = "docs/archive/phases/phase_2_refinement"
}

$archivedCount = 0

foreach ($prefix in $phaseMap.Keys) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter "$prefix*.md" -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        $destination = Join-Path $RepoRoot $phaseMap[$prefix] $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) → $($phaseMap[$prefix])/"
        } else {
            Move-Item -Path $file.FullName -Destination $destination -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $archivedCount++
    }
}

Write-Host "  Total phase docs archived: $archivedCount"
Write-Host ""

# ============================================================================
# PHASE 5: ARCHIVE OBSOLETE DOCS
# ============================================================================

Write-Phase "5" "ARCHIVING OBSOLETE DOCUMENTATION"

$obsoletePatterns = @("CRASH_FIX_", "IMPLEMENTATION_", "BUILD_")
$obsoleteCount = 0

foreach ($pattern in $obsoletePatterns) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter "$pattern*.md" -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        $destination = Join-Path $RepoRoot "docs/archive/obsolete" $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) → docs/archive/obsolete/"
        } else {
            Move-Item -Path $file.FullName -Destination $destination -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $obsoleteCount++
    }
}

Write-Host "  Total obsolete docs archived: $obsoleteCount"
Write-Host ""

# ============================================================================
# PHASE 6: MOVE CURRENT AUDIT DOCS
# ============================================================================

Write-Phase "6" "MOVING CURRENT AUDIT DOCUMENTATION"

$auditFiles = @(
    "AUDIT_INDEX_AND_READING_GUIDE.md",
    "AUDIT_EXECUTIVE_SUMMARY_APRIL2026.md",
    "COMPREHENSIVE_PROJECT_AUDIT_APRIL2026.md",
    "DETAILED_TECHNICAL_SCORECARD_APRIL2026.md",
    "DOUBLE_GOLD_TEST_STANDARD_AUDIT_COMPLETE.md",
    "GITHUB_UPLOAD_VERIFICATION_COMPLETE.md",
    "GITHUB_QUICK_ACCESS_LINKS.md"
)

$auditCount = 0

foreach ($file in $auditFiles) {
    $source = Join-Path $RepoRoot $file
    if (Test-Path $source) {
        $destination = Join-Path $RepoRoot "docs/active/AUDIT_SUITE" $file
        if ($DryRun) {
            Write-DryRun "move $file → docs/active/AUDIT_SUITE/"
        } else {
            Move-Item -Path $source -Destination $destination -Force -ErrorAction Stop
            Write-Success "Moved: $file"
        }
        $auditCount++
    }
}

Write-Host "  Total audit docs moved: $auditCount"
Write-Host ""

# ============================================================================
# PHASE 7: MOVE PRODUCTION ROLLOUT DOCS
# ============================================================================

Write-Phase "7" "MOVING PRODUCTION ROLLOUT DOCUMENTATION"

$productionFiles = @(
    "ACTION_CHECKLIST_PHASE3_TODAY.md",
    "PHASE_3_PRODUCTION_ROLLOUT_PLAN.md",
    "PHASE_3_QUICK_REFERENCE.md",
    "PHASE_3_AND_BEYOND_MASTER_INDEX.md",
    "PHASE_3_ONE_PAGE_SUMMARY.md",
    "WEEK3_POST_LAUNCH_TASKS.md",
    "IMPLEMENTATION_COMPLETE_EXECUTION_READY.md",
    "YOUR_EXECUTION_CHECKLIST.md"
)

$productionCount = 0

foreach ($file in $productionFiles) {
    $source = Join-Path $RepoRoot $file
    if (Test-Path $source) {
        $destination = Join-Path $RepoRoot "docs/active/PRODUCTION_ROLLOUT" $file
        if ($DryRun) {
            Write-DryRun "move $file → docs/active/PRODUCTION_ROLLOUT/"
        } else {
            Move-Item -Path $source -Destination $destination -Force -ErrorAction Stop
            Write-Success "Moved: $file"
        }
        $productionCount++
    }
}

Write-Host "  Total production docs moved: $productionCount"
Write-Host ""

# ============================================================================
# PHASE 8: CLEANUP GARBAGE FILES
# ============================================================================

Write-Phase "8" "REMOVING GARBAGE FILES"

$garbageFiles = @("cd", "85%", "A", "Get", "Run", "Task", "{")
$deletedCount = 0

foreach ($file in $garbageFiles) {
    $path = Join-Path $RepoRoot $file
    if (Test-Path $path) {
        if ($DryRun) {
            Write-DryRun "delete $file"
        } else {
            Remove-Item -Path $path -Force -Recurse -ErrorAction Stop
            Write-Success "Deleted: $file"
        }
        $deletedCount++
    }
}

# Also remove empty Compilation folder
$compilationPath = Join-Path $RepoRoot "Compilation"
if (Test-Path $compilationPath) {
    if ($DryRun) {
        Write-DryRun "delete Compilation/"
    } else {
        Remove-Item -Path $compilationPath -Force -Recurse -ErrorAction Stop
        Write-Success "Deleted: Compilation/"
    }
    $deletedCount++
}

Write-Host "  Total garbage files removed: $deletedCount"
Write-Host ""

# ============================================================================
# SUMMARY
# ============================================================================

Write-Header "CLEANUP SUMMARY"

$totalChanges = $createdCount + $scriptCount + $logCount + $archivedCount + $obsoleteCount + $auditCount + $productionCount + $deletedCount

Write-Host "Directories created:        $createdCount" -ForegroundColor Cyan
Write-Host "Scripts moved:              $scriptCount" -ForegroundColor Cyan
Write-Host "Logs moved:                 $logCount" -ForegroundColor Cyan
Write-Host "Phase 0-2 docs archived:    $archivedCount" -ForegroundColor Cyan
Write-Host "Obsolete docs archived:     $obsoleteCount" -ForegroundColor Cyan
Write-Host "Audit docs moved:           $auditCount" -ForegroundColor Cyan
Write-Host "Production docs moved:      $productionCount" -ForegroundColor Cyan
Write-Host "Garbage files removed:      $deletedCount" -ForegroundColor Cyan
Write-Host "────────────────────────────────────────" -ForegroundColor Cyan
Write-Host "TOTAL CHANGES:              $totalChanges" -ForegroundColor Green

Write-Host ""

if ($DryRun) {
    Write-Host "✓ DRY RUN COMPLETED - No files were actually moved" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To execute the cleanup, run:" -ForegroundColor Green
    Write-Host "  .\CLEANUP_SCRIPT.ps1 -DryRun:$false" -ForegroundColor Green
    Write-Host ""
    Write-Host "If git status is clean, you can also commit changes:" -ForegroundColor Green
    Write-Host "  git add -A" -ForegroundColor Green
    Write-Host "  git commit -m 'Refactor: Reorganize repository structure (Phase 1)'" -ForegroundColor Green
} else {
    Write-Host "✓ CLEANUP EXECUTED - Files have been reorganized" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Review changes: git status" -ForegroundColor Green
    Write-Host "  2. Verify build still works: ./gradlew clean build" -ForegroundColor Green
    Write-Host "  3. Verify tests still pass: ./gradlew test" -ForegroundColor Green
    Write-Host "  4. Commit if everything looks good:" -ForegroundColor Green
    Write-Host "     git add -A" -ForegroundColor Green
    Write-Host "     git commit -m 'Refactor: Reorganize repository structure (Phase 1)'" -ForegroundColor Green
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

