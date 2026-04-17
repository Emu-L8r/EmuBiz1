package com.emul8r.bizap.ui.gui3.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * Global Matrix pulse — oscillates 0.0 → 1.0 → 0.0 on a 2.2s cycle.
 *
 * Emitted ONCE from [MatrixTheme] at the root of the GUI3 composition tree.
 * All pulsing UI components (cards, buttons, badges, scanlines) read from here
 * instead of creating their own InfiniteTransition.
 *
 * Performance: 1 InfiniteTransition for the entire GUI3 UI tree (was 18+).
 * Immersion: All elements pulse in perfect sync — unified system "heartbeat."
 */
val LocalMatrixPulse = compositionLocalOf { 0f }

