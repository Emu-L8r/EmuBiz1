# Files to Delete - Master Prompt Issues 4 & 5

## ISSUE 4: Stale InvoiceDetailViewModel.kt

**Path:** `Bizap/ui/invoices/InvoiceDetailViewModel.kt`

This is a stale duplicate file outside `app/src/main/java`. The correct file is at:
`Bizap/app/src/main/java/com/emul8r/bizap/ui/invoices/InvoiceDetailViewModel.kt`

**Command to delete:**
```bash
rm -r Bizap/ui/invoices/InvoiceDetailViewModel.kt
```

---

## ISSUE 5: WorkManagerInitializer.kt

**Path:** `Bizap/app/src/main/java/com/emul8r/bizap/di/WorkManagerInitializer.kt`

This file conflicts with `BizapApplication.kt` which already initializes WorkManager via `Configuration.Provider`.

**Command to delete:**
```bash
rm Bizap/app/src/main/java/com/emul8r/bizap/di/WorkManagerInitializer.kt
```

---

## ISSUE 1 (Still Pending): Delete OLD BusinessProfileRepository

**Path:** `Bizap/app/src/main/java/com/emul8r/bizap/data/repository/BusinessProfileRepository.kt`

This is the OLD concrete class that was replaced by the domain interface. All imports have been fixed to use the domain interface and impl class.

**Command to delete:**
```bash
rm Bizap/app/src/main/java/com/emul8r/bizap/data/repository/BusinessProfileRepository.kt
```

---

## How to Execute

Use PowerShell in the repository root:
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz"

# Delete all three files
Remove-Item -Path "Bizap/ui/invoices/InvoiceDetailViewModel.kt" -Force
Remove-Item -Path "Bizap/app/src/main/java/com/emul8r/bizap/di/WorkManagerInitializer.kt" -Force
Remove-Item -Path "Bizap/app/src/main/java/com/emul8r/bizap/data/repository/BusinessProfileRepository.kt" -Force

# Verify they're gone
if (Test-Path "Bizap/ui/invoices/InvoiceDetailViewModel.kt") { Write-Host "ERROR: InvoiceDetailViewModel.kt still exists" } else { Write-Host "OK: InvoiceDetailViewModel.kt deleted" }
if (Test-Path "Bizap/app/src/main/java/com/emul8r/bizap/di/WorkManagerInitializer.kt") { Write-Host "ERROR: WorkManagerInitializer.kt still exists" } else { Write-Host "OK: WorkManagerInitializer.kt deleted" }
if (Test-Path "Bizap/app/src/main/java/com/emul8r/bizap/data/repository/BusinessProfileRepository.kt") { Write-Host "ERROR: BusinessProfileRepository.kt still exists" } else { Write-Host "OK: BusinessProfileRepository.kt deleted" }
```

---

## After Deleting

Run the build verification:
```powershell
cd Bizap
./gradlew clean :app:assembleDebug
```

Expected: BUILD SUCCESSFUL with no import errors

