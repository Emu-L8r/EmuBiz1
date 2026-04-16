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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Collection Efficiency Card
 * Shows a metric with trend indicator
 */
@Composable
internal fun CollectionEfficiencyCard(
    label: String,
    value: String,
    trend: String,
    isTrendingUp: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MatrixGreen.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MatrixGreen.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MatrixGreenBright,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isTrendingUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = "Trend",
                tint = if (isTrendingUp) MatrixGreen else MatrixError,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trend,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isTrendingUp) MatrixGreen else MatrixError,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

/**
 * Payment Timeline Item
 * Shows individual payment with date, customer, amount, and status
 */
@Composable
internal fun PaymentTimelineItemV3(
    date: String,
    customer: String,
    amount: String,
    invoiceId: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Date and Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MatrixGreen.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                )

                // Status Badge
                MatrixStatusBadge(
                    status = status,
                    style = when (status) {
                        "COMPLETED" -> MatrixStatusStyle.SUCCESS
                        "PENDING" -> MatrixStatusStyle.WARNING
                        "FAILED" -> MatrixStatusStyle.ERROR
                        else -> MatrixStatusStyle.NEUTRAL
                    }
                )
            }

            // Customer and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Invoice $invoiceId",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MatrixGreen.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                FormattedAmountMatrix(
                    amount = amount,
                    isPositive = true
                )
            }
        }
    }
}

/**
 * Payment Analytics Screen V3 (Matrix Edition)
 *
 * Displays payment metrics and analytics with Matrix styling:
 * - Payment trends and metrics
 * - Success rates and DSO (Days Sales Outstanding)
 * - Collection efficiency
 * - Recent payment timeline
 *
 * Features:
 * - Key metric cards (Total, Average, Success Rate, DSO)
 * - Collection efficiency metrics
 * - On-time payment rate
 * - Recent payments list with timeline
 * - Trend indicators (up/down)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAnalyticsScreenV3(
    businessId: Long,
    navController: NavHostController
) {
    MatrixBackgroundWrapper(screenType = ScreenType.ANALYTICS) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> PAYMENT ANALYTICS",
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
                    colors = matrixTopAppBarColors()
                )
            },
            containerColor = MatrixBlack.copy(alpha = 0.8f)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MatrixBlack)
                    .padding(paddingValues)
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
            // ============= HEADER STATS =============
            item {
                SectionCardMatrix(title = ">> PAYMENT METRICS") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Total Payments This Month
                        // TODO(GUI3): Inject PaymentAnalyticsRepository and observe real metrics
                        // Replace hardcoded "$45,234.50" with uiState.monthlyPaymentTotal
                        DetailRowMatrix(
                            label = "Total Payments (This Month)",
                            value = "$45,234.50"  // TODO: Wire real data
                        )

                        // Payment Success Rate
                        // TODO(GUI3): Wire real success rate from PaymentAnalyticsRepository
                        DetailRowMatrix(
                            label = "Success Rate",
                            value = "96.5%"  // TODO: Wire real rate
                        )

                        // Days Sales Outstanding
                        // TODO(GUI3): Calculate DSO from invoice data
                        DetailRowMatrix(
                            label = "Days Sales Outstanding (DSO)",
                            value = "12.3 days"  // TODO: Wire real DSO
                        )

                        // Average Payment Amount
                        // TODO(GUI3): Calculate average from payment records
                        DetailRowMatrix(
                            label = "Average Payment",
                            value = "$3,452.65"  // TODO: Wire real average
                        )
                    }
                }
            }

            // ============= COLLECTION EFFICIENCY =============
            item {
                MatrixCardPremium(title = ">> COLLECTION EFFICIENCY", isPulsing = false) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // On-time Rate
                        CollectionEfficiencyCard(
                            label = "On-Time Rate",
                            value = "94.2%",
                            trend = "+2.3%",
                            isTrendingUp = true
                        )

                        // Collection Efficiency
                        CollectionEfficiencyCard(
                            label = "Collection Efficiency",
                            value = "89.7%",
                            trend = "-1.2%",
                            isTrendingUp = false
                        )

                        // Recurring Revenue
                        CollectionEfficiencyCard(
                            label = "Recurring Revenue",
                            value = "65.8%",
                            trend = "+3.1%",
                            isTrendingUp = true
                        )

                        // Growth Rate (MoM)
                        CollectionEfficiencyCard(
                            label = "Growth Rate (MoM)",
                            value = "+12.4%",
                            trend = "+5.2%",
                            isTrendingUp = true
                        )
                    }
                }
            }

            // ============= RECENT PAYMENTS TIMELINE =============
            item {
                MatrixCardPremium(title = ">> RECENT PAYMENTS", isPulsing = false) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // TODO(GUI3): Inject PaymentAnalyticsRepository and observe real payments
                        // Payment Timeline Items - Currently hardcoded (TODO: wire real data)
                        PaymentTimelineItemV3(
                            date = "Apr 13, 2026",
                            customer = "Acme Corporation",
                            amount = "$5,234.50",
                            invoiceId = "INV-001234",
                            status = "COMPLETED"
                        )

                        PaymentTimelineItemV3(
                            date = "Apr 12, 2026",
                            customer = "TechStart Inc",
                            amount = "$3,100.00",
                            invoiceId = "INV-001233",
                            status = "COMPLETED"
                        )

                        PaymentTimelineItemV3(
                            date = "Apr 11, 2026",
                            customer = "Global Solutions",
                            amount = "$7,650.00",
                            invoiceId = "INV-001232",
                            status = "COMPLETED"
                        )

                        PaymentTimelineItemV3(
                            date = "Apr 10, 2026",
                            customer = "Enterprise Ltd",
                            amount = "$2,400.00",
                            invoiceId = "INV-001231",
                            status = "PENDING"
                        )

                        PaymentTimelineItemV3(
                            date = "Apr 09, 2026",
                            customer = "StartUp Hub",
                            amount = "$1,200.00",
                            invoiceId = "INV-001230",
                            status = "FAILED"
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }  // LazyColumn closes
    }  // Scaffold lambda closes
}  // MatrixBackground closes
}  // PaymentAnalyticsScreenV3 closes


