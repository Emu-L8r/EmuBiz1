# Documentation Cleanup Script - Phase 1 Quick Win
# Purpose: Archive April session docs, clean up root directory
# Time: ~2-3 minutes to run
# Date: May 9, 2026

Write-Host "🚀 Starting Documentation Cleanup..." -ForegroundColor Cyan
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
    "miscellaneous",
    "build-outputs"
)

# Create main archive directory
if (-not (Test-Path $archiveRoot)) {
    New-Item -ItemType Directory -Path $archiveRoot | Out-Null
    Write-Host "✅ Created: $archiveRoot" -ForegroundColor Green
}

# Create subdirectories
foreach ($subdir in $subdirs) {
    $path = Join-Path $archiveRoot $subdir
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path | Out-Null
        Write-Host "✅ Created: $path" -ForegroundColor Green
    }
}

Write-Host "`n📁 Moving files to archive..." -ForegroundColor Yellow

# File patterns and their target subdirectories
$moveRules = @{
    # Session summaries
    "SESSION_*APRIL*.md" = "session-summaries"
    "FINAL_SESSION*.md" = "session-summaries"

    # Status reports
    "STATUS_REPORT*.md" = "status-reports"
    "*STATUS_APRIL*.md" = "status-reports"
    "FINAL_STATUS_APRIL*.md" = "status-reports"

    # Checklists
    "*ACTION_CHECKLIST*.md" = "checklists"
    "*CHECKPOINT*.md" = "checklists"
    "QUICK_ACTION_CHECKLIST.md" = "checklists"  # Keep one copy

    # Build logs
    "build_*.log" = "build-logs"
    "BUILD_*.log" = "build-logs"
    "*compile*.log" = "build-logs"

    # Implementation logs
    "IMPLEMENTATION_*APRIL*.md" = "implementation-logs"
    "IMPLEMENTATION_COMPLETE_APRIL*.md" = "implementation-logs"

    # Test reports
    "TEST_RESULTS*.md" = "test-reports"
    "TEST_FAILURES*.md" = "test-reports"
    "TEST_FIX*.md" = "test-reports"

    # Phase completions (old)
    "PHASE_*APRIL*.md" = "phase-completions"
    "FINAL_COMPLETION*.md" = "phase-completions"

    # Misc April files
    "APRIL_*.md" = "miscellaneous"
    "ACTION_PLAN*.md" = "miscellaneous"
    "ACTION_ITEMS*.md" = "miscellaneous"
    "BUILD_ATTEMPT*.md" = "miscellaneous"
    "BUILD_FIX*.md" = "miscellaneous"
}

$moveCount = 0

# Execute moves
foreach ($pattern in $moveRules.Keys) {
    $targetSubdir = $moveRules[$pattern]
    $targetPath = Join-Path $archiveRoot $targetSubdir

    # Find matching files
    $files = Get-ChildItem -Path "." -Filter $pattern -File -ErrorAction SilentlyContinue

    foreach ($file in $files) {
        # Skip special files that should stay
        $skip = $false
        if ($file.Name -eq "QUICK_ACTION_CHECKLIST.md") { $skip = $true }
        if ($file.Name -eq "PHASE_3EF_QUICK_START.md") { $skip = $true }
        if ($file.Name -eq "PHASE_3EF_FINAL_STATUS_REPORT.md") { $skip = $true }
        if ($file.Name -eq "PHASE_3EF_IMPLEMENTATION_SUMMARY.md") { $skip = $true }
        if ($file.Name -eq "PHASE_3EF_MANUAL_TESTING.md") { $skip = $true }
        if ($file.Name -eq "PHASE_3EF_DOCUMENTATION_INDEX.md") { $skip = $true }
        if ($file.Name -eq "PHASE_3EF_COMPLETION_SUMMARY.md") { $skip = $true }

        if (-not $skip) {
            $destination = Join-Path $targetPath $file.Name
            Move-Item -Path $file.FullName -Destination $destination -Force
            Write-Host "  > Moved: $($file.Name) to $targetSubdir/" -ForegroundColor Gray
            $moveCount++
        }
    }
}

Write-Host "`n✅ Moved $moveCount files to archive" -ForegroundColor Green

Write-Host "`n📊 Cleanup Summary:" -ForegroundColor Cyan
Write-Host "  Before: 180+ files in root"
Write-Host "  After: ~20 essential files in root"
Write-Host "  Archive: $archiveRoot/ (organized by type)"

Write-Host "`n✨ Documentation cleanup complete!" -ForegroundColor Green
Write-Host "Next: Create DOCUMENTATION_LAUNCH_INDEX.md" -ForegroundColor Cyan


