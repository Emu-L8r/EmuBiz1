package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 34 → 35: Refactor analytics snapshot tables.
 *
 * Changes:
 * 1. Drop legacy `invoice_velocity_metrics` table (replaced by derived queries).
 * 2. Drop legacy `customer_revenue` table (replaced by derived queries).
 * 3. Recreate `daily_revenue_snapshots` with extended schema — adds growth metrics,
 *    multi-currency support, profile-scoped partitioning, and versioning.
 */
val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 34→35: Refactor analytics snapshot tables")

        // 1. Drop legacy tables no longer used
        database.execSQL("DROP TABLE IF EXISTS `invoice_velocity_metrics`")
        database.execSQL("DROP TABLE IF EXISTS `customer_revenue`")

        // 2. Recreate daily_revenue_snapshots with the new extended schema.
        //    Save existing rows first, then rebuild the table.
        database.execSQL("ALTER TABLE `daily_revenue_snapshots` RENAME TO `daily_revenue_snapshots_old`")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_revenue_snapshots` (
                `id`                  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `businessProfileId`   INTEGER NOT NULL,
                `dateString`          TEXT    NOT NULL,
                `dateMs`              INTEGER NOT NULL,
                `totalRevenue`        INTEGER NOT NULL,
                `invoiceCount`        INTEGER NOT NULL,
                `paidInvoiceCount`    INTEGER NOT NULL,
                `draftInvoiceCount`   INTEGER NOT NULL,
                `averageInvoiceAmount` INTEGER NOT NULL,
                `currencyBreakdown`   TEXT    NOT NULL,
                `dayOverDayGrowth`    REAL    NOT NULL,
                `weekOverWeekGrowth`  REAL    NOT NULL,
                `snapshotCreatedAtMs` INTEGER NOT NULL,
                `version`             INTEGER NOT NULL,
                `updatedAtMs`         INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Migrate existing rows from the old schema.
        // Old columns: id, businessId, date (TEXT), invoicedCents, paidCents, invoiceCount, paidCount, createdAt
        // Note: dateMs is set to 0 because the old schema stored dates as TEXT (e.g. "2026-03-17"),
        //       not as epoch milliseconds. A zero sentinel value is acceptable here since these are
        //       historical cache entries that will be refreshed on the next data sync.
        database.execSQL(
            """
            INSERT INTO `daily_revenue_snapshots`
                (id, businessProfileId, dateString, dateMs,
                 totalRevenue, invoiceCount, paidInvoiceCount, draftInvoiceCount,
                 averageInvoiceAmount, currencyBreakdown,
                 dayOverDayGrowth, weekOverWeekGrowth,
                 snapshotCreatedAtMs, version, updatedAtMs)
            SELECT
                id,
                businessId,
                date,
                0,
                invoicedCents,
                invoiceCount,
                paidCount,
                0,
                CASE WHEN invoiceCount > 0 THEN invoicedCents / invoiceCount ELSE 0 END,
                '{}',
                0.0,
                0.0,
                createdAt,
                1,
                createdAt
            FROM `daily_revenue_snapshots_old`
            """.trimIndent()
        )

        database.execSQL("DROP TABLE `daily_revenue_snapshots_old`")

        // 3. Recreate indices on the new table
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `idx_daily_business` ON `daily_revenue_snapshots` (`businessProfileId`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `idx_daily_date` ON `daily_revenue_snapshots` (`dateString`)"
        )

        Timber.i("✅ Migration 34→35 COMPLETE")
    }
}
