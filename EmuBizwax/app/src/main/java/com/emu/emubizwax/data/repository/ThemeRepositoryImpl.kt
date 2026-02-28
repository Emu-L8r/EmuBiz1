package com.emu.emubizwax.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emu.emubizwax.domain.repository.ThemeConfig
import com.emu.emubizwax.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {
    
    private object Keys {
        val PRIMARY_COLOR = stringPreferencesKey("primary_color")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    override val themeConfig: Flow<ThemeConfig> = dataStore.data.map { prefs ->
        ThemeConfig(
            primaryColorHex = prefs[Keys.PRIMARY_COLOR] ?: "#6200EE",
            isDarkMode = prefs[Keys.DARK_MODE] ?: false
        )
    }

    override suspend fun updatePrimaryColor(colorHex: String) {
        dataStore.edit { it[Keys.PRIMARY_COLOR] = colorHex }
    }

    override suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = isDark }
    }
}
