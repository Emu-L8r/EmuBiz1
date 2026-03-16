# 🚀 Analytics Implementation Guide
## Step-by-Step Code Templates for Dashboard Enhancement

**Date:** March 16, 2026  
**Phase:** Week 1 (Cash Flow Trend + Days to Pay)  
**Effort:** 2-3 days (one developer)  

---

## Part 1: Data Layer Enhancement

### Step 1.1: Create Data Models

Create `app/src/main/java/com/emul8r/bizap/domain/model/analytics/`:

#### `TopCustomerMetric.kt`
```kotlin
package com.emul8r.bizap.domain.model.analytics

/**
 * Top customer by revenue with percentage breakdown.
 * Used in RevenueConcentrationChart.
 */
data class TopCustomerMetric(
    val customerId: Long,
    val customerName: String,
    val revenueCents: Long,           // Total revenue from this customer
    val percentageOfTotal: Double,    // As percentage of total business revenue
    val invoiceCount: Int
) {
    val revenueFormatted: String
        get() = "\$${revenueCents / 100}.${String.format("%02d", revenueCents % 100)}"
    
    val percentageFormatted: String
        get() = String.format("%.1f%%", percentageOfTotal)
}
```

#### `DaysToPayMetric.kt`
```kotlin
package com.emul8r.bizap.domain.model.analytics

import java.time.LocalDate

/**
 * Historical Days Sales Outstanding (DSO) data point.
 * Used to show trends in payment collection speed.
 */
data class DaysToPayMetric(
    val date: LocalDate,           // Month/week this DSO was calculated
    val averageDaysToPayment: Double // How many days from SENT to PAID
) {
    val averageFormatted: String
        get() = String.format("%.1f days", averageDaysToPayment)
}
```

#### `CashFlowTrendData.kt`
```kotlin
package com.emul8r.bizap.domain.model.analytics

/**
 * Complete cash flow picture for a single day.
 * Shows what was invoiced vs. what was actually paid.
 */
data class CashFlowTrendData(
    val date: String,                    // "2026-03-16"
    val invoicedCents: Long,            // Amount invoiced this day
    val paidCents: Long,                // Amount paid this day
    val invoiceCount: Int,
    val paidInvoiceCount: Int
) {
    val invoicedFormatted: String
        get() = "\$${invoicedCents / 100}"
    
    val paidFormatted: String
        get() = "\$${paidCents / 100}"
}
```

#### `AnalyticsData.kt`
```kotlin
package com.emul8r.bizap.domain.model.analytics

import com.emul8r.bizap.domain.model.gui2.DailyRevenueTrendV2

/**
 * Aggregated analytics for dashboard.
 * Single source of truth for all advanced metrics.
 */
data class AnalyticsData(
    val cashFlowTrend: List<CashFlowTrendData>,
    val averageDaysToPayTrend: List<DaysToPayMetric>,
    val topCustomerMetrics: List<TopCustomerMetric>,
    val currentAverageDaysToPayment: Double,
    val totalRevenue: Long
)
```

---

### Step 1.2: Add Repository Methods

#### `AnalyticsRepository.kt` (new file)
```kotlin
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.domain.model.analytics.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for analytics aggregation.
 * Combines multiple data sources into actionable insights.
 */
interface AnalyticsRepository {
    
    /**
     * Observe top customers by revenue with percentage breakdown.
     */
    fun observeTopCustomers(
        businessId: Long,
        limit: Int = 5
    ): Flow<List<TopCustomerMetric>>
    
    /**
     * Observe historical Days Sales Outstanding (DSO) trend.
     * Returns last 12 months of average DSO.
     */
    fun observeAverageDaysToPayTrend(
        businessId: Long
    ): Flow<List<DaysToPayMetric>>
    
    /**
     * Observe current average days to payment.
     */
    fun observeCurrentAverageDaysToPayment(
        businessId: Long
    ): Flow<Double>
    
    /**
     * Observe cash flow trend: invoiced vs. paid over 30 days.
     */
    fun observeCashFlowTrend(
        businessId: Long,
        days: Int = 30
    ): Flow<List<CashFlowTrendData>>
    
    /**
     * Observe total revenue (all time).
     */
    fun observeTotalRevenue(businessId: Long): Flow<Long>
}
```

#### `AnalyticsRepositoryImpl.kt`
```kotlin
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.domain.model.analytics.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao
) : AnalyticsRepository {
    
    override fun observeTopCustomers(
        businessId: Long,
        limit: Int
    ): Flow<List<TopCustomerMetric>> {
        return analyticsDao.observeTopCustomersByRevenue(businessId, limit)
            .map { rows ->
                val totalRevenue = rows.sumOf { it.totalRevenue }
                if (totalRevenue == 0L) {
                    emptyList()
                } else {
                    rows.map { row ->
                        TopCustomerMetric(
                            customerId = row.customerId,
                            customerName = row.customerName,
                            revenueCents = row.totalRevenue,
                            percentageOfTotal = (row.totalRevenue.toDouble() / totalRevenue) * 100.0,
                            invoiceCount = row.invoiceCount
                        )
                    }
                }
            }
    }
    
    override fun observeAverageDaysToPayTrend(
        businessId: Long
    ): Flow<List<DaysToPayMetric>> {
        return analyticsDao.observeAverageDaysToPayTrend(businessId)
            .map { rows ->
                Timber.d("AnalyticsRepository: got ${rows.size} DSO trend points")
                rows.map { row ->
                    DaysToPayMetric(
                        date = row.date,
                        averageDaysToPayment = row.averageDaysToPayment
                    )
                }
            }
    }
    
    override fun observeCurrentAverageDaysToPayment(
        businessId: Long
    ): Flow<Double> {
        return analyticsDao.observeCurrentAverageDaysToPayment(businessId)
    }
    
    override fun observeCashFlowTrend(
        businessId: Long,
        days: Int
    ): Flow<List<CashFlowTrendData>> {
        return analyticsDao.observeCashFlowTrend(businessId, days)
            .map { rows ->
                Timber.d("AnalyticsRepository: got ${rows.size} cash flow points")
                rows.map { row ->
                    CashFlowTrendData(
                        date = row.date,
                        invoicedCents = row.invoicedCents,
                        paidCents = row.paidCents,
                        invoiceCount = row.invoiceCount,
                        paidInvoiceCount = row.paidInvoiceCount
                    )
                }
            }
    }
    
    override fun observeTotalRevenue(businessId: Long): Flow<Long> {
        return analyticsDao.observeTotalRevenue(businessId)
    }
}
```

---

### Step 1.3: Add DAO Methods

Add to `AnalyticsDao.kt`:

```kotlin
package com.emul8r.bizap.data.local.dao

import androidx.room.Query
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AnalyticsDao {
    
    // ═════════════════════════════════════════════════════════════════
    // TOP CUSTOMERS BY REVENUE
    // ═════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT 
            c.customerId,
            c.customerName,
            SUM(i.amountPaidCents) as totalRevenue,
            COUNT(i.invoiceId) as invoiceCount
        FROM customers c
        LEFT JOIN invoices i ON c.customerId = i.customerId
        WHERE c.businessProfileId = :businessId
            AND (i.status = 'PAID' OR i.status = 'PARTIALLY_PAID')
            AND i.isActive = 1
        GROUP BY c.customerId, c.customerName
        ORDER BY totalRevenue DESC
        LIMIT :limit
    """)
    fun observeTopCustomersByRevenue(
        businessId: Long,
        limit: Int
    ): Flow<List<TopCustomerRevenueRow>>
    
    data class TopCustomerRevenueRow(
        val customerId: Long,
        val customerName: String,
        val totalRevenue: Long,
        val invoiceCount: Int
    )
    
    // ═════════════════════════════════════════════════════════════════
    // AVERAGE DAYS TO PAY TREND (Last 12 months)
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Returns monthly average DSO (Days Sales Outstanding).
     * Calculates: AVG(PAID_DATE - SENT_DATE) for each month.
     * Only includes invoices that have been PAID.
     */
    @Query("""
        SELECT 
            DATE(i.paidDate) as date,
            CAST(
                AVG(CAST(
                    (julianday(i.paidDate) - julianday(i.sentDate)) AS REAL
                ))
            AS DOUBLE) as averageDaysToPayment
        FROM invoices i
        WHERE i.businessProfileId = :businessId
            AND i.status = 'PAID'
            AND i.paidDate IS NOT NULL
            AND i.sentDate IS NOT NULL
            AND date(i.paidDate) >= date('now', '-1 year')
        GROUP BY strftime('%Y-%m', i.paidDate)
        ORDER BY date DESC
    """)
    fun observeAverageDaysToPayTrend(
        businessId: Long
    ): Flow<List<DaysToPayTrendRow>>
    
    data class DaysToPayTrendRow(
        val date: LocalDate,
        val averageDaysToPayment: Double
    )
    
    /**
     * Current (today) average DSO across all PAID invoices.
     */
    @Query("""
        SELECT COALESCE(
            CAST(
                AVG(CAST(
                    (julianday(i.paidDate) - julianday(i.sentDate)) AS REAL
                ))
            AS DOUBLE),
            0.0
        )
        FROM invoices i
        WHERE i.businessProfileId = :businessId
            AND i.status = 'PAID'
            AND i.paidDate IS NOT NULL
            AND i.sentDate IS NOT NULL
    """)
    fun observeCurrentAverageDaysToPayment(
        businessId: Long
    ): Flow<Double>
    
    // ═════════════════════════════════════════════════════════════════
    // CASH FLOW TREND (Last 30 days)
    // ═════════════════════════════════════════════════════════════════
    
    /**
     * Daily cash flow: what was invoiced vs. what was paid.
     * Helps identify seasonal patterns and predict cash needs.
     */
    @Query("""
        WITH daily_data AS (
            SELECT 
                DATE(i.createdDate) as date,
                SUM(CASE WHEN i.status != 'DRAFT' THEN i.amountInvoicedCents ELSE 0 END) 
                    as invoiced_cents,
                COUNT(CASE WHEN i.status != 'DRAFT' THEN 1 ELSE NULL END) 
                    as invoice_count,
                SUM(CASE WHEN i.status = 'PAID' THEN i.amountPaidCents ELSE 0 END) 
                    as paid_cents,
                COUNT(CASE WHEN i.status = 'PAID' THEN 1 ELSE NULL END) 
                    as paid_count
            FROM invoices i
            WHERE i.businessProfileId = :businessId
                AND i.isActive = 1
                AND date(i.createdDate) >= date('now', '-' || :days || ' days')
            GROUP BY DATE(i.createdDate)
        )
        SELECT * FROM daily_data
        ORDER BY date ASC
    """)
    fun observeCashFlowTrend(
        businessId: Long,
        days: Int = 30
    ): Flow<List<CashFlowTrendRow>>
    
    data class CashFlowTrendRow(
        val date: String,           // "2026-03-16"
        val invoiced_cents: Long,
        val invoice_count: Int,
        val paid_cents: Long,
        val paid_count: Int
    ) {
        val invoicedCents: Long = invoiced_cents
        val paidCents: Long = paid_cents
        val invoiceCount: Int = invoice_count
        val paidInvoiceCount: Int = paid_count
    }
    
    // ═════════════════════════════════════════════════════════════════
    // TOTAL REVENUE
    // ═════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT COALESCE(SUM(amountPaidCents), 0)
        FROM invoices
        WHERE businessProfileId = :businessId
            AND status = 'PAID'
            AND isActive = 1
    """)
    fun observeTotalRevenue(businessId: Long): Flow<Long>
}
```

---

## Part 2: ViewModel

### Step 2.1: Create AnalyticsViewModel

Create `app/src/main/java/com/emul8r/bizap/ui/analytics/AnalyticsViewModel.kt`:

```kotlin
package com.emul8r.bizap.ui.analytics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.AnalyticsRepository
import com.emul8r.bizap.domain.model.analytics.AnalyticsData
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for advanced analytics dashboard.
 * Aggregates all analytics data into a single state.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    
    private val route: ScreenV2.Dashboard = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        analyticsRepository.observeCashFlowTrend(businessId, 30),
        analyticsRepository.observeAverageDaysToPayTrend(businessId),
        analyticsRepository.observeCurrentAverageDaysToPayment(businessId),
        analyticsRepository.observeTopCustomers(businessId, 5),
        analyticsRepository.observeTotalRevenue(businessId)
    ) { cashFlow, daysToPayTrend, currentDSO, topCustomers, totalRevenue ->
        Timber.d(
            "AnalyticsViewModel: state updated | cashFlow=${cashFlow.size} " +
            "| daysToPayTrend=${daysToPayTrend.size} | topCustomers=${topCustomers.size}"
        )
        AnalyticsUiState.Success(
            AnalyticsData(
                cashFlowTrend = cashFlow,
                averageDaysToPayTrend = daysToPayTrend,
                topCustomerMetrics = topCustomers,
                currentAverageDaysToPayment = currentDSO,
                totalRevenue = totalRevenue
            )
        )
    }
        .catch { error ->
            Timber.e(error, "AnalyticsViewModel: error loading state")
            emit(AnalyticsUiState.Error(error.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState.Loading
        )
}

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val data: AnalyticsData) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}
```

---

## Part 3: UI Components

### Step 3.1: CashFlowTrendChart

Create `app/src/main/java/com/emul8r/bizap/ui/dashboard/components/analytics/CashFlowTrendChart.kt`:

```kotlin
package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.analytics.CashFlowTrendData
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/**
 * Cash Flow Trend Chart
 * 
 * Shows invoiced vs. paid over the last 30 days.
 * Helps users identify seasonal patterns and predict cash needs.
 * 
 * Blue line: Invoiced amount
 * Green area: Paid amount
 * 
 * Interactive: Tap to see daily details
 */
@Composable
fun CashFlowTrendChart(
    dailyTrends: List<CashFlowTrendData>,
    modifier: Modifier = Modifier
) {
    val modelProducer = CartesianChartModelProducer()
    
    // Prepare chart data
    val invoicedAmounts = dailyTrends.map { it.invoicedCents / 100.0 }.toFloatArray()
    val paidAmounts = dailyTrends.map { it.paidCents / 100.0 }.toFloatArray()
    
    // Update chart with data
    modelProducer.runTransaction {
        lineSeries {
            series(invoicedAmounts.toList())
            series(paidAmounts.toList())
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Cash Flow Trend (30 Days)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("Invoiced", Color(0xFF1976D2))
            LegendItem("Paid", Color(0xFF388E3C))
        }
        
        // Chart
        CartesianChartHost(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            modelProducer = modelProducer,
            layers = listOf(
                rememberLineCartesianLayer(
                    lines = listOf(
                        androidx.compose.ui.graphics.Color(0xFF1976D2),
                        androidx.compose.ui.graphics.Color(0xFF388E3C)
                    )
                )
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
            isZoomEnabled = true
        )
        
        // Info text
        Text(
            text = "💡 Tip: Compare invoiced vs. paid to identify cash flow gaps. Look for patterns to predict future cash needs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Step 3.2: AverageDaysToPayMetric

Create `app/src/main/java/com/emul8r/bizap/ui/dashboard/components/analytics/AverageDaysToPayMetric.kt`:

```kotlin
package com.emul8r.bizap.ui.dashboard.components.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.analytics.DaysToPayMetric
import kotlin.math.max

/**
 * Average Days to Payment Metric
 * 
 * Shows how long invoices take to get paid on average.
 * Includes a sparkline showing trend over 12 months.
 * 
 * Color coding:
 * - Green: < 15 days (excellent)
 * - Yellow: 15-25 days (normal)
 * - Red: > 25 days (problem — need to improve collections)
 */
@Composable
fun AverageDaysToPayMetric(
    currentDaysToPayment: Double,
    trendHistory: List<DaysToPayMetric>,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        currentDaysToPayment < 15.0 -> Color(0xFF388E3C)  // Green
        currentDaysToPayment < 25.0 -> Color(0xFFF57C00)  // Yellow/Orange
        else -> Color(0xFFD32F2F)  // Red
    }
    
    val statusText = when {
        currentDaysToPayment < 15.0 -> "Excellent"
        currentDaysToPayment < 25.0 -> "Normal"
        else -> "Needs Attention"
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                text = "Average Days to Payment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Main number with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", currentDaysToPayment),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        fontSize = 48.sp
                    )
                    Text(
                        text = "days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Sparkline (simplified — can be enhanced with Vico)
            if (trendHistory.isNotEmpty()) {
                DaysToPaySparkline(trendHistory)
            }
            
            // Help text
            Text(
                text = "⏱️ Days from invoice sent to payment received",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple sparkline showing DSO trend.
 * Uses miniature bars for easy visualization.
 */
@Composable
private fun DaysToPaySparkline(data: List<DaysToPayMetric>) {
    if (data.isEmpty()) return
    
    val maxDays = max(data.maxByOrNull { it.averageDaysToPayment }?.averageDaysToPayment ?: 1.0, 1.0)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.takeLast(12).forEach { metric ->  // Last 12 months
            val heightFraction = (metric.averageDaysToPayment / maxDays).toFloat()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .background(
                        color = Color(0xFF1976D2).copy(alpha = 0.6f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )
        }
    }
    
    Text(
        text = "Last 12 months →",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}
```

---

## Part 4: Dependency Injection

### Step 4.1: Update Hilt Module

Add to your existing Hilt module (e.g., `RepositoryModule.kt`):

```kotlin
// In your existing @Module class
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        analyticsDao: AnalyticsDao
    ): AnalyticsRepository {
        return AnalyticsRepositoryImpl(analyticsDao)
    }
}
```

---

## Part 5: Integration into Dashboard

### Step 5.1: Update GUI1 DashboardScreen

Modify `DashboardScreen.kt`:

```kotlin
@Composable
fun DashboardScreen(
    navController: NavController,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    businessViewModel: BusinessProfileViewModel = hiltViewModel(),
    revenueViewModel: RevenueDashboardViewModel = hiltViewModel(),
    invoiceViewModel: InvoiceListViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel(),
    analyticsViewModel: AnalyticsViewModel = hiltViewModel()  // NEW
) {
    val customers by customerViewModel.uiState.collectAsStateWithLifecycle()
    val activeBusiness by businessViewModel.profileState.collectAsStateWithLifecycle()
    val revenueState by revenueViewModel.uiState.collectAsStateWithLifecycle()
    val invoiceState by invoiceViewModel.uiState.collectAsStateWithLifecycle()
    val currentNotesCount by notesViewModel.currentNotesCount.collectAsStateWithLifecycle()
    val analyticsState by analyticsViewModel.analyticsState.collectAsStateWithLifecycle()  // NEW
    
    var showSwitcher by remember { mutableStateOf(false) }

    // ... existing code ...

    Box(modifier = Modifier.fillMaxSize().subtleVerticalGradient()) {
        ImagePlaceholderBackground(alpha = 0.08f)

        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... existing items (business header, pie chart, notes) ...
            
            // NEW: Analytics Section
            item {
                when (val state = analyticsState) {
                    is AnalyticsUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(32.dp)
                        )
                    }
                    is AnalyticsUiState.Success -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Heading
                            Text(
                                text = "💡 Business Analytics",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            // Days to Pay Metric
                            AverageDaysToPayMetric(
                                currentDaysToPayment = state.data.currentAverageDaysToPayment,
                                trendHistory = state.data.averageDaysToPayTrend
                            )
                            
                            // Cash Flow Trend
                            CashFlowTrendChart(
                                dailyTrends = state.data.cashFlowTrend
                            )
                        }
                    }
                    is AnalyticsUiState.Error -> {
                        Text(
                            text = "Error loading analytics: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // ... rest of existing dashboard items ...
        }
    }
}
```

---

## Testing Checklist

- [ ] Build succeeds without errors
- [ ] DAO queries return correct data
- [ ] Repository flows emit data correctly
- [ ] ViewModel aggregates data properly
- [ ] Charts render without crashes
- [ ] Scrolling performance is smooth
- [ ] Test with 10+ invoices
- [ ] Test with 100+ invoices
- [ ] Test with real dates (not mock data)
- [ ] DSO calculation is correct (spot check manually)
- [ ] Top customers percentages add up to ~100%

---

## Common Issues & Solutions

### Issue: "Cannot find AnalyticsDao"
**Solution:** Make sure `AnalyticsDao` is part of your Room database:

```kotlin
@Database(
    entities = [
        Invoice::class,
        Customer::class,
        BusinessProfile::class,
        // ... others ...
    ],
    views = [
        // If using views for analytics
    ],
    version = X
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analyticsDao(): AnalyticsDao
}
```

### Issue: "Vico chart doesn't render"
**Solution:** Make sure you have the Vico dependency:

```gradle
implementation("com.patrykandpatrick:vico-compose:1.9.0")
implementation("com.patrykandpatrick:vico-compose-m3:1.9.0")
```

### Issue: "DSO calculation gives negative numbers"
**Solution:** Check that `paidDate` and `sentDate` are being set correctly when invoices are created/paid:

```kotlin
// In InvoiceEntity or wherever you set these
paidDate = if (status == PAID) now else null
sentDate = if (status != DRAFT) now else null
```

---

## Next Steps

1. **Implement in order:** Data layer → ViewModel → UI
2. **Test each layer independently** before integrating
3. **Run on real device** with real data (not emulator)
4. **Get user feedback** before polishing
5. **Plan Phase 2** (Revenue Concentration chart) based on learnings

**Ready to code? Start with Step 1.1 (Data Models).**

