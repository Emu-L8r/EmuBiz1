package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.invoices.CreateInvoiceViewModel
import com.emul8r.bizap.ui.invoices.UnifiedCreateInvoicePage
import timber.log.Timber

/**
 * Create/Edit Invoice Screen V3 (Matrix Edition)
 *
 * Form to create or edit invoices with Matrix styling.
 * Reuses the same ViewModel and core logic as GUI2 but applies
 * cyberpunk Matrix theme styling for the premium experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreenV3(
    businessId: Long,
    invoiceId: Long? = null,
    navController: NavHostController,
    viewModel: CreateInvoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditMode = invoiceId != null

    Timber.d("🟢 CreateInvoiceScreenV3: Rendering - businessId=$businessId")

    // Set the businessId from navigation route so invoices save to correct business
    LaunchedEffect(businessId) {
        Timber.d("🎯 CreateInvoiceScreenV3: Setting businessId=$businessId")
        viewModel.setBusinessId(businessId)
    }

    // Handle save success with GUI3 navigation callback
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Timber.d("✅ CreateInvoiceScreenV3: Save successful - navigating back")
            navController.popBackStack()
        }
    }

    MatrixBackground(intensity = 1.2f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditMode) ">> EDIT INVOICE" else ">> CREATE INVOICE",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreenBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    // NO back button in Matrix GUI - use nav stack or system back
                    colors = matrixTopAppBarColors()
                )
            },
            containerColor = MatrixBlack.copy(alpha = 0.8f)
        ) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                // Delegate to unified page - reuses all functionality
                UnifiedCreateInvoicePage(
                    businessId = businessId,
                    onInvoiceSaved = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
        }
    }
}

