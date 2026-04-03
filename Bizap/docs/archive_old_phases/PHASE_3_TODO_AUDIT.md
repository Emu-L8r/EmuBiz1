# 📋 PHASE 3 - TODO AUDIT & TECHNICAL DEBT ANALYSIS

**Date:** March 29, 2026  
**Total TODOs Found:** 20  
**Priority Breakdown:** Critical (1), High (5), Medium (10), Low (4)

---

## 🔴 CRITICAL (1) - Block Completion

### #1: GUI2 Notes Screen Implementation
**File:** `GuiV2NavGraph.kt:68`  
**Priority:** 🔴 CRITICAL  
**Description:**
```kotlin
// TODO: Implement Notes screen for GUI2
```

**Context:** Notes button on GUI2 dashboard navigates but has no UI yet. Currently shows TODO comment.

**Action Required:**
- Create `NotesScreenV2.kt` composable
- Add notes data model
- Implement CRUD operations
- Wire to navigation

**Estimated Effort:** 2-3 hours  
**Blocking:** No (GUI2 notes partially implemented)

---

## 🟠 HIGH (5) - Should Fix This Sprint

### #2: Dashboard Search Repository Integration  
**File:** `DashboardScreenV2.kt:162, 655`  
**Priority:** 🟠 HIGH  
**Description:**
```kotlin
// TODO: Wire to actual search repository in Week 2
// TODO: Replace with real search repository in Week 2
```

**Context:** Analytics search bar uses mock data instead of real CustomerDao/InvoiceDao queries.

**Action Required:**
- Wire `SearchRepository` to AnalyticsSearchBar
- Query CustomerDao for customers
- Query InvoiceDao for invoices
- Remove mock data

**Estimated Effort:** 1-2 hours  
**Business Value:** High - Direct user impact

---

### #3: Dashboard Metrics ViewModel Integration
**File:** `DashboardScreenV2.kt:194`  
**Priority:** 🟠 HIGH  
**Description:**
```kotlin
// TODO: Wire metrics from ViewModel once repository is wired
```

**Context:** Dashboard metrics currently use mock data. Should be wired to ViewModel.

**Action Required:**
- Create `DashboardMetricsViewModel` if not exists
- Wire `getDashboardMetrics()` from repository
- Update DashboardScreenV2 to use ViewModel instead of mock data
- Test with real database

**Estimated Effort:** 1.5 hours  
**Status:** Partially done - metrics calculate correctly but could be formalized in ViewModel

---

### #4-5: Revenue Analytics Repository Wiring
**Files:** 
- `RevenueAnalyticsViewModel.kt:71` - Wire to actual invoice queries
- `GetRevenueAnalyticsTrendUseCase.kt:29` - Wire with RevenueRepository

**Priority:** 🟠 HIGH  
**Description:**
```kotlin
// TODO: Wire to actual invoice queries in future
// TODO: Wire with RevenueRepository once repository is implemented.
```

**Context:** Revenue analytics use placeholder data structures.

**Action Required:**
- Implement RevenueRepository queries
- Wire invoice date/amount data to analytics
- Test with real transactions

**Estimated Effort:** 2 hours

---

## 🟡 MEDIUM (10) - Nice To Have

### #6-7: Chart Integration (Vico Library)
**Files:**
- `LineChartCard.kt:17` - Integrate Vico CartesianChartHost
- `BarChartCard.kt:16` - Integrate Vico column chart

**Priority:** 🟡 MEDIUM  
**Description:**
```kotlin
// TODO: Integrate Vico CartesianChartHost when dependency is verified.
// TODO: Integrate Vico column chart when dependency is verified.
```

**Status:** Placeholder components exist, real charts not integrated  
**Estimated Effort:** 2-3 hours

---

### #8-10: Payment Analytics Filtering
**Files:**
- `PaymentAnalyticsTabViewModel.kt:33` - Respect date range filtering
- `PaymentAnalyticsTabViewModel.kt:70` - Implement filtering logic
- `CustomerAnalyticsTabViewModel.kt:79` - Implement filtering logic

**Priority:** 🟡 MEDIUM  
**Description:**
```kotlin
// TODO: Enhance this to respect date range filtering once repository supports it.
// TODO: Implement filtering logic once repository supports date ranges.
```

**Context:** Date range filters exist in UI but filtering logic incomplete.

**Estimated Effort:** 1.5 hours  
**Status:** Partially done - basic filtering works

---

### #11: Point-Specific Drill Implementation
**File:** `RevenueAnalyticsTab.kt:107`  
**Priority:** 🟡 MEDIUM  
**Description:**
```kotlin
// TODO: Implement point-specific drill
```

**Context:** Charts show data but don't support drilling into specific data points.

**Estimated Effort:** 1 hour

---

### #12: Custom Date Range Dialog
**File:** `AnalyticsFocusedInsightsScreen.kt:104`  
**Priority:** 🟡 MEDIUM  
**Description:**
```kotlin
// TODO: Implement custom date range dialog
```

**Context:** Analytics screens support preset date ranges but not custom ones.

**Estimated Effort:** 1 hour

---

## 🟢 LOW (4) - Nice To Have / Future Work

### #13-15: Backup/Restore Deletion Implementation
**Files:**
- `BackupRestoreViewModel.kt:157` - Implement actual deletion in services
- `BackupRestoreViewModel.kt:191` - Implement customer deletion
- `BackupRestoreViewModel.kt:222` - Implement invoice deletion

**Priority:** 🟢 LOW  
**Description:**
```kotlin
// TODO: Implement actual deletion in services/repositories:
// TODO: Implement customer deletion:
// TODO: Implement invoice deletion:
```

**Context:** Backup/Restore feature has UI but deletion logic incomplete.

**Estimated Effort:** 1.5 hours  
**Business Value:** Medium

---

### #16: Send Reminder Screen Navigation
**File:** `DashboardScreenV2.kt:229`  
**Priority:** 🟢 LOW  
**Description:**
```kotlin
// TODO: Navigate to send reminder screen
```

**Context:** Dashboard has reminder button but navigation not implemented.

**Estimated Effort:** 0.5 hours

---

### #17: Discount Calculation
**File:** `CalculateInvoiceMetricsUseCase.kt:30`  
**Priority:** 🟢 LOW  
**Description:**
```kotlin
// TODO: Implement discount calculation when Invoice model is extended with discountAmount field
```

**Context:** Metrics calculation doesn't include discounts (not in data model yet).

**Estimated Effort:** 0.5 hours (when discounts added to Invoice)

---

### #18: Test Compatibility Issue
**File:** `CrossGUISyncTest.kt:44`  
**Priority:** 🟢 LOW  
**Description:**
```kotlin
@Ignore("TODO: Fix RevenueRepositoryImpl tickerFlow test compatibility - MockK issue with ticker emissions")
```

**Context:** Unit test skipped due to MockK compatibility with ticker.

**Estimated Effort:** 1 hour  
**Action:** Fix test infrastructure or mock setup

---

## 📊 SUMMARY TABLE

| Priority | Count | Total Hours | Quick Wins | Block Release |
|----------|-------|-------------|-----------|--------------|
| 🔴 CRITICAL | 1 | 2-3h | No | No (Notes is nice-to-have) |
| 🟠 HIGH | 5 | 6-7h | **Yes (2)** | No |
| 🟡 MEDIUM | 10 | 8-10h | Yes (3) | No |
| 🟢 LOW | 4 | 3-4h | Yes (2) | No |
| **TOTAL** | **20** | **19-24h** | **~7 items** | **None** |

---

## ✅ QUICK WINS (7 items, ~5-6 hours)

These can be done quickly to reduce technical debt:

1. **#2: Dashboard Search** - Wire real searches (1.5h) - HIGH VALUE
2. **#3: Dashboard Metrics ViewModel** - Formalize metrics (1h) - MEDIUM VALUE
3. **#6-7: Chart Integration** - Hook up Vico charts (2h) - MEDIUM VALUE
4. **#16: Reminder Navigation** - Wire screen (0.5h) - LOW VALUE
5. **#17: Discount Calculation** - When model updated (0.5h) - LOW VALUE
6. **#18: Fix Unit Test** - MockK compatibility (1h) - LOW VALUE

---

## 🔴 CRITICAL BLOCKERS

**None.** All TODOs are either enhancements or nice-to-haves. No TODOs block the app from working correctly.

---

## 📌 RECOMMENDATIONS

### **For Phase 3 Continuation:**

**Priority 1:** Implement #2 (Dashboard Search) - High user impact, quick to implement  
**Priority 2:** Complete #3 (Metrics ViewModel) - Proper architecture  
**Priority 3:** Wire analytics filters (#8-10) - Complete analytics feature  
**Priority 4:** Add Charts integration (#6-7) - Visual polish

### **For Future Sprints:**

- GUI2 Notes screen (#1)
- Advanced filtering & drill-down (#11-12)
- Backup deletion logic (#13-15)
- Revenue analytics completeness (#4-5)

---

**Next Action:** Choose 2-3 quick wins to implement from this audit.


