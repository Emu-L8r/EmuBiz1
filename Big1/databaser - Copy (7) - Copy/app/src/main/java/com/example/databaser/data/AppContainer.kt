package com.example.databaser.data

import android.content.Context

interface AppContainer {
    val businessInfoRepository: BusinessInfoRepository
    val customerRepository: CustomerRepository
    val dashboardRepository: DashboardRepository
    val invoiceRepository: InvoiceRepository
    val lineItemRepository: LineItemRepository
    val mySavesRepository: MySavesRepository
    val notesRepository: NotesRepository
    val predefinedLineItemRepository: PredefinedLineItemRepository
    val quoteRepository: QuoteRepository
}