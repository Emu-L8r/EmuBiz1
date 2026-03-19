# Bizap Terminal Scripts - Quick Start Guide

## 📋 Overview

This directory contains PowerShell scripts to help you quickly resolve build issues and manage your Bizap project. Choose the script based on your situation.

---

## 🚀 Quick Start

### Scenario 1: Small Local Changes (Preferred)
You have uncommitted work you want to keep:

```powershell
.\fix-build.ps1
```

✓ Stashes your work  
✓ Updates from main  
✓ Cleans and builds  
✓ Your changes are preserved with `git stash`

---

### Scenario 2: Start Fresh (Nuclear Option)
You want to completely reset to main:

```powershell
.\quick-recovery.ps1
```

⚠️ **Deletes all uncommitted changes**  
✓ Hard reset to origin/main  
✓ Removes all untracked files  
✓ Full clean build  

---

### Scenario 3: Using Android Studio Helpers
Load helper functions for manual control:

```powershell
. .\android-studio-helpers.ps1
Sync-AndBuild -Clean
```

Available functions:
- `Invalidate-AndroidStudio` - Shows AS cache invalidation steps
- `Stop-GradleDaemon` - Kill Gradle daemon
- `Sync-AndBuild` - Full sync and build
- `Quick-Build` - Quick rebuild
- `Run-App` - Install on device

---

## 📊 Current Project Issues

| Issue | Status | Script | Notes |
|-------|--------|--------|-------|
| Build failures | ✗ Active | fix-build.ps1 | GuiV2NavGraph.kt parameter mismatches |
| Git conflicts | ✗ Active | fix-build.ps1 | Uncommitted changes blocking pull |
| Duplicate functions | ✗ Active | quick-recovery.ps1 | Navigation extensions duplicated |
| Gradle cache issues | ✗ Likely | Both scripts | Cleared by `./gradlew clean` |

---

## 🔍 Detailed Script Reference

### `fix-build.ps1` - Recommended for Most Cases
**Purpose:** Safe recovery while preserving work

**Steps:**
1. Stashes local changes to `git stash`
2. Fetches latest from GitHub
3. Pulls latest main branch
4. Runs `./gradlew clean assembleDebug`
5. Shows APK location if successful

**Restore stashed changes:**
```powershell
git stash pop
```

**Time:** ~3-5 minutes

---

### `quick-recovery.ps1` - Complete Reset
**Purpose:** Start completely fresh from origin/main

**Steps:**
1. Confirms your intent (safety check)
2. Hard resets: `git reset --hard origin/main`
3. Removes untracked files: `git clean -fd`
4. Cleans Gradle: `./gradlew clean`
5. Builds: `./gradlew assembleDebug --no-daemon`

**Before running:**
- Commit any important work to a branch
- Or extract files you need manually
- This is your last warning before all uncommitted work is lost

**Time:** ~3-5 minutes

---

### `android-studio-helpers.ps1` - Advanced Usage
**Purpose:** Manual control over build/sync process

**Functions:**

```powershell
# Display cache invalidation instructions
Invalidate-AndroidStudio

# Stop Gradle daemon (useful if it's hanging)
Stop-GradleDaemon

# Full sync and build with clean
Sync-AndBuild -Clean

# Sync and build without clean (faster)
Sync-AndBuild

# Quick rebuild using cache
Quick-Build

# Install app on connected device/emulator
Run-App
```

**Loading the script:**
```powershell
. .\android-studio-helpers.ps1
```

---

## 🛠️ Manual Steps (If Scripts Don't Work)

### Option A: Safe Manual Recovery
```powershell
# Backup your work first
git stash

# Update from remote
git fetch origin
git pull origin main

# Clean and build
./gradlew.bat clean assembleDebug --no-daemon

# Restore if needed
git stash pop
```

### Option B: Nuclear Manual Reset
```powershell
# Hard reset to remote main
git fetch origin
git reset --hard origin/main
git clean -fd

# Clean Gradle and rebuild
./gradlew.bat clean
./gradlew.bat assembleDebug --no-daemon
```

### Option C: Emergency Gradle Cleanup
```powershell
# If gradle seems stuck or corrupted
./gradlew.bat --stop
rm -r .gradle
./gradlew.bat clean assembleDebug --no-daemon
```

---

## ✅ Verification Checklist

After running a script, verify success:

- [ ] Script exits with no errors
- [ ] `git status` shows "on branch main"
- [ ] `git status` shows "nothing to commit, working tree clean"
- [ ] APK exists at: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] Android Studio shows no red error icons
- [ ] Can open project in Android Studio without sync errors

---

## 📝 Common Git Commands

```powershell
# Check current status
git status

# See what changed recently
git log --oneline -10

# View current branch
git branch

# List all branches
git branch -a

# See stashed work
git stash list

# Unstash work
git stash pop

# View changes
git diff
```

---

## 🆘 Troubleshooting

### Build still fails after script?
1. Try: `./gradlew.bat --status` (check daemon status)
2. Then: `./gradlew.bat --stop` (stop daemon)
3. Then: `./gradlew.bat clean assembleDebug --no-daemon`

### "Out of memory" errors?
```powershell
# Increase Gradle heap size
# Add to gradle.properties:
# org.gradle.jvmargs=-Xmx4096m
```

### Still seeing old errors in AS?
1. File → Invalidate Caches / Clear Cached Data
2. Choose: Invalidate and Restart
3. Restart Android Studio

### Git merge conflicts?
```powershell
# Abort the merge
git merge --abort

# Hard reset
git reset --hard HEAD

# Start over
.\fix-build.ps1
```

### Need to see what's wrong?
```powershell
# Run build with verbose output
./gradlew.bat assembleDebug --stacktrace --info
```

---

## 📚 Related Files

- `BUILD_FIX_GUIDE.md` - Detailed troubleshooting guide
- `app/build.gradle.kts` - Main build configuration
- `.gitignore` - Files ignored by git
- `gradle.properties` - Gradle settings

---

## 🎯 Recommended Workflow

1. **Before making changes:**
   - Ensure `git status` shows clean working tree
   - Run `git pull origin main` to get latest

2. **While working:**
   - Commit frequently with meaningful messages
   - Push to your feature branch regularly

3. **If build breaks:**
   - First: `git stash` (save work)
   - Second: `.\fix-build.ps1` (recover)
   - Third: Review changes and merge carefully

4. **When merging PRs:**
   - Let GitHub Actions verify the build
   - Don't merge if tests fail
   - Pull latest before starting new work

---

## ⚡ Pro Tips

- Keep a separate terminal window for builds while editing in another
- Run `./gradlew --offline` if you're working without internet (requires full build once online first)
- Use `Quick-Build` after small changes (much faster than clean build)
- Always commit before pulling if you have uncommitted work
- Use descriptive branch names for feature work

---

## 📞 Support

If scripts fail with error:
1. Check the error message carefully
2. Try the "Manual Steps" section above
3. Run: `./gradlew.bat assembleDebug --stacktrace` to see full error
4. Check Android Studio's Build → Analyze APK or Build Output

---

**Last Updated:** March 19, 2026  
**Project:** Bizap  
**Scripts Version:** 1.0

