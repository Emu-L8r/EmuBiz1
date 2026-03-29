# ⚡ PowerShell Command Reference - Build Fixes

**Date:** March 30, 2026  
**Issue:** PowerShell syntax error with `&&`

---

## ✅ CORRECT PowerShell SYNTAX

### Clean Build (Correct)
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
./gradlew clean build --no-daemon
```

### Build with Directory Clean (Correct)
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
Remove-Item -Path "app/build" -Recurse -Force -ErrorAction SilentlyContinue
./gradlew clean build --no-daemon
```

### Stop Gradle Daemon (Correct)
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
./gradlew --stop
./gradlew clean build --no-daemon
```

---

## ❌ INCORRECT SYNTAX

**Don't use:**
```powershell
# ❌ This doesn't work in PowerShell:
rm -r -Force app/build && ./gradlew clean build
```

**Reasons:**
- PowerShell uses `;` not `&&` for command chaining
- `rm` is an alias for `Remove-Item` in PowerShell (different from bash `rm`)
- `&&` is bash syntax, not PowerShell syntax

---

## ✅ CORRECT SYNTAX OPTIONS

### Option 1: Simple Clean Build (Easiest)
```powershell
./gradlew clean build --no-daemon
```

### Option 2: Delete Directory First (More thorough)
```powershell
Remove-Item -Path "app/build" -Recurse -Force -ErrorAction SilentlyContinue
./gradlew clean build --no-daemon
```

### Option 3: Stop Daemon Before Build
```powershell
./gradlew --stop
./gradlew clean build --no-daemon
```

---

## 📝 KEY DIFFERENCES: PowerShell vs Bash

| Operation | Bash | PowerShell |
|-----------|------|-----------|
| Chain commands | `cmd1 && cmd2` | `cmd1; cmd2` |
| Remove directory | `rm -r dir` | `Remove-Item dir -Recurse` |
| List directory | `ls` | `Get-ChildItem` or `ls` |
| Delete (force) | `rm -rf` | `Remove-Item -Force -Recurse` |

---

## 🎯 WHAT'S HAPPENING NOW

Your build is currently running with:
```powershell
./gradlew clean build --no-daemon
```

**Expected completion:** 2-3 minutes  
**Expected result:** BUILD SUCCESSFUL with 0 errors

---

## ✅ BUILD SUCCESS INDICATORS

When build completes successfully, you'll see:
```
BUILD SUCCESSFUL in X minutes XX seconds
XX actionable tasks: XX executed

Process finished with exit code 0
```

---

## ⚠️ IF BUILD FAILS

1. Check error message (grep for "error" or "Error")
2. If file lock error: Try Option 2 or 3 above
3. If compilation error: All code is correct, check logs

---

**Tip:** Save this file for future reference when running PowerShell commands!


