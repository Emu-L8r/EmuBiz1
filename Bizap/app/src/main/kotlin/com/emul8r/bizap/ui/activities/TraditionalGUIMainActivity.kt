package com.emul8r.bizap.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.emul8r.bizap.MainActivity
import com.emul8r.bizap.MainScreen
import com.emul8r.bizap.ui.landing.LandingViewModel
import com.emul8r.bizap.presentation.viewmodel.SettingsViewModel
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.ui.theme.BizapTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for the traditional (GUI1) experience.
 *
 * Wraps the existing [MainScreen] composable in an ComponentActivity so it can
 * be started as an independent task.  The calling code may optionally pass
 * [EXTRA_BUSINESS_ID] to communicate the active business ID to the Composable
 * hierarchy via the intent.  If omitted, the default profile loaded by
 * [com.emul8r.bizap.ui.settings.BusinessProfileViewModel] inside [MainScreen]
 * is used automatically.
 *
 * Navigation back to the landing screen is handled by calling
 * [LandingViewModel.resetMode] which clears the persisted preference and lets
 * [com.emul8r.bizap.MainActivity] re-display [LandingScreen].
 */
@AndroidEntryPoint
class TraditionalGUIMainActivity : ComponentActivity() {

    companion object {
        /** Intent extra key used to pass the active business ID to this activity. */
        const val EXTRA_BUSINESS_ID = "extra_business_id"

        /**
         * Build a ready-to-use [Intent] that starts [TraditionalGUIMainActivity].
         *
         * @param context   Calling context.
         * @param businessId  Active business ID, or -1 to use the default profile.
         */
        fun createIntent(context: Context, businessId: Long = -1L): Intent =
            Intent(context, TraditionalGUIMainActivity::class.java).apply {
                putExtra(EXTRA_BUSINESS_ID, businessId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                MainScreen(
                    onSwitchGui = {
                        landingViewModel.resetMode()
                        // After clearing the GUI mode, launch MainActivity to show Landing Screen
                        startActivity(Intent(this@TraditionalGUIMainActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
