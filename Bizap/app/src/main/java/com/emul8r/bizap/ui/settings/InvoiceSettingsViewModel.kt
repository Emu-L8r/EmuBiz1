package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.di.UserIdProvider
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
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
    private val repository: InvoiceSettingsRepository,
    private val userIdProvider: UserIdProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceSettingsUiState())
    val uiState: StateFlow<InvoiceSettingsUiState> = _uiState.asStateFlow()

    // Get user ID from provider (single source of truth)
    private val userId: String
        get() = userIdProvider.getCurrentUserId()

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
     * Update selected HTML invoice style (for HTML-to-PDF theme).
     * Only used when selectedTheme == InvoiceTheme.HTML_PDF
     */
    fun updateSelectedHtmlStyle(style: HtmlInvoiceStyle) {
        Timber.d("════════════════════════════════════════════════════════════════")
        Timber.d("🎨 updateSelectedHtmlStyle() CALLED")
        Timber.d("   Selected Style: ${style.displayName} (ENUM: ${style.name})")
        Timber.d("   CSS File: ${style.styleFile}")
        Timber.d("════════════════════════════════════════════════════════════════")

        _uiState.value.settings?.let { current ->
            val oldStyle = current.selectedHtmlStyle
            val updated = current.copy(selectedHtmlStyle = style)
            _uiState.value = _uiState.value.copy(settings = updated)

            Timber.d("✅ UI State Updated:")
            Timber.d("   Old Style: ${oldStyle.displayName}")
            Timber.d("   New Style: ${updated.selectedHtmlStyle.displayName}")
            Timber.d("   ⚠️ NOTE: Style updated in UI but NOT yet persisted to database!")
            Timber.d("   Remember to call saveSettings() to persist this change")
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
     * Save current settings to repository.
     */
    fun saveSettings() {
        viewModelScope.launch {
            try {
                Timber.d("═══════════════════════════════════════════════════════════════════════════")
                Timber.d("💾 SAVE_SETTINGS_CALLED - Full Diagnostic Dump")
                Timber.d("═══════════════════════════════════════════════════════════════════════════")

                val currentSettings = _uiState.value.settings
                Timber.d("Current settings loaded: ${currentSettings != null}")

                if (currentSettings != null) {
                    Timber.d("📋 SETTINGS TO BE SAVED:")
                    Timber.d("   ✓ selectedTheme: ${currentSettings.selectedTheme.name}")
                    Timber.d("   ✓ selectedHtmlStyle: ${currentSettings.selectedHtmlStyle.displayName}")
                    Timber.d("   ✓ selectedHtmlStyle ENUM: ${currentSettings.selectedHtmlStyle.name}")
                    Timber.d("   ✓ selectedHtmlStyle CSS: ${currentSettings.selectedHtmlStyle.styleFile}")
                    Timber.d("   ✓ taxRate: ${currentSettings.taxRate}")
                    Timber.d("   ✓ paymentTermsDays: ${currentSettings.paymentTermsDays}")
                } else {
                    Timber.d("   ⚠️ Settings is NULL!")
                }

                if (currentSettings == null) {
                    Timber.e("❌ ERROR: Settings is NULL - cannot save")
                    _uiState.value = _uiState.value.copy(
                        error = "ERROR: Settings not loaded",
                        isSaving = false
                    )
                    return@launch
                }

                // Validate settings - selectedTheme should never be null due to default value
                if (!currentSettings.isValid()) {
                    Timber.e("❌ ERROR: Settings validation failed - theme is null")
                    _uiState.value = _uiState.value.copy(
                        error = "Invalid settings: Theme not selected"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isSaving = true)
                Timber.d("🔄 Calling repository.saveSettings() with:")
                Timber.d("   Theme: ${currentSettings.selectedTheme}")
                Timber.d("   HTML Style: ${currentSettings.selectedHtmlStyle.displayName}")

                // Call the suspend function - we're already in viewModelScope.launch context
                try {
                    repository.saveSettings(currentSettings)
                    Timber.d("✅ repository.saveSettings() completed successfully")
                    Timber.d("   ✓ Settings persisted to database")
                    Timber.d("   ✓ selectedHtmlStyle now in database: ${currentSettings.selectedHtmlStyle.displayName}")
                } catch (dbException: Exception) {
                    Timber.e(dbException, "❌ Database error while saving settings: ${dbException.message}")
                    throw dbException
                }

                // FIX #1: Don't call loadSettings() immediately to avoid race condition
                // Instead, just update the UI state to confirm success
                _uiState.value = _uiState.value.copy(
                    saveSuccess = true,
                    error = null,
                    isSaving = false
                )
                Timber.d("═══════════════════════════════════════════════════════════════════════════")
                Timber.d("✅ SAVE_SETTINGS COMPLETE - Settings saved successfully!")
                Timber.d("═══════════════════════════════════════════════════════════════════════════")

                // Auto-hide success message after 500ms
                delay(500)
                _uiState.value = _uiState.value.copy(saveSuccess = false)

            } catch (e: Exception) {
                Timber.e(e, "❌ Exception during saveSettings: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save settings: ${e.message}",
                    isSaving = false
                )
            }
        }
    }

    /**
     * Update footer message in current settings.
     * Phase 2: Auto-populated in invoice creation from this setting
     */
    fun updateFooterMessage(message: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(footerMessage = message)
            )
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
