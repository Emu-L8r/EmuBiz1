package com.example.databaser

import android.content.Context
import com.example.databaser.data.AppDatabase
import com.example.databaser.data.BusinessInfoRepository
import com.example.databaser.data.CustomerRepository
import com.example.databaser.data.DashboardRepository
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.LineItemRepository
import com.example.databaser.data.MySavesRepository
import com.example.databaser.data.NotesRepository
import com.example.databaser.data.OfflineBusinessInfoRepository
import com.example.databaser.data.OfflineCustomersRepository
import com.example.databaser.data.OfflineDashboardRepository
import com.example.databaser.data.OfflineInvoicesRepository
import com.example.databaser.data.OfflineLineItemRepository
import com.example.databaser.data.OfflineMySavesRepository
import com.example.databaser.data.OfflineNotesRepository
import com.example.databaser.data.OfflinePredefinedLineItemRepository
import com.example.databaser.data.PredefinedLineItemRepository
import kotlinx.coroutines.runBlocking

interface AppContainer {
    val invoiceRepository: InvoiceRepository
    val customerRepository: CustomerRepository
    val predefinedLineItemRepository: PredefinedLineItemRepository
    val lineItemRepository: LineItemRepository
    val businessInfoRepository: BusinessInfoRepository
    val mySavesRepository: MySavesRepository
    val dashboardRepository: DashboardRepository
    val notesRepository: NotesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        runBlocking {
            AppDatabase.getDatabase(context)
        }
    }

    override val invoiceRepository: InvoiceRepository by lazy {
        OfflineInvoicesRepository(database.invoiceDao(), database.lineItemDao())
    }
    override val customerRepository: CustomerRepository by lazy {
        OfflineCustomersRepository(database.customerDao())
    }
    override val predefinedLineItemRepository: PredefinedLineItemRepository by lazy {
        OfflinePredefinedLineItemRepository(database.predefinedLineItemDao())
    }
    override val lineItemRepository: LineItemRepository by lazy {
        OfflineLineItemRepository(database.lineItemDao())
    }
    override val businessInfoRepository: BusinessInfoRepository by lazy {
        OfflineBusinessInfoRepository(database.businessInfoDao())
    }
    override val mySavesRepository: MySavesRepository by lazy {
        OfflineMySavesRepository()
    }
    override val dashboardRepository: DashboardRepository by lazy {
        OfflineDashboardRepository(database.invoiceDao())
    }
    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(database.noteDao())
    }
}
