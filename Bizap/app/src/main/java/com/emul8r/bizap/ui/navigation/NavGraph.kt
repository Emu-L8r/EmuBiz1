package com.emul8r.bizap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui2.navigation.GuiV2NavGraph
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.state.AppStateViewModel
import com.emul8r.bizap.ui.theme.ThemeManager

/**
 * Navigation graph for the Bizap app (GUI2 - Modern only).
 *
 * Phase 1 Consolidation: Deleted GUI1 (MainScreen, Screen sealed class)
 * Now uses only GuiV2NavGraph for single, clean UI path.
 *
 * @param navController The navigation controller for managing navigation
 * @param themeManager Manages the current theme selection
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    themeManager: ThemeManager
) {
    // Get business profile for GUI2 start business ID
    val businessProfileViewModel: BusinessProfileViewModel = hiltViewModel()
    val businessProfile by businessProfileViewModel.profileState.collectAsStateWithLifecycle()

    // Observe UI mode from AppStateViewModel
    val appStateViewModel: AppStateViewModel = hiltViewModel()
    val uiMode by appStateViewModel.uiMode.collectAsStateWithLifecycle()

    // Default business ID when no valid business profile is available
    val DEFAULT_BUSINESS_ID = 1L
    val startBusinessId = businessProfile?.id?.takeIf { it > 0 } ?: DEFAULT_BUSINESS_ID

    // Modern GUI (GUI2) - single, clean navigation path
    GuiV2NavGraph(
        navController = navController,
        startBusinessId = startBusinessId,
        onSwitchToGui1 = {
            // GUI1 deleted - this callback is no longer used
            // Theme switching removed (only MODERN theme exists now)
        },
        uiMode = uiMode,
        onUIModeChange = { mode -> appStateViewModel.setUIMode(mode) }
    )
}
