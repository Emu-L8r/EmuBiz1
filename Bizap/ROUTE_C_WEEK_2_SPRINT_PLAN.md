# 🚀 ROUTE C WEEK 2 - ADVANCED ANALYTICS IMPLEMENTATION

**Start Date:** March 27, 2026 (Continuing immediately)  
**Duration:** ~12-16 hours  
**Goal:** Complete analytics system with real data + advanced reports  
**Status:** ✅ READY TO START

---

## 📊 WEEK 2 OVERVIEW

Build on the solid Week 1 foundation to implement:
1. Event logging integration (real data capture)
2. Real metric calculations (replace mock data)
3. Revenue analytics report (visualizations)
4. Payment analytics report (visualizations)

---

## 🎯 PHASE 2A: EVENT LOGGING INTEGRATION (Days 1-3)

### **Objective:** Wire ViewModels to log events to database

#### **Task 1: Create AnalyticsRepositoryImpl** (2-3 hours)

**File to Create:** `AnalyticsRepositoryImpl.kt`

```kotlin
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsEventDao: AnalyticsEventDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnalyticsRepository {
    
    override suspend fun logEvent(event: InvoiceAnalyticsEvent): Result<Unit> =
        runCatching {
            val eventEntity = AnalyticsEventEntity(
                businessId = event.businessId,
                eventType = event::class.simpleName ?: "Unknown",
                eventData = event.toJson(),  // Serialize event
                timestamp = event.timestamp,
                createdAt = System.currentTimeMillis()
            )
            analyticsEventDao.insertEvent(eventEntity)
        }
    
    override suspend fun getEventCount(
        businessId: Long,
        eventType: String,
        sinceMs: Long
    ): Result<Int> = runCatching {
        analyticsEventDao.getEventCountByType(businessId, eventType, sinceMs)
    }
    
    // ... other methods ...
}
```

#### **Task 2: Create AnalyticsEventDao** (1-1.5 hours)

**File to Create:** `AnalyticsEventDao.kt`

```kotlin
@Dao
interface AnalyticsEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AnalyticsEventEntity)
    
    @Query("""
        SELECT COUNT(*) FROM analytics_events 
        WHERE business_id = :businessId 
        AND event_type = :eventType 
        AND timestamp >= :sinceMs
    """)
    suspend fun getEventCountByType(
        businessId: Long,
        eventType: String,
        sinceMs: Long
    ): Int
    
    @Query("""
        SELECT * FROM analytics_events 
        WHERE business_id = :businessId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun observeRecentEvents(businessId: Long, limit: Int = 1000): Flow<List<AnalyticsEventEntity>>
}
```

#### **Task 3: Create AnalyticsEventEntity** (30 min)

**File to Create:** `AnalyticsEventEntity.kt`

```kotlin
@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "business_id")
    val businessId: Long,
    @ColumnInfo(name = "event_type")
    val eventType: String,
    @ColumnInfo(name = "event_data")
    val eventData: String,  // JSON serialized
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
```

#### **Task 4: Wire ViewModels to Log Events** (1-1.5 hours)

**Files to Update:**
- `InvoiceDetailViewModel.kt` - Log status changes, payments
- `CustomerDetailViewModel.kt` - Log customer views
- `DashboardViewModelV2.kt` - Log dashboard views

**Example Integration:**

```kotlin
// In InvoiceDetailViewModel
private fun updateInvoiceStatus(invoiceId: Long, newStatus: InvoiceStatus) {
    viewModelScope.launch {
        invoiceRepository.updateInvoiceStatus(invoiceId, newStatus)
            .onSuccess {
                // Log event
                analyticsRepository.logEvent(
                    InvoiceAnalyticsEvent.StatusChanged(
                        businessId = businessId,
                        invoiceId = invoiceId,
                        newStatus = newStatus.name
                    )
                )
            }
    }
}
```

---

## 🎯 PHASE 2B: REAL METRIC CALCULATIONS (Days 4-5)

### **Objective:** Replace mock data with real database queries

#### **Task 1: Wire getDashboardMetrics() to Real Data** (1.5-2 hours)

**File to Update:** `InvoiceRepositoryImpl.kt`

Replace the mock implementation with real queries:

```kotlin
override suspend fun getDashboardMetrics(businessId: Long): Result<DashboardMetrics> =
    runCatching {
        val now = System.currentTimeMillis()
        val monthStartMs = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        
        // Query real data from database
        val allInvoices = invoiceDao.getAllInvoicesByBusinessId(businessId)
        
        val unpaidInvoices = allInvoices.filter { 
            it.amountPaid < it.totalAmount 
        }
        val unpaidCount = unpaidInvoices.size
        val unpaidTotal = unpaidInvoices.sumOf { it.totalAmount - it.amountPaid }
        
        val overdueTotal = unpaidInvoices
            .filter { it.dueDate < now && (it.totalAmount - it.amountPaid) > 0 }
            .sumOf { it.totalAmount - it.amountPaid }
        
        val paidThisMonth = allInvoices
            .filter { it.amountPaid > 0 && it.updatedAt >= monthStartMs }
            .sumOf { it.amountPaid }
        
        DashboardMetrics(
            unpaidInvoiceCount = unpaidCount,
            unpaidAmount = unpaidTotal,
            overdueAmount = overdueTotal,
            paidThisMonth = paidThisMonth,
            totalCustomersOwed = unpaidTotal,
            lastUpdatedMs = now
        )
    }
```

#### **Task 2: Update Dashboard to Use Real Data** (1-1.5 hours)

**File to Update:** `DashboardScreenV2.kt`

```kotlin
// Replace mock metrics with real data from repository
val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle(
    initialValue = DashboardMetrics(0, 0, 0, 0, 0)
)

DashboardMetricsWidget(
    metrics = dashboardMetrics,  // Now real data!
    onUnpaidClick = { onNavigateToPayment() },
    onOverdueClick = { onNavigateToPayment() },
    onPaidClick = { onNavigateToRevenue() }
)
```

---

## 🎯 PHASE 2C: REVENUE ANALYTICS REPORT (Days 5-7)

### **Objective:** Build visual revenue trends with real data

#### **Task 1: Create Revenue Report ViewModel** (1.5-2 hours)

**File to Create:** `RevenueAnalyticsViewModel.kt`

```kotlin
@HiltViewModel
class RevenueAnalyticsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val businessId = savedStateHandle.get<Long>("businessId") ?: 0L
    
    val revenueData = invoiceRepository.getRevenueAnalytics(businessId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RevenueAnalytics()
        )
    
    val selectedPeriod = MutableStateFlow<TimePeriod>(TimePeriod.MONTHLY)
}

data class RevenueAnalytics(
    val dailyRevenue: List<DailyRevenue> = emptyList(),
    val weeklyRevenue: List<WeeklyRevenue> = emptyList(),
    val monthlyRevenue: List<MonthlyRevenue> = emptyList(),
    val totalRevenue: Long = 0,
    val averageDaily: Long = 0,
    val trend: Float = 0f  // % change
)
```

#### **Task 2: Create Revenue Chart Composable** (2-2.5 hours)

**File to Create:** `RevenueChartCard.kt`

```kotlin
@Composable
fun RevenueChartCard(
    data: RevenueAnalytics,
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Revenue Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Summary metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetric(
                    label = "Total Revenue",
                    value = CentsFormatter.formatCents(data.totalRevenue),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    label = "Daily Average",
                    value = CentsFormatter.formatCents(data.averageDaily),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Line chart (using Vico or similar)
            // TODO: Wire to actual chart library
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Revenue chart will render here")  // Placeholder
            }
        }
    }
}
```

---

## 🎯 PHASE 2D: PAYMENT ANALYTICS REPORT (Days 7-10)

### **Objective:** Build payment status visualization + metrics

#### **Task 1: Create Payment Analytics ViewModel** (1.5-2 hours)

**File to Create:** `PaymentAnalyticsViewModel.kt`

```kotlin
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {
    
    val paymentMetrics = invoiceRepository.getPaymentAnalytics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaymentMetrics()
        )
}

data class PaymentMetrics(
    val totalPayments: Int = 0,
    val collectionRate: Float = 0f,  // %
    val averagePaymentDays: Int = 0,
    val daysOutstanding: Int = 0,  // DSO
    val paymentsByStatus: Map<String, Int> = emptyMap(),
    val trendingUp: Boolean = true
)
```

#### **Task 2: Create Payment Status Chart** (2-2.5 hours)

**File to Create:** `PaymentStatusChartCard.kt`

```kotlin
@Composable
fun PaymentStatusChartCard(
    metrics: PaymentMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Payment Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Key metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBadge(
                    label = "Collection Rate",
                    value = "${metrics.collectionRate.toInt()}%",
                    color = BizapColors.AnalyticsExcellent,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "DSO",
                    value = "${metrics.daysOutstanding} days",
                    color = BizapColors.AnalyticsWarning,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Status breakdown pie chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("Payment status chart will render here")  // Placeholder
            }
        }
    }
}
```

---

## 📋 WEEK 2 IMPLEMENTATION CHECKLIST

### **Phase 2A: Event Logging** (Days 1-3)

- [ ] Create AnalyticsRepositoryImpl
- [ ] Create AnalyticsEventDao
- [ ] Create AnalyticsEventEntity
- [ ] Wire InvoiceDetailViewModel to log events
- [ ] Wire CustomerDetailViewModel to log events
- [ ] Wire DashboardViewModel to log views
- [ ] Test event insertion
- [ ] Verify database persistence

### **Phase 2B: Real Data** (Days 4-5)

- [ ] Update getDashboardMetrics() with real queries
- [ ] Add new DAO query methods
- [ ] Update DashboardScreenV2 to use real data
- [ ] Test with real database data
- [ ] Verify accuracy of calculations

### **Phase 2C: Revenue Reports** (Days 5-7)

- [ ] Create RevenueAnalyticsViewModel
- [ ] Add revenue query to InvoiceRepository
- [ ] Create RevenueChartCard composable
- [ ] Create Revenue Analytics Screen
- [ ] Integrate chart library (Vico)
- [ ] Test with real data
- [ ] Polish UI/animations

### **Phase 2D: Payment Reports** (Days 7-10)

- [ ] Create PaymentAnalyticsViewModel
- [ ] Add payment queries to InvoiceRepository
- [ ] Create PaymentStatusChartCard composable
- [ ] Create Payment Analytics Screen
- [ ] Implement payment status visualization
- [ ] Add collection rate metrics
- [ ] Test with real data
- [ ] Polish UI/animations

---

## 🎯 SUCCESS CRITERIA - WEEK 2

| Item | Success | Notes |
|------|---------|-------|
| Events logged | ✅ | Real data captured |
| Dashboard real data | ✅ | No more mock data |
| Revenue report | ✅ | Charts + trends |
| Payment report | ✅ | Status + metrics |
| Build passing | ✅ | 0 errors |
| Features tested | ✅ | Manual + visual |
| Performance | ✅ | 60fps smooth |

---

## 📊 ESTIMATED TIMELINE

```
Day 1: Phase 2A (Events) - Implementation
Day 2: Phase 2A (Events) - Testing + Integration
Day 3: Phase 2B (Real Data) - Wiring
Day 4: Phase 2B (Real Data) - Testing
Day 5: Phase 2C (Revenue) - ViewModel + Chart
Day 6: Phase 2C (Revenue) - Polish + Integration
Day 7: Phase 2D (Payment) - ViewModel + Chart
Day 8: Phase 2D (Payment) - Polish + Integration
Day 9-10: Testing + Polish + Documentation

Total: ~12-16 hours
```

---

## 🚀 READY TO START?

All Phase 2A tasks are ready to implement. Starting with event logging integration immediately.

**Next action:** Begin Phase 2A - AnalyticsRepositoryImpl implementation.

