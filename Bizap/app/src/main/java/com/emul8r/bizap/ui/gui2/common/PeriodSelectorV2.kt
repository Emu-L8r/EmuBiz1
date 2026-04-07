package com.emul8r.bizap.ui.gui2.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.R

/**
 * Period selector for comparative analytics.
 *
 * Provides quick selection buttons for common comparison periods:
 * - Month-over-Month (MTD vs Previous Month)
 * - Year-over-Year (YTD vs Previous Year)
 * - Quarter-over-Quarter
 * - Custom date range
 */
@Composable
fun PeriodSelectorV2(
    onMonthOverMonth: () -> Unit,
    onYearOverYear: () -> Unit,
    onQuarterOverQuarter: () -> Unit = {},
    onCustom: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("mtd") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.label_select_comparison_period),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Month-over-Month Button
            OutlinedButton(
                onClick = {
                    selectedPeriod = "mtd"
                    onMonthOverMonth()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedPeriod == "mtd")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(stringResource(R.string.label_mtd_vs_last_month))
            }

            // Year-over-Year Button
            OutlinedButton(
                onClick = {
                    selectedPeriod = "ytd"
                    onYearOverYear()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedPeriod == "ytd")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(stringResource(R.string.label_ytd_vs_last_year))
            }

            // Quarter-over-Quarter Button
            OutlinedButton(
                onClick = {
                    selectedPeriod = "qtd"
                    onQuarterOverQuarter()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedPeriod == "qtd")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(stringResource(R.string.label_qtd_vs_last_quarter))
            }

            // Custom Period Button
            OutlinedButton(
                onClick = {
                    selectedPeriod = "custom"
                    onCustom()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedPeriod == "custom")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Text(stringResource(R.string.label_custom_period))
            }
        }
    }
}

