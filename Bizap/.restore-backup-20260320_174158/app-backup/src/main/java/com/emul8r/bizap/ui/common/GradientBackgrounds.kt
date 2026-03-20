package com.emul8r.bizap.ui.common

import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.emul8r.bizap.R

/**
 * Background modifiers for consistent visual depth across screens.
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
     * Adds a faded image as a background placeholder.
     * Note: Wrap your content in a Box when using this if you want it to sit behind.
     */
    @Composable
    fun ImagePlaceholderBackground(
        drawableId: Int = R.drawable.thswalogo,
        alpha: Float = 0.08f
    ) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentScale = ContentScale.Inside
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
