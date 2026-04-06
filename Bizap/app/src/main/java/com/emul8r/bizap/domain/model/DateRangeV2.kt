package com.emul8r.bizap.domain.model

import java.time.LocalDate

/**
 * Represents a range of dates for filtering metrics and reports.
 */
data class DateRangeV2(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String = ""
) {
    companion object {
        val TODAY: DateRangeV2 get() {
            val now = LocalDate.now()
            return DateRangeV2(now, now, "Today")
        }

        val THIS_WEEK: DateRangeV2 get() {
            val now = LocalDate.now()
            val dayOfWeek = now.dayOfWeek.value
            val startOfWeek = now.minusDays((dayOfWeek - 1).toLong())
            return DateRangeV2(startOfWeek, now, "This Week")
        }

        val THIS_MONTH: DateRangeV2 get() {
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1)
            return DateRangeV2(startOfMonth, now, "This Month")
        }

        val THIS_YEAR: DateRangeV2 get() {
            val now = LocalDate.now()
            val startOfYear = now.withDayOfYear(1)
            return DateRangeV2(startOfYear, now, "This Year")
        }
    }
}
