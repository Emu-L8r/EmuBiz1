package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.*
import timber.log.Timber

/**
 * ✨ PHASE 2: COMPREHENSIVE VISUAL CUSTOMIZATION UI SECTIONS
 *
 * These composables provide UI controls for all 20+ visual customization options:
 * - Gradient headers
 * - Rounded corners & shadows
 * - Row styling (alternating colors)
 * - Divider options (style, color, thickness)
 * - Highlight options (totals, status badges)
 * - Background patterns
 * - Watermark controls
 */

/**
 * SECTION 1: Gradient & Accent Options
 * Control header gradient appearance and accent colors
 */
@Composable
fun GradientAccentSection(
    enableGradientHeader: Boolean,
    headerGradientEndColor: String,
    onGradientToggled: (Boolean) -> Unit,
    onGradientColorChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🎨 Gradient Header",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableGradientHeader,
                onCheckedChange = onGradientToggled
            )
        }

        if (enableGradientHeader) {
            Text(
                "Gradient End Color",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Simple color preset selector
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ColorPresets.list) { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGradientColorChanged(preset.hex) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(preset.hex)), RoundedCornerShape(4.dp))
                                .border(if (headerGradientEndColor == preset.hex) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        )
                        Text(preset.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * SECTION 2: Shape & Shadow Options
 * Control rounded corners and shadow effects
 */
@Composable
fun ShapeShadowSection(
    enableRoundedCorners: Boolean,
    cornerRadiusDp: Float,
    enableShadows: Boolean,
    shadowIntensity: Float,
    onRoundedCornersToggled: (Boolean) -> Unit,
    onCornerRadiusChanged: (Float) -> Unit,
    onShadowsToggled: (Boolean) -> Unit,
    onShadowIntensityChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Rounded Corners
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🎯 Rounded Corners",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableRoundedCorners,
                onCheckedChange = onRoundedCornersToggled
            )
        }

        if (enableRoundedCorners) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Radius: ${cornerRadiusDp.toInt()}dp", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = cornerRadiusDp,
                    onValueChange = onCornerRadiusChanged,
                    valueRange = 0f..16f,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider()

        // Shadows
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "✨ Drop Shadows",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableShadows,
                onCheckedChange = onShadowsToggled
            )
        }

        if (enableShadows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Intensity: ${(shadowIntensity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = shadowIntensity,
                    onValueChange = onShadowIntensityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * SECTION 3: Row Styling Options
 * Control alternating row colors for better readability
 */
@Composable
fun RowStylingSection(
    enableAlternatingRowColors: Boolean,
    alternateRowColor: String,
    onAlternatingToggled: (Boolean) -> Unit,
    onColorChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "📊 Alternating Row Colors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableAlternatingRowColors,
                onCheckedChange = onAlternatingToggled
            )
        }

        if (enableAlternatingRowColors) {
            Text(
                "Alternate Row Color",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ColorPresets.lightList) { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onColorChanged(preset.hex) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(preset.hex)), RoundedCornerShape(4.dp))
                                .border(if (alternateRowColor == preset.hex) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        )
                        Text(preset.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * SECTION 4: Divider Options
 * Control divider style, color, and thickness
 */
@Composable
fun DividerOptionsSection(
    enableDividers: Boolean,
    dividerStyle: DividerStyle,
    dividerColor: String,
    dividerThicknessPx: Float,
    onDividersToggled: (Boolean) -> Unit,
    onStyleChanged: (DividerStyle) -> Unit,
    onColorChanged: (String) -> Unit,
    onThicknessChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🔲 Dividers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableDividers,
                onCheckedChange = onDividersToggled
            )
        }

        if (enableDividers) {
            // Divider Style Selection
            Text("Style", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DividerStyle.values().forEach { style ->
                    FilterChip(
                        selected = dividerStyle == style,
                        onClick = { onStyleChanged(style) },
                        label = { Text(style.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Divider Color
            Text("Color", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ColorPresets.neutralList) { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onColorChanged(preset.hex) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(preset.hex)), RoundedCornerShape(4.dp))
                                .border(if (dividerColor == preset.hex) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        )
                        Text(preset.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Divider Thickness
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Thickness: ${dividerThicknessPx.toInt()}px", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = dividerThicknessPx,
                    onValueChange = onThicknessChanged,
                    valueRange = 0.5f..4f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * SECTION 5: Highlight Options
 * Control total box highlighting and status badges
 */
@Composable
fun HighlightOptionsSection(
    highlightTotals: Boolean,
    totalBoxStyle: TotalBoxStyle,
    enableStatusBadges: Boolean,
    badgeStyle: BadgeStyle,
    onHighlightToggled: (Boolean) -> Unit,
    onTotalBoxStyleChanged: (TotalBoxStyle) -> Unit,
    onBadgesToggled: (Boolean) -> Unit,
    onBadgeStyleChanged: (BadgeStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Highlight Totals
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "💰 Highlight Totals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = highlightTotals,
                onCheckedChange = onHighlightToggled
            )
        }

        if (highlightTotals) {
            Text("Total Box Style", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(TotalBoxStyle.values()) { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTotalBoxStyleChanged(style) }
                            .padding(8.dp)
                            .background(
                                if (totalBoxStyle == style) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (totalBoxStyle == style) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(style.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        HorizontalDivider()

        // Status Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🏷️  Status Badges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableStatusBadges,
                onCheckedChange = onBadgesToggled
            )
        }

        if (enableStatusBadges) {
            Text("Badge Style", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(BadgeStyle.values()) { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBadgeStyleChanged(style) }
                            .padding(8.dp)
                            .background(
                                if (badgeStyle == style) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (badgeStyle == style) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(style.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * SECTION 6: Background Pattern Options
 * Control background texture and pattern visibility
 */
@Composable
fun BackgroundPatternSection(
    enableBackgroundPattern: Boolean,
    backgroundPatternType: BackgroundPattern,
    patternOpacity: Float,
    onPatternToggled: (Boolean) -> Unit,
    onPatternTypeChanged: (BackgroundPattern) -> Unit,
    onOpacityChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🎭 Background Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableBackgroundPattern,
                onCheckedChange = onPatternToggled
            )
        }

        if (enableBackgroundPattern) {
            Text("Pattern Type", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(BackgroundPattern.values()) { pattern ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPatternTypeChanged(pattern) }
                            .padding(8.dp)
                            .background(
                                if (backgroundPatternType == pattern) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (backgroundPatternType == pattern) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(pattern.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Opacity: ${(patternOpacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = patternOpacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * SECTION 7: Watermark Options
 * Control watermark text and visibility
 */
@Composable
fun WatermarkSection(
    enableWatermarkText: Boolean,
    watermarkText: String,
    watermarkOpacity: Float,
    onWatermarkToggled: (Boolean) -> Unit,
    onWatermarkTextChanged: (String) -> Unit,
    onOpacityChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "💧 Watermark",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableWatermarkText,
                onCheckedChange = onWatermarkToggled
            )
        }

        if (enableWatermarkText) {
            TextField(
                value = watermarkText,
                onValueChange = onWatermarkTextChanged,
                label = { Text("Watermark Text") },
                placeholder = { Text("e.g., DRAFT, CONFIDENTIAL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Opacity: ${(watermarkOpacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = watermarkOpacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COLOR PRESET HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private object ColorPresets {
    data class Preset(val name: String, val hex: String)

    val list = listOf(
        Preset("Orange", "#FF9F43"),
        Preset("Red", "#E74C3C"),
        Preset("Purple", "#6B4C9A"),
        Preset("Blue", "#3498DB"),
        Preset("Teal", "#1ABC9C"),
        Preset("Green", "#27AE60"),
    )

    val lightList = listOf(
        Preset("Light Gray", "#F5F5F5"),
        Preset("Pale Blue", "#E3F2FD"),
        Preset("Light Purple", "#F3E5F5"),
        Preset("Light Green", "#E8F5E9"),
        Preset("Light Orange", "#FFF3E0"),
    )

    val neutralList = listOf(
        Preset("Light Gray", "#CCCCCC"),
        Preset("Dark Gray", "#666666"),
        Preset("Black", "#000000"),
        Preset("Blue", "#3498DB"),
        Preset("Purple", "#6B4C9A"),
    )
}







