package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListUiStateV2
import com.emul8r.bizap.ui.gui2.invoices.InvoiceListViewModelV2
import com.emul8r.bizap.ui.theme.Spacing
import java.util.*

/**
 * Invoice List Screen V3 (Matrix Edition)
 *
 * Displays all invoices with Matrix styling:
 * - Green on dark aesthetic
 * - Bordered invoice cards
 * - Status badges with semantic colors
 * - Monospace financial amounts
 * - Smooth animations
 *
 * **Features:**
 * - Scrollable invoice list with pagination
 * - Invoice cards showing:
 *   - Customer name
 *   - Invoice date and total amount
 *   - Status badge (DRAFT, SENT, PAID, OVERDUE, CANCELLED)
 *   - Invoice number
 * - Quick action buttons to create/manage invoices
 * - Empty state with helpful message
 * - Error state with retry button
 * - Click to view invoice details
 *
 * @param businessId ID of active business
 * @param navController Navigation controller for routing
 * @param viewModel ViewModel managing list state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreenV3(
    businessId: Long,
    navController: NavHostController,
    viewModel: InvoiceListViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatrixBackgroundWrapper(screenType = ScreenType.LIST) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BIZAP > INVOICES",
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
                    containerColor = MatrixSurface,
                    navigationIconContentColor = MatrixGreen,
                    titleContentColor = MatrixGreen,
                    actionIconContentColor = MatrixGreen
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ScreenV3.CreateInvoice(businessId)) },
                containerColor = MatrixGreen,
                contentColor = MatrixBlack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Invoice")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        when (val state = uiState) {
            is InvoiceListUiStateV2.Loading -> {
                LoadingStateV3(modifier = Modifier.padding(paddingValues))
            }
            is InvoiceListUiStateV2.Error -> {
                ErrorStateV3(
                    message = state.message,
                    onRetry = { /* Retry functionality */ },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is InvoiceListUiStateV2.Success -> {
                InvoiceListContentV3(
                    invoices = state.invoices,
                    onInvoiceClick = { invoiceId ->
                        navController.navigate(ScreenV3.InvoiceDetail(businessId, invoiceId))
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
    }
}

/**
 * Invoice List Content V3
 *
 * Main content area displaying the list of invoices or empty state.
 */
@Composable
private fun InvoiceListContentV3(
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (invoices.isEmpty()) {
        EmptyStateV3(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent),  // was MatrixBlack — rain now shows through
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(invoices) { invoice ->
                InvoiceCardV3(
                    invoice = invoice,
                    onClick = { onInvoiceClick(invoice.id) }
                )
            }
        }
    }
}

/**
 * Invoice Card V3 (Matrix Edition) - PREMIUM WITH GLOWING BADGES
 *
 * Individual invoice card with Matrix styling:
 * - Bordered card with green lines
 * - Customer name and invoice number
 * - Amount in monospace font
 * - Glowing status badge with semantic color
 * - Clickable to view details
 */
@Composable
private fun InvoiceCardV3(
    invoice: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MatrixSurface.copy(alpha = 0.80f),  // was MatrixSurface (fully opaque) — rain peeks through
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MatrixGreen,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header Row: Customer Name and Status Badge (GLOWING)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.SansSerif,
                            color = MatrixGreenBright,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "Invoice #${invoice.invoiceNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreen.copy(alpha = 0.7f)
                        )
                    )
                }

                // Status Badge - NOW GLOWING!
                GlowingStatusBadge(
                    status = invoice.status.name,
                    style = when (invoice.status.name) {
                        "PAID" -> MatrixStatusStyle.SUCCESS
                        "OVERDUE" -> MatrixStatusStyle.ERROR
                        "SENT" -> MatrixStatusStyle.INFO
                        "DRAFT" -> MatrixStatusStyle.WARNING
                        else -> MatrixStatusStyle.NEUTRAL
                    }
                )
            }

            // Divider Line
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MatrixGreen.copy(alpha = 0.3f))
            )

            // Footer Row: Date and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatInvoiceDateV3(invoice.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MatrixGreen.copy(alpha = 0.8f)
                    )
                )

                FormattedAmountMatrix(
                    amount = formatAmountV3(invoice.totalAmount),
                    isPositive = true
                )
            }
        }
    }
}

/**
 * Loading State V3
 *
 * Skeleton loading state with Matrix theme.
 */
@Composable
private fun LoadingStateV3(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),  // was MatrixBlack
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(5) {
            SkeletonInvoiceCardV3()
        }
    }
}

/**
 * Skeleton Invoice Card V3
 *
 * Loading placeholder matching invoice card dimensions.
 */
@Composable
private fun SkeletonInvoiceCardV3(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = MatrixGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(0.6f)
                        .height(16.dp)
                        .background(
                            MatrixGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                Spacer(
                    modifier = Modifier
                        .weight(0.3f)
                        .height(16.dp)
                        .background(
                            MatrixGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .background(
                        MatrixGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MatrixGreen.copy(alpha = 0.1f))
            )

            // Footer Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(0.4f)
                        .height(14.dp)
                        .background(
                            MatrixGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Spacer(
                    modifier = Modifier
                        .weight(0.4f)
                        .height(14.dp)
                        .background(
                            MatrixGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

/**
 * Empty State V3
 *
 * Shown when no invoices are available.
 */
@Composable
private fun EmptyStateV3(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)  // was MatrixBlack — rain shows through
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            // Empty Icon
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = Spacing.lg),
                tint = MatrixGreen.copy(alpha = 0.5f)
            )

            // Title
            Text(
                ">> NO INVOICES YET",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixGreen,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // Message
            Text(
                "Your invoice history will appear here",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MatrixGreen.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            Text(
                "Tap the + button to create your first invoice",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MatrixGreen.copy(alpha = 0.6f)
                )
            )
        }
    }
}

/**
 * Error State V3
 *
 * Shown when there's an error loading invoices.
 */
@Composable
private fun ErrorStateV3(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)  // was MatrixBlack — rain shows through
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Error Icon
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = Spacing.lg),
                tint = MatrixError
            )

            // Error Title
            Text(
                ">> ERROR",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MatrixError,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // Error Message
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MatrixGreen.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            // Retry Button
            MatrixButton(
                text = "RETRY",
                onClick = onRetry,
                modifier = Modifier.width(150.dp),
                isHighlight = true
            )
        }
    }
}

/**
 * Helper function to format invoice date
 */
private fun formatInvoiceDateV3(timestamp: Long): String {
    val java8Date = java.time.Instant.ofEpochMilli(timestamp)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return java8Date.format(formatter)
}

/**
 * Helper function to format amount to currency string
 */
private fun formatAmountV3(cents: Long): String {
    val dollars = cents / 100
    val centsPart = cents % 100
    return String.format(Locale.US, "$%,d.%02d", dollars, centsPart)
}
