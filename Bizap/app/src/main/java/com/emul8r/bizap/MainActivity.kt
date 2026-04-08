package com.emul8r.bizap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.emul8r.bizap.presentation.ui.theme.ThemeProvider
import com.emul8r.bizap.ui.gui2.navigation.GuiV2NavGraph
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.state.AppStateViewModel
import com.emul8r.bizap.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity for Bizap (GUI2 - Modern Only)
 *
 * Phase 1 Consolidation: Simplified after deleting GUI1
 * Now serves as the single launcher entry point for the app.
 * Directly shows GuiV2NavGraph wrapped in ThemeProvider for proper theming.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ThemeProvider {
                MainActivityContent()
            }
        }
    }
}

@Composable
private fun MainActivityContent(
    businessProfileViewModel: BusinessProfileViewModel = hiltViewModel(),
    appStateViewModel: AppStateViewModel = hiltViewModel(),
) {
    // Get business profile for start business ID
    val businessProfile by businessProfileViewModel.profileState.collectAsStateWithLifecycle()

    // Observe UI mode
    val uiMode by appStateViewModel.uiMode.collectAsStateWithLifecycle()

    // Default business ID
    val DEFAULT_BUSINESS_ID = 1L
    val startBusinessId = businessProfile?.id?.takeIf { it > 0 } ?: DEFAULT_BUSINESS_ID

    // Navigation controller
    val navController = rememberNavController()

    // Modern GUI (GUI2) - single, clean path
    GuiV2NavGraph(
        navController = navController,
        startBusinessId = startBusinessId,
        onSwitchToGui1 = {
            // GUI1 deleted - this callback is no longer used
        },
        uiMode = uiMode,
        onUIModeChange = { mode -> appStateViewModel.setUIMode(mode) }
    )
}




