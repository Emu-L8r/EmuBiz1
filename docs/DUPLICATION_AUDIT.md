# Duplication Audit

An inventory of duplicated code across GUI1 and GUI2, prioritised for consolidation.

> **Last updated:** April 2026  
> **Context:** The dual-GUI architecture was necessary during the GUI2 build-out. Now that GUI2 is the default (v2.0+), Tier 1 duplicates should be consolidated to reduce maintenance burden.

---

## Tier 1 — High Priority (Critical Duplication)

### 1.1 InvoiceDetailScreen vs InvoiceDetailScreenV2

| Aspect | Status |
|---|---|
| **Locations** | `ui/invoices/InvoiceDetailScreen.kt` + `ui/gui2/invoice/InvoiceDetailScreenV2.kt` |
| **Shared logic** | ~70% — both use `InvoiceDetailViewModel` and `InvoiceDetailUiState` |
| **ViewModel duplication** | None — consolidated as of Phase 4 |
| **State duplication** | None — `InvoiceDetailUiState` is shared |
| **Remaining duplication** | Layout/composable code only |
| **Consolidation strategy** | Extract shared `InvoiceDetailContent(invoice, onAction)` composable; GUI1 and GUI2 screens become thin wrappers |
| **Effort** | 3 days |
| **Test coverage needed** | `InvoiceDetailViewModelTest` already exists |

---

### 1.2 CreateInvoiceScreen vs CreateInvoiceScreenV2

| Aspect | Status |
|---|---|
| **Locations** | `ui/invoices/CreateInvoiceScreen.kt` + `ui/gui2/invoices/CreateInvoiceScreenV2.kt` |
| **Shared logic** | ~60% — both use `CreateInvoiceViewModel` |
| **ViewModel duplication** | None — `CreateInvoiceViewModel` is shared |
| **Remaining duplication** | Form layout, photo attachment UI |
| **Consolidation strategy** | Extract `CreateInvoiceForm(state, onAction)` composable |
| **Effort** | 3 days |
| **Test coverage needed** | `CreateInvoiceViewModelTest` should cover shared logic |

---

### 1.3 CustomerDetailScreen vs CustomerDetailScreenV2Content

| Aspect | Status |
|---|---|
| **Locations** | `ui/customers/CustomerDetailScreen.kt` + `ui/gui2/customers/CustomerDetailScreenV2.kt` |
| **Shared logic** | ~75% — both use `CustomerDetailViewModel` |
| **ViewModel duplication** | None — consolidated as of Phase 4 |
| **Consolidation strategy** | Extract `CustomerDetailContent(customer, onAction)` composable |
| **Effort** | 2 days |

---

### 1.4 Settings Screens

| Aspect | Status |
|---|---|
| **Locations** | `ui/settings/InvoiceSettingsScreen.kt` (shared between GUI1 and GUI2) |
| **Status** | Already consolidated — single screen used by both GUIs |
| **Action** | None needed |

---

## Tier 2 — Medium Priority

### 2.1 Analytics ViewModels

| Aspect | Notes |
|---|---|
| **Location** | `ui/analytics/` — multiple tab ViewModels with similar structure |
| **Duplication** | `RevenueAnalyticsTabViewModel`, `PaymentAnalyticsTabViewModel`, `CustomerAnalyticsTabViewModel` all implement similar "load snapshots → transform → emit state" patterns |
| **Strategy** | Extract `AnalyticsTabViewModel<T, S>` base class with generic load/transform |
| **Effort** | 2 days |

---

### 2.2 Payment Screens

| Aspect | Notes |
|---|---|
| **Location** | `ui/payments/` vs `ui/gui2/payments/` |
| **Duplication** | PaymentDetailScreen / RecordPaymentScreen exist in both |
| **Strategy** | Consolidate after Invoice screens (lower risk) |
| **Effort** | 2 days |

---

## Tier 3 — Low Priority

### 3.1 Utility Functions

| Aspect | Notes |
|---|---|
| **Location** | Various `*Utils.kt` files |
| **Duplication** | `formatMoney()` exists in both `HtmlPdfInvoiceService` and some UI helpers |
| **Strategy** | Extract to `domain/utils/MoneyFormatter.kt` |
| **Effort** | 1 day |

---

### 3.2 Extension Functions

| Aspect | Notes |
|---|---|
| **Location** | `ui/` and `data/` packages |
| **Duplication** | `escapeHtml()`, `addressLines()` are private to `HtmlPdfInvoiceService` but similar functions exist elsewhere |
| **Strategy** | Move to `utils/HtmlUtils.kt` |
| **Effort** | 0.5 days |

---

## Consolidation Process

When consolidating a screen pair:

1. **Audit** — diff the two files; identify truly shared logic vs layout differences
2. **Extract** — create `XxxContent(...)` composable with no navigation/scaffold
3. **Wrap** — have both screens use `XxxContent`
4. **Test** — run `./gradlew test`; verify no regressions
5. **Document** — update this file to mark as done

---

## Progress Tracker

| Item | Tier | Status | PR |
|---|---|---|---|
| InvoiceDetailScreen consolidation | 1 | ⏳ Pending | — |
| CreateInvoiceScreen consolidation | 1 | ⏳ Pending | — |
| CustomerDetailScreen consolidation | 1 | ✅ Done (Phase 4) | — |
| Settings consolidation | 1 | ✅ Done | — |
| Analytics ViewModel base class | 2 | ⏳ Pending | — |
| Payment screens consolidation | 2 | ⏳ Pending | — |
| MoneyFormatter utility | 3 | ⏳ Pending | — |
