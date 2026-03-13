package com.emul8r.bizap.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.R
import kotlinx.coroutines.delay

private val GradientPurpleStart = Color(0xFF5B3BA0)
private val GradientLavenderEnd = Color(0xFFE8E5F0)

/**
 * Branded splash screen shown at app startup.
 *
 * Features:
 * - Gradient background (purple → lavender)
 * - Company logo at 250dp
 * - Fade-in animation (800ms)
 * - Subtle logo pulse animation (1000ms loop)
 * - Rotating loading spinner (40dp, white)
 * - "Loading..." text below spinner
 * - Fade-out animation (500ms) before navigation
 *
 * @param onSplashComplete Called after the splash duration completes.
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var screenAlpha by remember { mutableStateOf(0f) }
    var exiting by remember { mutableStateOf(false) }

    // Fade-in / fade-out animation
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (exiting) 0f else screenAlpha,
        animationSpec = tween(
            durationMillis = if (exiting) 500 else 800,
            easing = LinearEasing
        ),
        label = "splashFade"
    )

    // Infinite pulse animation on the logo
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val logoPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulseAlpha"
    )

    LaunchedEffect(Unit) {
        // Trigger fade-in
        screenAlpha = 1f
        // Hold for splash duration
        delay(SPLASH_DURATION_MS)
        // Trigger fade-out
        exiting = true
        delay(FADE_OUT_DURATION_MS)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fadeInAlpha)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientPurpleStart, GradientLavenderEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Company logo with pulse effect
            Image(
                painter = painterResource(id = R.drawable.company_logo),
                contentDescription = "Bizap Logo",
                modifier = Modifier
                    .size(dimensionResource(R.dimen.splash_logo_size))
                    .alpha(logoPulseAlpha)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.splash_spacing_large)))

            // Loading spinner (CircularProgressIndicator handles its own rotation animation)
            CircularProgressIndicator(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.splash_spinner_size)),
                color = Color.White,
                strokeWidth = dimensionResource(R.dimen.splash_spinner_stroke)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // "Loading..." label
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

private const val SPLASH_DURATION_MS = 2_500L
private const val FADE_OUT_DURATION_MS = 500L