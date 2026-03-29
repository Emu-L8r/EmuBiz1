package com.emul8r.bizap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emul8r.bizap.data.local.entities.*
import com.emul8r.bizap.data.local.entity.NoteEntity
import com.emul8r.bizap.data.local.dao.*
import com.emul8r.bizap.data.local.typeconverters.DocumentStatusConverter
import com.emul8r.bizap.data.local.typeconverters.LocalDateTypeConverter
import com.emul8r.bizap.data.local.typeconverters.LocalDateTimeTypeConverter
import com.emul8r.bizap.data.local.typeconverters.UUIDTypeConverter

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
        AnalyticsEventEntity::class  // NEW: Event tracking
    ],
    version = 37,  // v36→37: Remove UNIQUE constraint on email (optional field)
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
    abstract fun analyticsEventDao(): AnalyticsEventDao  // NEW: Event tracking
}
