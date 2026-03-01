package com.emul8r.bizap.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import com.emul8r.bizap.data.repository.InvoiceTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class InvoiceTemplateViewModel @Inject constructor(
    private val repository: InvoiceTemplateRepository
) : ViewModel() {

    companion object {
        private const val TAG = "InvoiceTemplateViewModel"
    }

    private val _templates = MutableStateFlow<List<InvoiceTemplate>>(emptyList())
    val templates: StateFlow<List<InvoiceTemplate>> = _templates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    fun loadTemplates(businessProfileId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllTemplates(businessProfileId)
                .onSuccess { _templates.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun createTemplate(template: InvoiceTemplate) {
        viewModelScope.launch {
            repository.createTemplate(template)
                .onSuccess { loadTemplates(template.businessProfileId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun updateTemplate(template: InvoiceTemplate) {
        viewModelScope.launch {
            repository.updateTemplate(template)
                .onSuccess { loadTemplates(template.businessProfileId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteTemplate(templateId: String, businessProfileId: Long) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
                .onSuccess { loadTemplates(businessProfileId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun setAsDefault(templateId: String, businessProfileId: Long) {
        viewModelScope.launch {
            repository.setAsDefault(templateId, businessProfileId)
                .onSuccess { loadTemplates(businessProfileId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun addCustomField(field: InvoiceCustomField) {
        viewModelScope.launch {
            repository.addCustomField(field)
                .onFailure { _error.value = it.message }
        }
    }

    // NEW HELPER METHODS FOR REPAIR
    fun retryLoadTemplates(businessProfileId: Long) = loadTemplates(businessProfileId)
    
    fun navigateToCreate(businessProfileId: Long) {
        _navigationEvent.value = NavigationEvent.NavigateToCreate(businessProfileId)
    }

    fun navigateToEdit(templateId: String) {
        _navigationEvent.value = NavigationEvent.NavigateToEdit(templateId)
    }

    fun clearNavigationEvent() { _navigationEvent.value = null }
    fun clearError() { _error.value = null }

    sealed class NavigationEvent {
        data class NavigateToCreate(val businessProfileId: Long) : NavigationEvent()
        data class NavigateToEdit(val templateId: String) : NavigationEvent()
    }
}
