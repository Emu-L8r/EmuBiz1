package com.example.databaser.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.databaser.data.dao.CustomerDao
import com.example.databaser.data.dao.InvoiceDao
import com.example.databaser.data.dao.InvoiceLineItemDao
import com.example.databaser.data.dao.NoteDao
import com.example.databaser.data.dao.QuoteDao
import com.example.databaser.data.dao.QuoteLineItemDao
import com.example.databaser.data.model.Customer
import com.example.databaser.data.model.Invoice
import com.example.databaser.data.model.InvoiceLineItem
import com.example.databaser.data.model.Note
import com.example.databaser.data.model.Quote
import com.example.databaser.data.model.QuoteLineItem

@Database(
    entities = [
        Customer::class,
        Invoice::class,
        InvoiceLineItem::class,
        Note::class,
        Quote::class,
        QuoteLineItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceLineItemDao(): InvoiceLineItemDao
    abstract fun noteDao(): NoteDao
    abstract fun quoteDao(): QuoteDao
    abstract fun quoteLineItemDao(): QuoteLineItemDao
}
