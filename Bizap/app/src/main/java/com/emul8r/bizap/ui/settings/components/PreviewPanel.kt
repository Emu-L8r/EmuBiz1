package com.emul8r.bizap.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebSettings
import timber.log.Timber

/**
 * Live PDF Preview Panel (Right side of split view)
 *
 * Features:
 * - Real-time HTML preview using WebView
 * - Loading indicator during regeneration
 * - Quality score display
 * - Helpful tips about preview
 *
 * **IMPORTANT:** This component is designed for the split-screen layout.
 * Preview updates automatically when ViewModel's previewHtml StateFlow changes.
 */
@Composable
fun PreviewPanel(
    previewHtml: String?,
    isRegenerating: Boolean = false,
    quality: Float = 75f,
    modifier: Modifier = Modifier
) {
    Timber.d("🎬 PreviewPanel rendering: previewHtml=${previewHtml?.take(30)}..., isRegenerating=$isRegenerating")

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (previewHtml != null && !isRegenerating) {
            Timber.d("🎬 PreviewPanel: Showing WebView (${previewHtml.length} bytes)")
            // Main preview content
            PreviewWebView(previewHtml = previewHtml)
        } else if (isRegenerating) {
            Timber.d("🎬 PreviewPanel: Showing LoadingPreview")
            // Loading state
            LoadingPreview()
        } else {
            Timber.d("🎬 PreviewPanel: Showing EmptyPreview")
            // Empty state
            EmptyPreview()
        }

        // Top right corner: Quality indicator (always visible)
        QualityIndicator(quality = quality, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
    }
}

/**
 * WebView for displaying HTML preview
 * Optimized for performance and responsiveness
 */
@Composable
private fun PreviewWebView(
    previewHtml: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    domStorageEnabled = false
                    @Suppress("DEPRECATION")
                    databaseEnabled = false
                    @Suppress("DEPRECATION")
                    savePassword = false
                }
                // Disable JavaScript for security + performance
                settings.javaScriptEnabled = false
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            try {
                webView.loadDataWithBaseURL(
                    null,
                    previewHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
                Timber.d("📱 WebView preview loaded (size=${previewHtml.length} bytes)")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load preview in WebView")
            }
        }
    )
}

/**
 * Loading state with pulsing indicator
 */
@Composable
private fun LoadingPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Generating preview...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty state when no preview available
 */
@Composable
private fun EmptyPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No preview",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No preview available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Adjust settings to see preview updates in real-time",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * Quality score indicator (compact, floating)
 * Shows in top-right corner of preview
 */
@Composable
private fun QualityIndicator(
    quality: Float,
    modifier: Modifier = Modifier
) {
    val qualityColor = when {
        quality >= 80f -> Color(0xFF4CAF50)  // Green
        quality >= 60f -> Color(0xFFFFC107)  // Orange
        else -> Color(0xFFF44336)  // Red
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = qualityColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, qualityColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp, 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Quality",
                style = MaterialTheme.typography.labelSmall,
                color = qualityColor
            )
            Text(
                "${quality.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = qualityColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

/**
 * Compact quality gauge for inline display in settings sections
 * Used when showing quality alongside controls
 */
@Composable
fun QualityScoreCompact(
    score: Float,
    modifier: Modifier = Modifier
) {
    val percentage = (score * 100).toInt().coerceIn(0, 100)
    val qualityColor = when {
        percentage >= 80 -> Color(0xFF4CAF50)
        percentage >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quality Score", style = MaterialTheme.typography.labelSmall)
        Surface(
            shape = MaterialTheme.shapes.small,
            color = qualityColor.copy(alpha = 0.15f)
        ) {
            Text(
                "$percentage%",
                modifier = Modifier.padding(8.dp, 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = qualityColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}
