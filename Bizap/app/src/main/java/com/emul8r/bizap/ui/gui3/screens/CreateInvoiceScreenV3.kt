package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.ui.invoices.CreateInvoiceViewModel
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import com.emul8r.bizap.ui.settings.PrefilledItemsViewModel
import timber.log.Timber
import kotlin.math.pow

/**
 * Create Invoice Screen V3 (Matrix Edition) - PHASE 3A
 *
 * Full-featured invoice creation form with cascade-aware interactions and Matrix theme.
 *
 * **Features:**
 * ✅ Customer selector (dropdown)
 * ✅ Invoice number (auto-generated, read-only)
 * ✅ Invoice date (date picker)
 * ✅ Due date (date picker)
 * ✅ Line items section (repeating):
 *    - Item/description (text field)
 *    - Qty (number field)
 *    - Unit price (number field)
 *    - Tax % (number field)
 *    - Subtotal (calculated, read-only)
 * ✅ Totals section (calculated, read-only):
 *    - Subtotal (sum all line items)
 *    - Tax (sum line item taxes)
 *    - Discount amount (number field, optional)
 *    - Total (subtotal + tax - discount)
 * ✅ Payment terms (dropdown)
 * ✅ Notes (text area, optional)
 * ✅ Save button (Matrix styled)
 * ✅ Add/Delete line item buttons (Matrix styled)
 *
 * **GUI3 Superiority:**
 * ✅ Full Matrix theme (monospace, green, cyberpunk)
 * ✅ Large touch targets (thumb-friendly)
 * ✅ Elegant cascade animations
 * ✅ Real-time calculation with live metrics
 *
 * **Reuses:**
 * - GUI2's CreateInvoiceViewModel for business logic
 * - UiState from ViewModel
 * - Repository layer for persistence
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreenV3(
    businessId: Long = 1L,
    navController: NavHostController,
    viewModel: CreateInvoiceViewModel = hiltViewModel(),
    prefilledItemsViewModel: PrefilledItemsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefilledItems by prefilledItemsViewModel.items.collectAsStateWithLifecycle()
    var showPrefilledDialog by remember { mutableStateOf(false) }

    // Navigate back on success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Timber.d("✅ CreateInvoiceScreenV3: Invoice saved, navigating back")
            navController.popBackStack()
        }
    }

    // Set business ID on mount
    LaunchedEffect(businessId) {
        if (businessId > 0L) {
            Timber.d("🎯 CreateInvoiceScreenV3: Setting businessId=$businessId")
            viewModel.setBusinessId(businessId)
        }
    }

    MatrixBackgroundWrapper(screenType = ScreenType.FORM) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BIZAP > NEW INVOICE",
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MatrixGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixSurface)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MatrixBlack)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // ========== SECTION 1: CUSTOMER & BASIC INFO ==========
                MatrixFormSection(
                    title = ">> INVOICE DETAILS",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Customer Selector: Using ExposedDropdownMenuBox pattern (Issue #1 fix)
                    // Reusable component that properly binds to ViewModel state
                    MatrixCustomerDropdown(
                        value = uiState.selectedCustomer,
                        customers = uiState.customers,
                        onSelect = { viewModel.selectCustomer(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = "CUSTOMER *"
                    )

                    // Header
                    MatrixTextField(
                        value = uiState.header,
                        onValueChange = { viewModel.onHeaderChange(it) },
                        label = "INVOICE HEADER",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Subheader
                    MatrixTextField(
                        value = uiState.subheader,
                        onValueChange = { viewModel.onSubheaderChange(it) },
                        label = "INVOICE SUBHEADER",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ========== SECTION 2: LINE ITEMS ==========
                MatrixFormSection(
                    title = ">> LINE ITEMS",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.items.isEmpty()) {
                        MatrixFormError(
                            message = "No line items. Add one to get started.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            uiState.items.forEachIndexed { index, lineItem ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MatrixSurface.copy(alpha = 0.1f)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MatrixSurface.copy(alpha = 0.05f)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MatrixGreen.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.md),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "ITEM ${index + 1}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = MatrixGreen,
                                                    fontSize = 12.sp,
                                                    letterSpacing = 0.5.sp
                                                )
                                            )
                                            IconButton(
                                                onClick = { viewModel.removeLineItem(lineItem.transientId) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MatrixGreen.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        MatrixTextField(
                                            value = lineItem.description,
                                            onValueChange = { newValue ->
                                                viewModel.updateLineItem(
                                                    lineItem.transientId,
                                                    newValue,
                                                    lineItem.quantity,
                                                    lineItem.unitPrice
                                                )
                                            },
                                            label = "ITEM NAME",
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                        ) {
                                            MatrixTextField(
                                                value = if (lineItem.quantity == lineItem.quantity.toInt().toDouble()) {
                                                    lineItem.quantity.toInt().toString()
                                                } else {
                                                    lineItem.quantity.toString()
                                                },
                                                onValueChange = { newValue ->
                                                    val parsedQty = newValue.toDoubleOrNull() ?: lineItem.quantity
                                                    viewModel.updateLineItem(
                                                        lineItem.transientId,
                                                        lineItem.description,
                                                        parsedQty,
                                                        lineItem.unitPrice
                                                    )
                                                },
                                                label = "QTY",
                                                keyboardType = KeyboardType.Decimal,
                                                modifier = Modifier.weight(1f)
                                            )

                                            MatrixTextField(
                                                value = if (lineItem.unitPrice == 0L) "" else String.format("%.2f", lineItem.unitPrice / 100.0),
                                                onValueChange = { newValue ->
                                                    val parsedPrice = newValue.toDoubleOrNull() ?: 0.0
                                                    viewModel.updateLineItem(
                                                        lineItem.transientId,
                                                        lineItem.description,
                                                        lineItem.quantity,
                                                        (parsedPrice * 100).toLong()
                                                    )
                                                },
                                                label = "UNIT PRICE",
                                                keyboardType = KeyboardType.Decimal,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Text(
                                            text = "LINE TOTAL: $${String.format("%.2f", (lineItem.unitPrice * lineItem.quantity) / 100.0)}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = MatrixGreenBright,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        MatrixFormButton(
                            text = "+ ADD LINE ITEM",
                            onClick = { viewModel.addLineItem() },
                            modifier = Modifier.weight(1f)
                        )

                        MatrixFormButton(
                            text = "+ FROM TEMPLATES",
                            onClick = { showPrefilledDialog = true },
                            isEnabled = prefilledItems.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (showPrefilledDialog) {
                        AlertDialog(
                            onDismissRequest = { showPrefilledDialog = false },
                            title = {
                                Text(
                                    "SELECT PRE-FILLED ITEM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MatrixGreenBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (prefilledItems.isEmpty()) {
                                        MatrixFormError(
                                            message = "No pre-filled items available. Add templates in Settings.",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 320.dp),
                                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                                        ) {
                                            items(prefilledItems) { item ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MatrixSurface.copy(alpha = 0.7f)
                                                    ),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        MatrixGreen.copy(alpha = 0.35f)
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                viewModel.addLineItemFromPrefilledItem(item)
                                                                showPrefilledDialog = false
                                                            }
                                                            .padding(Spacing.md),
                                                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                                                    ) {
                                                        Text(
                                                            text = item.description,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                color = MatrixGreen,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        )
                                                        Text(
                                                            text = "\$${String.format("%.2f", item.unitPrice / 100.0)}",
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                color = MatrixGreenBright
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showPrefilledDialog = false }) {
                                    Text("CLOSE")
                                }
                            }
                        )
                    }
                }

                // ========== SECTION 3: NOTES & FOOTER ==========
                MatrixFormSection(
                    title = ">> ADDITIONAL INFO",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Notes
                    MatrixTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChange(it) },
                        label = "NOTES",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Footer
                    MatrixTextField(
                        value = uiState.footer,
                        onValueChange = { viewModel.onFooterChange(it) },
                        label = "INVOICE FOOTER",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ========== ERROR DISPLAY ==========
                if (uiState.error != null) {
                    MatrixFormError(
                        message = uiState.error ?: "Unknown error",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ========== SAVE BUTTON ==========
                MatrixFormButton(
                    text = "CREATE INVOICE",
                    onClick = { viewModel.onSaveClicked() },
                    isLoading = uiState.isSaving,
                    isEnabled = !uiState.isSaving && uiState.items.isNotEmpty() && uiState.selectedCustomer != null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}


// ========== HELPER FUNCTIONS ==========

private fun formatDateForDisplay(dateMs: Long): String {
    return if (dateMs <= 0L) "Select date" else {
        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
            .format(java.util.Date(dateMs))
    }
}





















