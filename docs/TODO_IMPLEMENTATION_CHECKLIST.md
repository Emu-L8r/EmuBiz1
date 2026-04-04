# TODO Implementation Checklist

Tracks every outstanding `// TODO` in the codebase, with context, implementation plan, and status.

> **Last updated:** April 2026

---

## TODO 1 — Firebase Auth Integration ⚠️ CRITICAL

**File:** `com/emul8r/bizap/di/UserIdProvider.kt`  
**Current code:**
```kotlin
fun getCurrentUserId(): String = "current_user"  // TODO: Replace with Firebase Auth
```
**Target:** Return the real Firebase Auth UID, or `"anonymous"` if not signed in.

**Tasks:**
- [ ] Add Firebase Auth dependency (`com.google.firebase:firebase-auth-ktx`)
- [ ] Inject `FirebaseAuth` into `UserIdProvider`
- [ ] Return `FirebaseAuth.currentUser?.uid ?: "anonymous"`
- [ ] Add fallback for offline / not-signed-in state
- [ ] Unit tests with mock `FirebaseAuth`
- [ ] Integration tests with real Firebase project

**Effort:** 3 days  
**Priority:** Critical (all user data is keyed on this ID)

---

## TODO 2 — Event Deserialization 🔥 HIGH

**File:** `com/emul8r/bizap/data/repository/AnalyticsRepositoryImpl.kt`  
**Current code:**
```kotlin
// TODO: Implement polymorphic deserialization — currently returns null
fun deserializeEvent(json: String): AnalyticsEvent? = null
```
**Target:** Deserialise stored analytics events back into typed `AnalyticsEvent` subclasses.

**Tasks:**
- [ ] Choose serialisation library (Gson with `RuntimeTypeAdapterFactory` or Moshi with sealed class support)
- [ ] Add type discriminator (`"type"` field) when storing events
- [ ] Implement polymorphic deserialisation
- [ ] Unit tests for all event types
- [ ] Migration script for existing events (add `type` field retroactively if needed)

**Effort:** 2 days

---

## TODO 3 — Backup / Restore Operations 🔥 HIGH

**File:** `com/emul8r/bizap/ui/settings/backup/BackupRestoreViewModel.kt`  
**Current code:**
```kotlin
fun resetAllData() {
    // TODO: Implement — delete all data
}
fun resetCustomerData() {
    // TODO: Implement — delete all customers
}
fun resetInvoiceData() {
    // TODO: Implement — delete all invoices and payments
}
```

**Tasks:**
- [ ] Add `deleteAll()` method to `CustomerRepository`
- [ ] Add `deleteAll()` method to `InvoiceRepository` (cascade to line items, payments)
- [ ] Implement each function in ViewModel with error handling
- [ ] Add confirmation dialog before destructive operations
- [ ] Show progress indicator during deletion
- [ ] Unit tests for each deletion path
- [ ] Verify cascades via DB constraints

**Effort:** 3 days

---

## TODO 4 — QR Code on PDFs

**File:** `com/emul8r/bizap/domain/pdf/PdfQrCodeRenderer.kt`  
**Current code:**
```kotlin
// TODO: Implement QR code bitmap generation using zxing
fun renderQrCode(content: String, size: Int): Bitmap? = null
```

**Tasks:**
- [ ] Add `com.google.zxing:core` dependency
- [ ] Implement `BarcodeEncoder` to generate QR bitmap
- [ ] Integrate into `HtmlPdfInvoiceService` (encode invoice URL or reference)
- [ ] Add optional QR flag to `InvoiceSettings`
- [ ] Tests with sample payment references

**Effort:** 2 days

---

## TODO 5 — Date Range Filtering in Analytics

**File:** `com/emul8r/bizap/ui/analytics/PaymentAnalyticsTabViewModel.kt`  
**Current code:**
```kotlin
// TODO: Filter snapshots by date range — UI has pickers but filter not wired
```

**Tasks:**
- [ ] Add `DateRange(start: Long, end: Long)` model class
- [ ] Add `getSnapshotsByDateRange(range: DateRange)` to `AnalyticsRepository`
- [ ] Wire `dateRange` StateFlow to `PaymentAnalyticsTabViewModel`
- [ ] Update Room query with `WHERE snapshot_date BETWEEN :start AND :end`
- [ ] Tests for filtering accuracy

**Effort:** 2 days

---

## TODO 6 — Banking Details in PDF

**File:** `com/emul8r/bizap/ui/invoices/html/InvoiceTemplateDataMapper.kt`  
**Current code:**
```kotlin
// TODO: Fetch BusinessProfile and map bankName, accountNumber, bsbNumber
```

**Tasks:**
- [ ] Inject `BusinessProfileRepository` into mapper
- [ ] Fetch profile in `mapToSnapshot()` and populate `bankName`, `bankAccountNumber`, `bankBsb`
- [ ] Validate on test invoices
- [ ] Unit test mapping with mock profile

**Effort:** 1 day

---

## TODO 7 — Advanced Invoice Filters

**File:** `com/emul8r/bizap/ui/gui2/invoices/InvoiceSearchAndFilter.kt`  
**Current code:**
```kotlin
// TODO: Add date range and amount range filters
```

**Tasks:**
- [ ] Add date picker Compose component
- [ ] Add range slider for amount filter
- [ ] Wire filters to existing `InvoiceListViewModel.filterInvoices()`
- [ ] Tests for filter combinations

**Effort:** 2 days

---

## TODO 8 — Analytics Drill-down

**File:** `com/emul8r/bizap/ui/analytics/RevenueAnalyticsTab.kt`  
**Current code:**
```kotlin
// TODO: Implement onDrillClick — show bottom sheet with period details
```

**Tasks:**
- [ ] Add tap listener to chart component
- [ ] Implement `onDrillClick(period: String)` callback
- [ ] Show `ModalBottomSheet` with revenue breakdown for that period

**Effort:** 1 day

---

## Status Summary

| # | Description | Priority | Status | Effort |
|---|---|---|---|---|
| 1 | Firebase Auth Integration | Critical | ⏳ Pending | 3 days |
| 2 | Event Deserialization | High | ⏳ Pending | 2 days |
| 3 | Backup/Restore Operations | High | ⏳ Pending | 3 days |
| 4 | QR Code on PDFs | Medium | ⏳ Pending | 2 days |
| 5 | Date Range Analytics | Medium | ⏳ Pending | 2 days |
| 6 | Banking Details in PDF | Medium | ⏳ Pending | 1 day |
| 7 | Advanced Invoice Filters | Medium | ⏳ Pending | 2 days |
| 8 | Analytics Drill-down | Low | ⏳ Pending | 1 day |
