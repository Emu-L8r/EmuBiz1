package com.emul8r.bizap.ui.customers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel = hiltViewModel(),
    onCustomerClick: (Long) -> Unit
) {
    val customers by viewModel.uiState.collectAsStateWithLifecycle()
    CustomerList(customers = customers, onCustomerClick = onCustomerClick)
}
