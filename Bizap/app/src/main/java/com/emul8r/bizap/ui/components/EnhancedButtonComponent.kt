package com.emul8r.bizap.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Enhanced Button component with consistent styling and professional appearance.
 *
 * IMPROVES:
 * - Consistent button heights (48dp standard, 40dp compact)
 * - Proper text sizing and weight
 * - Smooth transitions and press states
 * - Clear visual hierarchy (filled > tonal > outlined > text)
 * - Optional icon support for better UX
 *
 * Usage:
 * ```
 * EnhancedButton(
 *     text = "Create Invoice",
 *     onClick = { /* Handle click */ }
 * )
 *
 * EnhancedButton(
 *     text = "Send",
 *     icon = Icons.Default.Send,
 *     onClick = { /* Handle click */ },
 *     style = ButtonStyle.FILLED
 * )
 * ```
 */

enum class ButtonStyle {
    FILLED,      // Primary action (solid background)
    TONAL,       // Secondary action (tonal background)
    OUTLINED,    // Tertiary action (bordered)
    TEXT         // Minimal action (text only)
}

enum class ButtonSize {
    SMALL,       // 40dp height, compact padding
    MEDIUM,      // 48dp height, standard padding (default)
    LARGE        // 56dp height, generous padding
}

@Composable
fun EnhancedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.FILLED,
    size: ButtonSize = ButtonSize.MEDIUM,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val buttonHeight = when (size) {
        ButtonSize.SMALL -> 40.dp
        ButtonSize.MEDIUM -> 48.dp
        ButtonSize.LARGE -> 56.dp
    }

    val buttonModifier = modifier
        .height(buttonHeight)
        .padding(horizontal = Spacing.xs)

    when (style) {
        ButtonStyle.FILLED -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(Spacing.md)
            ) {
                ButtonContent(text = text, icon = icon, isLoading = isLoading, size = size)
            }
        }
        ButtonStyle.TONAL -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(Spacing.md),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                ButtonContent(text = text, icon = icon, isLoading = isLoading, size = size)
            }
        }
        ButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(Spacing.md)
            ) {
                ButtonContent(text = text, icon = icon, isLoading = isLoading, size = size)
            }
        }
        ButtonStyle.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = RoundedCornerShape(Spacing.md)
            ) {
                ButtonContent(text = text, icon = icon, isLoading = isLoading, size = size)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    isLoading: Boolean,
    size: ButtonSize
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.padding(Spacing.xs),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = when (size) {
                    ButtonSize.SMALL -> 12.sp
                    ButtonSize.MEDIUM -> 14.sp
                    ButtonSize.LARGE -> 16.sp
                },
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

/**
 * Floating Action Button with enhanced styling.
 */
@Composable
fun EnhancedFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    text: String? = null
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            },
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(Spacing.lg)
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(Spacing.lg)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Icon Button with enhanced touch target and visual feedback.
 */
@Composable
fun EnhancedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.MEDIUM
) {
    val iconSize = when (size) {
        ButtonSize.SMALL -> 20.dp
        ButtonSize.MEDIUM -> 24.dp
        ButtonSize.LARGE -> 28.dp
    }

    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
