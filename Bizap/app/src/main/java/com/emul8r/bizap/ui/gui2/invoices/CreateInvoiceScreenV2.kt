package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emul8r.bizap.ui.invoices.CreateInvoiceViewModel
import com.emul8r.bizap.ui.invoices.UnifiedCreateInvoicePage
import timber.log.Timber

/**
 * GUI2 Create Invoice Screen - Wrapper using UnifiedCreateInvoicePage
 *
 * **Purpose:**
 * Provides GUI2-specific wrapper around the unified invoice creation page.
 * Maintains backward compatibility with existing navigation and callbacks,
 * adds GUI2-style top bar with save button placement.
 *
 * **Features:**
 * - Reuses all functionality from UnifiedCreateInvoicePage
 * - GUI2 styling with modern top app bar
 * - Save button in top bar for tablet accessibility
 * - Responsive to phone and tablet form factors
 * - Business context routing via navigation route parameter
 * - Full invoice creation workflow
 *
 * **Delegates To:**
 * @see UnifiedCreateInvoicePage - Core invoice creation logic
 *
 * @param businessId Current business context (routed from navigation)
 * @param onCreate Callback when invoice successfully saved (navigate back)
 * @param onBack Callback when user presses back button
 * @param viewModel CreateInvoiceViewModel managing form state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreenV2(
    businessId: Long,
    onCreate: () -> Unit,
    onBack: () -> Unit,
    onCreateCustomer: (() -> Unit)? = null,
    viewModel: CreateInvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Timber.d("🔷 CreateInvoiceScreenV2: Rendering - businessId=$businessId")

    // 🔥 CRITICAL FIX: Set the businessId from navigation route so invoices save to correct business
    LaunchedEffect(businessId) {
        Timber.d("🎯 CreateInvoiceScreenV2: Setting businessId=$businessId")
        viewModel.setBusinessId(businessId)
    }

    // ✅ NOTE: Save success navigation is handled by UnifiedCreateInvoicePage.
    // We pass onCreate() as onInvoiceSaved callback to avoid double-pop navigation bug.
    // DO NOT add LaunchedEffect(uiState.saveSuccess) here - it causes double-callback!

    // Scaffold with GUI2-style top bar (save button in top bar)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // "+ Customer" shortcut button
                    onCreateCustomer?.let {
                        OutlinedButton(
                            onClick = it,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Customer", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    // GUI2 Save button in top bar (tablet-friendly)
                    Button(
                        onClick = {
                            Timber.d("💾 CreateInvoiceScreenV2: Save button clicked")
                            viewModel.onSaveClicked()
                        },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp).padding(end = 4.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Saving...")
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Delegate to unified page - reuses all functionality
            UnifiedCreateInvoicePage(
                businessId = businessId,
                onInvoiceSaved = onCreate,
                viewModel = viewModel
            )
        }
    }
}
