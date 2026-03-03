# PHASE 3: INTEGRATION LAYER SPECIFICATION

This document outlines how the Advanced Invoice Management (3A) and Multi-Currency (3B) modules integrate into the existing Bizap core without breaking Phase 2B's professional rendering engine.

## 1. THE CONVERGED DOMAIN MODEL
The `Invoice` domain model becomes the central integration point. 

```kotlin
data class Invoice(
    // ... Phase 2B fields ...
    val amountPaid: Double = 0.0,        // Integration 3A
    val parentInvoiceId: Long? = null,   // Integration 3A
    val version: Int = 1,                // Integration 3A
    val currencySymbol: String = "$"     // Integration 3B
) {
    val balanceRemaining: Double get() = totalAmount - amountPaid
    val isFullyPaid: Boolean get() = balanceRemaining <= 0.0
}
```

## 2. UPDATED SNAPSHOT PATTERN
The `InvoiceSnapshot` must be expanded to ensure the PDF engine (Phase 2B) can render payment status and correct currency symbols.

```kotlin
data class InvoiceSnapshot(
    // ... existing fields ...
    val amountPaid: Double,
    val balanceRemaining: Double,
    val currencySymbol: String,
    val version: Int
)
```

## 3. PDF ENGINE ADAPTATION (`InvoicePdfService`)
The `PdfTableRenderer` and `InvoicePdfService` require specific logic changes:

1.  **Symbol Injection:** All `String.format` calls in the PDF service must replace the hardcoded `$` with `snapshot.currencySymbol`.
2.  **Balance Summary:** A new section below "TOTAL DUE" will be added:
    *   If `amountPaid > 0`: Show "Amount Paid" and "Balance Remaining".
    *   If `amountPaid == 0`: Only show "Total Amount Due".

## 4. MIGRATION STRATEGY: THE "STEPPED" ASCENT
To ensure zero data loss, migrations will be executed in a specific order:

1.  **V6 to V7:** Execute `MIGRATION_6_7` (Management fields).
2.  **V7 to V8:** Execute `MIGRATION_7_8` (Currency fields).
3.  **Hilt Alignment:** Update `RepositoryModule` to bind the new management methods.

## 5. BACKWARD COMPATIBILITY
*   **Legacy Invoices (v6 and below):**
    *   Will default to `amountPaid = 0.0`.
    *   Will default to `currencySymbol = "$"`.
    *   Will default to `version = 1`.
*   This ensures that opening an old invoice in the new UI or re-generating its PDF (Phase 2B) works seamlessly without crashes.

**Next Step:** Proceed to `PHASE_3_IMPLEMENTATION_ROADMAP.md`.
