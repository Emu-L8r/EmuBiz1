package com.emul8r.bizap.data.health

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.dao.CustomerAnalyticsDao
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Health check service for analytics snapshot consistency.
 *
 * Verifies that:
 * - All invoices have corresponding analytics snapshots
 * - All invoices have payment snapshots
 * - No orphaned snapshots exist
 * - Snapshot data is consistent
 */
@Singleton
class SnapshotHealthCheck @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val analyticsDao: AnalyticsDao,
    private val paymentDao: InvoicePaymentDao,
    private val customerAnalyticsDao: CustomerAnalyticsDao
) {

    /**
     * Performs comprehensive health check on all snapshots.
     *
     * @return SnapshotHealthReport with detailed status and recommendations
     */
    suspend fun checkHealth(): SnapshotHealthReport {
        return try {
            Timber.d("🏥 Starting snapshot health check...")

            // Check invoice snapshots
            val invoiceHealth = checkInvoiceSnapshots()

            // Check payment snapshots
            val paymentHealth = checkPaymentSnapshots()

            // Check customer snapshots
            val customerHealth = checkCustomerSnapshots()

            // Aggregate results
            val isHealthy = invoiceHealth.isHealthy && paymentHealth.isHealthy && customerHealth.isHealthy
            val allIssues = listOfNotNull(
                if (!invoiceHealth.isHealthy) invoiceHealth.issue else null,
                if (!paymentHealth.isHealthy) paymentHealth.issue else null,
                if (!customerHealth.isHealthy) customerHealth.issue else null
            )

            val report = SnapshotHealthReport(
                timestamp = System.currentTimeMillis(),
                isHealthy = isHealthy,
                invoiceSnapshots = invoiceHealth,
                paymentSnapshots = paymentHealth,
                customerSnapshots = customerHealth,
                overallIssues = allIssues,
                recommendations = generateRecommendations(
                    invoiceHealth,
                    paymentHealth,
                    customerHealth
                )
            )

            logHealthReport(report)
            report
        } catch (e: Exception) {
            Timber.e(e, "❌ Health check failed")
            SnapshotHealthReport(
                timestamp = System.currentTimeMillis(),
                isHealthy = false,
                invoiceSnapshots = SnapshotTypeHealth.Error(e.message ?: "Unknown error"),
                paymentSnapshots = SnapshotTypeHealth.Error("Skipped due to prior error"),
                customerSnapshots = SnapshotTypeHealth.Error("Skipped due to prior error"),
                overallIssues = listOf("Health check exception: ${e.message}"),
                recommendations = listOf("Check logs for exception details")
            )
        }
    }

    /**
     * Checks invoice analytics snapshots consistency.
     */
    private suspend fun checkInvoiceSnapshots(): SnapshotTypeHealth {
        val invoiceCount = invoiceDao.count()
        val snapshotCount = analyticsDao.countInvoiceSnapshots()
        val missingCount = invoiceCount - snapshotCount

        return if (invoiceCount == snapshotCount) {
            SnapshotTypeHealth.Healthy(
                totalRecords = invoiceCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = 0
            )
        } else if (missingCount > 0) {
            SnapshotTypeHealth.Unhealthy(
                totalRecords = invoiceCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = missingCount,
                issue = "Missing $missingCount invoice analytics snapshots",
                affectedIds = analyticsDao.getMissingInvoiceSnapshots()
            )
        } else {
            SnapshotTypeHealth.Unhealthy(
                totalRecords = invoiceCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = missingCount,
                issue = "Found ${snapshotCount - invoiceCount} orphaned invoice snapshots",
                affectedIds = analyticsDao.getOrphanedInvoiceSnapshots()
            )
        }
    }

    /**
     * Checks payment snapshots consistency.
     */
    private suspend fun checkPaymentSnapshots(): SnapshotTypeHealth {
        val invoiceCount = invoiceDao.count()
        val snapshotCount = paymentDao.countSnapshots()
        val missingCount = invoiceCount - snapshotCount

        return if (invoiceCount == snapshotCount) {
            SnapshotTypeHealth.Healthy(
                totalRecords = invoiceCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = 0
            )
        } else if (missingCount > 0) {
            SnapshotTypeHealth.Unhealthy(
                totalRecords = invoiceCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = missingCount,
                issue = "Missing $missingCount payment snapshots",
                affectedIds = paymentDao.getMissingSnapshots()
            )
        } else {
            SnapshotTypeHealth.Unhealthy(
                totalRecords = invoiceCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = missingCount,
                issue = "Found ${snapshotCount - invoiceCount} orphaned payment snapshots",
                affectedIds = paymentDao.getOrphanedSnapshots()
            )
        }
    }

    /**
     * Checks customer analytics snapshots consistency.
     */
    private suspend fun checkCustomerSnapshots(): SnapshotTypeHealth {
        val customerCount = invoiceDao.countDistinctCustomers()  // ✅ Changed: Use invoice customer count
        val snapshotCount = customerAnalyticsDao.countSnapshots()
        val missingCount = customerCount - snapshotCount

        return if (customerCount == snapshotCount) {
            SnapshotTypeHealth.Healthy(
                totalRecords = customerCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = 0
            )
        } else if (missingCount > 0) {
            SnapshotTypeHealth.Unhealthy(
                totalRecords = customerCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = missingCount,
                issue = "Missing $missingCount customer analytics snapshots",
                affectedIds = customerAnalyticsDao.getMissingSnapshots()
            )
        } else {
            SnapshotTypeHealth.Unhealthy(
                totalRecords = customerCount,
                totalSnapshots = snapshotCount,
                missingSnapshots = missingCount,
                issue = "Found ${snapshotCount - customerCount} orphaned customer snapshots",
                affectedIds = customerAnalyticsDao.getOrphanedSnapshots()
            )
        }
    }

    /**
     * Generates actionable recommendations based on health status.
     */
    private fun generateRecommendations(
        invoiceHealth: SnapshotTypeHealth,
        paymentHealth: SnapshotTypeHealth,
        customerHealth: SnapshotTypeHealth
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Invoice recommendations
        when (invoiceHealth) {
            is SnapshotTypeHealth.Unhealthy -> {
                if (invoiceHealth.missingSnapshots > 0) {
                    recommendations.add(
                        "Run migration to backfill ${invoiceHealth.missingSnapshots} missing invoice snapshots"
                    )
                } else {
                    recommendations.add(
                        "Delete ${-invoiceHealth.missingSnapshots} orphaned invoice snapshots"
                    )
                }
            }
            else -> {}
        }

        // Payment recommendations
        when (paymentHealth) {
            is SnapshotTypeHealth.Unhealthy -> {
                if (paymentHealth.missingSnapshots > 0) {
                    recommendations.add(
                        "Create ${paymentHealth.missingSnapshots} missing payment snapshots"
                    )
                } else {
                    recommendations.add(
                        "Delete ${-paymentHealth.missingSnapshots} orphaned payment snapshots"
                    )
                }
            }
            else -> {}
        }

        // Customer recommendations
        when (customerHealth) {
            is SnapshotTypeHealth.Unhealthy -> {
                if (customerHealth.missingSnapshots > 0) {
                    recommendations.add(
                        "Create ${customerHealth.missingSnapshots} missing customer snapshots"
                    )
                } else {
                    recommendations.add(
                        "Delete ${-customerHealth.missingSnapshots} orphaned customer snapshots"
                    )
                }
            }
            else -> {}
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✅ All snapshot health checks passed - no action needed")
        }

        return recommendations
    }

    /**
     * Logs health report in readable format.
     */
    private fun logHealthReport(report: SnapshotHealthReport) {
        if (report.isHealthy) {
            Timber.i("✅ SNAPSHOT HEALTH: ALL SYSTEMS HEALTHY")
        } else {
            Timber.w("⚠️ SNAPSHOT HEALTH: ISSUES DETECTED")
        }

        Timber.d("📊 Invoice Snapshots: ${describeHealth(report.invoiceSnapshots)}")
        Timber.d("💰 Payment Snapshots: ${describeHealth(report.paymentSnapshots)}")
        Timber.d("👥 Customer Snapshots: ${describeHealth(report.customerSnapshots)}")

        report.overallIssues.forEach { issue ->
            Timber.w("❌ $issue")
        }

        report.recommendations.forEach { rec ->
            Timber.i("💡 $rec")
        }
    }

    private fun describeHealth(health: SnapshotTypeHealth): String = when (health) {
        is SnapshotTypeHealth.Healthy -> "✅ ${health.totalRecords} records, ${health.totalSnapshots} snapshots (HEALTHY)"
        is SnapshotTypeHealth.Unhealthy -> "⚠️ ${health.totalRecords} records, ${health.totalSnapshots} snapshots, ${health.missingSnapshots} missing"
        is SnapshotTypeHealth.Error -> "❌ ${health.errorMessage}"
    }
}

/**
 * Represents health status of a specific snapshot type.
 */
sealed class SnapshotTypeHealth {
    data class Healthy(
        val totalRecords: Int,
        val totalSnapshots: Int,
        val missingSnapshots: Int
    ) : SnapshotTypeHealth() {
        val isHealthy = true
    }

    data class Unhealthy(
        val totalRecords: Int,
        val totalSnapshots: Int,
        val missingSnapshots: Int,
        val issue: String,
        val affectedIds: List<Long> = emptyList()
    ) : SnapshotTypeHealth() {
        val isHealthy = false
    }

    data class Error(val errorMessage: String) : SnapshotTypeHealth() {
        val isHealthy = false
    }
}

/**
 * Complete health report for all snapshots.
 */
data class SnapshotHealthReport(
    val timestamp: Long,
    val isHealthy: Boolean,
    val invoiceSnapshots: SnapshotTypeHealth,
    val paymentSnapshots: SnapshotTypeHealth,
    val customerSnapshots: SnapshotTypeHealth,
    val overallIssues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
) {
    fun toPrettyString(): String = buildString {
        appendLine("╔══════════════════════════════════════════════════════════╗")
        appendLine("║ SNAPSHOT HEALTH REPORT")
        appendLine("║ Timestamp: ${java.time.Instant.ofEpochMilli(timestamp)}")
        appendLine("║ Status: ${if (isHealthy) "✅ HEALTHY" else "⚠️ UNHEALTHY"}")
        appendLine("╠══════════════════════════════════════════════════════════╣")

        appendLine("║ INVOICE SNAPSHOTS")
        when (val health = invoiceSnapshots) {
            is SnapshotTypeHealth.Healthy -> {
                appendLine("║ ✅ Status: HEALTHY")
                appendLine("║ Total Records: ${health.totalRecords}")
                appendLine("║ Total Snapshots: ${health.totalSnapshots}")
            }
            is SnapshotTypeHealth.Unhealthy -> {
                appendLine("║ ⚠️ Status: UNHEALTHY")
                appendLine("║ Total Records: ${health.totalRecords}")
                appendLine("║ Total Snapshots: ${health.totalSnapshots}")
                appendLine("║ Missing: ${health.missingSnapshots}")
                appendLine("║ Issue: ${health.issue}")
            }
            is SnapshotTypeHealth.Error -> {
                appendLine("║ ❌ Error: ${health.errorMessage}")
            }
        }

        appendLine("╠══════════════════════════════════════════════════════════╣")
        appendLine("║ PAYMENT SNAPSHOTS")
        when (val health = paymentSnapshots) {
            is SnapshotTypeHealth.Healthy -> {
                appendLine("║ ✅ Status: HEALTHY")
                appendLine("║ Total Snapshots: ${health.totalSnapshots}")
            }
            is SnapshotTypeHealth.Unhealthy -> {
                appendLine("║ ⚠️ Status: UNHEALTHY")
                appendLine("║ Missing: ${health.missingSnapshots}")
                appendLine("║ Issue: ${health.issue}")
            }
            is SnapshotTypeHealth.Error -> {
                appendLine("║ ❌ Error: ${health.errorMessage}")
            }
        }

        appendLine("╠══════════════════════════════════════════════════════════╣")
        appendLine("║ CUSTOMER SNAPSHOTS")
        when (val health = customerSnapshots) {
            is SnapshotTypeHealth.Healthy -> {
                appendLine("║ ✅ Status: HEALTHY")
                appendLine("║ Total Snapshots: ${health.totalSnapshots}")
            }
            is SnapshotTypeHealth.Unhealthy -> {
                appendLine("║ ⚠️ Status: UNHEALTHY")
                appendLine("║ Missing: ${health.missingSnapshots}")
                appendLine("║ Issue: ${health.issue}")
            }
            is SnapshotTypeHealth.Error -> {
                appendLine("║ ❌ Error: ${health.errorMessage}")
            }
        }

        if (overallIssues.isNotEmpty()) {
            appendLine("╠══════════════════════════════════════════════════════════╣")
            appendLine("║ ISSUES")
            overallIssues.forEach { issue ->
                appendLine("║ • $issue")
            }
        }

        if (recommendations.isNotEmpty()) {
            appendLine("╠══════════════════════════════════════════════════════════╣")
            appendLine("║ RECOMMENDATIONS")
            recommendations.forEach { rec ->
                appendLine("║ • $rec")
            }
        }

        appendLine("╚══════════════════════════════════════════════════════════╝")
    }
}

