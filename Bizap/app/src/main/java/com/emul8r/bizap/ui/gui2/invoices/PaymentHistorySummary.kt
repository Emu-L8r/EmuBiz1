package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.data.local.entities.PaymentEntity
import com.emul8r.bizap.ui.gui2.common.formatCents
import java.text.SimpleDateFormat
import java.util.*

/**
 * Payment History Summary Card (WIN #7)
 *
 * Displays aggregate information about payments for an invoice:
 * - Total amount paid
 * - Number of payments
 * - Average payment amount
 * - Last payment date
 */
@Composable
fun PaymentHistorySummary(
    payments: List<PaymentEntity>,
    modifier: Modifier = Modifier
) {
    if (payments.isEmpty()) {
        return  // Nothing to display
    }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val totalPaid = payments.sumOf { it.amount }
    val averagePayment = if (payments.isNotEmpty()) totalPaid / payments.size else 0L
    val lastPayment = payments.maxByOrNull { it.paymentDate }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                "Payment Summary",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Summary stats in a grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Paid
                SummaryStatItem(
                    label = "Total Paid",
                    value = formatCents(totalPaid),
                    modifier = Modifier.weight(1f)
                )

                // Number of Payments
                SummaryStatItem(
                    label = "Payments",
                    value = payments.size.toString(),
                    modifier = Modifier.weight(1f)
                )

                // Average Payment
                SummaryStatItem(
                    label = "Avg Payment",
                    value = formatCents(averagePayment),
                    modifier = Modifier.weight(1f)
                )
            }

            // Last Payment Date
            if (lastPayment != null) {
                Divider(modifier = Modifier.fillMaxWidth())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Last payment:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        dateFormatter.format(Date(lastPayment.paymentDate)),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Individual summary statistic item
 */
@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

