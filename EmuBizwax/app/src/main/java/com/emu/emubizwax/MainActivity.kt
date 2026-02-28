package com.emu.emubizwax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.emu.emubizwax.ui.navigation.AppBottomBar
import com.emu.emubizwax.ui.navigation.AppNavHost
import com.emu.emubizwax.ui.theme.InvoicingTheme
import com.emu.emubizwax.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val config by themeViewModel.themeConfig.collectAsState()
            val navController = rememberNavController()

            InvoicingTheme(themeConfig = config) {
                Scaffold(
                    bottomBar = { AppBottomBar(navController) }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
