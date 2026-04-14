package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.webkit.WebSettings
import android.webkit.WebView
import com.emul8r.bizap.domain.model.CanvasInvoiceTemplate
import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.model.InvoiceLocale
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.SpacingProfile
import com.emul8r.bizap.domain.model.Typography
import com.emul8r.bizap.domain.model.VisualAccents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewHtml by viewModel.previewHtml.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✨ PHASE 1: Derive stable preview state key for responsive recomposition
    val previewStateKey = remember(
        uiState.settings?.selectedPdfEngine,
        uiState.settings?.selectedPageLayout,
        uiState.settings?.selectedHtmlStyle,
        uiState.settings?.selectedCanvasTemplate,
        uiState.settings?.primaryColor,
        uiState.settings?.accentColor,
        uiState.settings?.enableGradientHeader,
        uiState.settings?.enableRoundedCorners,
        uiState.settings?.enableAlternatingRowColors,
        uiState.settings?.enableDividers,
        uiState.settings?.dividerStyle,
        uiState.settings?.highlightTotals,
        uiState.settings?.enableStatusBadges,
        uiState.settings?.enableBackgroundPattern,
        uiState.settings?.enableWatermarkText
    ) {
        viewModel.getPreviewStateKey()
    }

    // ✅ UX IMPROVEMENT: Tab state for intuitive navigation
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Quick Setup", "Design", "Advanced")

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.settings == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // ✅ VISUAL GUIDE: Tab navigation for better UX
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                // ✅ SMOOTH TRANSITIONS: Crossfade between tabs
                Crossfade(
                    targetState = selectedTabIndex,
                    modifier = Modifier.fillMaxSize(),
                    label = "Settings tab transition"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> QuickSetupTab(viewModel = viewModel, uiState = uiState)
                        1 -> DesignTab(viewModel = viewModel, uiState = uiState, previewHtml = previewHtml, previewStateKey = previewStateKey)
                        2 -> AdvancedTab(viewModel = viewModel, uiState = uiState)
                    }
                }
            }
        }
    }
}

/**
 * ✅ QUICK SETUP TAB: Simplified for new users
 * Only shows essential settings to get started quickly
 */
@Composable
private fun QuickSetupTab(
    viewModel: InvoiceSettingsViewModel,
    uiState: InvoiceSettingsUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Getting Started",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Choose how you want your invoices to look",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PdfEngineSection(
                selectedEngine = uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS,
                onEngineSelected = { viewModel.updateSelectedPdfEngine(it) }
            )
        }

        item {
            val selectedEngine = uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS

            // Only show template selection for Canvas and HTML_CSS engines
            // SASS_PROFESSIONAL is a complete template, no template selection needed
            if (selectedEngine != PdfEngine.SASS_PROFESSIONAL) {
                val currentTheme = when (selectedEngine) {
                    PdfEngine.HTML_CSS -> InvoiceTheme.HTML_PDF
                    PdfEngine.CANVAS   -> InvoiceTheme.CANVAS
                    PdfEngine.SASS_PROFESSIONAL -> InvoiceTheme.HTML_PDF  // Fallback
                }
                TemplateSelectionSection(
                    currentTheme = currentTheme,
                    selectedCanvasTemplate = uiState.settings?.selectedCanvasTemplate ?: CanvasInvoiceTemplate.MODERN,
                    selectedHtmlStyle = uiState.settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN,
                    onCanvasTemplateSelected = { viewModel.updateSelectedCanvasTemplate(it) },
                    onHtmlStyleSelected = { viewModel.updateSelectedHtmlStyle(it) }
                )
            } else {
                // SASS Professional info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "✨ SASS Professional Template",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Using premium two-column layout with professional branding. Customize colors, spacing, and visual accents in the Design tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "💡 Tip",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Visit the Design tab for typography options and preview. Advanced tab has more settings.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

/**
 * ✅ DESIGN TAB: For users who want to customize appearance
 */
@Composable
private fun DesignTab(
    viewModel: InvoiceSettingsViewModel,
    uiState: InvoiceSettingsUiState,
    previewHtml: String?,
    previewStateKey: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                "Customize Your Look",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            uiState.settings?.let { settings ->
                PageLayoutSection(
                    selectedLayout = settings.selectedPageLayout,
                    onLayoutSelected = { viewModel.updateSelectedPageLayout(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                TypographySection(
                    selectedTypography = settings.selectedTypography,
                    onTypographySelected = { viewModel.updateSelectedTypography(it) }
                )
            }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // PHASE 3: NEW CUSTOMIZATION LAYERS
        // ─────────────────────────────────────────────────────────────────────────

        item {
            uiState.settings?.let { settings ->
                ColorSchemeSection(
                    selectedColorScheme = settings.selectedColorScheme,
                    onColorSchemeSelected = { viewModel.updateSelectedColorScheme(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                SpacingProfileSection(
                    selectedSpacingProfile = settings.selectedSpacingProfile,
                    onSpacingProfileSelected = { viewModel.updateSelectedSpacingProfile(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                VisualAccentsSection(
                    accents = settings.getVisualAccents(),
                    onAccentsChanged = { viewModel.updateVisualAccents(it) }
                )
            }
        }

        // ─────────────────────────────────────────────────────────────────────────

        item {
            // ✨ PHASE 1: Use unified preview with stable state key for responsiveness
            UnifiedPdfPreview(
                previewHtml = previewHtml,
                onRefresh = { viewModel.generatePreview() },
                selectedEngine = uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS,
                selectedCanvasTemplate = uiState.settings?.selectedCanvasTemplate ?: CanvasInvoiceTemplate.MODERN,
                previewStateKey = previewStateKey
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

/**
 * ✅ ADVANCED TAB: For power users who need fine-tuning
 */
@Composable
private fun AdvancedTab(
    viewModel: InvoiceSettingsViewModel,
    uiState: InvoiceSettingsUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                "Advanced Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            uiState.settings?.let { settings ->
                VisibilityTogglesSection(
                    showBusinessAbn = settings.showBusinessAbn,
                    showCustomerPhone = settings.showCustomerPhone,
                    showStatusWatermark = settings.showStatusWatermark,
                    showPageNumbers = settings.showPageNumbers,
                    onShowBusinessAbnChanged = { viewModel.toggleShowBusinessAbn(it) },
                    onShowCustomerPhoneChanged = { viewModel.toggleShowCustomerPhone(it) },
                    onShowStatusWatermarkChanged = { viewModel.toggleShowStatusWatermark(it) },
                    onShowPageNumbersChanged = { viewModel.toggleShowPageNumbers(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                LocaleSelectionSection(
                    selectedLocale = settings.selectedLocale,
                    onLocaleSelected = { viewModel.updateSelectedLocale(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                PaymentSection(
                    paymentTermsDays = settings.paymentTermsDays,
                    onPaymentTermsChanged = { viewModel.updatePaymentTermsDays(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                TaxSection(
                    taxRate = settings.taxRate,
                    taxName = settings.taxName,
                    onTaxRateChanged = { viewModel.updateTaxRate(it) },
                    onTaxNameChanged = { viewModel.updateTaxName(it) }
                )
            }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // ✨ PHASE 2: VISUAL CUSTOMIZATION SECTIONS (20+ Options)
        // ─────────────────────────────────────────────────────────────────────────

        item {
            HorizontalDivider()
            Text(
                "✨ Visual Enhancements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        item {
            uiState.settings?.let { settings ->
                GradientAccentSection(
                    enableGradientHeader = settings.enableGradientHeader,
                    headerGradientEndColor = settings.headerGradientEndColor,
                    onGradientToggled = { viewModel.toggleGradientHeader(it) },
                    onGradientColorChanged = { viewModel.updateHeaderGradientEndColor(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                ShapeShadowSection(
                    enableRoundedCorners = settings.enableRoundedCorners,
                    cornerRadiusDp = settings.cornerRadiusDp,
                    enableShadows = settings.enableShadows,
                    shadowIntensity = settings.shadowIntensity,
                    onRoundedCornersToggled = { viewModel.toggleRoundedCorners(it) },
                    onCornerRadiusChanged = { viewModel.updateCornerRadius(it) },
                    onShadowsToggled = { viewModel.toggleShadows(it) },
                    onShadowIntensityChanged = { viewModel.updateShadowIntensity(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                RowStylingSection(
                    enableAlternatingRowColors = settings.enableAlternatingRowColors,
                    alternateRowColor = settings.alternateRowColor,
                    onAlternatingToggled = { viewModel.toggleAlternatingRowColors(it) },
                    onColorChanged = { viewModel.updateAlternateRowColor(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                DividerOptionsSection(
                    enableDividers = settings.enableDividers,
                    dividerStyle = settings.dividerStyle,
                    dividerColor = settings.dividerColor,
                    dividerThicknessPx = settings.dividerThicknessPx,
                    onDividersToggled = { viewModel.toggleDividers(it) },
                    onStyleChanged = { viewModel.updateDividerStyle(it) },
                    onColorChanged = { viewModel.updateDividerColor(it) },
                    onThicknessChanged = { viewModel.updateDividerThickness(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                HighlightOptionsSection(
                    highlightTotals = settings.highlightTotals,
                    totalBoxStyle = settings.totalBoxStyle,
                    enableStatusBadges = settings.enableStatusBadges,
                    badgeStyle = settings.badgeStyle,
                    onHighlightToggled = { viewModel.toggleHighlightTotals(it) },
                    onTotalBoxStyleChanged = { viewModel.updateTotalBoxStyle(it) },
                    onBadgesToggled = { viewModel.toggleStatusBadges(it) },
                    onBadgeStyleChanged = { viewModel.updateBadgeStyle(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                BackgroundPatternSection(
                    enableBackgroundPattern = settings.enableBackgroundPattern,
                    backgroundPatternType = settings.backgroundPatternType,
                    patternOpacity = settings.patternOpacity,
                    onPatternToggled = { viewModel.toggleBackgroundPattern(it) },
                    onPatternTypeChanged = { viewModel.updateBackgroundPatternType(it) },
                    onOpacityChanged = { viewModel.updatePatternOpacity(it) }
                )
            }
        }

        item {
            uiState.settings?.let { settings ->
                WatermarkSection(
                    enableWatermarkText = settings.enableWatermarkText,
                    watermarkText = settings.watermarkText,
                    watermarkOpacity = settings.watermarkOpacity,
                    onWatermarkToggled = { viewModel.toggleWatermarkText(it) },
                    onWatermarkTextChanged = { viewModel.updateWatermarkText(it) },
                    onOpacityChanged = { viewModel.updateWatermarkOpacity(it) }
                )
            }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // ✨ PHASE 3: BRANDING CUSTOMIZATION SECTIONS
        // ─────────────────────────────────────────────────────────────────────────

        item {
            HorizontalDivider()
            Text(
                "🎨 Brand Your Invoices",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(
                "Customize logos, colors, payment methods, QR codes, and more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ✨ Logo Section
        item {
            uiState.settings?.let { settings ->
                LogoSection(
                    enableLogo = settings.enableLogo,
                    logoUri = settings.logoUri,
                    logoWidthMm = settings.logoWidthMm,
                    logoHeightMm = settings.logoHeightMm,
                    logoPosition = settings.logoPosition,
                    onLogoToggled = { viewModel.toggleLogo(it) },
                    onLogoSelected = { viewModel.updateLogoUri(it) },
                    onWidthChanged = { viewModel.updateLogoWidth(it) },
                    onHeightChanged = { viewModel.updateLogoHeight(it) },
                    onPositionChanged = { viewModel.updateLogoPosition(it) }
                )
            }
        }

        // ✨ Motto Section
        item {
            uiState.settings?.let { settings ->
                MottoSection(
                    enableMotto = settings.enableMotto,
                    mottoText = settings.mottoText,
                    mottoFontSize = settings.mottoFontSize,
                    mottoColor = settings.mottoColor,
                    onMottoToggled = { viewModel.toggleMotto(it) },
                    onMottoTextChanged = { viewModel.updateMottoText(it) },
                    onFontSizeChanged = { viewModel.updateMottoFontSize(it) },
                    onColorChanged = { viewModel.updateMottoColor(it) }
                )
            }
        }

        // ✨ Payment Icons Section
        item {
            uiState.settings?.let { settings ->
                PaymentIconsSection(
                    enablePaymentIcons = settings.enablePaymentIcons,
                    acceptedPaymentMethodsJson = settings.acceptedPaymentMethodsJson,
                    paymentIconsSize = settings.paymentIconsSize,
                    onPaymentIconsToggled = { viewModel.togglePaymentIcons(it) },
                    onPaymentMethodsChanged = { viewModel.updatePaymentMethods(it) },
                    onSizeChanged = { viewModel.updatePaymentIconsSize(it) }
                )
            }
        }

        // ✨ Signature Section
        item {
            uiState.settings?.let { settings ->
                SignatureSection(
                    enableSignatureArea = settings.enableSignatureArea,
                    signatureLabel = settings.signatureLabel,
                    signatureLineLengthMm = settings.signatureLineLengthMm,
                    onSignatureToggled = { viewModel.toggleSignatureArea(it) },
                    onLabelChanged = { viewModel.updateSignatureLabel(it) },
                    onLineLengthChanged = { viewModel.updateSignatureLineLength(it) }
                )
            }
        }

        // ✨ QR Code Section
        item {
            uiState.settings?.let { settings ->
                QrCodeSection(
                    enableQrCode = settings.enableQrCode,
                    qrCodeContent = settings.qrCodeContent,
                    qrCodeSizeMm = settings.qrCodeSizeMm,
                    qrCodePosition = settings.qrCodePosition,
                    onQrToggled = { viewModel.toggleQrCode(it) },
                    onContentChanged = { viewModel.updateQrCodeContent(it) },
                    onSizeChanged = { viewModel.updateQrCodeSize(it) },
                    onPositionChanged = { viewModel.updateQrCodePosition(it) }
                )
            }
        }

        // ✨ Company Info Section
        item {
            uiState.settings?.let { settings ->
                CompanyInfoSection(
                    companyMotto = settings.companyMotto,
                    companyWebsite = settings.companyWebsite,
                    companySocialMediaJson = settings.companySocialMediaJson,
                    onMottoChanged = { viewModel.updateCompanyMotto(it) },
                    onWebsiteChanged = { viewModel.updateCompanyWebsite(it) },
                    onSocialMediaChanged = { platform, handle -> viewModel.updateSocialMediaHandle(platform, handle) }
                )
            }
        }

        // ✨ Branding Preview Section
        item {
            uiState.settings?.let { settings ->
                BrandingPreviewSection(
                    enableLogo = settings.enableLogo,
                    enableMotto = settings.enableMotto,
                    enablePaymentIcons = settings.enablePaymentIcons,
                    enableQrCode = settings.enableQrCode,
                    enableSignatureArea = settings.enableSignatureArea,
                    mottoText = settings.mottoText,
                    paymentIconsCount = try {
                        settings.acceptedPaymentMethodsJson.split(",").filter { it.isNotBlank() }.size
                    } catch (e: Exception) {
                        0
                    },
                    qrCodeContent = settings.qrCodeContent,
                    signatureLabel = settings.signatureLabel,
                    companySocialMediaJson = settings.companySocialMediaJson
                )
            }
        }

        // ─────────────────────────────────────────────────────────────────────────

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Column {
        Text("PDF Invoice Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Choose your PDF engine and template style", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Section 2: Grid of 4 template cards, swapping between Canvas and HTML templates. */
@Composable
fun TemplateSelectionSection(
    currentTheme: InvoiceTheme,
    selectedCanvasTemplate: CanvasInvoiceTemplate,
    selectedHtmlStyle: HtmlInvoiceStyle,
    onCanvasTemplateSelected: (CanvasInvoiceTemplate) -> Unit,
    onHtmlStyleSelected: (HtmlInvoiceStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val (titleText, descriptionText) = if (currentTheme == InvoiceTheme.CANVAS) {
            "2️⃣  Brand Palette" to "Choose your color palette for Canvas invoices"
        } else {
            "2️⃣  Invoice Style" to "Choose your HTML invoice template style"
        }

        Text(
            titleText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            descriptionText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (currentTheme == InvoiceTheme.CANVAS) {
            // 2×2 grid of Canvas templates
            val templates = CanvasInvoiceTemplate.values()
            for (i in templates.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CanvasTemplateCard(
                        template = templates[i],
                        isSelected = selectedCanvasTemplate == templates[i],
                        modifier = Modifier.weight(1f),
                        onClick = { onCanvasTemplateSelected(templates[i]) }
                    )
                    if (i + 1 < templates.size) {
                        CanvasTemplateCard(
                            template = templates[i + 1],
                            isSelected = selectedCanvasTemplate == templates[i + 1],
                            modifier = Modifier.weight(1f),
                            onClick = { onCanvasTemplateSelected(templates[i + 1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // 2×2 grid of HTML templates
            val styles = HtmlInvoiceStyle.values()
            for (i in styles.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HtmlStyleCard(
                        style = styles[i],
                        isSelected = selectedHtmlStyle == styles[i],
                        modifier = Modifier.weight(1f),
                        onClick = { onHtmlStyleSelected(styles[i]) }
                    )
                    if (i + 1 < styles.size) {
                        HtmlStyleCard(
                            style = styles[i + 1],
                            isSelected = selectedHtmlStyle == styles[i + 1],
                            modifier = Modifier.weight(1f),
                            onClick = { onHtmlStyleSelected(styles[i + 1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasTemplateCard(
    template: CanvasInvoiceTemplate,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(template.primaryHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val accentColor = try {
        Color(android.graphics.Color.parseColor(template.accentHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.secondary
    }

    val border = if (isSelected) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Color swatch preview
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryColor)
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                template.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                template.colorScheme,
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor
            )
            Text(
                template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HtmlStyleCard(
    style: HtmlInvoiceStyle,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accentColor = when (style) {
        HtmlInvoiceStyle.MODERN                -> MaterialTheme.colorScheme.primary
        HtmlInvoiceStyle.MINIMAL               -> Color(0xFF2C3E50)
        HtmlInvoiceStyle.CORPORATE             -> Color(0xFF003366)
        HtmlInvoiceStyle.CREATIVE              -> Color(0xFF00A8A8)
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL  -> Color(0xFF2563EB)
        HtmlInvoiceStyle.WARM_APPROACHABLE     -> Color(0xFFF59E0B)
        HtmlInvoiceStyle.SASS_PROFESSIONAL     -> Color(0xFF0A2540)
        HtmlInvoiceStyle.REFINED               -> Color(0xFF6B4C9A)  // Purple from REFINED template
        HtmlInvoiceStyle.PROFESSIONAL_PLUS     -> Color(0xFF1A1A2E)  // Charcoal from PROFESSIONAL_PLUS
    }
    val secondaryColor = when (style) {
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL  -> Color(0xFF1C1C2E)
        HtmlInvoiceStyle.WARM_APPROACHABLE     -> Color(0xFF1F2937)
        HtmlInvoiceStyle.SASS_PROFESSIONAL     -> Color(0xFFF7F9FC)
        HtmlInvoiceStyle.PROFESSIONAL_PLUS     -> Color(0xFF00C9A7)  // Teal accent
        else                                   -> null
    }

    val border = if (isSelected) BorderStroke(2.dp, accentColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
                if (secondaryColor != null) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(secondaryColor)
                    )
                }
                if (isSelected) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                style.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                style.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
fun PaymentSection(paymentTermsDays: Int, onPaymentTermsChanged: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("8️⃣  Payment Terms (Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { it.toIntOrNull()?.let(onPaymentTermsChanged) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun TaxSection(taxRate: Double, taxName: String, onTaxRateChanged: (Double) -> Unit, onTaxNameChanged: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("9️⃣  Tax Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = taxName, onValueChange = onTaxNameChanged, label = { Text("Tax Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { it.toDoubleOrNull()?.let(onTaxRateChanged) },
                label = { Text("Tax Rate") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(onSave: () -> Unit, onReset: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🔟 Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save Settings") }
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Reset to Defaults") }
    }
}

// ============================================================================
// PHASE 2: NEW FIVE-SECTION UI COMPONENTS
// ============================================================================

/**
 * SECTION 1: PDF Engine Selection (Canvas vs HTML+CSS)
 */
@Composable
fun PdfEngineSection(
    selectedEngine: PdfEngine,
    onEngineSelected: (PdfEngine) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "1️⃣  PDF Rendering Engine",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Select how your PDF invoices are generated (3 professional options)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EngineOptionCard(
                title = "Canvas",
                emoji = "🎨",
                description = "Artistic coordinate control",
                isSelected = selectedEngine == PdfEngine.CANVAS,
                modifier = Modifier.weight(1f),
                onClick = { onEngineSelected(PdfEngine.CANVAS) }
            )

            EngineOptionCard(
                title = "HTML+CSS",
                emoji = "📄",
                description = "Modern CSS styling",
                isSelected = selectedEngine == PdfEngine.HTML_CSS,
                modifier = Modifier.weight(1f),
                onClick = { onEngineSelected(PdfEngine.HTML_CSS) }
            )

            EngineOptionCard(
                title = "SASS Pro",
                emoji = "✨",
                description = "Premium two-column layout",
                isSelected = selectedEngine == PdfEngine.SASS_PROFESSIONAL,
                modifier = Modifier.weight(1f),
                onClick = { onEngineSelected(PdfEngine.SASS_PROFESSIONAL) }
            )
        }
    }
}

@Composable
private fun EngineOptionCard(
    title: String,
    emoji: String,
    description: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val border = if (isSelected) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * SECTION 3: Page Layout Selection (Classic vs Modern vs Spacious vs Compact)
 */
@Composable
fun PageLayoutSection(
    selectedLayout: PageLayout,
    onLayoutSelected: (PageLayout) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "3️⃣  Page Layout",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose how invoice content is organized (8 professional layouts)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: Classic, Modern, Spacious, Compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LayoutOptionCard(
                layout = PageLayout.CLASSIC,
                isSelected = selectedLayout == PageLayout.CLASSIC,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.CLASSIC) }
            )
            LayoutOptionCard(
                layout = PageLayout.MODERN,
                isSelected = selectedLayout == PageLayout.MODERN,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.MODERN) }
            )
            LayoutOptionCard(
                layout = PageLayout.SPACIOUS,
                isSelected = selectedLayout == PageLayout.SPACIOUS,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.SPACIOUS) }
            )
            LayoutOptionCard(
                layout = PageLayout.COMPACT,
                isSelected = selectedLayout == PageLayout.COMPACT,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.COMPACT) }
            )
        }

        // Row 2: Sidebar, Cards, Minimal, Focused
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LayoutOptionCard(
                layout = PageLayout.SIDEBAR,
                isSelected = selectedLayout == PageLayout.SIDEBAR,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.SIDEBAR) }
            )
            LayoutOptionCard(
                layout = PageLayout.CARDS,
                isSelected = selectedLayout == PageLayout.CARDS,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.CARDS) }
            )
            LayoutOptionCard(
                layout = PageLayout.MINIMAL_TABLES,
                isSelected = selectedLayout == PageLayout.MINIMAL_TABLES,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.MINIMAL_TABLES) }
            )
            LayoutOptionCard(
                layout = PageLayout.FOCUSED,
                isSelected = selectedLayout == PageLayout.FOCUSED,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.FOCUSED) }
            )
        }
    }
}

@Composable
private fun LayoutOptionCard(
    layout: PageLayout,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val border = if (isSelected) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(layout.emoji, fontSize = 20.sp)
            Text(layout.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(
                layout.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                fontSize = 8.sp
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * SECTION 5: Preview Mode Toggle
 */
@Composable
private fun PreviewModeSection(
    previewWithPlaceholder: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "5️⃣  Preview Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Show sample data without real info",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = previewWithPlaceholder,
                    onCheckedChange = onToggle
                )
            }
            Text(
                if (previewWithPlaceholder) "✅ Preview mode enabled" else "Preview mode disabled",
                style = MaterialTheme.typography.bodySmall,
                color = if (previewWithPlaceholder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ✨ DEPRECATED - PHASE 1 CONSOLIDATION
 *
 * SECTION 4: Live Preview — shows a WebView rendering of the selected invoice style.
 * REPLACED BY: UnifiedPdfPreview.kt for improved responsiveness and code consolidation
 *
 * This function has been consolidated into UnifiedPdfPreview composable for:
 * - Single code path instead of branching by engine type
 * - Stable state key-based recomposition
 * - Immediate preview updates
 * - Easier future maintenance
 */
@Composable
@Deprecated("Use UnifiedPdfPreview instead", ReplaceWith("UnifiedPdfPreview(...)"))
@Suppress("DEPRECATION")
private fun LivePreviewSection(
    previewHtml: String?,
    onRefresh: () -> Unit,
    selectedEngine: PdfEngine = PdfEngine.CANVAS,
    selectedCanvasTemplate: CanvasInvoiceTemplate = CanvasInvoiceTemplate.MODERN
) {
    // Delegate to UnifiedPdfPreview for all rendering
    UnifiedPdfPreview(
        previewHtml = previewHtml,
        onRefresh = onRefresh
    )
}

/**
 * ✨ DEPRECATED - PHASE 1 CONSOLIDATION
 *
 * Canvas template visual preview
 * REPLACED BY: UnifiedPdfPreview.kt
 */
@Composable
@Deprecated("Use UnifiedPdfPreview instead", ReplaceWith("UnifiedPdfPreview(...)"))
private fun CanvasTemplatePreview(
    template: CanvasInvoiceTemplate,
    previewHtml: String?,
    onRefresh: () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(template.primaryHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val accentColor = try {
        Color(android.graphics.Color.parseColor(template.accentHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.secondary
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "4️⃣  Live Preview (Canvas)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${template.displayName} - ${template.colorScheme}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh preview",
                    tint = primaryColor
                )
            }
        }

        // Visual color swatch preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(primaryColor, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Primary", style = MaterialTheme.typography.labelSmall)
                Text(template.primaryHex, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(accentColor, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Accent", style = MaterialTheme.typography.labelSmall)
                Text(template.accentHex, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            }
        }

        // HTML preview of Canvas template
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (previewHtml != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                setSupportZoom(true)
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                            setInitialScale(60)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            null,
                            previewHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Generating preview...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * ✨ DEPRECATED - PHASE 1 CONSOLIDATION
 *
 * HTML preview component
 * REPLACED BY: UnifiedPdfPreview.kt
 */
@Composable
@Deprecated("Use UnifiedPdfPreview instead", ReplaceWith("UnifiedPdfPreview(...)"))
private fun HtmlPreview(
    previewHtml: String?,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "4️⃣  Live Preview (HTML)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh preview",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            "Preview how your invoice will look with the selected style",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ✅ IMPROVEMENT: Add consistency guarantee badge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "This preview uses iText7-optimized CSS matching your generated PDF",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (previewHtml != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                                setSupportZoom(true)
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                            setInitialScale(50)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            null,
                            previewHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Generating preview...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TypographySection(
    selectedTypography: Typography,
    onTypographySelected: (Typography) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "5️⃣  Typography",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose your font style for invoices",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Typography.values().forEach { typography ->
                TypographyOptionCard(
                    title = when (typography) {
                        Typography.MODERN -> "Modern"
                        Typography.CLASSIC -> "Classic"
                        Typography.ROUNDED -> "Rounded"
                    },
                    description = when (typography) {
                        Typography.MODERN -> "Sans-serif"
                        Typography.CLASSIC -> "Serif"
                        Typography.ROUNDED -> "Rounded"
                    },
                    isSelected = selectedTypography == typography,
                    modifier = Modifier.weight(1f),
                    onClick = { onTypographySelected(typography) }
                )
            }
        }
    }
}

@Composable
private fun TypographyOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val border = if (isSelected) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun VisibilityTogglesSection(
    showBusinessAbn: Boolean,
    showCustomerPhone: Boolean,
    showStatusWatermark: Boolean,
    showPageNumbers: Boolean,
    onShowBusinessAbnChanged: (Boolean) -> Unit,
    onShowCustomerPhoneChanged: (Boolean) -> Unit,
    onShowStatusWatermarkChanged: (Boolean) -> Unit,
    onShowPageNumbersChanged: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "6️⃣  Component Visibility",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Choose what information to display on invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            VisibilityToggleItem(
                label = "Show Business ABN",
                checked = showBusinessAbn,
                onCheckedChange = onShowBusinessAbnChanged
            )

            VisibilityToggleItem(
                label = "Show Customer Phone",
                checked = showCustomerPhone,
                onCheckedChange = onShowCustomerPhoneChanged
            )

            VisibilityToggleItem(
                label = "Show Status Watermark (PAID/OVERDUE)",
                checked = showStatusWatermark,
                onCheckedChange = onShowStatusWatermarkChanged
            )

            VisibilityToggleItem(
                label = "Show Page Numbers",
                checked = showPageNumbers,
                onCheckedChange = onShowPageNumbersChanged
            )
        }
    }
}

@Composable
private fun VisibilityToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun LocaleSelectionSection(
    selectedLocale: InvoiceLocale,
    onLocaleSelected: (InvoiceLocale) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "7️⃣  Locale & Currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Select your region for currency symbols and date formats",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dropdown-style locale selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    InvoiceLocale.values().forEach { locale ->
                        LocaleOptionItem(
                            locale = locale,
                            isSelected = selectedLocale == locale,
                            onClick = { onLocaleSelected(locale) }
                        )
                    }
                }
            }

            // Display current selection info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Current: ${selectedLocale.displayName}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Format: ${selectedLocale.currencySymbol} | Date: ${selectedLocale.dateFormat}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LocaleOptionItem(
    locale: InvoiceLocale,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                locale.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "${locale.currencySymbol} | ${locale.dateFormat}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * COLOR SCHEME SECTION - Choose from 6 professional color palettes
 */
@Composable
private fun ColorSchemeSection(
    selectedColorScheme: ColorScheme,
    onColorSchemeSelected: (ColorScheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "4️⃣  Color Scheme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose a color palette for your invoice design",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: Professional, Vibrant, Minimal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSchemeCard(
                scheme = ColorScheme.PROFESSIONAL,
                isSelected = selectedColorScheme == ColorScheme.PROFESSIONAL,
                modifier = Modifier.weight(1f),
                onClick = { onColorSchemeSelected(ColorScheme.PROFESSIONAL) }
            )
            ColorSchemeCard(
                scheme = ColorScheme.VIBRANT,
                isSelected = selectedColorScheme == ColorScheme.VIBRANT,
                modifier = Modifier.weight(1f),
                onClick = { onColorSchemeSelected(ColorScheme.VIBRANT) }
            )
            ColorSchemeCard(
                scheme = ColorScheme.MINIMAL,
                isSelected = selectedColorScheme == ColorScheme.MINIMAL,
                modifier = Modifier.weight(1f),
                onClick = { onColorSchemeSelected(ColorScheme.MINIMAL) }
            )
        }

        // Row 2: Warm, Tech, Nature
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSchemeCard(
                scheme = ColorScheme.WARM,
                isSelected = selectedColorScheme == ColorScheme.WARM,
                modifier = Modifier.weight(1f),
                onClick = { onColorSchemeSelected(ColorScheme.WARM) }
            )
            ColorSchemeCard(
                scheme = ColorScheme.TECH,
                isSelected = selectedColorScheme == ColorScheme.TECH,
                modifier = Modifier.weight(1f),
                onClick = { onColorSchemeSelected(ColorScheme.TECH) }
            )
            ColorSchemeCard(
                scheme = ColorScheme.NATURE,
                isSelected = selectedColorScheme == ColorScheme.NATURE,
                modifier = Modifier.weight(1f),
                onClick = { onColorSchemeSelected(ColorScheme.NATURE) }
            )
        }
    }
}

@Composable
private fun ColorSchemeCard(
    scheme: ColorScheme,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val border = if (isSelected) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Color preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(android.graphics.Color.parseColor(scheme.primaryHex)))
            )
            Text(scheme.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * SPACING PROFILE SECTION - Choose spacing presets
 */
@Composable
private fun SpacingProfileSection(
    selectedSpacingProfile: SpacingProfile,
    onSpacingProfileSelected: (SpacingProfile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "5️⃣  Spacing Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Control whitespace and padding throughout the invoice",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // All 4 options in one row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpacingProfileCard(
                profile = SpacingProfile.TIGHT,
                isSelected = selectedSpacingProfile == SpacingProfile.TIGHT,
                modifier = Modifier.weight(1f),
                onClick = { onSpacingProfileSelected(SpacingProfile.TIGHT) }
            )
            SpacingProfileCard(
                profile = SpacingProfile.NORMAL,
                isSelected = selectedSpacingProfile == SpacingProfile.NORMAL,
                modifier = Modifier.weight(1f),
                onClick = { onSpacingProfileSelected(SpacingProfile.NORMAL) }
            )
            SpacingProfileCard(
                profile = SpacingProfile.GENEROUS,
                isSelected = selectedSpacingProfile == SpacingProfile.GENEROUS,
                modifier = Modifier.weight(1f),
                onClick = { onSpacingProfileSelected(SpacingProfile.GENEROUS) }
            )
            SpacingProfileCard(
                profile = SpacingProfile.PREMIUM,
                isSelected = selectedSpacingProfile == SpacingProfile.PREMIUM,
                modifier = Modifier.weight(1f),
                onClick = { onSpacingProfileSelected(SpacingProfile.PREMIUM) }
            )
        }
    }
}

@Composable
private fun SpacingProfileCard(
    profile: SpacingProfile,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val border = if (isSelected) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Card(
        modifier = modifier.clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(profile.emoji, fontSize = 20.sp)
            Text(profile.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * VISUAL ACCENTS SECTION - Toggle design enhancements
 */
@Composable
private fun VisualAccentsSection(
    accents: VisualAccents,
    onAccentsChanged: (VisualAccents) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "6️⃣  Visual Accents",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Fine-tune visual elements for your invoice design",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Toggle options
        VisualAccentToggle(
            icon = "🗃️",
            label = "Show Borders",
            isEnabled = accents.showBorders,
            onToggle = { onAccentsChanged(accents.copy(showBorders = !accents.showBorders)) }
        )
        VisualAccentToggle(
            icon = "⬜",
            label = "Show Shadows",
            isEnabled = accents.showShadows,
            onToggle = { onAccentsChanged(accents.copy(showShadows = !accents.showShadows)) }
        )
        VisualAccentToggle(
            icon = "─",
            label = "Show Dividers",
            isEnabled = accents.showDividers,
            onToggle = { onAccentsChanged(accents.copy(showDividers = !accents.showDividers)) }
        )
        VisualAccentToggle(
            icon = "💰",
            label = "Highlight Totals",
            isEnabled = accents.highlightTotals,
            onToggle = { onAccentsChanged(accents.copy(highlightTotals = !accents.highlightTotals)) }
        )
        VisualAccentToggle(
            icon = "🌈",
            label = "Use Gradients",
            isEnabled = accents.useGradients,
            onToggle = { onAccentsChanged(accents.copy(useGradients = !accents.useGradients)) }
        )
    }
}

@Composable
private fun VisualAccentToggle(
    icon: String,
    label: String,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isEnabled) 0.5f else 0.2f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 18.sp)
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Checkbox(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(24.dp)
        )
    }
}
