package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.customers.CustomerListUiState
import com.emul8r.bizap.ui.customers.CustomerListViewModel
import com.emul8r.bizap.ui.gui2.common.ErrorStateV2
import com.emul8r.bizap.ui.gui2.common.LoadingIndicatorV2
import com.emul8r.bizap.ui.gui2.customers.components.CompactCustomerList
import com.emul8r.bizap.ui.gui2.customers.components.ModernCustomerList
import com.emul8r.bizap.ui.theme.UIMode

/**
 * GUI2 Customer List Screen with dual-mode rendering support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreenV2(
    businessId: Long,
    onCustomerClick: (Long) -> Unit,
    onCreateCustomer: () -> Unit,
    onBack: () -> Unit,
    uiMode: UIMode = UIMode.MODERN,
    viewModel: CustomerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCustomer) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CustomerListUiState.Loading -> LoadingIndicatorV2(modifier = Modifier.padding(paddingValues))
            is CustomerListUiState.Error -> ErrorStateV2(
                message = state.message,
                modifier = Modifier.padding(paddingValues)
            )
            is CustomerListUiState.Success -> {
                if (uiMode == UIMode.COMPACT) {
                    CompactCustomerList(
                        customers = state.customers,
                        onCustomerClick = onCustomerClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    ModernCustomerList(
                        customers = state.customers,
                        onCustomerClick = onCustomerClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
