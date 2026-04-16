package com.emul8r.bizap.ui.gui3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.emul8r.bizap.ui.gui3.components.*
import com.emul8r.bizap.ui.gui3.theme.*
import com.emul8r.bizap.ui.gui3.util.ScreenType
import com.emul8r.bizap.ui.theme.Spacing

/**
 * Help Screen V3 (Matrix Edition)
 * FAQ, getting started guide, and support information with Matrix styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreenV3(
    businessId: Long,
    navController: NavHostController
) {
    MatrixBackgroundWrapper(screenType = ScreenType.UTILITY) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            ">> HELP & SUPPORT",
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MatrixGreen)
                        }
                    },
                    colors = matrixTopAppBarColors()
                )
            },
            containerColor = MatrixBlack.copy(alpha = 0.8f)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MatrixBlack)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Getting Started Section
                SectionCardMatrix(title = "GETTING STARTED") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        HelpItemMatrix(
                            title = "Creating Your First Invoice",
                            content = "Navigate to Invoices > Create Invoice, select a customer, add line items, and click Save."
                        )
                        HelpItemMatrix(
                            title = "Managing Customers",
                            content = "Go to Customers section to add new customers, update contact info, and track payment history."
                        )
                        HelpItemMatrix(
                            title = "Recording Payments",
                            content = "Use Payment Tracking to record customer payments against invoices automatically."
                        )
                    }
                }

                // FAQ Section
                SectionCardMatrix(title = "FREQUENTLY ASKED QUESTIONS") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        FAQItemMatrix(
                            question = "How do I export invoices as PDF?",
                            answer = "Open any invoice and use the Export button to download as PDF."
                        )
                        FAQItemMatrix(
                            question = "Can I customize invoice templates?",
                            answer = "Yes, go to Settings > Invoice Templates to customize headers, footers, and branding."
                        )
                        FAQItemMatrix(
                            question = "How are payments recorded?",
                            answer = "Navigate to Payment Tracking, click Record Payment, select invoice, and enter payment amount."
                        )
                        FAQItemMatrix(
                            question = "What payment methods are supported?",
                            answer = "Bizap tracks payment records for all methods: Cash, Check, Bank Transfer, Credit Card, etc."
                        )
                        FAQItemMatrix(
                            question = "How do I generate reports?",
                            answer = "Go to Reports section to view revenue, outstanding, and customer metrics reports."
                        )
                    }
                }

                // Contact Support Section
                SectionCardMatrix(title = "CONTACT SUPPORT") {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            "Email: support@bizap.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            "Phone: 1-800-BIZAP-1",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.8f)
                            )
                        )
                        Text(
                            "Web: www.bizap.com/support",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                // Version Info
                SectionCardMatrix(title = "ABOUT BIZAP") {
                    DetailRowMatrix(label = "App Version", value = "2.0.0")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Build", value = "April 2026")
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    DetailRowMatrix(label = "Matrix Edition", value = "Premium UI")
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
fun HelpItemMatrix(title: String, content: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MatrixGreenBright,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            content,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixGreen.copy(alpha = 0.7f),
                fontFamily = FontFamily.SansSerif
            )
        )
    }
}

@Composable
fun FAQItemMatrix(question: String, answer: String) {
    Column {
        Text(
            "Q: $question",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MatrixGreenBright,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            "A: $answer",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixGreen.copy(alpha = 0.8f),
                fontFamily = FontFamily.SansSerif
            )
        )
    }
}


