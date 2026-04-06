package com.emul8r.bizap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.emul8r.bizap.MainScreen
import com.emul8r.bizap.ui.gui2.navigation.GuiV2NavGraph
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.state.AppStateViewModel
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ThemeManager
import kotlinx.coroutines.launch

/**
 * Theme-aware navigation graph for the Bizap app.
 * 
 * This navigation graph dynamically switches between Classic (GUI1) and Modern (GUI2)
 * screen implementations based on the current theme selection. Theme changes take
 * effect immediately without requiring app restart.
 * 
 * Implementation Strategy:
 * - CLASSIC theme: Uses existing MainScreen (GUI1) with traditional navigation
 * - MODERN theme: Uses existing GuiV2NavGraph (GUI2) with modern navigation
 * 
 * This approach preserves existing screen implementations while enabling runtime
 * theme switching, eliminating the need for dual activities.
 * 
 * @param navController The navigation controller for managing navigation
 * @param themeManager Manages the current theme selection
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    themeManager: ThemeManager
) {
    // Observe current theme
    val theme by themeManager.theme.collectAsStateWithLifecycle()
    
    // Coroutine scope for theme switching
    val scope = rememberCoroutineScope()
    
    // Get business profile for GUI2 start business ID
    val businessProfileViewModel: BusinessProfileViewModel = hiltViewModel()
    val businessProfile by businessProfileViewModel.profileState.collectAsStateWithLifecycle()

    // Observe UI mode from AppStateViewModel
    val appStateViewModel: AppStateViewModel = hiltViewModel()
    val uiMode by appStateViewModel.uiMode.collectAsStateWithLifecycle()
    
    // Default business ID when no valid business profile is available
    // This matches the default used in ModernGUIMainActivity
    val DEFAULT_BUSINESS_ID = 1L
    val startBusinessId = businessProfile?.id?.takeIf { it > 0 } ?: DEFAULT_BUSINESS_ID

    // Switch between GUI implementations based on theme
    when (theme) {
        AppTheme.CLASSIC -> {
            // Traditional GUI (GUI1) - uses MainScreen with classic navigation
            MainScreen(
                onSwitchGui = {
                    // When user wants to switch GUI, toggle theme to Modern
                    scope.launch {
                        themeManager.setTheme(AppTheme.MODERN)
                    }
                }
            )
        }
        
        AppTheme.MODERN -> {
            // Modern GUI (GUI2) - uses GuiV2NavGraph with modern navigation
            GuiV2NavGraph(
                navController = navController,
                startBusinessId = startBusinessId,
                onSwitchToGui1 = {
                    // When user wants to switch GUI, toggle theme to Classic
                    scope.launch {
                        themeManager.setTheme(AppTheme.CLASSIC)
                    }
                },
                uiMode = uiMode,
                onUIModeChange = { mode -> appStateViewModel.setUIMode(mode) }
            )
        }
    }
}
