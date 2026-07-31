package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.data.service.pdf.PdfQualitySeverity
import com.emul8r.bizap.data.service.pdf.PdfQualityWarning
import com.emul8r.bizap.domain.model.*
import com.emul8r.bizap.domain.model.ColorScheme as InvoiceColorScheme
import com.emul8r.bizap.ui.gui2.settings.InvoiceCustomizationViewModel
import com.emul8r.bizap.ui.gui2.settings.InvoiceSettingsTab
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

/**
 * GUI3 Invoice Customization Settings Screen — Matrix cyberpunk aesthetic.
 *
 * Mirrors GUI2's InvoiceCustomizationSettingsScreenV2 with full Matrix theming.
 * Reuses [InvoiceCustomizationViewModel] (Pattern 2C — zero new ViewModel code).
 *
 * Features:
 * - 4 tabs: Overview, Advanced, Presets, Quality
 * - Matrix-styled tab row and form fields
 * - Real-time PDF preview (monospaced placeholder)
 * - Settings saved via shared ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCustomizationSettingsScreenV3(
    businessId: Long,
    navController: NavHostController,
    viewModel: InvoiceCustomizationViewModel = hiltViewModel()
) {
    val invoiceSettings by viewModel.invoiceSettings.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val previewHtml by viewModel.previewHtml.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    // Quality score computed locally from settings
    val qualityScore = remember(invoiceSettings) {
        val s = invoiceSettings ?: return@remember 0.75f
        var score = 1.0f
        if (!s.enableDividers) score -= 0.05f
        if (!s.enableAlternatingRowColors) score -= 0.03f
        if (s.footerMessage.isBlank()) score -= 0.04f
        if (s.enableGradientHeader) score += 0.05f
        if (s.enableRoundedCorners) score += 0.03f
        if (s.highlightTotals) score += 0.04f
        score.coerceIn(0f, 1f)
    }
    val qualityWarnings = remember(invoiceSettings) {
        val s = invoiceSettings ?: return@remember emptyList<PdfQualityWarning>()
        buildList {
            if (s.footerMessage.isBlank())
                add(PdfQualityWarning("No footer message set", "Add a footer for a professional look", PdfQualitySeverity.INFO, "NO_FOOTER"))
            if (!s.enableDividers)
                add(PdfQualityWarning("Row dividers disabled", "Enable dividers to improve readability", PdfQualitySeverity.INFO, "NO_DIVIDERS"))
        }
    }

    var selectedTab by remember { mutableStateOf(InvoiceSettingsTab.OVERVIEW) }
    var footerText by remember { mutableStateOf("") }
    var prefix by remember { mutableStateOf("INV-") }
    var includeNotes by remember { mutableStateOf(true) }
    var showLogo by remember { mutableStateOf(true) }
    var selectedPageLayout by remember { mutableStateOf<PageLayout>(PageLayout.MODERN) }
    var selectedColorScheme by remember { mutableStateOf<InvoiceColorScheme>(InvoiceColorScheme.PROFESSIONAL) }
    var enableGradient by remember { mutableStateOf(false) }
    var enableShadow by remember { mutableStateOf(true) }
    var enableRounded by remember { mutableStateOf(true) }

    LaunchedEffect(invoiceSettings) {
        invoiceSettings?.let { settings ->
            footerText = settings.footerMessage
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(message = error, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    MatrixBackgroundWrapper(screenType = ScreenType.SETTINGS) {
        Scaffold(
            containerColor = MatrixBlack,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> INVOICE SETTINGS",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreenBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MatrixGreen)
                        }
                    },
                    actions = {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = Spacing.sm),
                                color = MatrixGreen,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MatrixBlack,
                        titleContentColor = MatrixGreenBright,
                        navigationIconContentColor = MatrixGreen
                    )
                )
            }
        ) { padding ->
            if (isLoading) {
                MatrixLoadingScreen(message = "LOADING SETTINGS...")
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Matrix-styled tab row
                    MatrixSettingsTabRow(
                        tabs = InvoiceSettingsTab.all,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    // Tab content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.md)
                    ) {
                        when (selectedTab) {
                            InvoiceSettingsTab.OVERVIEW -> MatrixOverviewTab(
                                prefix = prefix,
                                onPrefixChange = { prefix = it.take(10) },
                                footerText = footerText,
                                onFooterChange = { footerText = it.take(200) },
                                includeNotes = includeNotes,
                                onIncludeNotesChange = { includeNotes = it },
                                showLogo = showLogo,
                                onShowLogoChange = { showLogo = it },
                                onSave = {
                                    Timber.d("GUI3: Saving overview settings")
                                    viewModel.updateFooterText(footerText)
                                }
                            )
                            InvoiceSettingsTab.ADVANCED -> MatrixAdvancedTab(
                                selectedPageLayout = selectedPageLayout,
                                onPageLayoutChange = {
                                    selectedPageLayout = it
                                    viewModel.updatePageLayout(it)
                                },
                                selectedColorScheme = selectedColorScheme,
                                onColorSchemeChange = {
                                    selectedColorScheme = it
                                    viewModel.updateColorScheme(it)
                                },
                                enableGradient = enableGradient,
                                onGradientChange = {
                                    enableGradient = it
                                    viewModel.toggleEffect("gradient", it)
                                },
                                enableShadow = enableShadow,
                                onShadowChange = {
                                    enableShadow = it
                                    viewModel.toggleEffect("shadow", it)
                                },
                                enableRounded = enableRounded,
                                onRoundedChange = {
                                    enableRounded = it
                                    viewModel.toggleEffect("rounded", it)
                                }
                            )
                            InvoiceSettingsTab.PRESETS -> MatrixPresetsPlaceholderTab()
                            InvoiceSettingsTab.QUALITY -> MatrixQualityTab(
                                qualityScore = qualityScore,
                                warnings = qualityWarnings
                            )
                        }
                    }

                    // PDF Preview panel at bottom
                    MatrixPdfPreviewPanel(
                        previewHtml = previewHtml,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = Spacing.md)
                            .padding(bottom = Spacing.md)
                    )
                }
            }
        }
    }
}

// ─── Tab Row ─────────────────────────────────────────────────────────────────

@Composable
private fun MatrixSettingsTabRow(
    tabs: List<InvoiceSettingsTab>,
    selectedTab: InvoiceSettingsTab,
    onTabSelected: (InvoiceSettingsTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MatrixBlack,
        contentColor = MatrixGreen,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            if (selectedTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = MatrixGreenBright
                )
            }
        }
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) MatrixGreenBright else MatrixGreen.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            )
        }
    }
    MatrixDivider()
}

// ─── Tab: Overview ───────────────────────────────────────────────────────────

@Composable
private fun MatrixOverviewTab(
    prefix: String,
    onPrefixChange: (String) -> Unit,
    footerText: String,
    onFooterChange: (String) -> Unit,
    includeNotes: Boolean,
    onIncludeNotesChange: (Boolean) -> Unit,
    showLogo: Boolean,
    onShowLogoChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MatrixSectionLabel("NUMBERING")
        MatrixTextField(value = prefix, onValueChange = onPrefixChange, label = "INVOICE PREFIX", modifier = Modifier.fillMaxWidth())
        MatrixDivider()
        MatrixSectionLabel("CONTENT")
        MatrixTextField(value = footerText, onValueChange = onFooterChange, label = "FOOTER MESSAGE (${footerText.length}/200)", modifier = Modifier.fillMaxWidth())
        MatrixToggle(checked = includeNotes, onCheckedChange = onIncludeNotesChange, label = "INCLUDE NOTES")
        MatrixToggle(checked = showLogo, onCheckedChange = onShowLogoChange, label = "SHOW COMPANY LOGO")
        Spacer(modifier = Modifier.height(Spacing.sm))
        MatrixPrimaryButton(text = "[ SAVE SETTINGS ]", onClick = onSave, modifier = Modifier.fillMaxWidth())
    }
}

// ─── Tab: Advanced ───────────────────────────────────────────────────────────

@Composable
private fun MatrixAdvancedTab(
    selectedPageLayout: PageLayout,
    onPageLayoutChange: (PageLayout) -> Unit,
    selectedColorScheme: InvoiceColorScheme,
    onColorSchemeChange: (InvoiceColorScheme) -> Unit,
    enableGradient: Boolean,
    onGradientChange: (Boolean) -> Unit,
    enableShadow: Boolean,
    onShadowChange: (Boolean) -> Unit,
    enableRounded: Boolean,
    onRoundedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MatrixSectionLabel("PAGE LAYOUT")
        MatrixDropdown(
            items = PageLayout.values().toList(),
            selectedItem = selectedPageLayout,
            onSelect = onPageLayoutChange,
            itemLabel = { it.name },
            modifier = Modifier.fillMaxWidth()
        )

        MatrixDivider()
        MatrixSectionLabel("COLOR SCHEME")
        MatrixDropdown(
            items = InvoiceColorScheme.values().toList(),
            selectedItem = selectedColorScheme,
            onSelect = onColorSchemeChange,
            itemLabel = { it.name },
            modifier = Modifier.fillMaxWidth()
        )

        MatrixDivider()
        MatrixSectionLabel("VISUAL EFFECTS")
        MatrixToggle(checked = enableGradient, onCheckedChange = onGradientChange, label = "GRADIENT HEADER")
        MatrixToggle(checked = enableShadow, onCheckedChange = onShadowChange, label = "DROP SHADOWS")
        MatrixToggle(checked = enableRounded, onCheckedChange = onRoundedChange, label = "ROUNDED CORNERS")
    }
}

// ─── Tab: Presets (placeholder) ───────────────────────────────────────────────

@Composable
private fun MatrixPresetsPlaceholderTab() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        MatrixSectionLabel("PRESET TEMPLATES")
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = "Presets coming in a future update.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixGreen.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

// ─── Tab: Quality ─────────────────────────────────────────────────────────────

@Composable
private fun MatrixQualityTab(
    qualityScore: Float,
    warnings: List<PdfQualityWarning>
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MatrixSectionLabel("QUALITY SCORE")

        // Score display
        Surface(
            modifier = Modifier.fillMaxWidth().border(
                width = 1.dp,
                color = qualityScoreColor(qualityScore).copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ),
            color = MatrixSurface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(qualityScore * 100).toInt()}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = qualityScoreColor(qualityScore),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "/ 100",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MatrixGreen.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = qualityScoreLabel(qualityScore),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = qualityScoreColor(qualityScore),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                )
            }
        }

        if (warnings.isNotEmpty()) {
            MatrixDivider()
            MatrixSectionLabel("WARNINGS (${warnings.size})")
            warnings.forEach { warning -> MatrixWarningCard(warning = warning) }
        } else {
            MatrixDivider()
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, MatrixGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                color = MatrixSurface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = MatrixGreen, modifier = Modifier.size(20.dp))
                    Text(
                        text = "NO WARNINGS — SETTINGS OPTIMAL",
                        style = MaterialTheme.typography.labelSmall.copy(color = MatrixGreen, fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixWarningCard(warning: PdfQualityWarning) {
    val color = when (warning.severity) {
        PdfQualitySeverity.ERROR -> MatrixError
        PdfQualitySeverity.WARNING -> MatrixWarning
        PdfQualitySeverity.INFO -> MatrixInfo
    }
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            Text("⚠ ${warning.message}", style = MaterialTheme.typography.labelSmall.copy(color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
            if (warning.suggestion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("→ ${warning.suggestion}", style = MaterialTheme.typography.labelSmall.copy(color = MatrixGreen.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace))
            }
        }
    }
}

// ─── PDF Preview Panel ────────────────────────────────────────────────────────

@Composable
private fun MatrixPdfPreviewPanel(previewHtml: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "[ LIVE PREVIEW ]",
            style = MaterialTheme.typography.labelSmall.copy(color = MatrixGreen.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace, letterSpacing = 2.sp),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxSize().border(1.dp, MatrixGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            color = MatrixSurface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (previewHtml != null) {
                    Text(
                        text = previewHtml.take(300).replace(Regex("<[^>]+>"), "").trim(),
                        style = MaterialTheme.typography.labelSmall.copy(color = MatrixGreen.copy(alpha = 0.8f), fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                        modifier = Modifier.fillMaxSize().padding(Spacing.sm)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MatrixGreen, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("GENERATING PREVIEW...", style = MaterialTheme.typography.labelSmall.copy(color = MatrixGreen.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace))
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun MatrixSectionLabel(text: String) {
    Text(
        text = "[ $text ]",
        style = MaterialTheme.typography.labelMedium.copy(color = MatrixGreen.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
    )
}

private fun qualityScoreColor(score: Float): androidx.compose.ui.graphics.Color = when {
    score >= 0.8f -> MatrixGreen
    score >= 0.6f -> MatrixWarning
    else -> MatrixError
}

private fun qualityScoreLabel(score: Float): String = when {
    score >= 0.9f -> "EXCELLENT"
    score >= 0.8f -> "GOOD"
    score >= 0.6f -> "FAIR"
    score >= 0.4f -> "POOR"
    else -> "CRITICAL"
}
