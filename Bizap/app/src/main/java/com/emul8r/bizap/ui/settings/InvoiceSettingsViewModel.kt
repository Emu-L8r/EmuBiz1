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
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
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
            _uiState.value = _uiState.value.copy(
                settings = current.copy(selectedPdfEngine = engine)
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

    /**
     * Generate live preview HTML using placeholder data and current settings.
     * Supports both Canvas and HTML engines with appropriate previews.
     * PHASE 4 (Approach 3): Canvas preview generation
     */
    fun generatePreview() {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings ?: return@launch
            try {
                val previewSnapshot = PlaceholderInvoiceGenerator.generatePreviewInvoice()

                when (currentSettings.selectedTheme) {
                    InvoiceTheme.CANVAS -> {
                        // ✨ PHASE 4 (Approach 3): Generate Canvas preview HTML
                        val canvasHtml = generateCanvasPreviewHtml(previewSnapshot, currentSettings)
                        _previewHtml.value = canvasHtml
                        Timber.d("✅ Canvas preview generated: ${currentSettings.selectedCanvasTemplate.displayName}")
                    }
                    InvoiceTheme.HTML_PDF -> {
                        // Existing HTML preview generation
                        val htmlService = HtmlPdfInvoiceService(context, currentSettings)
                        val html = htmlService.buildPreviewHtml(previewSnapshot, isQuote = false)
                        _previewHtml.value = html
                        Timber.d("✅ HTML preview generated: ${currentSettings.selectedHtmlStyle}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to generate live preview")
            }
        }
    }

    /**
     * ✨ NEW METHOD (Approach 3): Generate Canvas template preview HTML
     * Creates visual representation of Canvas template with colors and styling
     */
    private fun generateCanvasPreviewHtml(
        snapshot: InvoiceSnapshot,
        settings: InvoiceSettings
    ): String {
        val template = settings.selectedCanvasTemplate
        val primaryColor = template.primaryHex
        val accentColor = template.accentHex

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Invoice Preview</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                        background: #f5f5f5;
                        padding: 20px;
                        line-height: 1.6;
                    }
                    
                    .invoice {
                        max-width: 850px;
                        margin: 0 auto;
                        background: white;
                        padding: 40px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                    }
                    
                    .header {
                        border-bottom: 3px solid $primaryColor;
                        padding-bottom: 20px;
                        margin-bottom: 30px;
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-start;
                    }
                    
                    .company-info h1 {
                        color: $primaryColor;
                        font-size: 32px;
                        margin-bottom: 4px;
                    }
                    
                    .company-info p {
                        color: #666;
                        font-size: 13px;
                    }
                    
                    .invoice-title {
                        text-align: right;
                    }
                    
                    .invoice-title h2 {
                        color: $accentColor;
                        font-size: 24px;
                        font-weight: bold;
                        margin-bottom: 8px;
                    }
                    
                    .details-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 20px;
                        padding-bottom: 20px;
                        border-bottom: 1px solid #e0e0e0;
                    }
                    
                    .detail-column {
                        flex: 1;
                    }
                    
                    .detail-label {
                        color: $primaryColor;
                        font-weight: bold;
                        font-size: 12px;
                        text-transform: uppercase;
                        margin-bottom: 4px;
                    }
                    
                    .detail-value {
                        color: #333;
                        font-size: 14px;
                    }
                    
                    .items-section {
                        margin: 30px 0;
                    }
                    
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 20px;
                    }
                    
                    thead {
                        background: $primaryColor;
                        color: white;
                    }
                    
                    th {
                        padding: 12px;
                        text-align: left;
                        font-weight: 600;
                        font-size: 13px;
                    }
                    
                    td {
                        padding: 12px;
                        border-bottom: 1px solid #e0e0e0;
                        font-size: 14px;
                    }
                    
                    tbody tr:last-child td {
                        border-bottom: none;
                    }
                    
                    .amount {
                        text-align: right;
                        font-weight: 500;
                    }
                    
                    .totals {
                        display: flex;
                        justify-content: flex-end;
                        margin-top: 30px;
                    }
                    
                    .totals-column {
                        width: 250px;
                    }
                    
                    .total-row {
                        display: flex;
                        justify-content: space-between;
                        padding: 10px 0;
                        border-bottom: 1px solid #e0e0e0;
                        font-size: 14px;
                    }
                    
                    .total-row:last-child {
                        border-bottom: 2px solid $primaryColor;
                        padding-bottom: 12px;
                    }
                    
                    .total-label {
                        color: #666;
                    }
                    
                    .total-amount {
                        text-align: right;
                        font-weight: 500;
                    }
                    
                    .total-due-row {
                        background: $accentColor;
                        color: white;
                        padding: 15px;
                        margin-top: 10px;
                        border-radius: 4px;
                        font-size: 18px;
                        font-weight: bold;
                        display: flex;
                        justify-content: space-between;
                    }
                    
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #e0e0e0;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                    }
                    
                    .template-badge {
                        background: $primaryColor;
                        color: white;
                        padding: 6px 12px;
                        border-radius: 20px;
                        font-size: 11px;
                        font-weight: bold;
                        margin-top: 20px;
                        display: inline-block;
                    }
                </style>
            </head>
            <body>
                <div class="invoice">
                    <div class="header">
                        <div class="company-info">
                            <h1>ACME Corp</h1>
                            <p>ABN: 12 345 678 901</p>
                            <p>contact@example.com</p>
                        </div>
                        <div class="invoice-title">
                            <h2>INVOICE</h2>
                            <div class="detail-value">#INV-2026-001</div>
                        </div>
                    </div>
                    
                    <div class="details-row">
                        <div class="detail-column">
                            <div class="detail-label">Bill To</div>
                            <div class="detail-value">
                                <strong>Client Name</strong><br>
                                123 Business Street<br>
                                Suite 100, City, ST 12345
                            </div>
                        </div>
                        <div class="detail-column" style="text-align: right;">
                            <div class="detail-label">Date</div>
                            <div class="detail-value">April 4, 2026</div>
                            <div class="detail-label" style="margin-top: 12px;">Due Date</div>
                            <div class="detail-value">April 18, 2026</div>
                        </div>
                    </div>
                    
                    <div class="items-section">
                        <table>
                            <thead>
                                <tr>
                                    <th>Description</th>
                                    <th style="text-align: center;">Qty</th>
                                    <th style="text-align: right;">Unit Price</th>
                                    <th style="text-align: right;">Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Professional Services - Consulting</td>
                                    <td style="text-align: center;">40</td>
                                    <td class="amount">${'$'}125.00</td>
                                    <td class="amount">${'$'}5,000.00</td>
                                </tr>
                                <tr>
                                    <td>Software Development</td>
                                    <td style="text-align: center;">80</td>
                                    <td class="amount">${'$'}150.00</td>
                                    <td class="amount">${'$'}12,000.00</td>
                                </tr>
                                <tr>
                                    <td>Project Management & Support</td>
                                    <td style="text-align: center;">30</td>
                                    <td class="amount">${'$'}100.00</td>
                                    <td class="amount">${'$'}3,000.00</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="totals">
                        <div class="totals-column">
                            <div class="total-row">
                                <span class="total-label">Subtotal</span>
                                <span class="total-amount">${'$'}20,000.00</span>
                            </div>
                            <div class="total-row">
                                <span class="total-label">Tax (10%)</span>
                                <span class="total-amount">${'$'}2,000.00</span>
                            </div>
                            <div class="total-due-row">
                                <span>TOTAL DUE</span>
                                <span>${'$'}22,000.00</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Payment Terms:</strong> Due within 14 days of invoice date</p>
                        <p>Thank you for your business!</p>
                        <div class="template-badge">
                            Preview: ${template.displayName}
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
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
