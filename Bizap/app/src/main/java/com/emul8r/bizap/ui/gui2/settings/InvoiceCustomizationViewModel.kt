package com.emul8r.bizap.ui.gui2.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Invoice Customization Settings Screen
 * Manages invoice template settings and customization options
 * Persists settings to DataStore
 */
@HiltViewModel
class InvoiceCustomizationViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    // Invoice settings state
    private val _invoiceSettings = MutableStateFlow<InvoiceSettings?>(null)
    val invoiceSettings: StateFlow<InvoiceSettings?> = _invoiceSettings.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadInvoiceSettings()
    }

    /**
     * Load invoice customization settings from DataStore
     */
    private fun loadInvoiceSettings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("Loading invoice customization settings from DataStore")

                val prefs = dataStore.data.first()
                val settings = InvoiceSettings(
                    invoicePrefix = prefs[INVOICE_PREFIX_KEY] ?: "INV",
                    startingNumber = prefs[STARTING_NUMBER_KEY] ?: 1001,
                    includeNotes = prefs[INCLUDE_NOTES_KEY] ?: true,
                    includeTaxId = prefs[INCLUDE_TAX_ID_KEY] ?: true,
                    footerText = prefs[FOOTER_TEXT_KEY] ?: "",
                    showLogo = prefs[SHOW_LOGO_KEY] ?: true,
                    showCompanyInfo = prefs[SHOW_COMPANY_INFO_KEY] ?: true
                )
                _invoiceSettings.value = settings
                _errorMessage.value = null
            } catch (e: Exception) {
                Timber.e(e, "Failed to load invoice settings")
                _errorMessage.value = e.message ?: "Failed to load settings"
                _invoiceSettings.value = InvoiceSettings() // Use defaults on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update invoice settings and persist to DataStore
     */
    fun updateInvoiceSettings(settings: InvoiceSettings) {
        viewModelScope.launch {
            try {
                Timber.d("Updating invoice customization settings")
                _invoiceSettings.value = settings
                _errorMessage.value = null

                // Persist to DataStore
                dataStore.edit { prefs ->
                    prefs[INVOICE_PREFIX_KEY] = settings.invoicePrefix
                    prefs[STARTING_NUMBER_KEY] = settings.startingNumber
                    prefs[INCLUDE_NOTES_KEY] = settings.includeNotes
                    prefs[INCLUDE_TAX_ID_KEY] = settings.includeTaxId
                    prefs[FOOTER_TEXT_KEY] = settings.footerText
                    prefs[SHOW_LOGO_KEY] = settings.showLogo
                    prefs[SHOW_COMPANY_INFO_KEY] = settings.showCompanyInfo
                }

                Timber.d("✅ Invoice settings saved successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update invoice settings")
                _errorMessage.value = e.message ?: "Failed to save settings"
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

// DataStore preference keys
private val INVOICE_PREFIX_KEY = stringPreferencesKey("invoice_prefix")
private val STARTING_NUMBER_KEY = intPreferencesKey("starting_number")
private val INCLUDE_NOTES_KEY = booleanPreferencesKey("include_notes")
private val INCLUDE_TAX_ID_KEY = booleanPreferencesKey("include_tax_id")
private val FOOTER_TEXT_KEY = stringPreferencesKey("footer_text")
private val SHOW_LOGO_KEY = booleanPreferencesKey("show_logo")
private val SHOW_COMPANY_INFO_KEY = booleanPreferencesKey("show_company_info")

/**
 * Data class for invoice customization settings
 */
data class InvoiceSettings(
    val invoicePrefix: String = "INV",
    val startingNumber: Int = 1001,
    val includeNotes: Boolean = true,
    val includeTaxId: Boolean = true,
    val footerText: String = "",
    val showLogo: Boolean = true,
    val showCompanyInfo: Boolean = true
)


