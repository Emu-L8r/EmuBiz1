package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.BusinessInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val businessInfoRepository: BusinessInfoRepository) : ViewModel() {

    val useDarkMode: StateFlow<Boolean> = businessInfoRepository.getBusinessInfoStream()
        .map { it?.useDarkMode ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val useCompactMode: StateFlow<Boolean> = businessInfoRepository.getBusinessInfoStream()
        .map { it?.useCompactMode ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val theme: StateFlow<String> = businessInfoRepository.getBusinessInfoStream()
        .map { it?.theme ?: "Default" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Default"
        )

    val dateFormat: StateFlow<String> = businessInfoRepository.getBusinessInfoStream()
        .map { it?.dateFormat ?: "dd/MM/yyyy" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "dd/MM/yyyy"
        )

    fun updateDarkMode(useDarkMode: Boolean) {
        viewModelScope.launch {
            val currentInfo = businessInfoRepository.getBusinessInfoStream().first()
            if (currentInfo != null) {
                businessInfoRepository.updateBusinessInfo(currentInfo.copy(useDarkMode = useDarkMode))
            } else {
                val newInfo = BusinessInfo(
                    id = 1,
                    name = "",
                    address = "",
                    contactNumber = "",
                    useDarkMode = useDarkMode
                )
                businessInfoRepository.insertBusinessInfo(newInfo)
            }
        }
    }

    fun updateCompactMode(useCompactMode: Boolean) {
        viewModelScope.launch {
            val currentInfo = businessInfoRepository.getBusinessInfoStream().first()
            if (currentInfo != null) {
                businessInfoRepository.updateBusinessInfo(currentInfo.copy(useCompactMode = useCompactMode))
            } else {
                businessInfoRepository.insertBusinessInfo(
                    BusinessInfo(
                        id = 1,
                        name = "",
                        address = "",
                        contactNumber = "",
                        useCompactMode = useCompactMode
                    )
                )
            }
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            val currentInfo = businessInfoRepository.getBusinessInfoStream().first()
            if (currentInfo != null) {
                businessInfoRepository.updateBusinessInfo(currentInfo.copy(theme = theme))
            } else {
                businessInfoRepository.insertBusinessInfo(
                    BusinessInfo(
                        id = 1,
                        name = "",
                        address = "",
                        contactNumber = "",
                        theme = theme
                    )
                )
            }
        }
    }

    fun updateDateFormat(dateFormat: String) {
        viewModelScope.launch {
            val currentInfo = businessInfoRepository.getBusinessInfoStream().first()
            if (currentInfo != null) {
                businessInfoRepository.updateBusinessInfo(currentInfo.copy(dateFormat = dateFormat))
            } else {
                businessInfoRepository.insertBusinessInfo(
                    BusinessInfo(
                        id = 1,
                        name = "",
                        address = "",
                        contactNumber = "",
                        dateFormat = dateFormat
                    )
                )
            }
        }
    }
}
