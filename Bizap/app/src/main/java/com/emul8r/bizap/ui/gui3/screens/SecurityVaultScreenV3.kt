package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.DocumentStatus
import com.emul8r.bizap.ui.documents.DocumentVaultUiState
import com.emul8r.bizap.ui.documents.DocumentVaultViewModel
import com.emul8r.bizap.ui.gui3.components.DetailRowMatrix
import com.emul8r.bizap.ui.gui3.components.GlowingMatrixButton
import com.emul8r.bizap.ui.gui3.components.MatrixBackgroundWrapper
import com.emul8r.bizap.ui.gui3.components.MatrixCardPremium
import com.emul8r.bizap.ui.gui3.components.MatrixFormError
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.theme.MatrixGreenBright
import com.emul8r.bizap.ui.gui3.theme.MatrixSurface
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SecurityVaultScreenV3(
    businessId: Long,
    navController: NavHostController,
    viewModel: DocumentVaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatrixBackgroundWrapper(screenType = ScreenType.UTILITY) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BIZAP > SECURITY VAULT",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MatrixGreenBright
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixSurface)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                MatrixCardPremium(title = ">> SECURITY OVERVIEW") {
                    when (val state = uiState) {
                        is DocumentVaultUiState.Loading -> {
                            Box(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(color = MatrixGreen)
                            }
                        }

                        is DocumentVaultUiState.Error -> {
                            MatrixFormError(
                                message = state.message,
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                            )
                        }

                        is DocumentVaultUiState.Success -> {
                            val allDocuments = state.documents.values.flatten().sortedByDescending { it.id }
                            val totalSizeKb = allDocuments.sumOf { it.fileSize } / 1024.0
                            val latestDoc = allDocuments.firstOrNull()
                            val uniqueCustomers = allDocuments.map { it.invoice.customerName }.distinct().count()
                            val topCustomer = allDocuments
                                .groupBy { it.invoice.customerName }
                                .maxByOrNull { it.value.size }
                                ?.key
                                ?: "None"
                            val statusCounts = DocumentStatus.values().associateWith { status ->
                                allDocuments.count { it.status == status }
                            }
                            val topFileTypes = allDocuments
                                .groupingBy { it.fileType.uppercase() }
                                .eachCount()
                                .entries
                                .sortedByDescending { it.value }
                                .take(3)
                                .joinToString(", ") { (type, count) -> "$type=$count" }
                                .ifBlank { "None" }

                            DetailRowMatrix(
                                label = "Business ID",
                                value = businessId.toString(),
                                isHighlight = true
                            )
                            DetailRowMatrix(
                                label = "Document Groups",
                                value = state.documents.size.toString(),
                                isHighlight = false
                            )
                            DetailRowMatrix(
                                label = "Generated PDFs",
                                value = allDocuments.size.toString(),
                                isHighlight = false
                            )
                            DetailRowMatrix(
                                label = "Archived PDFs",
                                value = statusCounts[DocumentStatus.ARCHIVED].toString(),
                                isHighlight = (statusCounts[DocumentStatus.ARCHIVED] ?: 0) > 0
                            )
                            DetailRowMatrix(
                                label = "Sent PDFs",
                                value = statusCounts[DocumentStatus.SENT].toString(),
                                isHighlight = (statusCounts[DocumentStatus.SENT] ?: 0) > 0
                            )
                            DetailRowMatrix(
                                label = "Paid PDFs",
                                value = statusCounts[DocumentStatus.PAID].toString(),
                                isHighlight = (statusCounts[DocumentStatus.PAID] ?: 0) > 0
                            )
                            DetailRowMatrix(
                                label = "Unique Customers",
                                value = uniqueCustomers.toString(),
                                isHighlight = uniqueCustomers > 0
                            )
                            DetailRowMatrix(
                                label = "Storage Used",
                                value = "%.2f KB".format(totalSizeKb),
                                isHighlight = false
                            )
                            DetailRowMatrix(
                                label = "Latest File",
                                value = latestDoc?.invoice?.invoiceNumber ?: "None",
                                isHighlight = latestDoc != null
                            )
                            DetailRowMatrix(
                                label = "Top Customer",
                                value = topCustomer,
                                isHighlight = topCustomer != "None"
                            )
                            DetailRowMatrix(
                                label = "Top File Types",
                                value = topFileTypes,
                                isHighlight = topFileTypes != "None"
                            )
                        }
                    }
                }

                MatrixCardPremium(title = ">> ARCHIVE BREAKDOWN") {
                    when (val state = uiState) {
                        is DocumentVaultUiState.Success -> {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                DocumentStatus.values().forEach { status ->
                                    DetailRowMatrix(
                                        label = status.name,
                                        value = statusCountsValue(state, status),
                                        isHighlight = statusCountsValue(state, status) != "0" && status != DocumentStatus.ARCHIVED
                                    )
                                }
                            }
                        }

                        is DocumentVaultUiState.Loading -> {
                            CircularProgressIndicator(color = MatrixGreen)
                        }

                        is DocumentVaultUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                MatrixCardPremium(title = ">> MONTH GROUPS") {
                    when (val state = uiState) {
                        is DocumentVaultUiState.Success -> {
                            val groups = state.documents
                            if (groups.isEmpty()) {
                                Text(
                                    "No archive groups yet.",
                                    color = MatrixGreen.copy(alpha = 0.75f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    groups.forEach { (month, documents) ->
                                        DetailRowMatrix(
                                            label = month.uppercase(),
                                            value = "${documents.size} PDFs",
                                            isHighlight = documents.isNotEmpty()
                                        )
                                    }
                                }
                            }
                        }

                        is DocumentVaultUiState.Loading -> {
                            CircularProgressIndicator(color = MatrixGreen)
                        }

                        is DocumentVaultUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                MatrixCardPremium(title = ">> RECENT GENERATED PDFs") {
                    when (val state = uiState) {
                        is DocumentVaultUiState.Success -> {
                            val recentDocuments = state.documents.values.flatten().sortedByDescending { it.id }.take(5)
                            if (recentDocuments.isEmpty()) {
                                Text(
                                    "No generated PDFs have been saved yet.",
                                    color = MatrixGreen.copy(alpha = 0.75f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    recentDocuments.forEach { item ->
                                        Card(
                                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MatrixSurface.copy(alpha = 0.7f)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MatrixGreen.copy(alpha = 0.35f)
                                            )
                                        ) {
                                            Column(
                                                modifier = androidx.compose.ui.Modifier.padding(Spacing.md),
                                                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                                            ) {
                                                Text(
                                                    text = item.invoice.displayName.ifBlank { item.invoice.invoiceNumber },
                                                    color = MatrixGreenBright,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "${item.fileType} • ${item.status.name} • ${formatFileSize(item.fileSize)}",
                                                    color = MatrixGreen.copy(alpha = 0.8f),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Text(
                                                    text = item.absolutePath,
                                                    color = MatrixGreen.copy(alpha = 0.55f),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        is DocumentVaultUiState.Loading -> {
                            CircularProgressIndicator(color = MatrixGreen)
                        }

                        is DocumentVaultUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    GlowingMatrixButton(
                        text = "OPEN PDF VAULT",
                        onClick = {
                            Timber.d("SecurityVaultScreenV3: opening document vault")
                            navController.navigate(com.emul8r.bizap.ui.gui3.navigation.ScreenV3.Vault(businessId))
                        },
                        modifier = androidx.compose.ui.Modifier.weight(1f),
                        isHighlight = true
                    )

                    GlowingMatrixButton(
                        text = "BACK TO DASHBOARD",
                        onClick = { navController.popBackStack() },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    )
                }

                MatrixCardPremium(title = ">> SECURITY NOTES") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            "This vault presents document counts, recent PDFs, and storage usage in a Matrix-style summary while keeping the actual document archive available behind the vault link.",
                            color = MatrixGreen.copy(alpha = 0.8f)
                        )
                        HorizontalDivider(color = MatrixGreen.copy(alpha = 0.25f))
                        Text(
                            "Use this screen as the security dashboard and open the PDF vault for the full searchable document list.",
                            color = MatrixGreen.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(Spacing.xxl))
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb >= 1024) "%.2f MB".format(kb / 1024.0) else "%.2f KB".format(kb)
}

private fun statusCountsValue(
    state: DocumentVaultUiState.Success,
    status: DocumentStatus
): String {
    val allDocuments = state.documents.values.flatten()
    return allDocuments.count { it.status == status }.toString()
}





