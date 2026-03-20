package com.emul8r.bizap.ui.gui2.components.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Wraps [content] in a fade-in/fade-out animation.
 *
 * @param visible      Whether to show the content (defaults to true after first composition).
 * @param durationMs   Animation duration in milliseconds (default 350 ms).
 */
@Composable
fun FadeInAnimation(
    visible: Boolean = true,
    durationMs: Int = 350,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMs)),
        exit = fadeOut(animationSpec = tween(durationMs)),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Triggers a one-shot fade-in when the composable first enters the composition.
 */
@Composable
fun FadeInOnAppear(
    durationMs: Int = 350,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    FadeInAnimation(visible = visible, durationMs = durationMs, modifier = modifier, content = content)
}
