package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle

/**
 * Invoice Style Preview - Shows a visual sample of what each PDF style looks like
 *
 * This helps users understand the differences between styles:
 * - MODERN: Purple gradient, contemporary
 * - MINIMAL: Black & white, elegant
 * - CORPORATE: Blue gradient, formal
 * - CREATIVE: Orange/teal, vibrant
 */
@Composable
fun InvoiceStylePreview(
    style: HtmlInvoiceStyle,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(min = 400.dp, max = 800.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${style.displayName} Preview")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    contentAlignment = Alignment.TopStart
                ) {
                    StylePreviewContent(style)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    style.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Style Details
                StyleDetailsCard(style)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it!")
            }
        }
    )
}

@Composable
private fun StylePreviewContent(style: HtmlInvoiceStyle) {
    val (headerColor, fontFamily, layoutStyle) = when (style) {
        HtmlInvoiceStyle.MODERN -> Triple(
            Color(0xFF6B4C9A),
            FontFamily.SansSerif,
            "Contemporary gradient header"
        )

        HtmlInvoiceStyle.MINIMAL -> Triple(
            Color(0xFF1a1a1a),
            FontFamily.Default,
            "Clean, minimal lines"
        )

        HtmlInvoiceStyle.CORPORATE -> Triple(
            Color(0xFF003366),
            FontFamily.Serif,
            "Formal, traditional style"
        )

        HtmlInvoiceStyle.CREATIVE -> Triple(
            Color(0xFFFF6B35),
            FontFamily.SansSerif,
            "Vibrant, modern gradient"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(headerColor)
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "INVOICE",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content samples
        repeat(3) {
            Text(
                "├─ Sample invoice line ${'$'}${(it + 1) * 100}.00",
                fontSize = 10.sp,
                fontFamily = fontFamily,
                modifier = Modifier.padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer line
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Total: \$300.00",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun StyleDetailsCard(style: HtmlInvoiceStyle) {
    val details = when (style) {
        HtmlInvoiceStyle.MODERN -> mapOf(
            "Header" to "Purple gradient",
            "Font" to "Modern sans-serif (Segoe UI)",
            "Vibe" to "Contemporary, professional",
            "Best For" to "Tech startups, modern businesses"
        )

        HtmlInvoiceStyle.MINIMAL -> mapOf(
            "Header" to "Black border line",
            "Font" to "Classic sans-serif (Arial)",
            "Vibe" to "Clean, elegant, no-nonsense",
            "Best For" to "Consulting, legal, professional services"
        )

        HtmlInvoiceStyle.CORPORATE -> mapOf(
            "Header" to "Navy blue gradient",
            "Font" to "Formal serif (Georgia, Times New Roman)",
            "Vibe" to "Traditional, trustworthy, formal",
            "Best For" to "Finance, enterprises, government"
        )

        HtmlInvoiceStyle.CREATIVE -> mapOf(
            "Header" to "Orange gradient with rounded corners",
            "Font" to "Modern sans-serif with color accents",
            "Vibe" to "Vibrant, energetic, startup vibe",
            "Best For" to "Creative agencies, startups, marketing"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            details.forEach { (label, value) ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color) =
    this.then(
        Modifier.background(color).padding(0.dp)
    )

