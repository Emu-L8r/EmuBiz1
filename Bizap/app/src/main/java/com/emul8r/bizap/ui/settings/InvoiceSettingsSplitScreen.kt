package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.settings.components.PreviewPanel
import com.emul8r.bizap.ui.settings.components.SettingsPanel
import timber.log.Timber

/**
 * Invoice Settings with Split-Screen Layout (Preview-Centric Design)
 *
 * **NEW ARCHITECTURE (Phase 2 Implementation):**
 * - Left pane (50%): Scrollable settings controls
 * - Right pane (50%): Live PDF preview (always visible)
 * - Real-time updates: <100ms from user interaction to preview change
 *
 * **Design Goals:**
 * ✅ Preview is central to user experience (not hidden in tab)
 * ✅ User sees consequences of every settings change
 * ✅ Encourages experimentation (visual feedback is instant)
 * ✅ No tab switching needed (both settings and preview visible)
 * ✅ Quality score visible in top-right corner
 *
 * **Performance:**
 * - Debounce: 50ms (was 300ms) → 6x faster response
 * - Caching: Smart preview state key prevents unnecessary regenerations
 * - Preview regeneration: <500ms typical, <100ms if cached
 *
 * Usage:
 * ```
 * NavController.navigate("invoiceSettingsSplit")
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsSplitScreen(
    viewModel: InvoiceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewHtml by viewModel.previewHtml.collectAsStateWithLifecycle()
    val isRegenerating by viewModel.isRegeneratingPreview.collectAsStateWithLifecycle()
    val qualityScore by viewModel.qualityScore.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("✅ Settings saved successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Invoice") },
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
        if (uiState.isLoading && uiState.settings == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Loading settings...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (uiState.settings != null) {
            // Main content: Split layout (50% settings, 50% preview)
            LaunchedEffect(previewHtml?.hashCode()) {
                Timber.d("🔑 previewHtml CHANGED! New hash: ${previewHtml?.hashCode()}, length: ${previewHtml?.length ?: 0}")
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // LEFT PANE: Settings controls (scrollable)
                SettingsPanel(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )

                // VERTICAL DIVIDER
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // RIGHT PANE: Live preview (always visible)
                PreviewPanel(
                    previewHtml = previewHtml,
                    isRegenerating = isRegenerating,
                    quality = qualityScore,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else if (uiState.error != null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Failed to load settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { viewModel.retryLoadSettings() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
