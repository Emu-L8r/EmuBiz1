#!/usr/bin/env pwsh
# POST-MERGE VERIFICATION SCRIPT - Complete Automation
# Purpose: Verify build integrity after recent PR merge
# Usage: ./verify-post-merge.ps1

$ErrorActionPreference = "Stop"

# Colors
$GREEN = "`e[32m"
$RED = "`e[31m"
$YELLOW = "`e[33m"
$BLUE = "`e[34m"
$NC = "`e[0m"

# State tracking
$PASS_COUNT = 0
$FAIL_COUNT = 0
$ABORT = $false

function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host "$BLUE[$Text]$NC"
    Write-Host "━" * 60
}

function Check-Pass {
    param([string]$Message)
    Write-Host "$GREEN✅ PASS$NC: $Message"
    $PASS_COUNT++
}

function Check-Fail {
    param([string]$Message)
    Write-Host "$RED❌ FAIL$NC: $Message"
    $FAIL_COUNT++
    $ABORT = $true
}

function Check-Warn {
    param([string]$Message)
    Write-Host "$YELLOW⚠️  WARN$NC: $Message"
}

# ============================================
# SECTION 1: PRE-EXECUTION CHECKS
# ============================================

Write-Header "1. PRE-EXECUTION VERIFICATION"

$ProjectPath = "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
$CurrentPath = Get-Location
$IsCorrectPath = $CurrentPath -eq $ProjectPath -or $CurrentPath -eq (Convert-Path $ProjectPath)

if ($IsCorrectPath) {
    Check-Pass "Correct working directory"
} else {
    Check-Warn "Working directory mismatch - continuing anyway"
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
$StatusOutput = git status --porcelain 2>$null
if ($StatusOutput) {
    Check-Warn "Uncommitted changes detected ($(($StatusOutput | Measure-Object -Line).Lines) files)"
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

Write-Host "Current Branch: $BLUE$CurrentBranch$NC"
Write-Host "Current Commit: $BLUE$CurrentCommit$NC"
Write-Host "Latest Tag: $BLUE$LatestTag$NC"

if ($CurrentBranch -eq "main") {
    Check-Pass "On main branch (ready for merge)"
} elseif ($CurrentBranch -like "*recovery*" -or $CurrentBranch -like "*feature*") {
    Check-Warn "On feature branch: $CurrentBranch (not main)"
} else {
    Check-Warn "Unexpected branch: $CurrentBranch"
}

# ============================================
# SECTION 3: BUILD VERIFICATION (CRITICAL)
# ============================================

Write-Header "3. GRADLE CLEAN BUILD VERIFICATION"

Write-Host "Running: ./gradlew clean build -x test (this may take 3-5 minutes)..."
Write-Host ""

$BuildStartTime = Get-Date

try {
    $BuildOutput = & .\gradlew clean build -x test 2>&1
    $BuildExitCode = $LASTEXITCODE
    $BuildEndTime = Get-Date
    $BuildDuration = $BuildEndTime - $BuildStartTime

    if ($BuildExitCode -eq 0) {
        $BuildTimeSeconds = $BuildDuration.TotalSeconds
        Check-Pass "Build succeeded ($('{0:F1}' -f $BuildTimeSeconds) seconds)"

        # Check for common issues in output
        if ($BuildOutput -match "BUILD SUCCESSFUL") {
            Check-Pass "Build marked as SUCCESSFUL"
        }

        if ($BuildOutput -match "FAILURE|error:") {
            Check-Fail "Build output contains errors despite success code"
        }
    } else {
        Check-Fail "Build failed with exit code: $BuildExitCode"
        Write-Host ""
        Write-Host "$RED=== BUILD ERROR OUTPUT ===$NC"
        $BuildOutput | Write-Host
        Write-Host "$RED=== END ERROR OUTPUT ===$NC"
    }
} catch {
    Check-Fail "Build execution error: $_"
    Write-Host "Exception: $($_.Exception.Message)"
}

# ============================================
# SECTION 4: UNIT TESTS VERIFICATION
# ============================================

if (-not $ABORT) {
    Write-Header "4. UNIT TEST VERIFICATION"

    Write-Host "Running: ./gradlew test..."
    Write-Host ""

    try {
        $TestOutput = & .\gradlew test 2>&1
        $TestExitCode = $LASTEXITCODE

        if ($TestExitCode -eq 0) {
            Check-Pass "Test suite executed successfully"

            # Count passing tests
            if ($TestOutput -match "(\d+) passed") {
                $PassCount = [regex]::Matches($TestOutput, "(\d+) passed")[0].Groups[1].Value
                Check-Pass "Tests passed: $PassCount"
            } else {
                Check-Warn "Could not parse test count"
            }

            if ($TestOutput -match "0 failed") {
                Check-Pass "No test failures"
            }
        } else {
            Check-Fail "Test execution failed with exit code: $TestExitCode"
            Write-Host ""
            Write-Host "$RED=== TEST ERROR OUTPUT ===$NC"
            $TestOutput | Write-Host
            Write-Host "$RED=== END TEST ERROR OUTPUT ===$NC"
        }
    } catch {
        Check-Fail "Test execution error: $_"
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
                $DebugSize = "{0:F1}" -f ((Get-Item $DebugApk).Length / 1MB)
                Check-Pass "Debug APK built successfully ($DebugSize MB)"
            } else {
                Check-Fail "Debug APK file not found at $DebugApk"
            }
        } else {
            Check-Fail "Debug APK build failed"
        }
    } catch {
        Check-Fail "Debug APK build error: $_"
    }

    # Release APK
    Write-Host "Building Release APK..."
    try {
        $ReleaseOutput = & .\gradlew assembleRelease 2>&1
        $ReleaseExitCode = $LASTEXITCODE

        if ($ReleaseExitCode -eq 0) {
            $ReleaseApk = "app\build\outputs\apk\release\app-release.apk"
            if (Test-Path $ReleaseApk) {
                $ReleaseSize = "{0:F1}" -f ((Get-Item $ReleaseApk).Length / 1MB)
                Check-Pass "Release APK built successfully ($ReleaseSize MB)"
            } else {
                Check-Fail "Release APK file not found at $ReleaseApk"
            }
        } else {
            Check-Fail "Release APK build failed"
        }
    } catch {
        Check-Fail "Release APK build error: $_"
    }
}

# ============================================
# SECTION 6: MODULE VERIFICATION
# ============================================

Write-Header "6. MODULE STRUCTURE VERIFICATION"

$Modules = @("app", "data", "domain", "ui")
$ModulesFound = 0

foreach ($module in $Modules) {
    $ModulePath = $module
    if (Test-Path $ModulePath -PathType Container) {
        Check-Pass "Module :$module present"
        $ModulesFound++
    } else {
        Check-Fail "Module :$module missing"
    }
}

# Check settings.gradle.kts
if (Test-Path "settings.gradle.kts") {
    $SettingsContent = Get-Content "settings.gradle.kts" -Raw

    Write-Host ""
    Write-Host "Module includes in settings.gradle.kts:"

    $IncludeLines = Get-Content "settings.gradle.kts" | Select-String "include\(" -OutVariable IncludeLinesVar
    foreach ($line in $IncludeLines) {
        Write-Host "  $line"
    }
} else {
    Check-Fail "settings.gradle.kts not found"
}

# ============================================
# SECTION 7: BUILD CONFIGURATION
# ============================================

Write-Header "7. BUILD CONFIGURATION VERIFICATION"

# Check gradle wrapper
if (Test-Path "gradlew" -PathType Leaf) {
    Check-Pass "Gradle wrapper present"
} else {
    Check-Fail "Gradle wrapper not found"
}

# Check gradle properties
if (Test-Path "gradle.properties") {
    Check-Pass "gradle.properties exists"
} else {
    Check-Warn "gradle.properties not found"
}

# ============================================
# SECTION 8: FINAL SUMMARY
# ============================================

Write-Header "8. VERIFICATION SUMMARY"

Write-Host ""
Write-Host "$GREEN✅ Passed:$NC  $PASS_COUNT"
Write-Host "$RED❌ Failed:$NC  $FAIL_COUNT"
Write-Host ""

if ($FAIL_COUNT -eq 0) {
    Write-Host "$GREEN════════════════════════════════════════════════════$NC"
    Write-Host "$GREEN🎉 POST-MERGE VERIFICATION SUCCESSFUL!$NC"
    Write-Host "$GREEN════════════════════════════════════════════════════$NC"
    Write-Host ""
    Write-Host "✅ Build: PASSING"
    Write-Host "✅ Tests: PASSING"
    Write-Host "✅ Debug APK: BUILT"
    Write-Host "✅ Release APK: BUILT"
    Write-Host "✅ Modules: INTEGRATED"
    Write-Host ""
    Write-Host "🚀 CLEARED FOR OPTION C IMPLEMENTATION"
    Write-Host ""
    Write-Host "📋 Next Steps:"
    Write-Host "  1. Review: POST_MERGE_VERIFICATION_AND_OPTION_C_MASTER_PROMPT.md"
    Write-Host "  2. Choose: Your pace (aggressive/steady/flexible)"
    Write-Host "  3. Start: Monday with Issue #10 (Security Fix)"
    Write-Host ""
    Write-Host "📊 Timeline:"
    Write-Host "  Phase 1 (Week 1): 5 issues, 4.5 hours"
    Write-Host "  Phase 2 (Week 2): 2 issues, 7 hours"
    Write-Host "  Phase 3 (Week 3): 3 issues, 7 hours"
    Write-Host "  Total: 10 issues, 21 hours"
    Write-Host ""
    exit 0
} else {
    Write-Host "$RED════════════════════════════════════════════════════$NC"
    Write-Host "$RED⚠️  POST-MERGE VERIFICATION FAILED$NC"
    Write-Host "$RED════════════════════════════════════════════════════$NC"
    Write-Host ""
    Write-Host "❌ $FAIL_COUNT issue(s) detected"
    Write-Host ""
    Write-Host "📋 Troubleshooting:"
    Write-Host "  1. Review the errors above"
    Write-Host "  2. Check build output for detailed errors"
    Write-Host "  3. Common issues:"
    Write-Host "     - Room annotations in domain module"
    Write-Host "     - Navigation function references"
    Write-Host "     - Missing imports in moved files"
    Write-Host ""
    Write-Host "🔄 Rollback Option:"
    Write-Host "  git checkout v1.0.3-stable-build-20260320"
    Write-Host "  ./gradlew clean build -x test"
    Write-Host ""
    exit 1
}

