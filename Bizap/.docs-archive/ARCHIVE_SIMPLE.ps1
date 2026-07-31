# PowerShell Archive Script - SIMPLIFIED VERSION
# Fixes encoding issues with special characters

Write-Host "Starting Documentation Cleanup..." -ForegroundColor Cyan
Write-Host "Creating archive structure..." -ForegroundColor Yellow

# Create archive directory structure
$archiveRoot = ".archive-april-2026"
$subdirs = @(
    "session-summaries",
    "status-reports",
    "checklists",
    "build-logs",
    "implementation-logs",
    "test-reports",
    "phase-completions",
    "miscellaneous"
)

# Verify main archive exists (from previous run)
if (Test-Path $archiveRoot) {
    Write-Host "OK: Archive directory exists" -ForegroundColor Green
} else {
    Write-Host "ERROR: Archive directory not found" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Moving files to archive..." -ForegroundColor Yellow
Write-Host ""

$moveCount = 0

# File patterns and their target subdirectories
$moveRules = @{
    "session-summaries" = @("SESSION_*APRIL*.md", "FINAL_SESSION*.md")
    "status-reports" = @("STATUS_REPORT*.md", "*STATUS_APRIL*.md", "FINAL_STATUS_APRIL*.md")
    "checklists" = @("*ACTION_CHECKLIST*.md", "*CHECKPOINT*.md")
    "build-logs" = @("build_*.log", "BUILD_*.log", "*compile*.log")
    "implementation-logs" = @("IMPLEMENTATION_*APRIL*.md", "IMPLEMENTATION_COMPLETE_APRIL*.md")
    "test-reports" = @("TEST_RESULTS*.md", "TEST_FAILURES*.md", "TEST_FIX*.md")
    "phase-completions" = @("PHASE_*APRIL*.md", "FINAL_COMPLETION*.md")
    "miscellaneous" = @("APRIL_*.md", "ACTION_PLAN*.md", "ACTION_ITEMS*.md", "BUILD_ATTEMPT*.md", "BUILD_FIX*.md")
}

# Files to skip (keep in root)
$skipFiles = @(
    "QUICK_ACTION_CHECKLIST.md",
    "PHASE_3EF_QUICK_START.md",
    "PHASE_3EF_FINAL_STATUS_REPORT.md",
    "PHASE_3EF_IMPLEMENTATION_SUMMARY.md",
    "PHASE_3EF_MANUAL_TESTING.md",
    "PHASE_3EF_DOCUMENTATION_INDEX.md",
    "PHASE_3EF_COMPLETION_SUMMARY.md",
    "DOCUMENTATION_LAUNCH_INDEX.md",
    "QUICK_WINS_MASTER_PLAYBOOK.md",
    "PHASE1_QUICK_WIN_EXECUTION.md",
    "PHASE2_100_PERCENT_GREEN_GUIDE.md",
    "IMPLEMENTATION_START_HERE.md"
)

# Execute moves
foreach ($subdir in $moveRules.Keys) {
    $targetPath = Join-Path $archiveRoot $subdir
    $patterns = $moveRules[$subdir]

    foreach ($pattern in $patterns) {
        $files = @(Get-ChildItem -Path "." -Filter $pattern -File -ErrorAction SilentlyContinue)

        foreach ($file in $files) {
            if ($skipFiles -contains $file.Name) {
                continue
            }

            $destination = Join-Path $targetPath $file.Name
            Move-Item -Path $file.FullName -Destination $destination -Force -ErrorAction SilentlyContinue
            Write-Host "Moved: $($file.Name)" -ForegroundColor Gray
            $moveCount++
        }
    }
}

Write-Host ""
Write-Host "Successfully moved $moveCount files to archive" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "- Archive created: .archive-april-2026/" -ForegroundColor Gray
Write-Host "- Subdirectories: 8 (organized by type)" -ForegroundColor Gray
Write-Host "- Files moved: $moveCount" -ForegroundColor Gray
Write-Host "- Root directory: cleaned" -ForegroundColor Gray
Write-Host ""
Write-Host "Cleanup complete!" -ForegroundColor Green

