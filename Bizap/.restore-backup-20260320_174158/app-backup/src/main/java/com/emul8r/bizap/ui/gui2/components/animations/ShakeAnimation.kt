package com.emul8r.bizap.ui.gui2.components.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * State holder for a shake animation trigger.
 * Call [shake] to programmatically trigger the shake effect.
 */
class ShakeAnimationState {
    private val _trigger = mutableStateOf(0)
    val trigger: State<Int> = _trigger

    fun shake() {
        _trigger.value++
    }
}

@Composable
fun rememberShakeAnimationState(): ShakeAnimationState = remember { ShakeAnimationState() }

/**
 * Applies a horizontal shake animation to [content] whenever [shakeState.trigger] increments.
 * Useful for displaying validation errors on form fields.
 *
 * @param shakeState  Call [ShakeAnimationState.shake] to trigger the animation.
 * @param shakeOffset Maximum horizontal offset in pixels (default 20f).
 */
@Composable
fun ShakeAnimation(
    shakeState: ShakeAnimationState,
    shakeOffset: Float = 20f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val trigger by shakeState.trigger

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-shakeOffset) at 50
                    shakeOffset at 100
                    (-shakeOffset) at 150
                    shakeOffset at 200
                    (-shakeOffset) at 250
                    shakeOffset at 300
                    0f at 400
                }
            )
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier.graphicsLayer { translationX = offsetX.value }
    ) {
        content()
    }
}

