# Terminal Scripts Cheat Sheet

## 🎯 Decision Tree

```
Do you want to keep local changes?
    ├─ YES → Run: .\fix-build.ps1
    │        (preserves work with git stash)
    │
    └─ NO → Run: .\quick-recovery.ps1
             (nukes everything, fresh start)
```

## 🚀 Commands At A Glance

### Safe Recovery (Recommended)
```powershell
.\fix-build.ps1
```

### Nuclear Reset
```powershell
.\quick-recovery.ps1
```

### Load Helper Functions
```powershell
. .\android-studio-helpers.ps1
Sync-AndBuild -Clean
```

## 📋 Script Comparison Matrix

```
                     fix-build.ps1   quick-recovery.ps1   helpers.ps1
────────────────────────────────────────────────────────────────────
Keeps your work      ✓               ✗                    Manual
Time to complete     5 min           5 min                Variable
Git complexity       Simple          Simple               Advanced
Recommended for      Most cases      Last resort          Power users
Risk level           🟢 Low          🔴 Critical          🟡 Medium
────────────────────────────────────────────────────────────────────
```

## 🔧 Common Tasks

| Task | Command |
|------|---------|
| **Recover safely** | `.\fix-build.ps1` |
| **Complete reset** | `.\quick-recovery.ps1` |
| **Just rebuild** | `./gradlew.bat assembleDebug` |
| **Clean then build** | `./gradlew.bat clean assembleDebug` |
| **See what's wrong** | `./gradlew.bat assembleDebug --stacktrace` |
| **Stop gradle daemon** | `./gradlew.bat --stop` |
| **Check git status** | `git status` |
| **Restore stashed work** | `git stash pop` |
| **View git history** | `git log --oneline -5` |
| **View available branches** | `git branch -a` |

## ✅ Verification Checklist

After any build fix:

- [ ] Script completed without error
- [ ] No red X marks in Android Studio
- [ ] APK file exists in `app/build/outputs/apk/debug/`
- [ ] `git status` shows "working tree clean"
- [ ] Can rebuild cleanly: `./gradlew.bat assembleDebug`

## 🚨 Emergency Commands

```powershell
# If gradle is hung
./gradlew.bat --stop

# If everything is corrupted
rm -r .gradle
rm -r app/build

# Ultra nuclear option
git reset --hard HEAD
git clean -fd
./gradlew.bat clean assembleDebug --no-daemon
```

## 📞 Troubleshooting Quick Links

| Problem | Solution |
|---------|----------|
| "No parameter with name" | Run `.\fix-build.ps1` |
| "Conflicting overloads" | Run `.\fix-build.ps1` |
| Build timeout | Run `./gradlew.bat --stop` |
| Out of memory | Increase heap in gradle.properties |
| Still seeing errors | Invalidate AS caches: File → Invalidate Caches |
| Git merge conflict | Run `git merge --abort` then `.\fix-build.ps1` |

## 💾 File Reference

| File | Purpose |
|------|---------|
| `fix-build.ps1` | Safe recovery script |
| `quick-recovery.ps1` | Nuclear reset script |
| `android-studio-helpers.ps1` | Helper functions |
| `QUICK_START.md` | This file's parent doc |
| `SCRIPTS_README.md` | Full documentation |
| `BUILD_FIX_GUIDE.md` | Detailed troubleshooting |

## 🎬 Standard Recovery Workflow

```
1. Pick a script based on decision tree
   ↓
2. Run: .\<script-name>.ps1
   ↓
3. Wait 5-10 minutes
   ↓
4. Check for success markers (✓ above)
   ↓
5. If failed, try manual emergency commands
   ↓
6. Resume development!
```

## 🔗 Quick Links in This Project

- Main build config: `app/build.gradle.kts`
- Navigation graph: `app/src/main/java/.../GuiV2NavGraph.kt`
- Navigation helpers: `app/src/main/java/.../NavExtensionsV2.kt`
- Main activity: `app/src/main/java/.../MainActivity.kt`

## 📝 Git Stash Reference

```powershell
# Save work (done automatically by fix-build.ps1)
git stash

# List all stashes
git stash list

# Restore most recent stash
git stash pop

# Restore specific stash
git stash pop stash@{0}

# View stash contents
git stash show -p stash@{0}

# Delete a stash
git stash drop stash@{0}
```

## ⚙️ Manual Build Commands

```powershell
# Basic build
./gradlew.bat assembleDebug

# Build with stack trace (see full errors)
./gradlew.bat assembleDebug --stacktrace

# Build with detailed logging
./gradlew.bat assembleDebug --info

# Clean build
./gradlew.bat clean assembleDebug

# Clean then build with no daemon (most reliable)
./gradlew.bat clean assembleDebug --no-daemon

# Dry run (check if it would build)
./gradlew.bat build --dry-run

# Build and install on device
./gradlew.bat installDebug
```

---

**For full documentation, see:** `SCRIPTS_README.md`  
**For detailed troubleshooting, see:** `BUILD_FIX_GUIDE.md`  
**Updated:** March 19, 2026

