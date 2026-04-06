package com.emul8r.bizap.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.emul8r.bizap.domain.settings.UIPreferences
import com.emul8r.bizap.domain.model.UIMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIPreferencesImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UIPreferences {

    private companion object {
        val UI_MODE_KEY = stringPreferencesKey("ui_mode")
    }

    override val uiMode: Flow<UIMode> = dataStore.data.map { prefs ->
        prefs[UI_MODE_KEY]?.let { name ->
            runCatching { UIMode.valueOf(name) }.getOrDefault(UIMode.DEFAULT)
        } ?: UIMode.DEFAULT
    }

    override suspend fun setUIMode(mode: UIMode) {
        dataStore.edit { prefs ->
            prefs[UI_MODE_KEY] = mode.name
        }
    }
}
