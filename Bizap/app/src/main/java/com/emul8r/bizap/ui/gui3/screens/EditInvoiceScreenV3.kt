package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailViewModelV2
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailUiStateV2
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber

/**
 * EditInvoiceScreenV3 — Edit existing invoice (Matrix Edition)
 */
@Composable
fun EditInvoiceScreenV3(
    navController: NavController,
    viewModel: InvoiceDetailViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is InvoiceDetailUiStateV2.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "Loading invoice...",
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreen
                )
            }
        }

        is InvoiceDetailUiStateV2.Success -> {
            val invoice = (uiState as InvoiceDetailUiStateV2.Success).invoice

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "═══ EDIT INVOICE ═══",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    color = MatrixGreen,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Display invoice details (read-only for now)
                MatrixCardPremium(title = ">> INVOICE #${invoice.invoiceNumber}") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRowMatrix("Customer", invoice.customerName, isHighlight = true)
                        DetailRowMatrix("Amount", "$%.2f".format(invoice.totalAmount / 100.0))
                        DetailRowMatrix("Status", invoice.status.name)
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
                            Timber.d("Save invoice changes")
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

        is InvoiceDetailUiStateV2.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "Error loading invoice",
                    fontFamily = FontFamily.Monospace,
                    color = com.emul8r.bizap.ui.gui3.theme.MatrixError
                )
            }
        }

        is InvoiceDetailUiStateV2.NotFound -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "Invoice not found",
                    fontFamily = FontFamily.Monospace,
                    color = com.emul8r.bizap.ui.gui3.theme.MatrixWarning
                )
            }
        }
    }
}

