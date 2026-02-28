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

class ThemeViewModel(private val businessInfoRepository: BusinessInfoRepository) : ViewModel() {

    val theme: StateFlow<String> = businessInfoRepository.getBusinessInfoStream()
        .map { it?.theme ?: "Default" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Default"
        )

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                ThemeViewModel(application.container.businessInfoRepository)
            }
        }
    }
}