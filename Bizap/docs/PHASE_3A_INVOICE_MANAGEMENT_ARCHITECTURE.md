# PHASE 3A: ADVANCED INVOICE MANAGEMENT ARCHITECTURE

This document defines the structural and logical requirements for transitioning the Bizap invoicing engine from basic status tracking to a professional-grade audit-ready management system.

## 1. VISION: THE DETERMINISTIC AUDIT TRAIL
In Phase 3A, we move away from "mutating" invoices. Instead, we implement a **Snap-and-New** strategy. 
*   **Originals are Immutable:** Once an invoice is marked as `SENT`, it is locked.
*   **Versioned History:** If a mistake is found, the system creates a *new* invoice record derived from the original, preserving the audit trail of the previous version.

---

## 2. DATABASE SCHEMA EVOLUTION (V7)

### 2.1 Extended Status Workflow
We are expanding the `InvoiceStatus` from 3 states to 5 to handle real-world business scenarios.

```kotlin
enum class InvoiceStatus {
    DRAFT,          // Editable, no PDF in vault
    SENT,           // Locked, PDF in vault, awaiting payment
    PAID,           // Locked, fully settled
    OVERDUE,        // Locked, past due date
    PARTIALLY_PAID  // Locked, partial amount received
}
```

### 2.2 New Entity Fields
The `InvoiceEntity` will be updated to support partial payments and parent-child relationships for versioning.

| Field | Type | Description |
|-------|------|-------------|
| `amountPaid` | Double | The total amount received for this invoice. |
| `parentInvoiceId` | Long? | Reference to the original invoice if this is a corrected version. |
| `version` | Int | Incremental counter for corrections (default 1). |

### 2.3 Migration SQL (MIGRATION_6_7)
```sql
-- Add payment tracking
ALTER TABLE invoices ADD COLUMN amountPaid REAL NOT NULL DEFAULT 0.0;

-- Add versioning support
ALTER TABLE invoices ADD COLUMN parentInvoiceId INTEGER;
ALTER TABLE invoices ADD COLUMN version INTEGER NOT NULL DEFAULT 1;

-- Create index for version history lookups
CREATE INDEX index_invoices_parentInvoiceId ON invoices(parentInvoiceId);
```

---

## 3. DOMAIN LAYER: BUSINESS RULES

### 3.1 The "Immutability Rule"
A UseCase `LockInvoiceUseCase` will be created.
*   **Condition:** If `status != DRAFT`, all `update` operations must be rejected.
*   **Exception:** Only `status` and `amountPaid` can be updated on non-draft invoices.

### 3.2 The "Correction Flow"
A UseCase `CreateCorrectionInvoiceUseCase` will be created.
1.  Takes an existing `SENT` or `OVERDUE` invoice.
2.  Creates a new `Invoice` object with `parentInvoiceId = original.id`.
3.  Increments `version = original.version + 1`.
4.  Sets status back to `DRAFT` for the new copy.

---

## 4. UI LAYER SPECIFICATIONS

### 4.1 Invoice List Enhancements
*   **Overdue Highlighting:** Invoices where `status == SENT && currentDate > dueDate` will be visually flagged as `OVERDUE`.
*   **Progress Bars:** For `PARTIALLY_PAID` invoices, show a linear progress bar indicating `% Paid`.

### 4.2 Status Management Hub
A new bottom sheet component `InvoiceStatusHub` allowing the user to:
1.  Log a payment (updates `amountPaid`).
2.  Mark as fully paid.
3.  Trigger a "Create Correction" action.

---

## 5. REPOSITORY ALIGNMENT

The `InvoiceRepository` will be enhanced with:
```kotlin
interface InvoiceRepository {
    // ... existing methods ...
    suspend fun updateAmountPaid(invoiceId: Long, amount: Double)
    fun getVersionHistory(parentInvoiceId: Long): Flow<List<Invoice>>
    suspend fun createCorrection(originalInvoiceId: Long): Long
}
```

---

## 6. INTEGRATION WITH PHASE 2B
The `InvoiceSnapshot` will be updated to include `amountPaid` and `version`, ensuring the PDF always displays the current payment status (e.g., "Balance Remaining: $50.00").

**Next Step:** Proceed to `PHASE_3B_MULTICURRENCY_ARCHITECTURE.md`.
