package com.emul8r.bizap.ui.gui2.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategorizedSmartQuickTasks(
    overdueCount: Int = 0,
    draftCount: Int = 0,
    totalInvoices: Int = 0,
    onCreateInvoice: () -> Unit = {},
    onViewOverdue: () -> Unit = {},
    onCompleteDrafts: () -> Unit = {},
    onSendReminder: () -> Unit = {},
    onViewReports: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with expand all option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Tasks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    expandedCategory = if (expandedCategory == "all") null else "all"
                }
            ) {
                Text(if (expandedCategory == "all") "Collapse All" else "Expand All")
            }
        }

        // Priority Alert Section (Always Visible)
        if (overdueCount > 0 || draftCount > 0) {
            PriorityTasksSection(
                overdueCount = overdueCount,
                draftCount = draftCount,
                onViewOverdue = onViewOverdue,
                onCompleteDrafts = onCompleteDrafts,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Invoice Category
        ExpandableTaskCategory(
            title = "Invoices",
            icon = Icons.Default.Receipt,
            isExpanded = expandedCategory == "all" || expandedCategory == "invoices",
            onToggle = {
                expandedCategory = if (expandedCategory == "invoices") null else "invoices"
            },
            tasks = listOf(
                QuickTaskItem(
                    icon = Icons.Default.Add,
                    title = "Create Invoice",
                    description = "Start a new invoice",
                    onClick = onCreateInvoice,
                    isPrimary = true
                ),
                QuickTaskItem(
                    icon = Icons.AutoMirrored.Filled.Send,
                    title = "Send Reminder",
                    description = "Notify customers",
                    onClick = onSendReminder,
                    isPrimary = false
                )
            )
        )

        // Payment Category
        ExpandableTaskCategory(
            title = "Payments",
            icon = Icons.Default.Payment,
            isExpanded = expandedCategory == "all" || expandedCategory == "payments",
            onToggle = {
                expandedCategory = if (expandedCategory == "payments") null else "payments"
            },
            tasks = listOf(
                QuickTaskItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Record Payment",
                    description = "Log incoming payment",
                    onClick = { /* Navigate to payment screen */ },
                    isPrimary = true
                ),
                QuickTaskItem(
                    icon = Icons.Default.Schedule,
                    title = "Track Overdue",
                    description = "$overdueCount overdue",
                    onClick = onViewOverdue,
                    isPrimary = false,
                    badge = if (overdueCount > 0) overdueCount.toString() else null
                )
            )
        )

        // Reports Category
        ExpandableTaskCategory(
            title = "Reports & Analytics",
            icon = Icons.Default.BarChart,
            isExpanded = expandedCategory == "all" || expandedCategory == "reports",
            onToggle = {
                expandedCategory = if (expandedCategory == "reports") null else "reports"
            },
            tasks = listOf(
                QuickTaskItem(
                    icon = Icons.Default.GetApp,
                    title = "Export Report",
                    description = "Download as PDF",
                    onClick = onViewReports,
                    isPrimary = true
                ),
                QuickTaskItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "View Analytics",
                    description = "Business insights",
                    onClick = onViewReports,
                    isPrimary = false
                )
            )
        )
    }
}

@Composable
private fun PriorityTasksSection(
    overdueCount: Int,
    draftCount: Int,
    onViewOverdue: () -> Unit,
    onCompleteDrafts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "⚠️ Action Required",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (overdueCount > 0) {
            PriorityTaskCard(
                title = "Overdue Invoices",
                count = overdueCount,
                color = MaterialTheme.colorScheme.error,
                icon = Icons.Default.ErrorOutline,
                onClick = onViewOverdue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (draftCount > 0) {
            PriorityTaskCard(
                title = "Incomplete Drafts",
                count = draftCount,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                icon = Icons.Default.Edit,
                onClick = onCompleteDrafts,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun PriorityTaskCard(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$count items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Badge(
                modifier = Modifier.align(Alignment.Top),
                containerColor = color,
                contentColor = Color.White
            ) {
                Text(count.toString(), modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
private fun ExpandableTaskCategory(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    tasks: List<QuickTaskItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isExpanded) 0f else 180f)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    TaskCardItem(task = task)
                }
            }
        }
    }
}

@Composable
private fun TaskCardItem(
    task: QuickTaskItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = task.onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isPrimary)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (task.isPrimary)
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = task.icon,
                    contentDescription = task.title,
                    modifier = Modifier.size(20.dp),
                    tint = if (task.isPrimary)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (task.isPrimary) FontWeight.Bold else FontWeight.SemiBold
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (task.badge != null) {
                Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(task.badge, modifier = Modifier.padding(4.dp))
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class QuickTaskItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
    val isPrimary: Boolean = false,
    val badge: String? = null
)
