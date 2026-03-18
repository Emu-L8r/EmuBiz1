package com.emul8r.bizap.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.domain.model.ThemePreference
import com.emul8r.bizap.presentation.viewmodel.SettingsViewModel
import com.emul8r.bizap.ui.settings.ThemeViewModel
import com.emul8r.bizap.ui.theme.BizapTheme

/**
 * Composable wrapper that reads the user's [ThemePreference] from [SettingsViewModel] and
 * applies the correct [BizapTheme] variant before rendering [content].
 *
 * - [ThemePreference.LIGHT] → always uses the light scheme
 * - [ThemePreference.DARK]  → always uses the dark scheme
 * - [ThemePreference.AUTO]  → follows the device's system dark-mode setting (default)
 *
 * This composable is intended to sit at the root of the composition tree (e.g. in
 * `MainActivity`) so that theme changes propagate to the entire app without restarting.
 *
 * The legacy [ThemeViewModel] seed-colour is still respected so that per-business colour
 * customisation continues to work alongside the new three-way theme toggle.
 */
@Composable
fun ThemeProvider(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themePreference by settingsViewModel.themePreference.collectAsStateWithLifecycle()
    val themeConfig by themeViewModel.themeConfig.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()

    val isDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK  -> true
        ThemePreference.AUTO  -> isSystemDark
    }

    BizapTheme(
        themeConfig = ThemeConfig(
            seedColorHex = themeConfig.seedColorHex,
            isDarkMode = isDark
        ),
        content = content
    )
}
