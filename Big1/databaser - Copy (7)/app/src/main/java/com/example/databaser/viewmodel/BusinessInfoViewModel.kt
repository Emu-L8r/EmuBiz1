package com.example.databaser.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.AppDatabase
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.BusinessInfoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusinessInfoViewModel(
    private val businessInfoRepository: BusinessInfoRepository,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val businessInfo: StateFlow<BusinessInfo?> = businessInfoRepository
        .getBusinessInfoStream()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun saveBusinessInfo(
        name: String, 
        address: String, 
        email: String, 
        logoPath: String?, 
        contactNumber: String, 
        abn: String,
        useDarkMode: Boolean, 
        defaultDueDateDays: Int, 
        dateFormat: String, 
        currencySymbol: String
    ) {
        viewModelScope.launch {
            val currentInfo = businessInfo.value
            val businessInfo = BusinessInfo(
                id = 1,
                name = name,
                address = address,
                email = email,
                logoPath = logoPath,
                contactNumber = contactNumber,
                abn = abn,
                generalNotes = currentInfo?.generalNotes,
                paymentDetails = currentInfo?.paymentDetails,
                dateFormat = dateFormat,
                defaultDueDateDays = defaultDueDateDays,
                currencySymbol = currencySymbol,
                useDarkMode = useDarkMode
            )
            businessInfoRepository.insertBusinessInfo(businessInfo)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            appDatabase.clearAllTables()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                val businessInfoRepository = application.container.businessInfoRepository
                val appDatabase = AppDatabase.getDatabase(application)
                BusinessInfoViewModel(
                    businessInfoRepository = businessInfoRepository,
                    appDatabase = appDatabase
                )
            }
        }
    }
}
