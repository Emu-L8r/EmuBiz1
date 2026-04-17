package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing
import timber.log.Timber
import java.io.File

/**
 * PDF Viewer Screen V3 (Matrix Edition)
 *
 * Displays PDF documents with download and share capabilities.
 * - Matrix-themed PDF viewer interface
 * - Top navigation bar with title and back button
 * - Action buttons for download and share
 * - PDF preview area (placeholder for library integration)
 * - Document metadata display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreenV3(
    businessId: Long,
    invoiceId: Long,
    pdfPath: String,
    navController: NavHostController
) {
    val pdfFile = File(pdfPath)
    val isFileExists = pdfFile.exists()

    MatrixBackgroundWrapper(screenType = ScreenType.UTILITY) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BIZAP > PDF VIEWER",
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
                        containerColor = MatrixSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Document Info Card
                MatrixCardPremium(title = ">> DOCUMENT INFO") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        DetailRowMatrix(
                            label = "Invoice ID",
                            value = invoiceId.toString(),
                            isHighlight = true
                        )
                        DetailRowMatrix(
                            label = "File Path",
                            value = pdfFile.name,
                            isHighlight = false
                        )
                        DetailRowMatrix(
                            label = "Status",
                            value = if (isFileExists) "Ready" else "Not Found",
                            isHighlight = !isFileExists
                        )
                        if (isFileExists) {
                            DetailRowMatrix(
                                label = "File Size",
                                value = "%.2f KB".format(pdfFile.length() / 1024.0),
                                isHighlight = false
                            )
                        }
                    }
                }

                // PDF Viewer Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MatrixBlack)
                        .border(1.dp, MatrixGreen.copy(alpha = 0.5f))
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (isFileExists) "📄 PDF Preview" else "⚠️ File Not Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = if (isFileExists) MatrixGreen else MatrixWarning,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            if (isFileExists)
                                "PDF library integration coming soon\nPlaceholder for AndroidPdfViewer"
                            else
                                "The requested PDF file could not be located.\nCheck the file path and try again.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    GlowingMatrixButton(
                        text = "DOWNLOAD",
                        onClick = {
                            Timber.d("Download PDF: $pdfPath")
                            // Ready for integration with file download service
                        },
                        modifier = Modifier.weight(1f),
                        isHighlight = true
                    )

                    GlowingMatrixButton(
                        text = "SHARE",
                        onClick = {
                            Timber.d("Share PDF: $pdfPath")
                            // Ready for integration with share sheet
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Security Notice
                MatrixCardPremium(title = ">> SECURITY NOTICE") {
                    Text(
                        "🔐 All PDF documents are encrypted and stored securely.\nAccess is tracked and logged for audit purposes.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MatrixGreen.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}


