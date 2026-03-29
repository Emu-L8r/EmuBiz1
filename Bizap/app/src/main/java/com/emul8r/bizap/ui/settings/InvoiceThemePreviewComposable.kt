package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceTheme

/**
 * Invoice Theme Preview Component
 *
 * Displays a visual preview of how invoices will look with the selected theme.
 * Shows sample invoice with company name and sample items in the selected theme's style.
 */
@Composable
fun InvoiceThemePreview(
    selectedTheme: InvoiceTheme,
    companyName: String,
    primaryColor: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Invoice Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (selectedTheme) {
                InvoiceTheme.CANVAS -> CanvasThemePreview(companyName, primaryColor)
                InvoiceTheme.HTML_PDF -> HtmlThemePreview(companyName, primaryColor)
            }
        }
    }
}

@Composable
private fun CanvasThemePreview(
    companyName: String,
    primaryColor: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header with company name
        Text(
            text = if (companyName.isNotEmpty()) companyName else "Your Company Name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Accent line with primary color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = try {
                        Color(android.graphics.Color.parseColor(primaryColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sample items
        repeat(2) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Item ${index + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${(index + 1) * 100}.00",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Total
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "$300.00",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun HtmlThemePreview(
    companyName: String,
    primaryColor: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = try {
                    Color(android.graphics.Color.parseColor(primaryColor)).copy(alpha = 0.1f)
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                }
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Modern header with color band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    color = try {
                        Color(android.graphics.Color.parseColor(primaryColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (companyName.isNotEmpty()) companyName else "Your Company Name",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Modern items table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = try {
                            Color(android.graphics.Color.parseColor(primaryColor)).copy(alpha = 0.2f)
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        }
                    )
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Item", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Price", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            // Items
            repeat(2) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Item ${index + 1}", style = MaterialTheme.typography.bodySmall)
                    Text("$${(index + 1) * 100}.00", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Total with accent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = try {
                            Color(android.graphics.Color.parseColor(primaryColor)).copy(alpha = 0.15f)
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$300.00",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = try {
                        Color(android.graphics.Color.parseColor(primaryColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

/**
 * Theme Comparison Card
 *
 * Shows side-by-side comparison of theme features
 */
@Composable
fun ThemeComparisonCard(
    theme: InvoiceTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (theme) {
                        InvoiceTheme.CANVAS -> "Canvas Style"
                        InvoiceTheme.HTML_PDF -> "Modern HTML Style"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                when (theme) {
                    InvoiceTheme.CANVAS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FeatureBullet("✓ Clean, traditional design")
                            FeatureBullet("✓ Lightweight rendering")
                            FeatureBullet("✓ Optimized for print")
                        }
                    }
                    InvoiceTheme.HTML_PDF -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FeatureBullet("✓ Modern, professional design")
                            FeatureBullet("✓ Rich formatting options")
                            FeatureBullet("✓ Enhanced branding")
                        }
                    }
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
