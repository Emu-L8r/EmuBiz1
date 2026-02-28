package com.example.databaser.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.AppDatabase
import com.example.databaser.data.BusinessInfo
import com.example.databaser.data.BusinessInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessInfoViewModel @Inject constructor(
    private val businessInfoRepository: BusinessInfoRepository,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val businessInfo: StateFlow<BusinessInfo?> = businessInfoRepository
        .getBusinessInfoStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
            appDatabase.clearAllDataExceptSettings()
        }
    }
}
