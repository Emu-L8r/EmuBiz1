package com.example.databaser.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun M3SwipeToDismiss(
    state: androidx.compose.material.DismissState,
    background: @Composable (Boolean) -> Unit,
    dismissContent: @Composable (Boolean) -> Unit
) {
    SwipeToDismiss(
        state = state,
        background = { background(state.dismissDirection != null) },
        dismissContent = { dismissContent(state.dismissDirection != null) }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun M3SwipeToDismissBackground(
    dismissDirection: DismissDirection?,
    content: @Composable () -> Unit
) {
    val color by animateColorAsState(
        targetValue = when (dismissDirection) {
            DismissDirection.StartToEnd, DismissDirection.EndToStart -> MaterialTheme.colorScheme.error
            else -> Color.Transparent
        },
        label = "color"
    )
    val scale by animateFloatAsState(
        targetValue = if (dismissDirection != null) 1f else 0.75f,
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = when (dismissDirection) {
            DismissDirection.StartToEnd -> Alignment.CenterStart
            else -> Alignment.CenterEnd
        }
    ) {
        content()
    }
}
