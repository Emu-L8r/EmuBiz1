package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui2.analytics.RiskAnalyticsUiStateV2
import com.emul8r.bizap.ui.gui2.analytics.RiskAnalyticsViewModelV2
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing

/**
 * GUI3 Risk Analytics Screen — Matrix cyberpunk aesthetic.
 *
 * Reuses [RiskAnalyticsViewModelV2] (Pattern 2C).
 * Displays risk tier breakdown with Matrix glowing styles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAnalyticsScreenV3(
    businessId: Long,
    navController: NavHostController,
    viewModel: RiskAnalyticsViewModelV2 = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatrixBackgroundWrapper(screenType = ScreenType.ANALYTICS) {
        Scaffold(
            containerColor = MatrixBlack,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> RISK DASHBOARD",
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
                        containerColor = MatrixBlack,
                        titleContentColor = MatrixGreenBright,
                        navigationIconContentColor = MatrixGreen
                    )
                )
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is RiskAnalyticsUiStateV2.Loading -> MatrixLoadingScreen(
                    message = "SCANNING RISK MATRIX..."
                )
                is RiskAnalyticsUiStateV2.Error -> MatrixErrorScreen(
                    error = state.message,
                    onRetry = {},
                    onDismiss = {}
                )
                is RiskAnalyticsUiStateV2.Success -> {
                    val metrics = state.metrics
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        item { Spacer(modifier = Modifier.height(Spacing.sm)) }

                        // Status header
                        item {
                            Text(
                                text = "[ INVOICE HEALTH OVERVIEW ]",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MatrixGreen.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            )
                        }

                        // Risk tier cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                RiskTileV3(
                                    label = "HIGH RISK",
                                    count = metrics.highRiskCount,
                                    subtitle = "60+ days overdue",
                                    color = MatrixError,
                                    modifier = Modifier.weight(1f)
                                )
                                RiskTileV3(
                                    label = "AT RISK",
                                    count = metrics.atRiskCount,
                                    subtitle = "30–59 days overdue",
                                    color = MatrixWarning,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            RiskTileV3(
                                label = "HEALTHY",
                                count = metrics.healthyCount,
                                subtitle = "Paid or not yet due",
                                color = MatrixGreen,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Divider
                        item {
                            MatrixDivider()
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "[ EXPOSURE METRICS ]",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MatrixGreen.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            )
                        }

                        // Exposure stats
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = MatrixGreen.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                color = MatrixSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.md),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    DetailRowMatrix(
                                        label = "TOTAL OVERDUE INVOICES",
                                        value = "${metrics.overdueCount}"
                                    )
                                    MatrixDivider()
                                    DetailRowMatrix(
                                        label = "TOTAL OUTSTANDING",
                                        value = formatCentsCurrency(metrics.totalOutstandingCents)
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
                    }
                }
            }
        }
    }
}

/**
 * Risk tier display tile with Matrix styling.
 */
@Composable
private fun RiskTileV3(
    label: String,
    count: Int,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(
            width = 1.dp,
            color = color.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp)
        ),
        color = MatrixSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MatrixGreen.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

/** Format cents as currency string for Matrix display */
private fun formatCentsCurrency(cents: Long): String {
    val dollars = cents / 100.0
    return "$${String.format("%.2f", dollars)}"
}


