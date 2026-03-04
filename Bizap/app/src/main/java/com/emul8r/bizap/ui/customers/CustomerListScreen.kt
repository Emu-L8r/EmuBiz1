package com.emul8r.bizap.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.BuildConfig

@Composable
fun CustomerListScreen(
    onCustomerClick: (Long) -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.uiState.collectAsStateWithLifecycle()

    // MainActivity's Scaffold provides the TopAppBar, so just show content
    CustomerList(customers = customers, onCustomerClick = onCustomerClick)
}
