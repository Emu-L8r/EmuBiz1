# KNOWN LIMITATIONS & WORKAROUNDS

**Date:** March 21, 2026  
**Version:** 1.0  
**Status:** Current as of final testing  

---

## Overview

This document tracks known limitations, workarounds, and technical debt that users and developers should be aware of.

---

## 🟡 ACTIVE LIMITATIONS

### 1. Resource Shrinking Disabled

**What:** `isShrinkResources = false` in `build.gradle.kts`

**Why:** ProGuard resource optimization is currently disabled due to a resource naming conflict in the project's XML or Proto resources.

**Impact:** 
- APK size ~1-2 MB larger than optimal
- Not blocking for MVP/beta release
- Should be resolved before final Play Store release

**Workaround:** None needed - app works fine, just larger APK

**Resolution Timeline:** Post-v1.0 optimization  

**File to Update:** `app/build.gradle.kts` (search for `isShrinkResources`)

---

### 2. Dual GUI Maintenance Burden

**What:** GUI1 (Legacy/Classic) and GUI2 (Modern) are separate implementations

**Why:** Architectural decision to support both traditional Activities and modern Jetpack Compose

**Impact:** 
- Every new feature requires implementation in both places
- Higher maintenance cost (2x code review, 2x testing)
- Risk of "logic drift" (bug fixed in one GUI, not the other)

**Mitigation:** 
- ✅ Shared ViewModel layer (reduces drift)
- ✅ Same database (prevents data inconsistency)
- ✅ Feature parity matrix (this document tracks it)

**Sunset Plan:** 
- GUI1 (Classic) will be deprecated June 2027
- Users encouraged to migrate to GUI2 (Modern)
- GUI1 support for legacy users only

---

### 3. Native Library Complexity (SQLCipher)

**What:** App depends on SQLCipher for database encryption, which requires native `.so` files per architecture

**Why:** Security requirement - all user invoice/customer data must be encrypted at rest

**Impact:**
- Larger APK (~5 MB for SQLCipher native libs)
- Build complexity (must include arm64-v8a, armeabi-v7a, x86, x86_64)
- Risk if architecture mismatches (rare but possible on emulators)

**Workaround if crash:** 
- On emulator: Select "arm64-v8a" architecture when creating AVD
- Clean rebuild: `./gradlew clean assembleRelease`

**No changes needed** - this is intentional and working correctly

---

## 🟢 RESOLVED ISSUES

### ✅ Customer Email Validation (Fixed March 21)

**Previous Issue:** 
- Crash when creating invoice with customer that has no email
- Error: "IllegalArgumentException: Customer email cannot be blank"

**Fix Applied:**
- Removed email requirement from `CreateInvoiceViewModel.selectCustomer()`
- Email is now optional when creating customers
- Can select any customer for invoice, even without email

**Verification:** See FINAL_TESTING_READINESS_CHECKLIST.md, Phase 2.3

---

### ✅ Invoicing Velocity Chart (Fixed March 21)

**Previous Issue:**
- Bar chart showed all invoices as "SENT"
- No distinction between SENT and PAID invoices
- Misleading user data

**Fix Applied:**
- Extended `InvoiceVelocity` model with `invoicesPaidCount` field
- Updated SQL query to count SENT and PAID separately
- Stacked bar chart now shows:
  - Blue bars for SENT invoices
  - Green bars for PAID invoices

**Files Changed:**
- `AnalyticsModels.kt` (model)
- `AnalyticsDao.kt` (query)
- `InvoicingVelocityCard.kt` (visualization)

**Verification:** See FINAL_TESTING_READINESS_CHECKLIST.md, Phase 2.2

---

### ✅ Missing Top Bar Buttons (Fixed March 21)

**Previous Issue:**
- GUI1 had "Switch to GUI2" but no Settings button
- GUI2 had Settings but inconsistent button placement
- Users couldn't easily access Settings from GUI1

**Fix Applied:**
- Added Settings button (gear icon) to GUI1 top bar
- Added Switch to GUI1 button (swap icon) to GUI2 top bar
- Both buttons now consistent across both GUIs
- All top bar buttons in same location

**Files Changed:**
- `BizapTopAppBar.kt` (now accepts onSettingsClick)
- `MainActivity.kt` (GUI1 - pass Settings callback)
- `DashboardScreenV2.kt` (GUI2 - add Switch button)

**Verification:** See FINAL_TESTING_READINESS_CHECKLIST.md, Phase 1.5

---

### ✅ Duplicate Settings Options (Fixed March 21)

**Previous Issue:**
- Settings had both "Theme Mode" (dark/light toggle) AND "Advanced Color Themes"
- Both controlled similar functionality
- Confusing UX

**Fix Applied:**
- Removed standalone `ThemeSettingsCard` from `SettingsScreen.kt`
- Removed import of `ThemeSettingsCard`
- Kept only "Advanced Color Themes" which includes all theme controls

**File Changed:**
- `SettingsScreen.kt`

**Verification:** See FINAL_TESTING_READINESS_CHECKLIST.md, Phase 1.4

---

## ⚠️ TECHNICAL DEBT

### Build System Complexity
- **Impact:** Medium
- **Effort to Fix:** 3-4 hours
- **Priority:** Post-v1.0
- **Description:** Gradle build has many configurations. Could benefit from consolidation.

### Compose Nested Layouts
- **Impact:** Low (rare)
- **Effort to Fix:** 2-3 hours if occurs
- **Priority:** Monitor
- **Description:** LazyColumn inside LazyColumn can cause crashes. Mostly avoided via good architecture, but worth monitoring.

### Test Coverage for Navigation
- **Impact:** Medium
- **Effort to Fix:** 2-3 hours
- **Priority:** Post-v1.0
- **Description:** ~1,100 unit tests exist but few Compose integration tests for GUI switching

---

## 🔧 WORKAROUNDS FOR USERS

### If Settings not visible on GUI1
**Symptom:** Settings button (gear icon) not in top bar  
**Workaround:** Use bottom navigation bar → Settings option

### If GUI switch fails
**Symptom:** Tapping "Switch to GUI2" button crashes or doesn't navigate  
**Workaround:**  
1. Force close app: Settings → Apps → Bizap → Force Stop
2. Reopen app
3. Try switch again

### If data seems out of sync
**Symptom:** Invoice counts different in GUI1 vs GUI2  
**Workaround:**  
1. Pull down to refresh (if refresh available)
2. Navigate away and back to dashboard
3. Force close and reopen app

---

## 📋 DEPLOYMENT CHECKLIST

Before Play Store release:

- [ ] All tests passing (1,100+ unit tests)
- [ ] Manual testing completed (see FINAL_TESTING_READINESS_CHECKLIST.md)
- [ ] No crash reports in 48-hour testing period
- [ ] Resource shrinking issue documented (known limitation)
- [ ] All known issues documented in this file
- [ ] Feature parity verified (see GUI_PARITY_MATRIX.md)
- [ ] Signing credentials secured
- [ ] Release notes prepared

---

## 📞 SUPPORT

If users encounter issues:

1. **Check this document** - issue may be documented here with workaround
2. **Check FINAL_TESTING_READINESS_CHECKLIST.md** - verify expected behavior
3. **File bug report** with:
   - Device/OS version
   - Steps to reproduce
   - Screenshots
   - Exact error message

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | March 21, 2026 | Initial document - all fixes applied |
| - | - | - |

---

**Last Updated:** March 21, 2026  
**Maintained By:** Development Team  
**Next Review:** Post-v1.0 release


