package com.example.databaser.data

import android.content.Context

class AppDataContainer(private val context: Context) : AppContainer {
    override val businessInfoRepository: BusinessInfoRepository by lazy {
        OfflineBusinessInfoRepository(AppDatabase.getDatabase(context).businessInfoDao())
    }
    override val customerRepository: CustomerRepository by lazy {
        OfflineCustomersRepository(AppDatabase.getDatabase(context).customerDao())
    }
    override val dashboardRepository: DashboardRepository by lazy {
        OfflineDashboardRepository(AppDatabase.getDatabase(context).invoiceDao())
    }
    override val invoiceRepository: InvoiceRepository by lazy {
        OfflineInvoicesRepository(AppDatabase.getDatabase(context).invoiceDao(), AppDatabase.getDatabase(context).lineItemDao())
    }
    override val lineItemRepository: LineItemRepository by lazy {
        OfflineLineItemRepository(AppDatabase.getDatabase(context).lineItemDao())
    }
    override val mySavesRepository: MySavesRepository by lazy {
        OfflineMySavesRepository(context)
    }
    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(AppDatabase.getDatabase(context).noteDao())
    }
    override val predefinedLineItemRepository: PredefinedLineItemRepository by lazy {
        OfflinePredefinedLineItemRepository(AppDatabase.getDatabase(context).predefinedLineItemDao())
    }
    override val quoteRepository: QuoteRepository by lazy {
        OfflineQuotesRepository(AppDatabase.getDatabase(context).quoteDao(), AppDatabase.getDatabase(context).lineItemDao())
    }
}
