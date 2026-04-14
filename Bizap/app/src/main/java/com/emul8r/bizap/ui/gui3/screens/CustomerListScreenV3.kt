package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.ui.customers.CustomerListUiState
import com.emul8r.bizap.ui.customers.CustomerListViewModel
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

/**
 * Customer List Screen V3 (Matrix Edition)
 *
 * Displays all customers with Matrix styling:
 * - Green on dark aesthetic
 * - Bordered customer cards
 * - Search and filter capabilities
 * - Outstanding amount display
 * - Contact information
 *
 * Features:
 * - Scrollable customer list
 * - Search by name, email, or phone
 * - Outstanding balance display
 * - Quick stats (invoices, payments)
 * - Click to view customer details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreenV3(
    businessId: Long,
    navController: NavHostController,
    viewModel: CustomerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BIZAP > CUSTOMERS",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MatrixGreen
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MatrixGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MatrixSurface,
                    navigationIconContentColor = MatrixGreen,
                    titleContentColor = MatrixGreen,
                    actionIconContentColor = MatrixGreen
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Timber.d("GUI3: Navigate to create customer")
                    navController.navigate(ScreenV3.CreateCustomer(businessId))
                },
                containerColor = MatrixGreen,
                contentColor = MatrixBlack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        },
        containerColor = MatrixBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MatrixBlack)
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search customers...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixGreen.copy(alpha = 0.5f)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MatrixGreen.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatrixGreen,
                    unfocusedBorderColor = MatrixGreen.copy(alpha = 0.5f),
                    focusedTextColor = MatrixGreen,
                    cursorColor = MatrixGreen,
                    focusedContainerColor = MatrixSurface,
                    unfocusedContainerColor = MatrixSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Content
            when (val state = uiState) {
                is CustomerListUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MatrixBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MatrixGreen)
                    }
                }
                is CustomerListUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MatrixBlack)
                            .padding(Spacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                        ) {
                            Text(
                                "Error loading customers",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = MatrixError
                                )
                            )
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MatrixGreen.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
                is CustomerListUiState.Success -> {
                    CustomerListContentV3(
                        customers = state.customers,
                        searchQuery = searchQuery,
                        onCustomerClick = { customerId ->
                            navController.navigate(ScreenV3.CustomerDetail(businessId, customerId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerListContentV3(
    customers: List<Customer>,
    searchQuery: String,
    onCustomerClick: (Long) -> Unit
) {
    val filteredCustomers = customers.filter { customer ->
        customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.email?.contains(searchQuery, ignoreCase = true) == true ||
                customer.phone?.contains(searchQuery, ignoreCase = true) == true
    }

    if (filteredCustomers.isEmpty()) {
        EmptyCustomersStateV3()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MatrixBlack),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(filteredCustomers) { customer ->
                CustomerCardV3(
                    customer = customer,
                    onClick = { onCustomerClick(customer.id) }
                )
            }
        }
    }
}

@Composable
private fun CustomerCardV3(
    customer: Customer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header: Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.SansSerif,
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Text(
                        text = customer.email ?: "No email",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreen.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MatrixGreen.copy(alpha = 0.3f))
            )

            // Contact Info Row with Terminal Display
            TerminalDataDisplay(
                rows = listOf(
                    "PHONE" to (customer.phone ?: "No phone"),
                    "BUSINESS" to (customer.businessName ?: "N/A")
                )
            )
        }
    }
}

@Composable
private fun EmptyCustomersStateV3() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixBlack)
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = Spacing.lg),
                tint = MatrixGreen.copy(alpha = 0.5f)
            )

            Text(
                ">> NO CUSTOMERS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreen,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            Text(
                "Your customer list will appear here",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MatrixGreen.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            Text(
                "Tap the + button to add your first customer",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MatrixGreen.copy(alpha = 0.6f)
                )
            )
        }
    }
}
