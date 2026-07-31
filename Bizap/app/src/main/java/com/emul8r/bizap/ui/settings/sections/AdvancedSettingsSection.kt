package com.emul8r.bizap.ui.settings.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceLocale
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.PdfEngine
import com.emul8r.bizap.ui.settings.InvoiceSettingsViewModel
import com.emul8r.bizap.ui.settings.components.*

/**
 * Advanced Settings Section
 *
 * Tab containing advanced technical settings:
 * - PDF engine selection (Canvas, HTML, SASS)
 * - Locale selection (AU, US, UK, EU, CA, etc)
 * - Feature flags (debug builds only)
 * - Experimental options
 *
 * Organization:
 * - PDF Engine (accordion)
 * - Locale & Formatting (accordion)
 * - Debug Options (accordion - debug builds only)
 */
@Composable
fun AdvancedSettingsSection(
    viewModel: InvoiceSettingsViewModel,
    settings: InvoiceSettings?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Section header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Advanced Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Configure PDF engine, locale, and experimental features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // PDF Engine Selection
        item {
            SettingsAccordion(
                title = "PDF Engine",
                icon = Icons.Default.Print,
                initiallyExpanded = false,
                description = "Choose PDF generation method"
            ) {
                PdfEngineContent(
                    selectedEngine = settings?.selectedPdfEngine ?: PdfEngine.CANVAS,
                    onEngineSelected = { viewModel.updateSelectedPdfEngine(it) }
                )
            }
        }

        // Locale & Formatting
        item {
            SettingsAccordion(
                title = "Locale & Formatting",
                icon = Icons.Default.Language,
                description = "Region, language, and date/number formats"
            ) {
                LocaleContent(
                    selectedLocale = settings?.selectedLocale ?: InvoiceLocale.AUSTRALIAN,
                    onLocaleSelected = { /* TODO: updateLocale not yet implemented */ }
                )
            }
        }

        // Feature Flags (Debug Only)
        // TEMPORARILY DISABLED - ViewModel methods not implemented yet
        // Feature Flags (Debug Only)
        // TEMPORARILY DISABLED - Requires ViewModel method implementation
        /*
        item {
            SettingsAccordion(
                title = "Experimental Features",
                icon = Icons.Default.FlagCircle,
                description = "Beta and experimental options (advanced users)"
            ) {
                ExperimentalFeaturesContent(
                    settings = settings,
                    viewModel = viewModel
                )
             }
         }
         */

         item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * PDF Engine Selection Content
 */
@Composable
private fun PdfEngineContent(
    selectedEngine: PdfEngine,
    onEngineSelected: (PdfEngine) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Select how invoices are generated into PDF format",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Canvas Engine
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedEngine == PdfEngine.CANVAS,
                onClick = { onEngineSelected(PdfEngine.CANVAS) }
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    "Canvas (Android Native)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Fast, reliable, uses Android's built-in PDF API",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // HTML CSS Engine
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedEngine == PdfEngine.HTML_CSS,
                onClick = { onEngineSelected(PdfEngine.HTML_CSS) }
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    "HTML to PDF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Flexible styling, advanced layouts, requires iText library",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // SASS Professional Engine
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedEngine == PdfEngine.SASS_PROFESSIONAL,
                onClick = { onEngineSelected(PdfEngine.SASS_PROFESSIONAL) }
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    "SASS Professional (Premium)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Premium styling engine with advanced features (coming soon)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Locale Selection Content
 */
@Composable
private fun LocaleContent(
    selectedLocale: InvoiceLocale,
    onLocaleSelected: (InvoiceLocale) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Choose your region for date/number/currency formatting",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Locale selection with radio buttons
        InvoiceLocale.values().forEach { locale ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedLocale == locale,
                    onClick = { onLocaleSelected(locale) }
                )
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        locale.displayName(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        locale.example(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Experimental Features Content
 * DISABLED - Requires ViewModel method implementation
 */
/*
@Composable
private fun ExperimentalFeaturesContent(
    settings: InvoiceSettings?,
    viewModel: InvoiceSettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "⚠️ Experimental features may change or be removed without notice",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )

        SettingsDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Enable Advanced Analytics",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableAdvancedAnalytics ?: false,
                onCheckedChange = { viewModel.updateAdvancedAnalytics(it) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Enable Multi-Currency Support",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableMultiCurrency ?: false,
                onCheckedChange = { viewModel.updateMultiCurrency(it) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Enable Payment Reminders",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enablePaymentReminders ?: false,
                onCheckedChange = { viewModel.updatePaymentReminders(it) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Enable Data Export",
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = settings?.enableDataExport ?: false,
                onCheckedChange = { viewModel.updateDataExport(it) }
            )
        }
    }
}
*/

// Extension functions for display
private fun InvoiceLocale.displayName(): String = when (this) {
    InvoiceLocale.AUSTRALIAN    -> "🇦🇺 Australia"
    InvoiceLocale.UNITED_STATES -> "🇺🇸 United States"
    InvoiceLocale.BRITISH       -> "🇬🇧 United Kingdom"
    InvoiceLocale.EUROPEAN      -> "🇪🇺 European Union"
    InvoiceLocale.CANADIAN      -> "🇨🇦 Canada"
    InvoiceLocale.JAPANESE      -> "🇯🇵 Japan"
}

private fun InvoiceLocale.example(): String = when (this) {
    InvoiceLocale.AUSTRALIAN    -> "Date: 10/05/2026 | \$1,234.56"
    InvoiceLocale.UNITED_STATES -> "Date: 05/10/2026 | \$1,234.56"
    InvoiceLocale.BRITISH       -> "Date: 10/05/2026 | £1,234.56"
    InvoiceLocale.EUROPEAN      -> "Date: 10/05/2026 | €1.234,56"
    InvoiceLocale.CANADIAN      -> "Date: 2026-05-10 | \$1,234.56"
    InvoiceLocale.JAPANESE      -> "Date: 2026/05/10 | ¥1,234"
}

