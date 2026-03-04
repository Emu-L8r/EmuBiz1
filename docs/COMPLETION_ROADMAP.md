# Completion Roadmap

**Date:** 2026-03-04  
**Project:** Bizap / EmuBiz  
**Current Version:** 1.0 (versionCode 2)

---

## Stage 1 Status — Core Feature Set (Current)

### ✅ Fully Working

| Feature | Notes |
|---|---|
| Business Profile Setup | Multi-field form, logo upload, bank details, tax registration |
| Multi-Business Switching | `BusinessSwitcherDialog`, scoped data per business |
| Customer CRUD | Create, list, view, edit, delete customers |
| Invoice Creation | Full line item form, customer selection, tax, currency |
| Invoice Editing | Local edit state pattern, all fields editable |
| Invoice Detail View | Status, amounts, line items, version picker |
| Invoice PDF Generation | Android native PdfDocument API, A4, logo, custom fonts |
| PDF Export to Downloads | `MediaStore` export to `Downloads/Bizap/` |
| PDF Share via Android | `FileProvider` + `ACTION_SEND` chooser |
| Invoice Printing | System print integration via `PrintService` |
| Invoice Status Management | DRAFT → SENT → PAID / OVERDUE / PARTIALLY_PAID |
| Invoice Versioning | Correction copies with `parentInvoiceId`, `version` field |
| Quote Generation | Same PDF flow with "Quote" label |
| Document Vault | Lists all generated PDFs with file management |
| Prefilled Items | Reusable line item templates |
| Theme Settings | Material3 dynamic colour / custom theme via DataStore |
| Scoped Invoice Numbering | `INV-YYYY-NNNNNN` per business per year |

### ⚠️ Partially Working / Known Gaps

| Feature | Gap | Impact |
|---|---|---|
| Dashboard Revenue Card | Hardcoded `"$0.00"` — `RevenueDashboardViewModel` not wired | Users see no revenue data on dashboard |
| Payment Recording (`recordPayment`) | ViewModel method exists, UI button may not trigger it consistently | Partial payments may not update status reliably |
| Overwrite Dialog (PDF) | Dialog logic complete, UI integration needs verification in all paths | User may not be prompted in all overwrite scenarios |

### ❌ Not Started / Blocked

| Feature | Blocker |
|---|---|
| Automated Tests | Zero test files — test infrastructure configured but unused |
| Revenue metric on Dashboard | Just needs wiring (see Stage 2 items) |

---

## Stage 2 Planning — Multi-Currency & Advanced Features

### Multi-Currency Infrastructure (Already Built — Needs UI)

The following components exist and are fully functional; they simply need to be surfaced in the UI:

| Component | File | Status |
|---|---|---|
| `CurrencyEntity` | `data/local/entities/CurrencyEntity.kt` | ✅ Entity defined |
| `ExchangeRateEntity` | `data/local/entities/ExchangeRateEntity.kt` | ✅ Entity defined |
| `CurrencyDao` | `data/local/CurrencyDao.kt` | ✅ DAO defined |
| `CurrencyRepository` (interface) | `domain/repository/CurrencyRepository.kt` | ✅ Interface defined |
| `CurrencyRepositoryImpl` | `data/repository/CurrencyRepositoryImpl.kt` | ✅ Implementation bound in `RepositoryModule` |
| `ExchangeRateService` (Retrofit) | `data/service/ExchangeRateService.kt` | ✅ API interface defined |
| `ExchangeRateWorker` | `data/worker/ExchangeRateWorker.kt` | ✅ WorkManager worker defined |
| `NetworkModule` | `di/NetworkModule.kt` | ✅ Retrofit/OkHttp configured |
| `currencyCode` on `InvoiceEntity` | column in `invoices` table | ✅ Persisted (default `'AUD'`) |
| `currencyCode` on `LineItemEntity` | column in `line_items` table | ✅ Added in Migration v22→v23 |
| `getCurrencySymbol()` | `InvoicePdfService.kt` | ✅ Maps code to symbol for PDF |

**To wire multi-currency:**
1. Add currency selector to `CreateInvoiceScreen` and `EditInvoiceScreen`
2. Call `CurrencyRepository.getEnabledCurrencies()` from `CreateInvoiceViewModel`
3. Trigger `ExchangeRateWorker` on currency selection to ensure rates are fresh
4. Update PDF generation to use the invoice's `currencyCode`
5. Add currency display to Invoice Detail and Invoice List screens

**Estimated effort:** 2–3 days

---

## Advanced Features Assessment

Each of the following has a complete backend stack. The work required is UI wiring + navigation.

### Feature 1: Revenue Dashboard

**Effort:** 1–2 days  
**What exists:**
- `RevenueDashboardViewModel` — `ui/analytics/RevenueDashboardViewModel.kt`
- `GetRevenueMetricsUseCase` — `domain/usecase/GetRevenueMetricsUseCase.kt`
- `RevenueDashboardScreen` — `ui/analytics/RevenueDashboardScreen.kt`
- `DailyRevenueSnapshot` entity + DAO
- Vico charts library already in dependencies

**What's needed:**
1. Add `Screen.RevenueDashboard` to `Screen` sealed class in `ui/navigation/Screen.kt`
2. Add navigation route in `NavGraph`
3. Add nav entry point (e.g., from Dashboard or settings menu)
4. Fix Dashboard's hardcoded `"$0.00"` — wire `RevenueDashboardViewModel` or a lightweight version

---

### Feature 2: Invoice Templates

**Effort:** 2–3 days  
**What exists:**
- `InvoiceTemplate` + `InvoiceCustomField` entities (DB version 24)
- `InvoiceTemplateViewModel` — `ui/templates/InvoiceTemplateViewModel.kt`
- `TemplateSnapshotManager` — `ui/templates/TemplateSnapshotManager.kt`
- Template JSON snapshot system in `InvoicePdfService`
- `templateSnapshotJson` parameter already in `generateInvoice()`

**What's needed:**
1. Add `Screen.InvoiceTemplates` to navigation
2. Build `InvoiceTemplateScreen` UI for creating/editing templates (or complete the existing one)
3. Wire template selection into `CreateInvoiceScreen`
4. Pass selected template's JSON snapshot when calling `GenerateAndSaveInvoiceUseCase`

---

### Feature 3: Payment Tracking

**Effort:** 1–2 days  
**What exists:**
- `InvoicePaymentEntity` table + `PaymentDao`
- `InvoicePaymentSnapshot` and `DailyPaymentSnapshot` tables
- `InvoiceDetailViewModel.recordPayment()` method
- `GetPaymentAnalyticsUseCase`

**What's needed:**
1. Add "Record Payment" button/dialog to `InvoiceDetailScreen`
2. Connect button to `InvoiceDetailViewModel.recordPayment(amount)`
3. Display payment history in Invoice Detail
4. Wire `GetPaymentAnalyticsUseCase` to `RevenueDashboardViewModel`

---

### Feature 4: Customer Segmentation & Analytics

**Effort:** 3–4 days  
**What exists:**
- `GetCustomerAnalyticsUseCase` + `SegmentCustomersUseCase`
- `CustomerAnalyticsSnapshot` entity
- `CollectionMetrics` entity

**What's needed:**
1. Build `CustomerAnalyticsScreen` or extend `CustomerDetailScreen`
2. Add segmentation view (A/B/C customer tiers based on revenue)
3. Display collection metrics per customer
4. Wire to navigation

---

### Feature 5: Dunning Notices

**Effort:** 2–3 days  
**What exists:**
- `DunningNoticesViewModel` — `ui/dunning/DunningNoticesViewModel.kt`
- `GenerateDunningNoticesUseCase`

**What's needed:**
1. Complete `DunningNoticesScreen` (or build from scratch)
2. Add to navigation
3. Implement actual notice generation (PDF or email intent)
4. Test with overdue invoice scenarios

---

### Feature 6: Risk Scoring & Cash Flow Forecasting

**Effort:** 3–5 days  
**What exists:**
- `RiskDashboardViewModel` — `ui/analytics/RiskDashboardViewModel.kt`
- `IdentifyRiskInvoicesUseCase`
- `ForecastCashFlowUseCase`
- `BusinessHealthMetrics` entity

**What's needed:**
1. Build `RiskDashboardScreen` UI with risk indicator components
2. Wire `BusinessHealthMetrics` calculation (likely a background worker)
3. Integrate cash flow chart using Vico
4. Add to analytics navigation section

---

## Technical Debt

### High Priority (Fix Now)

| Debt Item | Location | Action |
|---|---|---|
| Duplicate DAO files | `data/local/AnalyticsDao.kt` + `data/local/dao/AnalyticsDao.kt` | Delete the unused one; confirm which is registered in `BizapDatabase` |
| Duplicate entity files | `data/local/entities/CustomerAnalyticsSnapshot.kt` + `data/local/entity/CustomerAnalyticsSnapshot.kt` | Delete the unused one |
| Out-of-package ViewModel | `Bizap/ui/invoices/InvoiceDetailViewModel.kt` | Review and delete if stale |
| Zero test coverage | All packages | Begin with use case and repository unit tests |

### Medium Priority (Address in Next Sprint)

| Debt Item | Location | Action |
|---|---|---|
| `BusinessProfileRepository` has no domain interface | `data/repository/BusinessProfileRepository.kt` | Extract interface to `domain/repository/` |
| Dashboard revenue hardcoded | `ui/dashboard/DashboardScreen.kt` line 93 | Wire `RevenueDashboardViewModel` |
| Documentation files in wrong locations | Various `.md` files in `Bizap/` root | Move/centralise to `docs/` directory |
| `InvoiceDao.insertInvoice()` uses `ABORT` strategy | `data/local/InvoiceDao.kt` | Consider `REPLACE` for update scenarios |

### Low Priority (Backlog)

| Debt Item | Action |
|---|---|
| Currency symbol mapping incomplete | Add `CAD`, `NZD`, `CHF`, `CNY`, `INR` to `getCurrencySymbol()` |
| PDF styling hardcoded to A4 | Consider configurable page size (Letter for US market) |
| Sparse KDoc | Add KDoc to all public ViewModel functions and use cases |
| Firebase Analytics events | No custom events configured — add key user journey tracking |
| Emoji log tags not centralised | Move to a `LogTags` constants object |

---

## Recommended Next Steps

### Immediate (Week 1)

1. **Fix duplicate files** — 1 hour, eliminates confusion and potential build issues
2. **Wire dashboard revenue** — 0.5 days, makes the dashboard useful
3. **Add first unit tests** — 1 day: `SaveInvoiceUseCase`, `InvoiceRepositoryImpl.saveInvoice()` monetary logic
4. **Record Payment UI** — 0.5 days: add dialog to Invoice Detail, wire existing ViewModel method

### Short Term (Weeks 2–3)

5. **Multi-currency UI** — 2–3 days: currency picker, exchange rate display
6. **Invoice Templates UI** — 2–3 days: template CRUD, selection during invoice creation
7. **Revenue Dashboard navigation** — 1 day: add to nav graph + fix chart data

### Medium Term (Month 2)

8. **Customer Analytics** — 3–4 days
9. **Dunning Notices** — 2–3 days
10. **Risk Dashboard** — 3–5 days
11. **Test coverage to 50%** — ongoing, target use cases + ViewModels

---

## Effort Estimates Table

| Feature | Est. Days | Prerequisite | Priority |
|---|---|---|---|
| Fix duplicate files | 0.1 | None | 🔴 Now |
| Dashboard revenue card | 0.5 | None | 🔴 Now |
| First unit tests (core) | 1.0 | None | 🔴 Now |
| Payment Recording UI | 0.5 | None | 🔴 Now |
| Multi-currency UI | 2.5 | None (infra exists) | 🟡 Soon |
| Invoice Templates UI | 2.5 | None (infra exists) | 🟡 Soon |
| Revenue Dashboard nav | 1.0 | None (VM exists) | 🟡 Soon |
| `BusinessProfileRepository` interface | 0.5 | None | 🟡 Soon |
| Customer Analytics screen | 3.5 | None (use cases exist) | 🟢 Later |
| Dunning Notices screen | 2.5 | None (VM exists) | 🟢 Later |
| Risk Dashboard screen | 4.0 | None (VM exists) | 🟢 Later |
| Cash Flow Forecasting UI | 2.0 | Risk Dashboard | 🟢 Later |
| Test coverage 50% | 5.0 | Core features stable | 🟢 Later |
| Business Health Metrics | 3.0 | Analytics infra | 🟢 Later |
| **Total estimated remaining** | **~29 days** | | |
