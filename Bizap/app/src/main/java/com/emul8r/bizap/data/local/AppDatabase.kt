package com.emul8r.bizap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emul8r.bizap.data.local.entities.BusinessHealthMetrics
import com.emul8r.bizap.data.local.entities.BusinessProfileEntity
import com.emul8r.bizap.data.local.entities.CurrencyEntity
import com.emul8r.bizap.data.local.entities.CustomerAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.data.local.entities.DailyRevenueSnapshot
import com.emul8r.bizap.data.local.entities.ExchangeRateEntity
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.data.local.entities.InvoiceAnalyticsSnapshot
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.LineItemEntity
import com.emul8r.bizap.data.local.entities.PendingOperation
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
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
        BusinessHealthMetrics::class
    ],
    version = 14, // INCREMENTED: Added analytics tables
    exportSchema = true
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
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14 // Register Analytics Foundation
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
