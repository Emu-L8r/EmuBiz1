package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.Note
import com.example.databaser.data.NoteRepository
import com.example.databaser.data.Quote
import com.example.databaser.data.QuoteRepository
import com.example.databaser.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val quoteRepository: QuoteRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val uiState: StateFlow<DashboardState> = combine(
        invoiceRepository.getAllInvoicesStream(),
        quoteRepository.getAllQuotesStream(),
        noteRepository.getAllNotesStream()
    ) { invoices, quotes, notes ->
        val paidInvoices = invoices.filter { it.isPaid }
        val sentInvoices = invoices.filter { it.isSent && !it.isPaid }
        val unsentInvoices = invoices.filter { !it.isSent && !it.isPaid }

        val paidInvoicesTotal = paidInvoices.sumOf { it.total }
        val sentInvoicesTotal = sentInvoices.sumOf { it.total }
        val unsentInvoicesTotal = unsentInvoices.sumOf { it.total }

        val unsentQuotes = quotes.filter { !it.isSent }
        val sentQuotes = quotes.filter { it.isSent }

        DashboardState(
            paidInvoices = paidInvoices,
            sentInvoices = sentInvoices,
            unsentInvoices = unsentInvoices,
            paidInvoicesTotal = paidInvoicesTotal,
            sentInvoicesTotal = sentInvoicesTotal,
            unsentInvoicesTotal = unsentInvoicesTotal,
            unsentQuotes = unsentQuotes,
            sentQuotes = sentQuotes,
            notes = notes,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardState(isLoading = true)
    )

    val filteredNotes: StateFlow<List<Note>> = combine(
        noteRepository.getAllNotesStream(),
        searchQuery
    ) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            notes.filter { it.content.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}

data class DashboardState(
    val paidInvoices: List<Invoice> = emptyList(),
    val sentInvoices: List<Invoice> = emptyList(),
    val unsentInvoices: List<Invoice> = emptyList(),
    val paidInvoicesTotal: Double = 0.0,
    val sentInvoicesTotal: Double = 0.0,
    val unsentInvoicesTotal: Double = 0.0,
    val unsentQuotes: List<Quote> = emptyList(),
    val sentQuotes: List<Quote> = emptyList(),
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val invoiceStatusEntries: Map<String, List<com.github.mikephil.charting.data.BarEntry>> = emptyMap()
)
