package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.gui2.invoice.DialogState
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailViewModelV2
import com.emul8r.bizap.ui.gui2.invoice.InvoiceDetailUiStateV2
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

/**
 * Invoice Detail Screen V3 (Matrix Edition)
 *
 * Displays detailed invoice information with Matrix styling.
 * - Full invoice details (customer, amount, status)
 * - Line items list
 * - Payment history
 * - Action buttons (edit, delete, export)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreenV3(
    businessId: Long,
    invoiceId: Long,
    navController: NavHostController,
    viewModel: InvoiceDetailViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatrixBackgroundWrapper(screenType = ScreenType.DETAIL) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BIZAP > INVOICE DETAIL",
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MatrixSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                when (uiState) {
                    is InvoiceDetailUiStateV2.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Loading invoice...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MatrixGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                    is InvoiceDetailUiStateV2.Success -> {
                        val state = uiState as InvoiceDetailUiStateV2.Success
                        val invoice = (uiState as InvoiceDetailUiStateV2.Success).invoice
                        InvoiceDetailContentV3(
                            invoice = invoice,
                            navController = navController,
                            businessId = businessId,
                            onExportPdf = { viewModel.openPdfExport() }
                        )

                        when (val dialogState = state.dialogState) {
                            is DialogState.PdfExport.Loading -> {
                                AlertDialog(
                                    onDismissRequest = { /* Keep loading dialog open */ },
                                    confirmButton = {},
                                    title = { Text("📄 Generating PDF") },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                                        ) {
                                            CircularProgressIndicator(color = MatrixGreen)
                                            Text(
                                                "Creating your invoice PDF...",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MatrixGreen
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                            is DialogState.PdfExport.Success -> {
                                val file = dialogState.file
                                AlertDialog(
                                    onDismissRequest = { viewModel.closeDialog() },
                                    title = { Text("✅ PDF Generated") },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                            Text("Invoice PDF saved successfully.")
                                            Text(
                                                file.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MatrixGreen
                                                )
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            viewModel.closeDialog()
                                            navController.navigate(
                                                com.emul8r.bizap.ui.gui3.navigation.ScreenV3.ViewPdf(
                                                    businessId = businessId,
                                                    invoiceId = invoice.id,
                                                    pdfPath = file.absolutePath
                                                )
                                            )
                                        }) {
                                            Text("View PDF")
                                        }
                                    },
                                    dismissButton = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                            TextButton(onClick = {
                                                viewModel.closeDialog()
                                                navController.navigate(com.emul8r.bizap.ui.gui3.navigation.ScreenV3.Vault(businessId))
                                            }) {
                                                Text("View in Vault")
                                            }
                                            TextButton(onClick = { viewModel.closeDialog() }) {
                                                Text("Done")
                                            }
                                        }
                                    }
                                )
                            }
                            is DialogState.PdfExport.Error -> {
                                AlertDialog(
                                    onDismissRequest = { viewModel.closeDialog() },
                                    confirmButton = {
                                        Button(onClick = { viewModel.closeDialog() }) {
                                            Text("OK")
                                        }
                                    },
                                    title = { Text("❌ Export Failed") },
                                    text = { Text(dialogState.message) }
                                )
                            }
                            else -> Unit
                        }
                    }
                    is InvoiceDetailUiStateV2.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Error loading invoice",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MatrixError,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                    is InvoiceDetailUiStateV2.NotFound -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Invoice not found",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MatrixWarning,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}

@Composable
private fun InvoiceDetailContentV3(
    invoice: Invoice,
    navController: NavHostController,
    businessId: Long,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Invoice Header
        MatrixCardPremium(title = ">> INVOICE #${invoice.invoiceNumber}") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                DetailRowMatrix(
                    label = "Customer",
                    value = invoice.customerName,
                    isHighlight = true
                )
                DetailRowMatrix(
                    label = "Status",
                    value = invoice.status.name,
                    isHighlight = false
                )
            }
        }

        // Financial Details
        MatrixCardPremium(title = ">> FINANCIAL DETAILS") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                DetailRowMatrix(
                    label = "Total Amount",
                    value = "$%.2f".format(invoice.totalAmount / 100.0),
                    isHighlight = true
                )
                DetailRowMatrix(
                    label = "Amount Paid",
                    value = "$%.2f".format(invoice.amountPaid / 100.0),
                    isHighlight = false
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {

            GlowingMatrixButton(
                text = "DELETE",
                onClick = { showDeleteDialog.value = true },
                modifier = Modifier.weight(1f)
            )

            GlowingMatrixButton(
                text = "PDF",
                onClick = {
                    onExportPdf()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog.value) {
        MatrixDialog(
            title = "DELETE INVOICE?",
            message = "Are you sure you want to delete invoice #${invoice.invoiceNumber}? This action cannot be undone.",
            onDismiss = { showDeleteDialog.value = false },
            onConfirm = {
                Timber.d("Delete invoice: ${invoice.id}")
                showDeleteDialog.value = false
                navController.popBackStack()
                // TODO: Call repository to delete invoice
            },
            confirmButtonText = "DELETE",
            dismissButtonText = "CANCEL"
        )
    }
}




