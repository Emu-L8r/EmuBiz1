package com.emul8r.bizap.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentNamingUtils {
    fun generateFileName(customerName: String, date: Long, counter: Int, type: String): String {
        val sanitizedCustomerName = customerName.replace(Regex("[^A-Za-z0-9]"), "-")
        val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(date))
        val paddedCounter = String.format("%03d", counter)
        val prefix = if (type.equals("quote", ignoreCase = true)) "Quote" else "Invoice"
        return "${prefix}_${sanitizedCustomerName}_${formattedDate}_$paddedCounter.pdf"
    }
}
