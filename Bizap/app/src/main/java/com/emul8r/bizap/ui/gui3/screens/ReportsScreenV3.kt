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
 * Reports Screen V3 (Matrix Edition)
 * Generate and view business reports with Matrix styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreenV3(
    businessId: Long,
    navController: NavHostController
) {
    MatrixBackground(intensity = 1.2f) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> BUSINESS REPORTS",
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
                // Period Summary
                SectionCardMatrix(title = "CURRENT PERIOD") {
                    DetailRowMatrix(label = "Period", value = "APR 2026")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Invoices Sent", value = "12")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Invoices Paid", value = "10")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Invoices Overdue", value = "1")
                }

                // Revenue Report
                SectionCardMatrix(title = "REVENUE SUMMARY") {
                    DetailRowMatrix(label = "Total Revenue", value = "$125,850.00", isHighlight = true)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Revenue (This Month)", value = "$12,500.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Average Invoice", value = "$2,150.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Collection Rate", value = "94.5%")
                }

                // Customer Report
                SectionCardMatrix(title = "CUSTOMER METRICS") {
                    DetailRowMatrix(label = "Total Customers", value = "18")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Active Customers", value = "15")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "New (This Month)", value = "2")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Avg. Invoice Value", value = "$4,250.00")
                }

                // Outstanding Report
                SectionCardMatrix(title = "OUTSTANDING ANALYSIS") {
                    DetailRowMatrix(label = "Total Outstanding", value = "$8,450.00", isHighlight = false)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Current (0-30 days)", value = "$5,200.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Overdue (30-60 days)", value = "$2,100.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Very Overdue (60+ days)", value = "$1,150.00")
                }

                // Generate Report Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    MatrixButton(
                        text = "EXPORT AS PDF",
                        onClick = { /* TODO: Export reports */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                    MatrixButton(
                        text = "VIEW DETAILED ANALYSIS",
                        onClick = { /* TODO: Show detailed view */ },
                        modifier = Modifier.fillMaxWidth(),
                        isHighlight = true
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}


