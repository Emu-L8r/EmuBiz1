package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.ThemeConfig
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeConfig: Flow<ThemeConfig>
    suspend fun updateSeedColor(hex: String)
    suspend fun updateDarkMode(isDark: Boolean)
}

