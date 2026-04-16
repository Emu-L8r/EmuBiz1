package com.emul8r.bizap.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Color as AndroidColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.emul8r.bizap.MainActivity
import com.emul8r.bizap.ui.gui3.navigation.ScreenV3
import com.emul8r.bizap.ui.gui3.screens.DashboardScreenV3
import com.emul8r.bizap.ui.gui3.screens.InvoiceListScreenV3
import com.emul8r.bizap.ui.gui3.screens.InvoiceDetailScreenV3
import com.emul8r.bizap.ui.gui3.screens.CreateInvoiceScreenV3
import com.emul8r.bizap.ui.gui3.screens.EditInvoiceScreenV3
import com.emul8r.bizap.ui.gui3.screens.SettingsScreenV3
import com.emul8r.bizap.ui.gui3.screens.CustomerListScreenV3
import com.emul8r.bizap.ui.gui3.screens.CustomerDetailScreenV3
import com.emul8r.bizap.ui.gui3.screens.CreateCustomerScreenV3
import com.emul8r.bizap.ui.gui3.screens.EditCustomerScreenV3
import com.emul8r.bizap.ui.gui3.screens.PaymentTrackingScreenV3
import com.emul8r.bizap.ui.gui3.screens.RevenueAnalyticsScreenV3
import com.emul8r.bizap.ui.gui3.screens.PaymentAnalyticsScreenV3
import com.emul8r.bizap.ui.gui3.screens.ReportsScreenV3
import com.emul8r.bizap.ui.gui3.screens.HelpScreenV3
import com.emul8r.bizap.ui.gui3.screens.PdfViewerScreenV3
import com.emul8r.bizap.ui.gui3.screens.SecurityVaultScreenV3
import com.emul8r.bizap.ui.gui3.screens.VaultScreenV3
import com.emul8r.bizap.ui.gui3.theme.MatrixTheme
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.landing.LandingViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.emul8r.bizap.ui.settings.PrefilledItemsScreen
import timber.log.Timber

/**
 * Matrix GUI Main Activity (GUI3)
 *
 * Entry point for the Matrix/Cyberpunk invoicing interface.
 * - Matrix theme (green on dark)
 * - Type-safe navigation (Compose Navigation)
 * - Dark mode optimized
 * - Cyberpunk aesthetic
 */
@AndroidEntryPoint
class MatrixGUIMainActivity : AppCompatActivity() {

    private var navController: androidx.navigation.NavHostController? = null

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          println("🔵 STARTUP: MatrixGUIMainActivity.onCreate() called")

          // Disable action bar completely
          supportActionBar?.hide()

        // Enable edge-to-edge rendering (extends behind system bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set system bar colors to pure black (Matrix theme) - CRITICAL FIX
        @Suppress("DEPRECATION")
        window.statusBarColor = AndroidColor.BLACK
        @Suppress("DEPRECATION")
        window.navigationBarColor = AndroidColor.BLACK

        // For API 29+: Set system bar contrasting for light icons on dark background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false  // Allow custom colors
        }

         // Use WindowInsetsControllerCompat for broader compatibility
         val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
         windowInsetsController?.isAppearanceLightStatusBars = false  // Dark icons for dark background
         windowInsetsController?.isAppearanceLightNavigationBars = false

        val businessId = intent.getLongExtra("businessId", 1L)

        setContent {
            println("🟡 STARTUP: setContent block executing")
            val landingViewModel: LandingViewModel = hiltViewModel()

            MatrixTheme(isDarkMode = true) { // Matrix is always dark mode
                println("🟢 STARTUP: MatrixTheme content block")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black)
                ) {
                    println("🟠 STARTUP: Box rendering")
                    MatrixGUINavigation(
                        businessId = businessId,
                        onSwitchToGui1 = {
                            Timber.d("Switching to GUI1 from GUI3")
                            landingViewModel.resetMode()
                            // Return to MainActivity which will show Landing screen
                            startActivity(
                                Intent(this@MatrixGUIMainActivity, MainActivity::class.java)
                                    .putExtra("businessId", businessId)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        },
                        onSwitchToGui2 = {
                            Timber.d("Switching to GUI2 from GUI3")
                            landingViewModel.resetMode()
                            // Return to MainActivity which will show Landing screen
                            startActivity(
                                Intent(this@MatrixGUIMainActivity, MainActivity::class.java)
                                    .putExtra("businessId", businessId)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        },
                        onNavControllerCreated = { controller ->
                            navController = controller
                        }
                    )
                }
            }
        }

        // Setup back navigation handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Timber.d("GUI3: Back button pressed")
                if (navController?.popBackStack() == false) {
                    // If we can't pop (at root), then finish the activity
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    Timber.d("GUI3: Back button pressed at root, finishing activity")
                } else {
                    Timber.d("GUI3: Back button navigated to previous screen")
                }
            }
        })
    }

    companion object {
        fun createIntent(context: Context, businessId: Long = 1L): Intent {
            return Intent(context, MatrixGUIMainActivity::class.java).apply {
                putExtra("businessId", businessId)
            }
        }
    }
}

/**
 * Navigation Graph for Matrix GUI (GUI3)
 *
 * Defines all screens and navigation flows for the Matrix UI.
 */
@Composable
fun MatrixGUINavigation(
    businessId: Long = 1L,
    onSwitchToGui1: () -> Unit = {},
    onSwitchToGui2: () -> Unit = {},
    onNavControllerCreated: (androidx.navigation.NavHostController) -> Unit = {}
) {
    println("🟣 STARTUP: MatrixGUINavigation composing with businessId=$businessId")
    val navController = rememberNavController()

    // Notify parent of navController creation
    androidx.compose.runtime.LaunchedEffect(navController) {
        onNavControllerCreated(navController)
    }

    NavHost(
        navController = navController,
        startDestination = ScreenV3.Dashboard(businessId = businessId),
        modifier = Modifier.fillMaxSize()
    ) {
         // Dashboard
         composable<ScreenV3.Dashboard> { backStackEntry ->
             println("🟤 STARTUP: Dashboard route MATCHED! Rendering DashboardScreenV3")
             val route: ScreenV3.Dashboard = backStackEntry.toRoute()
             println("🟤 STARTUP: route extracted successfully: businessId=${route.businessId}")
             DashboardScreenV3(
                 businessId = route.businessId,
                 navController = navController,
                 onSwitchToGui1 = onSwitchToGui1,
                 onSwitchToGui2 = onSwitchToGui2
             )
         }

        // Invoices List
        composable<ScreenV3.Invoices> { backStackEntry ->
            val route: ScreenV3.Invoices = backStackEntry.toRoute()
            InvoiceListScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Invoice Detail
        composable<ScreenV3.InvoiceDetail> { backStackEntry ->
            val route: ScreenV3.InvoiceDetail = backStackEntry.toRoute()
            InvoiceDetailScreenV3(
                businessId = route.businessId,
                invoiceId = route.invoiceId,
                navController = navController
            )
        }

        // Create Invoice
        composable<ScreenV3.CreateInvoice> { backStackEntry ->
            val route: ScreenV3.CreateInvoice = backStackEntry.toRoute()
            CreateInvoiceScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Edit Invoice
        composable<ScreenV3.EditInvoice> { backStackEntry ->
            val route: ScreenV3.EditInvoice = backStackEntry.toRoute()
            EditInvoiceScreenV3(
                businessId = route.businessId,
                invoiceId = route.invoiceId,
                navController = navController
            )
        }

        // Customers List
        composable<ScreenV3.Customers> { backStackEntry ->
            val route: ScreenV3.Customers = backStackEntry.toRoute()
            CustomerListScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Customer Detail
        composable<ScreenV3.CustomerDetail> { backStackEntry ->
            val route: ScreenV3.CustomerDetail = backStackEntry.toRoute()
            CustomerDetailScreenV3(
                businessId = route.businessId,
                customerId = route.customerId,
                navController = navController
            )
        }

        // Create Customer
        composable<ScreenV3.CreateCustomer> { backStackEntry ->
            val route: ScreenV3.CreateCustomer = backStackEntry.toRoute()
            CreateCustomerScreenV3(
                customerId = null,
                navController = navController
            )
        }

        // Edit Customer
        composable<ScreenV3.EditCustomer> { backStackEntry ->
            val route: ScreenV3.EditCustomer = backStackEntry.toRoute()
            EditCustomerScreenV3(
                businessId = route.businessId,
                customerId = route.customerId,
                navController = navController
            )
        }

        // Payment Tracking
        composable<ScreenV3.PaymentTracking> { backStackEntry ->
            val route: ScreenV3.PaymentTracking = backStackEntry.toRoute()
            PaymentTrackingScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Revenue Analytics
        composable<ScreenV3.RevenueAnalytics> { backStackEntry ->
            val route: ScreenV3.RevenueAnalytics = backStackEntry.toRoute()
            RevenueAnalyticsScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Payment Analytics
        composable<ScreenV3.PaymentAnalytics> { backStackEntry ->
            val route: ScreenV3.PaymentAnalytics = backStackEntry.toRoute()
            PaymentAnalyticsScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Settings
        composable<ScreenV3.Settings> { backStackEntry ->
            val route: ScreenV3.Settings = backStackEntry.toRoute()
            SettingsScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Reports
        composable<ScreenV3.Reports> { backStackEntry ->
            val route: ScreenV3.Reports = backStackEntry.toRoute()
            ReportsScreenV3(
                businessId = route.businessId,
                navController = navController
            )
        }

        // Prefilled Items
        composable<ScreenV3.PrefilledItems> { backStackEntry ->
            val route: ScreenV3.PrefilledItems = backStackEntry.toRoute()
            PrefilledItemsScreen()
        }

         // Help
         composable<ScreenV3.Help> { backStackEntry ->
             val route: ScreenV3.Help = backStackEntry.toRoute()
             HelpScreenV3(
                 businessId = route.businessId,
                 navController = navController
             )
         }

         // PDF Viewer (Phase 4.1)
         composable<ScreenV3.ViewPdf> { backStackEntry ->
             val route: ScreenV3.ViewPdf = backStackEntry.toRoute()
             PdfViewerScreenV3(
                 businessId = route.businessId,
                 invoiceId = route.invoiceId,
                 pdfPath = route.pdfPath,
                 navController = navController
             )
         }

         // Vault (Phase 4.2)
         composable<ScreenV3.Vault> { backStackEntry ->
             val route: ScreenV3.Vault = backStackEntry.toRoute()
             VaultScreenV3(
                 businessId = route.businessId,
                 navController = navController
             )
         }

         // Security Vault (info-rich GUI3 vault)
         composable<ScreenV3.SecurityVault> { backStackEntry ->
             val route: ScreenV3.SecurityVault = backStackEntry.toRoute()
             SecurityVaultScreenV3(
                 businessId = route.businessId,
                 navController = navController
             )
         }
     }
 }
