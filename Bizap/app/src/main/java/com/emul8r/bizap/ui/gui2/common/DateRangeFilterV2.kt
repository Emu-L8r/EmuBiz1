package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Date range filter component for analytics screens.
 *
 * Provides quick selection buttons (Today, This Week, This Month, YTD)
 * and custom date range picker option.
 *
 * @param selectedRange Currently selected date range
 * @param onRangeSelected Callback when range is selected
 * @param modifier Optional modifier
 */
@Composable
fun DateRangeFilterV2(
    selectedRange: DateRangeV2,
    onRangeSelected: (DateRangeV2) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Date Range",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onRangeSelected(DateRangeV2.TODAY) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedRange == DateRangeV2.TODAY)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Today", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = { onRangeSelected(DateRangeV2.THIS_WEEK) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedRange == DateRangeV2.THIS_WEEK)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Week", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = { onRangeSelected(DateRangeV2.THIS_MONTH) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedRange == DateRangeV2.THIS_MONTH)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Month", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = { onRangeSelected(DateRangeV2.THIS_YEAR) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedRange == DateRangeV2.THIS_YEAR)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Year", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Date range data class for analytics filtering.
 *
 * Represents a range of dates for filtering metrics and reports.
 */
data class DateRangeV2(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String = ""
) {
    companion object {
        val TODAY: DateRangeV2 get() {
            val now = LocalDate.now()
            return DateRangeV2(now, now, "Today")
        }

        val THIS_WEEK: DateRangeV2 get() {
            val now = LocalDate.now()
            val dayOfWeek = now.dayOfWeek.value
            val startOfWeek = now.minusDays((dayOfWeek - 1).toLong())
            return DateRangeV2(startOfWeek, now, "This Week")
        }

        val THIS_MONTH: DateRangeV2 get() {
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1)
            return DateRangeV2(startOfMonth, now, "This Month")
        }

        val THIS_YEAR: DateRangeV2 get() {
            val now = LocalDate.now()
            val startOfYear = now.withDayOfYear(1)
            return DateRangeV2(startOfYear, now, "This Year")
        }
    }
}

