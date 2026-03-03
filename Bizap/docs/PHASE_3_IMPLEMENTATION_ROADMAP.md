# PHASE 3: IMPLEMENTATION ROADMAP

This roadmap defines the 28-day sprint to complete the transformation of Bizap into an advanced, audit-ready invoicing platform.

## WEEK 1: ADVANCED MANAGEMENT CORE (Days 1-7)
**Goal:** Establish the data foundation for versioning and statuses.

*   **Day 1:** Implementation of Schema V7. Registry of `MIGRATION_6_7`.
*   **Day 2:** Domain update: `InvoiceStatus` enum expansion.
*   **Day 3:** UseCase development: `LockInvoiceUseCase` and `CreateCorrectionUseCase`.
*   **Day 4:** Repository update: Implementation of `createCorrection()` and history flows.
*   **Day 5:** UI: Branded list highlights for `OVERDUE` and `PAID` statuses.
*   **Day 6:** UI: Integration of the `InvoiceStatusHub` bottom sheet.
*   **Day 7:** Testing: Verify that correcting an invoice creates a new record and leaves the old one locked.

## WEEK 2: PAYMENT & AUDIT POLISH (Days 8-14)
**Goal:** Enable partial payments and historical snapshots.

*   **Day 8:** Implementation of `amountPaid` logic in `InvoiceDetailViewModel`.
*   **Day 9:** UI: Progress bars for `PARTIALLY_PAID` invoices.
*   **Day 10:** PDF Engine update: Adapting `InvoicePdfService` to show "Balance Remaining".
*   **Day 11:** Snapshot logic: Ensuring `amountPaid` is frozen in the Vault record.
*   **Day 12:** Audit Trail UI: A "History" tab in Invoice Detail to see parent versions.
*   **Day 13:** Bug bash: Stress testing the correction flow.
*   **Day 14:** Milestone Review: Verified Audit-Ready Management.

## WEEK 3: MULTI-CURRENCY FOUNDATION (Days 15-21)
**Goal:** Decouple the system from the hardcoded "$" symbol.

*   **Day 15:** Implementation of Schema V8. Registry of `MIGRATION_7_8`.
*   **Day 16:** Business Profile update: `currencyCode` and `currencySymbol` storage.
*   **Day 17:** Utility: Development of `CurrencyFormatter` engine.
*   **Day 18:** Global UI Audit: Replace all "$$$" strings with dynamic formatting.
*   **Day 19:** Settings: Implementation of the Currency Selector dropdown.
*   **Day 20:** PDF Engine update: Injecting `currencySymbol` into `PdfTableRenderer`.
*   **Day 21:** Testing: Verify app-wide switch to "€" or "£" without data corruption.

## WEEK 4: INTEGRATION & STABILITY (Days 22-28)
**Goal:** Finalize the Fail-Proof Enterprise System.

*   **Day 22:** Backward compatibility sweep for v6 legacy data.
*   **Day 23:** Edge case testing: Handling long currency symbols (e.g., "AUD$").
*   **Day 24:** Unit Testing: `CurrencyFormatter` and `BalanceCalculation` tests.
*   **Day 25:** Performance Tuning: Optimizing large list renders with new status types.
*   **Day 26:** Final Visual Polish: Accent colors for currency symbols.
*   **Day 27:** Documentation update: Final `READY_TO_TEST_SUMMARY` for Phase 3.
*   **Day 28:** **PHASE 3 SIGN-OFF.**

---

## RISK MITIGATION
1.  **Migration Failure:** `fallbackToDestructiveMigration` remains active only during dev days.
2.  **Precision Loss:** Double types are used for now; move to `BigDecimal` in Phase 4 if financial audits require it.
3.  **UI Clutter:** The `InvoiceStatusHub` will be simplified to keep the app "Snappy."
