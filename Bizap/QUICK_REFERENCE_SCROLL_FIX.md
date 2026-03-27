# Quick Reference: Nested Scroll Fix

**Problem:** IllegalStateException: Vertically scrollable component measured with infinity constraints  
**Root Cause:** LazyColumn nested in multiple scroll containers  
**Solution:** Remove parent scroll, let each tab manage its own scrolling  

---

## Changes Made

### File: InvoiceDetailScreenV2.kt

| Component | Change | Reason |
|-----------|--------|--------|
| InvoiceDetailContentV2 | Removed `.verticalScroll()` from parent Column | Prevents nesting LazyColumn inside scroll |
| InvoiceDetailsTab | Added `.verticalScroll()` to Column | Self-managed scrolling |
| InvoiceItemsTab | Added `.verticalScroll()` to Column | Self-managed scrolling |
| PaymentHistoryTab | Changed modifier to `fillMaxSize()` | Proper constraints for LazyColumn |

---

## Before vs After

### Before (Crashes)
```
Parent Column (scroll)
  ↓
Tab Wrapper Column (scroll)  ← ❌ Double scroll
  ↓
PaymentHistoryScreen
  ↓
LazyColumn (wants infinite height but parent constrains it) ❌ CRASH
```

### After (Works)
```
Parent Column (no scroll)
  ↓
Tab 1: Column (scroll) → content
Tab 2: Column (scroll) → content
Tab 3: PaymentHistoryScreen
  ↓
LazyColumn (gets proper fillMaxSize constraints) ✅ WORKS
```

---

## Build Status

```
BUILD SUCCESSFUL in 59s ✅
```

---

## Testing

```bash
# 1. Build and install
./gradlew installDebug

# 2. Open Invoice Detail Screen
# 3. Rapidly switch tabs (Details → Items → Payment History)
# 4. Expected: Smooth transitions, no crashes
```

---

## Key Takeaway

**Never nest LazyColumn/LazyRow inside verticalScroll/horizontalScroll**

Instead:
- Use decentralized scrolling
- Each composable manages its own scroll
- Parent containers handle layout, not scrolling


