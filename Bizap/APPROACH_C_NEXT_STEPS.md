# 🚀 Next Steps: Vico Chart Integration & Real Data

**Status:** Code skeleton complete. Ready for visual implementations.

---

## Phase 3: Vico Chart Integration (4-6 hours)

### 1️⃣ Implement LineChartCard with Real Vico

**File to update:** `app/src/main/kotlin/com/emul8r/bizap/ui/analytics/components/LineChartCard.kt`

Replace the placeholder with:

```kotlin
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun LineChartCard(
    data: List<ChartDataPoint>,
    title: String,
    modifier: Modifier = Modifier,
    onDataPointClick: (ChartDataPoint) -> Unit = {}
) {
    Card(...) {
        if (data.isNotEmpty()) {
            // Create chart model from data
            val chartModelProducer = CartesianChartModelProducer()
            
            LaunchedEffect(data) {
                chartModelProducer.setEntries(
                    listOf(
                        lineSeries {
                            series(data.map { it.value })
                        }
                    )
                )
            }
            
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLine(
                        shader = rememberDynamicShaders()
                    ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis()
                ),
                modelProducer = chartModelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
```

**Reference:** [Vico Documentation](https://patrykandpatrick.github.io/vico/)

---

### 2️⃣ Create BarChartCard Component

**New file:** `app/src/main/kotlin/com/emul8r/bizap/ui/analytics/components/BarChartCard.kt`

```kotlin
package com.emul8r.bizap.ui.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumn
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.emul8r.bizap.domain.analytics.ChartDataPoint

/**
 * Bar chart card using Vico library.
 * 
 * @param data List of chart data points
 * @param title Display title
 * @param modifier Optional modifier
 */
@Composable
fun BarChartCard(
    data: List<ChartDataPoint>,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 300.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (data.isNotEmpty()) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumn()
                    ),
                    // TODO: Wire modelProducer with data
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            } else {
                Text("No data available")
            }
        }
    }
}
```

---

### 3️⃣ Integrate Charts into Tab Screens

**Update:** `PaymentAnalyticsTab.kt`

After the Outstanding Amount card, add:

```kotlin
// Aging breakdown bar chart
item {
    BarChartCard(
        data = listOf(
            ChartDataPoint("Current", analytics.outstandingByAging.current.toFloat(), System.currentTimeMillis()),
            ChartDataPoint("30d", analytics.outstandingByAging.past30.toFloat(), System.currentTimeMillis()),
            ChartDataPoint("60d", analytics.outstandingByAging.past60.toFloat(), System.currentTimeMillis()),
            ChartDataPoint("90d", analytics.outstandingByAging.past90.toFloat(), System.currentTimeMillis()),
            ChartDataPoint("90+", analytics.outstandingByAging.ninetyPlus.toFloat(), System.currentTimeMillis())
        ),
        title = "Outstanding by Aging"
    )
}
```

---

## Phase 4: Wire Real Data (6-8 hours)

### 4️⃣ Create GetRevenueAnalyticsTrendUseCase

**New file:** `app/src/main/java/com/emul8r/bizap/domain/invoice/usecase/GetRevenueAnalyticsTrendUseCase.kt`

```kotlin
package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.analytics.ChartDataPoint
import com.emul8r.bizap.domain.analytics.TrendMetric
import com.emul8r.bizap.domain.invoice.repository.RevenueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class RevenueTrendData(
    val mtdRevenue: TrendMetric,
    val ytdRevenue: TrendMetric,
    val dailyTrend: List<ChartDataPoint>,
    val revenueByStatus: Map<String, Double>,
    val topInvoices: List<Pair<String, Double>>
)

class GetRevenueAnalyticsTrendUseCase @Inject constructor(
    private val repository: RevenueRepository
) {
    operator fun invoke(businessId: Long): Flow<RevenueTrendData> {
        return repository.getRevenueTrend(businessId)
    }
}
```

### 5️⃣ Create RevenueRepository Interface

**New file:** `app/src/main/java/com/emul8r/bizap/domain/invoice/repository/RevenueRepository.kt`

```kotlin
package com.emul8r.bizap.domain.invoice.repository

import com.emul8r.bizap.domain.analytics.ChartDataPoint
import com.emul8r.bizap.domain.analytics.TrendMetric
import kotlinx.coroutines.flow.Flow

interface RevenueRepository {
    fun getRevenueTrend(businessId: Long): Flow<RevenueTrendData>
    
    suspend fun getMTDRevenue(businessId: Long): TrendMetric
    suspend fun getYTDRevenue(businessId: Long): TrendMetric
    suspend fun getDailyTrend(businessId: Long, days: Int): List<ChartDataPoint>
    suspend fun getRevenueByStatus(businessId: Long): Map<String, Double>
    suspend fun getTopInvoices(businessId: Long, limit: Int = 10): List<Pair<String, Double>>
}
```

### 6️⃣ Implement RevenueRepositoryImpl

**New file:** `app/src/main/java/com/emul8r/bizap/data/repository/RevenueRepositoryImpl.kt`

Query existing invoice data and calculate:
- MTD: Sum invoices where date >= month start
- YTD: Sum invoices where date >= year start
- Daily trend: Group by date, count/sum
- By status: Group by invoice status
- Top invoices: Order by amount, limit 10

---

### 7️⃣ Update RevenueAnalyticsTabViewModel

Replace TODO with:

```kotlin
@Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository,
    private val getRevenueAnalyticsTrendUseCase: GetRevenueAnalyticsTrendUseCase
) : ViewModel()

val state: StateFlow<RevenueAnalyticsTabUiState> = _dateRange
    .flatMapLatest { range ->
        businessProfileRepository.activeProfile
            .flatMapLatest { profile ->
                getRevenueAnalyticsTrendUseCase(profile.id)
                    .map { trend ->
                        RevenueAnalyticsTabUiState(
                            mtdRevenue = trend.mtdRevenue,
                            ytdRevenue = trend.ytdRevenue,
                            dailyTrendData = trend.dailyTrend,
                            revenueByStatus = trend.revenueByStatus,
                            topInvoices = trend.topInvoices,
                            isLoading = false
                        )
                    }
                    .catch { error ->
                        emit(RevenueAnalyticsTabUiState(error = error.message))
                    }
            }
    }
    .stateIn(...)
```

---

## Phase 5: Testing & Polish (4-6 hours)

### 8️⃣ Manual Testing Checklist

- [ ] Run app and navigate to Analytics Insights
- [ ] All 4 tabs render without crashing
- [ ] Tab switching is smooth
- [ ] Date range filters update data
- [ ] Charts display with real data
- [ ] Drill-down bottom sheets work
- [ ] Loading/error states show properly
- [ ] Responsive on 6" and 10" screens

### 9️⃣ Unit Tests to Write

```kotlin
// Test TrendMetric deltas
@Test
fun `TrendMetric calculates correct delta percent`() {
    val metric = TrendMetric("Test", 100.0, 80.0, "$")
    assert(metric.deltaPercent == 25.0)
    assert(metric.trendDirection == TrendDirection.UP)
}

// Test ViewModel state transitions
@Test
fun `AnalyticsFocusedInsightsViewModel updates tab index`() {
    viewModel.setTabIndex(2)
    assertThat(viewModel.uiState.first().selectedTabIndex).isEqualTo(2)
}

// Test tab VM date range filtering
@Test
fun `RevenueAnalyticsTabViewModel reloads data on date range change`() {
    viewModel.setDateRange(AnalyticsDateRange.NINETY_DAYS)
    // Verify repository.getRevenueTrend called with correct params
}
```

---

## 🔗 Integration Points

### From Dashboard
```kotlin
Button(
    onClick = { navController.navigateToAnalyticsFocusedInsights(businessId) }
) {
    Icon(Icons.Default.InsightsIcon, ...)
    Text("Analytics Dashboard")
}
```

### From Settings Hub
```kotlin
// In SettingsHubScreenV2.kt
OutlinedButton(
    onClick = { navController.navigateToAnalyticsFocusedInsights(businessId) }
) {
    Icon(Icons.Default.AnalyticsIcon, ...)
    Text("View Analytics Insights")
}
```

---

## 📚 References

1. **Vico Docs:** https://patrykandpatrick.github.io/vico/
2. **Vico Examples:** https://github.com/patrykandpatrick/vico/tree/master/sample
3. **Material 3 Charts:** https://m3.material.io/components/charts/overview
4. **Your existing patterns:**
   - `PaymentAnalyticsScreen.kt` - for state management
   - `InvoiceDetailScreenV2.kt` - for UI patterns
   - `BusinessProfileRepository.kt` - for reactive queries

---

## ⏱️ Effort Estimate

| Task | Hours | Priority |
|------|-------|----------|
| Vico LineChartCard | 3 | High |
| BarChartCard | 2 | High |
| RevenueRepository | 4 | High |
| GetRevenueAnalyticsTrendUseCase | 2 | High |
| Wire Revenue ViewModel | 2 | High |
| Testing & Bugs | 4 | High |
| **Total** | **17** | |

**Estimated Completion:** 2-3 more working days

---

## 🎯 Success Criteria

✅ All charts render with real data  
✅ Date range filtering works across all tabs  
✅ Drill-down bottom sheets display detailed breakdowns  
✅ Performance: < 1s load time, < 500ms tab switch  
✅ Unit tests: 80%+ code coverage  
✅ Manual testing: All scenarios pass  

---

## 💡 Pro Tips

1. **Test Vico locally first** - Create a simple test composable before integrating
2. **Use mock data for UI polish** - Keep mocks while implementing repositories
3. **Accessibility** - Add contentDescriptions to chart components
4. **Performance** - Monitor Compose recomposition with Baseline Profiler
5. **Error boundaries** - Wrap chart rendering in try/catch for production

Good luck! 🚀

