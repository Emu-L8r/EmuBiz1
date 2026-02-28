package com.emul8r.emubizzz.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emul8r.emubizzz.data.local.db.dao.CustomerDao
import com.emul8r.emubizzz.data.local.db.entities.CustomerEntity
import com.emul8r.emubizzz.data.local.db.entities.InvoiceEntity

@Database(
    entities = [CustomerEntity::class, InvoiceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
}