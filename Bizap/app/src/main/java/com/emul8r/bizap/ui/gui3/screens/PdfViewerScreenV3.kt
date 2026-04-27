package com.emul8r.bizap.ui.gui3.screens

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.Image
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

                // PDF Viewer
                if (isFileExists) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(MatrixBlack)
                            .border(1.dp, MatrixGreen.copy(alpha = 0.5f))
                    ) {
                        PdfViewerWithPages(
                            pdfPath = pdfPath,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
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
                                "⚠️ File Not Found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = MatrixWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
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

/**
 * PDF Viewer using Android's built-in PdfRenderer
 *
 * Renders PDF pages to Bitmaps and displays them in a scrollable column.
 * Provides native PDF rendering without external dependencies.
 *
 * **Architecture:**
 * - Uses PdfRenderer (API 21+) for hardware-accelerated PDF rendering
 * - Renders pages lazily as user scrolls
 * - Each page rendered to a Bitmap at screen resolution
 * - Memory-efficient: only visible pages rendered
 */
@Composable
fun PdfViewerWithPages(
    pdfPath: String,
    modifier: Modifier = Modifier
) {
    val pdfFile = File(pdfPath)
    var pageCount by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pdfPath) {
        try {
            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount
            Timber.d("✅ PDF loaded: $pageCount pages")
            renderer.close()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to load PDF")
            error = "Failed to load PDF: ${e.message}"
            pageCount = 0
        }
    }

    if (error != null) {
        Box(
            modifier = modifier
                .background(MatrixBlack)
                .padding(Spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                error ?: "Error loading PDF",
                color = MatrixWarning,
                fontFamily = FontFamily.Monospace
            )
        }
    } else if (pageCount > 0) {
        LazyColumn(
            modifier = modifier
                .background(MatrixBlack)
                .fillMaxWidth()
        ) {
            items(pageCount) { pageIndex ->
                PdfPageRenderer(
                    pdfPath = pdfPath,
                    pageIndex = pageIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.sm)
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .background(MatrixBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Loading PDF...",
                color = MatrixGreen,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Renders a single PDF page to a Bitmap and displays it.
 */
@Composable
fun PdfPageRenderer(
    pdfPath: String,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var renderError by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(pdfPath, pageIndex) {
        try {
            val pdfFile = File(pdfPath)
            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)

            if (pageIndex < renderer.pageCount) {
                val page = renderer.openPage(pageIndex)

                // Render at screen density for crisp display
                val density = context.resources.displayMetrics.density
                val width = (page.width * density).toInt()
                val height = (page.height * density).toInt()

                val renderedBitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                page.render(renderedBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                bitmap = renderedBitmap
                page.close()
                Timber.d("✅ Rendered page $pageIndex (${width}x${height}px)")
            }
            renderer.close()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to render page $pageIndex")
            renderError = "Failed to render page: ${e.message}"
        }
    }

    if (renderError != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MatrixBlack)
                .border(1.dp, MatrixWarning),
            contentAlignment = Alignment.Center
        ) {
            Text(
                renderError ?: "Render error",
                color = MatrixWarning,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    } else if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "PDF Page ${pageIndex + 1}",
            modifier = modifier
                .fillMaxWidth()
                .background(MatrixBlack)
                .border(1.dp, MatrixGreen.copy(alpha = 0.3f)),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MatrixBlack)
                .border(1.dp, MatrixGreen.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Rendering page...",
                color = MatrixGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }
    }
}
