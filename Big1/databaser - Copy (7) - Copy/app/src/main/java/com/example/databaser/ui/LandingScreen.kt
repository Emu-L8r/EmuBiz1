package com.example.databaser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.databaser.data.PassphraseManager
import com.example.databaser.data.PassphraseStatus

@Composable
fun LandingScreen(
    passphraseManager: PassphraseManager,
    onPassphraseSet: () -> Unit
) {
    val passphraseStatus by passphraseManager.passphraseStatus.collectAsState()
    var enteredPassphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }

    when (passphraseStatus) {
        PassphraseStatus.SET -> {
            onPassphraseSet()
        }
        PassphraseStatus.NEEDS_CREATION -> {
            Scaffold {
                Column(
                    modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Create a Passphrase")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = enteredPassphrase,
                        onValueChange = { enteredPassphrase = it },
                        label = { Text("Passphrase") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = confirmPassphrase,
                        onValueChange = { confirmPassphrase = it },
                        label = { Text("Confirm Passphrase") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        if (enteredPassphrase.isNotBlank() && enteredPassphrase == confirmPassphrase) {
                            passphraseManager.setPassphrase(enteredPassphrase)
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
        PassphraseStatus.NOT_SET, PassphraseStatus.INVALID -> {
            Scaffold {
                Column(
                    modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Enter Passphrase")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = enteredPassphrase,
                        onValueChange = { enteredPassphrase = it },
                        label = { Text("Passphrase") }
                    )
                    if (passphraseStatus == PassphraseStatus.INVALID) {
                        Text("Invalid passphrase, please try again.")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { 
                        if(passphraseManager.checkPassphrase(enteredPassphrase)){
                            passphraseManager.setPassphrase(enteredPassphrase)
                        } else {
                            passphraseManager.passphraseStatus.value = PassphraseStatus.INVALID
                        }
                    }) {
                        Text("Unlock")
                    }
                }
            }
        }
    }
}