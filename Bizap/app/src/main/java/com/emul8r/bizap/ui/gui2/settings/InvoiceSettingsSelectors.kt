package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.BadgeStyle
import com.emul8r.bizap.domain.model.ColorScheme
import com.emul8r.bizap.domain.model.DividerStyle
import com.emul8r.bizap.domain.model.PageLayout
import com.emul8r.bizap.domain.model.SpacingProfile
import com.emul8r.bizap.domain.model.TotalBoxStyle

/**
 * Selector for Page Layout preference.
 * Displays 9 layout options with emoji icons.
 */
@Composable
fun PageLayoutSelector(
    current: PageLayout,
    onSelect: (PageLayout) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Page Layout",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Explicit list of all layouts
        val layouts = listOf(
            PageLayout.CLASSIC,
            PageLayout.MODERN,
            PageLayout.SPACIOUS,
            PageLayout.COMPACT,
            PageLayout.SIDEBAR,
            PageLayout.CARDS,
            PageLayout.MINIMAL_TABLES,
            PageLayout.FOCUSED,
            PageLayout.ADVANCED_PAGINATED
        )

        layouts.chunked(3).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { layout ->
                    LayoutOption(
                        label = layout.displayName,
                        emoji = layout.emoji,
                        isSelected = current == layout,
                        onClick = { onSelect(layout) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Selector for Spacing Profile preference.
 * Displays 4 spacing options.
 */
@Composable
fun SpacingProfileSelector(
    current: SpacingProfile,
    onSelect: (SpacingProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Spacing Profile",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(SpacingProfile.TIGHT, SpacingProfile.NORMAL, SpacingProfile.GENEROUS, SpacingProfile.PREMIUM).forEach { profile ->
                OptionChip(
                    label = profile.displayName,
                    emoji = profile.emoji,
                    isSelected = current == profile,
                    onClick = { onSelect(profile) }
                )
            }
        }
    }
}

/**
 * Selector for Color Scheme preference.
 * Displays 6 color scheme options with color preview.
 */
@Composable
fun ColorSchemeSelector(
    current: ColorScheme,
    onSelect: (ColorScheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Color Scheme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Explicit list of all color schemes
        val schemes: List<ColorScheme> = listOf(
            ColorScheme.PROFESSIONAL,
            ColorScheme.VIBRANT,
            ColorScheme.MINIMAL,
            ColorScheme.WARM,
            ColorScheme.TECH,
            ColorScheme.NATURE
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            schemes.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { scheme ->
                        ColorSchemeOption(
                            scheme = scheme,
                            isSelected = current == scheme,
                            onClick = { onSelect(scheme) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual color scheme option with color preview.
 */
@Composable
fun ColorSchemeOption(
    scheme: ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = scheme.emoji,
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = Color(android.graphics.Color.parseColor(scheme.primaryHex)),
                        shape = MaterialTheme.shapes.small
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = Color(android.graphics.Color.parseColor(scheme.accentHex)),
                        shape = MaterialTheme.shapes.small
                    )
            )
        }

        Text(
            text = scheme.displayName,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Selector for Total Box Style preference.
 * Displays 4 style options.
 */
@Composable
fun TotalBoxStyleSelector(
    current: TotalBoxStyle,
    onSelect: (TotalBoxStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Total Box Style",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(TotalBoxStyle.SUBTLE_BACKGROUND, TotalBoxStyle.ACCENT_BORDER, TotalBoxStyle.BOLD_HIGHLIGHT, TotalBoxStyle.GRADIENT_BACKGROUND).forEach { style ->
                OptionChip(
                    label = style.name.replace("_", " "),
                    emoji = "🎯",
                    isSelected = current == style,
                    onClick = { onSelect(style) }
                )
            }
        }
    }
}

/**
 * Generic option chip for selections.
 */
@Composable
fun OptionChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/**
 * Individual layout option.
 */
@Composable
fun LayoutOption(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Toggle for visual accents and features.
 */
@Composable
fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}



