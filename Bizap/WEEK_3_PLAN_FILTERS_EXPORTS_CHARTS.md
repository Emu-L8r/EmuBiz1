# 🎯 WEEK 3 IMPLEMENTATION PLAN - Charts, Filters & Exports

**Status:** Ready to Execute  
**Date:** March 28, 2026  
**Estimated Duration:** 6-8 hours  

---

## 📊 WEEK 3 OVERVIEW

### **What's Already Built (From Previous Phases)**
✅ Charts: LineChartCard, BarChartCard, PieChartCard (Vico integration)
✅ Revenue Analytics Screen with real metrics
✅ Payment Analytics Screen with real metrics
✅ Real-time database queries
✅ Professional Material 3 UI throughout

### **What Week 3 Will Add**
🎯 **Advanced Filters** - Date range selection, status filters
🎯 **Exports** - PDF reports, CSV data downloads
🎯 **Charts Enhancement** - Real data integration, drill-downs
🎯 **Polish** - Animations, loading states, error handling

---

## 🔄 PHASE 3A: ADVANCED FILTERS (2-3 hours)

### **3A.1: Date Range Filter Component**

**File:** `ui/gui2/common/DateRangeFilterV2.kt` (NEW)

```kotlin
@Composable
fun DateRangeFilterV2(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick select buttons: Today, This Week, This Month, YTD, Custom
        OutlinedButton(
            onClick = { onRangeSelected(DateRange.TODAY) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Today")
        }
        OutlinedButton(
            onClick = { onRangeSelected(DateRange.THIS_WEEK) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Week")
        }
        OutlinedButton(
            onClick = { onRangeSelected(DateRange.THIS_MONTH) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Month")
        }
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.weight(1f)
        ) {
            Text("Custom")
        }
    }
    
    // Material DatePicker for custom range
    if (showDatePicker) {
        // TODO: Implement Material DateRangePicker
    }
}

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        val TODAY get() = DateRange(LocalDate.now(), LocalDate.now())
        val THIS_WEEK get() {
            val now = LocalDate.now()
            val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
            return DateRange(startOfWeek, now)
        }
        val THIS_MONTH get() {
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1)
            return DateRange(startOfMonth, now)
        }
        val THIS_YEAR get() {
            val now = LocalDate.now()
            val startOfYear = now.withDayOfYear(1)
            return DateRange(startOfYear, now)
        }
    }
}
```

### **3A.2: Status Filter Chips**

**File:** `ui/gui2/common/StatusFilterChipsV2.kt` (NEW)

```kotlin
@Composable
fun StatusFilterChipsV2(
    selectedStatuses: Set<InvoiceStatus>,
    onStatusSelected: (Set<InvoiceStatus>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InvoiceStatus.values().forEach { status ->
            FilterChip(
                selected = status in selectedStatuses,
                onClick = {
                    val updated = selectedStatuses.toMutableSet()
                    if (status in updated) updated.remove(status)
                    else updated.add(status)
                    onStatusSelected(updated)
                },
                label = { Text(status.displayName) },
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}
```

### **3A.3: Wire Filters to Analytics Screens**

Update `RevenueAnalyticsScreenV2.kt`:
```kotlin
@Composable
fun RevenueAnalyticsScreenV2(...) {
    val dateRange = remember { mutableStateOf(DateRange.THIS_MONTH) }
    
    Column {
        DateRangeFilterV2(
            selectedRange = dateRange.value,
            onRangeSelected = { dateRange.value = it }
        )
        
        // Revenue metrics updated based on dateRange
        RevenueAnalyticsContentV2(
            metrics = calculateMetricsForDateRange(dateRange.value)
        )
    }
}
```

---

## 📄 PHASE 3B: PDF EXPORT (2-3 hours)

### **3B.1: Create PDF Export Repository**

**File:** `domain/repository/ExportRepository.kt` (NEW)

```kotlin
interface ExportRepository {
    /**
     * Generate PDF report for given revenue metrics
     */
    suspend fun exportRevenueReport(
        businessId: Long,
        metrics: RevenueMetricsV2,
        dateRange: DateRange
    ): ByteArray
    
    /**
     * Generate PDF report for given payment metrics
     */
    suspend fun exportPaymentReport(
        businessId: Long,
        metrics: PaymentMetricsV2,
        dateRange: DateRange
    ): ByteArray
}
```

### **3B.2: Implement PDF Export**

**File:** `data/repository/ExportRepositoryImpl.kt` (NEW)

```kotlin
class ExportRepositoryImpl(
    private val context: Context,
    private val pdfGenerationService: PdfGenerationService
) : ExportRepository {
    
    override suspend fun exportRevenueReport(
        businessId: Long,
        metrics: RevenueMetricsV2,
        dateRange: DateRange
    ): ByteArray = withContext(Dispatchers.Default) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Draw header
        drawText(canvas, "Revenue Report", 50f, 50f, 24f)
        drawText(canvas, "Period: ${dateRange.startDate} - ${dateRange.endDate}", 50f, 80f, 14f)
        
        // Draw metrics
        drawText(canvas, "MTD Revenue: ${formatCents(metrics.mtdRevenue)}", 50f, 130f, 14f)
        drawText(canvas, "YTD Revenue: ${formatCents(metrics.ytdRevenue)}", 50f, 160f, 14f)
        drawText(canvas, "Weekly Revenue: ${formatCents(metrics.weeklyRevenue)}", 50f, 190f, 14f)
        
        document.finishPage(page)
        
        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()
        
        outputStream.toByteArray()
    }
    
    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        fontSize: Float
    ) {
        val paint = Paint().apply {
            textSize = fontSize
            color = Color.BLACK
        }
        canvas.drawText(text, x, y, paint)
    }
}
```

### **3B.3: Add Export Buttons to Screens**

Update `RevenueAnalyticsScreenV2.kt`:
```kotlin
@Composable
fun RevenueAnalyticsScreenV2(...) {
    val viewModel: RevenueAnalyticsViewModelV2 = hiltViewModel()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revenue Analytics") },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportToPDF() }
                    ) {
                        Icon(Icons.Default.FileDownload, "Export PDF")
                    }
                }
            )
        }
    ) { ... }
}
```

---

## 📊 PHASE 3C: REAL DATA IN CHARTS (1-2 hours)

### **3C.1: Wire Real Data to LineChartCard**

Update `RevenueAnalyticsScreenV2.kt`:
```kotlin
// Instead of mock data, use real data from ViewModel
val dailyRevenueTrend = remember { mutableStateOf<List<ChartDataPoint>>(emptyList()) }

LaunchedEffect(metrics) {
    dailyRevenueTrend.value = metrics.dailyTrendData.map { trend ->
        ChartDataPoint(
            label = trend.date.dayOfMonth.toString(),
            value = trend.revenue.toFloat()
        )
    }
}

LineChartCard(
    data = dailyRevenueTrend.value,
    title = "Daily Revenue Trend"
)
```

### **3C.2: Add Drill-Down Functionality**

```kotlin
LineChartCard(
    data = dailyRevenueTrend.value,
    title = "Daily Revenue Trend",
    onDataPointClick = { point ->
        // Show bottom sheet with details for selected day
        showDayDetails(point)
    }
)
```

---

## 🎯 PHASE 3D: POLISH & REFINEMENT (1-2 hours)

### **3D.1: Add Loading States**

```kotlin
@Composable
fun RevenueAnalyticsScreenV2(...) {
    val isExporting by viewModel.isExporting.collectAsStateWithLifecycle()
    
    if (isExporting) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}
```

### **3D.2: Add Success/Error Notifications**

```kotlin
val exportMessage by viewModel.exportMessage.collectAsStateWithLifecycle()

LaunchedEffect(exportMessage) {
    if (exportMessage.isNotEmpty()) {
        snackbarHostState.showSnackbar(exportMessage)
    }
}
```

---

## 📋 IMPLEMENTATION CHECKLIST

### **Phase 3A: Filters**
- [ ] Create DateRangeFilterV2 component
- [ ] Create StatusFilterChipsV2 component
- [ ] Wire filters to RevenueAnalyticsScreenV2
- [ ] Wire filters to PaymentAnalyticsScreenV2
- [ ] Update repository queries to accept date ranges
- [ ] Test with different date ranges
- [ ] Build & verify

### **Phase 3B: Exports**
- [ ] Create ExportRepository interface
- [ ] Implement ExportRepositoryImpl (PDF)
- [ ] Add export buttons to screens
- [ ] Implement CSV export option
- [ ] Add export view model methods
- [ ] Test PDF generation
- [ ] Test CSV generation
- [ ] Build & verify

### **Phase 3C: Chart Data**
- [ ] Wire real data to LineChartCard
- [ ] Wire real data to BarChartCard
- [ ] Wire real data to PieChartCard
- [ ] Add drill-down on chart clicks
- [ ] Test with various data sets
- [ ] Build & verify

### **Phase 3D: Polish**
- [ ] Add loading indicators for exports
- [ ] Add success/error messages
- [ ] Add animations to filters
- [ ] Test error handling
- [ ] Final UI polish
- [ ] Build & verify final

---

## 🚀 EXECUTION STRATEGY

**Recommended Order:**
1. ✅ **Phase 3A (Filters)** - Quick win, enhances existing screens (1-2 hours)
2. ✅ **Phase 3B (Exports)** - High value feature (2-3 hours)
3. ✅ **Phase 3C (Chart Data)** - Polish with real data (1-2 hours)
4. ✅ **Phase 3D (Polish)** - Final refinements (1-2 hours)

**Total Time:** 5-9 hours

---

## 💾 FILES TO CREATE/MODIFY

### **New Files (4)**
- `ui/gui2/common/DateRangeFilterV2.kt`
- `ui/gui2/common/StatusFilterChipsV2.kt`
- `domain/repository/ExportRepository.kt`
- `data/repository/ExportRepositoryImpl.kt`

### **Files to Modify (4)**
- `RevenueAnalyticsScreenV2.kt` - Add filters & export
- `PaymentAnalyticsScreenV2.kt` - Add filters & export
- `RevenueAnalyticsViewModelV2.kt` - Add export methods
- `PaymentAnalyticsViewModelV2.kt` - Add export methods

---

## ✨ EXPECTED OUTCOMES

After Week 3:
✅ Users can filter analytics by date range
✅ Users can filter by status/type
✅ Users can export reports as PDF
✅ Users can export data as CSV
✅ Charts show real data (not mock)
✅ Drill-down functionality for details
✅ Professional loading/error states
✅ Full feature parity with enterprise apps

---

**Status:** 🎯 Ready to Execute  
**Confidence:** ⭐⭐⭐⭐⭐ (All prerequisites built)  
**Start Time:** Immediately

