package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.LineItem
import com.example.databaser.data.LineItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LineItemViewModel(private val lineItemRepository: LineItemRepository) : ViewModel() {

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                val lineItemRepository = application.container.lineItemRepository
                LineItemViewModel(lineItemRepository = lineItemRepository)
            }
        }
    }
}
