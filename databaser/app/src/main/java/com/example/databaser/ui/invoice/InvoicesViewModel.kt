package com.example.databaser.ui.invoice

import androidx.lifecycle.ViewModel
import com.example.databaser.data.dao.InvoiceDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InvoicesViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    val invoices = invoiceDao.getAllInvoices()
}
