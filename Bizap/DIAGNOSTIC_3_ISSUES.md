# 🔧 DIAGNOSTIC STEPS - 3 ISSUES (UPDATED)

## Issue #1: Selection swaps back to Modern ✅ VERIFIED FIX
**Fix Applied:** 
- Removed stale data reload in `InvoiceSettingsViewModel`.
- UI State is now the source of truth during the save cycle.
- Tested: Selection persists after "Save Settings" snackbar.

---

## Issue #2: PDF shows blank page ✅ FIX APPLIED (Verify in Samsung Notes)
**Fix Applied:**
- Switched to `HtmlConverter.convertToPdf()` for direct stream writing.
- Added explicit `outputStream.flush()` to prevent "0-page" PDF generation.
- **Verification Logcat Check:** Look for `✅ PDF created: ..., size: > 10KB` (Previous was 1.1 KB).

---

## Issue #3: Black screen on app load 🔍 DIAGNOSIS NEEDED
**Potential Root Causes:**
1. **Theme Initialization:** If `ThemeManager` hangs, Compose won't render.
2. **Hilt Injection:** Check if `AuthenticationManager` is failing to initialize.
3. **App State Stuck:** Is `AppState.SplashLoading` never transitioning?

**Immediate Diagnostic Command:**
`adb logcat *:E | grep com.emul8r.bizap`
- Look for `NullPointerException` in `MainActivity`.
- Look for `CompositionLocal not present` errors.

---

## 🚀 THE "CORRECT" ARCHITECTURE (Per Project Review)
- **Currency:** Continue using **Long (cents)**. Ensure PDF formatting uses `String.format("%.2f", cents/100.0)`.
- **UI:** Stick to the **GUI Consolidation** pattern. Do not fork screens for GUI1/GUI2.
- **Docs:** Maintain the `INVOICE_SPECIFICATION.md` for team/agent alignment.
