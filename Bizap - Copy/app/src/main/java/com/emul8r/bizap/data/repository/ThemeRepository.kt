package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.domain.model.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val SEED_COLOR = stringPreferencesKey("seed_color")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val themeConfig: Flow<ThemeConfig> = dataStore.data.map { prefs ->
        ThemeConfig(
            seedColorHex = prefs[Keys.SEED_COLOR] ?: "#6750A4",
            isDarkMode = prefs[Keys.DARK_MODE] ?: false
        )
    }

    suspend fun updateSeedColor(hex: String) {
        dataStore.edit { it[Keys.SEED_COLOR] = hex }
    }
}
