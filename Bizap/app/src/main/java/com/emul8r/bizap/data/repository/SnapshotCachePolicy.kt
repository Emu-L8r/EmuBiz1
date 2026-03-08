package com.emul8r.bizap.data.repository

/**
 * Documents the intended role of snapshot tables in the Bizap architecture.
 *
 * ## Snapshot Tables
 *
 * The following snapshot tables exist for optional performance caching:
 * - `invoice_analytics_snapshots` ([com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot])
 * - `daily_revenue_snapshots` ([com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot])
 * - `invoice_payment_snapshots` ([com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot])
 *
 * ## Policy Rules
 *
 * 1. **Snapshots are NEVER the source of truth for financial calculations.**
 *    All revenue, outstanding balance, and payment metric queries MUST go through
 *    [com.emul8r.bizap.data.local.dao.InvoiceDaoV2] or [com.emul8r.bizap.data.local.InvoiceDao].
 *
 * 2. **Snapshots are written as a write-through cache only.**
 *    They are updated whenever an invoice is created, updated, or a payment is recorded.
 *    Failures to update snapshots are logged and retried — they never block the main operation.
 *
 * 3. **Dashboards must use V2 repositories.**
 *    - Revenue Dashboard → [com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2]
 *    - Payment Analytics → [com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2]
 *    - Risk Dashboard → [com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2]
 *    - GUI1/GUI2 unified → [AnalyticsRepositoryBridge]
 *
 * 4. **Snapshot staleness is tolerated up to 24 hours.**
 *    The [com.emul8r.bizap.data.worker.SnapshotRepairWorker] repairs inconsistencies daily.
 *    The [com.emul8r.bizap.data.consistency.DataConsistencyValidator] detects discrepancies > 1¢.
 *
 * 5. **Snapshot health is surfaced to the user when stale data is detected.**
 *    See [com.emul8r.bizap.ui.health.SnapshotHealthViewModel] and related UI components.
 *
 * ## Rationale
 *
 * Snapshots were originally the primary source for dashboard metrics, causing the
 * "split personality" bug where dashboards showed $0 when snapshots were empty or stale.
 * This policy ensures all financial data flows through a single source of truth:
 * the `invoices` table queried directly via Room DAOs.
 */
object SnapshotCachePolicy {
    /**
     * Maximum age (in milliseconds) before a snapshot is considered stale.
     * After this threshold, the [com.emul8r.bizap.data.consistency.DataConsistencyValidator]
     * will flag the snapshot as potentially inconsistent.
     */
    const val MAX_SNAPSHOT_AGE_MS = 24 * 60 * 60 * 1000L  // 24 hours

    /**
     * Tolerance in cents for consistency checks between snapshot and calculated totals.
     * A difference of 1¢ or less is considered consistent (floating-point rounding).
     */
    const val CONSISTENCY_TOLERANCE_CENTS = 1L

    /**
     * Whether snapshots should be used as the primary source for dashboards.
     * MUST always be false — dashboards use V2 repositories directly.
     */
    const val USE_SNAPSHOTS_FOR_DASHBOARDS = false
}
