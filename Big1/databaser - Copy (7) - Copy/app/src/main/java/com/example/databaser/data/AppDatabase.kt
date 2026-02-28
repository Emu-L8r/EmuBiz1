package com.example.databaser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SupportFactory

const val DATABASE_NAME = "app_database"

@Database(entities = [Customer::class, Invoice::class, LineItem::class, PredefinedLineItem::class, BusinessInfo::class, Note::class, Quote::class], version = 29, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun lineItemDao(): LineItemDao
    abstract fun predefinedLineItemDao(): PredefinedLineItemDao
    abstract fun businessInfoDao(): BusinessInfoDao
    abstract fun noteDao(): NoteDao
    abstract fun quoteDao(): QuoteDao

    suspend fun clearAllDataExceptSettings() {
        withContext(Dispatchers.IO) {
            customerDao().deleteAll()
            invoiceDao().deleteAll()
            lineItemDao().deleteAll()
            predefinedLineItemDao().deleteAll()
            noteDao().deleteAll()
            quoteDao().deleteAll()
        }
    }

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context, passphraseManager: PassphraseManager): AppDatabase {
            return Instance ?: synchronized(this) {
                val passphrase = passphraseManager.getPassphrase()?.toByteArray(Charsets.ISO_8859_1)
                val factory = SupportFactory(passphrase)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration(true)
                    .build()
                Instance = instance
                instance
            }
        }
    }
}
