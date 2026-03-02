package com.emul8r.bizap.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.DashboardSettings
import com.emul8r.bizap.domain.repository.DashboardSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardSettingsViewModel @Inject constructor(
    private val repository: DashboardSettingsRepository
) : ViewModel() {

    val settings: StateFlow<DashboardSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardSettings()
        )

    fun updateSettings(newSettings: DashboardSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.updateSettings(DashboardSettings())
        }
    }
}
