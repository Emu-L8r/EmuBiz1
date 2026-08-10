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
import com.emul8r.bizap.data.local.migration.MIGRATION_42_43
import com.emul8r.bizap.data.local.migrations.MIGRATION_44_45
import com.emul8r.bizap.data.local.migrations.Migration_45_46
import com.emul8r.bizap.data.local.database.MIGRATION_46_47
import com.emul8r.bizap.data.local.database.MIGRATION_47_48
import com.emul8r.bizap.data.local.database.MIGRATION_48_49
import com.emul8r.bizap.data.local.typeconverters.DocumentStatusConverter
import com.emul8r.bizap.data.local.typeconverters.LocalDateTypeConverter
import com.emul8r.bizap.data.local.typeconverters.LocalDateTimeTypeConverter
import com.emul8r.bizap.data.local.typeconverters.UUIDTypeConverter

/**
 * # Database Schema & Migration Architecture
 *
 * **Current Version:** 46
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
 * | 42 | 43 | Add customization layers | migration/Migration_42_43.kt ⚠️ |
 * | 44 | 45 | Add invoice numbering | migrations/Migration_44_45.kt ✅ |
 * | 45 | 46 | Add payment media attachments | migrations/Migration_45_46.kt ✅ |
 *
 * | 46 | 47 | Add query optimization indices (Phase 2B) | database/Migrations.kt ✅ |
 * | 47 | 48 | Finalize invoice_settings schema (Phase 3F) | database/Migration_47_48.kt ✅ |
 * **Current Version:** 48
 * ## Naming Convention
 * - **Standardized (New):** `Migration_XX_YY.kt` (file) + `Migration(XX, YY)` (class)
 * **⚠️ Future Action:** Rename the `migration/` folder to `migrations/` and rename
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
        // Core Business Entities
        BusinessProfileEntity::class,
        CustomerEntity::class,

        // Invoice Entities (GUI1)
        InvoiceEntity::class,
        LineItemEntity::class,
        PrefilledItemEntity::class,

        // Invoice Entities (GUI2)
        InvoiceItemEntity::class,
        PaymentEntity::class,

        // Document & Generated Content
        GeneratedDocumentEntity::class,

        // Currency Management
        CurrencyEntity::class,
        ExchangeRateEntity::class,

        // Analytics Snapshots
        InvoiceAnalyticsSnapshot::class,
        DailyRevenueSnapshot::class,
        CustomerAnalyticsSnapshot::class,
        BusinessHealthMetrics::class,

        // Payment Tracking & Snapshots
        InvoicePaymentEntity::class,
        InvoicePaymentSnapshot::class,
        DailyPaymentSnapshot::class,
        CollectionMetrics::class,
        PaymentMediaAttachment::class,  // ✅ NEW: Payment proof media

        // Templates & Customization
        InvoiceTemplate::class,
        InvoiceCustomField::class,

        // Offline Operations
        PendingOperationEntity::class,
        OfflineOperation::class,

        // Notes & Events
        NoteEntity::class,
        AnalyticsEventEntity::class,

        // Search Index
        InvoiceFTS::class,

        // Settings & Preferences
        com.emul8r.bizap.domain.model.InvoiceSettings::class,
        DashboardPreferencesEntity::class
    ],
    version = 49,  // v48→49: Add watermark_image column + enable_brand_watermark safety net
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
    abstract fun paymentMediaAttachmentDao(): PaymentMediaAttachmentDao  // ✅ NEW
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
