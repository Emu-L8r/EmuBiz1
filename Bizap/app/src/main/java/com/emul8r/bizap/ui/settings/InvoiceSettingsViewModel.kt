package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.TaxHandling
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
    val validationErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false
)

/**
 * ViewModel for managing invoice settings.
 *
 * Handles:
 * - Loading invoice settings from repository
 * - Updating settings properties
 * - Saving settings to repository
 * - Resetting to default settings
 * - Error handling and user feedback
 */
@HiltViewModel
class InvoiceSettingsViewModel @Inject constructor(
    private val repository: InvoiceSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceSettingsUiState())
    val uiState: StateFlow<InvoiceSettingsUiState> = _uiState.asStateFlow()

    // TODO: Get from auth/session instead of hardcoding
    private val userId = "current_user"

    init {
        loadSettings()
    }

    /**
     * Load invoice settings from repository.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val settings = repository.getSettings(userId)
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
                Timber.d("Settings loaded successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load settings")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load settings: ${e.message ?: "Unknown error"}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Retry loading settings after a failure.
     */
    fun retryLoadSettings() {
        Timber.d("Retrying settings load...")
        loadSettings()
    }

    /**
     * Update business name in current settings.
     */
    fun updateBusinessName(name: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(businessName = name)
            )
        }
    }

    /**
     * Update business email in current settings.
     */
    fun updateBusinessEmail(email: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(businessEmail = email)
            )
        }
    }

    /**
     * Update business phone in current settings.
     */
    fun updateBusinessPhone(phone: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(businessPhone = phone)
            )
        }
    }

    /**
     * Update business address in current settings.
     */
    fun updateBusinessAddress(address: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(businessAddress = address)
            )
        }
    }

    /**
     * Update primary color in current settings.
     */
    fun updatePrimaryColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(primaryColor = color)
            )
        }
    }

    /**
     * Update selected invoice theme.
     */
    fun updateSelectedTheme(theme: InvoiceTheme) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedTheme = theme)
            )
        }
    }

    /**
     * Update payment terms (days) in current settings.
     */
    fun updatePaymentTermsDays(days: Int) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(paymentTermsDays = days)
            )
        }
    }

    /**
     * Update tax rate in current settings.
     */
    fun updateTaxRate(rate: Double) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(taxRate = rate)
            )
        }
    }

    /**
     * Update tax name (e.g., "GST", "VAT") in current settings.
     */
    fun updateTaxName(name: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(taxName = name)
            )
        }
    }

    /**
     * Update bank name in current settings.
     */
    fun updateBankName(name: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(bankName = name)
            )
        }
    }

    /**
     * Save current settings to repository.
     * OPTIMIZATION: Removed 2-second delay, success flag auto-resets via UI
     */
    fun saveSettings() {
        viewModelScope.launch {
            try {
                _uiState.value.settings?.let { settings ->
                    if (!settings.isValid()) {
                        _uiState.value = _uiState.value.copy(
                            error = "Invalid settings: Missing required fields"
                        )
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(isSaving = true)
                    repository.saveSettings(settings)
                    _uiState.value = _uiState.value.copy(
                        saveSuccess = true,
                        error = null,
                        isSaving = false
                    )
                    Timber.d("Settings saved successfully")
                    _uiState.value = _uiState.value.copy(saveSuccess = false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save settings")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save settings: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    /**
     * Reset settings to default values.
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                repository.resetToDefaults(userId)
                loadSettings()
                Timber.d("Settings reset to defaults")
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset settings")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to reset settings: ${e.message}"
                )
            }
        }
    }
}
