# GUI2 Implementation Checklist

## Data Layer
- [x] `InvoiceDaoV2` — 13 direct invoice queries
- [x] `DailyRevenueTrendV2` — query result model
- [x] `InvoiceStatusCountV2` — query result model
- [x] `AppDatabase` — updated to expose `invoiceDaoV2()`
- [x] `DatabaseModule` — provides `InvoiceDaoV2`

## Repository Layer
- [x] `RevenueRepositoryV2` — 5 revenue flows combined
- [x] `PaymentAnalyticsRepositoryV2` — payment metrics
- [x] `RiskAnalyticsRepositoryV2` — risk tier metrics
- [x] `BusinessContextRepositoryV2` — business context

## Domain Models
- [x] `BusinessContextV2`
- [x] `RevenueMetricsV2` + `DailyTrendPointV2`
- [x] `PaymentMetricsV2` + `StatusBreakdownV2`
- [x] `RiskMetricsV2`
- [x] `DashboardStateV2`

## Mapper
- [x] `DtoMapperV2` — DAO → domain model mappings

## Dependency Injection
- [x] `GuiV2Module` — binds all GUI2 repositories

## Landing Page
- [x] `GuiMode` — enum (GUI1, GUI2)
- [x] `LandingViewModel` — DataStore persistence
- [x] `LandingScreen` — card-based UI choice

## GUI2 Navigation
- [x] `ScreenV2` — 5 type-safe routes with mandatory businessId
- [x] `GuiV2NavGraph` — NavHost for all GUI2 screens
- [x] `NavExtensionsV2` — helper navigation functions

## GUI2 Common
- [x] `GuiV2Components` — MetricCardV2, LoadingIndicatorV2, ErrorStateV2, SectionHeaderV2, formatCents
- [x] `GuiV2Theme` — theme wrapper + semantic colors

## ViewModels
- [x] `DashboardViewModelV2`
- [x] `RevenueAnalyticsViewModelV2`
- [x] `PaymentAnalyticsViewModelV2`
- [x] `RiskAnalyticsViewModelV2`
- [x] `InvoiceDetailViewModelV2`

## UI Screens
- [x] `DashboardScreenV2`
- [x] `RevenueAnalyticsScreenV2`
- [x] `PaymentAnalyticsScreenV2`
- [x] `RiskAnalyticsScreenV2`
- [x] `InvoiceDetailScreenV2`

## MainActivity Integration
- [x] Landing screen shown on first launch
- [x] GUI1 / GUI2 routing based on persisted selection
- [x] `MainScreen` accepts `onSwitchGui` callback

## Documentation
- [x] `docs/DUAL_GUI_TECHNICAL_SPEC.md`
- [x] `docs/GUI2_IMPLEMENTATION_CHECKLIST.md`
- [x] `docs/MIGRATION_GUIDE.md`

## Tests
- [x] `InvoiceDaoV2Test` — query tests
- [x] `RevenueRepositoryV2Test` — repository flow tests
