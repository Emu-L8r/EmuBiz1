package com.example.databaser.ui.customer

import androidx.lifecycle.ViewModel
import com.example.databaser.data.dao.CustomerDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerDao: CustomerDao
) : ViewModel() {

    val customers = customerDao.getAllCustomers()

}
