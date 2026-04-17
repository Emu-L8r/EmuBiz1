package com.emul8r.bizap.ui.gui3.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.emul8r.bizap.domain.model.DocumentStatus
import com.emul8r.bizap.ui.documents.DocumentVaultItem
import com.emul8r.bizap.ui.documents.DocumentVaultUiState
import com.emul8r.bizap.ui.documents.DocumentVaultViewModel
import com.emul8r.bizap.ui.gui3.components.DetailRowMatrix
import com.emul8r.bizap.ui.gui3.components.GlowingMatrixButton
import com.emul8r.bizap.ui.gui3.components.MatrixBackgroundWrapper
import com.emul8r.bizap.ui.gui3.components.MatrixCardPremium
import com.emul8r.bizap.ui.gui3.components.MatrixFormError
import com.emul8r.bizap.ui.gui3.components.MatrixTextField
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.theme.MatrixGreenBright
import com.emul8r.bizap.ui.gui3.theme.MatrixSurface
import com.emul8r.bizap.ui.gui3.theme.matrixTopAppBarColors
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber
import java.io.File

/**
 * Vault Screen V3 (Matrix Edition)
 *
 * Rebuilt as an info-rich archive screen so GUI3 vault no longer feels sparse:
 * - Security/usage summary at the top
 * - Search/filter input
 * - Status breakdown and storage stats
 * - Recent document list with open/share actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreenV3(
    businessId: Long,
    navController: NavHostController,
    viewModel: DocumentVaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchTerm by viewModel.searchTerm.collectAsStateWithLifecycle()
    val context = LocalContext.current

    MatrixBackgroundWrapper(screenType = ScreenType.LIST) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BIZAP > DOCUMENT VAULT",
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
                    actions = {
                        IconButton(onClick = { navController.navigate(ScreenV3.SecurityVault(businessId)) }) {
                            Icon(
                                imageVector = Icons.Filled.Storage,
                                contentDescription = "Security vault",
                                tint = MatrixGreen
                            )
                        }
                    },
                    colors = matrixTopAppBarColors()
                )
            },
            containerColor = Color.Transparent
         ) { paddingValues ->
             Column(
                 modifier = Modifier
                     .fillMaxSize()
                     .padding(paddingValues)
                     .padding(Spacing.lg)
                     .verticalScroll(rememberScrollState()),
                 verticalArrangement = Arrangement.spacedBy(Spacing.lg)
             ) {
                MatrixCardPremium(title = ">> VAULT OVERVIEW", isPulsing = true) {
                    when (val state = uiState) {
                        is DocumentVaultUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(color = MatrixGreen)
                            }
                        }

                        is DocumentVaultUiState.Error -> {
                            MatrixFormError(
                                message = state.message,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is DocumentVaultUiState.Success -> {
                            val allDocuments = state.documents.values.flatten().sortedByDescending { it.id }
                            val totalSizeKb = allDocuments.sumOf { it.fileSize } / 1024.0
                            val activeFilter = if (searchTerm.isBlank()) "ALL DOCUMENTS" else searchTerm.uppercase()

                            DetailRowMatrix(label = "Business ID", value = businessId.toString(), isHighlight = true)
                            DetailRowMatrix(label = "Document Groups", value = state.documents.size.toString(), isHighlight = false)
                            DetailRowMatrix(label = "Generated PDFs", value = allDocuments.size.toString(), isHighlight = false)
                            DetailRowMatrix(label = "Storage Used", value = formatStorage(totalSizeKb), isHighlight = false)
                            DetailRowMatrix(label = "Active Filter", value = activeFilter, isHighlight = searchTerm.isNotBlank())
                        }
                    }
                }

                MatrixCardPremium(title = ">> SEARCH & FILTER") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Text(
                            "Search by customer name, invoice number, or document metadata.",
                            color = MatrixGreen.copy(alpha = 0.75f)
                        )
                        MatrixTextField(
                            value = searchTerm,
                            onValueChange = viewModel::onSearchTermChange,
                            label = "DOCUMENT SEARCH",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                MatrixCardPremium(title = ">> QUICK OPERATIONS") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Text(
                            "Jump to the broader Matrix security view or back to the dashboard.",
                            color = MatrixGreen.copy(alpha = 0.75f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
                            GlowingMatrixButton(
                                text = "SECURITY VIEW",
                                onClick = { navController.navigate(ScreenV3.SecurityVault(businessId)) },
                                modifier = Modifier.weight(1f),
                                isHighlight = false
                            )
                            GlowingMatrixButton(
                                text = "BACK",
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.weight(1f),
                                isHighlight = false
                            )
                        }
                    }
                }

                when (val state = uiState) {
                    is DocumentVaultUiState.Success -> {
                        val allDocuments = state.documents.values.flatten().sortedByDescending { it.id }
                        val statusCounts = DocumentStatus.values().associateWith { status ->
                            allDocuments.count { it.status == status }
                        }

                        MatrixCardPremium(title = ">> STATUS BREAKDOWN") {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                DocumentStatus.values().forEach { status ->
                                    DetailRowMatrix(
                                        label = status.name,
                                        value = statusCounts[status].toString(),
                                        isHighlight = status != DocumentStatus.ARCHIVED && (statusCounts[status] ?: 0) > 0
                                    )
                                }
                            }
                        }

                        if (allDocuments.isEmpty()) {
                            MatrixCardPremium(title = ">> EMPTY ARCHIVE") {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                    Text(
                                        "No documents are stored yet. Generate an invoice PDF to populate this vault.",
                                        color = MatrixGreen.copy(alpha = 0.8f)
                                    )
                                    GlowingMatrixButton(
                                        text = "OPEN SECURITY VIEW",
                                        onClick = { navController.navigate(ScreenV3.SecurityVault(businessId)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        isHighlight = true
                                    )
                                }
                            }
                        } else {
                            state.documents.forEach { (month, documents) ->
                                MatrixCardPremium(title = ">> ${month.uppercase()}") {
                                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                        documents.forEach { item ->
                                            VaultDocumentRow(
                                                item = item,
                                                context = context,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is DocumentVaultUiState.Loading -> {
                        MatrixCardPremium(title = ">> ARCHIVE LOADING") {
                            CircularProgressIndicator(color = MatrixGreen)
                        }
                    }

                    is DocumentVaultUiState.Error -> {
                        MatrixCardPremium(title = ">> ARCHIVE ERROR") {
                            MatrixFormError(message = state.message, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }
    }
}

@Composable
private fun VaultDocumentRow(
    item: DocumentVaultItem,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val file = File(item.absolutePath)
    Card(
        onClick = {
            try {
                if (item.absolutePath.isBlank()) {
                    Timber.e("❌ Document #${item.id} has blank file path")
                    return@Card
                }

                if (!file.exists()) {
                    Timber.e("❌ Document #${item.id} file not found: ${item.absolutePath}")
                    return@Card
                }

                val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e, "❌ Error opening document #${item.id}")
            }
        },
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MatrixSurface.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MatrixGreenBright
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = item.invoice.displayName.ifBlank { item.invoice.invoiceNumber },
                    color = MatrixGreenBright,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Customer: ${item.invoice.customerName}",
                    color = MatrixGreen.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "${item.fileType} • ${item.status.name} • ${formatFileSize(item.fileSize)}",
                    color = MatrixGreen.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = item.absolutePath,
                    color = MatrixGreen.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            IconButton(
                onClick = {
                    try {
                        if (!file.exists()) return@IconButton
                        val uri = FileProvider.getUriForFile(context, "com.emul8r.bizap.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share PDF"))
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error sharing document #${item.id}")
                    }
                },
                enabled = file.exists()
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = MatrixGreen
                )
            }
        }
    }
}

private fun formatStorage(bytesKb: Double): String {
    return if (bytesKb >= 1024.0) "%.2f MB".format(bytesKb / 1024.0) else "%.2f KB".format(bytesKb)
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb >= 1024) "%.2f MB".format(kb / 1024.0) else "%.2f KB".format(kb)
}

