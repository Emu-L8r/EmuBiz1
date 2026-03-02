package com.emul8r.bizap.data.service

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfPrintAdapter(private val file: File) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = android.print.PrintDocumentInfo.Builder("invoice.pdf")
            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        var input: FileInputStream? = null
        var output: FileOutputStream? = null

        try {
            input = FileInputStream(file)
            output = FileOutputStream(destination?.fileDescriptor)

            val buf = ByteArray(1024)
            var bytesRead: Int

            while (input.read(buf).also { bytesRead = it } > 0) {
                output.write(buf, 0, bytesRead)
            }

            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))

        } catch (e: Exception) {
            callback.onWriteFailed(e.toString())
        } finally {
            try {
                input?.close()
                output?.close()
            } catch (e: Exception) {
                // Ignored
            }
        }
    }
}

