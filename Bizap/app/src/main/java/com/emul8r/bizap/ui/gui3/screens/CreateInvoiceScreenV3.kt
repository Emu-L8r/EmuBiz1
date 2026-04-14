package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber

/**
 * Create/Edit Invoice Screen V3 (Matrix Edition)
 *
 * Form to create or edit invoices with Matrix styling.
 * - Customer selector
 * - Date pickers
 * - Line items management
 * - Total calculation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreenV3(
    businessId: Long,
    invoiceId: Long? = null,
    navController: NavHostController
) {
    val isEditMode = invoiceId != null

    MatrixBackground(intensity = 1.0f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditMode) "BIZAP > EDIT INVOICE" else "BIZAP > NEW INVOICE",
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
                    .background(MatrixBlack)
                    .padding(paddingValues)
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MatrixCardPremium(title = if (isEditMode) ">> EDIT INVOICE" else ">> NEW INVOICE") {
                    Text(
                        "Invoice form implementation coming in next update",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixGreen.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}


