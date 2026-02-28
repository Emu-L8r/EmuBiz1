package com.example.databaser.ui.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.dao.QuoteDao
import com.example.databaser.data.dao.QuoteLineItemDao
import com.example.databaser.data.model.Quote
import com.example.databaser.data.model.QuoteLineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteDetailsViewModel @Inject constructor(
    private val quoteDao: QuoteDao,
    private val quoteLineItemDao: QuoteLineItemDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _quote = MutableStateFlow<Quote?>(null)
    val quote = _quote.asStateFlow()

    private val _lineItems = MutableStateFlow<List<QuoteLineItem>>(emptyList())
    val lineItems = _lineItems.asStateFlow()

    private val quoteId: Long? = savedStateHandle.get<Long>("quoteId")
    private var tempIdCounter = -1L

    init {
        viewModelScope.launch {
            if (quoteId != null && quoteId > 0) {
                _quote.value = quoteDao.getQuoteById(quoteId).first()
                quoteLineItemDao.getLineItemsForQuote(quoteId).collect {
                    _lineItems.value = it
                }
            }
        }
    }

    fun saveQuote(customerId: Long, date: Long, header: String, subHeader: String, footer: String, lineItems: List<QuoteLineItem>) {
        viewModelScope.launch {
            val quoteToSave = _quote.value?.copy(
                customerId = customerId,
                date = date,
                header = header,
                subHeader = subHeader,
                footer = footer
            ) ?: Quote(customerId = customerId, date = date, header = header, subHeader = subHeader, footer = footer)

            val savedQuoteId: Long
            if (_quote.value == null) {
                savedQuoteId = quoteDao.insert(quoteToSave)
            } else {
                quoteDao.update(quoteToSave)
                savedQuoteId = quoteToSave.id
            }

            lineItems.forEach { lineItem ->
                val lineItemWithCorrectQuoteId = lineItem.copy(quoteId = savedQuoteId)
                // Use <= 0 to check for temporary client-side IDs
                if (lineItem.id <= 0L) {
                    // Reset temp id to 0 for auto-generation by Room
                    quoteLineItemDao.insert(lineItemWithCorrectQuoteId.copy(id = 0))
                } else {
                    quoteLineItemDao.update(lineItemWithCorrectQuoteId)
                }
            }
        }
    }

    fun addLineItem() {
        // Create a new item with a temporary negative ID for client-side tracking
        val newItem = QuoteLineItem(id = tempIdCounter--, quoteId = quoteId ?: 0, description = "", quantity = 1, price = 0.0)
        _lineItems.value = _lineItems.value + newItem
    }

    fun updateLineItem(lineItem: QuoteLineItem) {
        val currentList = _lineItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == lineItem.id }
        if (index != -1) {
            currentList[index] = lineItem
            _lineItems.value = currentList
        }
    }

    fun removeLineItem(lineItem: QuoteLineItem) {
        viewModelScope.launch {
            // Only issue a delete command to the DAO if the ID is positive (i.e., it exists in the database)
            if (lineItem.id > 0L) {
                quoteLineItemDao.delete(lineItem.id)
            }
            // Always remove the item from the local list
            _lineItems.value = _lineItems.value.filterNot { it.id == lineItem.id }
        }
    }
}
