# 🎯 TASK 12: REVENUE DASHBOARD - IMPLEMENTATION GUIDE

**Status:** Ready to Start  
**Duration:** 8 hours (Week 2)  
**Pathway:** 4 (Analytics & Business Intelligence)  
**Difficulty:** Medium  
**Dependencies:** ✅ Task 11 (Analytics DB Layer)

---

## 📊 OBJECTIVE

Build a professional revenue dashboard screen that displays:
- Total revenue (YTD, MTD, this week)
- Revenue trends (interactive line chart)
- Revenue by currency (breakdown)
- Top invoices (recent high-value)

**Expected Result:**  
A beautiful, responsive dashboard that updates in real-time as invoices are created/paid.

---

## 🎨 UI DESIGN

```
┌─────────────────────────────────────────┐
│ Dashboard                          2026 │
├─────────────────────────────────────────┤
│                                         │
│  TOTAL REVENUE THIS MONTH               │
│  $47,250.00 AUD                         │
│  ↑ 12.5% from last month                │
│                                         │
├─────────────────────────────────────────┤
│ REVENUE BREAKDOWN                       │
│                                         │
│  AUD: $32,500.00  (68.8%)               │
│  USD: $12,750.00  (27.0%)               │
│  EUR: $2,000.00   (4.2%)                │
│                                         │
├─────────────────────────────────────────┤
│ REVENUE TREND (Last 30 Days)            │
│                                         │
│    ^                                    │
│    |     ╱╲                             │
│ $  |    ╱  ╲  ╱╲                        │
│ 50k|   ╱    ╲╱  ╲    ╱╲                 │
│    |  ╱           ╲  ╱  ╲               │
│ 25k| ╱             ╲╱    ╲              │
│    |╱                     ╲             │
│    └─────────────────────────          │
│      1d  7d  14d  21d  30d              │
│                                         │
├─────────────────────────────────────────┤
│ TOP INVOICES (Last 30 Days)             │
│                                         │
│ 1. INV-2026-00042  $8,500.00  PAID     │
│ 2. INV-2026-00041  $6,250.00  SENT     │
│ 3. INV-2026-00040  $5,100.00  DRAFT    │
│ 4. INV-2026-00039  $4,800.00  PAID     │
│ 5. INV-2026-00038  $3,650.00  PENDING  │
│                                         │
└─────────────────────────────────────────┘
```

---

## 🏗️ ARCHITECTURE BREAKDOWN

### **Layer 1: Domain (Business Logic)**

**RevenueAnalytics.kt** - Data model
```kotlin
data class RevenueAnalytics(
    val totalRevenueMonth: Double,
    val totalRevenueYear: Double,
    val totalRevenueWeek: Double,
    val monthOverMonthGrowth: Double,
    val currencyBreakdown: Map<String, Double>,
    val dailyTrend: List<DailyRevenuePoint>,
    val topInvoices: List<TopInvoiceItem>
)

data class DailyRevenuePoint(
    val date: LocalDate,
    val amount: Double
)

data class TopInvoiceItem(
    val invoiceId: Int,
    val invoiceNumber: String,
    val amount: Double,
    val status: String
)
```

### **Layer 2: Data (Repository)**

**RevenueRepository.kt** - Data access
```kotlin
interface RevenueRepository {
    fun observeRevenueAnalytics(businessId: Int): Flow<RevenueAnalytics>
    fun observeMonthlyRevenue(businessId: Int, year: Int, month: Int): Flow<Double>
    fun observeDailyTrend(businessId: Int, days: Int): Flow<List<DailyRevenuePoint>>
    fun observeTopInvoices(businessId: Int, limit: Int): Flow<List<TopInvoiceItem>>
}
```

**RevenueRepositoryImpl.kt** - Implementation
```kotlin
@Singleton
class RevenueRepositoryImpl(
    private val analyticsDao: AnalyticsDao,
    private val calculator: AnalyticsCalculator,
    private val businessRepository: BusinessProfileRepository
) : RevenueRepository {
    
    override fun observeRevenueAnalytics(businessId: Int): Flow<RevenueAnalytics> {
        return businessRepository.activeProfileId
            .flatMapLatest { bid ->
                combine(
                    getMonthlyRevenue(bid),
                    getYearlyRevenue(bid),
                    getWeeklyRevenue(bid),
                    getCurrencyBreakdown(bid),
                    getDailyTrend(bid, 30),
                    getTopInvoices(bid, 5)
                ) { month, year, week, breakdown, trend, top ->
                    RevenueAnalytics(
                        totalRevenueMonth = month,
                        totalRevenueYear = year,
                        totalRevenueWeek = week,
                        monthOverMonthGrowth = calculator.calculateMonthOverMonthGrowth(...),
                        currencyBreakdown = breakdown,
                        dailyTrend = trend,
                        topInvoices = top
                    )
                }
            }
    }
    
    // ... individual calculation methods
}
```

### **Layer 3: UI (ViewModel + Screen)**

**DashboardViewModel.kt** - State management
```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val revenueRepository: RevenueRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    val revenueAnalytics: StateFlow<RevenueAnalytics?> =
        revenueRepository.observeRevenueAnalytics(activeBusinessId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    val isLoading: StateFlow<Boolean> = // ...
    val error: StateFlow<String?> = // ...
}
```

**DashboardScreen.kt** - UI composition
```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val analytics by viewModel.revenueAnalytics.collectAsState()
    
    when {
        viewModel.isLoading.value -> LoadingScreen()
        viewModel.error.value != null -> ErrorScreen(viewModel.error.value!!)
        analytics != null -> {
            Column {
                RevenueHeaderSection(analytics!!)
                CurrencyBreakdownSection(analytics!!.currencyBreakdown)
                RevenueTrendChart(analytics!!.dailyTrend)
                TopInvoicesSection(analytics!!.topInvoices)
            }
        }
    }
}
```

---

## 📋 IMPLEMENTATION STEPS

### **Step 1: Create Domain Models (30 minutes)**

1. Create `domain/model/RevenueAnalytics.kt`
2. Create `domain/model/DailyRevenuePoint.kt`
3. Create `domain/model/TopInvoiceItem.kt`

**Files to create:** 3
**Lines of code:** ~60

---

### **Step 2: Create Repository Interface (30 minutes)**

1. Create `domain/repository/RevenueRepository.kt` (interface)
2. Define 4 suspend/Flow methods

**Files to create:** 1  
**Lines of code:** ~25

---

### **Step 3: Create Repository Implementation (90 minutes)**

1. Create `data/repository/RevenueRepositoryImpl.kt`
2. Implement using AnalyticsDao queries
3. Implement calculations using AnalyticsCalculator
4. Wire up reactive flows with flatMapLatest

**Files to create:** 1  
**Lines of code:** ~200

---

### **Step 4: Create ViewModel (45 minutes)**

1. Create `ui/dashboard/DashboardViewModel.kt`
2. Define StateFlow<RevenueAnalytics?>
3. Define StateFlow<Boolean> for loading
4. Define StateFlow<String?> for errors
5. Inject repository and observe

**Files to create:** 1  
**Lines of code:** ~80

---

### **Step 5: Create UI Components (90 minutes)**

1. Create `ui/dashboard/DashboardScreen.kt`
2. Create `ui/dashboard/components/RevenueHeaderSection.kt`
3. Create `ui/dashboard/components/CurrencyBreakdownSection.kt`
4. Create `ui/dashboard/components/RevenueTrendChart.kt`
5. Create `ui/dashboard/components/TopInvoicesSection.kt`

**Files to create:** 5  
**Lines of code:** ~400

---

### **Step 6: Create Tests (120 minutes)**

1. Create `RevenueRepositoryTest.kt`
2. Create `DashboardViewModelTest.kt`
3. Write 12+ test cases

**Files to create:** 2  
**Lines of code:** ~250
**Tests:** 12+

---

### **Step 7: Verify & Deploy (30 minutes)**

1. Compile and verify no errors
2. Run all tests
3. Deploy APK
4. Manual testing on emulator

---

## 🧪 TEST CASES

**RevenueRepositoryTest (7 tests):**
```
✓ Monthly revenue calculation
✓ Yearly revenue calculation
✓ Weekly revenue calculation
✓ Currency breakdown calculation
✓ Daily trend generation (30-day window)
✓ Top invoices retrieval (ordered by amount)
✓ Reactive update on invoice change
```

**DashboardViewModelTest (5 tests):**
```
✓ ViewModel initialization loads analytics
✓ Loading state transitions properly
✓ Error state displays correctly
✓ Analytics data flows to UI
✓ Handles repository errors gracefully
```

---

## 🎯 ACCEPTANCE CRITERIA

```
✅ MUST HAVE:
  □ Total revenue displays (MTD, YTD, this week)
  □ Month-over-month growth percentage
  □ Currency breakdown with percentages
  □ 30-day revenue trend chart
  □ Top 5 invoices by amount
  □ Real-time updates (reactive)
  □ All tests passing (100%)
  □ Zero compilation errors

✅ SHOULD HAVE:
  □ Professional styling (Material Design 3)
  □ Smooth animations
  □ Accessible colors
  □ Responsive layout (all screen sizes)
  □ Loading spinner animation
  □ Error message display

✅ NICE TO HAVE:
  □ Time period selector (week/month/year)
  □ Export as PDF
  □ Share via email
  □ Drill-down to invoice details
  □ Date range picker
```

---

## 🔧 TECHNICAL REQUIREMENTS

### **Dependencies (Already Installed):**
- ✅ Jetpack Compose
- ✅ Room Database
- ✅ Coroutines & Flow
- ✅ Hilt
- ✅ Timber (logging)

### **New Dependencies (May Need):**
- 📊 Chart Library (e.g., MPAndroidChart or Vico)
- 📱 Jetpack Accompanist (for Material 3)

### **Database Requirements:**
- ✅ AnalyticsDao (already created in Task 11)
- ✅ InvoiceAnalyticsSnapshot table
- ✅ DailyRevenueSnapshot table

---

## 📈 ESTIMATED EFFORT

```
Analysis:           30 minutes
Domain Models:      30 minutes
Repository:        120 minutes
ViewModel:          45 minutes
UI Components:      90 minutes
Tests:             120 minutes
Verification:       30 minutes
─────────────────────────────
TOTAL:             465 minutes (~7.75 hours)

CONTINGENCY:       +15% = 53 minutes
FINAL ESTIMATE:    8 hours
```

---

## 🚀 START CHECKLIST

Before you start Task 12:

```
✅ Task 11 Complete?
   □ Analytics entities created
   □ AnalyticsDao working
   □ AnalyticsCalculator tested

✅ Dependencies Installed?
   □ Jetpack Compose latest
   □ Room Database latest
   □ Coroutines latest

✅ Database Ready?
   □ Daily snapshots being populated
   □ Invoice analytics snapshots created
   □ Indices properly defined

✅ Time Available?
   □ 8 uninterrupted hours
   □ No meetings/distractions
   □ Fresh mind (not tired)

✅ Development Environment?
   □ Android Studio open
   □ Emulator running
   □ Gradle cache warmed

Ready to start? Begin with Step 1.
```

---

## 📝 REFERENCE LINKS

**Related Files:**
- `data/local/AnalyticsDao.kt` - Data access
- `domain/analytics/AnalyticsCalculator.kt` - Calculations
- `ui/theme/Theme.kt` - Colors & styling

**Documentation:**
- Task 11 Summary: `TASK_11_ANALYTICS_FOUNDATION_COMPLETE.md`
- Pathway 4 Overview: `PATHWAY_4_ANALYTICS_ROADMAP.md`
- Build Status: Project root `build_verification.log`

---

## ⚡ QUICK START COMMAND

Once you're ready to begin:

```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

# Create domain models
mkdir -p app/src/main/java/com/emul8r/bizap/domain/model

# Create repository
mkdir -p app/src/main/java/com/emul8r/bizap/data/repository

# Create UI
mkdir -p app/src/main/java/com/emul8r/bizap/ui/dashboard/components

# Start with domain models first
```

---

**Task 12 is ready to execute. All prerequisites are in place. Good luck!** 🚀

