package com.example.databaser.ui.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.dao.CustomerDao
import com.example.databaser.data.dao.InvoiceDao
import com.example.databaser.data.dao.NoteDao
import com.example.databaser.data.dao.QuoteDao
import com.example.databaser.data.model.Customer
import com.example.databaser.data.model.Invoice
import com.example.databaser.data.model.Note
import com.example.databaser.data.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customerDao: CustomerDao,
    private val noteDao: NoteDao,
    private val invoiceDao: InvoiceDao,
    private val quoteDao: QuoteDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _customer = MutableStateFlow<Customer?>(null)
    val customer = _customer.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices = _invoices.asStateFlow()

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes = _quotes.asStateFlow()

    private val customerId: Long? = savedStateHandle.get<Long>("customerId")

    init {
        viewModelScope.launch {
            if (customerId != null && customerId > 0) {
                _customer.value = customerDao.getCustomerById(customerId).first()
                noteDao.getNotesForCustomer(customerId).collect { notes ->
                    _notes.value = notes
                }
                invoiceDao.getInvoicesForCustomer(customerId).collect { invoices ->
                    _invoices.value = invoices
                }
                quoteDao.getQuotesForCustomer(customerId).collect { quotes ->
                    _quotes.value = quotes
                }
            }
        }
    }

    fun saveCustomer(
        name: String,
        companyName: String?,
        abn_acn: String?,
        address: String,
        phone: String,
        email: String
    ) {
        viewModelScope.launch {
            val customerToSave = _customer.value?.copy(
                name = name,
                companyName = companyName,
                abn_acn = abn_acn,
                address = address,
                phone = phone,
                email = email
            ) ?: Customer(
                name = name,
                companyName = companyName,
                abn_acn = abn_acn,
                address = address,
                phone = phone,
                email = email
            )

            if (_customer.value == null) {
                customerDao.insert(customerToSave)
            } else {
                customerDao.update(customerToSave)
            }
        }
    }

    fun saveNote(text: String) {
        viewModelScope.launch {
            if (customerId != null) {
                noteDao.insert(Note(customerId = customerId, text = text))
            }
        }
    }
}
