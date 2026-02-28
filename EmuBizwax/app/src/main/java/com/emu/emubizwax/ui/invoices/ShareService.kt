package com.emu.emubizwax.ui.invoices

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ShareService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun sharePdf(file: File, customerName: String) {
        // 1. Generate the secure content:// URI
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // 2. Create the Intent
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice for $customerName")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached invoice/quote.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. Launch the Share Sheet
        val chooser = Intent.createChooser(intent, "Share Invoice via...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
