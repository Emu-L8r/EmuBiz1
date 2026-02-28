package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
import com.emul8r.bizap.domain.repository.PrefilledItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrefilledItemsViewModel @Inject constructor(
    private val repository: PrefilledItemRepository
) : ViewModel() {

    val items: StateFlow<List<PrefilledItemEntity>> = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addItem(description: String, unitPrice: Double) {
        viewModelScope.launch {
            repository.insertItem(PrefilledItemEntity(description = description, unitPrice = unitPrice))
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }
}
