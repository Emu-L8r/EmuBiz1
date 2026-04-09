package com.emul8r.bizap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emul8r.bizap.data.local.entities.*
import com.emul8r.bizap.data.local.entity.NoteEntity
import com.emul8r.bizap.data.local.dao.*
import com.emul8r.bizap.data.local.migration.MIGRATION_AddInvoiceSettings
import com.emul8r.bizap.data.local.migration.MIGRATION_AddPdfEngineAndLayout
import com.emul8r.bizap.data.local.migration.MIGRATION_AddSignatureField
import com.emul8r.bizap.data.local.typeconverters.DocumentStatusConverter
import com.emul8r.bizap.data.local.typeconverters.LocalDateTypeConverter
import com.emul8r.bizap.data.local.typeconverters.LocalDateTimeTypeConverter
import com.emul8r.bizap.data.local.typeconverters.UUIDTypeConverter

/**
 * # Database Schema & Migration Architecture
 *
 * **Current Version:** 42
 * **Location:** data/local/migrations/ (standardized)
 *
 * ## Migration History (Version Chain)
 *
 * | From | To | Description | Location |
 * |------|----|----|----------|
 * | 21 | 22 | (legacy) | migrations/Migration_21_22.kt |
 * | ... | ... | (v22-36 chain) | migrations/Migration_*_*.kt |
 * | 36 | 37 | (legacy) | migrations/Migration_36_37.kt |
 * | 37 | 38 | Add invoice_settings table | migration/MIGRATION_AddInvoiceSettings.kt ⚠️ |
 * | 38 | 39 | (intermediate) | migrations/Migration_38_39.kt |
 * | 39 | 40 | Add PDF engine & layout | migration/MIGRATION_AddPdfEngineAndLayout.kt ⚠️ |
 * | 40 | 41 | Add signature field | migration/MIGRATION_AddSignatureField.kt ⚠️ |
 * | 41 | 42 | Add discount + FTS4 | migration/Migration_41_42.kt ⚠️ |
 *
 * **⚠️ Future Action:** Rename the `migration/` folder to `migrations/` and rename
 * MIGRATION_* classes to Migration_*_* for consistency (post-launch cleanup).
 *
 * ## Naming Convention
 * - **Standardized (New):** `Migration_XX_YY.kt` (file) + `Migration(XX, YY)` (class)
 * - **Legacy (Old):** `Migration_XX_YY.kt` (file) + `Migration(XX, YY)` (class)
 * - **Transitional (Mixed):** Some descriptive names remain (will be renamed)
 *
 * For new migrations:
 * ```kotlin
 * object Migration_42_43 : Migration(42, 43) {
 *     override fun migrate(database: SupportSQLiteDatabase) { ... }
 * }
 * ```
 *
 * ## Rollback Support
 * Room does NOT support downgrading versions. If you need to test a migration:
 * 1. Uninstall the app completely
 * 2. Verify on a fresh install
 * 3. Or test on separate emulator instance
 */
@Database(
    entities = [
        CustomerEntity::class,
        InvoiceEntity::class,
        LineItemEntity::class,
        PrefilledItemEntity::class,
        GeneratedDocumentEntity::class,
        BusinessProfileEntity::class,
        CurrencyEntity::class,
        ExchangeRateEntity::class,
        InvoiceAnalyticsSnapshot::class,
        DailyRevenueSnapshot::class,
        CustomerAnalyticsSnapshot::class,
        BusinessHealthMetrics::class,
        InvoicePaymentEntity::class,
        InvoicePaymentSnapshot::class,
        DailyPaymentSnapshot::class,
        CollectionMetrics::class,
        InvoiceTemplate::class,
        InvoiceCustomField::class,
        PendingOperationEntity::class,
        OfflineOperation::class,
        InvoiceItemEntity::class,
        PaymentEntity::class,
        NoteEntity::class,
        AnalyticsEventEntity::class,  // Event tracking
        com.emul8r.bizap.domain.model.InvoiceSettings::class,  // Invoice settings
        com.emul8r.bizap.data.local.entities.InvoiceFTS::class  // Full-text search
    ],
    version = 42,  // v41→42: Add discount_amount column; create InvoiceFTS virtual table
    exportSchema = true
)
@TypeConverters(
    DocumentStatusConverter::class,
    LocalDateTypeConverter::class,
    LocalDateTimeTypeConverter::class,
    UUIDTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun prefilledItemDao(): PrefilledItemDao
    abstract fun documentDao(): DocumentDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun customerAnalyticsDao(): CustomerAnalyticsDao
    abstract fun invoicePaymentDao(): InvoicePaymentDao
    abstract fun invoiceTemplateDao(): InvoiceTemplateDao
    abstract fun invoiceCustomFieldDao(): InvoiceCustomFieldDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun offlineOperationDao(): OfflineOperationDao
    abstract fun invoiceDaoV2(): InvoiceDaoV2
    abstract fun customerDaoV2(): CustomerDaoV2
    abstract fun paymentDaoV2(): PaymentDaoV2
    abstract fun noteDao(): NoteDao
    abstract fun analyticsEventDao(): AnalyticsEventDao  // Event tracking
    abstract fun invoiceSettingsDao(): InvoiceSettingsDao  // Invoice settings
}
