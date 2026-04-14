package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Customer Detail Screen V3 (Matrix Edition)
 * Shows customer profile and contact information with Matrix styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreenV3(
    businessId: Long,
    customerId: Long,
    navController: NavHostController
) {
    MatrixBackground(intensity = 1.2f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> CUSTOMER PROFILE",
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
                    colors = matrixTopAppBarColors()
                )
            },
            containerColor = MatrixBlack.copy(alpha = 0.8f)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MatrixBlack)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Customer Contact Section
                SectionCardMatrix(title = "CONTACT INFORMATION") {
                    DetailRowMatrix(label = "Customer ID", value = "#$customerId")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Status", value = "ACTIVE")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Email", value = "contact@customer.com")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Phone", value = "+1-555-0123")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Address", value = "123 Business Ave, City, ST")
                }

                // Financial Summary Section
                SectionCardMatrix(title = "FINANCIAL SUMMARY") {
                    DetailRowMatrix(label = "Total Invoiced", value = "$12,500.00", isHighlight = true)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Paid", value = "$10,200.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Outstanding", value = "$2,300.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Overdue", value = "$0.00")
                }

                // Activity Section
                SectionCardMatrix(title = "RECENT ACTIVITY") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            "Last Invoice: 2026-04-10",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            "Last Payment: 2026-04-05",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            "Member Since: 2025-01-15",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    MatrixButton(
                        text = "EDIT",
                        onClick = { /* TODO: Navigate to edit */ },
                        modifier = Modifier.weight(1f),
                        isHighlight = false
                    )
                    MatrixButton(
                        text = "VIEW INVOICES",
                        onClick = { /* TODO: Filter invoices */ },
                        modifier = Modifier.weight(1f),
                        isHighlight = true
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}



