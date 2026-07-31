#!/usr/bin/env pwsh
# POST-MERGE VERIFICATION SCRIPT - Simplified & Fixed
# Purpose: Verify build integrity after recent PR merge
# Usage: .\verify-post-merge-fixed.ps1

$ErrorActionPreference = "Stop"

# State tracking
$PASS_COUNT = 0
$FAIL_COUNT = 0
$ABORT = $false

function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host "[$Text]"
    Write-Host "━" * 60
}

function Check-Pass {
    param([string]$Message)
    Write-Host "[PASS] $Message"
    $script:PASS_COUNT++
}

function Check-Fail {
    param([string]$Message)
    Write-Host "[FAIL] $Message"
    $script:FAIL_COUNT++
    $script:ABORT = $true
}

function Check-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message"
}

# ============================================
# SECTION 1: PRE-EXECUTION CHECKS
# ============================================

Write-Header "1. PRE-EXECUTION VERIFICATION"

$ProjectPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$CurrentPath = Get-Location

if ($CurrentPath -like "*Bizap*") {
    Check-Pass "Correct working directory: $CurrentPath"
} else {
    Check-Warn "Working directory: $CurrentPath"
}

# Check git repo
try {
    $GitRemote = git remote get-url origin 2>$null
    if ($GitRemote -like "*EmuBiz1*" -or $GitRemote -like "*Bizap*") {
        Check-Pass "GitHub remote configured correctly"
    } else {
        Check-Fail "Unexpected GitHub remote: $GitRemote"
    }
} catch {
    Check-Fail "Not a git repository"
}

# Check for uncommitted changes (warn only)
$StatusOutput = @(git status --porcelain 2>$null)
if ($StatusOutput.Count -gt 0) {
    Check-Warn "Uncommitted changes detected ($($StatusOutput.Count) files)"
} else {
    Check-Pass "Working tree clean"
}

# ============================================
# SECTION 2: GIT STATE VERIFICATION
# ============================================

Write-Header "2. GIT STATE VERIFICATION"

$CurrentBranch = git branch --show-current 2>$null
$CurrentCommit = git rev-parse --short HEAD 2>$null
$LatestTag = git describe --tags 2>$null

Write-Host "Current Branch: $CurrentBranch"
Write-Host "Current Commit: $CurrentCommit"
Write-Host "Latest Tag: $LatestTag"

if ($CurrentBranch -eq "main") {
    Check-Pass "On main branch (ready for merge)"
} else {
    Check-Warn "On branch: $CurrentBranch"
}

# ============================================
# SECTION 3: BUILD VERIFICATION (CRITICAL)
# ============================================

Write-Header "3. GRADLE CLEAN BUILD VERIFICATION"

Write-Host "Running: ./gradlew clean build -x test"
Write-Host "This may take 3-5 minutes..."
Write-Host ""

$BuildStartTime = Get-Date

try {
    $BuildOutput = & .\gradlew clean build -x test 2>&1
    $BuildExitCode = $LASTEXITCODE
    $BuildEndTime = Get-Date
    $BuildDuration = $BuildEndTime - $BuildStartTime

    if ($BuildExitCode -eq 0) {
        $BuildTimeSeconds = $BuildDuration.TotalSeconds
        Check-Pass "Build succeeded ($([Math]::Round($BuildTimeSeconds, 1)) seconds)"

        # Check for success marker
        if ($BuildOutput -match "BUILD SUCCESSFUL") {
            Check-Pass "Build marked as SUCCESSFUL"
        }

        # Check for errors
        if ($BuildOutput -match "FAILURE|error:") {
            Check-Fail "Build output contains errors despite success code"
        }
    } else {
        Check-Fail "Build failed with exit code: $BuildExitCode"
        Write-Host ""
        Write-Host "=== BUILD ERROR OUTPUT ==="
        $BuildOutput | Write-Host
        Write-Host "=== END ERROR OUTPUT ==="
    }
} catch {
    Check-Fail "Build execution error: $($_.Exception.Message)"
}

# ============================================
# SECTION 4: UNIT TESTS VERIFICATION
# ============================================

if (-not $ABORT) {
    Write-Header "4. UNIT TEST VERIFICATION"

    Write-Host "Running: ./gradlew test"
    Write-Host ""

    try {
        $TestOutput = & .\gradlew test 2>&1
        $TestExitCode = $LASTEXITCODE

        if ($TestExitCode -eq 0) {
            Check-Pass "Test suite executed successfully"

            # Count passing tests
            $TestOutputStr = $TestOutput -join "`n"
            if ($TestOutputStr -match "(\d+) passed") {
                $PassCount = $matches[1]
                Check-Pass "Tests passed: $PassCount"
            } else {
                Check-Warn "Could not parse test count"
            }

            if ($TestOutputStr -match "0 failed") {
                Check-Pass "No test failures"
            }
        } else {
            Check-Fail "Test execution failed with exit code: $TestExitCode"
        }
    } catch {
        Check-Fail "Test execution error: $($_.Exception.Message)"
    }
}

# ============================================
# SECTION 5: APK BUILD VERIFICATION
# ============================================

if (-not $ABORT) {
    Write-Header "5. APK BUILD VERIFICATION"

    # Debug APK
    Write-Host "Building Debug APK..."
    try {
        $DebugOutput = & .\gradlew assembleDebug 2>&1
        $DebugExitCode = $LASTEXITCODE

        if ($DebugExitCode -eq 0) {
            $DebugApk = "app\build\outputs\apk\debug\app-debug.apk"
            if (Test-Path $DebugApk) {
                $DebugSize = [Math]::Round((Get-Item $DebugApk).Length / 1MB, 1)
                Check-Pass "Debug APK built successfully ($DebugSize MB)"
            } else {
                Check-Fail "Debug APK file not found"
            }
        } else {
            Check-Fail "Debug APK build failed"
        }
    } catch {
        Check-Fail "Debug APK build error: $($_.Exception.Message)"
    }

    # Release APK
    Write-Host "Building Release APK..."
    try {
        $ReleaseOutput = & .\gradlew assembleRelease 2>&1
        $ReleaseExitCode = $LASTEXITCODE

        if ($ReleaseExitCode -eq 0) {
            $ReleaseApk = "app\build\outputs\apk\release\app-release.apk"
            if (Test-Path $ReleaseApk) {
                $ReleaseSize = [Math]::Round((Get-Item $ReleaseApk).Length / 1MB, 1)
                Check-Pass "Release APK built successfully ($ReleaseSize MB)"
            } else {
                Check-Fail "Release APK file not found"
            }
        } else {
            Check-Fail "Release APK build failed"
        }
    } catch {
        Check-Fail "Release APK build error: $($_.Exception.Message)"
    }
}

# ============================================
# SECTION 6: MODULE VERIFICATION
# ============================================

Write-Header "6. MODULE STRUCTURE VERIFICATION"

$Modules = @("app", "data", "domain")
$ModulesFound = 0

foreach ($module in $Modules) {
    if (Test-Path $module -PathType Container) {
        Check-Pass "Module :$module present"
        $ModulesFound++
    } else {
        Check-Fail "Module :$module missing"
    }
}

# ============================================
# SECTION 7: FINAL SUMMARY
# ============================================

Write-Header "7. VERIFICATION SUMMARY"

Write-Host ""
Write-Host "Passed:  $PASS_COUNT"
Write-Host "Failed:  $FAIL_COUNT"
Write-Host ""

if ($FAIL_COUNT -eq 0) {
    Write-Host "════════════════════════════════════════════════════"
    Write-Host "POST-MERGE VERIFICATION SUCCESSFUL!"
    Write-Host "════════════════════════════════════════════════════"
    Write-Host ""
    Write-Host "Status: ALL CHECKS PASSED"
    Write-Host ""
    Write-Host "Build:        PASSING"
    Write-Host "Tests:        PASSING"
    Write-Host "Debug APK:    BUILT"
    Write-Host "Release APK:  BUILT"
    Write-Host "Modules:      INTEGRATED"
    Write-Host ""
    Write-Host "CLEARED FOR OPTION C IMPLEMENTATION"
    Write-Host ""
    Write-Host "Next Steps:"
    Write-Host "  1. Review: PROJECT_IMPROVEMENT_INITIATIVE_SUMMARY.md"
    Write-Host "  2. Choose: Your pace (aggressive/steady/flexible)"
    Write-Host "  3. Start: Monday with Issue #10 (Security Fix)"
    Write-Host ""
    exit 0
} else {
    Write-Host "════════════════════════════════════════════════════"
    Write-Host "POST-MERGE VERIFICATION FAILED"
    Write-Host "════════════════════════════════════════════════════"
    Write-Host ""
    Write-Host "Failed Checks: $FAIL_COUNT"
    Write-Host ""
    Write-Host "Troubleshooting:"
    Write-Host "  1. Review the errors above"
    Write-Host "  2. Check build logs"
    Write-Host ""
    Write-Host "Rollback Option:"
    Write-Host "  git checkout v1.0.3-stable-build-20260320"
    Write-Host "  ./gradlew clean build -x test"
    Write-Host ""
    exit 1
}

