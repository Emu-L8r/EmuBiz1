# 🔧 Bizap Health Report — Remediation Checklist

**Date:** April 9, 2026  
**Status:** Action Items for Production Readiness

---

## 🔴 **WEEK 1: CRITICAL FIXES** (Must-Do for Production)

### Task 1: Fix PINStorage Synchronous I/O
**Priority:** CRITICAL | **Effort:** 2-3 hours | **Impact:** HIGH

**Problem:**
- PINStorage.kt uses synchronous SharedPreferences calls
- Blocks main thread for 20-50ms on every PIN check
- Files: `data/local/PINStorage.kt:33-67`

**Solution:**
```kotlin
// Replace SharedPreferences with DataStore
class PINStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
    private val PIN_SALT_KEY = stringPreferencesKey("pin_salt")
    
    suspend fun isPINSet(): Boolean {
        return dataStore.data.first()[PIN_HASH_KEY] != null
    }
    
    suspend fun verifyPIN(pin: String): Boolean {
        val storedHash = dataStore.data.first()[PIN_HASH_KEY] ?: return false
        val salt = dataStore.data.first()[PIN_SALT_KEY] ?: return false
        // ... verification logic ...
    }
}
```

**Checklist:**
- [ ] Create new `PINStorageV2.kt` with DataStore
- [ ] Migrate existing PIN data from SharedPreferences
- [ ] Update AuthenticationManager to use new storage
- [ ] Test PIN setup and verification flows
- [ ] Remove old PINStorage.kt
- [ ] Run manual UI tests on PIN screens

**Success Criteria:**
- No synchronous I/O on main thread
- PIN verification time < 5ms
- All PIN tests passing

---

### Task 2: Optimize Database Queries
**Priority:** CRITICAL | **Effort:** 4-6 hours | **Impact:** HIGH

**Problem:**
- DATE() in WHERE clauses prevents index usage
- Query p99 latency: 250ms (target: <100ms)
- Missing @Transaction annotations on 3+ queries

**Solution 1: Remove DATE() from WHERE Clauses**

**File:** `data/local/dao/InvoiceDaoV2.kt:141-154`

```kotlin
// BEFORE (BAD):
@Query("""
    SELECT ... 
    WHERE DATE(date/1000, 'unixepoch') >= DATE('now', '-30 days')
""")

// AFTER (GOOD):
@Query("""
    SELECT ... 
    WHERE date >= :startDateMillis
""")
fun observeLast30DaysRevenueTrend(
    businessId: Long,
    startDateMillis: Long = System.currentTimeMillis() - (30 * 86400000L)
): Flow<List<RevenueTrendPoint>>
```

**Solution 2: Add @Transaction Annotations**

```kotlin
// Add @Transaction to aggregate queries
@Transaction
@Query("""
    SELECT status, COUNT(*) AS count
    FROM invoices
    WHERE businessProfileId = :businessId
    GROUP BY status
""")
fun observeInvoiceCountByStatus(businessId: Long): Flow<List<InvoiceStatusCountV2>>
```

**Checklist:**
- [ ] Refactor `observeLast30DaysRevenueTrend()` (line 141)
- [ ] Refactor `observeAverageDaysToPayment()` (line 259)
- [ ] Refactor `observeHighRiskInvoiceCount()` (line 222)
- [ ] Refactor `observeAtRiskInvoiceCount()` (line 231)
- [ ] Add @Transaction to `observeInvoiceCountByStatus()` (line 188)
- [ ] Add @Transaction to `observeLast30DaysRevenueTrend()` (line 141)
- [ ] Add @Transaction to `observeAverageDaysToPayment()` (line 259)
- [ ] Run database tests with large datasets (10,000+ invoices)
- [ ] Profile query performance before/after

**Success Criteria:**
- Query p99 < 100ms
- All queries use indexed fields
- No full table scans in EXPLAIN QUERY PLAN

---

### Task 3: Add Missing Reporting Tests
**Priority:** HIGH | **Effort:** 8-10 hours | **Impact:** HIGH

**Problem:**
- AdvancedReportingViewModel: 0 tests
- BusinessInsightsViewModel: 0 tests
- ComparativeMetricsViewModelV2: 0 tests

**Solution:**

**File:** `app/src/test/java/com/emul8r/bizap/ui/reporting/AdvancedReportingViewModelTest.kt`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AdvancedReportingViewModelTest {
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var viewModel: AdvancedReportingViewModel
    private val analyticsRepository: AnalyticsRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AdvancedReportingViewModel(analyticsRepository, ...)
    }
    
    @Test
    fun `loadReportData - success returns data`() = runTest {
        // Arrange
        val mockData = listOf(...)
        coEvery { analyticsRepository.getReportData(...) } returns Result.success(mockData)
        
        // Act
        viewModel.loadReportData(...)
        
        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is ReportUiState.Success)
        assertEquals(mockData, (state as ReportUiState.Success).data)
    }
    
    // Add 10+ more tests for edge cases, errors, etc.
}
```

**Checklist:**
- [ ] Create `AdvancedReportingViewModelTest.kt` (15+ tests)
- [ ] Create `BusinessInsightsViewModelTest.kt` (15+ tests)
- [ ] Create `ComparativeMetricsViewModelV2Test.kt` (15+ tests)
- [ ] Test success scenarios
- [ ] Test error scenarios (network failure, empty data)
- [ ] Test loading states
- [ ] Test data transformations
- [ ] Run `./gradlew test` and verify 100% pass rate
- [ ] Check coverage report: `./gradlew jacocoTestReport`

**Success Criteria:**
- 45+ new tests added
- All reporting ViewModels > 80% coverage
- 0 failing tests

---

### Task 4: Remove Placeholder Certificate Pins
**Priority:** HIGH | **Effort:** 1 hour | **Impact:** CRITICAL

**Problem:**
- SecurityConfig.kt has placeholder hashes (AAA..., BBB...)
- If accidentally enabled, will break production

**Solution (Option 1: Remove):**

```kotlin
// Delete file: security/SecurityConfig.kt
// Remove all references in NetworkModule.kt
```

**Solution (Option 2: Fix):**

```bash
# Get real certificate hashes
openssl s_client -connect firebase.googleapis.com:443 < /dev/null 2>&1 | \
    openssl x509 -pubkey -noout | \
    openssl pkey -pubin -outform der | \
    openssl dgst -sha256 -binary | \
    openssl enc -base64

# Replace placeholders with real hashes
```

**Checklist:**
- [ ] Option A: Delete `SecurityConfig.kt` OR
- [ ] Option B: Get real hashes and update SecurityConfig.kt
- [ ] Remove references from `NetworkModule.kt`
- [ ] Test network calls work correctly
- [ ] Verify HTTPS connections successful
- [ ] Document decision in SECURITY.md

**Success Criteria:**
- No placeholder hashes in codebase
- Network calls functional
- No certificate pinning errors in logs

---

## 🟠 **WEEK 2: HIGH PRIORITY FIXES**

### Task 5: Optimize Dashboard Recomposition
**Priority:** HIGH | **Effort:** 3-4 hours | **Impact:** MEDIUM

**Problem:**
- 8+ separate `collectAsStateWithLifecycle()` calls
- Dashboard recomposes on every ViewModel update

**Solution:**

**File:** `ui/gui2/dashboard/DashboardViewModelV2.kt`

```kotlin
// Create single combined UI state
data class DashboardUiStateV2(
    val revenue: RevenueMetrics?,
    val payment: PaymentMetrics?,
    val invoiceCount: Map<InvoiceStatus, Int>,
    val customers: List<Customer>,
    val analyticsVelocity: InvoicingVelocity?,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Combine all flows
val uiState: StateFlow<DashboardUiStateV2> = combine(
    revenueRepository.observeRevenueMetrics(businessId),
    paymentRepository.observePaymentMetrics(businessId),
    invoiceRepository.observeInvoiceCountByStatus(businessId),
    customerRepository.observeAllCustomers(businessId),
    analyticsRepository.observeInvoicingVelocity(businessId)
) { revenue, payment, invoiceCount, customers, velocity ->
    DashboardUiStateV2(
        revenue = revenue,
        payment = payment,
        invoiceCount = invoiceCount.associate { it.status to it.count },
        customers = customers,
        analyticsVelocity = velocity
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DashboardUiStateV2(isLoading = true)
)
```

**Checklist:**
- [ ] Create `DashboardUiStateV2` data class
- [ ] Combine all flows using `combine()`
- [ ] Update `DashboardScreen.kt` to use single state
- [ ] Remove individual `collectAsStateWithLifecycle()` calls
- [ ] Test dashboard loads correctly
- [ ] Profile recomposition count (should drop from 25+ to <10)

**Success Criteria:**
- Single StateFlow in DashboardScreen
- Recomposition count < 10 per state change
- No visual regressions

---

### Task 6: Add distinctUntilChanged()
**Priority:** MEDIUM | **Effort:** 1-2 hours | **Impact:** MEDIUM

**Problem:**
- InvoiceListViewModelV2 and others missing `distinctUntilChanged()`
- UI recomposes even when data unchanged

**Solution:**

**File:** `ui/gui2/invoices/InvoiceListViewModelV2.kt:24-62`

```kotlin
val uiState: StateFlow<InvoiceListUiStateV2> = invoiceRepository
    .getAllInvoicesWithItems()
    .distinctUntilChanged()  // ← ADD THIS
    .map { invoices ->
        // ... filtering logic ...
    }
    .catch { ... }
    .stateIn(...)
```

**Checklist:**
- [ ] Add to `InvoiceListViewModelV2.kt:25`
- [ ] Add to `CustomerListViewModelV2.kt`
- [ ] Add to `PaymentListViewModelV2.kt`
- [ ] Add to any other ViewModels with Flow → StateFlow
- [ ] Test UI doesn't recompose on unchanged data
- [ ] Profile recomposition count

**Success Criteria:**
- All ViewModels have `distinctUntilChanged()`
- Recomposition count reduced by 20-30%

---

### Task 7: Sanitize PII from Logs
**Priority:** MEDIUM | **Effort:** 2-3 hours | **Impact:** MEDIUM

**Problem:**
- Financial amounts logged via Timber
- Invoice IDs and customer references visible

**Solution:**

```kotlin
// BEFORE:
Timber.w("recordPayment: Amount exceeds balance - requested=$amount, remaining=$remaining")

// AFTER:
Timber.w("recordPayment: Amount exceeds outstanding balance")
```

**Checklist:**
- [ ] Search for `Timber.*\$amount` patterns
- [ ] Replace with generic messages
- [ ] Search for customer names/emails in logs
- [ ] Replace with IDs or hash values
- [ ] Add lint rule to prevent PII logging
- [ ] Update logging guidelines in CONTRIBUTING.md

**Success Criteria:**
- No financial amounts in logs
- No customer PII in logs
- Logs still useful for debugging

---

### Task 8: Exclude WAL Files from Backup
**Priority:** MEDIUM | **Effort:** 15 minutes | **Impact:** MEDIUM

**Problem:**
- SQLCipher WAL files not excluded from backup

**Solution:**

**File:** `app/src/main/res/xml/backup_rules.xml`

```xml
<full-backup-content>
    <exclude domain="database" path="bizap.db" />
    <exclude domain="database" path="bizap.db-shm" />  <!-- ADD -->
    <exclude domain="database" path="bizap.db-wal" />  <!-- ADD -->
    <exclude domain="sharedpref" path="." />
</full-backup-content>
```

**Checklist:**
- [ ] Add `-shm` exclusion
- [ ] Add `-wal` exclusion
- [ ] Test backup configuration
- [ ] Verify WAL files not included in backup

**Success Criteria:**
- WAL files excluded from Google Cloud backup
- Main database still excluded

---

## 🟡 **WEEK 3-4: MEDIUM PRIORITY** (Optional)

### Task 9: Add Espresso UI Tests (12-15 hours)
- [ ] Invoice creation flow
- [ ] Payment recording
- [ ] Navigation tests

### Task 10: Test Missing Repositories (6-8 hours)
- [ ] AuthenticationRepositoryImpl
- [ ] DocumentRepositoryImpl
- [ ] 8+ other repositories

### Task 11: Refactor BusinessContextRepositoryV2 (4-6 hours)
- [ ] Add business logic or move to QueryHelper
- [ ] Update documentation

### Task 12: Move API Key to Backend (4-6 hours)
- [ ] Create backend endpoint
- [ ] Remove from BuildConfig
- [ ] Use RemoteConfig

---

## 📊 **PROGRESS TRACKING**

### Week 1 Progress:
- [ ] Task 1: PINStorage (0/6 steps)
- [ ] Task 2: Database Queries (0/9 steps)
- [ ] Task 3: Reporting Tests (0/8 steps)
- [ ] Task 4: Certificate Pins (0/6 steps)

**Total:** 0/29 steps (0%)

### Week 2 Progress:
- [ ] Task 5: Dashboard (0/6 steps)
- [ ] Task 6: distinctUntilChanged (0/6 steps)
- [ ] Task 7: PII Logs (0/6 steps)
- [ ] Task 8: WAL Backup (0/4 steps)

**Total:** 0/22 steps (0%)

---

## 🎯 **SUCCESS METRICS**

### After Week 1:
- [ ] PINStorage main thread time: 0ms
- [ ] Database query p99: <100ms
- [ ] Test coverage (reporting): 80%+
- [ ] Certificate pinning: Fixed/removed

### After Week 2:
- [ ] Dashboard recomposition: <10
- [ ] distinctUntilChanged coverage: 100%
- [ ] PII in logs: 0 instances
- [ ] WAL backup exclusion: Verified

**Overall Production Readiness:** 95% ✅

---

**Last Updated:** April 9, 2026  
**Next Review:** After Week 1 completion
