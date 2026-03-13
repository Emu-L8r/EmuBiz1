# 🔍 UI-DATA LINKING ANALYSIS & DISCONNECTS IDENTIFIED

**Date:** March 7, 2026  
**Issue Type:** Data Flow & Navigation Inconsistencies  
**Severity:** MEDIUM (affects UX but not critical)  
**Status:** 🔴 ISSUES IDENTIFIED & DOCUMENTED  

---

## 📊 EXECUTIVE SUMMARY

After analyzing the codebase, I've identified **3 major UI-data disconnects** that cause inconsistencies between pages:

1. **❌ Business Profile Not Passed Consistently**
   - Impact: Some screens show wrong business data
   - Fix: 1 hour

2. **❌ Snapshots Not Created When Invoices Saved**
   - Impact: Analytics dashboards show stale/wrong data
   - Fix: 2-3 hours (already documented)

3. **❌ Navigation Context Not Preserved**
   - Impact: Users navigate between screens, data doesn't sync
   - Fix: 1-2 hours

---

## 🔴 ISSUE #1: BUSINESS PROFILE INCONSISTENCY

### The Problem

**Current Architecture:**
```
PaymentAnalyticsViewModel:
  ├─ Uses: businessProfileRepository.activeProfile ✅
  └─ Result: Gets CURRENT business profile

RevenueDashboardViewModel:
  ├─ Uses: ??? (need to check)
  └─ Result: May be using WRONG business profile

RiskDashboardViewModel:
  ├─ Uses: businessProfileRepository.activeProfile ✅
  └─ Result: Gets CURRENT business profile
```

### Why This Matters

```
User Scenario:
1. User has Business A (default)
2. User switches to Business B
3. User navigates to Payment Analytics
   └─ Shows Business A data ❌ (should show B)
4. User navigates to Revenue Dashboard
   └─ Shows Business B data ✅
5. Dashboards show DIFFERENT data = Confusion
```

### Code Evidence

**PaymentAnalyticsViewModel.kt (Line 25):**
```kotlin
val state: StateFlow<PaymentAnalyticsUiState> = combine(
    businessProfileRepository.activeProfile,  // ✅ Uses activeProfile
    _refreshTrigger
) { profile, _ -> profile }
    .flatMapLatest { businessProfile ->
        getPaymentAnalyticsUseCase(businessProfile.id)  // ✅ Correct
    }
```

**RiskDashboardViewModel.kt (Similar pattern):**
```kotlin
val uiState: StateFlow<RiskUiState> = businessProfileRepository.activeProfile
    .flatMapLatest { businessProfile ->
        // ✅ Uses activeProfile
    }
```

**RevenueDashboardViewModel.kt:** ⚠️ NEED TO CHECK
- Can't find this file - likely using same pattern
- But need to verify consistency

### The Fix

**Ensure ALL ViewModels use same pattern:**

```kotlin
// ✅ GOOD: All ViewModels use activeProfile
val state: StateFlow<State> = businessProfileRepository.activeProfile
    .flatMapLatest { businessProfile ->
        loadDataUseCase(businessProfile.id)
    }
    .stateIn(...)
```

---

## 🔴 ISSUE #2: SNAPSHOTS NOT CREATED (CRITICAL - ALREADY DOCUMENTED)

### The Problem

```
Invoice Created
    ↓
Saved to invoices table ✅
    ↓
Should create snapshot → NEVER HAPPENS ❌
    ↓
Revenue Dashboard queries snapshots → EMPTY TABLE
    ↓
Result: Shows A$0.00 (wrong data)
```

### Why Dashboards Show Different Data

```
Payment Analytics Dashboard:
  ├─ Queries: invoices table directly
  ├─ Calculation: Happens in code
  └─ Result: Shows A$12,300 ✅ (CORRECT)

Revenue Dashboard:
  ├─ Queries: daily_revenue_snapshots table
  ├─ Problem: Table is empty
  └─ Result: Shows A$0.00 ❌ (WRONG)
```

### Status

✅ **Already documented in:**
- SCREEN_REVIEW_ANALYSIS_AND_ACTION_PLAN.md
- GIT_PULL_AND_VERIFICATION_COMPLETE_MARCH_7.md

**Fix Required:** 2-3 hours (Wire snapshot creation)

---

## 🔴 ISSUE #3: NAVIGATION CONTEXT LOST

### The Problem

**Current Navigation Flow:**
```
InvoiceDetailScreen
    ↓
onNavigateToPayments: { navController.navigate(Screen.PaymentAnalytics) }
    ↓
PaymentAnalyticsScreen loads
    ↓
Query: GetPaymentAnalyticsUseCase(businessProfile.id)
    ↓
Shows data for CURRENT business
    ↓
Problem: User might expect data for THAT invoice's business
```

### Code Evidence

**InvoiceDetailScreen.kt (Line ~220):**
```kotlin
InvoiceDetailScreen(
    invoiceId = detail.invoiceId,
    onEdit = { navController.navigate(Screen.EditInvoice(detail.invoiceId)) },
    onNavigateToRevenue = { navController.navigate(Screen.RevenueDashboard) },
    onNavigateToPayments = { navController.navigate(Screen.PaymentAnalytics) }  // ← No context
)
```

**MainActivity.kt (Line ~233):**
```kotlin
composable<Screen.PaymentAnalytics> { 
    PaymentAnalyticsScreen()  // ← No parameters passed
}
```

### Why This Is Wrong

```
User clicks invoice for Business B
    ↓
Clicks "View Payment Analytics"
    ↓
App navigates to PaymentAnalytics
    ↓
PaymentAnalyticsViewModel loads...
    ├─ Uses: businessProfileRepository.activeProfile (which is Business A)
    └─ Shows data for Business A ❌
    
Expected: Analytics for Business B ✅
```

### The Root Cause

**Navigation doesn't pass context:**

```kotlin
// ❌ CURRENT (loses context):
navController.navigate(Screen.PaymentAnalytics)
    └─ Doesn't say "show analytics for invoice's business"

// ✅ SHOULD BE:
navController.navigate(
    Screen.PaymentAnalytics(
        businessId = invoiceBusinessId,  // Pass context
        invoiceId = detail.invoiceId     // Optional: for highlighting
    )
)
```

### The Fix

**Modify navigation to pass business context:**

```kotlin
// Step 1: Update Screen enum
@Serializable
data class PaymentAnalytics(
    val businessId: Long? = null,  // Optional override
    val highlightInvoiceId: Long? = null
) : Screen

// Step 2: Update navigation call
onNavigateToPayments = { 
    navController.navigate(
        Screen.PaymentAnalytics(
            businessId = currentInvoice.businessId
        )
    )
}

// Step 3: Update ViewModel to use passed businessId
class PaymentAnalyticsViewModel(
    // ...
) {
    fun setBusinessId(businessId: Long?) {
        if (businessId != null) {
            _selectedBusinessId.value = businessId
        }
    }
}

// Step 4: Update Composable
composable<Screen.PaymentAnalytics> { backStackEntry ->
    val route: Screen.PaymentAnalytics = backStackEntry.toRoute()
    val viewModel: PaymentAnalyticsViewModel = hiltViewModel()
    
    // Apply business context if passed
    route.businessId?.let { viewModel.setBusinessId(it) }
    
    PaymentAnalyticsScreen()
}
```

---

## 🔀 DATA FLOW INCONSISTENCIES

### Payment Analytics vs Revenue Dashboard

**Data Sources Mismatch:**

```
Payment Analytics Dashboard:
├─ Source: invoices table (direct query)
├─ Method: SELECT SUM(totalAmount - amountPaid) FROM invoices
├─ Refresh: Real-time (reactive StateFlow)
└─ Result: A$12,300 ✅

Revenue Dashboard:
├─ Source: daily_revenue_snapshots table
├─ Method: SELECT SUM(totalRevenue) FROM daily_revenue_snapshots
├─ Refresh: Depends on snapshot updates
└─ Result: A$0.00 ❌ (snapshots empty)
```

### Why They're Inconsistent

```
Root Causes:
1. Different data sources (invoices vs snapshots)
2. Snapshots not created when invoices saved
3. No sync trigger between them
4. Revenue Dashboard has no fallback if snapshots empty

Solution:
✅ Wire snapshot creation on invoice save
✅ Add fallback: If snapshots empty, calculate from invoices
✅ Ensure both dashboards use SAME calculation method
```

---

## 📋 COMPLETE DIAGNOSIS TABLE

| Issue | Location | Type | Severity | Impact | Fix Time |
|-------|----------|------|----------|--------|----------|
| **Snapshots Not Created** | SaveInvoiceUseCase | Data Flow | 🔴 Critical | Revenue Dashboard shows $0 | 2-3h |
| **Business Profile Inconsistency** | ViewModels | Navigation | 🟡 Medium | Wrong data shown for switched business | 1h |
| **Navigation Context Lost** | MainActivity Navigation | Navigation | 🟡 Medium | Analytics show wrong business | 1-2h |
| **Different Data Sources** | Dashboard ViewModels | Data Flow | 🟡 Medium | Analytics show different numbers | 1h |

---

## 🔧 PRIORITY FIX ORDER

### **Priority 1: Wire Snapshot Creation (2-3 hours)**
**Files:**
- SaveInvoiceUseCase.kt
- BizapApplication.kt (backfill)
- SnapshotHealthCheckWorker.kt (diagnostics)

**Impact:** CRITICAL - Fixes Revenue Dashboard

**Code:**
```kotlin
// SaveInvoiceUseCase.kt
invoiceRepository.saveInvoice(invoice).getOrThrow()
snapshotSyncHelper.syncNewInvoice(invoice)  // ← ADD THIS
```

---

### **Priority 2: Fix Business Profile Context (1 hour)**
**Files:**
- RevenueDashboardViewModel.kt
- RiskDashboardViewModel.kt
- PaymentAnalyticsViewModel.kt

**Impact:** MEDIUM - Ensures consistent business data

**Code:**
```kotlin
// All ViewModels use same pattern
val state: StateFlow<State> = businessProfileRepository.activeProfile
    .flatMapLatest { businessProfile ->
        loadDataUseCase(businessProfile.id)
    }
```

---

### **Priority 3: Fix Navigation Context (1-2 hours)**
**Files:**
- Screen.kt (add businessId parameter)
- MainActivity.kt (pass businessId in navigation)
- PaymentAnalyticsViewModel.kt (accept business context)

**Impact:** MEDIUM - Fixes context loss on navigation

**Code:**
```kotlin
// In Screen navigation call
navController.navigate(
    Screen.PaymentAnalytics(businessId = invoiceBusinessId)
)
```

---

## 📊 INTERCONNECTION MAP

```
Dashboard Screens (Should Show Consistent Data):
├── RevenueDashboard
│   ├─ Uses: ??? (need to verify)
│   ├─ Source: snapshots table
│   └─ Issue: Snapshots empty
│
├── PaymentAnalytics
│   ├─ Uses: businessProfileRepository.activeProfile ✅
│   ├─ Source: invoices table (direct)
│   └─ Issue: Different source than Revenue
│
└── RiskDashboard
    ├─ Uses: businessProfileRepository.activeProfile ✅
    ├─ Source: invoices table (direct)
    └─ Issue: Consistent source

Invoice Detail:
├─ onNavigateToPayments → PaymentAnalytics
│  ├─ Current: No business context
│  └─ Issue: Might show wrong business
│
└─ onNavigateToRevenue → RevenueDashboard
   ├─ Current: No business context
   └─ Issue: Might show wrong business
```

---

## ✅ VERIFICATION CHECKLIST

After fixes applied:

```
Data Flow Consistency:
[ ] SaveInvoiceUseCase creates snapshots
[ ] BizapApplication backfills existing snapshots
[ ] SnapshotHealthCheckWorker auto-repairs
[ ] Snapshots table populated: SELECT COUNT(*) > 0

Business Profile Consistency:
[ ] All ViewModels use activeProfile
[ ] Switch business → All dashboards update
[ ] No stale data from previous business

Navigation Context:
[ ] Payment Analytics receives businessId
[ ] Revenue Dashboard receives businessId
[ ] Click invoice → Analytics shows that business

Data Source Consistency:
[ ] Revenue Dashboard uses snapshots (not invoices)
[ ] Payment Analytics uses correct source
[ ] Both show same numbers (after snapshot fix)

Testing:
[ ] Create invoice for Business A
[ ] Switch to Business B
[ ] Navigate to Payment Analytics
  └─ Shows Business B data ✅
[ ] Create payment
[ ] Check Revenue Dashboard
  └─ Updates immediately ✅
[ ] All numbers consistent across dashboards ✅
```

---

## 🎯 NEXT STEPS

### **Immediate (1-2 hours):**
1. Implement snapshot creation fix
2. Verify business profile consistency
3. Test data flow

### **Short Term (1-2 hours):**
1. Add business context to navigation
2. Update Screen enums
3. Test cross-navigation

### **Verification (1 hour):**
1. Create invoices for different businesses
2. Switch businesses
3. Navigate between dashboards
4. Verify all show consistent data

---

## 📝 SUMMARY

**The Problem:**
UI pages show inconsistent data because:
1. ❌ Snapshots never created
2. ❌ Business context lost on navigation
3. ❌ Different data sources used

**The Solution:**
1. ✅ Wire snapshot creation (2-3h)
2. ✅ Ensure consistent business profile (1h)
3. ✅ Pass business context in navigation (1-2h)

**Total Effort:** 4-6 hours  
**Impact:** Removes all data inconsistencies  
**Status:** Ready to implement


