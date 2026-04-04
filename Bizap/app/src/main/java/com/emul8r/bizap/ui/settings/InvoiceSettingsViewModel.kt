package com.emul8r.bizap.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.data.service.HtmlPdfInvoiceService
import com.emul8r.bizap.data.service.preview.PlaceholderInvoiceGenerator
import com.emul8r.bizap.di.UserIdProvider
import com.emul8r.bizap.domain.model.CanvasInvoiceTemplate
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val repository: InvoiceSettingsRepository,
    private val userIdProvider: UserIdProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceSettingsUiState())
    val uiState: StateFlow<InvoiceSettingsUiState> = _uiState.asStateFlow()

    private val _previewHtml = MutableStateFlow<String?>(null)
    val previewHtml: StateFlow<String?> = _previewHtml.asStateFlow()

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
                // Generate initial preview once settings are loaded
                if (settings != null) {
                    generatePreview()
                }
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

    fun updateSelectedPdfEngine(engine: PdfEngine) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedPdfEngine = engine)
            )
        }
    }

    fun updateSelectedPageLayout(layout: PageLayout) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedPageLayout = layout)
            )
        }
    }

    fun updatePreviewWithPlaceholder(enabled: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(previewWithPlaceholder = enabled)
            )
        }
    }

    fun updateSelectedCanvasTemplate(template: CanvasInvoiceTemplate) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedCanvasTemplate = template)
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
        // Auto-generate live preview when style changes
        generatePreview()
    }

    /**
     * Generate live preview HTML using placeholder data and current settings.
     * The result is stored in [previewHtml] for display in the settings UI.
     */
    fun generatePreview() {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings ?: return@launch
            try {
                val previewSnapshot = PlaceholderInvoiceGenerator.generatePreviewInvoice()
                // HtmlPdfInvoiceService is instantiated per-call since its settings are dynamic
                // (change with every style selection). HTML generation does not use context;
                // only the PDF file-writing path requires it.
                val htmlService = HtmlPdfInvoiceService(context, currentSettings)
                val html = htmlService.buildPreviewHtml(previewSnapshot, isQuote = false)
                _previewHtml.value = html
                Timber.d("✅ Live preview HTML generated for style: ${currentSettings.selectedHtmlStyle}")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to generate live preview")
            }
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
