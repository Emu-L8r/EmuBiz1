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
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.domain.model.PageLayout

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

                item {
                    // Section 1: PDF Engine Selection
                    PdfEngineSelectionSection(
                        currentTheme = uiState.settings?.selectedTheme ?: InvoiceTheme.CANVAS,
                        onThemeSelected = { viewModel.updateSelectedTheme(it) }
                    )
                }

                item {
                    // Section 2: Template Selection (dynamic based on engine)
                    val currentTheme = uiState.settings?.selectedTheme ?: InvoiceTheme.CANVAS
                    TemplateSelectionSection(
                        currentTheme = currentTheme,
                        selectedCanvasTemplate = uiState.settings?.selectedCanvasTemplate ?: CanvasInvoiceTemplate.MODERN,
                        selectedHtmlStyle = uiState.settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN,
                        onCanvasTemplateSelected = { viewModel.updateSelectedCanvasTemplate(it) },
                        onHtmlStyleSelected = { viewModel.updateSelectedHtmlStyle(it) }
                    )
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

                item {
                    ActionButtonsSection(
                        onSave = { viewModel.saveSettings() },
                        onReset = { viewModel.resetToDefaults() }
                    )
                }

                // ============================================================================

                item {
                    // PHASE 2: PDF Engine Section
                    PdfEngineSection(
                        selectedEngine = uiState.settings?.selectedPdfEngine ?: PdfEngine.CANVAS,
                        onEngineSelected = { viewModel.updateSelectedPdfEngine(it) }
                    )
                }

                item {
                    // PHASE 2: Page Layout Section
                    PageLayoutSection(
                        selectedLayout = uiState.settings?.selectedPageLayout ?: PageLayout.CLASSIC,
                        onLayoutSelected = { viewModel.updateSelectedPageLayout(it) }
                    )
                }

                item {
                    // PHASE 2: Preview Mode Toggle
                    PreviewModeSection(
                        previewWithPlaceholder = uiState.settings?.previewWithPlaceholder ?: false,
                        onToggle = { viewModel.updatePreviewWithPlaceholder(it) }
                    )
                }

                item {
                    // PHASE 2: Live Preview Placeholder
                    LivePreviewSection(
                        previewHtml = previewHtml,
                        onRefresh = { viewModel.generatePreview() }
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

/** Section 1: Two large clickable cards for engine selection (Canvas vs HTML). */
@Composable
fun PdfEngineSelectionSection(
    currentTheme: InvoiceTheme,
    onThemeSelected: (InvoiceTheme) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "1️⃣  PDF Engine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            "Select how your PDF invoices are generated",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        EngineCard(
            title = "CANVAS",
            subtitle = "Artistic, colorful, full design control",
            emoji = "🎨",
            isSelected = currentTheme == InvoiceTheme.CANVAS,
            selectedColor = MaterialTheme.colorScheme.primary,
            onClick = { onThemeSelected(InvoiceTheme.CANVAS) }
        )

        EngineCard(
            title = "HTML",
            subtitle = "Simple, clean, minimal design",
            emoji = "📄",
            isSelected = currentTheme == InvoiceTheme.HTML_PDF,
            selectedColor = MaterialTheme.colorScheme.secondary,
            onClick = { onThemeSelected(InvoiceTheme.HTML_PDF) }
        )
    }
}

@Composable
private fun EngineCard(
    title: String,
    subtitle: String,
    emoji: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val border = if (isSelected) BorderStroke(2.dp, selectedColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    val containerColor = if (isSelected) selectedColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = border,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = selectedColor
                )
            } else {
                RadioButton(selected = false, onClick = onClick)
            }
        }
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
        Text(
            "2️⃣  Template Selection",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (currentTheme == InvoiceTheme.CANVAS) "Choose your Canvas invoice template style"
            else "Choose your HTML invoice template style",
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
    }
    val secondaryColor = when (style) {
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL  -> Color(0xFF1C1C2E)
        HtmlInvoiceStyle.WARM_APPROACHABLE     -> Color(0xFF1F2937)
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
fun ThemeSelectionSection(currentTheme: InvoiceTheme?, onThemeSelected: (InvoiceTheme) -> Unit) {
    PdfEngineSelectionSection(
        currentTheme = currentTheme ?: InvoiceTheme.CANVAS,
        onThemeSelected = onThemeSelected
    )
}

@Composable
fun HtmlStyleSelectionSection(
    selectedStyle: HtmlInvoiceStyle,
    onStyleSelected: (HtmlInvoiceStyle) -> Unit,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("HTML Invoice Styles", style = MaterialTheme.typography.titleMedium)

            HtmlInvoiceStyle.values().forEach { style ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedStyle == style, onClick = { onStyleSelected(style) })
                    Column {
                        Text(style.displayName, fontWeight = FontWeight.Bold)
                        Text(style.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorsSection(primaryColor: String, onColorChanged: (String) -> Unit) {
    val colors = listOf("#6B4C9A", "#2E5090", "#27AE60", "#E67E22")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Brand Color", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = primaryColor == hex,
                            onClick = { onColorChanged(hex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSection(paymentTermsDays: Int, onPaymentTermsChanged: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Payment Terms (Days)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { it.toIntOrNull()?.let(onPaymentTermsChanged) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
fun TaxSection(taxRate: Double, taxName: String, onTaxRateChanged: (Double) -> Unit, onTaxNameChanged: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tax Configuration", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = taxName, onValueChange = onTaxNameChanged, label = { Text("Tax Name") })
            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { it.toDoubleOrNull()?.let(onTaxRateChanged) },
                label = { Text("Tax Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(onSave: () -> Unit, onReset: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
 * SECTION 2: Page Layout Selection (Classic vs Modern)
 */
@Composable
fun PageLayoutSection(
    selectedLayout: PageLayout,
    onLayoutSelected: (PageLayout) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "2️⃣  Page Layout",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose how invoice content is organized",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

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
 * SECTION 4: Preview Mode Toggle
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
                        "4️⃣  Preview Mode",
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
 * SECTION 5: Live Preview — shows a WebView rendering of the selected invoice style.
 * Updates automatically when the style changes.
 */
@Composable
private fun LivePreviewSection(
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
                "5️⃣  Live Preview",
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
                .height(380.dp)
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
                            // Scale to show a full-page preview
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
