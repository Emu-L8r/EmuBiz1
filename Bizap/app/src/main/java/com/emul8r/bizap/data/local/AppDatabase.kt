package com.emul8r.bizap.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emul8r.bizap.data.local.entities.*
import com.emul8r.bizap.data.local.dao.*
import com.emul8r.bizap.data.local.typeconverters.DocumentStatusConverter

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
        PendingOperation::class,
        InvoiceAnalyticsSnapshot::class,
        DailyRevenueSnapshot::class,
        CustomerAnalyticsSnapshot::class,
        BusinessHealthMetrics::class,
        InvoicePaymentEntity::class,
        InvoicePaymentSnapshot::class,
        DailyPaymentSnapshot::class,
        CollectionMetrics::class,
        InvoiceTemplate::class,
        InvoiceCustomField::class
    ],
    version = 20,
    autoMigrations = [
        AutoMigration(from = 17, to = 20)
    ],
    exportSchema = false
)
@TypeConverters(DocumentStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun prefilledItemDao(): PrefilledItemDao
    abstract fun documentDao(): DocumentDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun customerAnalyticsDao(): CustomerAnalyticsDao
    abstract fun invoicePaymentDao(): InvoicePaymentDao
    abstract fun invoiceTemplateDao(): InvoiceTemplateDao
    abstract fun invoiceCustomFieldDao(): InvoiceCustomFieldDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizap.db"
                )
                    .addMigrations(
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
