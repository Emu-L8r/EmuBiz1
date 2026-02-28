package com.example.databaser.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.databaser.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySavesScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val documents by produceState<List<File>>(initialValue = emptyList(), lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val downloadsDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile?.resolve("Download")
            value = downloadsDir?.listFiles { file -> file.name.endsWith(".pdf") }?.toList() ?: emptyList()
        }
    }

    Scaffold {
        if (documents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.no_saved_documents))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it)) {
                items(documents) { document ->
                    Card(modifier = Modifier.padding(8.dp), onClick = {
                        val authority = "${context.packageName}.provider"
                        val uri = FileProvider.getUriForFile(context, authority, document)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }) {
                        ListItem(headlineContent = { Text(document.name) })
                    }
                }
            }
        }
    }
}

