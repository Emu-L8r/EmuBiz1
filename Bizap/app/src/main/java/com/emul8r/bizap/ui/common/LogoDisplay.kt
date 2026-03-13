package com.emul8r.bizap.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.emul8r.bizap.R

/**
 * Reusable Bizap logo display component.
 * Provides consistent logo rendering across the app with configurable size and shadow.
 *
 * @param modifier Additional modifiers to apply
 * @param size Logo size (width and height)
 * @param showShadow Whether to display shadow elevation (default true)
 */
@Composable
fun BizapLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showShadow: Boolean = true
) {
    val logoModifier = if (showShadow) {
        modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
    } else {
        modifier.size(size)
    }

    Image(
        painter = painterResource(R.drawable.company_logo),
        contentDescription = "Bizap Logo",
        modifier = logoModifier,
        contentScale = ContentScale.Fit
    )
}
