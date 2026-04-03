package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import timber.log.Timber

/**
 * WIN #20: PDF Export Result Dialog with Instant Viewer Option
 *
 * After user exports PDF, show this dialog with:
 * - Success confirmation
 * - File details (name, size)
 * - 🎯 "View PDF" button → Opens PDF immediately (NO navigation back-forth!)
 * - Share button → Share the PDF
 * - Done button → Close dialog
 *
 * BEFORE: Export PDF → Navigate back to invoice → Navigate to vault → Open PDF (4 steps!)
 * AFTER: Export PDF → Tap "View PDF" (1 step!) ✅
 *
 * Usage:
 * PdfExportResultDialog(
 *     fileName = "Invoice_INV-2026-001.pdf",
 *     fileSizeKb = 245,
 *     onViewPdf = { openPdfViewer() },
 *     onShare = { sharePdf() },
 *     onDismiss = { closeDialog() }
 * )
 */
@Composable
fun PdfExportResultDialog(
    fileName: String,
    fileSizeKb: Int,
    onViewPdf: () -> Unit,
    onShare: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Timber.d("PdfExportResultDialog: fileName=$fileName, size=${fileSizeKb}KB")

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "📄 PDF Generated Successfully",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // File details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "File Name",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    fileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Storage,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    "File Size",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "$fileSizeKb KB",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Help text
                Text(
                    "Your PDF is ready to view or share!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            // Primary action: View PDF
            Button(
                onClick = {
                    Timber.d("User clicked 'View PDF'")
                    onViewPdf()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RemoveRedEye, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("View PDF")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Secondary action: Share
                OutlinedButton(
                    onClick = {
                        Timber.d("User clicked 'Share PDF'")
                        onShare()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }

                // Tertiary action: Done
                TextButton(
                    onClick = {
                        Timber.d("User clicked 'Done'")
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done")
                }
            }
        }
    )
}

/**
 * Error dialog for PDF export failures
 */
@Composable
fun PdfExportErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "❌ PDF Export Failed",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}



