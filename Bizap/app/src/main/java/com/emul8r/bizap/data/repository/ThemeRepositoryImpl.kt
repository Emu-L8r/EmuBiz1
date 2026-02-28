package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.emul8r.bizap.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {

    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")

    override val isDarkTheme: Flow<Boolean> = dataStore.data.map {
        it[isDarkThemeKey] ?: false
    }

    override suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[isDarkThemeKey] = isDark
        }
    }
}

