# Bizap Build Fix Guide

## Current Status
Your project has multiple build failures due to:
1. Parameter mismatches in composable function calls
2. Duplicate navigation extension functions
3. Uncommitted local changes conflicting with remote updates

## Available Scripts

### Script 1: `fix-build.ps1` (RECOMMENDED - Safer Option)
**What it does:**
- Stashes your local changes (preserves them)
- Fetches latest from remote
- Pulls latest main branch
- Cleans and builds
- Preserves local work with `git stash`

**When to use:**
- You have work in progress you want to keep
- You want a gradual recovery
- You prefer to review changes before fully committing

**How to run:**
```powershell
.\fix-build.ps1
```

**To restore your stashed changes later:**
```powershell
git stash pop
```

---

### Script 2: `quick-recovery.ps1` (AGGRESSIVE - Nuclear Option)
**What it does:**
- Hard resets to origin/main
- Removes ALL local changes
- Cleans all untracked files
- Does a full clean build
- Gets project to 100% stable state immediately

**When to use:**
- You want a completely clean slate
- Local changes are causing too many conflicts
- You're ready to start fresh
- You don't need your local uncommitted work

**How to run:**
```powershell
.\quick-recovery.ps1
```

**WARNING:** This will DELETE all uncommitted local changes!

---

## Manual Alternative (Step by Step)

If you prefer doing it manually:

```powershell
# Step 1: Stash changes (optional)
git stash

# Step 2: Fetch latest
git fetch origin

# Step 3: Pull main
git pull origin main

# Step 4: Clean Gradle
./gradlew.bat clean

# Step 5: Build
./gradlew.bat assembleDebug --no-daemon

# Step 6: (Optional) Restore stashed changes
git stash pop
```

---

## Troubleshooting

### Build still fails?
1. Clear more cache: `./gradlew.bat --stop` then try again
2. Delete build folder: `rm -r app/build`
3. Delete .gradle folder: `rm -r .gradle`
4. Then run: `./gradlew.bat clean assembleDebug`

### Git merge conflicts?
```powershell
# View conflicts
git diff

# Reset if needed
git merge --abort
git reset --hard HEAD
```

### "No parameter with name" errors still present?
These indicate the composable signatures don't match the callsites. Run the scripts to ensure you have the latest code that matches.

---

## Recommended Sequence

1. **First Time:** Run `fix-build.ps1` to safely recover
2. **If that doesn't work:** Run `quick-recovery.ps1` for full reset
3. **If you still have issues:** Check Android Studio for additional errors or contact team

---

## Project Health Status

**Current Issues:**
- ✗ Build failures in GuiV2NavGraph.kt
- ✗ Duplicate navigation functions
- ✗ Parameter mismatches in composables
- ✗ Uncommitted changes blocking pull

**After Running Scripts:**
- ✓ Should resolve all conflicts
- ✓ Should have working build
- ✓ Ready for development
- ✓ Main branch up to date

---

## Post-Fix Verification

After running a script, verify success:

```powershell
# Check build status
./gradlew.bat assembleDebug --dry-run

# Verify APK exists
ls app/build/outputs/apk/debug/app-debug.apk

# Check git status
git status
```

---

## Need More Info?

Check these files for context:
- `app/src/main/java/com/emul8r/bizap/ui/gui2/navigation/GuiV2NavGraph.kt`
- `app/src/main/java/com/emul8r/bizap/ui/gui2/navigation/NavExtensionsV2.kt`
- `app/src/main/java/com/emul8r/bizap/MainActivity.kt`

---

Generated: March 19, 2026

