package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

/**
 * PDF Preview Panel for showing real-time invoice previews.
 *
 * Displays HTML content that would be rendered to PDF, with loading states
 * and error handling.
 */
@Composable
fun PdfPreviewPanel(
    previewFlow: Flow<String?>,
    modifier: Modifier = Modifier
) {
    val previewContent = previewFlow.collectAsState(initial = null)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "PDF Preview"
                )
                Text(
                    text = "PDF Preview",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Refresh indicator
            if (previewContent.value == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Divider(modifier = Modifier.padding(bottom = 12.dp))

        // Preview content
        when {
            previewContent.value == null -> {
                PreviewLoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            previewContent.value?.isNotEmpty() == true -> {
                PreviewContentDisplay(
                    htmlContent = previewContent.value ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                )
            }
            else -> {
                PreviewErrorState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        // Info text
        Text(
            text = "This preview updates automatically as you change settings (with 1 second delay).",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

/**
 * Loading state for preview.
 */
@Composable
private fun PreviewLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp)
            Text(
                text = "Generating preview...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error state for preview.
 */
@Composable
private fun PreviewErrorState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Preview generation failed",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Display HTML preview content.
 *
 * In a real implementation, this would render the HTML to a PDF viewer or WebView.
 * For now, shows a text preview with HTML highlighting.
 */
@Composable
private fun PreviewContentDisplay(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Show first 500 chars of HTML with syntax highlighting
        val displayContent = if (htmlContent.length > 500) {
            htmlContent.substring(0, 500) + "...\n\n(Preview truncated - full content rendered to PDF)"
        } else {
            htmlContent
        }

        SelectionContainer {
            Text(
                text = displayContent,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Info about what users will see
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "💡 This HTML content is converted to a professional PDF with your selected colors, spacing, and layout preferences.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/**
 * Standalone preview indicator for compact display.
 */
@Composable
fun CompactPreviewIndicator(
    previewFlow: Flow<String?>,
    modifier: Modifier = Modifier
) {
    val previewState = previewFlow.collectAsState(initial = null)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PDF Preview",
            style = MaterialTheme.typography.labelMedium
        )

        when {
            previewState.value == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 1.5.dp
                )
            }
            previewState.value?.isNotEmpty() == true -> {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Preview Ready",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            else -> {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * SelectionContainer for allowing text selection in preview.
 * (Placeholder - actual implementation uses Compose's SelectionContainer)
 */
@Composable
private fun SelectionContainer(content: @Composable () -> Unit) {
    content()
}


