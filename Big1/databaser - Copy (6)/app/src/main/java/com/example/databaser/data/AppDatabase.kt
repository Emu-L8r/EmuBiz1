package com.example.databaser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

const val DATABASE_NAME = "app_database"

@Database(entities = [Customer::class, Invoice::class, LineItem::class, PredefinedLineItem::class, BusinessInfo::class, Note::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun lineItemDao(): LineItemDao
    abstract fun predefinedLineItemDao(): PredefinedLineItemDao
    abstract fun businessInfoDao(): BusinessInfoDao
    abstract fun noteDao(): NoteDao

    suspend fun clearAllDataExceptSettings() {
        withContext(Dispatchers.IO) {
            customerDao().deleteAll()
            invoiceDao().deleteAll()
            lineItemDao().deleteAll()
            predefinedLineItemDao().deleteAll()
            noteDao().deleteAll()
        }
    }

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE invoices ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val factory = SupportFactory(SQLiteDatabase.getBytes("test".toCharArray()))
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_7_8)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                Instance = instance
                instance
            }
        }
    }
}
