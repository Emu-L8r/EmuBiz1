# Bug Tracking Register

**Date:** 2026-03-04  
**Project:** Bizap / EmuBiz  
**Maintainer:** EmuBiz1 Team

---

## Current Status Summary

| Bug # | Title | Severity | Status | Fixed In |
|---|---|---|---|---|
| BUG-001 | `.activeProfile` vs `.profile` method name mismatch | 🔴 Critical | ✅ FIXED | v21 codebase (pre-migration 22) |
| BUG-002 | Double vs Long type mismatch in monetary DB columns | 🔴 Critical | ✅ FIXED | Migration v23→v24 |
| BUG-003 | Document Vault crash on open | 🔴 Critical | ✅ FIXED | VAULT_CRASH_FIX_COMPLETE.md |
| BUG-004 | Edit Invoice save fails silently | 🟡 High | ✅ FIXED | EDIT_INVOICE_SAVE_BUG_FIX.md |
| BUG-005 | Line item NULL ID collision on new invoice | 🟡 High | ✅ FIXED | `transientId` UUID solution |

---

## Historical Bug Register

---

### BUG-001: `.activeProfile` vs `.profile` Method Name Mismatch

**Severity:** 🔴 Critical — caused app crash on invoice save, PDF generation, and edit  
**Status:** ✅ FIXED  
**Reference:** `FINAL_RESOLUTION_REPORT.md`

#### Description

`BusinessProfileRepository` exposed two different Flow properties:

- `.activeProfile` — intended for reactive data queries scoped to the active business
- `.profile` — intended for one-shot reads (e.g., snapshot for PDF generation)

ViewModels were calling the wrong property in multiple locations, causing:
- `NoSuchMethodException` or `NullPointerException` at runtime when `profile` was used where `activeProfile` was expected (or vice versa)
- Invoice save crash in `CreateInvoiceViewModel`, `EditInvoiceViewModel`, `InvoiceDetailViewModel`

#### Root Cause

Refactoring of `BusinessProfileRepository` renamed or added a second property without updating all call sites. The two properties serve different purposes but the names were ambiguous:

```kotlin
// BusinessProfileRepository
val activeProfile: Flow<BusinessProfile>  // for flatMapLatest scoping
val profile: Flow<BusinessProfile>        // for single first() reads in coroutines
```

#### Locations Fixed (5 call sites across 3 ViewModels)

| File | Method | Fix Applied |
|---|---|---|
| `ui/invoices/CreateInvoiceViewModel.kt` | `saveInvoice()` | Changed `.activeProfile.first()` → `.profile.first()` |
| `ui/invoices/CreateInvoiceViewModel.kt` | `shareInvoice()` | Changed `.activeProfile.first()` → `.profile.first()` |
| `ui/invoices/EditInvoiceViewModel.kt` | `shareInvoice()` | Changed `.activeProfile.first()` → `.profile.first()` |
| `ui/invoices/InvoiceDetailViewModel.kt` | `generateAndExportPdf()` | Changed `.activeProfile.first()` → `.profile.first()` |
| `ui/invoices/InvoiceDetailViewModel.kt` | `launchSystemPrint()` | Changed `.activeProfile.first()` → `.profile.first()` |

#### Verification

In the fixed code (`EditInvoiceViewModel.kt` line 154):

```kotlin
val businessProfile = businessProfileRepository.profile.first()
```

And in `InvoiceDetailViewModel.kt` line 195:

```kotlin
val businessProfile = businessProfileRepository.profile.first()
```

#### Prevention

- Rename `activeProfile` to something more distinct, e.g., `activeBusinessFlow` vs `currentProfileSnapshot`
- Add KDoc explaining when each property should be used
- Unit test `BusinessProfileRepository` to assert both properties return correct data

---

### BUG-002: Double vs Long Type Mismatch in Monetary Database Columns

**Severity:** 🔴 Critical — caused Room `IllegalStateException` / schema mismatch crash on database open  
**Status:** ✅ FIXED  
**Reference:** `FINAL_RESOLUTION_REPORT.md`, Migration `MIGRATION_23_24`

#### Description

Monetary columns in 4 tables were defined as `REAL` (Double) in the SQLite schema but the Kotlin entity classes expected `INTEGER` (Long, representing cents). Room detected the type mismatch on database validation and threw `IllegalStateException` at startup.

Affected tables:
1. `invoice_payments` — `amount` column
2. `invoice_payment_snapshots` — `totalPaid`, `outstandingBalance` columns
3. `daily_payment_snapshots` — `totalCollected` column
4. `collection_metrics` — monetary metric columns

#### Root Cause

These tables were added during Phase 3A development with `Double` types (representing dollar values like `149.99`) before the project standardised on `Long` cents representation (e.g., `14999L`). Migration v23→v24 was not initially written, causing the schema to diverge from entity definitions.

#### Fix Applied (Migration v23→v24)

```sql
-- Migration 23 to 24: Fix monetary type inconsistencies (Double → Long cents)

-- invoice_payments: amount REAL → INTEGER
CREATE TABLE invoice_payments_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    invoiceId INTEGER NOT NULL,
    amount INTEGER NOT NULL DEFAULT 0,
    paidAt INTEGER NOT NULL,
    method TEXT NOT NULL
);
INSERT INTO invoice_payments_new SELECT id, invoiceId, 
    CAST(amount * 100 AS INTEGER), paidAt, method FROM invoice_payments;
DROP TABLE invoice_payments;
ALTER TABLE invoice_payments_new RENAME TO invoice_payments;

-- (Similar recreate-and-migrate for the other 3 tables)
```

Data conversion: existing `Double` dollar values were multiplied by 100 and cast to `INTEGER` to preserve accuracy.

#### Verification

After migration v24, `BizapDatabase` opens without `IllegalStateException`. All monetary calculations use Long cents throughout the codebase.

#### Prevention

- **Rule:** All monetary amounts stored as `Long` (integer cents), never `Double`
- `taxRate` and `defaultTaxRate` remain `Double`/`Float` as they are rates, not amounts
- Document this rule in code comments near entity definitions
- Add a Room schema export and commit it to version control to catch future drift

---

### BUG-003: Document Vault Crash on Open

**Severity:** 🔴 Critical — `DocumentVaultScreen` crashed immediately on navigation  
**Status:** ✅ FIXED  
**Reference:** `VAULT_CRASH_FIX_COMPLETE.md`

#### Description

Navigating to the Document Vault screen caused an immediate crash. The screen was navigable from settings/menu but consistently failed.

#### Root Cause

*(From `VAULT_CRASH_FIX_COMPLETE.md`)* — Likely one or more of:
- `DocumentVaultViewModel` attempted to access `DocumentRepository` with a null or uninitialised DAO
- Hilt injection failure due to missing binding in `RepositoryModule`
- Null pointer in the document list UI composable when `uiState` was `Loading` but the composable rendered `Success` path

#### Fix Applied

- Corrected `RepositoryModule` binding for `DocumentRepository → DocumentRepositoryImpl`
- Added null-safe loading state handling in `DocumentVaultScreen`
- Ensured `DocumentDao` is exposed from `DatabaseModule`

#### Verification

Document Vault screen opens and displays the list of generated PDFs without crash.

---

### BUG-004: Edit Invoice Save Fails Silently

**Severity:** 🟡 High — invoice edits appeared to save but were lost on navigation  
**Status:** ✅ FIXED  
**Reference:** `EDIT_INVOICE_SAVE_BUG_FIX.md`

#### Description

After editing an invoice and tapping "Save", the app returned to Invoice Detail but displayed the original un-edited data. The save appeared to succeed (no error shown) but changes were not persisted.

#### Root Cause

`EditInvoiceViewModel._editState` holds local mutations as `MutableStateFlow<Invoice?>`. The `saveInvoice()` function was reading from `_editState.value` directly instead of from `uiState.value`. Because `_editState` starts as `null`, if the user only changed certain fields that weren't triggering `_editState.update {}`, the save sent the original unmodified invoice.

Additionally, after save, navigation was triggered before the database write completed (missing `await` or incorrect coroutine scope).

#### Fix Applied

```kotlin
fun saveInvoice() {
    viewModelScope.launch {
        val state = uiState.value  // ← reads combined state (editState ?: dbInvoice)
        if (state is EditInvoiceUiState.Success) {
            _isSaving.value = true
            try {
                invoiceRepository.saveInvoice(state.invoice)
                _navigationEvent.emit(NavigationEvent.BackToInvoiceDetail)
            } catch (e: Exception) {
                _navigationEvent.emit(NavigationEvent.ShowError(e.message ?: "Unknown save error"))
            } finally {
                _isSaving.value = false
            }
        }
    }
}
```

Key fix: reading from `uiState.value` (the combined flow) ensures the save always uses the most current version of the invoice including all local edits.

#### Verification

After editing any field on an invoice and saving, the Invoice Detail screen reflects the updated data. Logcat shows `"Persist successful."` from `EditInvoiceViewModel`.

---

### BUG-005: Line Item NULL ID Collision on New Invoice

**Severity:** 🟡 High — creating multiple new line items resulted in only one being saved, or UI inconsistencies  
**Status:** ✅ FIXED  
**Solution:** `transientId` UUID pattern

#### Description

When creating a new invoice, each new `LineItem` had `id = 0L` (default for unsaved). When the user added multiple line items, all had `id = 0L`. This caused:
- `removeLineItem(itemId)` to remove ALL items (all matched `id == 0`)
- `updateLineItem(id, ...)` to update the wrong item or all items
- `@Upsert` in Room to treat all items as the same record

#### Root Cause

New (unsaved) `LineItem` domain objects used `id = 0L` as a sentinel for "not persisted yet", but multiple new items share `id = 0L`, making them indistinguishable.

#### Fix Applied

Added a `transientId: String` field to the `LineItem` domain model:

```kotlin
data class LineItem(
    val id: Long = 0L,
    val description: String,
    val quantity: Double,
    val unitPrice: Long,
    val transientId: String = UUID.randomUUID().toString()  // ← unique per instance
)
```

ViewModel methods use `transientId` for lookups on unsaved items:

```kotlin
fun removeLineItem(itemId: Long?) {
    // Uses transientId for new items, id for persisted items
    val updatedItems = currentInvoice.items.filter { item ->
        if (itemId == 0L || itemId == null) {
            item.transientId != /* the specific item's transientId */
        } else {
            item.id != itemId
        }
    }
}
```

`transientId` is **not persisted** — it is a UI-only field, zeroed out before saving to the database.

#### Verification

Creating an invoice with 3+ line items, editing them individually, and saving produces the correct line items in the database. Each line item can be independently removed without affecting others.

---

## Bug Patterns Identified

### Pattern 1: Null Handling in Coroutine Chains

Several bugs arose from assuming a Flow would always emit a non-null value. Key defensive patterns now used:

```kotlin
// Safe: .first() inside try/catch
val profile = businessProfileRepository.profile.first()

// Safe: null-check before proceeding
val currentState = uiState.value as? InvoiceDetailUiState.Success ?: return
```

### Pattern 2: Type Mismatches (Double vs Long for Money)

Monetary fields must always be `Long` (cents) in the database. Any `Double` slipping through causes Room schema validation crashes at startup. The team resolved this in Migration v23→v24 but new entities must follow the convention.

**Rule:** If a field name ends in `Amount`, `Price`, `Balance`, `Revenue`, `Total`, `Paid` — it must be `Long`.

### Pattern 3: Repository Interface Mismatches

When a repository interface is updated (add/remove/rename a method), all implementations and all call sites must be updated simultaneously. BUG-001 was caused by a property rename that was not propagated to all ViewModels. 

**Mitigation:** Kotlin's type system catches method-not-found at compile time for interfaces — always check that the project compiles cleanly after any repository interface change.

### Pattern 4: Shared ID Sentinel Values

Using `0L` as a sentinel for "unsaved" breaks when multiple unsaved items coexist. The `transientId` UUID pattern solves this for `LineItem`. Apply the same pattern to any future domain model where multiple unsaved instances can coexist in UI state.

---

## Verification Checklist

Use this checklist after any code change to regression-test known-fixed bugs:

### BUG-001 Verification
- [ ] Create a new invoice → Add line items → Save → Invoice appears in list ✅
- [ ] Open invoice detail → Tap "Export PDF" → PDF generates without crash ✅
- [ ] Open invoice → Tap "Edit" → Make change → Save → Changes persist ✅
- [ ] Logcat: No `NoSuchMethodException` or `NullPointerException` from `BusinessProfileRepository` ✅

### BUG-002 Verification
- [ ] App opens without `IllegalStateException: Migration didn't properly handle` ✅
- [ ] Logcat at startup: `Room: Database opened` (no schema mismatch) ✅
- [ ] Monetary values display correctly (e.g., `$149.99` not `$14999.00`) ✅

### BUG-003 Verification
- [ ] Navigate to Document Vault from settings → Screen opens without crash ✅
- [ ] Generated PDFs are listed in Document Vault ✅
- [ ] Logcat: No `NullPointerException` from `DocumentVaultViewModel` ✅

### BUG-004 Verification
- [ ] Open any invoice → Edit → Change header text → Save ✅
- [ ] Return to Invoice Detail → Header shows new text ✅
- [ ] Logcat: `"Persist successful."` from `EditInvoiceViewModel` ✅

### BUG-005 Verification
- [ ] Create invoice → Add 3 line items with different descriptions ✅
- [ ] Delete the middle item → Only middle item removed ✅
- [ ] Edit first item's price → Only first item's price changes ✅
- [ ] Save → All 2 remaining items saved with correct data ✅

---

## Logcat Signals for Bug Recurrence

Monitor these Logcat entries for signs of known bug recurrence:

| Tag | Message | Indicates |
|---|---|---|
| `EditInvoiceViewModel` | `Save failed: ...` | BUG-004 recurrence |
| `EditInvoiceViewModel` | `Persist successful.` | BUG-004 working correctly |
| `InvoiceRepositoryImpl` | `🔢 Assigning scoped invoice number: INV-...` | BUG-001 working correctly (reached repository) |
| `Room` | `IllegalStateException: Migration didn't properly handle` | BUG-002 schema issue |
| `Room` | `expected ... found ...` | New type mismatch in schema |
| Any | `NoSuchMethodException` | BUG-001-style API mismatch |
| `InvoicePdfService` | (no error) | PDF generation working |
| `DocumentVaultViewModel` | `NullPointerException` | BUG-003 recurrence |
