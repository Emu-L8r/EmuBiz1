# TODO Implementation Checklist

Tracks every outstanding `// TODO` in the codebase, with context, implementation plan, and status.

> **Last updated:** April 2026 (Phase 3 implementation complete)

---

## TODO 1 — Firebase Auth Integration ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/di/UserIdProvider.kt`  
**Implementation:** `FirebaseAuth` injected via `FirebaseModule.provideFirebaseAuth()`. Returns `FirebaseAuth.currentUser?.uid ?: "anonymous"` with exception fallback.

**Tasks:**
- [x] Add Firebase Auth dependency (`com.google.firebase:firebase-auth`)
- [x] Inject `FirebaseAuth` into `UserIdProvider`
- [x] Return `FirebaseAuth.currentUser?.uid ?: "anonymous"`
- [x] Add fallback for offline / not-signed-in state
- [ ] Unit tests with mock `FirebaseAuth`
- [ ] Integration tests with real Firebase project

**Effort:** 3 days  
**Priority:** Critical (all user data is keyed on this ID)

---

## TODO 2 — Event Deserialization ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/data/repository/AnalyticsRepositoryImpl.kt`  
**Implementation:** Custom `InvoiceAnalyticsEventDeserializer` (Gson `JsonDeserializer`) reads the `"type"` discriminator field stored alongside each event. Events are stored with `type` field added at serialisation time.

**Tasks:**
- [x] Choose serialisation library (Gson `JsonDeserializer`)
- [x] Add type discriminator (`"type"` field) when storing events
- [x] Implement polymorphic deserialisation for all 4 event types
- [ ] Unit tests for all event types
- [ ] Migration script for existing events (add `type` field retroactively if needed)

---

## TODO 3 — Backup / Restore Operations ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/ui/settings/backup/BackupRestoreViewModel.kt`  
**Implementation:** `CustomerRepository.deleteAllCustomers()` and `InvoiceRepository.deleteAllInvoices()` added and injected into ViewModel. Each reset function now delegates to the real repository methods.

**Tasks:**
- [x] Add `deleteAllCustomers()` method to `CustomerRepository` + impl
- [x] Add `deleteAllInvoices()` method to `InvoiceRepository` + impl (cascades to line items, payments, snapshots)
- [x] Implement each function in ViewModel with error handling
- [ ] Add confirmation dialog before destructive operations (UI layer)
- [x] Show progress indicator during deletion (reuses BackupInProgress state)
- [ ] Unit tests for each deletion path
- [x] Verify cascades via DAO deleteAll queries

---

## TODO 4 — QR Code on PDFs ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/domain/pdf/PdfQrCodeRenderer.kt`  
**Implementation:** ZXing `QRCodeWriter` encodes to `BitMatrix`, then converted to `Bitmap` and drawn to `Canvas`.

**Tasks:**
- [x] Add `com.google.zxing:core:3.5.3` dependency
- [x] Implement `generateQrBitmap()` using `QRCodeWriter`
- [x] Draw bitmap to canvas in `drawPaymentQrCode()` and `drawPaymentUrl()`
- [ ] Integrate optional QR flag into `InvoiceSettings`
- [ ] Tests with sample payment references

---

## TODO 5 — Date Range Filtering in Analytics ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/ui/analytics/PaymentAnalyticsTabViewModel.kt`  
**Implementation:** `combine(_dateRange, businessProfileRepository.activeProfile)` triggers re-subscription when date range changes. `filterByDateRange()` filters `riskInvoices` and `cashFlowForecast` by `LocalDate` cutoff.

**Tasks:**
- [x] Wire `_dateRange` StateFlow as upstream trigger
- [x] Filter `riskInvoices` by `dueDate >= cutoffDate`
- [x] Filter `cashFlowForecast` by `projectedDate >= cutoffDate`
- [ ] Tests for filtering accuracy

---

## TODO 6 — Banking Details in PDF ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/ui/invoices/html/InvoiceTemplateDataMapper.kt`  
**Implementation:** `mapToTemplateData()` now accepts an optional `BusinessProfile` parameter. Maps `bankName`, `accountNumber`, `accountName`, `bsbNumber`, and a masked `accountNumberMasked` to template variables.

**Tasks:**
- [x] Add `BusinessProfile` parameter to `mapToTemplateData()`
- [x] Map `bankName`, `accountNumber`, `bsbNumber`, `accountName`
- [x] Add `accountNumberMasked` for safe display
- [ ] Validate on test invoices (requires template update)
- [ ] Unit test mapping with mock profile

---

## TODO 7 — Advanced Invoice Filters ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/ui/gui2/invoices/InvoiceSearchAndFilter.kt`  
**Implementation:** `AdvancedFilters()` composable now has `OutlinedTextField` pairs for date (DD/MM/YYYY) and amount ($) ranges. `parseDateRange()` and `parseAmountRange()` helper functions validate and parse inputs into `DateRange` and `LongRange` types.

**Tasks:**
- [x] Add date input text fields (DD/MM/YYYY format)
- [x] Add amount min/max input text fields
- [x] Parse date and amount ranges with validation
- [x] Wire filters to `onFilterChange` callback via `InvoiceSearchQuery`
- [ ] Tests for filter combinations

---

## TODO 8 — Analytics Drill-down ✅ IMPLEMENTED

**File:** `com/emul8r/bizap/ui/analytics/RevenueAnalyticsTab.kt`  
**Implementation:** `LineChartCard.onDataPointClick` is now wired in `RevenueAnalyticsTab` to call `onDrillClick("Daily Revenue: ${point.label}", ...)`. Bars in `LineChartCard` are clickable and update `hoveredIndex` for visual feedback.

**Tasks:**
- [x] Wire `onDataPointClick` callback in `RevenueAnalyticsTab`
- [x] Make chart bars clickable via `Modifier.clickable { }` in `LineChartCard`
- [x] Pass point label + value to `onDrillClick`

---

## Status Summary

| # | Description | Priority | Status | Effort |
|---|---|---|---|---|
| 1 | Firebase Auth Integration | Critical | ✅ Done | 3 days |
| 2 | Event Deserialization | High | ✅ Done | 2 days |
| 3 | Backup/Restore Operations | High | ✅ Done | 3 days |
| 4 | QR Code on PDFs | Medium | ✅ Done | 2 days |
| 5 | Date Range Analytics | Medium | ✅ Done | 2 days |
| 6 | Banking Details in PDF | Medium | ✅ Done | 1 day |
| 7 | Advanced Invoice Filters | Medium | ✅ Done | 2 days |
| 8 | Analytics Drill-down | Low | ✅ Done | 1 day |
