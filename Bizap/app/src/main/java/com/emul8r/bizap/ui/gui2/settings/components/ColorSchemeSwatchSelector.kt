package com.emul8r.bizap.ui.gui2.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.ColorScheme

/**
 * Displays color scheme options as visual swatch cards with:
 * - Primary & accent color swatches
 * - Color scheme name
 * - Selected indicator (checkmark + highlight border)
 *
 * Much better UX than a plain dropdown!
 */
@Composable
fun ColorSchemeSwatchSelector(
    selectedScheme: ColorScheme,
    onSchemeSelected: (ColorScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "🎨 Color Scheme",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            "Choose a color palette for your invoices",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Grid of color scheme cards (3 per row)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Professional, Vibrant, Minimal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSchemeCard(
                    scheme = ColorScheme.PROFESSIONAL,
                    isSelected = selectedScheme == ColorScheme.PROFESSIONAL,
                    onSelect = { onSchemeSelected(it) },
                    modifier = Modifier.weight(1f)
                )
                ColorSchemeCard(
                    scheme = ColorScheme.VIBRANT,
                    isSelected = selectedScheme == ColorScheme.VIBRANT,
                    onSelect = { onSchemeSelected(it) },
                    modifier = Modifier.weight(1f)
                )
                ColorSchemeCard(
                    scheme = ColorScheme.MINIMAL,
                    isSelected = selectedScheme == ColorScheme.MINIMAL,
                    onSelect = { onSchemeSelected(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Warm, Tech, Nature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSchemeCard(
                    scheme = ColorScheme.WARM,
                    isSelected = selectedScheme == ColorScheme.WARM,
                    onSelect = { onSchemeSelected(it) },
                    modifier = Modifier.weight(1f)
                )
                ColorSchemeCard(
                    scheme = ColorScheme.TECH,
                    isSelected = selectedScheme == ColorScheme.TECH,
                    onSelect = { onSchemeSelected(it) },
                    modifier = Modifier.weight(1f)
                )
                ColorSchemeCard(
                    scheme = ColorScheme.NATURE,
                    isSelected = selectedScheme == ColorScheme.NATURE,
                    onSelect = { onSchemeSelected(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColorSchemeCard(
    scheme: ColorScheme,
    isSelected: Boolean,
    onSelect: (ColorScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    val borderWidth = if (isSelected) 2.dp else 1.dp

    val primaryColor = Color(android.graphics.Color.parseColor(scheme.primaryHex))
    val accentColor = Color(android.graphics.Color.parseColor(scheme.accentHex))

    Card(
        modifier = modifier
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect(scheme) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Color swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(primaryColor, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(accentColor, RoundedCornerShape(4.dp))
                )
            }

            // Scheme name with emoji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    scheme.emoji,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    scheme.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Selected checkmark
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

