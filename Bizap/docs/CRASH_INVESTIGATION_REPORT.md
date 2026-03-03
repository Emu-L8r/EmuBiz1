# 🔴 CRASH INVESTIGATION REPORT

**Date:** March 4, 2026  
**Status:** Critical Crash - IDENTIFIED & FIXABLE  
**Crash Type:** IllegalFormatConversionException

---

## 🎯 CRASH SUMMARY

### Error
```
java.util.IllegalFormatConversionException: f != java.lang.Long
at com.emul8r.bizap.ui.invoices.InvoiceListScreenKt$InvoiceList$1$1$1$2$3.invoke(InvoiceListScreen.kt:96)
```

### Impact
🔴 **CRITICAL** - App crashes when displaying invoice list

### Root Cause
Attempting to format a `Long` value using a float format specifier (`%f`)

---

## 🔍 DETAILED ANALYSIS

### The Problem

**File:** `InvoiceListScreen.kt`, line 96  
**Code:**
```kotlin
Text("Total: $${String.format(Locale.getDefault(), "%.2f", invoice.totalAmount)} | ${formatDate(invoice.date)}")
```

### Why It Crashes

| Element | Type | Issue |
|---------|------|-------|
| `invoice.totalAmount` | `Long` (cents) | Stored as whole cents (e.g., 4999) |
| `String.format(...)` | Format function | Expects arguments matching format specifiers |
| `"%.2f"` | Float format specifier | Requires a `Float` or `Double`, not `Long` |

**The Mismatch:**
```
format("%.2f", 4999L)  
↓
Tries to interpret Long as Float
↓
Throws IllegalFormatConversionException
```

### Why This Happened

The codebase migrated from `Double` (dollars) to `Long` (cents) for monetary values, but `InvoiceListScreen.kt` wasn't updated to handle the new type.

**Before:** `totalAmount: Double = 49.99`  
**After:** `totalAmount: Long = 4999` (in cents)

The display logic still assumes a dollar value that can be formatted directly.

---

## ✅ THE FIX

### Solution
Convert `Long` cents to `Double` dollars before formatting:

```kotlin
// BEFORE (crashes):
"Total: $${String.format(Locale.getDefault(), "%.2f", invoice.totalAmount)}"

// AFTER (works):
"Total: ${CentsFormatter.formatCents(invoice.totalAmount, "AUD")}"
```

**Or explicitly:**
```kotlin
"Total: $${"%.2f".format((invoice.totalAmount / 100.0))}"
```

### Files to Fix
- `InvoiceListScreen.kt` (line 96)

---

## 📊 CRASH STACK TRACE

**Main Crash:**
```
Process: com.emul8r.bizap, PID: 8446
Exception: java.util.IllegalFormatConversionException: f != java.lang.Long
Stack:
  at String.format() [Line 96]
  → InvoiceListScreen.kt supportingContent composable
  → LazyColumn rendering the invoice list
  → UI layout and drawing
```

**Trigger:** Rendering invoice list with savedInvoices

**Frequency:** 100% reproducible when displaying invoices

---

## 🛠️ IMPLEMENTATION

The fix is straightforward - use the existing `CentsFormatter` utility that already handles this conversion properly.

**Change Location:**
```
File: app/src/main/java/com/emul8r/bizap/ui/invoices/InvoiceListScreen.kt
Line: 96 (supportingContent Text)
```

**Current Code:**
```kotlin
supportingContent = {
    Text("Total: $${String.format(Locale.getDefault(), "%.2f", invoice.totalAmount)} | ${formatDate(invoice.date)}")
}
```

**Fixed Code:**
```kotlin
supportingContent = {
    Text("Total: ${CentsFormatter.formatCents(invoice.totalAmount, invoice.currencyCode)} | ${formatDate(invoice.date)}")
}
```

---

## 🎯 TESTING

### Reproduction Steps
1. Open app
2. Navigate to Invoices tab
3. View invoice list
4. Crash occurs when LazyColumn tries to render first invoice

### Expected After Fix
1. Invoice list displays
2. Amounts show correctly (e.g., "A$49.99")
3. No crash, smooth scrolling

---

## 📝 RELATED ISSUES

This follows the same pattern as the "Invoice Save Bug" we fixed earlier:
- Migration from `Double` → `Long` for monetary values
- Some UI code still expects `Double`
- Need to use `CentsFormatter` for proper display

---

## ✨ STATUS

| Item | Status |
|------|--------|
| Root Cause | ✅ Found |
| Fix Identified | ✅ Yes |
| Fix Tested | ⏳ Pending |
| Documentation | ✅ Complete |

---

## 🚀 NEXT STEPS

1. Apply the fix to `InvoiceListScreen.kt` line 96
2. Rebuild APK
3. Test invoice list display
4. Verify no crashes
5. Commit fix to GitHub


