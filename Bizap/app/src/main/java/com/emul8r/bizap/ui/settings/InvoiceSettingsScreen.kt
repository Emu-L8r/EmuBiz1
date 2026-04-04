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
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.model.InvoiceLocale
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewHtml by viewModel.previewHtml.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Settings") },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    InfoSection()
                }

                // ════════════════════════════════════════════════════════════════════
                // UNIFIED WORKFLOW: PDF GENERATION SETTINGS (Steps 1-4)
                // ════════════════════════════════════════════════════════════════════

                item {
                    // 1️⃣ Step 1: Choose PDF Engine (Canvas vs HTML)
                    PdfEngineSection(
                        selectedEngine = uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS,
                        onEngineSelected = { viewModel.updateSelectedPdfEngine(it) }
                    )
                }

                item {
                    // 2️⃣ Step 2: Choose Template/Style (updates based on engine)
                    // Derive theme from selectedPdfEngine (authoritative) so the template
                    // grid always reflects what the user picked in Step 1.
                    val currentTheme = when (uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS) {
                        PdfEngine.HTML_CSS -> InvoiceTheme.HTML_PDF
                        PdfEngine.CANVAS   -> InvoiceTheme.CANVAS
                    }
                    TemplateSelectionSection(
                        currentTheme = currentTheme,
                        selectedCanvasTemplate = uiState.settings?.selectedCanvasTemplate ?: CanvasInvoiceTemplate.MODERN,
                        selectedHtmlStyle = uiState.settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN,
                        onCanvasTemplateSelected = { viewModel.updateSelectedCanvasTemplate(it) },
                        onHtmlStyleSelected = { viewModel.updateSelectedHtmlStyle(it) }
                    )
                }

                item {
                    // 3️⃣ Step 3: Choose Page Layout (Classic vs Modern)
                    PageLayoutSection(
                        selectedLayout = uiState.settings?.selectedPageLayout ?: PageLayout.CLASSIC,
                        onLayoutSelected = { viewModel.updateSelectedPageLayout(it) }
                    )
                }

                item {
                    // 4️⃣ Step 4: Live Preview (real-time visualization of all choices)
                    LivePreviewSection(
                        previewHtml = previewHtml,
                        onRefresh = { viewModel.generatePreview() },
                        selectedEngine = uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS,
                        selectedCanvasTemplate = uiState.settings?.selectedCanvasTemplate ?: CanvasInvoiceTemplate.MODERN
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // ════════════════════════════════════════════════════════════════════
                // OPTIONAL: DESIGN CUSTOMIZATION (Steps 5-6)
                // ════════════════════════════════════════════════════════════════════

                item {
                    // 5️⃣ Typography Selection
                    uiState.settings?.let { settings ->
                        TypographySection(
                            selectedTypography = settings.selectedTypography,
                            onTypographySelected = { viewModel.updateSelectedTypography(it) }
                        )
                    }
                }

                item {
                    // 6️⃣ Visibility Toggles
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // ════════════════════════════════════════════════════════════════════
                // INTERNATIONAL SETTINGS (Step 7)
                // ════════════════════════════════════════════════════════════════════

                item {
                    // 7️⃣ Locale & Currency Formatting
                    uiState.settings?.let { settings ->
                        LocaleSelectionSection(
                            selectedLocale = settings.selectedLocale,
                            onLocaleSelected = { viewModel.updateSelectedLocale(it) }
                        )
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                // ════════════════════════════════════════════════════════════════════
                // OPTIONAL: BUSINESS SETTINGS (Steps 8+)
                // ════════════════════════════════════════════════════════════════════

                item {
                    // 8️⃣ Payment Terms Configuration
                    uiState.settings?.let { settings ->
                        PaymentSection(
                            paymentTermsDays = settings.paymentTermsDays,
                            onPaymentTermsChanged = { viewModel.updatePaymentTermsDays(it) }
                        )
                    }
                }

                item {
                    // 9️⃣ Tax Configuration
                    uiState.settings?.let { settings ->
                        TaxSection(
                            taxRate = settings.taxRate,
                            taxName = settings.taxName,
                            onTaxRateChanged = { viewModel.updateTaxRate(it) },
                            onTaxNameChanged = { viewModel.updateTaxName(it) }
                        )
                    }
                }

                item {
                    // 🔟 Save & Reset Actions
                    ActionButtonsSection(
                        onSave = { viewModel.saveSettings() },
                        onReset = { viewModel.resetToDefaults() }
                    )
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
    }
    val secondaryColor = when (style) {
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL  -> Color(0xFF1C1C2E)
        HtmlInvoiceStyle.WARM_APPROACHABLE     -> Color(0xFF1F2937)
        HtmlInvoiceStyle.SASS_PROFESSIONAL     -> Color(0xFFF7F9FC)
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
            "Select how your PDF invoices are generated",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EngineOptionCard(
                title = "Canvas",
                emoji = "🎨",
                description = "Direct coordinate control",
                isSelected = selectedEngine == PdfEngine.CANVAS,
                modifier = Modifier.weight(1f),
                onClick = { onEngineSelected(PdfEngine.CANVAS) }
            )

            EngineOptionCard(
                title = "HTML+CSS",
                emoji = "📄",
                description = "CSS-based styling",
                isSelected = selectedEngine == PdfEngine.HTML_CSS,
                modifier = Modifier.weight(1f),
                onClick = { onEngineSelected(PdfEngine.HTML_CSS) }
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
            "Choose how invoice content is organized",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: Classic & Modern
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LayoutOptionCard(
                title = "Classic",
                emoji = "📋",
                description = "Traditional layout",
                isSelected = selectedLayout == PageLayout.CLASSIC,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.CLASSIC) }
            )

            LayoutOptionCard(
                title = "Modern",
                emoji = "🎯",
                description = "Compact grid layout",
                isSelected = selectedLayout == PageLayout.MODERN,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.MODERN) }
            )
        }

        // Row 2: Spacious & Compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LayoutOptionCard(
                title = "Spacious",
                emoji = "✨",
                description = "Generous spacing",
                isSelected = selectedLayout == PageLayout.SPACIOUS,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.SPACIOUS) }
            )

            LayoutOptionCard(
                title = "Compact",
                emoji = "📊",
                description = "Executive tight fit",
                isSelected = selectedLayout == PageLayout.COMPACT,
                modifier = Modifier.weight(1f),
                onClick = { onLayoutSelected(PageLayout.COMPACT) }
            )
        }
    }
}

@Composable
private fun LayoutOptionCard(
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
 * SECTION 4: Live Preview — shows a WebView rendering of the selected invoice style.
 * Updates automatically when the style changes.
 */
@Composable
private fun LivePreviewSection(
    previewHtml: String?,
    onRefresh: () -> Unit,
    selectedEngine: PdfEngine = PdfEngine.CANVAS,
    selectedCanvasTemplate: CanvasInvoiceTemplate = CanvasInvoiceTemplate.MODERN
) {
    when (selectedEngine) {
        PdfEngine.CANVAS -> {
            CanvasTemplatePreview(
                template = selectedCanvasTemplate,
                previewHtml = previewHtml,
                onRefresh = onRefresh
            )
        }
        PdfEngine.HTML_CSS -> {
            HtmlPreview(
                previewHtml = previewHtml,
                onRefresh = onRefresh
            )
        }
    }
}

/**
 * Canvas template visual preview
 */
@Composable
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
 * HTML preview component
 */
@Composable
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
