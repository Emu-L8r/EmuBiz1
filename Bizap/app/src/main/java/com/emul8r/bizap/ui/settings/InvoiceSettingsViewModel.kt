package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.di.UserIdProvider
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI state for invoice settings screen.
 */
data class InvoiceSettingsUiState(
    val settings: InvoiceSettings? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val isSaving: Boolean = false
)

/**
 * ViewModel for managing invoice settings.
 */
@HiltViewModel
class InvoiceSettingsViewModel @Inject constructor(
    private val repository: InvoiceSettingsRepository,
    private val userIdProvider: UserIdProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceSettingsUiState())
    val uiState: StateFlow<InvoiceSettingsUiState> = _uiState.asStateFlow()

    private val userId: String
        get() = userIdProvider.getCurrentUserId()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val settings = repository.getSettings(userId)
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
                Timber.d("Settings loaded successfully: style=${settings?.selectedHtmlStyle}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load settings: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun retryLoadSettings() {
        loadSettings()
    }

    fun updateSelectedTheme(theme: InvoiceTheme) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedTheme = theme)
            )
        }
    }

    fun updateSelectedHtmlStyle(style: HtmlInvoiceStyle) {
        Timber.d("🎨 ViewModel: Updating style to ${style.displayName}")
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedHtmlStyle = style)
            )
        }
    }

    fun updatePrimaryColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(primaryColor = color)
            )
        }
    }

    fun updatePaymentTermsDays(days: Int) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(paymentTermsDays = days)
            )
        }
    }

    fun updateTaxRate(rate: Double) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(taxRate = rate)
            )
        }
    }

    fun updateTaxName(name: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(taxName = name)
            )
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings ?: return@launch
            
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                Timber.d("💾 Saving settings: style=${currentSettings.selectedHtmlStyle}")
                
                repository.saveSettings(currentSettings)
                
                _uiState.value = _uiState.value.copy(
                    saveSuccess = true,
                    isSaving = false
                )
                
                delay(1500)
                _uiState.value = _uiState.value.copy(saveSuccess = false)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save settings")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                repository.resetToDefaults(userId)
                loadSettings()
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset settings")
            }
        }
    }
}
