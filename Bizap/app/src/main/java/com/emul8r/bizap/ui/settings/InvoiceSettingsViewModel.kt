package com.emul8r.bizap.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.data.service.HtmlPdfInvoiceService
import com.emul8r.bizap.data.service.layout.PageLayoutFactory
import com.emul8r.bizap.data.service.preview.PlaceholderInvoiceGenerator
import com.emul8r.bizap.di.UserIdProvider
import com.emul8r.bizap.domain.model.CanvasInvoiceTemplate
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.model.InvoiceColorScheme
import com.emul8r.bizap.domain.model.InvoiceLocale
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.Typography
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
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

    // ===== PHASE 4: Preview Debouncing (Approach 2) =====
    private var previewDebounceJob: Job? = null
    private val PREVIEW_DEBOUNCE_MS = 300L

    init {
        loadSettings()
    }

    // Debounced preview generation to prevent hammering during rapid clicks
    private fun debouncedGeneratePreview() {
        previewDebounceJob?.cancel()  // Cancel previous scheduled job
        previewDebounceJob = viewModelScope.launch {
            delay(PREVIEW_DEBOUNCE_MS)  // Wait 300ms for user to stop changing
            generatePreview()  // Then generate once
        }
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
            // Keep selectedTheme in sync with selectedPdfEngine so preview routing works correctly
            val syncedTheme = when (engine) {
                PdfEngine.HTML_CSS -> InvoiceTheme.HTML_PDF
                PdfEngine.CANVAS   -> InvoiceTheme.CANVAS
            }
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedPdfEngine = engine, selectedTheme = syncedTheme)
            )
        }
        // ✨ PHASE 4 (Approach 1): Trigger preview refresh with debouncing
        debouncedGeneratePreview()
    }

    fun updateSelectedPageLayout(layout: PageLayout) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedPageLayout = layout)
            )
        }
        // ✨ PHASE 4 (Approach 1): Trigger preview refresh with debouncing
        debouncedGeneratePreview()
    }

    fun updateSelectedCanvasTemplate(template: CanvasInvoiceTemplate) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedCanvasTemplate = template)
            )
        }
        // ✨ PHASE 4 (Approach 1): Trigger preview refresh with debouncing
        debouncedGeneratePreview()
    }

    fun updateSelectedHtmlStyle(style: HtmlInvoiceStyle) {
        Timber.d("🎨 ViewModel: Updating style to ${style.displayName}")
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedHtmlStyle = style)
            )
        }
        // ✨ PHASE 4 (Approach 2): Use debounced version instead of immediate generatePreview()
        debouncedGeneratePreview()
    }

    fun updateSelectedTypography(typography: Typography) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedTypography = typography)
            )
        }
        debouncedGeneratePreview()
    }

    fun toggleShowBusinessAbn(show: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(showBusinessAbn = show)
            )
        }
        debouncedGeneratePreview()
    }

    fun toggleShowCustomerPhone(show: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(showCustomerPhone = show)
            )
        }
        debouncedGeneratePreview()
    }

    fun toggleShowStatusWatermark(show: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(showStatusWatermark = show)
            )
        }
        debouncedGeneratePreview()
    }

    fun toggleShowPageNumbers(show: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(showPageNumbers = show)
            )
        }
        debouncedGeneratePreview()
    }

    /**
     * Generate live preview HTML using placeholder data and current settings.
     * Supports both Canvas and HTML engines with appropriate previews.
     * ✨ FIXED: Canvas preview now respects selectedPageLayout via layout factory!
     */
    fun generatePreview() {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings ?: return@launch
            try {
                // ✨ ALWAYS use placeholder data for preview (cleaner, focused on design)
                val previewSnapshot = PlaceholderInvoiceGenerator.generatePreviewInvoice()

                // Route by selectedPdfEngine
                val useCanvas = currentSettings.selectedPdfEngine == PdfEngine.CANVAS

                if (useCanvas) {
                    // ✨ FIXED: Canvas preview now uses LAYOUT FACTORY
                    // This respects selectedPageLayout!
                    val colorScheme = InvoiceColorScheme(
                        primaryColor = currentSettings.selectedCanvasTemplate.primaryHex,
                        accentColor = currentSettings.selectedCanvasTemplate.accentHex
                    )
                    val layoutProvider = PageLayoutFactory.createLayout(currentSettings.selectedPageLayout)
                    val canvasHtml = layoutProvider.buildInvoiceHtml(previewSnapshot, isQuote = false, colorScheme)
                    _previewHtml.value = canvasHtml
                    Timber.d("✅ Canvas preview updated: template=${currentSettings.selectedCanvasTemplate.displayName}, layout=${currentSettings.selectedPageLayout}")
                } else {
                    // HTML-to-PDF preview generation
                    val htmlService = HtmlPdfInvoiceService(context, currentSettings)
                    val html = htmlService.buildPreviewHtml(previewSnapshot, isQuote = false)
                    _previewHtml.value = html
                    Timber.d("✅ HTML preview updated: layout=${currentSettings.selectedPageLayout}, style=${currentSettings.selectedHtmlStyle}")
                }
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

                // CRITICAL FIX: Save settings with proper database synchronization
                repository.saveSettings(currentSettings)
                Timber.d("✅ Settings persisted to database")

                // CRITICAL FIX: Add delay to ensure Room database transaction is fully committed
                // This prevents race conditions where subsequent reads get stale data
                delay(150)

                // CRITICAL FIX: Force reload settings from database to verify save worked
                // and ensure next PDF generation gets fresh data (not cached/stale)
                Timber.d("🔄 Reloading settings from database to verify save...")
                val reloadedSettings = repository.getSettings(currentSettings.userId)

                if (reloadedSettings != null) {
                    // Update UI state with freshly loaded settings
                    _uiState.value = _uiState.value.copy(
                        settings = reloadedSettings,
                        saveSuccess = true,
                        isSaving = false
                    )
                    Timber.d("✅ Settings verified from database: style=${reloadedSettings.selectedHtmlStyle}")
                } else {
                    throw IllegalStateException("Settings not found in database after save")
                }

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

    fun updateSelectedLocale(locale: InvoiceLocale) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedLocale = locale)
            )
        }
        debouncedGeneratePreview()
    }
}
