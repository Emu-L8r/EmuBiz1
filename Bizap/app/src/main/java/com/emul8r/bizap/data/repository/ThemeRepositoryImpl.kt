package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {
    private object Keys {
        val SEED_COLOR = stringPreferencesKey("seed_color")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    override val themeConfig: Flow<ThemeConfig> = dataStore.data.map { prefs ->
        ThemeConfig(
            seedColorHex = prefs[Keys.SEED_COLOR] ?: "#6750A4",
            isDarkMode = prefs[Keys.DARK_MODE] ?: false
        )
    }

    override suspend fun updateSeedColor(hex: String) {
        dataStore.edit { it[Keys.SEED_COLOR] = hex }
    }

    override suspend fun updateDarkMode(isDark: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = isDark }
    }
}

