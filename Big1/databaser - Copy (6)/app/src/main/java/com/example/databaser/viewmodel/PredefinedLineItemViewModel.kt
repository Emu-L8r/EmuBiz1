package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.PredefinedLineItem
import com.example.databaser.data.PredefinedLineItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PredefinedLineItemViewModel(private val predefinedLineItemRepository: PredefinedLineItemRepository) : ViewModel() {

    val allPredefinedLineItems: StateFlow<List<PredefinedLineItem>> = predefinedLineItemRepository
        .getAllPredefinedLineItemsStream()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPredefinedLineItem(predefinedLineItem: PredefinedLineItem) {
        viewModelScope.launch {
            predefinedLineItemRepository.insertPredefinedLineItem(predefinedLineItem)
        }
    }

    fun updatePredefinedLineItem(predefinedLineItem: PredefinedLineItem) {
        viewModelScope.launch {
            predefinedLineItemRepository.updatePredefinedLineItem(predefinedLineItem)
        }
    }

    fun deletePredefinedLineItem(predefinedLineItem: PredefinedLineItem) {
        viewModelScope.launch {
            predefinedLineItemRepository.deletePredefinedLineItem(predefinedLineItem)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                val predefinedLineItemRepository = application.container.predefinedLineItemRepository
                PredefinedLineItemViewModel(predefinedLineItemRepository = predefinedLineItemRepository)
            }
        }
    }
}
