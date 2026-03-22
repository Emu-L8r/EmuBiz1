package com.emul8r.bizap.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.data.health.SnapshotHealthReport
import com.emul8r.bizap.data.health.SnapshotTypeHealth
import com.emul8r.bizap.ui.designsystem.BizapColors

/**
 * Warning banner displayed when snapshot health check detects issues.
 * Shows non-intrusive alert at top of screen with details and action buttons.
 */
@Composable
fun SnapshotHealthWarningBanner(
    healthReport: SnapshotHealthReport?,
    onDismiss: () -> Unit = {},
    onRunBackfill: () -> Unit = {}
) {
    if (healthReport == null || healthReport.isHealthy) {
        return
    }

    AnimatedVisibility(
        visible = !healthReport.isHealthy,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(BizapColors.AnalyticsWarning.copy(alpha = 0.15f)),
            color = BizapColors.AnalyticsWarning.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Warning icon
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = BizapColors.AnalyticsWarning,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(12.dp))

                // Message and details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "⚠️ Analytics Data Incomplete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = buildString {
                            healthReport.overallIssues.forEach { issue ->
                                append("• $issue\n")
                            }
                        }.trimEnd(),
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Show first recommendation
                    if (healthReport.recommendations.isNotEmpty()) {
                        Text(
                            text = "💡 ${healthReport.recommendations.first()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Expandable details card for snapshot health issues.
 * Shows full report with all recommendations.
 */
@Composable
fun SnapshotHealthDetailsCard(
    healthReport: SnapshotHealthReport,
    modifier: Modifier = Modifier,
    onRunBackfill: () -> Unit = {}
) {
    val isExpanded = remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded.value = !isExpanded.value },
        color = BizapColors.AnalyticsWarning.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Snapshot Health Report",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (isExpanded.value) "▼" else "▶",
                    fontSize = 14.sp
                )
            }

            // Expandable content
            AnimatedVisibility(visible = isExpanded.value) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Summary
                    SummarySection(healthReport)

                    Spacer(modifier = Modifier.size(8.dp))

                    // Issues
                    if (healthReport.overallIssues.isNotEmpty()) {
                        IssuesSection(healthReport.overallIssues)
                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    // Recommendations
                    if (healthReport.recommendations.isNotEmpty()) {
                        RecommendationsSection(healthReport.recommendations)
                        Spacer(modifier = Modifier.size(12.dp))
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRunBackfill,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Run Backfill")
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Button(
                            onClick = { isExpanded.value = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple inline warning for dashboard screens.
 * Minimal footprint, dismissible.
 */
@Composable
fun SnapshotHealthWarningInline(
    healthReport: SnapshotHealthReport?,
    modifier: Modifier = Modifier,
    onRunBackfill: () -> Unit = {}
) {
    if (healthReport == null || healthReport.isHealthy) {
        return
    }

    val isDismissed = remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = !isDismissed.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFFFEAEA),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ Analytics data incomplete",
                    fontSize = 13.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onRunBackfill,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(width = 100.dp, height = 36.dp),
                ) {
                    Text("Fix", fontSize = 12.sp)
                }

                IconButton(
                    onClick = { isDismissed.value = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog showing full health report details.
 * Used for comprehensive issue review.
 */
@Composable
fun SnapshotHealthDialog(
    healthReport: SnapshotHealthReport,
    onDismiss: () -> Unit,
    onRunBackfill: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Snapshot Health Report")
            }
        },
        text = {
            Column {
                Text(
                    text = buildString {
                        append("Status: ${if (healthReport.isHealthy) "✅ Healthy" else "⚠️ Unhealthy"}\n\n")

                        append("Issues:\n")
                        healthReport.overallIssues.forEach { issue ->
                            append("• $issue\n")
                        }

                        append("\nRecommendations:\n")
                        healthReport.recommendations.forEach { rec ->
                            append("• $rec\n")
                        }
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(onClick = onRunBackfill) {
                Text("Run Backfill")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

/**
 * Helper: Summary section
 */
@Composable
private fun SummarySection(healthReport: SnapshotHealthReport) {
    Column {
        Text(
            text = "Summary",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Text(
            text = buildString {
                append("Invoice Snapshots: ${describeHealth(healthReport.invoiceSnapshots)}\n")
                append("Payment Snapshots: ${describeHealth(healthReport.paymentSnapshots)}\n")
                append("Customer Snapshots: ${describeHealth(healthReport.customerSnapshots)}")
            },
            fontSize = 11.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Helper: Issues section
 */
@Composable
private fun IssuesSection(issues: List<String>) {
    Column {
        Text(
            text = "Issues (${issues.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )

        Text(
            text = issues.joinToString("\n") { "• $it" },
            fontSize = 11.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Helper: Recommendations section
 */
@Composable
private fun RecommendationsSection(recommendations: List<String>) {
    Column {
        Text(
            text = "Recommendations (${recommendations.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )

        Text(
            text = recommendations.joinToString("\n") { "• $it" },
            fontSize = 11.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Helper: Convert health status to readable text
 */
private fun describeHealth(health: SnapshotTypeHealth): String = when (health) {
    is SnapshotTypeHealth.Healthy -> "✅ ${health.totalRecords} snapshots (healthy)"
    is SnapshotTypeHealth.Unhealthy -> "⚠️ ${health.missingSnapshots} missing"
    is SnapshotTypeHealth.Error -> "❌ Error"
}

