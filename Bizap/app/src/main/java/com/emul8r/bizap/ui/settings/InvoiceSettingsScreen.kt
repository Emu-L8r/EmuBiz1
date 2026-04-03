package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import timber.log.Timber

/**
 * Invoice Settings Screen - Phase 5 Polish Edition
 *
 * Allows users to:
 * - Select invoice theme (Canvas or HTML-to-PDF)
 * - Configure company branding
 * - Set brand colors
 * - Configure payment details
 * - Set tax configuration
 *
 * Phase 5 Improvements:
 * - Enhanced visual hierarchy
 * - Better spacing and separation
 * - Improved typography
 * - Helper text and descriptions
 * - Polish and refinement throughout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Settings saved successfully")
            Timber.d("Settings saved successfully")
        }
    }

    // Show snackbar on error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar("⚠️ Error: $error")
            Timber.e("Settings error: $error")
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.settings == null -> {
                // Initial loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading settings...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            uiState.error != null && uiState.settings == null -> {
                // Error state with retry option
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Failed to Load Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.retryLoadSettings() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
            uiState.settings != null -> {
                // Settings loaded successfully
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Welcome/Info Section
                    item {
                        InfoSection()
                    }

                    // Theme Selection Section
                    item {
                        ThemeSelectionSection(
                            currentTheme = uiState.settings?.selectedTheme,
                            onThemeSelected = { viewModel.updateSelectedTheme(it) }
                        )
                    }

                    // HTML Style Selection (always visible for preview/selection)
                    item {
                        uiState.settings?.let { settings ->
                            HtmlStyleSelectionSection(
                                currentStyle = settings.selectedHtmlStyle,
                                onStyleSelected = { viewModel.updateSelectedHtmlStyle(it) },
                                isActive = settings.selectedTheme == InvoiceTheme.HTML_PDF
                            )
                        }
                    }

                    // Theme Preview Section
                    item {
                        uiState.settings?.let { settings ->
                            InvoiceThemePreview(
                                selectedTheme = settings.selectedTheme,
                                companyName = "Your Company",
                                primaryColor = settings.primaryColor
                            )
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }


                    // Colors Section
                    item {
                        uiState.settings?.let { settings ->
                            ColorsSection(
                                primaryColor = settings.primaryColor,
                                onColorChanged = { viewModel.updatePrimaryColor(it) }
                            )
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Payment Section
                    item {
                        uiState.settings?.let { settings ->
                            PaymentSection(
                                paymentTermsDays = settings.paymentTermsDays,
                                onPaymentTermsChanged = { viewModel.updatePaymentTermsDays(it) }
                            )
                        }
                    }

                    // Tax Section
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

                    // Action Buttons
                    item {
                        ActionButtonsSection(
                            onSave = { viewModel.saveSettings() },
                            onReset = { viewModel.resetToDefaults() }
                        )
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Column {
        Text(
            text = "PDF Invoice Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select your PDF template style and customize invoice appearance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeSelectionSection(
    currentTheme: InvoiceTheme?,
    onThemeSelected: (InvoiceTheme) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Invoice Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Select how your invoices will be styled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            val theme = currentTheme ?: InvoiceTheme.CANVAS
            // Canvas Style Option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (theme == InvoiceTheme.CANVAS)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (theme == InvoiceTheme.CANVAS)
                    androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    )
                else
                    null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = theme == InvoiceTheme.CANVAS,
                        onClick = { onThemeSelected(InvoiceTheme.CANVAS) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Canvas Style",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Text(
                            "Clean, traditional invoice design",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (theme == InvoiceTheme.CANVAS) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // HTML-to-PDF Option
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (theme == InvoiceTheme.HTML_PDF)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (theme == InvoiceTheme.HTML_PDF)
                    androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    )
                else
                    null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = theme == InvoiceTheme.HTML_PDF,
                        onClick = { onThemeSelected(InvoiceTheme.HTML_PDF) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Modern HTML Style",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Text(
                            "Professional, modern design (Phase 6)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (theme == InvoiceTheme.HTML_PDF) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HtmlStyleSelectionSection(
    currentStyle: HtmlInvoiceStyle?,
    onStyleSelected: (HtmlInvoiceStyle) -> Unit,
    isActive: Boolean = false
) {
    var previewStyle by remember { mutableStateOf<HtmlInvoiceStyle?>(null) }

    // FIX #4: Track the selected style in local state so UI updates immediately
    // Initialize with database value if available, otherwise use MODERN as fallback
    var selectedStyle by remember { mutableStateOf(currentStyle ?: HtmlInvoiceStyle.MODERN) }

    // FIX #6: Track if this is the first composition to avoid unnecessary callbacks
    var isFirstComposition by remember { mutableStateOf(true) }

    // FIX #4: When currentStyle changes (from DB), update local state AND sync with ViewModel
    // This ensures bidirectional synchronization between DB, ViewModel, and UI
    LaunchedEffect(currentStyle) {
        currentStyle?.let { dbStyle ->
            // Only update if it actually changed to avoid infinite loops
            if (selectedStyle != dbStyle) {
                selectedStyle = dbStyle
                Timber.d("📝 HTML STYLE SYNCED FROM DB: ${dbStyle.displayName}")

                // FIX #4: Invoke callback to notify ViewModel that DB and UI are now synchronized
                // This prevents the "selection reverts" issue by explicitly confirming the selection
                onStyleSelected(dbStyle)
                Timber.d("✅ DB SYNC CALLBACK INVOKED: ${dbStyle.displayName}")
            }
            isFirstComposition = false
        } ?: run {
            // FIX #6: If currentStyle is NULL (DB value not loaded), warn developer
            if (!isFirstComposition) {
                Timber.w("⚠️ WARNING: currentStyle is NULL - Settings may not have loaded from database")
            }
        }
    }

    // Log available styles
    LaunchedEffect(Unit) {
        Timber.d("📋 HTML INVOICE STYLES AVAILABLE:")
        HtmlInvoiceStyle.values().forEach { style ->
            Timber.d("  ✓ ${style.displayName} (${style.styleFile})")
        }
        Timber.d("📝 CURRENT SELECTED STYLE: ${selectedStyle.displayName}")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 1.dp),
        border = if (isActive)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        else
            null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "HTML Invoice Styles (4 Available)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isActive) {
                    androidx.compose.material3.Badge(
                        modifier = Modifier.align(Alignment.Top),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("Active", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Text(
                "Choose your preferred professional invoice design style",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // List all available HTML styles with color previews
            HtmlInvoiceStyle.values().forEach { htmlStyle ->
                val styleColorScheme = getStyleColorScheme(htmlStyle)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedStyle == htmlStyle)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (selectedStyle == htmlStyle)
                        androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    else
                        null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedStyle == htmlStyle,
                            onClick = {
                                selectedStyle = htmlStyle
                                Timber.d("🎨 USER SELECTED STYLE: ${htmlStyle.displayName}")
                                onStyleSelected(htmlStyle)
                            }
                        )
                        // Color preview box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = styleColorScheme,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                )
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                htmlStyle.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Text(
                                htmlStyle.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Preview button
                        OutlinedButton(
                            onClick = {
                                Timber.d("👁️ USER PREVIEWING STYLE: ${htmlStyle.displayName}")
                                previewStyle = htmlStyle
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Preview", style = MaterialTheme.typography.labelSmall)
                        }
                        if (selectedStyle == htmlStyle) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Show preview dialog when selected
    previewStyle?.let {
        InvoiceStylePreview(
            style = it,
            onDismiss = {
                previewStyle = null
            }
        )
    }
}

/**
 * Helper function to get the primary color for each HTML invoice style.
 * Used for visual preview in the style selection UI.
 */
fun getStyleColorScheme(style: HtmlInvoiceStyle): Color {
    return when (style) {
        HtmlInvoiceStyle.MODERN -> Color(0xFF6B4C9A)      // Purple gradient
        HtmlInvoiceStyle.MINIMAL -> Color(0xFF1a1a1a)     // Black & white
        HtmlInvoiceStyle.CORPORATE -> Color(0xFF003366)   // Navy blue
        HtmlInvoiceStyle.CREATIVE -> Color(0xFFFF6B35)    // Orange/teal vibrant
    }
}


@Composable
fun ColorsSection(
    primaryColor: String,
    onColorChanged: (String) -> Unit
) {
    // List of user-friendly colors - NO HEX CODES NEEDED
    val colors = listOf(
        ColorOption("Professional Purple", "#6B4C9A"),
        ColorOption("Corporate Blue", "#2E5090"),
        ColorOption("Success Green", "#27AE60"),
        ColorOption("Warm Orange", "#E67E22"),
        ColorOption("Elegant Navy", "#1A3A52"),
        ColorOption("Vibrant Red", "#E74C3C"),
        ColorOption("Trusty Teal", "#16A085"),
        ColorOption("Rich Burgundy", "#8B0000"),
        ColorOption("Modern Gray", "#34495E"),
        ColorOption("Sunny Yellow", "#F39C12"),
        ColorOption("Calm Sky", "#3498DB"),
        ColorOption("Fresh Mint", "#1ABC9C")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Brand Colors",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Select a color for your invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Color preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = try {
                        Color(android.graphics.Color.parseColor(primaryColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Current Color Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            // Color grid - NO HEX CODES!
            Text(
                "Choose a color below:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 3x4 grid of color options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in colors.indices step 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { offset ->
                            val index = i + offset
                            if (index < colors.size) {
                                val color = colors[index]
                                val colorValue = try {
                                    Color(android.graphics.Color.parseColor(color.hexCode))
                                } catch (e: Exception) {
                                    Color.Gray
                                }

                                Button(
                                    onClick = { onColorChanged(color.hexCode) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorValue
                                    ),
                                    border = if (primaryColor.equals(color.hexCode, ignoreCase = true))
                                        BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                                    else
                                        null
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            color.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        if (primaryColor.equals(color.hexCode, ignoreCase = true)) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Empty space for grid alignment
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper data class for color selection
data class ColorOption(val displayName: String, val hexCode: String)

@Composable
fun PaymentSection(
    paymentTermsDays: Int,
    onPaymentTermsChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Payment Terms",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Default payment terms for invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = paymentTermsDays.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onPaymentTermsChanged(it) }
                },
                label = { Text("Payment Terms") },
                placeholder = { Text("30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Days from invoice date") }
            )
        }
    }
}

@Composable
fun TaxSection(
    taxRate: Double,
    taxName: String,
    onTaxRateChanged: (Double) -> Unit,
    onTaxNameChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tax Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Default tax rate applied to invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = taxRate.toString(),
                onValueChange = { value ->
                    value.toDoubleOrNull()?.let { onTaxRateChanged(it) }
                },
                label = { Text("Tax Rate") },
                placeholder = { Text("10.0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                supportingText = { Text("Percentage (0-100)") }
            )

            OutlinedTextField(
                value = taxName,
                onValueChange = onTaxNameChanged,
                label = { Text("Tax Name") },
                placeholder = { Text("e.g., GST, VAT, Sales Tax") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Save Settings")
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Reset to Defaults")
        }
    }
}
