# 🚀 ACTION PLAN: Phase 2 — Remove Mock Data & Wire Analytics

**Date**: March 3, 2026  
**Status**: Ready for execution  
**Estimated Time**: 2-3 hours  
**Dependencies**: Phase 1 (✅ COMPLETE)

---

## OVERVIEW

Remove mock/hardcoded data from ViewModels and wire them to actual data sources.

---

## TASK 1: Fix PaymentAnalyticsViewModel

**File**: `app/src/main/kotlin/com/emul8r/bizap/ui/invoice/analytics/PaymentAnalyticsViewModel.kt`

**Issue**: Returns hardcoded mock data regardless of actual invoices/payments

**Fix**:
1. Remove the hardcoded `mockAnalytics` object
2. Inject `InvoicePaymentDao` (or `AnalyticsDao` if it has payment metrics)
3. Create a reactive flow that queries real payment data
4. Return actual analytics or empty state if no data

**Pseudocode**:
```kotlin
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor(
    private val invoicePaymentDao: InvoicePaymentDao,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {
    
    val analytics: Flow<PaymentAnalyticsSummary> = 
        businessProfileRepository.activeProfileId.flatMapLatest { profileId ->
            invoicePaymentDao.getPaymentMetrics(profileId)
                .map { metrics ->
                    PaymentAnalyticsSummary(
                        businessProfileId = profileId,
                        totalInvoices = metrics.totalCount,
                        paidInvoices = metrics.paidCount,
                        unpaidInvoices = metrics.unpaidCount,
                        // ... map other fields
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, PaymentAnalyticsSummary.empty())
}
```

**Database Query Needed**:
- Check if `InvoicePaymentDao` exists and has methods to get payment counts/summaries
- If not, add a query: `SELECT COUNT(*), SUM(amount_paid) FROM invoice_payments WHERE business_profile_id = ?`

---

## TASK 2: Fix RevenueDashboardViewModel

**File**: `app/src/main/java/com/emul8r/bizap/ui/dashboard/RevenueDashboardViewModel.kt`

**Issue**: Hardcoded `private val businessId = 1L` — ignores multi-business

**Fix**:
1. Remove hardcoded `businessId = 1L`
2. Inject `BusinessProfileRepository`
3. Wire business ID to repository's activeProfileId flow
4. Update all DAO queries to use reactive businessId

**Pseudocode**:
```kotlin
@HiltViewModel
class RevenueDashboardViewModel @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {
    
    val businessId: StateFlow<Long> = businessProfileRepository.activeProfileId
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1L)
    
    val dailyRevenue: Flow<List<DailyRevenueSnapshot>> = businessId
        .flatMapLatest { profileId ->
            analyticsDao.getLast30DaysRevenue(profileId)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
```

---

## TASK 3: Fix RiskDashboardViewModel

**File**: `app/src/main/java/com/emul8r/bizap/ui/dashboard/RiskDashboardViewModel.kt`

**Issue**: Same as RevenueDashboard — hardcoded `businessId = 1L`

**Fix**: Same pattern as RevenueDashboardViewModel
- Remove hardcoded value
- Use reactive businessId from repository
- Update all DAO queries

---

## TASK 4: Fix DunningNoticesViewModel

**File**: `app/src/main/java/com/emul8r/bizap/ui/dunning/DunningNoticesViewModel.kt`

**Issue**: Same as above — hardcoded `businessId = 1L`

**Fix**: Same pattern
- Remove hardcoded value
- Use reactive businessId
- Update queries

---

## TASK 5: Verify PaymentAnalyticsScreen is Wired Correctly

**File**: `app/src/main/kotlin/com/emul8r/bizap/ui/invoice/analytics/PaymentAnalyticsScreen.kt`

**Check**:
1. Does it use `PaymentAnalyticsViewModel`?
2. Does it observe the `analytics` StateFlow?
3. Does it handle empty state gracefully (no data yet)?

**Fix** (if needed):
```kotlin
val analyticsState by viewModel.analytics.collectAsStateWithLifecycle()

when (analyticsState) {
    is PaymentAnalyticsSummary -> {
        // Display real analytics
        ShowAnalyticsUI(analyticsState)
    }
    null, PaymentAnalyticsSummary.empty() -> {
        // Show empty state
        EmptyState("No payment data yet")
    }
}
```

---

## VERIFICATION STEPS

After each fix, verify:

1. **Code compiles**:
   ```bash
   ./gradlew :app:compileDebugKotlin
   ```

2. **No dangling references**:
   ```bash
   grep -r "private val businessId = 1L" app/src/main/
   # Should return nothing
   ```

3. **Tests still pass**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

4. **Manual test on device**:
   - Create 2 business profiles
   - Create invoices in each
   - Switch between businesses
   - Revenue dashboard should update
   - Payment analytics should show real data

---

## COMMON PITFALLS

1. **Forget to update ALL references**
   - Search for `businessId = 1L` in all ViewModels
   - Ensure ALL queries pass the reactive businessId

2. **Mock data still hardcoded somewhere**
   - Remove all `mockAnalytics`, `mockRevenue`, etc. objects
   - Replace with actual DAO queries

3. **Reactive flow not set up correctly**
   - Test that `businessId` flow actually emits when business is switched
   - Test that dependent flows re-query when businessId changes

4. **Empty state not handled**
   - If no data exists yet, app should show empty state, not crash
   - Don't return hardcoded defaults, return null or empty list

---

## TIMELINE

- Task 1 (PaymentAnalytics): 30 minutes
- Task 2 (RevenueDashboard): 30 minutes
- Task 3 (RiskDashboard): 20 minutes
- Task 4 (DunningNotices): 20 minutes
- Task 5 (Verification): 30 minutes
- **Total**: ~2.5 hours

---

## SUCCESS CRITERIA

- ✅ No hardcoded `businessId = 1L` remaining
- ✅ No mock data in production code
- ✅ All ViewModels use reactive businessId from repository
- ✅ All queries parameterized by business profile
- ✅ Build passes
- ✅ Tests pass
- ✅ Manual test on device shows real data and reactive updates

---

**Ready to start?**

Once Phase 2 is complete, move to Phase 3: Business Profile Reactivity (create activeProfileId flow in repository)

