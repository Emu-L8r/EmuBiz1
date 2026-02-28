package com.emu.emubizwax.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emu.emubizwax.data.local.dao.CustomerDao
import com.emu.emubizwax.data.local.dao.InvoiceDao
import com.emu.emubizwax.data.local.dao.LineItemDao
import com.emu.emubizwax.data.local.entities.CustomerEntity
import com.emu.emubizwax.data.local.entities.InvoiceEntity
import com.emu.emubizwax.data.local.entities.LineItemEntity

@Database(
    entities = [CustomerEntity::class, InvoiceEntity::class, LineItemEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun lineItemDao(): LineItemDao
}
