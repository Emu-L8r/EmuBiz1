package com.emul8r.bizap.data.service

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class PrintService @Inject constructor(@ApplicationContext private val context: Context) {

    fun printPdf(file: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Invoice Document"
        printManager.print(jobName, MyPrintDocumentAdapter(file), null)
    }

    private class MyPrintDocumentAdapter(private val file: File) : PrintDocumentAdapter() {

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

            val info = PrintDocumentInfo.Builder("invoice.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
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

            } catch (e: IOException) {
                callback.onWriteFailed(e.toString())
            } finally {
                try {
                    input?.close()
                    output?.close()
                } catch (e: IOException) {
                    // Ignore
                }
            }
        }
    }
}
