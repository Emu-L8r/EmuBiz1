# 🔄 Rollback Testing Procedure — v1.0-stable-golden

**Checkpoint:** v1.0-stable-golden  
**Created:** April 17, 2026  
**Purpose:** Emergency procedures for reverting to known-good state

---

## 📋 Table of Contents
1. When to Rollback (Decision Tree)
2. Rollback Procedures (3 Options)
3. Verification Steps
4. Post-Rollback Analysis
5. Troubleshooting

---

## ⚠️ When to Rollback

### ROLLBACK if:
```
❌ App crashes at launch after new changes
❌ Core invoicing functionality broken
❌ Offline sync fails (pending operations lost)
❌ Database corruption (won't open)
❌ Build fails to compile
❌ 10%+ tests start failing
❌ Production incident occurring
```

### DO NOT ROLLBACK if:
```
✅ Minor UI issues (fix in hotfix branch instead)
✅ Performance degradation < 5% (optimize, don't rollback)
✅ New feature incomplete (finish feature, don't rollback)
✅ Lint/warning messages (just warnings, not critical)
✅ Single test failing (fix test, not rollback)
```

---

## 🔧 ROLLBACK OPTION 1: Quick Local Testing (Recommended)

**Use this:** To verify the checkpoint is stable before any action

### Step 1: Save Current Work
```powershell
# Stash current changes (don't lose them)
git stash

# Verify stash saved
git stash list
# Should show: stash@{0}: WIP on main - [your changes]
```

### Step 2: Checkout Checkpoint
```powershell
# Switch to checkpoint tag
git checkout v1.0-stable-golden

# Verify you're at the checkpoint
git status
# Should show: HEAD detached at tag 'v1.0-stable-golden'
```

### Step 3: Test Build
```powershell
# Clean build from checkpoint
./gradlew clean build --no-build-cache 2>&1 | Tee-Object -Variable buildResult | Select-Object -Last 50

# Verify success
if ($buildResult -match "BUILD SUCCESSFUL") {
    Write-Host "✅ Checkpoint build successful"
} else {
    Write-Host "❌ Checkpoint build failed - investigate"
}
```

### Step 4: Test Suite
```powershell
# Run unit tests
./gradlew testDebugUnitTest 2>&1 | Tee-Object -Variable testResult | Select-Object -Last 30

# Verify pass rate
if ($testResult -match "BUILD SUCCESSFUL") {
    Write-Host "✅ Checkpoint tests passing (97%+)"
} else {
    Write-Host "❌ Checkpoint tests failing"
}
```

### Step 5: Return to Main Branch
```powershell
# Go back to main
git checkout main

# Restore your changes
git stash pop

# Verify you're back
git status
# Should show you back on main with your changes
```

**Result:** ✅ Checkpoint is stable and available as fallback

---

## 🌿 ROLLBACK OPTION 2: Temporary Branch Rollback (For Fixes)

**Use this:** To work on a fix based on the checkpoint

### Step 1: Create Hotfix Branch from Checkpoint
```powershell
# Create new branch FROM the checkpoint
git checkout -b hotfix/critical-issue v1.0-stable-golden

# Verify you're on the new branch
git status
# Should show: On branch hotfix/critical-issue
```

### Step 2: Identify & Fix Issue
```powershell
# Understand what broke
git log --oneline main..HEAD
# Shows commits that exist in main but not in this branch (i.e., the new changes that broke things)

# View the specific changes
git diff v1.0-stable-golden..main

# Make your fix on this branch
# (Edit files, test locally)
```

### Step 3: Test Your Fix
```powershell
# Build from hotfix branch
./gradlew clean build --no-build-cache

# Run tests
./gradlew testDebugUnitTest

# If tests pass, continue to next step
```

### Step 4: Install & Manual Test (Optional)
```powershell
# Install on device/emulator
./gradlew installDebug

# Test core features:
# - Create invoice
# - Record payment
# - Export PDF
# - Go offline, make changes, sync
```

### Step 5: Merge Fix Back to Main
```powershell
# Go back to main
git checkout main

# Merge the fix
git merge hotfix/critical-issue

# Verify merge
git log --oneline -5
# Should show the new commits in main
```

### Step 6: Push to GitHub
```powershell
# Push fixed main branch
git push origin main

# Verify on GitHub
Start-Process "https://github.com/Emu-L8r/EmuBiz1"
```

**Result:** ✅ Issue fixed and main branch updated

---

## 💥 ROLLBACK OPTION 3: Full Emergency Rollback (Last Resort)

**⚠️ DESTRUCTIVE:** Only use if absolutely necessary. Discards all uncommitted work.

### Step 1: Verify This Is Necessary
```powershell
# Check what you're about to lose
git status
git diff

# If you see important changes, stash them FIRST
git stash
```

### Step 2: Reset to Checkpoint
```powershell
# ⚠️ WARNING: This discards all changes
# ⚠️ Make sure you've stashed important work above

git reset --hard v1.0-stable-golden

# Verify you're at checkpoint
git log --oneline -1
# Should show the checkpoint commit
```

### Step 3: Verify Rollback Successful
```powershell
# Build from checkpoint
./gradlew clean build --no-build-cache

# Run tests
./gradlew testDebugUnitTest

# Both should pass
```

### Step 4: Push to GitHub (If Production Emergency)
```powershell
# ⚠️ WARNING: Force push overwrites remote history
# Only do this in true emergency

git push origin main --force

# Alert team immediately
Write-Host "🚨 EMERGENCY ROLLBACK EXECUTED"
Write-Host "Repository rolled back to v1.0-stable-golden"
Write-Host "Notify team immediately!"
```

**Result:** ✅ Repository back to stable checkpoint (but history changed)

---

## ✅ VERIFICATION AFTER ROLLBACK

### Verification Checklist

```powershell
# 1. Verify Commit
git log --oneline -1
# Should show: commit matching v1.0-stable-golden

# 2. Verify Build
./gradlew clean build --no-build-cache 2>&1 | tail -20
# Should show: BUILD SUCCESSFUL (0 errors, 0 warnings)

# 3. Verify Tests
./gradlew testDebugUnitTest 2>&1 | tail -20
# Should show: BUILD SUCCESSFUL with 97%+ pass rate

# 4. Verify Tag
git describe --tags
# Should show: v1.0-stable-golden (or close to it)

# 5. Check Git History
git log --oneline -5
# Should show checkpoint near top
```

### All Green? ✅
If all checks pass:
- ✅ Rollback successful
- ✅ Checkpoint is stable
- ✅ Ready to investigate issue
- ✅ Can now proceed with fix

### Something Failed? ❌
If any checks fail:
- ❌ Run troubleshooting (see below)
- ❌ Contact team lead
- ❌ May need to investigate further

---

## 📊 POST-ROLLBACK ANALYSIS

### Step 1: Understand What Went Wrong
```powershell
# Compare checkpoint with main branch
git diff v1.0-stable-golden..main > rollback-diff.txt

# Show commits that broke things
git log v1.0-stable-golden..main --oneline > breaking-commits.txt

# Show file changes
git diff --name-status v1.0-stable-golden..main > changed-files.txt
```

### Step 2: Root Cause Analysis

| Question | Investigation |
|----------|---|
| **What failed?** | App crash? Build error? Test failure? |
| **When?** | On startup? During feature use? |
| **Where?** | Which component? Which file? |
| **Why?** | Logic error? Dependency issue? Configuration? |
| **How to fix?** | Code change? Configuration? Dependency update? |

### Step 3: Create Fix
```powershell
# Create feature branch for fix
git checkout -b fix/issue-description main

# Implement fix
# Edit files...
# Test locally

# Verify fix
./gradlew clean build
./gradlew testDebugUnitTest

# If tests pass and feature works, merge back
git checkout main
git merge fix/issue-description
git push origin main
```

### Step 4: Prevent Recurrence
```powershell
# Add test to catch this issue next time
# File: app/src/test/java/com/emul8r/bizap/...Test.kt

# Example:
# @Test
# fun testCriticalFeatureStillWorks() {
#     // Test the feature that broke
#     // Ensure it doesn't break again
# }

# Run test to verify it catches the issue
./gradlew test -k "testCriticalFeatureStillWorks"
```

---

## 🔧 TROUBLESHOOTING

### Issue: "Merge conflict" when trying to merge
```powershell
# View conflicts
git status

# For each conflicting file:
# 1. Open in editor
# 2. Resolve conflict (choose checkpoint version or new version)
# 3. Save file
# 4. git add <file>

# Complete merge
git commit -m "Resolve merge conflicts during rollback"
```

### Issue: "Build fails" even at checkpoint
```powershell
# Try complete clean
./gradlew clean build --no-build-cache --refresh-dependencies 2>&1 | tail -50

# Or reset gradle cache
Remove-Item .gradle -Recurse -Force
./gradlew clean build
```

### Issue: "Tests fail" at checkpoint
```powershell
# Restart emulator/device
# Uninstall and reinstall app
./gradlew uninstallDebug
./gradlew installDebug
./gradlew testDebugUnitTest
```

### Issue: "Can't checkout" the tag
```powershell
# Fetch latest from remote
git fetch origin

# Verify tag exists remotely
git ls-remote --tags origin | Select-String "v1.0-stable-golden"

# Try checking out again
git checkout v1.0-stable-golden
```

### Issue: "Force push rejected"
```powershell
# You need admin access to force push
# Contact repo admin to enable force push, or:

# Create new branch instead
git checkout -b rollback-fix
git push origin rollback-fix

# Create pull request to merge to main
```

---

## 📞 When to Contact Team Lead

- ❌ Rollback succeeded but original issue still present
- ❌ Multiple rollbacks needed in quick succession
- ❌ Force push to main required
- ❌ Data loss occurred (check backup)
- ❌ Production incident ongoing (coordinate response)

---

## 🚀 Success Criteria

```
✅ Checkout to checkpoint succeeds
✅ Build passes (0 errors, 0 warnings)
✅ Tests pass (97%+ pass rate)
✅ No merge conflicts
✅ Core features working on device
✅ Offline sync operational
✅ Database accessible
✅ Firebase connected
✅ Zero crash logs in Crashlytics
```

---

## 📋 Post-Rollback Checklist

- [ ] Rollback completed successfully
- [ ] Build verified (clean)
- [ ] Tests verified (97%+)
- [ ] App tested on device/emulator
- [ ] Core features working
- [ ] Offline queue tested
- [ ] PDF export working
- [ ] Root cause identified
- [ ] Fix implemented
- [ ] Tests added to prevent recurrence
- [ ] Team notified
- [ ] Incident closed

---

**Rollback Procedure:** ✅ COMPLETE  
**Checkpoint Stable:** ✅ YES  
**Emergency Procedures:** ✅ DOCUMENTED  

**For questions:** See CHECKPOINT_V1_0_BASELINE_APRIL17.md or RELEASE_NOTES_v1.0-stable-golden.md

