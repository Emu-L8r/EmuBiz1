package com.emul8r.bizap.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Gradient background modifiers for consistent visual depth across screens.
 */
object GradientBackgrounds {
    
    /**
     * Subtle vertical gradient from surface to surface variant.
     * Perfect for screen backgrounds that need subtle depth.
     */
    @Composable
    fun Modifier.subtleVerticalGradient(): Modifier {
        val colorScheme = MaterialTheme.colorScheme
        return this.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorScheme.surface,
                    colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        )
    }
    
    /**
     * Primary colored gradient for header sections.
     * Creates professional branded appearance.
     */
    @Composable
    fun Modifier.primaryHeaderGradient(): Modifier {
        val colorScheme = MaterialTheme.colorScheme
        return this.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorScheme.primary,
                    colorScheme.primary.copy(alpha = 0.85f)
                )
            )
        )
    }
    
    /**
     * Custom gradient with specified colors.
     * Useful for specific design requirements.
     */
    fun Modifier.customGradient(
        startColor: Color,
        endColor: Color,
        vertical: Boolean = true
    ): Modifier {
        return this.background(
            brush = if (vertical) {
                Brush.verticalGradient(colors = listOf(startColor, endColor))
            } else {
                Brush.horizontalGradient(colors = listOf(startColor, endColor))
            }
        )
    }
}
