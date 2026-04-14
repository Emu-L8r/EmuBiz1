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
import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.model.InvoiceColorScheme
import com.emul8r.bizap.domain.model.InvoiceLocale
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.SpacingProfile
import com.emul8r.bizap.domain.model.Typography
import com.emul8r.bizap.domain.model.VisualAccents
import com.emul8r.bizap.domain.model.LogoPosition
import com.emul8r.bizap.domain.model.QrCodePosition
import com.emul8r.bizap.domain.model.PaymentMethod
import com.emul8r.bizap.domain.model.DividerStyle
import com.emul8r.bizap.domain.model.TotalBoxStyle
import com.emul8r.bizap.domain.model.BadgeStyle
import com.emul8r.bizap.domain.model.BackgroundPattern
import com.emul8r.bizap.domain.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    private var lastPreviewKey: String = ""

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

     /**
      * ✨ PHASE 1: Intelligent preview generation that only regenerates when settings key changes.
      * This prevents unnecessary WebView recreation and improves responsiveness.
      */
     private fun intelligentGeneratePreview() {
         val currentKey = getPreviewStateKey()
         if (currentKey != lastPreviewKey) {
             lastPreviewKey = currentKey
             Timber.d("🔄 Preview key changed, regenerating preview (key=$currentKey)")
             debouncedGeneratePreview()
         } else {
             Timber.d("⏭️  Preview key unchanged, skipping regeneration")
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

     /**
      * ✨ PHASE 1: Create stable preview state key from critical settings.
      * Uses hashing to determine when preview should regenerate.
      * Only regenerates when key changes (not on every recomposition).
      */
     fun getPreviewStateKey(): String {
         val settings = _uiState.value.settings ?: return "LOADING"
         return buildString {
             append(settings.selectedPdfEngine.name)
             append(settings.selectedPageLayout.name)
             append(settings.selectedHtmlStyle.displayName)
             append(settings.selectedCanvasTemplate.displayName)
             append(settings.selectedTypography.name)
             append(settings.primaryColor)
             append(settings.accentColor)
             append(settings.enableGradientHeader)
             append(settings.headerGradientEndColor)
             append(settings.enableRoundedCorners)
             append(settings.enableAlternatingRowColors)
             append(settings.enableDividers)
             append(settings.dividerStyle.name)
             append(settings.highlightTotals)
             append(settings.enableStatusBadges)
             append(settings.enableBackgroundPattern)
             append(settings.enableWatermarkText)
         }.hashCode().toString()
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
                PdfEngine.SASS_PROFESSIONAL -> InvoiceTheme.HTML_PDF  // SASS also uses HTML rendering
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

    // ═══════════════════════════════════════════════════════════════════════════════
    // ✨ PHASE 2: VISUAL CUSTOMIZATION LAYER METHODS (20+ Options)
    // ═══════════════════════════════════════════════════════════════════════════════

    // GRADIENT & ACCENT (2 methods)
    fun toggleGradientHeader(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableGradientHeader = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateHeaderGradientEndColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(headerGradientEndColor = color)
            )
        }
        debouncedGeneratePreview()
    }

    // SHAPE & SHADOW (4 methods)
    fun toggleRoundedCorners(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableRoundedCorners = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateCornerRadius(radiusDp: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(cornerRadiusDp = radiusDp.coerceIn(0f, 16f))
            )
        }
        debouncedGeneratePreview()
    }

    fun toggleShadows(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableShadows = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateShadowIntensity(intensity: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(shadowIntensity = intensity.coerceIn(0f, 1f))
            )
        }
        debouncedGeneratePreview()
    }

    // ROW STYLING (2 methods)
    fun toggleAlternatingRowColors(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableAlternatingRowColors = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateAlternateRowColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(alternateRowColor = color)
            )
        }
        debouncedGeneratePreview()
    }

    // DIVIDER OPTIONS (4 methods)
    fun toggleDividers(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableDividers = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateDividerStyle(style: DividerStyle) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(dividerStyle = style)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateDividerColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(dividerColor = color)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateDividerThickness(thicknessPx: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(dividerThicknessPx = thicknessPx.coerceIn(0.5f, 4f))
            )
        }
        debouncedGeneratePreview()
    }

    // HIGHLIGHT OPTIONS (4 methods)
    fun toggleHighlightTotals(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(highlightTotals = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateTotalBoxStyle(style: TotalBoxStyle) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(totalBoxStyle = style)
            )
        }
        debouncedGeneratePreview()
    }

    fun toggleStatusBadges(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableStatusBadges = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateBadgeStyle(style: BadgeStyle) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(badgeStyle = style)
            )
        }
        debouncedGeneratePreview()
    }

    // BACKGROUND PATTERN OPTIONS (3 methods)
    fun toggleBackgroundPattern(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableBackgroundPattern = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateBackgroundPatternType(pattern: BackgroundPattern) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(backgroundPatternType = pattern)
            )
        }
        debouncedGeneratePreview()
    }

    fun updatePatternOpacity(opacity: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(patternOpacity = opacity.coerceIn(0f, 1f))
            )
        }
        debouncedGeneratePreview()
    }

    // WATERMARK OPTIONS (3 methods)
    fun toggleWatermarkText(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableWatermarkText = enable)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateWatermarkText(text: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(watermarkText = text)
            )
        }
        debouncedGeneratePreview()
    }

    fun updateWatermarkOpacity(opacity: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(watermarkOpacity = opacity.coerceIn(0f, 1f))
            )
        }
        debouncedGeneratePreview()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // ✨ PHASE 3: BRANDING LAYER METHODS (15+ Options)
    // ═══════════════════════════════════════════════════════════════════════════════

    // LOGO (5 methods)
    fun toggleLogo(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableLogo = enable)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateLogoUri(uri: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(logoUri = uri)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateLogoWidth(widthMm: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(logoWidthMm = widthMm.coerceIn(0f, 100f))
            )
        }
        intelligentGeneratePreview()
    }

    fun updateLogoHeight(heightMm: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(logoHeightMm = heightMm.coerceIn(0f, 100f))
            )
        }
        intelligentGeneratePreview()
    }

    fun updateLogoPosition(position: LogoPosition) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(logoPosition = position)
            )
        }
        intelligentGeneratePreview()
    }

    // MOTTO (4 methods)
    fun toggleMotto(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableMotto = enable)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateMottoText(text: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(mottoText = text.take(100))
            )
        }
        intelligentGeneratePreview()
    }

    fun updateMottoFontSize(size: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(mottoFontSize = size.coerceIn(8f, 18f))
            )
        }
        intelligentGeneratePreview()
    }

    fun updateMottoColor(color: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(mottoColor = color)
            )
        }
        intelligentGeneratePreview()
    }

    // PAYMENT ICONS (3 methods)
    fun togglePaymentIcons(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enablePaymentIcons = enable)
            )
        }
        intelligentGeneratePreview()
    }

    fun updatePaymentMethods(methods: List<PaymentMethod>) {
        _uiState.value.settings?.let { current ->
            val json = methods.joinToString(",", "[", "]") { "\"${it.name}\"" }
            _uiState.value = _uiState.value.copy(
                settings = current.copy(acceptedPaymentMethodsJson = json)
            )
        }
        intelligentGeneratePreview()
    }

    fun updatePaymentIconsSize(sizeMm: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(paymentIconsSize = sizeMm.coerceIn(8f, 24f))
            )
        }
        intelligentGeneratePreview()
    }

    // SIGNATURE (3 methods)
    fun toggleSignatureArea(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableSignatureArea = enable)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateSignatureLabel(label: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(signatureLabel = label)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateSignatureLineLength(lengthMm: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(signatureLineLengthMm = lengthMm.coerceIn(20f, 80f))
            )
        }
        intelligentGeneratePreview()
    }

    // QR CODE (4 methods)
    fun toggleQrCode(enable: Boolean) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(enableQrCode = enable)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateQrCodeContent(content: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(qrCodeContent = content)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateQrCodeSize(sizeMm: Float) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(qrCodeSizeMm = sizeMm.coerceIn(10f, 50f))
            )
        }
        intelligentGeneratePreview()
    }

    fun updateQrCodePosition(position: QrCodePosition) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(qrCodePosition = position)
            )
        }
        intelligentGeneratePreview()
    }

    // COMPANY INFO (3 methods)
    fun updateCompanyMotto(motto: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(companyMotto = motto)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateCompanyWebsite(website: String) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(companyWebsite = website)
            )
        }
        intelligentGeneratePreview()
    }

    fun updateSocialMediaHandle(platform: String, handle: String) {
        _uiState.value.settings?.let { current ->
            // Parse existing JSON and update
            val updatedJson = current.companySocialMediaJson // Parse and update logic
            _uiState.value = _uiState.value.copy(
                settings = current.copy(companySocialMediaJson = updatedJson)
            )
        }
        intelligentGeneratePreview()
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SAVE & RESET METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

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

    /**
     * Update selected color scheme for invoice PDF styling.
     */
    fun updateSelectedColorScheme(colorScheme: ColorScheme) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedColorScheme = colorScheme)
            )
        }
        Timber.d("🎨 Color scheme updated: ${colorScheme.displayName}")
        debouncedGeneratePreview()
    }

    /**
     * Update selected spacing profile for invoice PDF layout.
     */
    fun updateSelectedSpacingProfile(spacingProfile: SpacingProfile) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedSpacingProfile = spacingProfile)
            )
        }
        Timber.d("📐 Spacing profile updated: ${spacingProfile.displayName}")
        debouncedGeneratePreview()
    }

    /**
     * Update visual accents (borders, shadows, dividers, highlights, gradients).
     */
    fun updateVisualAccents(accents: VisualAccents) {
        _uiState.value.settings?.let { current ->
            _uiState.value = _uiState.value.copy(
                settings = current.copy(visualAccentsJson = accents.toJsonString())
            )
        }
        Timber.d("✨ Visual accents updated")
        debouncedGeneratePreview()
    }

    /**
     * Toggle a specific visual accent by name.
     */
    fun toggleVisualAccent(accentName: String) {
        _uiState.value.settings?.let { current ->
            val currentAccents = current.getVisualAccents()
            val updated = when (accentName) {
                "showBorders" -> currentAccents.copy(showBorders = !currentAccents.showBorders)
                "showShadows" -> currentAccents.copy(showShadows = !currentAccents.showShadows)
                "showDividers" -> currentAccents.copy(showDividers = !currentAccents.showDividers)
                "highlightTotals" -> currentAccents.copy(highlightTotals = !currentAccents.highlightTotals)
                "useGradients" -> currentAccents.copy(useGradients = !currentAccents.useGradients)
                else -> currentAccents
            }
            updateVisualAccents(updated)
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
