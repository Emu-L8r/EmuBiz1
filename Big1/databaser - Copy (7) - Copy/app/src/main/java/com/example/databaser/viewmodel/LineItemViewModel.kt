package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.LineItem
import com.example.databaser.data.LineItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LineItemViewModel @Inject constructor(private val lineItemRepository: LineItemRepository) : ViewModel() {

    fun getLineItemsForInvoice(invoiceId: Int): Flow<List<LineItem>> {
        return lineItemRepository.getLineItemsForInvoiceStream(invoiceId)
    }

    fun getLineItemById(lineItemId: Int): Flow<LineItem?> {
        return lineItemRepository.getLineItemStream(lineItemId)
    }

    fun addLineItem(lineItem: LineItem) {
        viewModelScope.launch {
            lineItemRepository.insertLineItem(lineItem)
        }
    }

    fun updateLineItem(lineItem: LineItem) {
        viewModelScope.launch {
            lineItemRepository.updateLineItem(lineItem)
        }
    }

    fun deleteLineItem(lineItem: LineItem) {
        viewModelScope.launch {
            lineItemRepository.deleteLineItem(lineItem)
        }
    }

    fun deleteLineItemsByInvoiceId(invoiceId: Int) {
        viewModelScope.launch {
            getLineItemsForInvoice(invoiceId).first().forEach {
                deleteLineItem(it)
            }
        }
    }
}
