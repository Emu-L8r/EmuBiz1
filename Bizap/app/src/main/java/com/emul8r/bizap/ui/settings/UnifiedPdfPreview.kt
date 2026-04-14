package com.emul8r.bizap.ui.settings

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.emul8r.bizap.domain.model.CanvasInvoiceTemplate
import com.emul8r.bizap.domain.model.PdfEngine
import timber.log.Timber

/**
 * ✨ PHASE 1: Unified PDF preview composable for all engines.
 * Consolidates Canvas and HTML preview rendering into single component.
 * Improves responsiveness by using shared WebView management.
 *
 * Features:
 * - Engine-specific rendering (Canvas via layout factory, HTML via HtmlPdfInvoiceService)
 * - Immediate preview updates via stable state key
 * - WebView cache clearing on meaningful settings changes
 * - One-frame delay acceptable for responsive feel
 */
@Composable
fun UnifiedPdfPreview(
    previewHtml: String?,
    onRefresh: () -> Unit,
    selectedEngine: PdfEngine = PdfEngine.CANVAS,
    selectedCanvasTemplate: CanvasInvoiceTemplate = CanvasInvoiceTemplate.MODERN,
    previewStateKey: String = "default"
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header with title and refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                when (selectedEngine) {
                    PdfEngine.CANVAS -> "📋 Live Preview (Canvas)"
                    PdfEngine.HTML_CSS -> "4️⃣  Live Preview (HTML)"
                    PdfEngine.SASS_PROFESSIONAL -> "✨ Live Preview (SASS Pro)"
                },
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

        // Consistency guarantee badge
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
                    when (selectedEngine) {
                        PdfEngine.CANVAS -> "Canvas preview respects your layout and template choices"
                        PdfEngine.HTML_CSS -> "HTML preview uses iText7-optimized CSS matching your PDF"
                        PdfEngine.SASS_PROFESSIONAL -> "SASS preview shows professional two-column layout with premium styling"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Preview Box with unified rendering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (previewHtml != null) {
                // ✨ PHASE 1: Key-based recomposition for stable WebView management
                // previewStateKey changes only when critical settings change
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
                            setInitialScale(when (selectedEngine) {
                                PdfEngine.CANVAS -> 60
                                else -> 50
                            })
                        }
                    },
                    update = { webView ->
                        // ✨ PHASE 1: Clear cache between updates to prevent stale rendering
                        // One-frame delay acceptable for responsive feel
                        webView.clearCache(true)
                        webView.loadDataWithBaseURL(
                            null,
                            previewHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                        Timber.d("🔄 WebView updated for engine=$selectedEngine (key=$previewStateKey)")
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

