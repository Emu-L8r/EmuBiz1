package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle

/**
 * Invoice Style Preview - Shows a visual sample of what each PDF style looks like.
 *
 * Displays a mini mock-up of the invoice header, sample line items, and a totals
 * row, along with a details card describing the style's characteristics.
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
                // Mini invoice preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape),
                    contentAlignment = Alignment.TopStart
                ) {
                    StylePreviewContent(style)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    style.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
    // Destructure header color + font for each style — rendering happens below
    val (headerColor, fontFamily) = when (style) {
        HtmlInvoiceStyle.MODERN               -> Pair(Color(0xFF6B4C9A), FontFamily.SansSerif)
        HtmlInvoiceStyle.MINIMAL              -> Pair(Color(0xFF1a1a1a), FontFamily.Default)
        HtmlInvoiceStyle.CORPORATE            -> Pair(Color(0xFF003366), FontFamily.Serif)
        HtmlInvoiceStyle.CREATIVE             -> Pair(Color(0xFFFF6B35), FontFamily.SansSerif)
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL -> Pair(Color(0xFF1C1C2E), FontFamily.SansSerif)
        HtmlInvoiceStyle.WARM_APPROACHABLE    -> Pair(Color(0xFFF59E0B), FontFamily.SansSerif)
        HtmlInvoiceStyle.SASS_PROFESSIONAL    -> Pair(Color(0xFF0A2540), FontFamily.SansSerif)
        HtmlInvoiceStyle.REFINED              -> Pair(Color(0xFF6B4C9A), FontFamily.SansSerif)
        HtmlInvoiceStyle.PROFESSIONAL_PLUS    -> Pair(Color(0xFF1A1A2E), FontFamily.SansSerif)
        HtmlInvoiceStyle.CLEAN_PROFESSIONAL   -> Pair(Color(0xFF003366), FontFamily.SansSerif)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Header bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(headerColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "INVOICE",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sample line items
        repeat(3) { i ->
            Text(
                text = "├─ Sample invoice line \$${(i + 1) * 100}.00",
                fontSize = 10.sp,
                fontFamily = fontFamily,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total: \$300.00",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun StyleDetailsCard(style: HtmlInvoiceStyle) {
    val details: Map<String, String> = when (style) {
        HtmlInvoiceStyle.MODERN -> mapOf(
            "Header" to "Purple gradient",
            "Font"   to "Modern sans-serif (Segoe UI)",
            "Vibe"   to "Contemporary, professional",
            "Best For" to "Tech startups, modern businesses"
        )
        HtmlInvoiceStyle.MINIMAL -> mapOf(
            "Header" to "Black border line",
            "Font"   to "Classic sans-serif (Arial)",
            "Vibe"   to "Clean, elegant, no-nonsense",
            "Best For" to "Consulting, legal, professional services"
        )
        HtmlInvoiceStyle.CORPORATE -> mapOf(
            "Header" to "Navy blue gradient",
            "Font"   to "Formal serif (Georgia, Times New Roman)",
            "Vibe"   to "Traditional, trustworthy, formal",
            "Best For" to "Finance, enterprises, government"
        )
        HtmlInvoiceStyle.CREATIVE -> mapOf(
            "Header" to "Orange gradient with rounded corners",
            "Font"   to "Modern sans-serif with color accents",
            "Vibe"   to "Vibrant, energetic, startup vibe",
            "Best For" to "Creative agencies, startups, marketing"
        )
        HtmlInvoiceStyle.PREMIUM_PROFESSIONAL -> mapOf(
            "Header" to "Dark navy with blue accent bar",
            "Font"   to "Modern sans-serif (Segoe UI)",
            "Vibe"   to "Premium, minimalist, high-contrast",
            "Best For" to "Premium services, consulting, B2B"
        )
        HtmlInvoiceStyle.WARM_APPROACHABLE -> mapOf(
            "Header" to "Warm cream with amber accent",
            "Font"   to "Friendly sans-serif (Segoe UI)",
            "Vibe"   to "Warm, approachable, friendly",
            "Best For" to "Small business, trades, personal services"
        )
        HtmlInvoiceStyle.SASS_PROFESSIONAL -> mapOf(
            "Header" to "Deep navy blue with professional layout",
            "Font"   to "Modern sans-serif (Segoe UI)",
            "Vibe"   to "Professional, compiled SASS styles",
            "Best For" to "Enterprise, professional services, B2B"
        )
        HtmlInvoiceStyle.REFINED -> mapOf(
            "Header" to "Purple gradient, 60px height",
            "Font"   to "Modern sans-serif (Segoe UI)",
            "Vibe"   to "Grid-matched with Canvas, professional",
            "Best For" to "Businesses wanting consistent styling"
        )
        HtmlInvoiceStyle.PROFESSIONAL_PLUS -> mapOf(
            "Header" to "Dark charcoal sidebar with teal accent bar",
            "Font"   to "Modern sans-serif (Arial/Segoe UI)",
            "Vibe"   to "Premium, modern, structured — highest quality template",
            "Best For" to "Premium services, B2B, enterprise"
        )
        HtmlInvoiceStyle.CLEAN_PROFESSIONAL -> mapOf(
            "Header" to "Flat navy — readable, no gradient",
            "Font"   to "Modern sans-serif (Segoe UI)",
            "Vibe"   to "Reference-quality: left accent lines, gold totals, generous whitespace",
            "Best For" to "Any business wanting a polished, professional look"
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Text(
                        text = "$label: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.widthIn(min = 56.dp)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
