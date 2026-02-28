package com.example.databaser.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

const val DATABASE_NAME = "app_database"

@Database(entities = [Customer::class, Invoice::class, LineItem::class, PredefinedLineItem::class, BusinessInfo::class, Note::class], version = 9, exportSchema = false)
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

        // Temporary synchronous overload for compatibility with existing call sites
        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val provider = PassphraseProvider(context)
                runBlocking {
                    getDatabase(context, provider)
                }
            }
        }

        suspend fun getDatabase(context: Context, passphraseProvider: PassphraseProvider): AppDatabase {
            return Instance ?: synchronized(this) {
                // Ensure SQLCipher native libs loaded
                SQLiteDatabase.loadLibs(context)

                // Ensure passphrase exists and get raw bytes
                passphraseProvider.ensurePassphraseExists()
                val passphrase = passphraseProvider.getRawPassphrase()

                // Attempt legacy migration (non-destructive rekey) if needed
                try {
                    val statusStore = MigrationStatusStore(context)
                    val migratedStatus = LegacyMigrationHelper.tryMigrateLegacyDb(context, passphraseProvider, statusStore)
                    Log.i("AppDatabase", "Legacy migration result: $migratedStatus")
                } catch (t: Throwable) {
                    Log.w("AppDatabase", "Legacy migration failed: ${t.message}")
                }

                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_7_8)
                    .openHelperFactory(factory)
                    .build()

                // Wipe sensitive passphrase bytes from memory
                passphraseProvider.wipe(passphrase)

                Instance = instance
                instance
            }
        }
    }
}
