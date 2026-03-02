package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.data.repository.InvoiceTemplateRepository
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceSettingsViewModel @Inject constructor(
    private val templateRepository: InvoiceTemplateRepository,
    private val profileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceSettingsUiState>(InvoiceSettingsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val businessId = profileRepository.getActiveBusinessId()
                val result = templateRepository.getDefaultTemplate(businessId)
                
                result.onSuccess { template ->
                    if (template != null) {
                        _uiState.value = InvoiceSettingsUiState.Success(template)
                    } else {
                        // Create a default template if none exists
                        val newDefault = InvoiceTemplate(
                            businessProfileId = businessId,
                            name = "Default",
                            designType = "PROFESSIONAL",
                            isDefault = true
                        )
                        templateRepository.createTemplate(newDefault)
                        _uiState.value = InvoiceSettingsUiState.Success(newDefault)
                    }
                }.onFailure {
                    _uiState.value = InvoiceSettingsUiState.Error("Failed to load settings: ${it.message}")
                }
            } catch (e: Exception) {
                _uiState.value = InvoiceSettingsUiState.Error("Critical error: ${e.message}")
            }
        }
    }

    fun updateSettings(template: InvoiceTemplate) {
        viewModelScope.launch {
            templateRepository.updateTemplate(template)
            _uiState.value = InvoiceSettingsUiState.Success(template)
        }
    }

    fun resetToDefaults() {
        val currentState = _uiState.value as? InvoiceSettingsUiState.Success ?: return
        val defaultTemplate = currentState.template.copy(
            primaryColor = "#FF5722",
            fontFamily = "SANS_SERIF",
            showPhone = true,
            showEmail = true,
            showAddress = true,
            showTaxId = true,
            marginPreset = "NORMAL",
            fontSizePreset = "NORMAL",
            showZebraStripes = true,
            footerMessage = "Thank you for your business!"
        )
        updateSettings(defaultTemplate)
    }
}

sealed interface InvoiceSettingsUiState {
    object Loading : InvoiceSettingsUiState
    data class Success(val template: InvoiceTemplate) : InvoiceSettingsUiState
    data class Error(val message: String) : InvoiceSettingsUiState
}
