package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Revenue Breakdown Item
 * Shows category breakdown with percentage and trend
 */
@Composable
internal fun RevenueBreakdownItemV3(
    category: String,
    amount: String,
    percentage: Double,
    trend: String,
    isTrendingUp: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                .padding(Spacing.md)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MatrixGreen.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTrendingUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Trend",
                            tint = if (isTrendingUp) MatrixGreen else MatrixError,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isTrendingUp) MatrixGreen else MatrixError,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top Customer V3
 * Shows rank, customer name, revenue, and percentage
 */
@Composable
fun TopCustomerV3(
    rank: String,
    customer: String,
    revenue: String,
    invoiceCount: String,
    percentOfTotal: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = rank,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MatrixGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )

            // Customer Details
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
                    text = "$invoiceCount invoices • $percentOfTotal of total",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MatrixGreen.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Revenue Amount
            Text(
                text = revenue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MatrixGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Monthly Revenue Item
 * Shows month, revenue, and comparison to previous month
 */
@Composable
fun MonthlyRevenueItemV3(
    month: String,
    revenue: String,
    previousMonth: String,
    isGrowth: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = month,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MatrixGreenBright,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "vs $previousMonth",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MatrixGreen.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGrowth) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = "Trend",
                    tint = if (isGrowth) MatrixGreen else MatrixError,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = revenue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MatrixGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Revenue Analytics Screen V3 (Matrix Edition)
 *
 * Displays revenue metrics and analytics with Matrix styling:
 * - Total revenue and growth rates
 * - Revenue breakdown by product/service
 * - Monthly revenue trends
 * - Top performing invoices
 *
 * Features:
 * - Key metric cards (Total, Average, Growth, Projection)
 * - Revenue breakdown by category
 * - Monthly revenue trends
 * - Top performing customers
 * - Revenue insights and predictions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueAnalyticsScreenV3(
    businessId: Long,
    navController: NavHostController
) {
    MatrixBackgroundWrapper(screenType = ScreenType.ANALYTICS) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> REVENUE ANALYTICS",
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
            // ============= REVENUE METRICS =============
            item {
                SectionCardMatrix(title = ">> REVENUE METRICS") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Total Revenue (YTD)
                        DetailRowMatrix(
                            label = "Total Revenue (YTD)",
                            value = "$487,534.80"
                        )

                        // Growth Rate (YoY)
                        DetailRowMatrix(
                            label = "Growth Rate (YoY)",
                            value = "18.4%"
                        )

                        // Revenue Projection (EOY)
                        DetailRowMatrix(
                            label = "Projected Revenue (EOY)",
                            value = "$892,450.00"
                        )

                        // Average Invoice Value
                        DetailRowMatrix(
                            label = "Average Invoice Value",
                            value = "$4,234.65"
                        )
                    }
                }
            }

            // ============= REVENUE BREAKDOWN =============
            item {
                MatrixCardPremium(title = ">> REVENUE BREAKDOWN", isPulsing = false) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Product Revenue
                        RevenueBreakdownItemV3(
                            category = "Product Sales",
                            amount = "$298,500.00",
                            percentage = 61.2,
                            trend = "+8.5%",
                            isTrendingUp = true
                        )

                        // Services Revenue
                        RevenueBreakdownItemV3(
                            category = "Service Revenue",
                            amount = "$156,800.00",
                            percentage = 32.1,
                            trend = "+15.2%",
                            isTrendingUp = true
                        )

                        // Consulting Revenue
                        RevenueBreakdownItemV3(
                            category = "Consulting Fees",
                            amount = "$32,234.80",
                            percentage = 6.6,
                            trend = "+2.1%",
                            isTrendingUp = true
                        )
                    }
                }
            }

            // ============= TOP CUSTOMERS =============
            item {
                SectionCardMatrix(title = ">> TOP REVENUE CUSTOMERS") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Top Customer 1
                        TopCustomerV3(
                            rank = "1.",
                            customer = "Enterprise Corp",
                            revenue = "$87,500.00",
                            invoiceCount = "24",
                            percentOfTotal = "17.9%"
                        )

                        // Top Customer 2
                        TopCustomerV3(
                            rank = "2.",
                            customer = "TechStart Solutions",
                            revenue = "$62,300.00",
                            invoiceCount = "18",
                            percentOfTotal = "12.8%"
                        )

                        // Top Customer 3
                        TopCustomerV3(
                            rank = "3.",
                            customer = "Global Industries",
                            revenue = "$45,200.00",
                            invoiceCount = "14",
                            percentOfTotal = "9.3%"
                        )

                        // Top Customer 4
                        TopCustomerV3(
                            rank = "4.",
                            customer = "Innovation Labs",
                            revenue = "$38,900.00",
                            invoiceCount = "11",
                            percentOfTotal = "8.0%"
                        )

                        // Top Customer 5
                        TopCustomerV3(
                            rank = "5.",
                            customer = "Digital Ventures",
                            revenue = "$31,450.00",
                            invoiceCount = "9",
                            percentOfTotal = "6.4%"
                        )
                    }
                }
            }

            // ============= MONTHLY TRENDS =============
            item {
                SectionCardMatrix(title = ">> MONTHLY REVENUE TREND") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Month trend items
                        MonthlyRevenueItemV3(
                            month = "April 2026",
                            revenue = "$45,234.50",
                            previousMonth = "$42,100.00",
                            isGrowth = true
                        )

                        MonthlyRevenueItemV3(
                            month = "March 2026",
                            revenue = "$42,100.00",
                            previousMonth = "$38,900.00",
                            isGrowth = true
                        )

                        MonthlyRevenueItemV3(
                            month = "February 2026",
                            revenue = "$38,900.00",
                            previousMonth = "$41,200.00",
                            isGrowth = false
                        )

                        MonthlyRevenueItemV3(
                            month = "January 2026",
                            revenue = "$41,200.00",
                            previousMonth = "$39,500.00",
                            isGrowth = true
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}

/**

 * Shows category breakdown with percentage and trend
 */
@Composable
fun RevenueBreakdownItemV3(
    category: String,
    amount: String,
    percentage: Double,
    trend: String,
    isTrendingUp: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                .padding(Spacing.md)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MatrixGreen.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTrendingUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Trend",
                            tint = if (isTrendingUp) MatrixGreen else MatrixError,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isTrendingUp) MatrixGreen else MatrixError,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top Customer V3
 * Shows rank, customer name, revenue, and percentage
 */
@Composable
fun TopCustomerV3(
    rank: String,
    customer: String,
    revenue: String,
    invoiceCount: String,
    percentOfTotal: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = rank,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MatrixGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )

            // Customer Details
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
                    text = "$invoiceCount invoices • $percentOfTotal of total",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MatrixGreen.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Revenue Amount
            Text(
                text = revenue,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MatrixGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Monthly Revenue Item
 * Shows month, revenue, and comparison to previous month
 */
@Composable
fun MonthlyRevenueItemV3(
    month: String,
    revenue: String,
    previousMonth: String,
    isGrowth: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = month,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MatrixGreenBright,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "vs $previousMonth",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MatrixGreen.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGrowth) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = "Trend",
                    tint = if (isGrowth) MatrixGreen else MatrixError,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = revenue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MatrixGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
             }
        }
    }
    }
}
