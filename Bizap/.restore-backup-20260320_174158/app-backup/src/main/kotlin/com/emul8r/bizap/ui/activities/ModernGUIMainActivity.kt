package com.emul8r.bizap.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.emul8r.bizap.MainActivity
import com.emul8r.bizap.ui.gui2.navigation.GuiV2NavGraph
import com.emul8r.bizap.ui.landing.LandingViewModel
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.presentation.viewmodel.SettingsViewModel
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.ui.theme.BizapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for the modern (GUI2) experience.
 *
 * Hosts the Compose-based [GuiV2NavGraph] inside a Material 3 theme.  The
 * business ID is read from [EXTRA_BUSINESS_ID] in the launching [Intent]; when
 * absent the activity reads the current profile from [BusinessProfileViewModel].
 *
 * Switching back to GUI1 (or to the landing screen) is handled by calling
 * [LandingViewModel.resetMode], which clears the DataStore preference and
 * causes [com.emul8r.bizap.MainActivity] to re-display [LandingScreen].
 */
@AndroidEntryPoint
class ModernGUIMainActivity : ComponentActivity() {

    companion object {
        /** Intent extra key used to pass the active business ID to this activity. */
        const val EXTRA_BUSINESS_ID = "extra_business_id"

        /** Fallback business ID used when no valid ID is available from intent or profile. */
        const val DEFAULT_BUSINESS_ID = 1L

        /**
         * Build a ready-to-use [Intent] that starts [ModernGUIMainActivity].
         *
         * @param context    Calling context.
         * @param businessId Active business ID, or -1 to use the default profile.
         */
        fun createIntent(context: Context, businessId: Long = -1L): Intent =
            Intent(context, ModernGUIMainActivity::class.java).apply {
                putExtra(EXTRA_BUSINESS_ID, businessId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intentBusinessId = intent.getLongExtra(EXTRA_BUSINESS_ID, -1L)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()

            val isDark = when (settings.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.AUTO -> isSystemDark
            }

            val config = ThemeConfig(
                seedColorHex = "#FF6200EE",
                isDarkMode = isDark
            )

            BizapTheme(themeConfig = config) {
                val landingViewModel: LandingViewModel = hiltViewModel()
                val businessProfileViewModel: BusinessProfileViewModel = hiltViewModel()
                val businessProfile by businessProfileViewModel.profileState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                val resolvedBusinessId = when {
                    intentBusinessId > 0L -> intentBusinessId
                    businessProfile.id > 0L -> businessProfile.id
                    else -> DEFAULT_BUSINESS_ID
                }

                GuiV2NavGraph(
                    navController = navController,
                    startBusinessId = resolvedBusinessId,
                    onSwitchToGui1 = {
                        landingViewModel.resetMode()
                        // After clearing the GUI mode, launch MainActivity to show Landing Screen
                        startActivity(Intent(this@ModernGUIMainActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
