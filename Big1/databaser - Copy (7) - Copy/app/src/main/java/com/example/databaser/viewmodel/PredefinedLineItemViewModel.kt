package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.PredefinedLineItem
import com.example.databaser.data.PredefinedLineItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PredefinedLineItemViewModel @Inject constructor(private val predefinedLineItemRepository: PredefinedLineItemRepository) : ViewModel() {

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
}
