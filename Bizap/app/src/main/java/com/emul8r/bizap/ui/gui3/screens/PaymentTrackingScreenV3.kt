package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Payment Tracking Screen V3 (Matrix Edition)
 * Track and manage invoice payments with Matrix styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTrackingScreenV3(
    businessId: Long,
    navController: NavHostController
) {
    MatrixBackgroundWrapper(screenType = ScreenType.LIST) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> PAYMENT TRACKING",
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Record payment */ },
                    containerColor = MatrixBlack.copy(alpha = 0.9f),
                    contentColor = MatrixGreenBright,
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = MatrixGreen,
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Record Payment", tint = MatrixGreenBright)
                }
            },
            containerColor = Color.Transparent
         ) { paddingValues ->
             Column(
                 modifier = Modifier
                     .fillMaxSize()
                     .verticalScroll(rememberScrollState())
                     .padding(paddingValues)
                     .padding(Spacing.lg),
                 verticalArrangement = Arrangement.spacedBy(Spacing.lg)
             ) {
                // Payment Summary
                SectionCardMatrix(title = "PAYMENT SUMMARY") {
                    DetailRowMatrix(label = "Total Received", value = "$45,250.00", isHighlight = true)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "This Month", value = "$8,500.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Pending", value = "$3,200.00")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Overdue", value = "$1,850.00")
                }

                // Recent Payments
                SectionCardMatrix(title = "RECENT PAYMENTS") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        PaymentItemMatrix(
                            date = "2026-04-12",
                            customer = "Acme Corp",
                            amount = "$2,500.00",
                            status = "COMPLETED"
                        )
                        PaymentItemMatrix(
                            date = "2026-04-10",
                            customer = "Tech Solutions",
                            amount = "$1,200.00",
                            status = "COMPLETED"
                        )
                        PaymentItemMatrix(
                            date = "2026-04-08",
                            customer = "Global Industries",
                            amount = "$3,100.00",
                            status = "COMPLETED"
                        )
                        PaymentItemMatrix(
                            date = "2026-04-07",
                            customer = "Enterprise Ltd",
                            amount = "$950.00",
                            status = "PENDING"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
fun PaymentItemMatrix(date: String, customer: String, amount: String, status: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MatrixGreen, shape = RoundedCornerShape(4.dp)),
        color = MatrixSurface,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreen.copy(alpha = 0.7f)
                    )
                )
                Text(
                    customer,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        color = MatrixGreenBright,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                FormattedAmountMatrix(
                    amount = amount,
                    isHighlight = true
                )
                MatrixStatusBadge(
                    status = status,
                    modifier = Modifier.padding(top = Spacing.xs),
                    style = if (status == "COMPLETED")
                        MatrixStatusStyle.SUCCESS else MatrixStatusStyle.INFO
                )
            }
        }
    }
}




