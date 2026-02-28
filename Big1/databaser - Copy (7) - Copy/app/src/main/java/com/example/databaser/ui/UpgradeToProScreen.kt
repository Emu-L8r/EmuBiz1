package com.example.databaser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.databaser.R

@Composable
fun UpgradeToProScreen(onUpgradeClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Upgrade to Pro", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Unlock all features by upgrading to the Pro version.")
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "- Create unlimited invoices")
        Text(text = "- More features coming soon!")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onUpgradeClick) {
            Text(text = "Upgrade Now")
        }
    }
}
