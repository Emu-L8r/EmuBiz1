# DUAL GUI TECHNICAL SPECIFICATION

## Overview

The Dual GUI system allows users to choose between the legacy **GUI1** experience and a new, architecturally clean **GUI2** experience. The choice is persisted in DataStore and can be changed at any time from Settings.

---

## 1. Architecture Principles

### GUI1 (Legacy)
- Snapshot-based analytics (DailyRevenueSnapshot, InvoiceAnalyticsSnapshot)
- Navigation without mandatory businessId parameter
- Known issues with snapshot staleness and context ambiguity

### GUI2 (New)
- **Option C**: Direct invoice queries — no snapshot dependencies
- **Context-aware navigation**: every screen receives `businessId` as a mandatory parameter
- **Single source of truth**: all data read directly from the `invoices` table
- **Reactive**: all flows automatically update on invoice changes
- **Type-safe**: Sealed classes for UI state, `@Serializable` route objects

---

## 2. Landing Page

| File | Purpose |
|------|---------|
| `ui/landing/GuiMode.kt` | `enum class GuiMode { GUI1, GUI2 }` |
| `ui/landing/LandingViewModel.kt` | Reads/writes selection to DataStore |
| `ui/landing/LandingScreen.kt` | UI — card-based choice between GUI1 and GUI2 |

### Flow
```
App Launch
    → MainActivity reads DataStore via LandingViewModel
    → null: show LandingScreen (first launch or reset)
    → GUI1: show MainScreen (legacy)
    → GUI2: show GuiV2NavGraph with businessId
```

---

## 3. GUI2 Navigation

| File | Purpose |
|------|---------|
| `ui/gui2/navigation/ScreenV2.kt` | Sealed `@Serializable` route classes |
| `ui/gui2/navigation/GuiV2NavGraph.kt` | NavHost with all GUI2 routes |
| `ui/gui2/navigation/NavExtensionsV2.kt` | Helper extension functions |

### Routes
```kotlin
ScreenV2.Dashboard(businessId)
ScreenV2.RevenueAnalytics(businessId)
ScreenV2.PaymentAnalytics(businessId)
ScreenV2.RiskAnalytics(businessId)
ScreenV2.InvoiceDetail(businessId, invoiceId)
```

---

## 4. Database Layer — InvoiceDaoV2

All queries target the `invoices` table directly. No snapshot joins.

| Method | Returns | Description |
|--------|---------|-------------|
| `observeMTDRevenue(businessId)` | `Flow<Long>` | Month-to-date paid revenue (cents) |
| `observeYTDRevenue(businessId)` | `Flow<Long>` | Year-to-date paid revenue (cents) |
| `observeWeeklyRevenue(businessId)` | `Flow<Long>` | Last 7 days paid revenue (cents) |
| `observeTotalPaidRevenue(businessId)` | `Flow<Long>` | All-time paid revenue (cents) |
| `observeLast30DaysRevenueTrend(businessId)` | `Flow<List<DailyRevenueTrendV2>>` | Per-day revenue for last 30 days |
| `observeOutstandingAmount(businessId)` | `Flow<Long>` | Unpaid balance (SENT+PARTIALLY_PAID+OVERDUE) |
| `observeCollectedAmount(businessId)` | `Flow<Long>` | Sum of amountPaid across all invoices |
| `observeInvoiceCountByStatus(businessId)` | `Flow<List<InvoiceStatusCountV2>>` | Count per status |
| `observeOverdueCount(businessId)` | `Flow<Int>` | Count of OVERDUE invoices |
| `observeHighRiskInvoiceCount(businessId)` | `Flow<Int>` | OVERDUE 60+ days |
| `observeAtRiskInvoiceCount(businessId)` | `Flow<Int>` | OVERDUE 30–59 days |
| `observeHealthyInvoiceCount(businessId)` | `Flow<Int>` | PAID or not yet due |
| `observeAverageDaysToPayment(businessId)` | `Flow<Double>` | Avg days from issue to due date (PAID invoices) |

---

## 5. Repository Layer

| Repository | Combines | Emits |
|-----------|----------|-------|
| `RevenueRepositoryV2` | 5 revenue flows | `Flow<RevenueMetricsV2>` |
| `PaymentAnalyticsRepositoryV2` | outstanding + collected + status + overdue + avgDays | `Flow<PaymentMetricsV2>` |
| `RiskAnalyticsRepositoryV2` | highRisk + atRisk + healthy + overdue + outstanding | `Flow<RiskMetricsV2>` |
| `BusinessContextRepositoryV2` | activeProfile | `Flow<BusinessContextV2>` |

---

## 6. ViewModel Layer

All ViewModels use `SavedStateHandle.toRoute<ScreenV2.*>()` to extract the guaranteed-non-null `businessId`.

| ViewModel | Route | State type |
|-----------|-------|-----------|
| `DashboardViewModelV2` | `ScreenV2.Dashboard` | `DashboardUiStateV2` |
| `RevenueAnalyticsViewModelV2` | `ScreenV2.RevenueAnalytics` | `RevenueAnalyticsUiStateV2` |
| `PaymentAnalyticsViewModelV2` | `ScreenV2.PaymentAnalytics` | `PaymentAnalyticsUiStateV2` |
| `RiskAnalyticsViewModelV2` | `ScreenV2.RiskAnalytics` | `RiskAnalyticsUiStateV2` |
| `InvoiceDetailViewModelV2` | `ScreenV2.InvoiceDetail` | `InvoiceDetailUiStateV2` |

---

## 7. Domain Models

| Model | Fields |
|-------|--------|
| `BusinessContextV2` | businessId, businessName, currencyCode |
| `RevenueMetricsV2` | mtdRevenue, ytdRevenue, weeklyRevenue, totalPaidRevenue, outstandingAmount, collectedAmount, dailyTrend |
| `PaymentMetricsV2` | totalInvoices, paidCount, sentCount, overdueCount, partiallyPaidCount, draftCount, outstandingAmount, collectedAmount, averageDaysToPayment, statusBreakdown |
| `RiskMetricsV2` | highRiskCount, atRiskCount, healthyCount, overdueCount, totalOutstandingCents |
| `DashboardStateV2` | businessContext, revenueMetrics, paymentMetrics, riskMetrics |

---

## 8. Dependency Injection

`GuiV2Module` (Hilt `@Module`, `SingletonComponent`) provides:
- `RevenueRepositoryV2`
- `PaymentAnalyticsRepositoryV2`
- `RiskAnalyticsRepositoryV2`
- `BusinessContextRepositoryV2`

All ViewModels use `@HiltViewModel` + `@Inject`.

---

## 9. Risk Classification

| Tier | Condition |
|------|-----------|
| High Risk | `status = OVERDUE` AND `dueDate` < today − 60 days (60+ days overdue) |
| At Risk | `status = OVERDUE` AND today − 60 days ≤ `dueDate` < today − 30 days (30–59 days overdue) |
| Healthy | `status = PAID` OR (`status = SENT` AND `dueDate` ≥ today or unset) |
