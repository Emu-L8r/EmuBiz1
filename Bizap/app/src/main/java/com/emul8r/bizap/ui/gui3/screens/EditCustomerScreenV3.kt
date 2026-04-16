package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.customers.CustomerDetailViewModel
import com.emul8r.bizap.ui.customers.CustomerDetailUiState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber

/**
 * EditCustomerScreenV3 — Edit existing customer (Matrix Edition)
 */
@Composable
fun EditCustomerScreenV3(
    navController: NavController,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatrixBackground(intensity = 1.0f) {
        when (uiState) {
            is CustomerDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MatrixBlack.copy(alpha = 0.8f))
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        "Loading customer...",
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreen
                    )
                }
            }

            is CustomerDetailUiState.Success -> {
                val customer = (uiState as CustomerDetailUiState.Success).customer

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MatrixBlack.copy(alpha = 0.8f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "═══ EDIT CUSTOMER ═══",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = MatrixGreen,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display customer details (read-only for now)
                    MatrixCardPremium(title = ">> ${customer.name}") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRowMatrix("Name", customer.name, isHighlight = true)
                            DetailRowMatrix("Email", customer.email ?: "N/A")
                            DetailRowMatrix("Phone", customer.phone ?: "N/A")
                            DetailRowMatrix("Status", "ACTIVE")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowingMatrixButton(
                            text = "SAVE",
                            onClick = {
                                Timber.d("Save customer changes")
                                navController.popBackStack()
                            },
                            modifier = Modifier.weight(1f),
                            isHighlight = true
                        )

                        GlowingMatrixButton(
                            text = "CANCEL",
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            is CustomerDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MatrixBlack.copy(alpha = 0.8f))
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        "Error: ${(uiState as CustomerDetailUiState.Error).message}",
                        fontFamily = FontFamily.Monospace,
                        color = com.emul8r.bizap.ui.gui3.theme.MatrixError
                    )
                }
            }
        }
    }
}
