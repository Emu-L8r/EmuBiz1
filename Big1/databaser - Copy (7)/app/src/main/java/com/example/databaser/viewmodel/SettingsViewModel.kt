package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.BusinessInfoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val businessInfoRepository: BusinessInfoRepository) : ViewModel() {

    val useDarkMode: StateFlow<Boolean> = businessInfoRepository.getBusinessInfoStream()
        .map { it?.useDarkMode ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                SettingsViewModel(application.container.businessInfoRepository)
            }
        }
    }
}