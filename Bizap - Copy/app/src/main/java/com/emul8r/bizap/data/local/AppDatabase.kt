package com.emul8r.bizap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.LineItemEntity
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
import com.emul8r.bizap.data.local.typeconverters.DocumentStatusConverter

@Database(
    entities = [CustomerEntity::class, InvoiceEntity::class, LineItemEntity::class, PrefilledItemEntity::class, GeneratedDocumentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DocumentStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun prefilledItemDao(): PrefilledItemDao
    abstract fun documentDao(): DocumentDao
}
