package com.emul8r.bizap.ui.gui2.components.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Slides content in from the right (positive x offset) and out to the left.
 * Used for detail screen push transitions.
 *
 * @param visible      Whether to show the content.
 * @param durationMs   Animation duration in milliseconds (default 350 ms).
 */
@Composable
fun SlideInFromRightAnimation(
    visible: Boolean = true,
    durationMs: Int = 350,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(durationMs),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn(animationSpec = tween(durationMs)),
        exit = slideOutHorizontally(
            animationSpec = tween(durationMs),
            targetOffsetX = { fullWidth -> -fullWidth }
        ) + fadeOut(animationSpec = tween(durationMs)),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Triggers a one-shot slide-in from the right when the composable first appears.
 */
@Composable
fun SlideInOnAppear(
    durationMs: Int = 350,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    SlideInFromRightAnimation(
        visible = visible,
        durationMs = durationMs,
        modifier = modifier,
        content = content
    )
}
