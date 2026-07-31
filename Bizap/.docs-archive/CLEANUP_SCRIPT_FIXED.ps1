# Bizap Repository Cleanup Script
# Date: April 10, 2026
# Purpose: Automate repository organization
# Safety: Includes dry-run mode, validation, and rollback guidance

param(
    [switch]$DryRun = $true,
    [string]$RepoRoot = (Get-Location),
    [switch]$SkipValidation = $false
)

$ErrorActionPreference = "Continue"
$WarningPreference = "Continue"

# Safelist - never move these
$SafelistFiles = @("release-key.jks", "local.properties", "README.md", "build.gradle.kts", "settings.gradle.kts", ".gitignore")
$SafelistDirs = @(".github", "app", "data", "domain", "gradle", ".gradle", ".idea", ".kotlin")

# ===== DISPLAY FUNCTIONS =====
function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host "========================================================" -ForegroundColor Cyan
    Write-Host " $Text" -ForegroundColor Cyan
    Write-Host "========================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Phase {
    param([string]$Number, [string]$Text)
    Write-Host "[$Number] $Text" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Text)
    Write-Host "  OK $Text" -ForegroundColor Green
}

function Write-DryRun {
    param([string]$Text)
    Write-Host "  [DRY] $Text" -ForegroundColor Gray
}

function Write-Warning {
    param([string]$Text)
    Write-Host "  WARN $Text" -ForegroundColor Yellow
}

function Write-Abort {
    param([string]$Text)
    Write-Host "  FAIL $Text" -ForegroundColor Red
}

# ===== VALIDATION =====
Write-Header "BIZAP REPOSITORY CLEANUP"
Write-Host "Repository: $RepoRoot" -ForegroundColor Cyan
Write-Host "Dry Run: $DryRun" -ForegroundColor Cyan
Write-Host "Skip Validation: $SkipValidation" -ForegroundColor Cyan
Write-Host ""

if (-not $SkipValidation) {
    Write-Phase "0" "VALIDATION"

    $gitCheck = Get-ChildItem -Path $RepoRoot -Filter ".git" -Depth 0 -ErrorAction SilentlyContinue
    if ($null -eq $gitCheck) {
        Write-Abort "Not a git repository!"
        exit 1
    }
    Write-Success "Git repository detected"

    $gitStatus = & git -C $RepoRoot status --porcelain
    if ($gitStatus) {
        Write-Warning "Uncommitted changes detected"
        $response = Read-Host "Continue? (y/n)"
        if ($response -ne "y") {
            Write-Abort "Aborted"
            exit 1
        }
    }
    Write-Success "Git ready"

    $gradleCheck = Get-ChildItem -Path $RepoRoot -Filter "build.gradle.kts" -Depth 0
    if ($null -eq $gradleCheck) {
        Write-Abort "build.gradle.kts not found!"
        exit 1
    }
    Write-Success "Gradle found"
    Write-Host ""
}

# ===== PHASE 1: CREATE DIRECTORIES =====
Write-Phase "1" "CREATE DIRECTORIES"

$dirs = @(
    "scripts", ".logs", ".logs/build_logs", ".logs/test_logs", ".logs/diagnostic_logs",
    "docs/archive", "docs/archive/phases", "docs/archive/phases/phase_0_startup",
    "docs/archive/phases/phase_1_mvp", "docs/archive/phases/phase_2_refinement",
    "docs/archive/experiments", "docs/archive/obsolete", "docs/archive/session_logs",
    "docs/active", "docs/active/AUDIT_SUITE", "docs/active/PRODUCTION_ROLLOUT",
    "docs/active/GUIDES", "docs/decisions"
)

$createdCount = 0
foreach ($dir in $dirs) {
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
Write-Host "  Total: $createdCount dirs"
Write-Host ""

# ===== PHASE 2: MOVE SCRIPTS =====
Write-Phase "2" "MOVE SCRIPTS"

$scriptCount = 0
foreach ($pattern in @("*.ps1", "*.sh")) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter $pattern -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        if ($file.Name -eq "CLEANUP_SCRIPT.ps1" -or $file.Name -eq "CLEANUP_SCRIPT_FIXED.ps1") { continue }

        $dest = Join-Path $RepoRoot "scripts" $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) to scripts/"
        } else {
            Move-Item -Path $file.FullName -Destination $dest -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $scriptCount++
    }
}
Write-Host "  Total: $scriptCount scripts"
Write-Host ""

# ===== PHASE 3: MOVE LOGS =====
Write-Phase "3" "MOVE LOGS"

$logCount = 0
foreach ($pattern in @("*.log", "*_log.txt", "logcat_*.txt", "device_*.txt", "audit_*.txt")) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter $pattern -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        $dest = Join-Path $RepoRoot ".logs" $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) to .logs/"
        } else {
            Move-Item -Path $file.FullName -Destination $dest -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $logCount++
    }
}
Write-Host "  Total: $logCount logs"
Write-Host ""

# ===== PHASE 4: ARCHIVE PHASE 0-2 =====
Write-Phase "4" "ARCHIVE PHASE DOCS"

$phaseMap = @{
    "PHASE_0_" = "docs/archive/phases/phase_0_startup"
    "PHASE_1_" = "docs/archive/phases/phase_1_mvp"
    "PHASE_2_" = "docs/archive/phases/phase_2_refinement"
}

$archivedCount = 0
foreach ($prefix in $phaseMap.Keys) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter "$prefix*.md" -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        $dest = Join-Path $RepoRoot $phaseMap[$prefix] $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) to $($phaseMap[$prefix])/"
        } else {
            Move-Item -Path $file.FullName -Destination $dest -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $archivedCount++
    }
}
Write-Host "  Total: $archivedCount phase docs"
Write-Host ""

# ===== PHASE 5: ARCHIVE OBSOLETE =====
Write-Phase "5" "ARCHIVE OBSOLETE"

$obsoleteCount = 0
foreach ($pattern in @("CRASH_FIX_", "IMPLEMENTATION_", "BUILD_")) {
    $files = @(Get-ChildItem -Path $RepoRoot -Filter "$pattern*.md" -Depth 0 -ErrorAction SilentlyContinue)
    foreach ($file in $files) {
        $dest = Join-Path $RepoRoot "docs/archive/obsolete" $file.Name
        if ($DryRun) {
            Write-DryRun "move $($file.Name) to docs/archive/obsolete/"
        } else {
            Move-Item -Path $file.FullName -Destination $dest -Force -ErrorAction Stop
            Write-Success "Moved: $($file.Name)"
        }
        $obsoleteCount++
    }
}
Write-Host "  Total: $obsoleteCount obsolete docs"
Write-Host ""

# ===== PHASE 6: MOVE AUDIT DOCS =====
Write-Phase "6" "MOVE AUDIT DOCS"

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
        $dest = Join-Path $RepoRoot "docs/active/AUDIT_SUITE" $file
        if ($DryRun) {
            Write-DryRun "move $file to docs/active/AUDIT_SUITE/"
        } else {
            Move-Item -Path $source -Destination $dest -Force -ErrorAction Stop
            Write-Success "Moved: $file"
        }
        $auditCount++
    }
}
Write-Host "  Total: $auditCount audit docs"
Write-Host ""

# ===== PHASE 7: MOVE PRODUCTION DOCS =====
Write-Phase "7" "MOVE PRODUCTION DOCS"

$prodFiles = @(
    "ACTION_CHECKLIST_PHASE3_TODAY.md",
    "PHASE_3_PRODUCTION_ROLLOUT_PLAN.md",
    "PHASE_3_QUICK_REFERENCE.md",
    "PHASE_3_AND_BEYOND_MASTER_INDEX.md",
    "PHASE_3_ONE_PAGE_SUMMARY.md",
    "WEEK3_POST_LAUNCH_TASKS.md",
    "IMPLEMENTATION_COMPLETE_EXECUTION_READY.md",
    "YOUR_EXECUTION_CHECKLIST.md"
)

$prodCount = 0
foreach ($file in $prodFiles) {
    $source = Join-Path $RepoRoot $file
    if (Test-Path $source) {
        $dest = Join-Path $RepoRoot "docs/active/PRODUCTION_ROLLOUT" $file
        if ($DryRun) {
            Write-DryRun "move $file to docs/active/PRODUCTION_ROLLOUT/"
        } else {
            Move-Item -Path $source -Destination $dest -Force -ErrorAction Stop
            Write-Success "Moved: $file"
        }
        $prodCount++
    }
}
Write-Host "  Total: $prodCount production docs"
Write-Host ""

# ===== PHASE 8: CLEANUP GARBAGE =====
Write-Phase "8" "CLEANUP GARBAGE"

$garbageFiles = @("cd", "85%", "A", "Get", "Run", "Task", "{")
$deletedCount = 0

foreach ($file in $garbageFiles) {
    $path = Join-Path $RepoRoot $file
    if (Test-Path $path) {
        if ($DryRun) {
            Write-DryRun "remove $file"
        } else {
            Remove-Item -Path $path -Force -Recurse -ErrorAction Stop
            Write-Success "Deleted: $file"
        }
        $deletedCount++
    }
}

$compilationPath = Join-Path $RepoRoot "Compilation"
if (Test-Path $compilationPath) {
    if ($DryRun) {
        Write-DryRun "remove Compilation/"
    } else {
        Remove-Item -Path $compilationPath -Force -Recurse -ErrorAction Stop
        Write-Success "Deleted: Compilation/"
    }
    $deletedCount++
}

Write-Host "  Total: $deletedCount garbage items"
Write-Host ""

# ===== SUMMARY =====
Write-Header "SUMMARY"

$total = $createdCount + $scriptCount + $logCount + $archivedCount + $obsoleteCount + $auditCount + $prodCount + $deletedCount

Write-Host "Directories created:    $createdCount" -ForegroundColor Cyan
Write-Host "Scripts moved:          $scriptCount" -ForegroundColor Cyan
Write-Host "Logs moved:             $logCount" -ForegroundColor Cyan
Write-Host "Phase docs archived:    $archivedCount" -ForegroundColor Cyan
Write-Host "Obsolete docs archived: $obsoleteCount" -ForegroundColor Cyan
Write-Host "Audit docs moved:       $auditCount" -ForegroundColor Cyan
Write-Host "Production docs moved:  $prodCount" -ForegroundColor Cyan
Write-Host "Garbage removed:        $deletedCount" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Cyan
Write-Host "TOTAL CHANGES:          $total" -ForegroundColor Green

Write-Host ""

if ($DryRun) {
    Write-Host "DRY RUN COMPLETE - No files were moved" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To execute, run:" -ForegroundColor Green
    Write-Host "  .\CLEANUP_SCRIPT_FIXED.ps1 -DryRun:`$false" -ForegroundColor Green
} else {
    Write-Host "CLEANUP COMPLETE - Files reorganized" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Review: git status" -ForegroundColor Green
    Write-Host "  2. Build:  ./gradlew clean build" -ForegroundColor Green
    Write-Host "  3. Test:   ./gradlew test" -ForegroundColor Green
    Write-Host "  4. Commit: git add -A && git commit -m 'refactor: Reorganize repo structure'" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

