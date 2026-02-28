package com.emu.emubizwax.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeConfig: Flow<ThemeConfig>
    suspend fun updatePrimaryColor(colorHex: String)
    suspend fun setDarkMode(isDark: Boolean)
}

data class ThemeConfig(
    val primaryColorHex: String = "#6200EE", // Default Purple
    val isDarkMode: Boolean = false
)
