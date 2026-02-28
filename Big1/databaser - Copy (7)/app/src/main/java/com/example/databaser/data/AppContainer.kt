package com.example.databaser.data

import android.content.Context

interface AppContainer {
    val invoiceRepository: InvoiceRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private var _invoiceRepository: InvoiceRepository? = null

    override val invoiceRepository: InvoiceRepository
        get() {
            if (_invoiceRepository == null) {
                throw IllegalStateException("Invoice repository not initialized")
            }
            return _invoiceRepository!!
        }

    suspend fun initialize() {
        val database = AppDatabase.getDatabase(context)
        _invoiceRepository = OfflineInvoicesRepository(database.invoiceDao(), database.lineItemDao())
    }
}
